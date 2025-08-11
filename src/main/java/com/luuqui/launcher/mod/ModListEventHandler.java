package com.luuqui.launcher.mod;

import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.LocaleManager;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.data.Mod;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsManager;
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

public class ModListEventHandler
{
  @Inject private ModListGUI gui;

  protected ModManager _modManager;
  protected LocaleManager _localeManager;
  protected SettingsManager _settingsManager;
  protected FlamingoManager _flamingoManager;

  @Inject
  public ModListEventHandler (ModManager _modManager,
                              LocaleManager _localeManager,
                              SettingsManager _settingsManager,
                              FlamingoManager _flamingoManager)
  {
    this._modManager = _modManager;
    this._localeManager = _localeManager;
    this._settingsManager = _settingsManager;
    this._flamingoManager = _flamingoManager;
  }

  @SuppressWarnings("unused")
  public void refreshEvent (ActionEvent action)
  {
    refreshMods(true);
  }

  public void refreshMods (boolean mount)
  {
    Thread refreshThread = new Thread(() -> {
      this.gui.refreshButton.setEnabled(false);
      this.gui.enableAllModsButton.setEnabled(false);
      this.gui.disableAllModsButton.setEnabled(false);
      this.gui.addModButton.setEnabled(false);

      _modManager.checkInstalled();

      if (mount) {
        if (_modManager.getRebuildRequired() && Settings.doRebuilds) {
          _modManager.startFileRebuild();
        }
        _modManager.mount();
      }

      this.gui.updateModList(null);

      this.gui.refreshButton.setEnabled(true);
      this.gui.enableAllModsButton.setEnabled(true);
      this.gui.disableAllModsButton.setEnabled(true);
      this.gui.addModButton.setEnabled(true);
    });
    refreshThread.start();
  }

  @SuppressWarnings("unused")
  public void getModsEvent (ActionEvent action)
  {
    DesktopUtil.openWebpage(LauncherGlobals.URL_GET_MODS);
  }

  @SuppressWarnings("unused")
  public void openModsFolderEvent (ActionEvent action)
  {
    String rootDir = LauncherGlobals.USER_DIR;
    if(_flamingoManager.getSelectedServer() != null) {
      rootDir = _flamingoManager.getSelectedServer().getRootDirectory();
    }
    DesktopUtil.openDir(rootDir + "/mods");
  }

  @SuppressWarnings("all")
  public void disableMod (Mod mod)
  {
    String keySuffix = "";
    if(_flamingoManager.getSelectedServer() != null) {
      keySuffix = _flamingoManager.getSelectedServer().isOfficial() ? "" : "_" + _flamingoManager.getSelectedServer().getSanitizedName();
    }
    String disabledMods = _settingsManager.getValue("modloader.disabledMods" + keySuffix);
    _settingsManager.setValue("modloader.disabledMods" + keySuffix,
        disabledMods.equals("") ? mod.getFileName() : disabledMods + "," + mod.getFileName());
    mod.setEnabled(false);
    _modManager.setMountRequired(true);
    _modManager.setRebuildRequired(true);
  }

  @SuppressWarnings("all")
  public void enableMod (Mod mod)
  {
    String keySuffix = "";
    if(_flamingoManager.getSelectedServer() != null) {
      keySuffix = _flamingoManager.getSelectedServer().isOfficial() ? "" : "_" + _flamingoManager.getSelectedServer().getSanitizedName();
    }
    String disabledMods = _settingsManager.getValue("modloader.disabledMods" + keySuffix);
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
    _settingsManager.setValue("modloader.disabledMods" + keySuffix, disabledMods);
    mod.setEnabled(true);
    _modManager.setMountRequired(true);
    _modManager.setRebuildRequired(true);
  }

