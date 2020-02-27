package xyz.lucasallegri.launcher;

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import xyz.lucasallegri.dialog.DialogError;
import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.mods.ModList;
import xyz.lucasallegri.launcher.mods.ModLoader;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.DesktopUtil;
import xyz.lucasallegri.util.FileUtil;
import xyz.lucasallegri.util.SteamUtil;
import xyz.lucasallegri.util.SystemUtil;

public class Boot {
	
	public static void onBootStart() {
		
		checkStartLocation();
		
		try {
			KnightLog.setup();
			SettingsProperties.setup();
			SettingsProperties.loadFromProp();
		} catch (IOException ex) {
			KnightLog.logException(ex);
		}
		
		checkDirectories();
		checkShortcut();
		
		DiscordInstance.start();
		DiscordInstance.setPresence("Booting up");
		
		setupLookAndFeel();
		Fonts.setup();
		
	}
	
	public static void onBootEnd() {
		
		ModLoader.checkInstalled();
		
		if(Settings.doRebuilds && ModLoader.rebuildJars) ModLoader.startJarRebuild();
		
		DiscordInstance.setPresence("Ready for launch (" + ModList.installedMods.size() + " mods)");
		
	}
	
	private static void setupLookAndFeel() {
		
		for( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
			if( "Windows".equals(info.getName()) ) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					KnightLog.logException(e);
				}
			}
		}
		
	}
	
	private static void checkDirectories() {
		FileUtil.createFolder("mods");
	}
	
	private static void checkStartLocation() {
		/*
		 * Checking if we're being ran inside the game's directory, "getdown.txt" should always be present if so.
		 */
		if(!FileUtil.fileExists("getdown.txt")) {
			DialogError.push("You need to place this .jar inside your Spiral Knights main directory."
					+ System.lineSeparator() + SteamUtil.getGamePathWindows());
			DesktopUtil.openDir(SteamUtil.getGamePathWindows());
			return;
		}
	}
	
	private static void checkShortcut() {
		/*
		 * Create a shortcut to the application if there's none.
		 */
		if(SystemUtil.isWindows() && Settings.createShortcut
				&& !FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + LauncherConstants.LNK_FILE_NAME)) {
			
			DesktopUtil.createShellLink(System.getProperty("java.home") + "\\bin\\javaw.exe", 
										"-jar \"" + System.getProperty("user.dir") + "\\KnightLauncher.jar\"", 
										System.getProperty("user.dir"), 
										System.getProperty("user.dir") + "\\icon-128.ico", 
										"Start KnightLauncher", 
										LauncherConstants.LNK_FILE_NAME
										);
		}
	}

}
