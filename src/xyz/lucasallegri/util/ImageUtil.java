package xyz.lucasallegri.util;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

import xyz.lucasallegri.logging.KnightLog;

public class ImageUtil {
	
	public static Image getImageFromURL(String url) {
		
		Image image = null;
		
		try {
			URL _url = new URL(url);
			image = ImageIO.read(_url);
			image = image.getScaledInstance(514, 311, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			KnightLog.log.severe(e.getLocalizedMessage());
		}
		
		return image;
	}

}
