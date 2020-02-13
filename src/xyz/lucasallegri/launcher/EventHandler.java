package xyz.lucasallegri.launcher;

import xyz.lucasallegri.launcher.mods.Mods;
import xyz.lucasallegri.util.FileUtil;
import xyz.lucasallegri.util.SteamUtil;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class EventHandler {
	
	public static void launchEvent(ActionEvent action) {
		
		Thread launchThread = new Thread(new Runnable(){
			public void run() {
				ProgressBar.setBarMax(4);
				ProgressBar.showBar();
				ProgressBar.setState("Rebuilding game files...");
				ProgressBar.showState();
				
				try {
					
					ProgressBar.setBarValue(1);
					ProgressBar.setState("Rebuilding full-music-bundle.jar...");
					FileUtil.unzip("rsrc/full-music-bundle.jar", "rsrc/");
					
					ProgressBar.setBarValue(2);
					ProgressBar.setState("Rebuilding full-rest-bundle.jar...");
					FileUtil.unzip("rsrc/full-rest-bundle.jar", "rsrc/");
					
					ProgressBar.setBarValue(3);
					ProgressBar.setState("Rebuilding intro-bundle.jar...");
					FileUtil.unzip("rsrc/intro-bundle.jar", "rsrc/");
					
					ProgressBar.setBarValue(4);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
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

}
