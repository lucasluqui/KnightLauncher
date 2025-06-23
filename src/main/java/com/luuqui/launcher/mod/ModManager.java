package com.luuqui.launcher.mod;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.launcher.*;
import com.luuqui.launcher.LocaleManager;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.data.JarMod;
import com.luuqui.launcher.mod.data.Mod;
import com.luuqui.launcher.mod.data.Modpack;
import com.luuqui.launcher.mod.data.ZipMod;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsManager;
import com.luuqui.util.Compressor;
import com.luuqui.util.FileUtil;
import com.luuqui.util.ImageUtil;
import com.luuqui.util.JavaUtil;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
  @Inject protected DiscordPresenceClient _discordPresenceClient;

  private final LinkedList<Mod> modList = new LinkedList<>();

  public Boolean mountRequired = false;
  public Boolean rebuildRequired = false;

  public Boolean checking = false;

  public ModManager ()
  {

  }

  public void init ()
  {

  }

  public void checkInstalled ()
  {
    if(!checking) {
      checking = true;

      _launcherCtx.launcherGUI.eventHandler.updateServerSwitcher(true);

      Server selectedServer = _flamingoManager.getSelectedServer();
      String selectedServerName = "";
      String modFolderPath = LauncherGlobals.USER_DIR + "/mods/";

      if (selectedServer != null) {
        selectedServerName = selectedServer.getSanitizedName();
        modFolderPath = selectedServer.getModsDirectory();
      }

      // Clean the list in case something remains in it.
      if (getModCount() > 0) clearModList();

      // Append all .modpack, .zip, and .jar files inside the mod folder into an ArrayList.
      List<File> rawFiles = new ArrayList<>();
      rawFiles.addAll(FileUtil.filesInDirectory(modFolderPath, ".modpack"));
      rawFiles.addAll(FileUtil.filesInDirectory(modFolderPath, ".zip"));
      rawFiles.addAll(FileUtil.filesInDirectory(modFolderPath, ".jar"));

      for (File file : rawFiles) {
        String fileName = file.getName();
        Mod mod = null;
        if (fileName.endsWith("zip")) {
          mod = new ZipMod(fileName);
        } else if (fileName.endsWith("jar")) {
          mod = new JarMod(fileName);
        } else if (fileName.endsWith("modpack")) {
          mod = new Modpack(fileName);
        }

        parseModData(mod);

        modList.add(mod);
        mod.wasAdded();
      }

      // Finally, let's see which have been set as disabled.
      parseDisabledMods();

      // Check if there's a new or removed mod since the last execution, rebuild will be needed in that case.
      String key = "modloader.appliedModsHash";
      if(!selectedServerName.equalsIgnoreCase("")) key += "_" + selectedServerName;
      if (Integer.parseInt(_settingsManager.getValue(key)) != getEnabledModsHash()) {
        log.info("Hashcode doesn't match, preparing for rebuild and remount...");
        rebuildRequired = true;
        mountRequired = true;
      }

      if(gameVersionChanged()) {
        log.info("Game version has changed", "new", _flamingoManager.getLocalGameVersion());
        rebuildRequired = true;
        mountRequired = true;
      }

      checkJarModsRequirements();

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
    String selectedServerName = selectedServer.getSanitizedName();

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
          ((Modpack) mod).mount(_flamingoManager.getSelectedServer().getRootDirectory());
        } else if (mod instanceof ZipMod) {
          ((ZipMod) mod).mount(_flamingoManager.getSelectedServer().getRootDirectory());
        } else {
          mod.mount();
        }
        long lastModified = new File(_flamingoManager.getSelectedServer().getRootDirectory() + "/mods/" + mod.getFileName()).lastModified();
        hashSet.add(lastModified);
        _launcherCtx._progressBar.setBarValue(i + 1);
      }
    }

    String key = "modloader.appliedModsHash";
    if(!selectedServerName.equalsIgnoreCase("")) key += "_" + selectedServerName;
    _settingsManager.setValue(key, Integer.toString(hashSet.hashCode()));

    // Make sure no cheat mod slips in.
    if(!_flamingoManager.getSelectedServer().isOfficial()) extractSafeguard();

    // Clean the game from unwanted files.
    clean();

    // Update the last known game version.
    String versionKey = "modloader.lastKnownVersion";
    if(!selectedServerName.equalsIgnoreCase("")) versionKey += "_" + selectedServerName;
    _settingsManager.setValue(versionKey, _flamingoManager.getLocalGameVersion());

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
    if(_flamingoManager.getSelectedServer() != null) {
      rootDir = _flamingoManager.getSelectedServer().getRootDirectory();
      bundles = _flamingoManager.getSelectedServer().isOfficial() ? RSRC_BUNDLES : THIRDPARTY_RSRC_BUNDLES;
    }

    _launcherCtx._progressBar.startTask();
    _launcherCtx._progressBar.setBarMax(bundles.length + 1);

    // Clear the entirety of the rsrc folder leaving only the jar and zip bundles.
    _discordPresenceClient.setDetails(_localeManager.getValue("m.purge"));
    _launcherCtx._progressBar.setState(_localeManager.getValue("m.purge"));
    FileUtil.purgeDirectory(new File(rootDir + "/rsrc/"), new String[] {".jar", ".jarv", ".zip"});

    // Iterate through all bundles to rebuild the game files.
    _discordPresenceClient.setDetails(_localeManager.getValue("m.rebuild"));
    _launcherCtx._progressBar.setState(_localeManager.getValue("m.rebuild"));

    for (int i = 0; i < bundles.length; i++) {
      _launcherCtx._progressBar.setBarValue(i + 1);
      _discordPresenceClient.setDetails(_localeManager.getValue("presence.rebuilding", new String[]{String.valueOf(i + 1), String.valueOf(bundles.length)}));
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

    // Strict requires resetting code and config jars to vanilla too.
    if (_flamingoManager.getSelectedServer().isOfficial() && strict) {
      _discordPresenceClient.setDetails(_localeManager.getValue("m.reset_code"));
      _launcherCtx._progressBar.setState(_localeManager.getValue("m.reset_code"));
      resetCode();
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
      FileUtil.extractFileWithinJar("/modules/safeguard/bundle.zip", LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip");
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

  private void parseDisabledMods ()
  {
    String selectedServerName = "";
    if(_flamingoManager.getSelectedServer() != null) selectedServerName = _flamingoManager.getSelectedServer().getSanitizedName();

    String key = "modloader.disabledMods";
    if(!selectedServerName.equalsIgnoreCase("")) key += "_" + selectedServerName;
    String rawString = _settingsManager.getValue(key);

    // No mods to parse as disabled, nothing to do.
    if(rawString.equals("")) return;

    String[] fileNames = rawString.split(",");
    for(Mod mod : modList) {
      for(int i = 0; i < fileNames.length; i++) {
        if(mod.getFileName().equals(fileNames[i])) {
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

  public void parseModData (Mod mod)
  {
    JSONObject modJson;
    try {
      modJson = new JSONObject(Compressor.readFileInsideZip(_flamingoManager.getSelectedServer().getRootDirectory() + "/mods/" + mod.getFileName(), "mod.json")).getJSONObject("mod");
    } catch (Exception e) {
      modJson = null;
    }

    if (mod != null && modJson != null) {
      mod.setDisplayName(modJson.getString("name"));
      mod.setDescription(modJson.getString("description"));
      mod.setAuthor(modJson.getString("author"));
      mod.setVersion(modJson.getString("version"));

      try {
        mod.setImage(modJson.getString("image"));
      } catch (Exception e) {
        try {
          mod.setImage(ImageUtil.imageToBase64(ImageIO.read(Compressor.getISFromFileInsideZip(_flamingoManager.getSelectedServer().getRootDirectory() + "/mods/" + mod.getFileName(), "mod.png"))));
        } catch (Exception e2) {
          mod.setImage(null);
        }
      }

      if(mod instanceof JarMod) {
        try {
          int minJDKVersion = !modJson.isNull("minJDKVersion") ? Integer.parseInt(modJson.getString("minJDKVersion")) : 8;
          int maxJDKVersion = !modJson.isNull("maxJDKVersion") ? Integer.parseInt(modJson.getString("maxJDKVersion")) : 8;
          String pxVersion = !modJson.isNull("pxVersion") ? modJson.getString("pxVersion") : "0";
          ((JarMod) mod).setMinJDKVersion(minJDKVersion);
          ((JarMod) mod).setMaxJDKVersion(maxJDKVersion);
          ((JarMod) mod).setPXVersion(pxVersion);
        } catch (JSONException ignored) {}
      }
    }
  }

  private void checkJarModsRequirements ()
  {
    int gameJVMVersion = JavaUtil.getJVMVersion(JavaUtil.getGameJVMExePath());
    String pxVersion = _flamingoManager.getSelectedServer() == null ? _flamingoManager.getLocalGameVersion() : _flamingoManager.getSelectedServer().version;

    boolean hasIncompatibility = false;
    for(Mod mod : modList) {
      if(mod instanceof JarMod) {
        // Disable any jar mod below the min JDK requirements or above the max JDK requirements.
        boolean jdkCompatible = gameJVMVersion >= ((JarMod) mod).getMinJDKVersion() && gameJVMVersion <= ((JarMod) mod).getMaxJDKVersion();
        ((JarMod) mod).setJDKCompatible(jdkCompatible);

        // Disable any jar mod that isn't compatible with this pcode version.
        boolean pxCompatible = pxVersion.equalsIgnoreCase(((JarMod) mod).getPXVersion());
        ((JarMod) mod).setPXCompatible(pxCompatible);

        if(mod.isEnabled()) mod.setEnabled(jdkCompatible && pxCompatible);

        if((!jdkCompatible || !pxCompatible) && Settings.loadCodeMods) {
          hasIncompatibility = true;
        }
      }
    }

    _launcherCtx.modListGUI.eventHandler.showIncompatibleCodeModsWarning(hasIncompatibility);
  }

  private boolean gameVersionChanged ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();
    String key = "modloader.lastKnownVersion";

    if(selectedServer != null) {
      key += _flamingoManager.getSelectedServer().isOfficial() ? "" : "_" + _flamingoManager.getSelectedServer().getSanitizedName();
    }

    String lastKnownVersion = _settingsManager.getValue(key);
    String currentVersion = _flamingoManager.getLocalGameVersion();

    if(!lastKnownVersion.equalsIgnoreCase(currentVersion)) {
      return true;
    }

    return false;
  }

  private void resetCode ()
  {
    int downloadAttempts = 0;
    boolean downloadCompleted = false;

    while (downloadAttempts <= 3 && !downloadCompleted) {
      downloadAttempts++;
      log.info("Resetting code jars", "attempts", downloadAttempts);
      try {
        FileUtils.copyURLToFile(
            new URL("http://gamemedia2.spiralknights.com/spiral/" + _flamingoManager.getLocalGameVersion() + "/code/projectx-pcode.jar"),
            new File(LauncherGlobals.USER_DIR + "/code/", "projectx-pcode.jar"),
            0,
            0
        );
        FileUtils.copyURLToFile(
            new URL("http://gamemedia2.spiralknights.com/spiral/" + _flamingoManager.getLocalGameVersion() + "/code/projectx-config.jar"),
            new File(LauncherGlobals.USER_DIR + "/code/", "projectx-config.jar"),
            0,
            0
        );
        downloadCompleted = true;
      } catch (IOException e) {
        // Keep retrying.
        log.error(e);
      }
    }
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
