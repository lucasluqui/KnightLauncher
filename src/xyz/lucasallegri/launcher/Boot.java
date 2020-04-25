package xyz.lucasallegri.launcher;

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import mdlaf.MaterialLookAndFeel;
import xyz.lucasallegri.dialog.DialogError;
import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.mods.ModList;
import xyz.lucasallegri.launcher.mods.ModLoader;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.DesktopUtil;
import xyz.lucasallegri.util.FileUtil;
import xyz.lucasallegri.util.INetUtil;
import xyz.lucasallegri.util.SteamUtil;
import xyz.lucasallegri.util.SystemUtil;

public class Boot {
	
	public static void onBootStart() {
		
		checkStartLocation();
		setupHTTPSProtocol();
		setupLauncherStyle();
		
		try {
			KnightLog.setup();
			SettingsProperties.setup();
			SettingsProperties.loadFromProp();
		} catch (IOException ex) {
			KnightLog.logException(ex);
		}
		
		Language.setup();
		checkDirectories();
		checkShortcut();
		checkVersion();
		DiscordInstance.start();
		Fonts.setup();
		
	}
	
	public static void onBootEnd() {
		
		if(!JVMPatcher.isPatched() && SystemUtil.is64Bit() && SystemUtil.hasValidJavaHome()) JVMPatcher.start();
		
		ModLoader.checkInstalled();
		if(Settings.doRebuilds && ModLoader.rebuildFiles) ModLoader.startFileRebuild();
		
		DiscordInstance.setPresence(Language.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
		
	}
	
	private static void setupLauncherStyle() {
		
		for( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
			if( "Windows".equals(info.getName()) ) {
				try {
					UIManager.setLookAndFeel(new MaterialLookAndFeel());
				} catch (UnsupportedLookAndFeelException e) {
					KnightLog.logException(e);
				}
			}
		}
		
	}
	
	private static void checkDirectories() {
		FileUtil.createDir("mods");
		FileUtil.createDir("KnightLauncher/logs/");
	}
	
	private static void checkStartLocation() {
		/*
		 * Checking if we're being ran inside the game's directory, "getdown.txt" should always be present if so.
		 */
		if(!FileUtil.fileExists("getdown-pro.jar")) {
			DialogError.push("You need to place this .jar inside your Spiral Knights main directory."
					+ System.lineSeparator() + SteamUtil.getGamePathWindows());
			DesktopUtil.openDir(SteamUtil.getGamePathWindows());
			System.exit(1);
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
	
	private static void checkVersion() {
		String latestVer = INetUtil.getWebpageContent(LauncherConstants.VERSION_QUERY_URL);
		if(latestVer == null) {
			LauncherGUI.offlineMode = true;
		} else if (!latestVer.contains(LauncherConstants.VERSION)) {
			LauncherGUI.showUpdateButton = true;
		}
	}
	
	private static void setupHTTPSProtocol() {
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		System.setProperty("http.agent", "Mozilla/5.0");
		System.setProperty("https.agent", "Mozilla/5.0");
	}

}
