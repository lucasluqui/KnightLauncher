package com.lucasallegri.launcher.mods;

import java.awt.event.ActionEvent;

import com.lucasallegri.launcher.LauncherConstants;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.util.DesktopUtil;

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
		ModLoader.mount();
	}
	
	public static void getModsEvent(ActionEvent action) {
		DesktopUtil.openWebpage(LauncherConstants.GET_MODS_URL);
	}
	
}
