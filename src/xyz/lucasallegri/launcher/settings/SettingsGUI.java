package xyz.lucasallegri.launcher.settings;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class SettingsGUI {

	public static JFrame settingsGUIFrame;

	public static void compose() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SettingsGUI window = new SettingsGUI();
					window.settingsGUIFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public SettingsGUI() {
		initialize();
	}

	private void initialize() {
		settingsGUIFrame = new JFrame();
		settingsGUIFrame.setBounds(100, 100, 450, 300);
		settingsGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
