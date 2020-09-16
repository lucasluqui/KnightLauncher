package com.lucasallegri.dialog;

import javax.swing.JOptionPane;

import com.lucasallegri.launcher.LanguageManager;

public class DialogWarning {
	
	public static void push(String msg) {
		JOptionPane.showMessageDialog(null,
			    msg,
			    "Knight Launcher Warning",
			    JOptionPane.WARNING_MESSAGE
			    );
	}
	
	public static void pushTranslated(String msg) {
		JOptionPane.showMessageDialog(null,
			    msg,
			    LanguageManager.getValue("t.warning"),
			    JOptionPane.WARNING_MESSAGE
			    );
	}

}
