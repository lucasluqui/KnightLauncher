package com.luuqui.launcher.mod;

import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.launcher.*;
import com.luuqui.launcher.Locale;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.data.JarMod;
import com.luuqui.launcher.mod.data.Mod;
import com.luuqui.launcher.mod.data.Modpack;
import com.luuqui.launcher.mod.data.ZipMod;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsGUI;
import com.luuqui.launcher.setting.SettingsProperties;
import com.luuqui.util.Compressor;
import com.luuqui.util.FileUtil;
import com.luuqui.util.ImageUtil;
import com.luuqui.util.JavaUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipFile;

import static com.luuqui.launcher.mod.Log.log;

public class ModLoader {

  private static final LinkedList<Mod> modList = new LinkedList<>();

  private static final String[] RSRC_BUNDLES = {
    "full-music-bundle.jar",
    "full-rest-bundle.jar",
    "intro-bundle.jar"
  };

  private static final String[] THIRDPARTY_RSRC_BUNDLES = {
    "base.zip"
  };

  public static Boolean mountRequired = false;
  public static Boolean rebuildRequired = false;

  public static Boolean checking = false;

  public static void checkInstalled() {
    if(!checking) {
      checking = true;

      LauncherEventHandler.updateServerSwitcher(true);

      Server selectedServer = LauncherApp.selectedServer;
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

      // Finally lets see which have been set as disabled.
      parseDisabledMods();

      // Check if there's a new or removed mod since last execution, rebuild will be needed in that case.
      String key = "modloader.appliedModsHash";
      if(!selectedServerName.equalsIgnoreCase("")) key += "_" + selectedServerName;
      if (Integer.parseInt(SettingsProperties.getValue(key)) != getEnabledModsHash()) {
        log.info("Hashcode doesn't match, preparing for rebuild and remount...");
        rebuildRequired = true;
        mountRequired = true;
      }

      checkJarModsJDKRequirements();

      // Check if there's directories in the mod folder and push a warning to the user.
      for (File file : FileUtil.filesAndDirectoriesInDirectory(modFolderPath)) {
        if(file.isDirectory()) {
          ModListEventHandler.showDirectoriesWarning(true);
          break;
        }
        ModListEventHandler.showDirectoriesWarning(false);
      }

      ModListGUI.labelModCount.setText(String.valueOf(ModLoader.getModList().size()));
      ModListGUI.updateModList(null);
      LauncherEventHandler.updateServerSwitcher(false);
      checking = false;
    }
  }

  public static void mount() {

    Server selectedServer = LauncherApp.selectedServer;
    String selectedServerName = selectedServer.getSanitizedName();

    if (Settings.doRebuilds && ModLoader.rebuildRequired) ModLoader.startFileRebuild();

    LauncherEventHandler.updateServerSwitcher(true);
    LauncherGUI.launchButton.setEnabled(false);
    LauncherGUI.settingsButton.setEnabled(false);
    LauncherGUI.modButton.setEnabled(false);

    ProgressBar.startTask();
    ProgressBar.setBarMax(getEnabledModCount() + 1);
    ProgressBar.setState(Locale.getValue("m.mount"));
    DiscordPresenceClient.getInstance().setDetails(Locale.getValue("m.mount"));
    LinkedList<Mod> localList = getModList();
    Set<Long> hashSet = new HashSet<>();

    for (int i = 0; i < getModCount(); i++) {
      if(localList.get(i).isEnabled()) {
        localList.get(i).mount();
        long lastModified = new File(localList.get(i).getAbsolutePath()).lastModified();
        hashSet.add(lastModified);
        ProgressBar.setBarValue(i + 1);
      }
    }

    String key = "modloader.appliedModsHash";
    if(!selectedServerName.equalsIgnoreCase("")) key += "_" + selectedServerName;
    SettingsProperties.setValue(key, Integer.toString(hashSet.hashCode()));

    // Make sure no cheat mod slips in.
    if(!LauncherApp.selectedServer.isOfficial()) extractSafeguard();

    // Clean the game from unwanted files.
    clean();

    mountRequired = false;
    ProgressBar.finishTask();

    LauncherEventHandler.updateServerSwitcher(false);
    LauncherGUI.launchButton.setEnabled(true);
    LauncherGUI.settingsButton.setEnabled(true);
    LauncherGUI.modButton.setEnabled(true);
  }

  public static void startFileRebuild() {
    rebuildFiles();
  }

