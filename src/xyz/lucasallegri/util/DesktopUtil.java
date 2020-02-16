package xyz.lucasallegri.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class DesktopUtil {
	
	public static void openDir(String path) {
		try {
			Desktop.getDesktop().open(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
