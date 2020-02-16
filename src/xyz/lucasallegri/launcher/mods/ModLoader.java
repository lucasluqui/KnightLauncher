package xyz.lucasallegri.launcher.mods;

import java.io.File;
import java.io.IOException;
import java.util.List;

import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.launcher.ProgressBar;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;

public class ModLoader {
	
	public static Boolean modLoadFinished = false;
	public static Boolean rebuildJars = false;
	
	public static void checkInstalled() {
		
		List<String> rawFiles = FileUtil.fileNamesInDirectory("mods/", ".zip");
		for(String file : rawFiles) {
			Mod mod = new Mod(file.substring(0, file.length() - 4), file);
			ModList.installedMods.add(mod);
			
			String hash = FileUtil.getZipHash("mods/" + file);
			String hashFilePath = "mods/" + mod.getDisplayName() + ".hash";
			if(FileUtil.fileExists(hashFilePath)) {
				try {
					String fileHash = FileUtil.readFile(hashFilePath);
					if(hash.startsWith(fileHash)) continue;
					new File(hashFilePath).delete();
					FileUtil.writeFile(hashFilePath, hash);
					rebuildJars = true;
				} catch (IOException e) {
					KnightLog.logException(e);
				}
			} else {
				FileUtil.writeFile(hashFilePath, hash);
				rebuildJars = true;
			}
		}
		
	}
	
	public static void mount() {
		
		LauncherGUI.launchButton.setEnabled(false);
		ProgressBar.setBarMax(ModList.installedMods.size() + 1);
		ProgressBar.setState("Mounting mods...");
		
		for(int i = 0; i < ModList.installedMods.size(); i++) {
			ProgressBar.setBarValue(i + 1);
			try {
				KnightLog.log.info("Mounting mod: " + ModList.installedMods.get(i).getDisplayName());
				ProgressBar.setState("Mounting mods... (" + ModList.installedMods.get(i).getDisplayName() + ")");
				FileUtil.unzip("mods/" + ModList.installedMods.get(i).getFileName(), "rsrc/");
				KnightLog.log.info(ModList.installedMods.get(i).getDisplayName() + " was mounted successfully.");
			} catch (IOException e) {
				KnightLog.logException(e);
			}
		}
		
		modLoadFinished = true;
		
		ProgressBar.setState("All mods mounted. Launching game...");
		ProgressBar.setBarMax(1);
		ProgressBar.setBarValue(1);
		LauncherGUI.launchButton.setEnabled(true);
		
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
			
		} catch (IOException ex) {
			KnightLog.logException(ex);
		}
	}

}
