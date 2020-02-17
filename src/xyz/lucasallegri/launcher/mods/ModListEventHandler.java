package xyz.lucasallegri.launcher.mods;

import java.awt.event.ActionEvent;

public class ModListEventHandler {
	
	public static void refreshEvent(ActionEvent action) {
		
		ModLoader.checkInstalled();
		if(ModLoader.rebuildJars) { ModLoader.startJarRebuild(); }
		ModListGUI.modListGUIFrame.dispose();
		ModListGUI.compose();
		
	}
	
}
