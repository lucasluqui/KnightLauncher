package xyz.lucasallegri.launcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import xyz.lucasallegri.util.FileUtil;

public class Boot {
	
	public static void onBootStart() {
		
		setupLookAndFeel();
		checkForDirectories();
		
	}
	
	public static void onBootEnd() {
		
		
		
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
