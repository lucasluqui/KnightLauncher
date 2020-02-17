package xyz.lucasallegri.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

}
