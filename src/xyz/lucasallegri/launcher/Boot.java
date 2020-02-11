package xyz.lucasallegri.launcher;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

public class Boot {
	
	public static void onBootStart() {
		
		setupLookAndFeel();
		
	}
	
	public static void onBootEnd() {
		
		populatePlatforms();
		
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
	
	private static void populatePlatforms() {
		LauncherGUI.platformChoice.add("Steam");
		LauncherGUI.platformChoice.add("Standalone");
	}

}
