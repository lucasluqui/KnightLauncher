package xyz.lucasallegri.launcher.mods;

import java.awt.event.ActionEvent;

import xyz.lucasallegri.launcher.LauncherConstants;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.util.DesktopUtil;

public class ModListEventHandler {
	
	public static void refreshEvent(ActionEvent action) {
		
		ModLoader.checkInstalled();
		if(ModLoader.rebuildRequired && Settings.doRebuilds) { ModLoader.startFileRebuild(); }
		ModListGUI.modListGUIFrame.dispose();
		ModListGUI.compose();
		
	}
	
	public static void getModsEvent(ActionEvent action) {
		DesktopUtil.openWebpage(LauncherConstants.GET_MODS_URL);
	}
	
}
