package com.lucasallegri.util;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageUtil {
	
	public static Image getImageFromURL(String url) {
		
		Image image = null;
		
		try {
			URL _url = new URL(url);
			image = ImageIO.read(_url);
			image = image.getScaledInstance(589, 234, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return image;
	}

}
