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

      if(gameVersionChanged()) {
        log.info("Game version has changed", "new", LauncherApp.getLocalGameVersion().trim());
        rebuildRequired = true;
        mountRequired = true;
      }

      checkJarModsRequirements();

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
    ProgressBar.setBarMax(getEnabledModCount());
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

    // Update the last known game version.
    String versionKey = "modloader.lastKnownVersion";
    if(!selectedServerName.equalsIgnoreCase("")) versionKey += "_" + selectedServerName;
    SettingsProperties.setValue(versionKey, LauncherApp.getLocalGameVersion());

    mountRequired = false;
    ProgressBar.finishTask();

    LauncherEventHandler.updateServerSwitcher(false);
    LauncherGUI.launchButton.setEnabled(true);
    LauncherGUI.settingsButton.setEnabled(true);
    LauncherGUI.modButton.setEnabled(true);
  }

  public static void startFileRebuild() {
    rebuildFiles(false);
  }

  public static void startStrictFileRebuild() {
    rebuildFiles(true);
  }

  private static void rebuildFiles(boolean strict) {
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

    // Clear the entirety of the rsrc folder leaving only the jar and zip bundles.
    DiscordPresenceClient.getInstance().setDetails(Locale.getValue("m.purge"));
    ProgressBar.setState(Locale.getValue("m.purge"));
    FileUtil.purgeDirectory(new File(rootDir + "/rsrc/"), new String[] {".jar", ".jarv", ".zip"});

    // Iterate through all bundles to rebuild the game files.
    DiscordPresenceClient.getInstance().setDetails(Locale.getValue("m.rebuild"));
    ProgressBar.setState(Locale.getValue("m.rebuild"));

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

    // Strict requires resetting code and config jars to vanilla too.
    if (LauncherApp.selectedServer.isOfficial() && strict) {
      DiscordPresenceClient.getInstance().setDetails(Locale.getValue("m.reset_code"));
      ProgressBar.setState(Locale.getValue("m.reset_code"));
      resetCode();
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

  private static void checkJarModsRequirements() {
    int gameJVMVersion = JavaUtil.getJVMVersion(JavaUtil.getGameJVMExePath());
    String pxVersion = LauncherApp.selectedServer == null ? LauncherApp.getLocalGameVersion() : LauncherApp.selectedServer.version;

    boolean hasIncompatibility = false;
    for(Mod mod : modList) {
      if(mod instanceof JarMod) {
        // Disable any jar mod that is below the min JDK requirements or above the max JDK requirements.
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

    ModListEventHandler.showIncompatibleCodeModsWarning(hasIncompatibility);
  }

  private static boolean gameVersionChanged() {
    Server selectedServer = LauncherApp.selectedServer;
    String key = "modloader.lastKnownVersion";

    if(selectedServer != null) {
      key += LauncherApp.selectedServer.isOfficial() ? "" : "_" + LauncherApp.selectedServer.getSanitizedName();
    }

    String lastKnownVersion = SettingsProperties.getValue(key);
    String currentVersion = LauncherApp.getLocalGameVersion().trim();

    if(!lastKnownVersion.equalsIgnoreCase(currentVersion)) {
      return true;
    }

    return false;
  }

  private static void resetCode() {
    int downloadAttempts = 0;
    boolean downloadCompleted = false;

    while (downloadAttempts <= 3 && !downloadCompleted) {
      downloadAttempts++;
      log.info("Resetting code jars", "attempts", downloadAttempts);
      try {
        FileUtils.copyURLToFile(
            new URL("http://gamemedia2.spiralknights.com/spiral/" + LauncherApp.getLocalGameVersion() + "/code/projectx-pcode.jar"),
            new File(LauncherGlobals.USER_DIR + "/code/", "projectx-pcode.jar"),
            0,
            0
        );
        FileUtils.copyURLToFile(
            new URL("http://gamemedia2.spiralknights.com/spiral/" + LauncherApp.getLocalGameVersion() + "/code/projectx-config.jar"),
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

  public static final String[] FILTER_LIST = new String[] {
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
