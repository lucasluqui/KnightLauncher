package xyz.lucasallegri.dialog;

import javax.swing.JOptionPane;

import xyz.lucasallegri.launcher.Language;

public class DialogWarning {
	
	public static void push(String msg) {
		JOptionPane.showMessageDialog(null,
			    msg,
			    "KnightLauncher Warning",
			    JOptionPane.WARNING_MESSAGE
			    );
	}
	
	public static void pushTranslated(String msg) {
		JOptionPane.showMessageDialog(null,
			    msg,
			    Language.getValue("t.warning"),
			    JOptionPane.WARNING_MESSAGE
			    );
	}

}
