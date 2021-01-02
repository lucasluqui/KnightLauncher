package com.lucasallegri.launcher.mods;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import org.json.JSONObject;

import com.lucasallegri.discord.DiscordInstance;
import com.lucasallegri.launcher.LanguageManager;
import com.lucasallegri.launcher.LauncherConstants;
import com.lucasallegri.launcher.LauncherGUI;
import com.lucasallegri.launcher.Modules;
import com.lucasallegri.launcher.ProgressBar;
import com.lucasallegri.launcher.settings.SettingsGUI;
import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.util.Compressor;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.SystemUtil;

import static com.lucasallegri.launcher.mods.Log.log;

public class ModLoader {
	
	public static Boolean mountRequired = false;
	public static Boolean rebuildRequired = false;
	
	public static void checkInstalled() {
		
		// Clean the list in case something remains in it.
		if(ModList.installedMods.size() > 0) ModList.installedMods.clear();
		
		/*
		 * Append all .zip and .jar files inside the mod folder into an ArrayList.
		 */
		List<String> rawFiles = FileUtil.fileNamesInDirectory("mods/", ".zip");
		rawFiles.addAll(FileUtil.fileNamesInDirectory("mods/", ".jar"));
		
		for(String file : rawFiles) {
			JSONObject modJson;
			try {
				modJson = new JSONObject(Compressor.readFileInsideZip(LauncherConstants.USER_DIR + "/mods/" + file, "mod.json")).getJSONObject("mod");
			} catch(Exception e) {
				modJson = null;
			}
			Mod mod = new Mod(file);
			if(modJson != null) {
				mod.setDisplayName(modJson.getString("name"));
				mod.setDescription(modJson.getString("description"));
				mod.setAuthor(modJson.getString("author"));
				mod.setVersion(modJson.getString("version"));
			}
			ModList.installedMods.add(mod);
			log.info("A mod was found", new Object[] {"name", mod.displayName, "object", mod.toString()});
			
			/*
			 * Compute a hash for each mod file and check that it matches on every execution, if it doesn't, then rebuild.
			 */
			String hash = Compressor.getZipHash("mods/" + file);
			String hashFilePath = "mods/" + mod.getFileName() + ".hash";
			if(FileUtil.fileExists(hashFilePath)) {
				try {
					String fileHash = FileUtil.readFile(hashFilePath);
					if(hash.startsWith(fileHash)) continue;
					new File(hashFilePath).delete();
					FileUtil.writeFile(hashFilePath, hash);
					rebuildRequired = true;
					mountRequired = true;
				} catch (IOException e) {
					log.error(e);
				}
			} else {
				FileUtil.writeFile(hashFilePath, hash);
				rebuildRequired = true;
				mountRequired = true;
			}
		}
		
		/*
		 * Check if there's a new or removed mod since last execution, rebuild will be needed in that case.
		 */
		if(Integer.parseInt(SettingsProperties.getValue("modloader.lastModCount")) != ModList.installedMods.size()) {
			SettingsProperties.setValue("modloader.lastModCount", Integer.toString(ModList.installedMods.size()));
			rebuildRequired = true;
			mountRequired = true;
		}
		
	}
	
	public static void mount() {
		
		LauncherGUI.launchButton.setEnabled(false);
		ProgressBar.showBar(true);
		ProgressBar.showState(true);
		ProgressBar.setBarMax(ModList.installedMods.size() + 1);
		ProgressBar.setState(LanguageManager.getValue("m.mount"));
		DiscordInstance.setPresence(LanguageManager.getValue("m.mount"));
		
		for(int i = 0; i < ModList.installedMods.size(); i++) {
			ProgressBar.setBarValue(i + 1);
			Compressor.unzip("./mods/" + ModList.installedMods.get(i).getFileName(), "./rsrc/", SystemUtil.isMac());
			log.info("Mod mounted successfully", new Object[] {"mod", ModList.installedMods.get(i).getDisplayName()});
		}
		
		log.info("Extracting safeguard...");
		Modules.setupSafeguard();
		log.info("Extracted safeguard.");
		
		ProgressBar.showBar(false);
		ProgressBar.showState(false);
		LauncherGUI.launchButton.setEnabled(true);
		
	}
	
	public static void startFileRebuild() {
		Thread rebuildThread = new Thread(new Runnable() {
			public void run() {
				rebuildFiles();
			}
		});
		rebuildThread.start();
	}	
	
	private static void rebuildFiles() {
		
		try {
			LauncherGUI.launchButton.setEnabled(false);
			LauncherGUI.settingsButton.setEnabled(false);
			SettingsGUI.forceRebuildButton.setEnabled(false); 
		} catch(Exception e) {}
		String[] jarFiles = {"full-music-bundle.jar", "full-rest-bundle.jar", "intro-bundle.jar"};
		
		ProgressBar.showBar(true);
		ProgressBar.showState(true);
		ProgressBar.setBarMax(jarFiles.length + 1);
		DiscordInstance.setPresence(LanguageManager.getValue("m.clean"));
		ProgressBar.setState(LanguageManager.getValue("m.clean"));
		
		// Iterate through all 3 .jar files to clean up the game files.
		for(int i = 0; i < jarFiles.length; i++) {
			ProgressBar.setBarValue(i + 1);
			DiscordInstance.setPresence(LanguageManager.getValue("presence.rebuilding", new String[]{String.valueOf(i + 1), String.valueOf(jarFiles.length)}));
			try {
				FileUtil.unpackJar(new ZipFile("./rsrc/" + jarFiles[i]), new File("./rsrc/"), false);
			} catch (IOException e) {
				log.error(e);
			}
			//Compressor.unzip("./rsrc/" + jarFiles[i], "./rsrc/", false);
		}
		
		// Check for decompiled configs (.xml) present in the configs folder and delete them on sight.
		List<String> configs = FileUtil.fileNamesInDirectory(LauncherConstants.USER_DIR + "/rsrc/config", ".xml");
		for(String config : configs) {
			new File(LauncherConstants.USER_DIR + "/rsrc/config/" + config).delete();
		}
		
		ProgressBar.setBarValue(jarFiles.length + 1);
		ProgressBar.showBar(false);
		ProgressBar.showState(false);
		rebuildRequired = false;
		try {
			LauncherGUI.launchButton.setEnabled(true);
			LauncherGUI.settingsButton.setEnabled(true);
			SettingsGUI.forceRebuildButton.setEnabled(true);
		} catch(Exception e) {}
		DiscordInstance.setPresence(LanguageManager.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
	}

}
