package xyz.lucasallegri.launcher.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

public class SettingsEventHandler {
	
	public static void platformChangeEvent(ItemEvent event) {
		Settings.gamePlatform = SettingsGUI.choicePlatform.getSelectedItem();
	}

}
