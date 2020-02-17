package xyz.lucasallegri.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import mslinks.ShellLink;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.logging.KnightLog;

public class DesktopUtil {
	
	public static void openDir(String path) {
		try {
			Desktop.getDesktop().open(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            KnightLog.logException(e);
	        }
	    }
	}

	public static void openWebpage(String url) {
	    try {
	        openWebpage(new URL(url).toURI());
	    } catch (URISyntaxException | MalformedURLException e) {
	        KnightLog.logException(e);
	    }
	}
	
	public static String getPathToDesktop() {
		return System.getProperty("user.home") + File.separator + "Desktop" + File.separator;
	}
	
	public static void createShortcut() {
		ShellLink sl = ShellLink.createLink(System.getProperty("java.home") + "\\bin\\javaw.exe")
				.setCMDArgs("-jar \"" + System.getProperty("user.dir") + "\\KnightLauncher.jar\"")
				.setWorkingDir(System.getProperty("user.dir"))
				.setIconLocation(System.getProperty("user.dir") + "\\icon-128.ico")
				.setName("Start KnightLauncher");
		
		try {
			sl.saveTo(getPathToDesktop() + "/Knight Launcher.lnk");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
