package xyz.lucasallegri.launcher;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import xyz.lucasallegri.launcher.mods.ModLoader;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;

public class Boot {
	
	public static void onBootStart() {
		
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
