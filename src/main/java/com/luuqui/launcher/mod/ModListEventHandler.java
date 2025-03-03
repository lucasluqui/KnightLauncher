package com.luuqui.launcher.mod;

import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.Locale;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.data.Mod;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsProperties;
import com.luuqui.util.DesktopUtil;
import com.luuqui.util.ThreadingUtil;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class ModListEventHandler {

  public static void refreshEvent(ActionEvent action) {
    refreshMods();
  }

  public static void refreshMods() {
    Thread refreshThread = new Thread(() -> {
      ModListGUI.refreshButton.setEnabled(false);
      ModListGUI.enableAllModsButton.setEnabled(false);
      ModListGUI.disableAllModsButton.setEnabled(false);

      ModListGUI.labelRefreshing.setVisible(true);
      ModLoader.checkInstalled();
      if (ModLoader.rebuildRequired && Settings.doRebuilds) {
        ModLoader.startFileRebuild();
      }
      ModLoader.mount();
      ModListGUI.updateModList(null);

      ModListGUI.refreshButton.setEnabled(true);
      ModListGUI.enableAllModsButton.setEnabled(true);
      ModListGUI.disableAllModsButton.setEnabled(true);
    });
    refreshThread.start();
  }

  public static void getModsEvent(ActionEvent action) {
    DesktopUtil.openWebpage(LauncherGlobals.URL_GET_MODS);
  }

  public static void openModsFolderEvent(ActionEvent action) {
    String rootDir = LauncherGlobals.USER_DIR;
    if(LauncherApp.selectedServer != null) {
      rootDir = LauncherApp.selectedServer.getRootDirectory();
    }
    DesktopUtil.openDir(rootDir + "/mods");
  }

  public static void disableMod(Mod mod) {
    String keySuffix = "";
    if(LauncherApp.selectedServer != null) {
      keySuffix = LauncherApp.selectedServer.isOfficial() ? "" : "_" + LauncherApp.selectedServer.getSanitizedName();
    }
    String disabledMods = SettingsProperties.getValue("modloader.disabledMods" + keySuffix);
    SettingsProperties.setValue("modloader.disabledMods" + keySuffix,
        disabledMods.equals("") ? mod.getFileName() : disabledMods + "," + mod.getFileName());
    mod.setEnabled(false);
    ModLoader.mountRequired = true;
    ModLoader.rebuildRequired = true;
  }

  public static void enableMod(Mod mod) {
    String keySuffix = "";
    if(LauncherApp.selectedServer != null) {
      keySuffix = LauncherApp.selectedServer.isOfficial() ? "" : "_" + LauncherApp.selectedServer.getSanitizedName();
    }
    String disabledMods = SettingsProperties.getValue("modloader.disabledMods" + keySuffix);
    if(disabledMods.contains(",")) {
      ArrayList<String> disabledModsList = new ArrayList<>(Arrays.asList(disabledMods.split(",")));
      disabledModsList.remove(mod.getFileName());
      disabledMods = "";
      for(String disabledMod : disabledModsList) {
        if(disabledMods.equals("")) {
          disabledMods += disabledMod;
        } else {
          disabledMods += "," + disabledMod;
        }
      }
    } else {
      disabledMods = "";
    }
    SettingsProperties.setValue("modloader.disabledMods" + keySuffix, disabledMods);
    mod.setEnabled(true);
    ModLoader.mountRequired = true;
    ModLoader.rebuildRequired = true;
  }

  public static void showDirectoriesWarning(boolean show) {
    if (!show) {
      ModListGUI.warningNotice.setVisible(false);
      return;
    }

    // Show the warning with a slight delay to make sure the GUI can load beforehand.
    Thread showDirectoriesWarningThread = new Thread(() -> {
      ModListGUI.warningNotice.setVisible(true);
      ModListGUI.currentWarning = Locale.getValue("error.folders_within_mods_folder");
    });
    ThreadingUtil.executeWithDelay(showDirectoriesWarningThread, 2000);
  }

  public static void searchMod() {
    ModListGUI.updateModList(ModListGUI.searchBox.getText());
  }

  public static void selectedServerChanged() {
    Server selectedServer = LauncherApp.selectedServer;

    if(selectedServer != null) {
      new Thread(ModLoader::checkInstalled).start();
      ModListGUI.viewingModsLabel.setText(Locale.getValue("m.viewing_mods", selectedServer.name));
    }
  }

  public static void checkServerSettingsKeys(String serverName) {
    SettingsProperties.createKeyIfNotExists("modloader.appliedModsHash_" + serverName, "0");
    SettingsProperties.createKeyIfNotExists("modloader.disabledMods_" + serverName, "");
  }

  public static void enableAllModsEvent(ActionEvent event) {
    for(Mod mod : ModLoader.getModList()) {
      enableMod(mod);
    }
    refreshMods();
  }

  public static void disableAllModsEvent(ActionEvent event) {
    for(Mod mod : ModLoader.getModList()) {
      disableMod(mod);
    }
    refreshMods();
  }
}
