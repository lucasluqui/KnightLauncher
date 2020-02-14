package xyz.lucasallegri.launcher.settings;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

import xyz.lucasallegri.launcher.LauncherGUI;

import java.awt.Choice;
import javax.swing.JCheckBox;

public class SettingsGUI {

	public static JFrame settingsGUIFrame;
	public static Choice choicePlatform;
	public static JCheckBox checkboxRebuilds;

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
		settingsGUIFrame.setTitle("KnightLauncher Settings");
		settingsGUIFrame.setBounds(100, 100, 200, 135);
		settingsGUIFrame.setResizable(false);
		settingsGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsGUIFrame.getContentPane().setLayout(null);
		
		JLabel labelChoicePlatform = new JLabel("Platform");
		labelChoicePlatform.setBounds(10, 15, 48, 14);
		labelChoicePlatform.setFont(LauncherGUI.fontReg);
		settingsGUIFrame.getContentPane().add(labelChoicePlatform);
		
		choicePlatform = new Choice();
		choicePlatform.setBounds(62, 11, 83, 20);
		choicePlatform.setFont(LauncherGUI.fontReg);
		settingsGUIFrame.getContentPane().add(choicePlatform);
		choicePlatform.add("Steam");
		choicePlatform.add("Standalone");
		choicePlatform.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.platformChangeEvent(event);
			}
		});
		
		checkboxRebuilds = new JCheckBox("Rebuilds");
		checkboxRebuilds.setSelected(true);
		checkboxRebuilds.setBounds(6, 40, 97, 23);
		checkboxRebuilds.setFont(LauncherGUI.fontReg);
		settingsGUIFrame.getContentPane().add(checkboxRebuilds);
		
		settingsGUIFrame.setLocationRelativeTo(null);
		
	}
}