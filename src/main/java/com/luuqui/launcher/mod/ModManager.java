package com.luuqui.launcher.mod;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.download.DownloadManager;
import com.luuqui.download.data.URLDownloadQueue;
import com.luuqui.launcher.*;
import com.luuqui.launcher.LocaleManager;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.data.*;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsManager;
import com.luuqui.util.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipFile;

import static com.luuqui.launcher.mod.Log.log;

@Singleton
public class ModManager
{
  @Inject protected LauncherContext _launcherCtx;
  @Inject protected LocaleManager _localeManager;
  @Inject protected SettingsManager _settingsManager;
  @Inject protected FlamingoManager _flamingoManager;
  @Inject protected DownloadManager _downloadManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;

  private final LinkedList<Mod> modList = new LinkedList<>();

  private final List<LocaleChange> globalLocaleChanges = new ArrayList<>();

  public Boolean mountRequired = false;
  public Boolean rebuildRequired = false;

  public Boolean checking = false;

  public ModManager ()
  {
    // empty.
  }

  public void init ()
  {
    // empty.
  }

  public void checkInstalled ()
  {
    if (!checking) {
      checking = true;

      _launcherCtx.launcherGUI.eventHandler.updateServerSwitcher(true);

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
          mod = new ZipMod(modFolderPath, fileName);
        } else if (fileName.endsWith("jar")) {
          mod = new JarMod(modFolderPath, fileName);
        } else if (fileName.endsWith("modpack")) {
          mod = new Modpack(modFolderPath, fileName);
        }

        modList.add(mod);
        mod.wasAdded();
      }

      // Finally, let's see which have been set as disabled.
      parseDisabledMods();

      // Check if there's a new or removed mod since the last execution, rebuild will be needed in that case.
      if (Integer.parseInt(_settingsManager.getValue("modloader.appliedModsHash", selectedServer)) != getEnabledModsHash()) {
        log.info("Hashcode doesn't match, preparing for rebuild and remount...");
        rebuildRequired = true;
        mountRequired = true;
      }

      if (gameVersionChanged()) {
        log.info("Game version has changed", "new", selectedServer.getLocalVersion());
        rebuildRequired = true;
        mountRequired = true;
      }

      checkModCompatibility();

      // Check if there's directories in the mod folder and push a warning to the user.
      for (File file : FileUtil.filesAndDirectoriesInDirectory(modFolderPath)) {
        if(file.isDirectory()) {
          _launcherCtx.modListGUI.eventHandler.showDirectoriesWarning(true);
          break;
        }
        _launcherCtx.modListGUI.eventHandler.showDirectoriesWarning(false);
      }

