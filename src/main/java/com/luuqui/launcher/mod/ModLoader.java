package com.luuqui.launcher.mod;

import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.*;
import com.luuqui.launcher.mod.data.JarMod;
import com.luuqui.launcher.mod.data.Mod;
import com.luuqui.launcher.mod.data.Modpack;
import com.luuqui.launcher.mod.data.ZipMod;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsProperties;
import com.luuqui.util.Compressor;
import com.luuqui.util.FileUtil;
import com.luuqui.util.ImageUtil;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import static com.luuqui.launcher.mod.Log.log;

public class ModLoader {

  private static final LinkedList<Mod> modList = new LinkedList<>();

  private static final String MOD_FOLDER_PATH = LauncherGlobals.USER_DIR + "/mods/";

  private static final String[] RSRC_BUNDLES = { "full-music-bundle.jar", "full-rest-bundle.jar", "intro-bundle.jar" };

  public static Boolean mountRequired = false;
  public static Boolean rebuildRequired = false;

  public static void checkInstalled() {

    // Clean the list in case something remains in it.
    if (getModCount() > 0) clearModList();

    // Append all .zip and .jar files inside the mod folder into an ArrayList.
    List<File> rawFiles = FileUtil.filesInDirectory(MOD_FOLDER_PATH, ".zip");
    rawFiles.addAll(FileUtil.filesInDirectory(MOD_FOLDER_PATH, ".jar"));
    rawFiles.addAll(FileUtil.filesInDirectory(MOD_FOLDER_PATH, ".modpack"));

    for (File file : rawFiles) {
      JSONObject modJson;
      try {
        modJson = new JSONObject(Compressor.readFileInsideZip(file.getAbsolutePath(), "mod.json")).getJSONObject("mod");
      } catch (Exception e) {
        modJson = null;
      }

      String fileName = file.getName();
      Mod mod = null;
      if (fileName.endsWith("zip")) {
        mod = new ZipMod(fileName);
      } else if (fileName.endsWith("jar")) {
        mod = new JarMod(fileName);
      } else if (fileName.endsWith("modpack")) {
        mod = new Modpack(fileName);
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
            mod.setImage(ImageUtil.imageToBase64(ImageIO.read(Compressor.getISFromFileInsideZip(file.getAbsolutePath(), "mod.png"))));
          } catch (Exception e2) {
            mod.setImage(null);
          }
        }
      }

      modList.add(mod);
      mod.wasAdded();
    }

    // Finally lets see which have been set as disabled.
    parseDisabledMods();

    // Check if there's a new or removed mod since last execution, rebuild will be needed in that case.
    if (Integer.parseInt(SettingsProperties.getValue("modloader.appliedModsHash")) != getEnabledModsHash()) {
      log.info("Hashcode doesn't match, preparing for rebuild and remount...");
      rebuildRequired = true;
      mountRequired = true;
    }

    // Check if there's directories in the mod folder and push a warning to the user.
    for (File file : FileUtil.filesAndDirectoriesInDirectory(MOD_FOLDER_PATH)) {
      if(file.isDirectory()) {
        ModListEventHandler.showDirectoriesWarning(true);
        break;
      }
      ModListEventHandler.showDirectoriesWarning(false);
    }

    ModListGUI.labelModCount.setText(String.valueOf(ModLoader.getModList().size()));
    ModListGUI.updateModList(null);
  }

  public static void mount() {
    if (Settings.doRebuilds && ModLoader.rebuildRequired) ModLoader.startFileRebuild();

    LauncherGUI.serverList.setEnabled(false);
    LauncherGUI.launchButton.setEnabled(false);
    LauncherGUI.settingsButton.setEnabled(false);
    LauncherGUI.modButton.setEnabled(false);

    ProgressBar.startTask();
    ProgressBar.setBarMax(getEnabledModCount() + 1);
    ProgressBar.setState(Locale.getValue("m.mount"));
    DiscordRPC.getInstance().setDetails(Locale.getValue("m.mount"));
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

    SettingsProperties.setValue("modloader.appliedModsHash", Integer.toString(hashSet.hashCode()));

    // Make sure no cheat mod slips in.
    extractSafeguard();

    // Clean the game from unwanted files.
    clean();

    mountRequired = false;
    ProgressBar.finishTask();
    LauncherDigester.doDigest();

    LauncherGUI.serverList.setEnabled(true);
    LauncherGUI.launchButton.setEnabled(true);
    LauncherGUI.settingsButton.setEnabled(true);
    LauncherGUI.modButton.setEnabled(true);
  }

  public static void startFileRebuild() {
    rebuildFiles();
  }

  private static void rebuildFiles() {
    try {
      LauncherGUI.serverList.setEnabled(false);
      LauncherGUI.launchButton.setEnabled(false);
      LauncherGUI.settingsButton.setEnabled(false);
      LauncherGUI.modButton.setEnabled(false);
    } catch (Exception ignored) {}


    ProgressBar.startTask();
    ProgressBar.setBarMax(RSRC_BUNDLES.length + 1);
    DiscordRPC.getInstance().setDetails(Locale.getValue("m.clean"));
    ProgressBar.setState(Locale.getValue("m.clean"));

    // Iterate through all 3 bundles to clean up the game files.
    for (int i = 0; i < RSRC_BUNDLES.length; i++) {
      ProgressBar.setBarValue(i + 1);
      DiscordRPC.getInstance().setDetails(Locale.getValue("presence.rebuilding", new String[]{String.valueOf(i + 1), String.valueOf(RSRC_BUNDLES.length)}));
      try {
        FileUtil.unpackJar(new ZipFile(LauncherGlobals.USER_DIR + "/rsrc/" + RSRC_BUNDLES[i]), new File(LauncherGlobals.USER_DIR + "/rsrc/"), false);
      } catch (IOException e) {
        log.error(e);
      }
    }

    // Check for .xml configs present in the configs folder and delete them.
    List<String> configs = FileUtil.fileNamesInDirectory(LauncherGlobals.USER_DIR + "/rsrc/config", ".xml");
    for (String config : configs) {
      new File(LauncherGlobals.USER_DIR + "/rsrc/config/" + config).delete();
    }

    ProgressBar.setBarValue(RSRC_BUNDLES.length + 1);
    ProgressBar.finishTask();
    rebuildRequired = false;

    try {
      LauncherGUI.serverList.setEnabled(true);
      LauncherGUI.launchButton.setEnabled(true);
      LauncherGUI.settingsButton.setEnabled(true);
      LauncherGUI.modButton.setEnabled(true);
    } catch (Exception ignored) {}

    DiscordRPC.getInstance().setDetails(Locale.getValue("presence.launch_ready"));
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
    String rawString = SettingsProperties.getValue("modloader.disabledMods");

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
    new File(LauncherGlobals.USER_DIR + "/rsrc/mod.json").delete();
    new File(LauncherGlobals.USER_DIR + "/rsrc/mod.png").delete();
    for (String filePath : FileUtil.fileNamesInDirectory(LauncherGlobals.USER_DIR + "/mods", ".hash")) {
      new File(LauncherGlobals.USER_DIR + "/mods/" + filePath).delete();
    }
  }

}
