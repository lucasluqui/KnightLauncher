package com.lucasallegri.launcher;

import static com.lucasallegri.launcher.Log.log;

public class ProgressBar {
	
	public static void setState(String newState) {
		LauncherGUI.launchState.setText(newState);
		log.info(newState);
	}
	
	public static void setBarValue(int n) {
		LauncherGUI.launchProgressBar.setValue(n);
	}
	
	public static void setBarMax(int n) {
		LauncherGUI.launchProgressBar.setMaximum(n);
	}
	
	public static void showState(boolean show) {
		LauncherGUI.launchState.setVisible(show);
	}
	
	public static void showBar(boolean show) {
		LauncherGUI.launchProgressBar.setVisible(show);
	}

}
