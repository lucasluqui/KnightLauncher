package com.lucasallegri.launcher;

import java.awt.EventQueue;

import com.lucasallegri.launcher.mods.ModListGUI;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.launcher.settings.SettingsGUI;
import com.lucasallegri.logging.Logging;
import com.lucasallegri.util.SystemUtil;

public class LauncherApp {
	
	protected static LauncherGUI lgui;
	protected static SettingsGUI sgui;
	protected static ModListGUI mgui;
	protected static JVMPatcher jvmPatcher;

	public static void main(String[] args) {
		
		BootManager.onBootStart();
		LauncherApp app = new LauncherApp();
		
		if(SystemUtil.is64Bit() && SystemUtil.isWindows() && !Settings.jvmPatched) {
			app.composeJVMPatcher(app);
		} else {
			app.composeLauncherGUI(app);
			app.composeSettingsGUI(app);
			app.composeModListGUI(app);
		}
		
		BootManager.onBootEnd();

	}
	
	private LauncherGUI composeLauncherGUI(LauncherApp app) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					lgui = new LauncherGUI(app);
					lgui.switchVisibility();
				} catch (Exception e) {
					Logging.logException(e);
				}
			}
		});
		return lgui;
	}
	
	private SettingsGUI composeSettingsGUI(LauncherApp app) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					sgui = new SettingsGUI(app);
				} catch (Exception e) {
					Logging.logException(e);
				}
			}
		});
		return sgui;
	}
	
	private ModListGUI composeModListGUI(LauncherApp app) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mgui = new ModListGUI(app);
				} catch (Exception e) {
					Logging.logException(e);
				}
			}
		});
		return mgui;
	}
	
	private JVMPatcher composeJVMPatcher(LauncherApp app) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					jvmPatcher = new JVMPatcher(app);
				} catch (Exception e) {
					Logging.logException(e);
				}
			}
		});
		return jvmPatcher;
	}

}
