package xyz.lucasallegri.dialog;

import javax.swing.JOptionPane;

public class DialogError {
	
	public static void push(String msg) {
		JOptionPane.showMessageDialog(null,
			    msg,
			    "KnightLauncher Error",
			    JOptionPane.ERROR_MESSAGE
			    );
	}
	
}
