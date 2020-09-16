package com.lucasallegri.launcher.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.lucasallegri.launcher.LanguageManager;
import com.lucasallegri.launcher.ProgressBar;
import com.lucasallegri.logging.Logging;

public class GameSettings {
	
	public static void load() {
		try {
			
			ProgressBar.showBar(true);
			ProgressBar.showState(true);
			ProgressBar.setBarMax(1);
			ProgressBar.setBarValue(0);
			ProgressBar.setState(LanguageManager.getValue("m.apply"));
			
			new File("extra.txt").delete();
			PrintWriter writer = new PrintWriter("extra.txt", "UTF-8");
			
			if(Settings.gameUseStringDeduplication) writer.println("-XX:+UseStringDeduplication");
			if(Settings.gameDisableExplicitGC) writer.println("-XX:+DisableExplicitGC");
			if(Settings.gameUseCustomGC) writer.println("-XX:+Use" + Settings.gameGarbageCollector + "GC");
			if(Settings.gameUndecoratedWindow) writer.println("-Dorg.lwjgl.opengl.Window.undecorated=true");
			
			writer.println("-Xms" + (Settings.gameMemory / 2) + "M");
			writer.println("-Xmx" + Settings.gameMemory + "M");
			writer.println(Settings.gameAdditionalArgs);
			
			writer.close();
			
			ProgressBar.setBarValue(1);
			ProgressBar.showBar(false);
			ProgressBar.showState(false);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			Logging.logException(e);
		}
	}

}
