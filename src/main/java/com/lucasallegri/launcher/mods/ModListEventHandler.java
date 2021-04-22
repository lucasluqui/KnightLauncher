package com.lucasallegri.launcher.mods;

import com.lucasallegri.launcher.Locale;
import com.lucasallegri.launcher.LauncherApp;
import com.lucasallegri.launcher.LauncherGlobals;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.util.DesktopUtil;

import java.awt.event.ActionEvent;

public class ModListEventHandler {

  public static void refreshEvent(ActionEvent action) {

    ModLoader.checkInstalled();
    if (ModLoader.rebuildRequired && Settings.doRebuilds) {
      ModLoader.startFileRebuild();
    }
    ModListGUI.modListContainer.removeAll();
    for (Mod mod : ModList.installedMods) {
      ModListGUI.modListContainer.add(mod.getDisplayName());
    }
    ModListGUI.labelModCount.setText(Integer.toString(ModList.installedMods.size()));
    ModLoader.mountRequired = true;
  }

  public static void forceApplyEvent(ActionEvent action) {
    ModListGUI.labelForceApplyState.setText("Applying...");
    ModLoader.mount();
    ModListGUI.labelForceApplyState.setText("Applied");
    LauncherApp.getRPC().setDetails(Locale.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
  }

  public static void getModsEvent(ActionEvent action) {
    DesktopUtil.openWebpage(LauncherGlobals.GET_MODS_URL);
  }

}