      _launcherCtx.modListGUI.labelModCount.setText(String.valueOf(getModList().size()));
      _launcherCtx.modListGUI.updateModList(null);
      _launcherCtx.launcherGUI.eventHandler.updateServerSwitcher(false);
      checking = false;
    }
  }

  public void mount ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();
    String rootDir = selectedServer.getRootDirectory();

    if (Settings.doRebuilds && rebuildRequired) startFileRebuild();

    _launcherCtx.launcherGUI.eventHandler.updateServerSwitcher(true);
    _launcherCtx.launcherGUI.launchButton.setEnabled(false);
    _launcherCtx.launcherGUI.settingsButton.setEnabled(false);
    _launcherCtx.launcherGUI.modButton.setEnabled(false);

    _launcherCtx._progressBar.startTask();
    _launcherCtx._progressBar.setBarMax(getEnabledModCount());
    _launcherCtx._progressBar.setState(_localeManager.getValue("m.mount"));
    _discordPresenceClient.setDetails(_localeManager.getValue("m.mount"));
    LinkedList<Mod> localList = getModList();
    Set<Long> hashSet = new HashSet<>();

    for (int i = 0; i < getModCount(); i++) {
      Mod mod = localList.get(i);
      if(mod.isEnabled()) {
        if (mod instanceof Modpack) {
          ((Modpack) mod).mount(rootDir);
        } else if (mod instanceof ZipMod) {
          ZipMod zipMod = ((ZipMod) mod);
          zipMod.mount(rootDir);
          if (zipMod.hasLocaleChanges()) {
            this.globalLocaleChanges.addAll(zipMod.getLocaleChanges());
          }
        } else {
          mod.mount();
        }
        long lastModified = new File(rootDir + "/mods/" + mod.getFileName()).lastModified();
        hashSet.add(lastModified);
        _launcherCtx._progressBar.setBarValue(i + 1);
      }
    }

    _settingsManager.setValue("modloader.appliedModsHash", Integer.toString(hashSet.hashCode()), selectedServer);

    // Mount all the locale changes.
    if (!globalLocaleChanges.isEmpty()) {
      log.info("Mounting locale changes...");
      try {
        // Unpack the current projectx-config jar file.
        ZipFile projectxConfig = new ZipFile(rootDir + "/code/projectx-config.jar");
        FileUtil.unpackJar(projectxConfig, new File(rootDir + "/code/locale-changes/"), false);
        projectxConfig.close();

        // Create per-bundle batch of locale changes.
        HashMap<String, List<LocaleChange>> sortedLocaleChanges = new HashMap<>();
        for (LocaleChange localeChange : this.globalLocaleChanges) {
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

        // Turn all the locale changes back into a jar file.
        String[] outputCapture;
        if (SystemUtil.isWindows()) {
          outputCapture = ProcessUtil.runAndCapture(new String[] { "cmd.exe", "/C", JavaUtil.getGameJVMDirPath() + "/bin/jar.exe", "cvf", "code/projectx-config-new.jar", "-C", "code/locale-changes/", "." });
        } else {
          outputCapture = ProcessUtil.runAndCapture(new String[] { "/bin/bash", "-c", JavaUtil.getGameJVMDirPath() + "/bin/jar", "cvf", "code/projectx-config-new.jar", "-C", "code/locale-changes/", "." });
        }
        log.debug("Locale changes capture, stdout=", outputCapture[0], "stderr=", outputCapture[1]);

        // Delete the temporary directory used to store locale changes.
        FileUtils.deleteDirectory(new File(rootDir + "/code/locale-changes"));

        // Rename the current projectx-config to old and the new one to its original name.
        FileUtils.moveFile(new File(rootDir + "/code/projectx-config.jar"), new File(rootDir + "/code/projectx-config-old.jar"));
        FileUtils.moveFile(new File(rootDir + "/code/projectx-config-new.jar"), new File(rootDir + "/code/projectx-config.jar"));

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
      FileUtil.unpackJar(config, new File(rootDir + "/code/class-changes/"), false);
      config.close();
    } catch (IOException e) {
      log.error(e);
    }

    // And now after merging their contents, we turn it back into config.jar.
    String[] outputCapture;
    if (SystemUtil.isWindows()) {
      outputCapture = ProcessUtil.runAndCapture(new String[] { "cmd.exe", "/C", JavaUtil.getGameJVMDirPath() + "/bin/jar.exe", "cvf", "code/config-new.jar", "-C", "code/class-changes/", "." });
    } else {
      outputCapture = ProcessUtil.runAndCapture(new String[] { "/bin/bash", "-c", JavaUtil.getGameJVMDirPath() + "/bin/jar", "cvf", "code/config-new.jar", "-C", "code/class-changes/", "." });
    }
    log.debug("Class changes capture, stdout=", outputCapture[0], "stderr=", outputCapture[1]);

    try {
      // Delete the temporary directory used to store class changes.
      FileUtils.deleteDirectory(new File(rootDir + "/code/class-changes"));

      // Rename the current config to old and the new one to its original name.
      FileUtils.moveFile(new File(rootDir + "/code/config.jar"), new File(rootDir + "/code/config-old.jar"));
      FileUtils.moveFile(new File(rootDir + "/code/config-new.jar"), new File(rootDir + "/code/config.jar"));

      // And finally, remove the old one. We don't need to store it as we'll fetch the original from getdown
      // when a rebuild is triggered.
      FileUtils.delete(new File(rootDir + "/code/config-old.jar"));
      log.info("Class changes mounted");
    } catch (IOException e) {
      log.error(e);
    }

    // Make sure no cheat mod slips in.
    if (!selectedServer.isOfficial()) extractSafeguard();

    // Clean the game from unwanted files.
    clean();

    // Update the last known game version.
    _settingsManager.setValue("modloader.lastKnownVersion", selectedServer.getLocalVersion(), selectedServer);

    mountRequired = false;
    _launcherCtx._progressBar.finishTask();

    _launcherCtx.launcherGUI.eventHandler.updateServerSwitcher(false);
    _launcherCtx.launcherGUI.launchButton.setEnabled(true);
    _launcherCtx.launcherGUI.settingsButton.setEnabled(true);
    _launcherCtx.launcherGUI.modButton.setEnabled(true);
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
    try {
      _launcherCtx.launcherGUI.eventHandler.updateServerSwitcher(true);
      _launcherCtx.launcherGUI.launchButton.setEnabled(false);
      _launcherCtx.launcherGUI.settingsButton.setEnabled(false);
      _launcherCtx.launcherGUI.modButton.setEnabled(false);
      _launcherCtx.settingsGUI.forceRebuildButton.setEnabled(false);
    } catch (Exception ignored) {}

    String rootDir = LauncherGlobals.USER_DIR;
    String[] bundles = RSRC_BUNDLES;
    Server selectedServer = _flamingoManager.getSelectedServer();
    if (selectedServer != null) {
      rootDir = selectedServer.getRootDirectory();
      bundles = selectedServer.isOfficial() ? RSRC_BUNDLES : THIRDPARTY_RSRC_BUNDLES;
    }

    _launcherCtx._progressBar.startTask();
    _launcherCtx._progressBar.setBarMax(bundles.length + 1);

    // Clear the entirety of the rsrc folder leaving only the jar and zip bundles.
    _discordPresenceClient.setDetails(_localeManager.getValue("m.purge"));
    _launcherCtx._progressBar.setState(_localeManager.getValue("m.purge"));
    FileUtil.purgeDirectory(new File(rootDir + "/rsrc/"), new String[] { ".jar", ".jarv", ".zip" });

    // Iterate through all bundles to rebuild the game files.
    _discordPresenceClient.setDetails(_localeManager.getValue("m.rebuild"));
    _launcherCtx._progressBar.setState(_localeManager.getValue("m.rebuild"));

    for (int i = 0; i < bundles.length; i++) {
      _launcherCtx._progressBar.setBarValue(i + 1);
      _discordPresenceClient.setDetails(_localeManager.getValue("presence.rebuilding", new String[] { String.valueOf(i + 1), String.valueOf(bundles.length) }));
      try {
        FileUtil.unpackJar(new ZipFile(rootDir + "/rsrc/" + bundles[i]), new File(rootDir + "/rsrc/"), false);
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
      _launcherCtx._progressBar.setState(_localeManager.getValue("m.reset_code"));
      resetConfig();

      // And if we're on strict mode, we do the same for projectx-code too.
      if (strict) {
        resetCode();
      }
    }

    _launcherCtx._progressBar.setBarValue(bundles.length + 1);
    _launcherCtx._progressBar.finishTask();
    rebuildRequired = false;

    try {
      _launcherCtx.launcherGUI.eventHandler.updateServerSwitcher(false);
      _launcherCtx.launcherGUI.launchButton.setEnabled(true);
      _launcherCtx.launcherGUI.settingsButton.setEnabled(true);
      _launcherCtx.launcherGUI.modButton.setEnabled(true);
      _launcherCtx.settingsGUI.forceRebuildButton.setEnabled(true);
    } catch (Exception ignored) {}

    _discordPresenceClient.setDetails(_localeManager.getValue("presence.launch_ready"));
  }

  public void extractSafeguard ()
  {
    try {
      log.info("Extracting safeguard...");
      FileUtil.extractFileWithinJar("/rsrc/modules/safeguard/bundle.zip", LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip");
      Compressor.unzip(LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip", LauncherGlobals.USER_DIR + "/rsrc/", false);
      log.info("Extracted safeguard.");
    } catch (IOException e) {
      log.error(e);
    }
  }

  public int getModCount ()
  {
    return modList.size();
  }

  public int getEnabledModCount ()
  {
    int count = 0;
    for(Mod mod : modList) {
      if(mod.isEnabled()) count++;
    }
    return count;
  }

  private int getEnabledModsHash ()
  {
    Set<Long> hashSet = new HashSet<>();
    for(Mod mod : modList) {
      long lastModified = new File(_flamingoManager.getSelectedServer().getRootDirectory() + "/mods/" + mod.getFileName()).lastModified();
      if(mod.isEnabled()) hashSet.add(lastModified);
    }
    return hashSet.hashCode();
  }

  public LinkedList<Mod> getModList ()
  {
    // We don't want to return the actual object, so let's clone it.
    return new LinkedList<>(modList);
  }

  private void clearModList ()
  {
    modList.clear();
  }

  private void clearLocaleChanges ()
  {
    globalLocaleChanges.clear();
  }

  private void parseDisabledMods ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();
    String rawString = _settingsManager.getValue("modloader.disabledMods", selectedServer);

    // No mods to parse as disabled, nothing to do.
    if (rawString.equals("")) return;

    String[] fileNames = rawString.split(",");
    for (Mod mod : modList) {
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

    boolean hasIncompatibility = false;
    for (Mod mod : modList) {
      // Parse whether the mod is compatible with the current game version.
      // Not a strict requirement for most mods, only for ZipMods with 'class' type and JarMods.
      boolean pxCompatible = pxVersion.equalsIgnoreCase(mod.getPXVersion());
      mod.setPXCompatible(pxCompatible);

      if (mod instanceof ZipMod) {
        ZipMod zipMod = (ZipMod) mod;

        if (zipMod.getType() != null && zipMod.getType().equalsIgnoreCase("class")) {
          if (!pxCompatible) {
            zipMod.setEnabled(false);
            hasIncompatibility = true;
          }
        }
      }

      if (mod instanceof JarMod) {
        JarMod jarMod = (JarMod) mod;

        // Disable any jar mod below the min JDK requirements or above the max JDK requirements.
        boolean jdkCompatible = gameJVMVersion >= jarMod.getMinJDKVersion() && gameJVMVersion <= jarMod.getMaxJDKVersion();
        jarMod.setJDKCompatible(jdkCompatible);

        if (jarMod.isEnabled()) jarMod.setEnabled(jdkCompatible && pxCompatible);

        if((!jdkCompatible || !pxCompatible) && Settings.loadCodeMods) {
          hasIncompatibility = true;
        }
      }
    }

    _launcherCtx.modListGUI.eventHandler.showIncompatibleModsWarning(hasIncompatibility);
  }

  private boolean gameVersionChanged ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    String lastKnownVersion = _settingsManager.getValue("modloader.lastKnownVersion", selectedServer);
    String currentVersion = selectedServer.getLocalVersion();

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

  @SuppressWarnings("all")
  private class ModList
  {
    private final LinkedList<Mod> installedMods;

    private ModList ()
    {
      this.installedMods = new LinkedList<>();
    }

    private void addMod (Mod mod)
    {
      if(mod.isEnabled()) this.installedMods.add(mod);
    }

    private int getModCount ()
    {
      return installedMods.size();
    }
  }

  private final String[] RSRC_BUNDLES = {
      "full-music-bundle.jar",
      "full-rest-bundle.jar",
      "intro-bundle.jar"
  };

  private final String[] THIRDPARTY_RSRC_BUNDLES = {
      "base.zip"
  };

}