  public void showDirectoriesWarning (boolean show)
  {
    if (!show) {
      this.gui.warningNotice.setVisible(false);
      return;
    }

    // Show the warning with a slight delay to make sure the GUI can load beforehand.
    Thread showDirectoriesWarningThread = new Thread(() -> {
      this.gui.warningNotice.setVisible(true);
      this.gui.currentWarning = _localeManager.getValue("error.folders_within_mods_folder");
    });
    ThreadingUtil.executeWithDelay(showDirectoriesWarningThread, 2000);
  }

  public void showIncompatibleModsWarning (boolean show)
  {
    if (!show) {
      this.gui.warningNotice.setVisible(false);
      return;
    }

    // Show the warning with a slight delay to make sure the GUI can load beforehand.
    Thread showDirectoriesWarningThread = new Thread(() -> {
      this.gui.warningNotice.setVisible(true);
      this.gui.currentWarning = _localeManager.getValue("error.incompatible_mod");
    });
    ThreadingUtil.executeWithDelay(showDirectoriesWarningThread, 2000);
  }

  public void searchMod ()
  {
    this.gui.updateModList(this.gui.searchBox.getText());
  }

  public void selectedServerChanged ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    if (selectedServer != null) {
      new Thread(_modManager::checkInstalled).start();
      this.gui.viewingModsLabel.setText(_localeManager.getValue("m.viewing_mods", selectedServer.name));
    }
  }

  public void checkServerSettingsKeys (String serverName)
  {
    _settingsManager.createKeyIfNotExists("modloader.appliedModsHash_" + serverName, "0");
    _settingsManager.createKeyIfNotExists("modloader.disabledMods_" + serverName, "");
    _settingsManager.createKeyIfNotExists("modloader.lastKnownVersion_" + serverName, "0");
    _settingsManager.createKeyIfNotExists("modloader.forcedMountsForCurrentVersion_" + serverName, "0");
  }

  @SuppressWarnings("unused")
  public void enableAllModsEvent (ActionEvent event)
  {
    for(Mod mod : _modManager.getModList()) {
      enableMod(mod);
    }
    refreshMods(false);
  }

  @SuppressWarnings("unused")
  public void disableAllModsEvent (ActionEvent event)
  {
    for(Mod mod : _modManager.getModList()) {
      disableMod(mod);
    }
    refreshMods(false);
  }

  @SuppressWarnings("unused")
  public void addModEvent (ActionEvent event)
  {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Add a mod");
    fileChooser.setApproveButtonText("Add");

    FileNameExtensionFilter restrict = new FileNameExtensionFilter(".zip, .jar, .modpack", "zip", "jar", "modpack");
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.addChoosableFileFilter(restrict);

    int response = fileChooser.showOpenDialog(null);

    if (response == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      if (path.endsWith(".zip") || path.endsWith(".jar") || path.endsWith(".modpack")) {
        File file = new File(path);
          try {
            FileUtils.moveFile(file, new File(_flamingoManager.getSelectedServer().getRootDirectory() + "/mods/" + file.getName()));
            log.info("Added mod: " + file.getName());
            refreshMods(false);
          } catch (IOException e) {
            log.error(e);
          }
      } else {
        Dialog.push(_localeManager.getValue("error.mod_file_format"), _localeManager.getValue("t.add_mod_error"), JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  @SuppressWarnings("all")
  public void removeModEvent (Mod mod)
  {
    boolean confirm = Dialog.pushWithConfirm(_localeManager.getValue("m.destructive_action"), _localeManager.getValue("b.remove_mod_tooltip", mod.getDisplayName()), JOptionPane.WARNING_MESSAGE);
    if (confirm) {
      new File(_flamingoManager.getSelectedServer().getRootDirectory() + "/mods/" + mod.getFileName()).delete();
      log.info("Removed mod: " + mod);
      refreshMods(false);
    }
  }

  public void showWarningEvent (Mod mod)
  {
    Dialog.push(
        _localeManager.getValue("m.mod_warnings", new String[] { mod.getDisplayName(), mod.getWarningMessage() }), _localeManager.getValue("t.warning"), JOptionPane.WARNING_MESSAGE);
  }
}
