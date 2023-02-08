package com.lucasallegri.launcher.mods;

import com.lucasallegri.discord.DiscordRPC;
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
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipFile;

import static com.lucasallegri.launcher.mods.Log.log;

public class ModLoader {

  private static final LinkedList<Mod> modList = new LinkedList<>();

  private static final String[] RSRC_BUNDLES = { "full-music-bundle.jar", "full-rest-bundle.jar", "intro-bundle.jar" };

  public static Boolean mountRequired = false;
  public static Boolean rebuildRequired = false;

  public static void checkInstalled() {

    // Clean the list in case something remains in it.
    if (getModCount() > 0) clearModList();

    // Append all .zip and .jar files inside the mod folder into an ArrayList.
    List<File> rawFiles = FileUtil.filesInDirectory(LauncherGlobals.USER_DIR + "/mods/", ".zip");
    rawFiles.addAll(FileUtil.filesInDirectory(LauncherGlobals.USER_DIR + "/code-mods/", ".jar"));

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
      }

      if (mod != null && modJson != null) {
        mod.setDisplayName(modJson.getString("name"));
        mod.setDescription(modJson.getString("description"));
        mod.setAuthor(modJson.getString("author"));
        mod.setVersion(modJson.getString("version"));
      }

      addMod(mod);
      mod.wasAdded();

      // Compute a hash for each mod file and check that it matches on every execution, if it doesn't, then rebuild.
      String hash = Compressor.getZipHash(file.getAbsolutePath());
      String hashFilePath = file.getAbsolutePath() + ".hash";

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
    if (Integer.parseInt(SettingsProperties.getValue("modloader.lastModCount")) != getModCount()) {
      SettingsProperties.setValue("modloader.lastModCount", Integer.toString(getModCount()));
      rebuildRequired = true;
      mountRequired = true;
    }

    // Finally lets see which have been set as disabled.
    parseDisabledMods();
  }

  public static void mount() {

    LauncherGUI.launchButton.setEnabled(false);
    if(rebuildRequired) startFileRebuild();
    ProgressBar.startTask();
    ProgressBar.setBarMax(getEnabledModCount() + 1);
    ProgressBar.setState(Locale.getValue("m.mount"));
    DiscordRPC.getInstance().setDetails(Locale.getValue("m.mount"));
    LinkedList<Mod> localList = getModList();

    for (int i = 0; i < getModCount(); i++) {
      if(localList.get(i).isEnabled()) {
        localList.get(i).mount();
        ProgressBar.setBarValue(i + 1);
      }
    }

    // Make sure no cheat mod slips in.
    extractSafeguard();

    mountRequired = false;
    ProgressBar.finishTask();
    LauncherDigester.doDigest();
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
      LauncherGUI.launchButton.setEnabled(true);
      LauncherGUI.settingsButton.setEnabled(true);
      SettingsGUI.forceRebuildButton.setEnabled(true);
    } catch (Exception ignored) {}

    DiscordRPC.getInstance().setDetails(Locale.getValue("presence.launch_ready", String.valueOf(getEnabledModCount())));
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

  private static void addMod(Mod mod) {
    if(mod.isEnabled()) modList.add(mod);
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
    ModListGUI.updateModList();
  }

}
