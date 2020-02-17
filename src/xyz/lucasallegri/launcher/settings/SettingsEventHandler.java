package xyz.lucasallegri.launcher.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import xyz.lucasallegri.launcher.ProgressBar;
import xyz.lucasallegri.launcher.mods.ModLoader;

public class SettingsEventHandler {
	
	public static void platformChangeEvent(ItemEvent event) {
		Settings.gamePlatform = SettingsGUI.choicePlatform.getSelectedItem();
		SettingsProperties.setValue("platform", SettingsGUI.choicePlatform.getSelectedItem());
	}
	
	public static void rebuildsChangeEvent(ActionEvent event) {
		Settings.doRebuilds = SettingsGUI.checkboxRebuilds.isSelected();
		SettingsProperties.setValue("rebuilds", SettingsGUI.checkboxRebuilds.isSelected() ? "true" : "false");
	}

	public static void keepOpenChangeEvent(ActionEvent event) {
		Settings.keepOpen = SettingsGUI.checkboxKeepOpen.isSelected();
		SettingsProperties.setValue("keepOpen", SettingsGUI.checkboxKeepOpen.isSelected() ? "true" : "false");
	}
	
	public static void forceRebuildEvent() {
		
		ModLoader.startJarRebuild();
		
	}
	
	public static void createShortcutChangeEvent(ActionEvent event) {
		Settings.createShortcut = SettingsGUI.checkboxShortcut.isSelected();
		SettingsProperties.setValue("createShortcut", SettingsGUI.checkboxShortcut.isSelected() ? "true" : "false");
	}

}
