package xyz.lucasallegri.launcher.settings;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

import xyz.lucasallegri.launcher.Fonts;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.logging.KnightLog;

import java.awt.Choice;
import javax.swing.JCheckBox;
import javax.swing.JButton;

public class SettingsGUI {

	public static JFrame settingsGUIFrame;
	public static Choice choicePlatform;
	public static JCheckBox checkboxRebuilds;
	public static JCheckBox checkboxKeepOpen;
	public static JButton forceRebuildButton;
	public static JCheckBox checkboxShortcut;

	public static void compose() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SettingsGUI window = new SettingsGUI();
					window.settingsGUIFrame.setVisible(true);
				} catch (Exception e) {
					KnightLog.logException(e);
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
		settingsGUIFrame.setBounds(100, 100, 200, 190);
		settingsGUIFrame.setResizable(false);
		settingsGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsGUIFrame.getContentPane().setLayout(null);
		
		JLabel labelChoicePlatform = new JLabel("Platform");
		labelChoicePlatform.setBounds(10, 15, 48, 14);
		labelChoicePlatform.setFont(Fonts.fontReg);
		settingsGUIFrame.getContentPane().add(labelChoicePlatform);
		
		choicePlatform = new Choice();
		choicePlatform.setBounds(62, 11, 83, 20);
		choicePlatform.setFont(Fonts.fontReg);
		choicePlatform.setFocusable(false);
		settingsGUIFrame.getContentPane().add(choicePlatform);
		choicePlatform.add("Steam");
		choicePlatform.add("Standalone");
		choicePlatform.select(Settings.gamePlatform);
		choicePlatform.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.platformChangeEvent(event);
			}
		});
		
		checkboxRebuilds = new JCheckBox("Rebuilds");
		checkboxRebuilds.setBounds(6, 40, 97, 23);
		checkboxRebuilds.setFont(Fonts.fontReg);
		checkboxRebuilds.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxRebuilds);
		checkboxRebuilds.setSelected(Settings.doRebuilds);
		checkboxRebuilds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.rebuildsChangeEvent(_action);
			}
		});
		
		checkboxKeepOpen = new JCheckBox("Keep open on launch");
		checkboxKeepOpen.setBounds(6, 62, 139, 23);
		checkboxKeepOpen.setFont(Fonts.fontReg);
		checkboxKeepOpen.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxKeepOpen);
		checkboxKeepOpen.setSelected(Settings.keepOpen);
		checkboxKeepOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.keepOpenChangeEvent(_action);
			}
		});
		
		forceRebuildButton = new JButton("Force Rebuild");
		forceRebuildButton.setBounds(10, 114, 103, 23);
		forceRebuildButton.setFont(Fonts.fontMed);
		forceRebuildButton.setFocusPainted(false);
		forceRebuildButton.setFocusable(false);
		settingsGUIFrame.getContentPane().add(forceRebuildButton);
		forceRebuildButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.forceRebuildEvent();
			}
		});
		
		checkboxShortcut = new JCheckBox("Create shortcut");
		checkboxShortcut.setBounds(6, 84, 123, 23);
		checkboxShortcut.setFont(Fonts.fontReg);
		checkboxShortcut.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxShortcut);
		checkboxShortcut.setSelected(Settings.createShortcut);
		checkboxShortcut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.createShortcutChangeEvent(_action);
			}
		});
		
		settingsGUIFrame.setLocationRelativeTo(null);
		
		settingsGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        LauncherGUI.settingsButton.setEnabled(true);
		    }
		});
		
	}
}
