package xyz.lucasallegri.dialog;

import javax.swing.JOptionPane;

import xyz.lucasallegri.launcher.Language;

public class DialogError {
	
	public static void push(String msg) {
		JOptionPane.showMessageDialog(null,
			    msg,
			    "KnightLauncher Error",
			    JOptionPane.ERROR_MESSAGE
			    );
	}
	
	public static void pushTranslated(String msg) {
		JOptionPane.showMessageDialog(null,
			    msg,
			    Language.getValue("t.error"),
			    JOptionPane.ERROR_MESSAGE
			    );
	}
	
}
