package com.lucasallegri.launcher;

import java.io.File;
import java.io.IOException;

import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.logging.KnightLog;
import com.lucasallegri.util.Compressor;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.SystemUtil;

public class Modules {
	
	public static void setupIngameRPC() {
		if(SystemUtil.isWindows() && SystemUtil.is64Bit()) {
			try {
				FileUtil.extractFileWithinJar("/modules/skdiscordrpc/bundle.zip", "KnightLauncher/modules/skdiscordrpc/bundle.zip");
				Compressor.unzip("KnightLauncher/modules/skdiscordrpc/bundle.zip", "KnightLauncher/modules/skdiscordrpc/", false);
				new File(LauncherConstants.USER_DIR + "KnightLauncher/modules/skdiscordrpc/bundle.zip").delete();
				SettingsProperties.setValue("launcher.ingameRPCSetup", "true");
			} catch (IOException e) {
				KnightLog.logException(e);
			}
		} else {
			SettingsProperties.setValue("launcher.ingameRPCSetup", "true");
			SettingsProperties.setValue("launcher.useIngameRPC", "false");
		}
	}
	
	public static void setupSafeguard() {
		try {
			FileUtil.extractFileWithinJar("/modules/safeguard/bundle.zip", "KnightLauncher/modules/safeguard/bundle.zip");
			Compressor.unzip("KnightLauncher/modules/safeguard/bundle.zip", "rsrc/", false);
		} catch (IOException e) {
			KnightLog.logException(e);
		}
		
	}

}
