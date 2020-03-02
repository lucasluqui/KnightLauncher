package xyz.lucasallegri.launcher.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import xyz.lucasallegri.logging.KnightLog;

public class GameSettings {
	
	public static void load() {
		try {
			new File("extra.txt").delete();
			PrintWriter writer = new PrintWriter("extra.txt", "UTF-8");
			if(Settings.gameUseStringDeduplication) writer.println("-XX:+UseStringDeduplication");
			if(Settings.gameUseG1GC) writer.println("-XX:+UseG1GC");
			if(Settings.gameDisableExplicitGC) writer.println("-XX:+DisableExplicitGC");
			if(Settings.gameUndecoratedWindow) writer.println("-Dorg.lwjgl.opengl.Window.undecorated=true");
			writer.println(Settings.gameAdditionalArgs);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			KnightLog.logException(e);
		}
	}

}
