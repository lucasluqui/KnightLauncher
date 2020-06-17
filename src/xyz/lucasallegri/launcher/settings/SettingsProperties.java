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
import xyz.lucasallegri.launcher.LauncherConstants;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;

public class SettingsProperties {

	private static final String PROP_VER = "9";
	
	private static Properties prop = new Properties();
	private static String propPath = LauncherConstants.USER_DIR + File.separator + "KnightLauncher.properties";
	
	public static void setup() {
		try {
			if(!FileUtil.fileExists(propPath)) {
				FileUtil.createFile(propPath);
				FileUtil.extractFileWithinJar("/config/base.properties", propPath);
			} else if(FileUtil.fileExists(propPath) && getValue("PROP_VER") != null
					&& !getValue("PROP_VER").equals(PROP_VER)) {
				KnightLog.log.info("Old PROP_VER detected, resetting properties file.");
				FileUtil.extractFileWithinJar("/config/base.properties", propPath);
			}
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}
	
	@Deprecated
	private static void fillWithBaseProp(Boolean alreadyExists) throws IOException {
		String _alreadyExists = String.valueOf(alreadyExists);
		String baseProp = 	"PROP_VER=" + PROP_VER + System.lineSeparator() +
							"launcher.rebuilds=true"  + System.lineSeparator() +
							"launcher.keepOpen=false" + System.lineSeparator() +
							"launcher.createShortcut=true" + System.lineSeparator() +
							"launcher.lang=en" + System.lineSeparator() +
							"launcher.style=dark" + System.lineSeparator() +
							"launcher.jvm_patched=false" + System.lineSeparator() +
							"modloader.lastModCount=0" + System.lineSeparator() +
							"compressor.unzipMethod=safe" + System.lineSeparator() +
							"compressor.extractBuffer=8196" + System.lineSeparator() +
							"game.platform=Steam" + System.lineSeparator() +
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
		String value;
        try (InputStream is = new FileInputStream(propPath)) {
        	prop.load(is);
        	value = prop.getProperty(key);
        	KnightLog.log.info("Request for prop key: " + key + ", reply value: " + value);
            return value;
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
		Settings.doRebuilds = Boolean.parseBoolean(getValue("launcher.rebuilds"));
		Settings.keepOpen = Boolean.parseBoolean(getValue("launcher.keepOpen"));
		Settings.createShortcut = Boolean.parseBoolean(getValue("launcher.createShortcut"));
		Settings.lang = getValue("launcher.lang");
		Settings.launcherStyle = getValue("launcher.style");
		Settings.compressorUnzipMethod = getValue("compressor.unzipMethod");
		Settings.compressorExtractBuffer = Integer.parseInt(getValue("compressor.extractBuffer"));
		Settings.gamePlatform = getValue("game.platform");
		Settings.gameUseStringDeduplication = Boolean.parseBoolean(getValue("game.useStringDeduplication"));
		Settings.gameUseG1GC = Boolean.parseBoolean(getValue("game.useG1GC"));
		Settings.gameDisableExplicitGC = Boolean.parseBoolean(getValue("game.disableExplicitGC"));
		Settings.gameUndecoratedWindow = Boolean.parseBoolean(getValue("game.undecoratedWindow"));
		Settings.gameAdditionalArgs = getValue("game.additionalArgs");
		Settings.gameMemory = Integer.parseInt(getValue("game.memory"));
		Settings.jvmPatched = Boolean.parseBoolean(getValue("launcher.jvm_patched"));
		KnightLog.log.info("Successfully loaded all settings from prop file.");
	}
	
}
