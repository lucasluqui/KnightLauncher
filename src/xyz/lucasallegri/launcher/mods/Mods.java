package xyz.lucasallegri.launcher.mods;

import java.io.IOException;

import xyz.lucasallegri.launcher.ProgressBar;
import xyz.lucasallegri.util.FileUtil;

public class Mods {
	
	public static Boolean modSetupFinished = false;
	
	public static void checkInstalled() {
		
		ModList.installedMods = FileUtil.fileNamesInDirectory("mods/");
		
	}
	
	public static void mount() {
		
		Mods.checkInstalled();
		
		ProgressBar.setBarMax(ModList.installedMods.size() + 1);
		ProgressBar.setState("Mounting mods...");
		
		for(int i = 0; i < ModList.installedMods.size(); i++) {
			ProgressBar.setBarValue(i + 1);
			try {
				FileUtil.unzip("mods/" + ModList.installedMods.get(i), "rsrc/");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		modSetupFinished = true;
		
		ProgressBar.setState("All mods mounted. Launching game...");
		ProgressBar.setBarMax(1);
		ProgressBar.setBarValue(1);
		
	}

}
