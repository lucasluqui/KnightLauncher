package xyz.lucasallegri.launcher.settings;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import xyz.lucasallegri.launcher.DefaultColors;
import xyz.lucasallegri.launcher.Fonts;
import xyz.lucasallegri.launcher.Language;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.logging.KnightLog;
import java.awt.Choice;

public class SettingsGUI {

	public static JFrame settingsGUIFrame;
	public static JComboBox<String> choicePlatform;
	public static JComboBox<String> choiceLanguage;
	public static JComboBox<String> choiceStyle;
	public static JComboBox<String> choiceMemory;
	public static JCheckBox checkboxRebuilds;
	public static JCheckBox checkboxKeepOpen;
	public static JButton forceRebuildButton;
	public static JCheckBox checkboxShortcut;
	public static JCheckBox checkboxStringDeduplication;
	public static JCheckBox checkboxG1GC;
	public static JCheckBox checkboxExplicitGC;
	public static JCheckBox checkboxUndecorated;
	public static JEditorPane argumentsPane;
	
	int pY, pX;

	public static void compose() {
		EventQueue.invokeLater(new Runnable() {
			@SuppressWarnings("static-access")
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
		LauncherGUI.settingsButton.setEnabled(false);
		initialize();
	}

	private void initialize() {
		settingsGUIFrame = new JFrame();
		settingsGUIFrame.setTitle(Language.getValue("t.settings"));
		settingsGUIFrame.setBounds(100, 100, 300, 590);
		settingsGUIFrame.setResizable(false);
		settingsGUIFrame.setUndecorated(true);
		settingsGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsGUIFrame.getContentPane().setLayout(null);
		
		JLabel labelLauncherSettings = new JLabel(Language.getValue("m.launcher_settings"));
		labelLauncherSettings.setFont(Fonts.fontMed);
		labelLauncherSettings.setBounds(10, 36, 271, 14);
		settingsGUIFrame.getContentPane().add(labelLauncherSettings);
		
		JSeparator sepLauncherSettings = new JSeparator();
		sepLauncherSettings.setBounds(10, 56, 272, 2);
		settingsGUIFrame.getContentPane().add(sepLauncherSettings);
		
		JLabel labelChoicePlatform = new JLabel(Language.getValue("m.platform"));
		labelChoicePlatform.setBounds(15, 79, 95, 14);
		labelChoicePlatform.setFont(Fonts.fontReg);
		settingsGUIFrame.getContentPane().add(labelChoicePlatform);
		
		choicePlatform = new JComboBox<String>();
		choicePlatform.setBounds(125, 75, 120, 20);
		choicePlatform.setFont(Fonts.fontReg);
		choicePlatform.setFocusable(false);
		settingsGUIFrame.getContentPane().add(choicePlatform);
		choicePlatform.addItem(Language.getValue("o.steam"));
		choicePlatform.addItem(Language.getValue("o.standalone"));
		choicePlatform.setSelectedItem(Settings.gamePlatform);
		choicePlatform.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.platformChangeEvent(event);
			}
		});
		
		JLabel labelLanguage = new JLabel(Language.getValue("m.language"));
		labelLanguage.setBounds(15, 124, 95, 14);
		labelLanguage.setFont(Fonts.fontReg);
		settingsGUIFrame.getContentPane().add(labelLanguage);
		
