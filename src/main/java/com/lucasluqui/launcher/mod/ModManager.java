package com.lucasluqui.launcher.mod;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.download.DownloadManager;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.launcher.LauncherContext;
import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.flamingo.data.Server;
import com.lucasluqui.launcher.mod.data.*;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsManager;
import com.lucasluqui.launcher.ui.LauncherUI;
import com.lucasluqui.launcher.ui.ModListUI;
import com.lucasluqui.util.*;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipFile;

import static com.lucasluqui.launcher.mod.Log.log;

@Singleton
public class ModManager
{
  public ModManager ()
  {
    // empty.
  }

  public void init ()
  {
    try (InputStream is = ModManager.class.getResourceAsStream("/rsrc/config/last-changed.properties")) {
      _lastChanged.load(is);
    } catch (IOException e) {
      log.error(e);
    }
  }

  public void checkInstalled ()
  {
    if (!_checking) {
      _checking = true;
      _ctx.getApp().toggleElementsBlock(true);
      ModListUI modListUI = _ctx.getApp().getUI(ModListUI.class);

      Server selectedServer = _flamingoManager.getSelectedServer();
      String modFolderPath = LauncherGlobals.USER_DIR + "/mods/";

      if (selectedServer != null) {
        modFolderPath = selectedServer.getModsDirectory();
      }

      // Clean both lists in case old data remains in it.
      if (getModCount() > 0) {
        clearModList();
        clearLocaleChanges();
      }

      // Append all .modpack, .zip, and .jar files inside the mod folder into an ArrayList.
      List<File> rawFiles = new ArrayList<>();
      rawFiles.addAll(FileUtil.filesInDirectory(modFolderPath, ".modpack"));
      rawFiles.addAll(FileUtil.filesInDirectory(modFolderPath, ".zip"));
      rawFiles.addAll(FileUtil.filesInDirectory(modFolderPath, ".jar"));

      for (File file : rawFiles) {
        String fileName = file.getName();
        Mod mod = null;
        if (fileName.endsWith("zip")) {
          mod = new ZipMod(
            modFolderPath, fileName, Settings.fileProtection ? this.FILTER_LIST : null, _lastChanged);
        } else if (fileName.endsWith("jar")) {
          mod = new JarMod(modFolderPath, fileName);
        } else if (fileName.endsWith("modpack")) {
          mod = new Modpack(modFolderPath, fileName);
        }

        if (mod != null) {
          _modList.addMod(mod);
        }
      }

      // Finally, let's see which have been set as disabled.
      parseDisabledMods();

      // Check if there's any incompatible mods.
      checkModCompatibility();

      // Mounting after a game update sometimes causes getdown to re-validate files again, making that
      // first mount essentially useless, so with this setting we make sure mods are force mounted at least 2 times
      // with the current known version.
      int forcedMountsForCurrentVersion = Integer.parseInt(
        _settingsManager.getValue("modloader.forcedMountsForCurrentVersion", selectedServer));
      if (forcedMountsForCurrentVersion < FORCED_MOUNTS_PER_VERSION) {
        log.info("Forced mounts quota for current version not met",
          "forcedMounts", forcedMountsForCurrentVersion);
        _rebuildRequired = true;
        _mountRequired = true;
      }

      // Check if there's a new or removed mod since the last execution, rebuild will be needed in that case.
      int previousModHash = Integer.parseInt(_settingsManager.getValue("modloader.appliedModsHash", selectedServer));
      int currentModHash = getEnabledModsHash();
      if (previousModHash != currentModHash) {
        log.info("Hashcode doesn't match, preparing for rebuild and remount...",
          "previous", previousModHash,
          "current", currentModHash
        );
        _rebuildRequired = true;
        _mountRequired = true;
      }

      // If the game version has changed since the last time mods were mounted, then we schedule a new mount.
      if (gameVersionChanged()) {
        _settingsManager.setValue("modloader.forcedMountsForCurrentVersion", "0", selectedServer);
        _rebuildRequired = true;
        _mountRequired = true;
      }

      // Check if there's directories in the mod folder and push a warning to the user.
      for (File file : FileUtil.filesAndDirectoriesInDirectory(modFolderPath)) {
        if (file.isDirectory()) {
          modListUI.eventHandler.showDirectoriesWarning(true);
          break;
        }
        modListUI.eventHandler.showDirectoriesWarning(false);
      }

      modListUI.labelModCount.setText(String.valueOf(getModList().size()));
      modListUI.updateModList(null);
      _ctx.getApp().toggleElementsBlock(false);
      _checking = false;
    }
  }

