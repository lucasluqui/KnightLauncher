package com.luuqui.launcher.mod;

import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.Locale;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.data.Mod;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsProperties;
import com.luuqui.util.DesktopUtil;
import com.luuqui.util.ThreadingUtil;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.luuqui.launcher.mod.Log.log;

public class ModListEventHandler {

  public static void refreshEvent(ActionEvent action) {
    refreshMods();
  }

  public static void refreshMods() {
    Thread refreshThread = new Thread(() -> {
      ModListGUI.refreshButton.setEnabled(false);
      ModListGUI.enableAllModsButton.setEnabled(false);
      ModListGUI.disableAllModsButton.setEnabled(false);
      ModListGUI.addModButton.setEnabled(false);

      ModLoader.checkInstalled();
      if (ModLoader.rebuildRequired && Settings.doRebuilds) {
        ModLoader.startFileRebuild();
      }
      ModLoader.mount();
      ModListGUI.updateModList(null);

      ModListGUI.refreshButton.setEnabled(true);
      ModListGUI.enableAllModsButton.setEnabled(true);
      ModListGUI.disableAllModsButton.setEnabled(true);
      ModListGUI.addModButton.setEnabled(true);
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

  public static void showIncompatibleCodeModsWarning(boolean show) {
    if (!show) {
      ModListGUI.warningNotice.setVisible(false);
      return;
    }

    // Show the warning with a slight delay to make sure the GUI can load beforehand.
    Thread showDirectoriesWarningThread = new Thread(() -> {
      ModListGUI.warningNotice.setVisible(true);
      ModListGUI.currentWarning = Locale.getValue("error.incompatible_code_mods");
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
    SettingsProperties.createKeyIfNotExists("modloader.lastKnownVersion_" + serverName, "0");
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

  public static void addModEvent(ActionEvent event) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Add mod");
    fileChooser.setApproveButtonText("Add");

    FileNameExtensionFilter restrict = new FileNameExtensionFilter(".zip, .jar, .modpack", "zip", "jar", "modpack");
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.addChoosableFileFilter(restrict);

    int response = fileChooser.showOpenDialog(null);

    if (response == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      if(path.endsWith(".zip") || path.endsWith(".jar") || path.endsWith(".modpack")) {
        File file = new File(path);
          try {
            FileUtils.copyFile(file, new File(LauncherApp.selectedServer.getRootDirectory() + "/mods/" + file.getName()));
            log.info("Adding mod: " + file.getName());
            refreshMods();
          } catch (IOException e) {
            log.error(e);
          }
      } else {
        Dialog.push(Locale.getValue("error.mod_file_format"), Locale.getValue("t.add_mod_error"), JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public static void removeModEvent(Mod mod) {
    boolean confirm = Dialog.pushWithConfirm(Locale.getValue("m.destructive_action"), Locale.getValue("b.remove_mod", mod.getDisplayName()), JOptionPane.WARNING_MESSAGE);
    if(confirm) {
      new File(mod.getAbsolutePath()).delete();
      log.info("Removed mod: " + mod);
      refreshMods();
    }
  }
}
