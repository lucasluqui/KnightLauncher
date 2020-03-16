package xyz.lucasallegri.launcher.settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;

public class SettingsProperties {

	private static final String PROP_VER = "6";
	private static Properties prop = new Properties();
	private static String propPath = System.getProperty("user.dir") + File.separator + "KnightLauncher.properties";
	
	public static void setup() {
		try {
			if(!FileUtil.fileExists(propPath)) {
				FileUtil.createFile(propPath);
				fillWithBaseProp();
			} else if(FileUtil.fileExists(propPath) && getValue("PROP_VER") != null
					&& !getValue("PROP_VER").startsWith(PROP_VER)) {
				KnightLog.log.info("Old PROP_VER detected, resetting properties file.");
				FileUtil.recreateFile(propPath);
				fillWithBaseProp();
			}
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}
	
	private static void fillWithBaseProp() throws IOException {
		String baseProp = 	"PROP_VER=" + PROP_VER + System.lineSeparator() +
							"lastModCount=0" + System.lineSeparator() +
							"game.platform=Steam" + System.lineSeparator() +
							"rebuilds=true"  + System.lineSeparator() +
							"keepOpen=false" + System.lineSeparator() +
							"createShortcut=true" + System.lineSeparator() +
							"jvmPatched=false" + System.lineSeparator() +
							"lang=en" + System.lineSeparator() +
							"game.useStringDeduplication=false" + System.lineSeparator() +
							"game.useG1GC=false" + System.lineSeparator() +
							"game.disableExplicitGC=false" + System.lineSeparator() +
							"game.undecoratedWindow=false" + System.lineSeparator() +
							"game.additionalArgs=" + System.lineSeparator() +
							"game.memory=512";
		BufferedWriter writer = new BufferedWriter(new FileWriter(propPath, true));
		writer.append(baseProp);
		writer.close();
	}
	
	public static String getValue(String key) {
        try (InputStream is = new FileInputStream(propPath)) {
        	prop.load(is);
        	KnightLog.log.info("Request for prop key: " + key);
            return prop.getProperty(key);
        } catch (IOException e) {
        	KnightLog.logException(e);
        }
		return null;
	}
	
	public static void setValue(String key, String value) {
		try (OutputStream os = new FileOutputStream(propPath)) {
			prop.setProperty(key, value);
			prop.store(new FileOutputStream(propPath), null);
			KnightLog.log.info("Setting new key value: key=" + key + ",value=" + value);
		} catch(IOException e) {
			KnightLog.logException(e);
		}
	}
	
	public static void loadFromProp() {
		Settings.gamePlatform = getValue("game.platform");
		Settings.doRebuilds = Boolean.parseBoolean(getValue("rebuilds"));
		Settings.keepOpen = Boolean.parseBoolean(getValue("keepOpen"));
		Settings.createShortcut = Boolean.parseBoolean(getValue("createShortcut"));
		Settings.jvmPatched = Boolean.parseBoolean(getValue("jvmPatched"));
		Settings.lang = getValue("lang");
		Settings.gameUseStringDeduplication = Boolean.parseBoolean(getValue("game.useStringDeduplication"));
		Settings.gameUseG1GC = Boolean.parseBoolean(getValue("game.useG1GC"));
		Settings.gameDisableExplicitGC = Boolean.parseBoolean(getValue("game.disableExplicitGC"));
		Settings.gameUndecoratedWindow = Boolean.parseBoolean(getValue("game.undecoratedWindow"));
		Settings.gameAdditionalArgs = getValue("game.additionalArgs");
		Settings.gameMemory = Integer.parseInt(getValue("game.memory"));
		KnightLog.log.info("Successfully loaded all settings from prop file.");
	}
	
}
