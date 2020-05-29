package xyz.lucasallegri.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.logging.KnightLog;

public class Language {
	
	private static Properties prop = new Properties();
	private static InputStream propStream = null;
	
	public static String[] AVAILABLE_LANGUAGES = {
			"English",
			"Español",
			"Deutsch",
			"Português (Brasil)",
			"Français",
			"Japanese",
			"Estonian",
			"Russian"
	};
	
	public static void setup() {
		propStream = Language.class.getResourceAsStream("/lang/lang_" + Settings.lang + ".properties");
	}
	
	public static String getValue(String key) {
		String value = null;
		try {
			prop.load(propStream);
			value = prop.getProperty(key);
		} catch (IOException e) {
			KnightLog.logException(e);
		}
		if(value != null) return value.substring(1, value.length() - 1);
		return key;
	}
	
	public static String getValue(String key, String arg) {
		String value = null;
		try {
			prop.load(propStream);
			value = prop.getProperty(key);
			if(value != null) value = MessageFormat.format(prop.getProperty(key), arg);
		} catch (IOException e) {
			KnightLog.logException(e);
		}
		if(value != null) return value.substring(1, value.length() - 1);
		return key;
	}
	
	public static String getValue(String key, String[] args) {
		String value = null;
		try {
			prop.load(propStream);
			value = prop.getProperty(key);
			if(value != null) value = MessageFormat.format(prop.getProperty(key), (Object[])args);
		} catch (IOException e) {
			KnightLog.logException(e);
		}
		if(value != null) return value.substring(1, value.length() - 1);
		return key;
	}
	
	public static String getLangName(String code) {
		switch(code) {
		case "en": return "English";
		case "es": return "Español";
		case "de": return "Deutsch";
		case "pt-br": return "Português (Brasil)";
		case "fr": return "Français";
		case "jp": return "Japanese";
		case "et-ee": return "Estonian";
		case "ru": return "Russian";
		}
		return null;
	}
	
	public static String getLangCode(String detailed) {
		switch(detailed) {
		case "English": return "en";
		case "Español": return "es";
		case "Deutsch": return "de";
		case "Português (Brasil)": return "pt-br";
		case "Français": return "fr";
		case "Japanese": return "jp";
		case "Estonian": return "et-ee";
		case "Russian": return "ru";
		}
		return null;
	}

}
