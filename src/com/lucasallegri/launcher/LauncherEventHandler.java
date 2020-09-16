package com.lucasallegri.launcher;

import java.awt.event.ActionEvent;

import com.lucasallegri.discord.DiscordInstance;
import com.lucasallegri.launcher.mods.ModLoader;
import com.lucasallegri.launcher.settings.GameSettings;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.logging.KnightLog;
import com.lucasallegri.util.ProcessUtil;
import com.lucasallegri.util.SteamUtil;
import com.lucasallegri.util.SystemUtil;

public class LauncherEventHandler {
	
	public static void launchGameEvent(ActionEvent action) {
		
		Thread launchThread = new Thread(new Runnable(){
			public void run() {
				
				if(ModLoader.mountRequired) ModLoader.mount();
				GameSettings.load();
				
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
				
				DiscordInstance.stop();
				if(Settings.useIngameRPC) ProcessUtil.startApplication(new String[] {".\\KnightLauncher\\modules\\skdiscordrpc\\SK-DiscordRPC.exe"});
				if(!Settings.keepOpen) LauncherGUI.launcherGUIFrame.dispose();
				
			}
		});
		launchThread.start();
		
	}
	
	public static void launchGameAltEvent(ActionEvent action) {
		
		Thread launchAltThread = new Thread(new Runnable(){
			public void run() {
				
				Settings.gameMemory = 512;
				GameSettings.load();
					
				if(!SystemUtil.isWindows()) {
					ProcessUtil.startApplication(LauncherConstants.STANDALONE_LAUNCHER_ARGS_LINUX_MAC);
				} else {
					ProcessUtil.startApplication(LauncherConstants.STANDALONE_LAUNCHER_ARGS);
				}
				
				DiscordInstance.stop();
				
			}
		});
		launchAltThread.start();
		
	}

}
