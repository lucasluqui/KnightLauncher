package xyz.lucasallegri.launcher;

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import xyz.lucasallegri.dialog.DialogError;
import xyz.lucasallegri.launcher.mods.ModLoader;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.DesktopUtil;
import xyz.lucasallegri.util.FileUtil;
import xyz.lucasallegri.util.SteamUtil;

public class Boot {
	
	public static void onBootStart() {
		
		/*
		 * Checking if we're being ran inside the game's directory, "getdown.txt" should always be present if so.
		 */
		if(!FileUtil.fileExists("getdown.txt")) {
			DialogError.push("You need to place this .jar inside your Spiral Knights main directory."
					+ System.lineSeparator() + SteamUtil.getGamePathWindows());
			DesktopUtil.openDir(SteamUtil.getGamePathWindows());
			return;
		}
		
		try {
			KnightLog.setup();
			SettingsProperties.setup();
		} catch (IOException ex) {
			KnightLog.logException(ex);
		}
		
		KnightLog.log.info(System.getProperty("java.home"));
		setupLookAndFeel();
		Fonts.setup();
		checkForDirectories();
		SettingsProperties.loadFromProp();
		
	}
	
	public static void onBootEnd() {
		
		ModLoader.checkInstalled();
		
		if(Settings.doRebuilds && ModLoader.rebuildJars) {
			
			ProgressBar.showBar(true);
			ProgressBar.showState(true);
			
			Thread rebuildThread = new Thread(new Runnable(){
				public void run() { ModLoader.rebuildJars(); }
			});
			rebuildThread.start();
		}
		
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
	
	private static void checkForDirectories() {
		FileUtil.createFolder("mods");
	}

}
