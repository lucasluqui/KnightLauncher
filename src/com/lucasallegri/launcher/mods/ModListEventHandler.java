package com.lucasallegri.launcher.mods;

import java.awt.event.ActionEvent;

import com.lucasallegri.launcher.LauncherConstants;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.util.DesktopUtil;

public class ModListEventHandler {
	
	public static void refreshEvent(ActionEvent action) {
		
		ModLoader.checkInstalled();
		if(ModLoader.rebuildRequired && Settings.doRebuilds) { ModLoader.startFileRebuild(); }
//		ModListGUI.modListGUIFrame.dispose();
//		ModListGUI.compose();
		ModLoader.mountRequired = true;
		
	}
	
	public static void getModsEvent(ActionEvent action) {
		DesktopUtil.openWebpage(LauncherConstants.GET_MODS_URL);
	}
	
}
