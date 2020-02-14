package xyz.lucasallegri.launcher;

import xyz.lucasallegri.launcher.mods.Mods;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.util.ProcessUtil;
import xyz.lucasallegri.util.SteamUtil;
import java.awt.event.ActionEvent;

public class LauncherEventHandler {
	
	public static void launchEvent(ActionEvent action) {
		
		Thread launchThread = new Thread(new Runnable(){
			public void run() {
				
				Mods.mount();
				
				if(Mods.modSetupFinished) {
					if(Settings.gamePlatform.startsWith("Steam")) {
						
						try {
							SteamUtil.startGameById("99900");
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					} else {
						
						ProcessUtil.startApplication(LauncherConstants.STANDALONE_CLIENT_ARGS);
						
					}
					
					LauncherGUI.launcherGUIForm.dispose();
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
