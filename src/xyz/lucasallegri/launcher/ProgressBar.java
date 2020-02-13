package xyz.lucasallegri.launcher;

public class ProgressBar {
	
	public static void setState(String newState) {
		LauncherGUI.launchState.setText(newState);
	}
	
	public static void setBarValue(int n) {
		LauncherGUI.launchProgressBar.setValue(n);
	}
	
	public static void setBarMax(int n) {
		LauncherGUI.launchProgressBar.setMaximum(n);
	}
	
	public static void showState() {
		LauncherGUI.launchState.setVisible(LauncherGUI.launchState.isVisible() ? false : true);
	}
	
	public static void showBar() {
		LauncherGUI.launchProgressBar.setVisible(LauncherGUI.launchProgressBar.isVisible() ? false : true);
	}

}
