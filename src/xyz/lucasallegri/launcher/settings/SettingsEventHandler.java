package xyz.lucasallegri.launcher.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

public class SettingsEventHandler {
	
	public static void platformChangeEvent(ItemEvent event) {
		Settings.gamePlatform = SettingsGUI.choicePlatform.getSelectedItem();
		SettingsProperties.setValue("platform", SettingsGUI.choicePlatform.getSelectedItem());
	}
	
	public static void rebuildsChangeEvent(ActionEvent event) {
		Settings.doRebuilds = SettingsGUI.checkboxRebuilds.isSelected();
		SettingsProperties.setValue("rebuilds", SettingsGUI.checkboxRebuilds.isSelected() ? "true" : "false");
	}

}
