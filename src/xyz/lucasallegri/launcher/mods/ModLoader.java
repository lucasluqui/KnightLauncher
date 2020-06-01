package xyz.lucasallegri.launcher.mods;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.Language;
import xyz.lucasallegri.launcher.LauncherConstants;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.launcher.ProgressBar;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.Compressor;
import xyz.lucasallegri.util.FileUtil;
import xyz.lucasallegri.util.SystemUtil;

public class ModLoader {
	
	public static Boolean modLoadFinished = false;
	public static Boolean rebuildFiles = false;
	
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
				mod.setCompatibilityVersion(modJson.getString("compatibility"));
			}
			ModList.installedMods.add(mod);
			KnightLog.log.info(mod.toString());
			
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
					rebuildFiles = true;
				} catch (IOException e) {
					KnightLog.logException(e);
				}
			} else {
				FileUtil.writeFile(hashFilePath, hash);
				rebuildFiles = true;
			}
		}
		
		/*
		 * Check if there's a new or removed mod since last execution, rebuild will be needed in that case.
		 */
		if(Integer.parseInt(SettingsProperties.getValue("modloader.lastModCount")) != ModList.installedMods.size()) {
			SettingsProperties.setValue("modloader.lastModCount", Integer.toString(ModList.installedMods.size()));
			rebuildFiles = true;
		}
		
	}
	
	public static void mount() {
		
		LauncherGUI.launchButton.setEnabled(false);
		ProgressBar.showBar(true);
		ProgressBar.showState(true);
		ProgressBar.setBarMax(ModList.installedMods.size() + 1);
		ProgressBar.setState(Language.getValue("m.mount"));
		DiscordInstance.setPresence(Language.getValue("m.mount_start"));
		
		for(int i = 0; i < ModList.installedMods.size(); i++) {
			ProgressBar.setBarValue(i + 1);
			Compressor.unzip("./mods/" + ModList.installedMods.get(i).getFileName(), "./rsrc/", SystemUtil.isMac());
			KnightLog.log.info(ModList.installedMods.get(i).getDisplayName() + " was mounted successfully.");
		}
		
		modLoadFinished = true;
		
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
		
		LauncherGUI.launchButton.setEnabled(false);
		LauncherGUI.settingsButton.setEnabled(false);
		try { SettingsGUI.forceRebuildButton.setEnabled(false); } catch(Exception e) {}
		DiscordInstance.setPresence("Rebuilding...");
		
		String[] jarFiles = {"full-music-bundle.jar", "full-rest-bundle.jar", "intro-bundle.jar"};
		
		ProgressBar.showBar(true);
		ProgressBar.showState(true);
		ProgressBar.setBarMax(jarFiles.length + 1);
		ProgressBar.setState(Language.getValue("m.clean"));
		
		// Iterate through all 3 .jar files to clean up the game files.
		for(int i = 0; i < jarFiles.length; i++) {
			ProgressBar.setBarValue(i + 1);
			DiscordInstance.setPresence(Language.getValue("presence.rebuilding", new String[]{String.valueOf(i + 1), String.valueOf(jarFiles.length)}));
			Compressor.unzip("./rsrc/" + jarFiles[i], "./rsrc/", false);
		}
		
		// Check for decompiled configs (.xml) present in the configs folder and delete them on sight.
		List<String> configs = FileUtil.fileNamesInDirectory(LauncherConstants.USER_DIR + "/rsrc/config", ".xml");
		for(String config : configs) {
			new File(LauncherConstants.USER_DIR + "/rsrc/config/" + config).delete();
		}
		
		ProgressBar.setBarValue(jarFiles.length + 1);
		ProgressBar.showBar(false);
		ProgressBar.showState(false);
		rebuildFiles = false;
		LauncherGUI.launchButton.setEnabled(true);
		LauncherGUI.settingsButton.setEnabled(true);
		try { SettingsGUI.forceRebuildButton.setEnabled(true); } catch(Exception e) {}
		DiscordInstance.setPresence(Language.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
	}

}
