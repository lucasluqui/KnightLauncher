package xyz.lucasallegri.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import mslinks.ShellLink;
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
	
	public static void createShellLink(String target, String args, String workDir, String ico, String hover, String name) {
		ShellLink sl = ShellLink.createLink(target)
				.setCMDArgs(args)
				.setWorkingDir(workDir)
				.setIconLocation(ico)
				.setName(hover);
		try {
			sl.saveTo(getPathToDesktop() + "/" + name + ".lnk");
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}

}
