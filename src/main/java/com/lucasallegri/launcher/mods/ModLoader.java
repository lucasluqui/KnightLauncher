package com.lucasallegri.launcher.mods;

import com.lucasallegri.launcher.*;
import com.lucasallegri.launcher.mods.data.JarMod;
import com.lucasallegri.launcher.mods.data.Mod;
import com.lucasallegri.launcher.mods.data.ZipMod;
import com.lucasallegri.launcher.settings.SettingsGUI;
import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.util.Compressor;
import com.lucasallegri.util.FileUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import static com.lucasallegri.launcher.mods.Log.log;

public class ModLoader {

  private static final String[] RSRC_BUNDLES = { "full-music-bundle.jar", "full-rest-bundle.jar", "intro-bundle.jar" };

  public static Boolean mountRequired = false;
  public static Boolean rebuildRequired = false;

  public static void checkInstalled() {

    // Clean the list in case something remains in it.
    if (ModList.installedMods.size() > 0) ModList.installedMods.clear();

    // Append all .zip and .jar files inside the mod folder into an ArrayList.
    List<String> rawFiles = FileUtil.fileNamesInDirectory(LauncherGlobals.USER_DIR + "/mods/", ".zip");
    rawFiles.addAll(FileUtil.fileNamesInDirectory(LauncherGlobals.USER_DIR + "/code-mods/", ".jar"));

    for (String file : rawFiles) {
      JSONObject modJson;
      try {
        modJson = new JSONObject(Compressor.readFileInsideZip(LauncherGlobals.USER_DIR + "/mods/" + file, "mod.json")).getJSONObject("mod");
      } catch (Exception e) {
        modJson = null;
      }

      Mod mod = null;
      if (file.endsWith("zip")) {
        mod = new ZipMod(file);
      } else if (file.endsWith("jar")) {
        mod = new JarMod(file);
      }

      if (mod != null && modJson != null) {
        mod.setDisplayName(modJson.getString("name"));
        mod.setDescription(modJson.getString("description"));
        mod.setAuthor(modJson.getString("author"));
        mod.setVersion(modJson.getString("version"));
      }

      ModList.installedMods.add(mod);
      mod.wasAdded();

      // Compute a hash for each mod file and check that it matches on every execution, if it doesn't, then rebuild.
      String hash = Compressor.getZipHash(LauncherGlobals.USER_DIR + "/mods/" + file);
      String hashFilePath = LauncherGlobals.USER_DIR + "/mods/" + mod.getFileName() + ".hash";

      if (FileUtil.fileExists(hashFilePath)) {
        try {
          // We read the hash file contents.
          String fileHash = FileUtil.readFile(hashFilePath);

          // If both hashes match then we move on.
          if (hash.startsWith(fileHash)) continue;

          // They don't? We write a new one and schedule a file rebuild and remount.
          new File(hashFilePath).delete();
          FileUtil.writeFile(hashFilePath, hash);
          rebuildRequired = true;
          mountRequired = true;

        } catch (IOException e) {
          log.error(e);
        }
      } else {
        // And if we don't have any hash at all then let's make it.
        FileUtil.writeFile(hashFilePath, hash);
        rebuildRequired = true;
        mountRequired = true;
      }
    }

    // Check if there's a new or removed mod since last execution, rebuild will be needed in that case.
    if (Integer.parseInt(SettingsProperties.getValue("modloader.lastModCount")) != ModList.installedMods.size()) {
      SettingsProperties.setValue("modloader.lastModCount", Integer.toString(ModList.installedMods.size()));
      rebuildRequired = true;
      mountRequired = true;
    }
  }

  public static void mount() {

    LauncherGUI.launchButton.setEnabled(false);
    ProgressBar.showBar(true);
    ProgressBar.showState(true);
    ProgressBar.setBarMax(ModList.installedMods.size() + 1);
    ProgressBar.setState(Locale.getValue("m.mount"));
    LauncherApp.getRPC().setDetails(Locale.getValue("m.mount"));

    for (int i = 0; i < ModList.installedMods.size(); i++) {
      ProgressBar.setBarValue(i + 1);
      ModList.installedMods.get(i).mount();
    }

    // Make sure no cheat mod slips in.
    extractSafeguard();

    ProgressBar.showBar(false);
    ProgressBar.showState(false);
    LauncherGUI.launchButton.setEnabled(true);
  }

  public static void startFileRebuild() {
    Thread rebuildThread = new Thread(() -> rebuildFiles());
    rebuildThread.start();
  }

  private static void rebuildFiles() {
    try {
      LauncherGUI.launchButton.setEnabled(false);
      LauncherGUI.settingsButton.setEnabled(false);
      SettingsGUI.forceRebuildButton.setEnabled(false);
    } catch (Exception ignored) {}


    ProgressBar.showBar(true);
    ProgressBar.showState(true);
    ProgressBar.setBarMax(RSRC_BUNDLES.length + 1);
    LauncherApp.getRPC().setDetails(Locale.getValue("m.clean"));
    ProgressBar.setState(Locale.getValue("m.clean"));

    // Iterate through all 3 bundles to clean up the game files.
    for (int i = 0; i < RSRC_BUNDLES.length; i++) {
      ProgressBar.setBarValue(i + 1);
      LauncherApp.getRPC().setDetails(Locale.getValue("presence.rebuilding", new String[]{String.valueOf(i + 1), String.valueOf(RSRC_BUNDLES.length)}));
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
    ProgressBar.showBar(false);
    ProgressBar.showState(false);
    rebuildRequired = false;

    try {
      LauncherGUI.launchButton.setEnabled(true);
      LauncherGUI.settingsButton.setEnabled(true);
      SettingsGUI.forceRebuildButton.setEnabled(true);
    } catch (Exception ignored) {}

    LauncherApp.getRPC().setDetails(Locale.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
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

}
