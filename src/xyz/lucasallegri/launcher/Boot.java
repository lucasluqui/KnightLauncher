package xyz.lucasallegri.launcher;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import xyz.lucasallegri.launcher.mods.Mods;
import xyz.lucasallegri.util.FileUtil;

public class Boot {
	
	public static void onBootStart() {
		
		setupLookAndFeel();
		checkForDirectories();
		
	}
	
	public static void onBootEnd() {
		
		Thread rebuildThread = new Thread(new Runnable(){
			public void run() { Mods.rebuildJars(); }
		});
		rebuildThread.start();
		
		Mods.checkInstalled();
		
	}
	
	private static void setupLookAndFeel() {
		
		for( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
			if( "Windows".equals(info.getName()) ) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private static void checkForDirectories() {
		FileUtil.createFolder("mods");
	}

}
