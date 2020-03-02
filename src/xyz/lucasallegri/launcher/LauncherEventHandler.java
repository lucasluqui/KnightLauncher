package xyz.lucasallegri.launcher;

import xyz.lucasallegri.launcher.mods.ModLoader;
import xyz.lucasallegri.launcher.settings.GameSettings;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.ProcessUtil;
import xyz.lucasallegri.util.SteamUtil;

import java.awt.event.ActionEvent;

public class LauncherEventHandler {
	
	public static void launchGameEvent(ActionEvent action) {
		
		Thread launchThread = new Thread(new Runnable(){
			public void run() {
				
				ProgressBar.showBar(true);
				ProgressBar.showState(true);
				
				ModLoader.mount();
				GameSettings.load();
				
				if(ModLoader.modLoadFinished) {
					if(Settings.gamePlatform.startsWith("Steam")) {
						try {
							SteamUtil.startGameById("99900");
						} catch (Exception e) {
							KnightLog.logException(e);
						}
					} else {
						ProcessUtil.startApplication(LauncherConstants.STANDALONE_CLIENT_ARGS);
					}
					
					if(!Settings.keepOpen) LauncherGUI.launcherGUIFrame.dispose();
				}
			}
		});
		launchThread.start();
		
	}

}
