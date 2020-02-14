package xyz.lucasallegri.launcher.mods;

import java.io.IOException;

import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.launcher.ProgressBar;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;

public class ModLoader {
	
	public static Boolean modLoadFinished = false;
	
	public static void checkInstalled() {
		
		ModList.installedMods = FileUtil.fileNamesInDirectory("mods/", ".zip");
		
	}
	
	public static void mount() {
		
		ModLoader.checkInstalled();
		
		ProgressBar.setBarMax(ModList.installedMods.size() + 1);
		ProgressBar.setState("Mounting mods...");
		
		for(int i = 0; i < ModList.installedMods.size(); i++) {
			ProgressBar.setBarValue(i + 1);
			try {
				KnightLog.log.info("Mounting mod: " + ModList.installedMods.get(i));
				FileUtil.unzip("mods/" + ModList.installedMods.get(i), "rsrc/");
				KnightLog.log.info(ModList.installedMods.get(i) + " was mounted successfully.");
			} catch (IOException e) {
				KnightLog.log.severe(e.getLocalizedMessage());
			}
		}
		
		modLoadFinished = true;
		
		ProgressBar.setState("All mods mounted. Launching game...");
		ProgressBar.setBarMax(1);
		ProgressBar.setBarValue(1);
		
	}
	
	public static void rebuildJars() {
		
		LauncherGUI.launchButton.setEnabled(false);
		
		ProgressBar.setBarMax(4);
		ProgressBar.setState("Rebuilding game files...");
		
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
			ProgressBar.setState("Rebuild complete, game launch ready.");
			LauncherGUI.launchButton.setEnabled(true);
			
		} catch (IOException e1) {
			KnightLog.log.severe(e1.getLocalizedMessage());
		}
	}

}
