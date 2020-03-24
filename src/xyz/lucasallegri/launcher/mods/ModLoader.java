package xyz.lucasallegri.launcher.mods;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.Language;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.launcher.ProgressBar;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.Compressor;
import xyz.lucasallegri.util.FileUtil;

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
			JSONObject modJson = new JSONObject(Compressor.readFileInsideZip("mods/" + file, "mod.json")).getJSONObject("mod");
			Mod mod = new Mod(file);
			if(modJson != null) {
				mod.setDisplayName(modJson.getString("name"));
				mod.setDescription(modJson.getString("description"));
				mod.setAuthor(modJson.getString("author"));
				mod.setVersion(modJson.getString("version"));
				mod.setVersion(modJson.getString("compatibility"));
			}
			ModList.installedMods.add(mod);
			KnightLog.log.info(mod.toString());
			
			/*
			 * Compute a hash for each mod file and check that it matches on every execution, if it doesn't, then rebuild.
			 */
			String hash = Compressor.getZipHash("mods/" + file);
			String hashFilePath = "mods/" + mod.getDisplayName() + ".hash";
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
		if(Integer.parseInt(SettingsProperties.getValue("lastModCount")) != ModList.installedMods.size()) {
			SettingsProperties.setValue("lastModCount", Integer.toString(ModList.installedMods.size()));
			rebuildFiles = true;
		}
		
	}
	
	public static void mount() {
		
		LauncherGUI.launchButton.setEnabled(false);
		ProgressBar.setBarMax(ModList.installedMods.size() + 1);
		ProgressBar.setState(Language.getValue("m.mount_start"));
		DiscordInstance.setPresence(Language.getValue("m.mount_start"));
		
		for(int i = 0; i < ModList.installedMods.size(); i++) {
			ProgressBar.setBarValue(i + 1);
			ProgressBar.setState(Language.getValue("m.mounting", ModList.installedMods.get(i).getDisplayName()));
			Compressor.unzip4j("mods/" + ModList.installedMods.get(i).getFileName(), "rsrc/");
			KnightLog.log.info(ModList.installedMods.get(i).getDisplayName() + " was mounted successfully.");
		}
		
		modLoadFinished = true;
		
		ProgressBar.setState(Language.getValue("m.mount_finished"));
		ProgressBar.setBarMax(1);
		ProgressBar.setBarValue(1);
		LauncherGUI.launchButton.setEnabled(true);
		
	}
	
	public static void startFileRebuild() {
		ProgressBar.showBar(true);
		ProgressBar.showState(true);
		
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
		
		ProgressBar.setBarMax(jarFiles.length + 1);
		ProgressBar.setState(Language.getValue("m.rebuild_start"));
		
		for(int i = 0; i < jarFiles.length; i++) {
			ProgressBar.setBarValue(i + 1);
			ProgressBar.setState(Language.getValue("m.rebuilding", jarFiles[i]));
			DiscordInstance.setPresence(Language.getValue("presence.rebuilding", new String[]{String.valueOf(i + 1), String.valueOf(jarFiles.length)}));
			Compressor.unzip4j("rsrc/" + jarFiles[i], "rsrc/");
		}
		
		ProgressBar.setBarValue(jarFiles.length + 1);
		ProgressBar.setState(Language.getValue("m.rebuild_complete"));
		rebuildFiles = false;
		LauncherGUI.launchButton.setEnabled(true);
		LauncherGUI.settingsButton.setEnabled(true);
		try { SettingsGUI.forceRebuildButton.setEnabled(true); } catch(Exception e) {}
		DiscordInstance.setPresence(Language.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
	}

}
