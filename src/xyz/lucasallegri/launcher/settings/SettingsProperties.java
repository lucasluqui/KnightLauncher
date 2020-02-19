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

	public static final String PROP_VER = "4";
	public static Properties prop = new Properties();
	private static String propPath = System.getProperty("user.dir") + File.separator + "KnightLauncher.properties";
	
	public static void setup() {
		try {
			if(!FileUtil.fileExists(propPath)) {
				File file = new File(propPath);
				file.createNewFile();
				fillWithBaseProp();
			} else if(FileUtil.fileExists(propPath) && !getValue("propver").startsWith(PROP_VER)) {
				KnightLog.log.info("Old prop version detected, resetting properties file.");
				File file = new File(propPath);
				file.delete();
				file.createNewFile();
				fillWithBaseProp();
			}
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}
	
	private static void fillWithBaseProp() throws IOException {
		String baseProp = 	"propver=" + PROP_VER + System.lineSeparator() +
							"lastModCount=0" + System.lineSeparator() +
							"platform=Steam" + System.lineSeparator() +
							"rebuilds=true"  + System.lineSeparator() +
							"keepOpen=false" + System.lineSeparator() +
							"createShortcut=true";
		BufferedWriter writer = new BufferedWriter(new FileWriter(propPath, true));
		writer.append(baseProp);
		writer.close();
	}
	
	public static String getValue(String key) {
        try (InputStream is = new FileInputStream(propPath)) {
        	prop.load(is);
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
		} catch(IOException e) {
			KnightLog.logException(e);
		}
	}
	
	public static void loadFromProp() {
		Settings.gamePlatform = getValue("platform");
		Settings.doRebuilds = Boolean.parseBoolean(getValue("rebuilds"));
		Settings.keepOpen = Boolean.parseBoolean(getValue("keepOpen"));
		Settings.createShortcut = Boolean.parseBoolean(getValue("createShortcut"));
	}
	
}
