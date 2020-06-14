package xyz.lucasallegri.launcher;

import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.mods.ModLoader;
import xyz.lucasallegri.launcher.settings.GameSettings;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.ProcessUtil;
import xyz.lucasallegri.util.SteamUtil;
import xyz.lucasallegri.util.SystemUtil;

import java.awt.event.ActionEvent;

public class LauncherEventHandler {
	
	public static void launchGameEvent(ActionEvent action) {
		
		Thread launchThread = new Thread(new Runnable(){
			public void run() {
				
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
						
						if(!SystemUtil.isWindows()) {
							ProcessUtil.startApplication(LauncherConstants.STANDALONE_LAUNCHER_ARGS_LINUX_MAC);
						} else {
							ProcessUtil.startApplication(LauncherConstants.STANDALONE_LAUNCHER_ARGS);
						}
						
					}
					
					if(Settings.keepOpen) {
						DiscordInstance.setPresence(Language.getValue("presence.launch_ready"));
					} else {
						DiscordInstance.stop();
						LauncherGUI.launcherGUIFrame.dispose();
					}
				}
			}
		});
		launchThread.start();
		
	}

}