  public void mount ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();
    String rootDir = selectedServer.getRootDirectory();

    if (Settings.doRebuilds && _rebuildRequired) startFileRebuild();

    _ctx.getApp().toggleElementsBlock(true);

    _ctx._progressBar.startTask();
    _ctx._progressBar.setBarMax(getEnabledModCount());
    _ctx._progressBar.setState(_localeManager.getValue("m.mount"));
    _discordPresenceClient.setDetails(_localeManager.getValue("m.mount"));
    LinkedList<Mod> localList = getModList();

    for (int i = 0; i < getModCount(); i++) {
      Mod mod = localList.get(i);
      if (mod.isEnabled()) {
        if (mod instanceof Modpack) {
          ((Modpack) mod).mount(rootDir, FILTER_LIST, _lastChanged);
        } else if (mod instanceof ZipMod) {
          ZipMod zipMod = ((ZipMod) mod);
          zipMod.mount(rootDir, FILTER_LIST, _lastChanged);
          if (zipMod.hasLocaleChanges()) {
            this._localeChanges.addAll(zipMod.getLocaleChanges());
          }
        } else {
          mod.mount();
        }
        _ctx._progressBar.setBarValue(i + 1);
      }
    }

    _settingsManager.setValue("modloader.appliedModsHash", String.valueOf(getEnabledModsHash()), selectedServer);

    // Mount all the locale changes.
    if (!_localeChanges.isEmpty()) {
      log.info("Mounting locale changes...");
      try {
        // Unpack the current projectx-config jar file.
        ZipFile projectxConfig = new ZipFile(rootDir + "/code/projectx-config.jar");
        ZipUtil.unpackJar(projectxConfig, new File(rootDir + "/code/locale-changes/"), false);
        projectxConfig.close();

        // Create per-bundle batch of locale changes.
        HashMap<String, List<LocaleChange>> sortedLocaleChanges = new HashMap<>();
        for (LocaleChange localeChange : this._localeChanges) {
          if (!sortedLocaleChanges.containsKey(localeChange.getBundle())) {
            sortedLocaleChanges.put(localeChange.getBundle(), new ArrayList<>());
          }
          sortedLocaleChanges.get(localeChange.getBundle()).add(localeChange);
        }

        // Iterate through each bundle's changes.
        for (String bundle : sortedLocaleChanges.keySet()) {
          String bundlePath = rootDir + "/code/locale-changes/rsrc/i18n/" + bundle;

          Properties properties = new Properties();
          properties.load(Files.newInputStream(new File(bundlePath).toPath()));

          List<LocaleChange> localeChanges = sortedLocaleChanges.get(bundle);
          for (LocaleChange localeChange : localeChanges) {
            properties.setProperty(localeChange.getKey(), localeChange.getValue());
          }

          properties.store(Files.newOutputStream(new File(bundlePath).toPath()), null);
          properties.clear();
        }

        // Make sure there's no remnants of previous runs.
        try {
          FileUtils.delete(new File(rootDir + "/code/projectx-config-old.jar"));
          FileUtils.delete(new File(rootDir + "/code/projectx-config-new.jar"));
        } catch (Exception ignored) {
        }

        // Turn all the locale changes back into a jar file.
        try {
          JavaUtil.createJar(
            Paths.get(rootDir + "/code/locale-changes/"),
            Paths.get(rootDir + "/code/projectx-config-new.jar"),
            null
          );
        } catch (Exception e) {
          log.error(e);
        }

        // Delete the temporary directory used to store locale changes.
        FileUtils.deleteDirectory(new File(rootDir + "/code/locale-changes"));


        // Rename the current projectx-config to old and the new one to its original name.
        if (FileUtil.fileExists(rootDir + "/code/projectx-config-new.jar")) {
          FileUtils.moveFile(new File(rootDir + "/code/projectx-config.jar"), new File(rootDir + "/code/projectx-config-old.jar"));
          FileUtils.moveFile(new File(rootDir + "/code/projectx-config-new.jar"), new File(rootDir + "/code/projectx-config.jar"));
        }

        // And finally, remove the old one. We don't need to store it as we'll fetch the original from getdown
        // when a rebuild is triggered.
        FileUtils.delete(new File(rootDir + "/code/projectx-config-old.jar"));
        log.info("Locale changes mounted");
      } catch (Exception e) {
        log.error(e);
      }
    }

