package xyz.lucasallegri.launcher;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
		ProgressBar.showBar(true);
		ProgressBar.showState(true);
		
		ProgressBar.setState("Renaming current java_vm to old_java_vm...");
		FileUtil.rename(new File("java_vm"), new File("old_java_vm"));
		ProgressBar.setState("Creating new java_vm...");
		FileUtil.createFolder("java_vm");
		try {
			ProgressBar.setState("Moving installed JRE to java_vm...");
			KnightLog.log.info("java.home = " + System.getProperty("java.home"));
			FileUtils.copyDirectory(new File(System.getProperty("java.home")), new File("java_vm"));
			ProgressBar.setState("Successfully patched java_vm");
			ProgressBar.setBarValue(1);
			SettingsProperties.setValue("jvmPatched", "true");
			Settings.jvmPatched = true;
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}

}