  private static void rebuildFiles() {
    try {
      LauncherEventHandler.updateServerSwitcher(true);
      LauncherGUI.launchButton.setEnabled(false);
      LauncherGUI.settingsButton.setEnabled(false);
      LauncherGUI.modButton.setEnabled(false);
      SettingsGUI.forceRebuildButton.setEnabled(false);
    } catch (Exception ignored) {}

    String rootDir = LauncherGlobals.USER_DIR;
    String[] bundles = RSRC_BUNDLES;
    if(LauncherApp.selectedServer != null) {
      rootDir = LauncherApp.selectedServer.getRootDirectory();
      bundles = LauncherApp.selectedServer.isOfficial() ? RSRC_BUNDLES : THIRDPARTY_RSRC_BUNDLES;
    }

    ProgressBar.startTask();
    ProgressBar.setBarMax(bundles.length + 1);
    DiscordPresenceClient.getInstance().setDetails(Locale.getValue("m.clean"));
    ProgressBar.setState(Locale.getValue("m.clean"));

    // Iterate through all 3 bundles to clean up the game files.
    for (int i = 0; i < bundles.length; i++) {
      ProgressBar.setBarValue(i + 1);
      DiscordPresenceClient.getInstance().setDetails(Locale.getValue("presence.rebuilding", new String[]{String.valueOf(i + 1), String.valueOf(bundles.length)}));
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

    ProgressBar.setBarValue(bundles.length + 1);
    ProgressBar.finishTask();
    rebuildRequired = false;

    try {
      LauncherEventHandler.updateServerSwitcher(false);
      LauncherGUI.launchButton.setEnabled(true);
      LauncherGUI.settingsButton.setEnabled(true);
      LauncherGUI.modButton.setEnabled(true);
      SettingsGUI.forceRebuildButton.setEnabled(true);
    } catch (Exception ignored) {}

    DiscordPresenceClient.getInstance().setDetails(Locale.getValue("presence.launch_ready"));
  }

  public static void extractSafeguard() {
    try {
      log.info("Extracting safeguard...");
      FileUtil.extractFileWithinJar("/modules/safeguard/bundle.zip", LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip");
      Compressor.unzip(LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip", LauncherGlobals.USER_DIR + "/rsrc/", false);
      log.info("Extracted safeguard.");
    } catch (IOException e) {
      log.error(e);
    }
  }

  public static int getModCount() {
    return modList.size();
  }

  public static int getEnabledModCount() {
    int count = 0;
    for(Mod mod : modList) {
      if(mod.isEnabled()) count++;
    }
    return count;
  }

  private static int getEnabledModsHash() {
    Set<Long> hashSet = new HashSet<>();
    for(Mod mod : modList) {
      long lastModified = new File(mod.getAbsolutePath()).lastModified();
      if(mod.isEnabled()) hashSet.add(lastModified);
    }
    return hashSet.hashCode();
  }

  public static LinkedList<Mod> getModList() {
    // We don't want to return the actual object so let's clone it.
    return new LinkedList<>(modList);
  }

  private static void clearModList() {
    modList.clear();
  }

  private static void parseDisabledMods() {
    String selectedServerName = "";
    if(LauncherApp.selectedServer != null) selectedServerName = LauncherApp.selectedServer.getSanitizedName();

    String key = "modloader.disabledMods";
    if(!selectedServerName.equalsIgnoreCase("")) key += "_" + selectedServerName;
    String rawString = SettingsProperties.getValue(key);

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

  private static void clean() {
    String rootDir = LauncherApp.selectedServer.getRootDirectory();
    new File(rootDir + "/rsrc/mod.json").delete();
    new File(rootDir + "/rsrc/mod.png").delete();
    for (String filePath : FileUtil.fileNamesInDirectory(rootDir + "/mods", ".hash")) {
      new File(rootDir + "/mods/" + filePath).delete();
    }
  }

  public static void parseModData(Mod mod) {
    JSONObject modJson;
    try {
      modJson = new JSONObject(Compressor.readFileInsideZip(mod.getAbsolutePath(), "mod.json")).getJSONObject("mod");
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
          mod.setImage(ImageUtil.imageToBase64(ImageIO.read(Compressor.getISFromFileInsideZip(mod.getAbsolutePath(), "mod.png"))));
        } catch (Exception e2) {
          mod.setImage(null);
        }
      }

      if(mod instanceof JarMod) {
        try {
          int minJDKVersion = Integer.parseInt(modJson.getString("minJDKVersion"));
          int maxJDKVersion = Integer.parseInt(modJson.getString("maxJDKVersion"));
          ((JarMod) mod).setMinJDKVersion(minJDKVersion);
          ((JarMod) mod).setMaxJDKVersion(maxJDKVersion);
        } catch (JSONException ignored) {}
      }
    }
  }

  private static void checkJarModsJDKRequirements() {
    int gameJVMVersion = JavaUtil.getJVMVersion(JavaUtil.getGameJVMExePath());
    for(Mod mod : modList) {
      if(mod instanceof JarMod) {
        // Disable any jar mod that is below the min JDK requirements or above the max JDK requirements.
        boolean compatible = gameJVMVersion >= ((JarMod) mod).getMinJDKVersion() && gameJVMVersion <= ((JarMod) mod).getMaxJDKVersion();
        if(mod.isEnabled()) mod.setEnabled(compatible);
        ((JarMod) mod).setMeetsJDKRequirements(compatible);
      }
    }
  }

}