    // Mount all class changes into config.jar.
    // First unzip the current config.jar contents into the directory where class mods were mounted to.
    log.info("Mounting class changes...");
    try {
      ZipFile config = new ZipFile(rootDir + "/code/config.jar");
      ZipUtil.unpackJar(config, new File(rootDir + "/code/class-changes/"), false);
      config.close();
    } catch (IOException e) {
      log.error(e);
    }

    // Make sure there's no remnants of previous runs.
    try {
      FileUtils.delete(new File(rootDir + "/code/config-old.jar"));
      FileUtils.delete(new File(rootDir + "/code/config-new.jar"));
    } catch (Exception ignored) {
    }

    // And now after merging their contents, we turn it back into a jar.
    try {
      JavaUtil.createJar(
        Paths.get(rootDir + "/code/class-changes/"),
        Paths.get(rootDir + "/code/config-new.jar"),
        null
      );
    } catch (Exception e) {
      log.error(e);
    }

    try {
      // Delete the temporary directory used to store class changes.
      FileUtils.deleteDirectory(new File(rootDir + "/code/class-changes"));

      // Rename the current config to old and the new one to its original name.
      if (FileUtil.fileExists(rootDir + "/code/config-new.jar")) {
        FileUtils.moveFile(new File(rootDir + "/code/config.jar"), new File(rootDir + "/code/config-old.jar"));
        FileUtils.moveFile(new File(rootDir + "/code/config-new.jar"), new File(rootDir + "/code/config.jar"));
      }

      // And finally, remove the old one. We don't need to store it as we'll fetch the original from getdown
      // when a rebuild is triggered.
      FileUtils.delete(new File(rootDir + "/code/config-old.jar"));
      log.info("Class changes mounted");
    } catch (IOException e) {
      log.error(e);
    }

    // Make sure no cheat mod slips in.
    if (selectedServer.isOfficial()) extractSafeguard();

    // Clean the game from unwanted files.
    clean();

    // Update the last known game version.
    _settingsManager.setValue("modloader.lastKnownVersion", selectedServer.getLocalVersion(), selectedServer);

    // Increment the forced mount counter for this known version if it's below 2.
    int forcedMountsForCurrentVersion = Integer.parseInt(
      _settingsManager.getValue("modloader.forcedMountsForCurrentVersion"));
    if (forcedMountsForCurrentVersion < FORCED_MOUNTS_PER_VERSION) {
      forcedMountsForCurrentVersion++;
      _settingsManager.setValue("modloader.forcedMountsForCurrentVersion",
        Integer.toString(forcedMountsForCurrentVersion));
    }

    _mountRequired = false;
    _ctx._progressBar.finishTask();

