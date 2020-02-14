package xyz.lucasallegri.launcher;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import xyz.lucasallegri.launcher.mods.Mods;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;

public class Boot {
	
	public static void onBootStart() {
		
		setupLookAndFeel();
		checkForDirectories();
		SettingsProperties.loadFromProp();
		
	}
	
	public static void onBootEnd() {
		
		if(Settings.doRebuilds) {
			Thread rebuildThread = new Thread(new Runnable(){
				public void run() { Mods.rebuildJars(); }
			});
			rebuildThread.start();
		}
		
		Mods.checkInstalled();
		
	}
	
	private static void setupLookAndFeel() {
		
		for( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
			if( "Windows".equals(info.getName()) ) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					KnightLog.log.severe(e.getLocalizedMessage());
				}
			}
		}
		
	}
	
	private static void checkForDirectories() {
		FileUtil.createFolder("mods");
	}

}
