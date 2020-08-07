package xyz.lucasallegri.launcher.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import xyz.lucasallegri.launcher.LauncherConstants;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;

public class SettingsProperties {

	private static final String PROP_VER = "11";
	
	private static Properties prop = new Properties();
	private static String propPath = LauncherConstants.USER_DIR + File.separator + "KnightLauncher.properties";
	
	public static void setup() {
		try {
			if(!FileUtil.fileExists(propPath)) {
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
		Settings.gameDisableExplicitGC = Boolean.parseBoolean(getValue("game.disableExplicitGC"));
		Settings.gameUndecoratedWindow = Boolean.parseBoolean(getValue("game.undecoratedWindow"));
		Settings.gameUseCustomGC = Boolean.parseBoolean(getValue("game.useCustomGC"));
		Settings.gameGarbageCollector = getValue("game.garbageCollector");
		Settings.gameAdditionalArgs = getValue("game.additionalArgs");
		Settings.gameMemory = Integer.parseInt(getValue("game.memory"));
		Settings.jvmPatched = Boolean.parseBoolean(getValue("launcher.jvm_patched"));
		Settings.ingameRPCSetup = Boolean.parseBoolean(getValue("launcher.ingameRPCSetup"));
		Settings.useIngameRPC = Boolean.parseBoolean(getValue("launcher.useIngameRPC"));
		KnightLog.log.info("Successfully loaded all settings from prop file.");
	}
	
}