    _ctx.getApp().toggleElementsBlock(false);
  }

  public void startFileRebuild ()
  {
    rebuildFiles(false);
  }

  public void startStrictFileRebuild ()
  {
    rebuildFiles(true);
  }

  private void rebuildFiles (boolean strict)
  {
    _ctx.getApp().toggleElementsBlock(true);

    String rootDir = LauncherGlobals.USER_DIR;
    String[] bundles = RSRC_BUNDLES;
    Server selectedServer = _flamingoManager.getSelectedServer();
    if (selectedServer != null) {
      rootDir = selectedServer.getRootDirectory();
      bundles = selectedServer.isOfficial() ? RSRC_BUNDLES : THIRDPARTY_RSRC_BUNDLES;
    }

    _ctx._progressBar.startTask();
    _ctx._progressBar.setBarMax(bundles.length + 1);

    // Clear the entirety of the rsrc folder leaving only the jar and zip bundles.
    if (Settings.filePurging) {
      _discordPresenceClient.setDetails(_localeManager.getValue("m.purge"));
      _ctx._progressBar.setState(_localeManager.getValue("m.purge"));
      FileUtil.purgeDirectory(new File(rootDir + "/rsrc/"), new String[]{".jar", ".jarv", ".zip"});
    }

    // Iterate through all bundles to rebuild the game files.
    _discordPresenceClient.setDetails(_localeManager.getValue("m.rebuild"));
    _ctx._progressBar.setState(_localeManager.getValue("m.rebuild"));

    for (int i = 0; i < bundles.length; i++) {
      _ctx._progressBar.setBarValue(i + 1);
      _discordPresenceClient.setDetails(_localeManager.getValue("presence.rebuilding", new String[]{String.valueOf(i + 1), String.valueOf(bundles.length)}));
      try {
        ZipUtil.unpackJar(new ZipFile(rootDir + "/rsrc/" + bundles[i]), new File(rootDir + "/rsrc/"), false);
      } catch (IOException e) {
        log.error(e);
      }
    }

    // Check for .xml configs present in the configs folder and delete them.
    List<String> configs = FileUtil.fileNamesInDirectory(rootDir + "/rsrc/config", ".xml");
    for (String config : configs) {
      new File(rootDir + "/rsrc/config/" + config).delete();
    }

    // Reset projectx-config to clear any locale changes.
    if (selectedServer.isOfficial()) {
      _discordPresenceClient.setDetails(_localeManager.getValue("m.reset_code"));
      _ctx._progressBar.setState(_localeManager.getValue("m.reset_code"));
      resetConfig();

      // And if we're on strict mode, we do the same for projectx-code too.
      if (strict) {
        resetCode();
      }
    }

    _ctx._progressBar.setBarValue(bundles.length + 1);
    _ctx._progressBar.finishTask();
    _rebuildRequired = false;

    _ctx.getApp().toggleElementsBlock(false);
    _discordPresenceClient.setDetails(_localeManager.getValue("presence.ready"));
  }

  public void extractSafeguard ()
  {
    try {
      log.info("Extracting safeguard...");
      ZipUtil.extractFileWithinJar("/rsrc/modules/safeguard/bundle.zip", LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip");
      ZipUtil.unzip(LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip", LauncherGlobals.USER_DIR + "/rsrc/");
      log.info("Extracted safeguard");
    } catch (IOException e) {
      log.info("Failed to extract safeguard");
      log.error(e);
    }
  }

  private int getEnabledModsHash ()
  {
    Set<Long> hashSet = new HashSet<>();
    String rootDir = _flamingoManager.getSelectedServer().getRootDirectory();
    for (Mod mod : _modList.getAll()) {
      long lastModified = new File(rootDir + "/mods/" + mod.getFileName()).lastModified();
      if (mod.isEnabled()) hashSet.add(lastModified);
    }

    return hashSet.hashCode();
  }

  private void clearModList ()
  {
    _modList.clear();
  }

  private void clearLocaleChanges ()
  {
    _localeChanges.clear();
  }

  private void parseDisabledMods ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();
    String rawString = _settingsManager.getValue("modloader.disabledMods", selectedServer);

    // No mods to parse as disabled, nothing to do.
    if (rawString.equals("")) return;

    String[] fileNames = rawString.split(",");
    for (Mod mod : _modList.getAll()) {
      for (int i = 0; i < fileNames.length; i++) {
        if (mod.getFileName().equals(fileNames[i])) {
          mod.setEnabled(false);
          break;
        }
      }
    }
  }

  private void clean ()
  {
    String rootDir = _flamingoManager.getSelectedServer().getRootDirectory();
    new File(rootDir + "/rsrc/mod.json").delete();
    new File(rootDir + "/rsrc/mod.png").delete();
    for (String filePath : FileUtil.fileNamesInDirectory(rootDir + "/mods", ".hash")) {
      new File(rootDir + "/mods/" + filePath).delete();
    }
  }

  private void checkModCompatibility ()
  {
    int gameJVMVersion = JavaUtil.getJVMVersion(JavaUtil.getGameJVMExePath());
    String pxVersion = _flamingoManager.getSelectedServer().getLocalVersion();

    for (Mod mod : _modList.getAll()) {
      // Parse whether the mod is compatible with the current game version.
      // Not a strict requirement for most mods, only for ZipMods with 'class' type and JarMods.
      boolean pxCompatible = pxVersion.equalsIgnoreCase(mod.getPXVersion());
      mod.setPXCompatible(pxCompatible);

      if (mod instanceof ZipMod) {
        ZipMod zipMod = (ZipMod) mod;

        // Parse any invalid file header warnings.
        if (zipMod.hasInvalidFileHeaders()) {
          List<String> protectedFileHeaders = new ArrayList<>();
          List<String> illegalFileHeaders = new ArrayList<>();
          List<String> outdatedFileHeaders = new ArrayList<>();

          for (Map.Entry<FileHeader, Integer> entry : zipMod.getFileHeaderData().getFileHeaders().entrySet()) {
            if (entry.getValue() == 1) {
              protectedFileHeaders.add(entry.getKey().getFileName());
            } else if (entry.getValue() == 2) {
              illegalFileHeaders.add(entry.getKey().getFileName());
            } else if (entry.getValue() == 3) {
              outdatedFileHeaders.add(entry.getKey().getFileName());
            }
          }

          if (!protectedFileHeaders.isEmpty()) {
            zipMod.addWarningMessage(_localeManager.getValue(
              "m.warning_file_headers",
              new String[]{
                String.join("\n", protectedFileHeaders),
                _localeManager.getValue("m.warning_file_headers_protected")
              })
            );
          }

          if (!illegalFileHeaders.isEmpty()) {
            zipMod.addWarningMessage(_localeManager.getValue(
              "m.warning_file_headers",
              new String[]{
                String.join("\n", illegalFileHeaders),
                _localeManager.getValue("m.warning_file_headers_illegal")
              })
            );
          }

          if (!outdatedFileHeaders.isEmpty()) {
            zipMod.addWarningMessage(_localeManager.getValue(
              "m.warning_file_headers",
              new String[]{
                String.join("\n", outdatedFileHeaders),
                _localeManager.getValue("m.warning_file_headers_outdated")
              })
            );
          }
        }

        // Parse any incompatible PX warnings.
        if (zipMod.getType() != null && zipMod.getType().equalsIgnoreCase("class")) {
          if (!pxCompatible) {
            zipMod.setEnabled(false);
            zipMod.addWarningMessage(_localeManager.getValue("m.warning_incompatible_px"));
          }
        } else if (zipMod.getType() == null) {
          // Resource mods don't need to match game version.
          if (zipMod.getType() == null) {
            mod.setPXCompatible(true);
          }
        }
      }

      if (mod instanceof JarMod) {
        JarMod jarMod = (JarMod) mod;

        // Disable any jar mod below the min JDK requirements or above the max JDK requirements.
        boolean jdkCompatible = gameJVMVersion >= jarMod.getMinJDKVersion() && gameJVMVersion <= jarMod.getMaxJDKVersion();
        jarMod.setJDKCompatible(jdkCompatible);

        if (jarMod.isEnabled()) jarMod.setEnabled(jdkCompatible && pxCompatible);

        if ((!jdkCompatible || !pxCompatible) && Settings.loadCodeMods) {
          if (!jdkCompatible)
            jarMod.addWarningMessage(_localeManager.getValue("m.warning_incompatible_jdk"));
          if (!pxCompatible)
            jarMod.addWarningMessage(_localeManager.getValue("m.warning_incompatible_px"));
        }
      }
    }
  }

  public boolean gameVersionChanged ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    String lastKnownVersion = _settingsManager.getValue("modloader.lastKnownVersion", selectedServer);
    String currentVersion = selectedServer.getLocalVersion();

    if (!lastKnownVersion.equalsIgnoreCase(currentVersion)) {
      log.info("Game version changed", "old", lastKnownVersion, "new", currentVersion);
    }

    return !lastKnownVersion.equalsIgnoreCase(currentVersion);
  }

  private void resetConfig ()
  {
    log.info("Resetting config jars...");
    URLDownloadQueue downloadQueue = new URLDownloadQueue("Reset config jars");
    try {
      downloadQueue.addToQueue(
        new URL("http://gamemedia2.spiralknights.com/spiral/" + _flamingoManager.getLocalGameVersion() + "/code/projectx-config.jar"),
        new File(LauncherGlobals.USER_DIR + "/code/", "projectx-config.jar")
      );
      downloadQueue.addToQueue(
        new URL("http://gamemedia2.spiralknights.com/spiral/" + _flamingoManager.getLocalGameVersion() + "/code/config.jar"),
        new File(LauncherGlobals.USER_DIR + "/code/", "config.jar")
      );
    } catch (MalformedURLException e) {
      log.error(e);
    }
    _downloadManager.add(downloadQueue);
    _downloadManager.processQueues();
  }

  private void resetCode ()
  {
    log.info("Resetting code jar...");
    URLDownloadQueue downloadQueue = new URLDownloadQueue("Reset code jar");
    try {
      downloadQueue.addToQueue(
        new URL("http://gamemedia2.spiralknights.com/spiral/" + _flamingoManager.getLocalGameVersion() + "/code/projectx-pcode.jar"),
        new File(LauncherGlobals.USER_DIR + "/code/", "projectx-pcode.jar")
      );
    } catch (MalformedURLException e) {
      log.error(e);
    }
    _downloadManager.add(downloadQueue);
    _downloadManager.processQueues();
  }

  public int getModCount ()
  {
    return _modList.getModCount();
  }

  public int getEnabledModCount ()
  {
    return _modList.getEnabledModCount();
  }

  public LinkedList<Mod> getModList ()
  {
    return _modList.getAll();
  }

  public Boolean getMountRequired ()
  {
    return this._mountRequired;
  }

  public void setMountRequired (Boolean mountRequired)
  {
    this._mountRequired = mountRequired;
  }

  public Boolean getRebuildRequired ()
  {
    return this._rebuildRequired;
  }

  public void setRebuildRequired (Boolean rebuildRequired)
  {
    this._rebuildRequired = rebuildRequired;
  }

  public Properties getLastChanged ()
  {
    return this._lastChanged;
  }

  /**
   * A class to centralize all queries to the mod list in one place.
   * <code>Mod</code> objects within can be modified outside of it...
   * TODO Prevent modification outside of this class.
   */
  @SuppressWarnings("all")
  protected class ModList
  {
    /** The shiny toy. */
    private ModList ()
    {
      this.mods = new LinkedList<>();
    }

    /**
     * Adds a <code>Mod</code> to the list.
     * @param mod The <code>Mod</code> to be added.
     */
    private void addMod (Mod mod)
    {
      this.mods.add(mod);
      mod.wasAdded();
    }

    /**
     * Clears the <code>LinkedList</code> within this class.
     */
    private void clear ()
    {
      this.mods.clear();
    }

    /**
     * Get the number of <code>Mod</code> objects in the list.
     * @return Number of <code>Mod</code> objects within the <code>LinkedList</code>.
     */
    private int getModCount ()
    {
      return this.mods.size();
    }

    /**
     * Get the number of ENABLED <code>Mod</code> objects in the list.
     * @return Number of <code>Mod</code> objects within the <code>LinkedList</code> that are set as enabled.
     */
    private int getEnabledModCount ()
    {
      int count = 0;
      for (Mod mod : this.mods) {
        if (mod.isEnabled())
          count++;
      }
      return count;
    }

    /**
     * Get the <code>LinkedList</code> within this class.
     * This does not sound like a good idea. Everything should be handled in here.
     * But I don't have the time to refactor that whole thing. So for now it stays this way.
     * @return The <code>LinkedList</code> with all <code>Mod</code> objects.
     */
    private LinkedList<Mod> getAll ()
    {
      return this.mods;
    }

    /**
     * Get a CLONE of the <code>LinkedList</code> within this class.
     * This is how it should work!
     * @return A clone of the <code>LinkedList</code> with all <code>Mod</code> objects.
     */
    private LinkedList<Mod> getClone ()
    {
      // We don't want to return the actual object, so let's clone it first.
      return new LinkedList<Mod>(this.mods);
    }

    /** A <code>LinkedList</code> holding all <code>Mod</code> objects. */
    private final LinkedList<Mod> mods;
  }

  /** The launcher's context class. */
  @Inject protected LauncherContext _ctx;

  /** Locale manager, for handling i18n bits. */
  @Inject protected LocaleManager _localeManager;

  /** Manages the launcher user settings. */
  @Inject protected SettingsManager _settingsManager;

  /** Communicates with the launcher's backend server. */
  @Inject protected FlamingoManager _flamingoManager;

  /** Handles file downloading launcher-wide */
  @Inject protected DownloadManager _downloadManager;

  /** Discord RPC client. */
  @Inject protected DiscordPresenceClient _discordPresenceClient;

  /** Holds a list of all mods. */
  protected final ModList _modList = new ModList();

  /** Holds a list of all LocaleChange objects across all mods. */
  protected final List<LocaleChange> _localeChanges = new ArrayList<>();

  /** Game files mapped to their last modified stamp
   * to prioritize newer vanilla files over modded ones. */
  protected final Properties _lastChanged = new Properties();

  /** Flags whether a mod mounting is required. */
  protected Boolean _mountRequired = false;

  /** Flags whether a file rebuild is required. */
  protected Boolean _rebuildRequired = false;

  /** Flags whether we're currently parsing files in the mods directory. */
  protected Boolean _checking = false;

  /** Number of times a forced mod mount will be required for a newly detected
   * client version. */
  private final int FORCED_MOUNTS_PER_VERSION = 2;

  /** Resource bundles. */
  private final String[] RSRC_BUNDLES = {
    "full-music-bundle.jar",
    "full-rest-bundle.jar",
    "intro-bundle.jar"
  };

  /** Resource bundles, but for third party servers. */
  private final String[] THIRDPARTY_RSRC_BUNDLES = {
    "base.zip"
  };

  /** Nasty files that very often collide with updates. */
  private final String[] FILTER_LIST = new String[] {
    "config/accessory.dat",
    "config/accessory.xml",
    "config/actor.dat",
    "config/actor.xml",
    "config/area.dat",
    "config/area.xml",
    "config/attack.dat",
    "config/attack.xml",
    "config/battle_sprite.dat",
    "config/battle_sprite.xml",
    //"config/behavior.dat",
    //"config/behavior.xml",
    "config/catalog.dat",
    "config/catalog.xml",
    "config/conversation.dat",
    "config/conversation.xml",
    "config/cursor.dat",
    "config/cursor.xml",
    "config/depot_catalog.dat",
    "config/depot_catalog.xml",
    "config/depth_scale.dat",
    "config/depth_scale.xml",
    "config/description.dat",
    "config/description.xml",
    "config/effect.dat",
    "config/effect.xml",
    "config/emote.dat",
    "config/emote.xml",
    "config/event.dat",
    "config/event.xml",
    "config/fire_action.dat",
    "config/fire_action.xml",
    "config/font.dat",
    "config/font.xml",
    "config/forge_property.dat",
    "config/forge_property.xml",
    "config/gift.dat",
    "config/gift.xml",
    "config/ground.dat",
    "config/ground.xml",
    "config/harness.dat",
    "config/harness.xml",
    "config/interact.dat",
    "config/interact.xml",
    "config/interface_script.dat",
    "config/interface_script.xml",
    "config/item.dat",
    "config/item.xml",
    "config/item_depth_weight.dat",
    "config/item_depth_weight.xml",
    "config/item_property.dat",
    "config/item_property.xml",
    "config/level_table.dat",
    "config/level_table.xml",
    "config/material.dat",
    "config/material.xml",
    "config/mission.dat",
    "config/mission.xml",
    "config/mission_group.dat",
    "config/mission_group.xml",
    "config/mission_property.dat",
    "config/mission_property.xml",
    "config/parameterized_handler.dat",
    "config/parameterized_handler.xml",
    "config/path.dat",
    "config/path.xml",
    "config/placeable.dat",
    "config/placeable.xml",
    "config/recipe.dat",
    "config/recipe.xml",
    "config/recipe_property.dat",
    "config/recipe_property.xml",
    "config/render_effect.dat",
    "config/render_effect.xml",
    "config/render_queue.dat",
    "config/render_queue.xml",
    "config/render_scheme.dat",
    "config/render_scheme.xml",
    "config/scene_global.dat",
    "config/scene_global.xml",
    "config/shader.dat",
    "config/shader.xml",
    "config/sounder.dat",
    "config/sounder.xml",
    //"config/spawn_table.dat",
    //"config/spawn_table.xml",
    "config/status_condition.dat",
    "config/status_condition.xml",
    "config/style.dat",
    "config/style.xml",
    "config/texture.dat",
    "config/texture.xml",
    "config/tile.dat",
    "config/tile.xml",
    "config/tile_replacement.dat",
    "config/tile_replacement.xml",
    "config/uplink.dat",
    "config/uplink.xml",
    "config/variant.dat",
    "config/variant.xml",
    //"config/variant_table.dat",
    //"config/variant_table.xml",
    "config/wall.dat",
    "config/wall.xml",
  };

}
