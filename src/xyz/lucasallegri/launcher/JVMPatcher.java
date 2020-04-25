package xyz.lucasallegri.launcher;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;

public class JVMPatcher {
	
	public static void start() {
		ProgressBar.showBar(true);
		ProgressBar.showState(true);
		
		Thread patchThread = new Thread(new Runnable() {
			public void run() {
				patch();
			}
		});
		patchThread.start();
	}
	
	public static void patch() {
		ProgressBar.setBarMax(1);
		ProgressBar.setState(Language.getValue("m.jvm_rename"));
		FileUtil.rename(new File("java_vm"), new File("old_java_vm"));
		ProgressBar.setState(Language.getValue("m.jvm_create"));
		FileUtil.createDir("java_vm");
		try {
			ProgressBar.setState(Language.getValue("m.jvm_movingjre"));
			KnightLog.log.info("java.home = " + System.getProperty("java.home"));
			FileUtils.copyDirectory(new File(System.getProperty("java.home")), new File("java_vm"));
			ProgressBar.setState(Language.getValue("m.jvm_success"));
			ProgressBar.setBarValue(1);
			SettingsProperties.setValue("jvmPatched", "true");
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}
	
//	public static Boolean isPatched() {
//		if(!Settings.jvmPatched && FileUtil.fileExists("old_java_vm")) SettingsProperties.setValue("jvmPatched", "true");
//		return Settings.jvmPatched || FileUtil.fileExists("old_java_vm");
//	}

}
