package xyz.lucasallegri.launcher.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import xyz.lucasallegri.launcher.Language;
import xyz.lucasallegri.launcher.LauncherConstants;
import xyz.lucasallegri.launcher.ProgressBar;
import xyz.lucasallegri.logging.KnightLog;

public class GameSettings {
	
	public static List<String> parsedClientArgs = new ArrayList<String>();
	
	public static void load() {
		try {
			
			ProgressBar.showBar(true);
			ProgressBar.showState(true);
			ProgressBar.setBarMax(1);
			ProgressBar.setBarValue(0);
			ProgressBar.setState(Language.getValue("m.applying"));
			
			new File("extra.txt").delete();
			PrintWriter writer = new PrintWriter("extra.txt", "UTF-8");
			
			for(String arg : LauncherConstants.STANDALONE_CLIENT_ARGS) {
				parsedClientArgs.add(arg);
			}
			
			if(Settings.gameUseStringDeduplication) {
				parsedClientArgs.add("-XX:+UseStringDeduplication");
				writer.println("-XX:+UseStringDeduplication");
			}
			if(Settings.gameUseG1GC) {
				parsedClientArgs.add("-XX:+UseG1GC");
				writer.println("-XX:+UseG1GC");
			}
			if(Settings.gameDisableExplicitGC) {
				parsedClientArgs.add("-XX:+DisableExplicitGC");
				writer.println("-XX:+DisableExplicitGC");
			}
			if(Settings.gameUndecoratedWindow) {
				parsedClientArgs.add("-Dorg.lwjgl.opengl.Window.undecorated=true");
				writer.println("-Dorg.lwjgl.opengl.Window.undecorated=true");
			}
			
			parsedClientArgs.add("-Xms" + (Settings.gameMemory / 2) + "M");
			writer.println("-Xms" + (Settings.gameMemory / 2) + "M");
			
			parsedClientArgs.add("-Xmx" + Settings.gameMemory + "M");
			writer.println("-Xmx" + Settings.gameMemory + "M");
			
			parsedClientArgs.add(Settings.gameAdditionalArgs);
			writer.println(Settings.gameAdditionalArgs);
			
			writer.close();
			
			ProgressBar.setBarValue(1);
			ProgressBar.showBar(false);
			ProgressBar.showState(false);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			KnightLog.logException(e);
		}
	}

}
