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
	
	public static void showState(boolean show) {
		LauncherGUI.launchState.setVisible(show);
	}
	
	public static void showBar(boolean show) {
		LauncherGUI.launchProgressBar.setVisible(show);
	}

}
