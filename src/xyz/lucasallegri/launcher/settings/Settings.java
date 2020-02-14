package xyz.lucasallegri.launcher.settings;

public class Settings {
	
	public static String apiEndpoint = "px-api.lucasallegri.xyz:5500/v1/";
	public static String gamePlatform = SettingsGUI.choicePlatform.getSelectedItem();
	public static Boolean doRebuilds = SettingsGUI.checkboxRebuilds.isSelected();

}
