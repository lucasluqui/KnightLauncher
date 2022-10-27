package com.lucasallegri.launcher.mods;

import com.lucasallegri.launcher.Locale;
import com.lucasallegri.launcher.LauncherApp;
import com.lucasallegri.launcher.LauncherGlobals;
import com.lucasallegri.launcher.mods.data.Mod;
import com.lucasallegri.launcher.mods.data.ZipMod;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.util.DesktopUtil;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModListEventHandler {

  public static void refreshEvent(ActionEvent action) {

    ModLoader.checkInstalled();
    if (ModLoader.rebuildRequired && Settings.doRebuilds) {
      ModLoader.startFileRebuild();
    }
    ModListGUI.modListContainer.removeAll();
    for (Mod mod : ModLoader.getModList()) {
      ModListGUI.modListContainer.add(mod.getDisplayName());
    }
    ModListGUI.labelModCount.setText(Integer.toString(ModLoader.getEnabledModCount()));
    ModLoader.mountRequired = true;
    ModListGUI.updateModList();
  }

  public static void forceApplyEvent(ActionEvent action) {
    ModListGUI.labelForceApplyState.setText("Applying...");
    ModLoader.mount();
    ModListGUI.labelForceApplyState.setText("Applied");
    LauncherApp.getRPC().setDetails(Locale.getValue("presence.launch_ready", String.valueOf(ModLoader.getEnabledModCount())));
  }

  public static void getModsEvent(ActionEvent action) {
    DesktopUtil.openWebpage(LauncherGlobals.GET_MODS_URL);
  }

  public static void disableMod(Mod mod) {
    String disabledMods = SettingsProperties.getValue("modloader.disabledMods");
    SettingsProperties.setValue("modloader.disabledMods",
        disabledMods.equals("") ? mod.getFileName() : disabledMods + "," + mod.getFileName());
    mod.setEnabled(false);
    ModListGUI.updateModList();
    ModLoader.mountRequired = true;
    ModLoader.rebuildRequired = true;
  }

  public static void enableMod(Mod mod) {
    String disabledMods = SettingsProperties.getValue("modloader.disabledMods");
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
    SettingsProperties.setValue("modloader.disabledMods", disabledMods);
    mod.setEnabled(true);
    ModListGUI.updateModList();
    ModLoader.mountRequired = true;
    ModLoader.rebuildRequired = true;
  }
}