		choiceLanguage = new JComboBox<String>();
		choiceLanguage.setBounds(125, 120, 120, 20);
		choiceLanguage.setFont(Fonts.fontReg);
		choiceLanguage.setFocusable(false);
		settingsGUIFrame.getContentPane().add(choiceLanguage);
		for(String lang : Language.AVAILABLE_LANGUAGES) {
			choiceLanguage.addItem(lang);
		}
		choiceLanguage.setSelectedItem(Language.getLangName(Settings.lang));
		choiceLanguage.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.languageChangeEvent(event);
			}
		});
		
		JLabel labelStyle = new JLabel(Language.getValue("m.launcher_style"));
		labelStyle.setFont(Fonts.fontReg);
		labelStyle.setBounds(15, 169, 95, 14);
		settingsGUIFrame.getContentPane().add(labelStyle);
		
		choiceStyle = new JComboBox<String>();
		choiceStyle.setBounds(125, 165, 120, 20);
		choiceStyle.setFont(Fonts.fontReg);
		choiceStyle.setFocusable(false);
		settingsGUIFrame.getContentPane().add(choiceStyle);
		choiceStyle.addItem(Language.getValue("o.dark"));
		choiceStyle.addItem(Language.getValue("o.light"));
		choiceStyle.setSelectedIndex(Settings.launcherStyle.equals("dark") ? 0 : 1);
		choiceStyle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.styleChangeEvent(event);
			}
		});
		
		checkboxRebuilds = new JCheckBox(Language.getValue("m.rebuilds"));
		checkboxRebuilds.setBounds(11, 203, 270, 23);
		checkboxRebuilds.setFont(Fonts.fontReg);
		checkboxRebuilds.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxRebuilds);
		checkboxRebuilds.setSelected(Settings.doRebuilds);
		checkboxRebuilds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.rebuildsChangeEvent(_action);
			}
		});
		
		checkboxKeepOpen = new JCheckBox(Language.getValue("m.keep_open"));
		checkboxKeepOpen.setBounds(11, 225, 270, 21);
		checkboxKeepOpen.setFont(Fonts.fontReg);
		checkboxKeepOpen.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxKeepOpen);
		checkboxKeepOpen.setSelected(Settings.keepOpen);
		checkboxKeepOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.keepOpenChangeEvent(_action);
			}
		});
		
		forceRebuildButton = new JButton(Language.getValue("b.force_rebuild"));
		forceRebuildButton.setBounds(166, 247, 120, 23);
		forceRebuildButton.setFont(Fonts.fontMed);
		forceRebuildButton.setFocusPainted(false);
		forceRebuildButton.setFocusable(false);
		forceRebuildButton.setToolTipText(Language.getValue("b.force_rebuild"));
		settingsGUIFrame.getContentPane().add(forceRebuildButton);
		forceRebuildButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.forceRebuildEvent();
			}
		});
		
		checkboxShortcut = new JCheckBox(Language.getValue("m.create_shortcut"));
		checkboxShortcut.setBounds(11, 247, 139, 23);
		checkboxShortcut.setFont(Fonts.fontReg);
		checkboxShortcut.setFocusPainted(false);
		checkboxShortcut.setToolTipText(Language.getValue("m.create_shortcut"));
		settingsGUIFrame.getContentPane().add(checkboxShortcut);
		checkboxShortcut.setSelected(Settings.createShortcut);
		checkboxShortcut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.createShortcutChangeEvent(_action);
			}
		});
		
		JSeparator sepExtraTxt = new JSeparator();
		sepExtraTxt.setBounds(10, 308, 272, 2);
		settingsGUIFrame.getContentPane().add(sepExtraTxt);
		
		JLabel labelExtraTxt = new JLabel(Language.getValue("m.extratxt_settings"));
		labelExtraTxt.setFont(Fonts.fontMed);
		labelExtraTxt.setBounds(10, 288, 271, 14);
		settingsGUIFrame.getContentPane().add(labelExtraTxt);
		
		JLabel labelMemory = new JLabel(Language.getValue("m.allocated_memory"));
		labelMemory.setFont(Fonts.fontReg);
		labelMemory.setBounds(15, 327, 124, 14);
		settingsGUIFrame.getContentPane().add(labelMemory);
		
		choiceMemory = new JComboBox<String>();
		choiceMemory.setFont(Fonts.fontReg);
		choiceMemory.setFocusable(false);
		choiceMemory.setBounds(145, 323, 135, 20);
		settingsGUIFrame.getContentPane().add(choiceMemory);
		choiceMemory.addItem(Language.getValue("o.memory_default"));
		choiceMemory.addItem(Language.getValue("o.memory_low"));
		choiceMemory.addItem(Language.getValue("o.memory_med"));
		choiceMemory.addItem(Language.getValue("o.memory_high"));
		choiceMemory.addItem(Language.getValue("o.memory_flex"));
		choiceMemory.setSelectedIndex(parseSelectedMemoryAsIndex());
		choiceMemory.setToolTipText((String)choiceMemory.getSelectedItem());
		choiceMemory.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.memoryChangeEvent(event);
			}
		});
		
		checkboxStringDeduplication = new JCheckBox(Language.getValue("m.use_string_deduplication"));
		checkboxStringDeduplication.setFont(Fonts.fontReg);
		checkboxStringDeduplication.setBounds(11, 357, 249, 23);
		checkboxStringDeduplication.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxStringDeduplication);
		checkboxStringDeduplication.setSelected(Settings.gameUseStringDeduplication);
		checkboxStringDeduplication.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.useStringDeduplicationChangeEvent(_action);
			}
		});
		
		checkboxG1GC = new JCheckBox(Language.getValue("m.use_g1gc"));
		checkboxG1GC.setFont(Fonts.fontReg);
		checkboxG1GC.setBounds(11, 379, 249, 23);
		checkboxG1GC.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxG1GC);
		checkboxG1GC.setSelected(Settings.gameUseG1GC);
		checkboxG1GC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.useG1GCChangeEvent(_action);
			}
		});
		
		checkboxExplicitGC = new JCheckBox(Language.getValue("m.disable_explicit_gc"));
		checkboxExplicitGC.setFont(Fonts.fontReg);
		checkboxExplicitGC.setBounds(11, 401, 249, 23);
		checkboxExplicitGC.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxExplicitGC);
		checkboxExplicitGC.setSelected(Settings.gameDisableExplicitGC);
		checkboxExplicitGC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.disableExplicitGCChangeEvent(_action);
			}
		});
		
		checkboxUndecorated = new JCheckBox(Language.getValue("m.undecorated_window"));
		checkboxUndecorated.setFont(Fonts.fontReg);
		checkboxUndecorated.setBounds(11, 423, 249, 23);
		checkboxUndecorated.setFocusPainted(false);
		settingsGUIFrame.getContentPane().add(checkboxUndecorated);
		checkboxUndecorated.setSelected(Settings.gameUndecoratedWindow);
		checkboxUndecorated.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.undecoratedWindowChangeEvent(_action);
			}
		});
		
		JLabel labelArgumentsPane = new JLabel(Language.getValue("m.additional_args"));
		labelArgumentsPane.setFont(Fonts.fontMed);
		labelArgumentsPane.setBounds(13, 462, 271, 14);
		settingsGUIFrame.getContentPane().add(labelArgumentsPane);
		
		argumentsPane = new JEditorPane();
		argumentsPane.setFont(Fonts.fontReg);
		argumentsPane.setBounds(11, 421, 255, 85);
		settingsGUIFrame.getContentPane().add(argumentsPane);
		argumentsPane.setText(Settings.gameAdditionalArgs);
		
		JScrollPane scrollBar = new JScrollPane(argumentsPane);
		scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollBar.setBounds(11, 483, 272, 85);
		settingsGUIFrame.getContentPane().add(scrollBar);
		
		JPanel titleBar = new JPanel();
		titleBar.setBounds(0, 0, settingsGUIFrame.getWidth(), 20);
		titleBar.setBackground(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT);
		settingsGUIFrame.getContentPane().add(titleBar);
		
		
		/*
		 * Based on Paul Samsotha's reply @ StackOverflow
		 * link: https://stackoverflow.com/questions/24476496/drag-and-resize-undecorated-jframe
		 */
		titleBar.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		    	
		        pX = me.getX();
		        pY = me.getY();
		    }
		});
		titleBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				
		        pX = me.getX();
		        pY = me.getY();
		    }
		
		    @Override 
		    public void mouseDragged(MouseEvent me) {
		
		    	settingsGUIFrame.setLocation(settingsGUIFrame.getLocation().x + me.getX() - pX,
		    	settingsGUIFrame.getLocation().y + me.getY() - pY);
		    }
		});
		titleBar.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent me) {
		
				settingsGUIFrame.setLocation(settingsGUIFrame.getLocation().x + me.getX() - pX,
				settingsGUIFrame.getLocation().y + me.getY() - pY);
		    }
		
			@Override
			public void mouseMoved(MouseEvent arg0) {
				// Auto-generated method stub
			}
		});
		titleBar.setLayout(null);
		
		JLabel windowTitle = new JLabel(Language.getValue("t.settings"));
		windowTitle.setFont(Fonts.fontMed);
		windowTitle.setBounds(10, 0, settingsGUIFrame.getWidth() - 100, 20);
		titleBar.add(windowTitle);
		
		JButton closeButton = new JButton("x");
		closeButton.setBounds(settingsGUIFrame.getWidth() - 22, 0, 20, 20);
		closeButton.setFocusPainted(false);
		closeButton.setFocusable(false);
		closeButton.setBorder(BorderFactory.createLineBorder(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT));
		closeButton.setFont(Fonts.fontMed);
		titleBar.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       settingsGUIFrame.dispose();
		    }
		});
		
		JButton minimizeButton = new JButton("_");
		minimizeButton.setBounds(settingsGUIFrame.getWidth() - 42, 0, 20, 20);
		minimizeButton.setFocusPainted(false);
		minimizeButton.setFocusable(false);
		minimizeButton.setBorder(BorderFactory.createLineBorder(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT));
		minimizeButton.setFont(Fonts.fontMed);
		titleBar.add(minimizeButton);
		minimizeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		    	settingsGUIFrame.setState(Frame.ICONIFIED);
		    }
		});
		
		settingsGUIFrame.setLocationRelativeTo(null);
		
		settingsGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        LauncherGUI.settingsButton.setEnabled(true);
		        SettingsEventHandler.saveAdditionalArgs();
		    }
		});
		
	}
	
	public static int parseSelectedMemoryAsInt() {
		switch(choiceMemory.getSelectedIndex()) {
		case 0: return 512;
		case 1: return 1024;
		case 2: return 2048;
		case 3: return 4096;
		case 4: return 8192;
		}
		return 512;
	}
	
	public static int parseSelectedMemoryAsIndex() {
		switch(Settings.gameMemory) {
		case 512: return 0;
		case 1024: return 1;
		case 2048: return 2;
		case 4096: return 3;
		case 8192: return 4;
		}
		return 0;
	}
}
