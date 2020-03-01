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
import javax.swing.JSeparator;
import javax.swing.JEditorPane;
import javax.swing.DropMode;

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
		settingsGUIFrame.setBounds(100, 100, 325, 500);
		settingsGUIFrame.setResizable(false);
		settingsGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsGUIFrame.getContentPane().setLayout(null);
		
		JLabel labelLauncherSettings = new JLabel("Launcher Settings");
		labelLauncherSettings.setFont(Fonts.fontMed);
		labelLauncherSettings.setBounds(10, 16, 271, 14);
		settingsGUIFrame.getContentPane().add(labelLauncherSettings);
		
		JSeparator sepLauncherSettings = new JSeparator();
		sepLauncherSettings.setBounds(10, 36, 272, 2);
		settingsGUIFrame.getContentPane().add(sepLauncherSettings);
		
		JLabel labelChoicePlatform = new JLabel("Platform");
		labelChoicePlatform.setBounds(15, 55, 48, 14);
		labelChoicePlatform.setFont(Fonts.fontReg);
		settingsGUIFrame.getContentPane().add(labelChoicePlatform);
		
		choicePlatform = new Choice();
		choicePlatform.setBounds(80, 51, 83, 20);
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
		checkboxRebuilds.setBounds(11, 85, 97, 23);
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
		checkboxKeepOpen.setBounds(11, 107, 139, 23);
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
		forceRebuildButton.setBounds(183, 129, 103, 23);
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
		checkboxShortcut.setBounds(11, 129, 123, 23);
		checkboxShortcut.setFont(Fonts.fontReg);
		checkboxShortcut.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxShortcut);
		checkboxShortcut.setSelected(Settings.createShortcut);
		checkboxShortcut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.createShortcutChangeEvent(_action);
			}
		});
		
		JSeparator sepExtraTxt = new JSeparator();
		sepExtraTxt.setBounds(10, 190, 272, 2);
		settingsGUIFrame.getContentPane().add(sepExtraTxt);
		
		JLabel labelExtraTxt = new JLabel("Spiral Knights Settings (extra.txt)");
		labelExtraTxt.setFont(Fonts.fontMed);
		labelExtraTxt.setBounds(10, 170, 271, 14);
		settingsGUIFrame.getContentPane().add(labelExtraTxt);
		
		JLabel labelMemory = new JLabel("Allocated Memory");
		labelMemory.setFont(Fonts.fontReg);
		labelMemory.setBounds(15, 209, 93, 14);
		settingsGUIFrame.getContentPane().add(labelMemory);
		
		Choice choiceMemory = new Choice();
		choiceMemory.setFont(Fonts.fontReg);
		choiceMemory.setFocusable(false);
		choiceMemory.setBounds(125, 205, 115, 20);
		settingsGUIFrame.getContentPane().add(choiceMemory);
		choiceMemory.add("Default (512 MB)");
		choiceMemory.add("Low (1 GB)");
		choiceMemory.add("Medium (2 GB)");
		choiceMemory.add("High (4 GB)");
		choiceMemory.add("Flex (8 GB)");
		
		JCheckBox checkboxStringDeduplication = new JCheckBox("Use String Deduplication");
		checkboxStringDeduplication.setFont(Fonts.fontReg);
		checkboxStringDeduplication.setBounds(11, 239, 176, 23);
		settingsGUIFrame.getContentPane().add(checkboxStringDeduplication);
		
		JCheckBox checkboxG1GC = new JCheckBox("Use G1GC");
		checkboxG1GC.setFont(Fonts.fontReg);
		checkboxG1GC.setBounds(11, 261, 97, 23);
		settingsGUIFrame.getContentPane().add(checkboxG1GC);
		
		JCheckBox checkboxExplicitGC = new JCheckBox("Disable Explicit GC");
		checkboxExplicitGC.setFont(Fonts.fontReg);
		checkboxExplicitGC.setBounds(11, 283, 139, 23);
		settingsGUIFrame.getContentPane().add(checkboxExplicitGC);
		
		JCheckBox checkboxUndecorated = new JCheckBox("Undecorated Window");
		checkboxUndecorated.setFont(Fonts.fontReg);
		checkboxUndecorated.setBounds(11, 305, 139, 23);
		settingsGUIFrame.getContentPane().add(checkboxUndecorated);
		
		JEditorPane argumentsPane = new JEditorPane();
		argumentsPane.setFont(Fonts.fontReg);
		argumentsPane.setBounds(11, 365, 272, 85);
		settingsGUIFrame.getContentPane().add(argumentsPane);
		
		JLabel labelArgumentsPane = new JLabel("Additional arguments (Advanced)");
		labelArgumentsPane.setFont(Fonts.fontMed);
		labelArgumentsPane.setBounds(13, 344, 271, 14);
		settingsGUIFrame.getContentPane().add(labelArgumentsPane);
		
		settingsGUIFrame.setLocationRelativeTo(null);
		
		settingsGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        LauncherGUI.settingsButton.setEnabled(true);
		    }
		});
		
	}
}
