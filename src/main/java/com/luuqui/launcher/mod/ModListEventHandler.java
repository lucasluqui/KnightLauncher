package com.luuqui.launcher.mod;

import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.Locale;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.mod.data.Mod;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsProperties;
import com.luuqui.util.DesktopUtil;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class ModListEventHandler {

  public static void refreshEvent(ActionEvent action) {
    Thread refreshThread = new Thread(() -> {
      ModListGUI.labelRefreshing.setVisible(true);
      ModLoader.checkInstalled();
      if (ModLoader.rebuildRequired && Settings.doRebuilds) {
        ModLoader.startFileRebuild();
      }
      ModListGUI.labelModCount.setText(Integer.toString(ModLoader.getModCount()));
      ModLoader.mount();
      ModListGUI.updateModList();
      ModListGUI.labelRefreshing.setVisible(false);
    });
    refreshThread.start();
  }

  public static void forceApplyEvent(ActionEvent action) {
    ModListGUI.labelRefreshing.setText("Applying...");
    ModLoader.mount();
    ModListGUI.labelRefreshing.setText("Applied");
    DiscordRPC.getInstance().setDetails(Locale.getValue("presence.launch_ready", String.valueOf(ModLoader.getEnabledModCount())));
  }

  public static void getModsEvent(ActionEvent action) {
    DesktopUtil.openWebpage(LauncherGlobals.URL_GET_MODS);
  }

  public static void disableMod(Mod mod) {
    String disabledMods = SettingsProperties.getValue("modloader.disabledMods");
    SettingsProperties.setValue("modloader.disabledMods",
        disabledMods.equals("") ? mod.getFileName() : disabledMods + "," + mod.getFileName());
    mod.setEnabled(false);
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
    ModLoader.mountRequired = true;
    ModLoader.rebuildRequired = true;
  }
}
