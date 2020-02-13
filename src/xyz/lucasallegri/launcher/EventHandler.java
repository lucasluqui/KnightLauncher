package xyz.lucasallegri.launcher;

import xyz.lucasallegri.launcher.mods.Mods;
import xyz.lucasallegri.util.SteamUtil;
import java.awt.event.ActionEvent;

public class EventHandler {
	
	public static void launchEvent(ActionEvent action) {
		
		Thread launchThread = new Thread(new Runnable(){
			public void run() {
				
				Mods.mount();
				
				if(Mods.modSetupFinished) {
					try {
						SteamUtil.startGameById("99900");
						LauncherGUI.launcherGUIForm.dispose();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		launchThread.start();
		
	}
	
	public static void rebuildEvent(ActionEvent action) {
		new Thread(new Runnable(){
			public void run() { Mods.rebuildJars(); }
		});
	}

}
