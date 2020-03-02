package xyz.lucasallegri.launcher.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import xyz.lucasallegri.launcher.Language;
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
		
		ModLoader.startFileRebuild();
		
	}
	
	public static void createShortcutChangeEvent(ActionEvent event) {
		Settings.createShortcut = SettingsGUI.checkboxShortcut.isSelected();
		SettingsProperties.setValue("createShortcut", SettingsGUI.checkboxShortcut.isSelected() ? "true" : "false");
	}
	
	public static void languageChangeEvent(ItemEvent event) {
		Settings.lang = Language.getLangCode(SettingsGUI.choiceLanguage.getSelectedItem());
		SettingsProperties.setValue("lang", Language.getLangCode(SettingsGUI.choiceLanguage.getSelectedItem()));
	}
	
	public static void useStringDeduplicationChangeEvent(ActionEvent action) {
		Settings.gameUseStringDeduplication = SettingsGUI.checkboxStringDeduplication.isSelected();
		SettingsProperties.setValue("game.useStringDeduplication", SettingsGUI.checkboxStringDeduplication.isSelected()  ? "true" : "false");
	}

	public static void useG1GCChangeEvent(ActionEvent action) {
		Settings.gameUseG1GC = SettingsGUI.checkboxG1GC.isSelected();
		SettingsProperties.setValue("game.useG1GC", SettingsGUI.checkboxG1GC.isSelected()  ? "true" : "false");
	}
	
	public static void disableExplicitGCChangeEvent(ActionEvent action) {
		Settings.gameDisableExplicitGC = SettingsGUI.checkboxExplicitGC.isSelected();
		SettingsProperties.setValue("game.disableExplicitGC", SettingsGUI.checkboxExplicitGC.isSelected()  ? "true" : "false");
	}
	
	public static void undecoratedWindowChangeEvent(ActionEvent action) {
		Settings.gameUndecoratedWindow = SettingsGUI.checkboxUndecorated.isSelected();
		SettingsProperties.setValue("game.undecoratedWindow", SettingsGUI.checkboxUndecorated.isSelected()  ? "true" : "false");
	}
	
	public static void saveAdditionalArgs() {
		Settings.gameAdditionalArgs = SettingsGUI.argumentsPane.getText();
		SettingsProperties.setValue("game.additionalArgs", SettingsGUI.argumentsPane.getText());
	}

}
