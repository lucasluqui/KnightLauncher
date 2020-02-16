package xyz.lucasallegri.dialog;

import javax.swing.JOptionPane;

import xyz.lucasallegri.launcher.LauncherGUI;

public class DialogError {
	
	public static void push(String msg) {
		JOptionPane.showMessageDialog(LauncherGUI.launcherGUIFrame,
			    msg,
			    "KnightLauncher Error",
			    JOptionPane.ERROR_MESSAGE);
	}
	
}
