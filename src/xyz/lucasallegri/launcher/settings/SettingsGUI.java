package xyz.lucasallegri.launcher.settings;

import java.awt.EventQueue;
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
import javax.swing.JComboBox;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import mdlaf.utils.MaterialBorders;
import xyz.lucasallegri.launcher.Fonts;
import xyz.lucasallegri.launcher.Language;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.ColorUtil;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;

public class SettingsGUI {

	public static JFrame settingsGUIFrame;
	public static JComboBox<String> choicePlatform;
	public static JComboBox<String> choiceLanguage;
	public static JComboBox<String> choiceStyle;
	public static JComboBox<String> choiceMemory;
	public static JToggleButton switchCleaning;
	public static JToggleButton switchKeepOpen;
	public static JToggleButton switchShortcut;
	public static JButton forceRebuildButton;
	public static JToggleButton switchStringDedup;
	public static JToggleButton switchUseG1GC;
	public static JToggleButton switchExplicitGC;
	public static JToggleButton switchUndecoratedWindow;
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
		LauncherGUI.launcherGUIFrame.setVisible(false);
		initialize();
	}

	private void initialize() {
		settingsGUIFrame = new JFrame();
		settingsGUIFrame.setTitle(Language.getValue("t.settings"));
		settingsGUIFrame.setBounds(100, 100, 850, 475);
		settingsGUIFrame.setResizable(false);
		settingsGUIFrame.setUndecorated(true);
		settingsGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsGUIFrame.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		tabbedPane.setBounds(-2, 20, 852, 455);
		tabbedPane.setFont(Fonts.fontMedBig);
		tabbedPane.addTab(Language.getValue("tab.appearance"), createAppearancePanel());
		tabbedPane.addTab(Language.getValue("tab.behavior"), createBehaviorPanel());
		tabbedPane.addTab(Language.getValue("tab.game"), createGamePanel());
		tabbedPane.addTab(Language.getValue("tab.files"), createFilesPanel());
		tabbedPane.addTab(Language.getValue("tab.extratxt"), createExtraPanel());
		tabbedPane.addTab(Language.getValue("tab.mods"), createModsPanel());
		tabbedPane.addTab(Language.getValue("tab.connection"), createConnectionPanel());
		settingsGUIFrame.getContentPane().add(tabbedPane);
		
		JPanel titleBar = new JPanel();
		titleBar.setBounds(0, 0, settingsGUIFrame.getWidth(), 20);
		titleBar.setBackground(ColorUtil.getTitleBarColor());
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
		
		Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_CLOSE_O, 14, ColorUtil.getForegroundColor());
		JButton closeButton = new JButton(closeIcon);
		closeButton.setBounds(settingsGUIFrame.getWidth() - 22, 0, 20, 20);
		closeButton.setFocusPainted(false);
		closeButton.setFocusable(false);
		closeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
		closeButton.setFont(Fonts.fontMed);
		titleBar.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       settingsGUIFrame.dispose();
		    }
		});
		
		Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_MINIMIZE, 14, ColorUtil.getForegroundColor());
		JButton minimizeButton = new JButton(minimizeIcon);
		minimizeButton.setBounds(settingsGUIFrame.getWidth() - 42, 0, 20, 20);
		minimizeButton.setFocusPainted(false);
		minimizeButton.setFocusable(false);
		minimizeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
		minimizeButton.setFont(Fonts.fontMed);
		titleBar.add(minimizeButton);
		
		settingsGUIFrame.setLocationRelativeTo(null);
		
		settingsGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		    	LauncherGUI.launcherGUIFrame.setVisible(true);
		        SettingsEventHandler.saveAdditionalArgs();
		    }
		});
		
	}
	
	protected JPanel createAppearancePanel() {
		JPanel appearancePanel = new JPanel();
		appearancePanel.setLayout(null);
		
		JLabel headerLabel = new JLabel(Language.getValue("tab.appearance"));
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		headerLabel.setBounds(25, 11, 450, 50);
		headerLabel.setFont(Fonts.fontMedGiant);
		appearancePanel.add(headerLabel);
		
		JLabel labelStyle = new JLabel(Language.getValue("m.launcher_style"));
		labelStyle.setBounds(25, 90, 175, 18);
		labelStyle.setFont(Fonts.fontRegBig);
		appearancePanel.add(labelStyle);
		
		choiceStyle = new JComboBox<String>();
		choiceStyle.setBounds(25, 115, 150, 20);
		choiceStyle.setFocusable(false);
		choiceStyle.setFont(Fonts.fontReg);
		choiceStyle.addItem(Language.getValue("o.dark"));
		choiceStyle.addItem(Language.getValue("o.light"));
		appearancePanel.add(choiceStyle);
		choiceStyle.setSelectedIndex(Settings.launcherStyle.equals("dark") ? 0 : 1);
		choiceStyle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.styleChangeEvent(event);
			}
		});
		
		JLabel labelLanguage = new JLabel(Language.getValue("m.language"));
		labelLanguage.setBounds(225, 90, 175, 18);
		labelLanguage.setFont(Fonts.fontRegBig);
		appearancePanel.add(labelLanguage);
		
		choiceLanguage = new JComboBox<String>();
		choiceLanguage.setBounds(225, 115, 150, 20);
		choiceLanguage.setFocusable(false);
		choiceLanguage.setFont(Fonts.fontReg);
		for(String lang : Language.AVAILABLE_LANGUAGES) {
			choiceLanguage.addItem(lang);
		}
		appearancePanel.add(choiceLanguage);
		choiceLanguage.setSelectedItem(Language.getLangName(Settings.lang));
		choiceLanguage.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.languageChangeEvent(event);
			}
		});
		
		return appearancePanel;
	}
	
	protected JPanel createBehaviorPanel() {
		JPanel behaviorPanel = new JPanel();
		behaviorPanel.setLayout(null);
		
		JLabel headerLabel = new JLabel(Language.getValue("tab.behavior"));
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		headerLabel.setBounds(25, 11, 450, 50);
		headerLabel.setFont(Fonts.fontMedGiant);
		behaviorPanel.add(headerLabel);
		
		JLabel labelCleaning = new JLabel(Language.getValue("m.rebuilds"));
		labelCleaning.setBounds(25, 90, 350, 18);
		labelCleaning.setFont(Fonts.fontRegBig);
		behaviorPanel.add(labelCleaning);
		
		JLabel labelCleaningExplained = new JLabel(Language.getValue("m.file_cleaning_explained"));
		labelCleaningExplained.setBounds(25, 110, 600, 16);
		labelCleaningExplained.setFont(Fonts.fontReg);
		behaviorPanel.add(labelCleaningExplained);
		
		switchCleaning = new JToggleButton("");
		switchCleaning.setBounds(680, 95, 30, 23);
		switchCleaning.setFocusPainted(false);
		behaviorPanel.add(switchCleaning);
		switchCleaning.setSelected(Settings.doRebuilds);
		switchCleaning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.rebuildsChangeEvent(_action);
			}
		});
		
		JSeparator sep = new JSeparator();
		sep.setBounds(25, 140, 690, 16);
		behaviorPanel.add(sep);
		
		JLabel labelKeepOpen = new JLabel(Language.getValue("m.keep_open"));
		labelKeepOpen.setBounds(25, 155, 350, 18);
		labelKeepOpen.setFont(Fonts.fontRegBig);
		behaviorPanel.add(labelKeepOpen);
		
		JLabel labelKeepOpenExplained = new JLabel(Language.getValue("m.keep_open_explained"));
		labelKeepOpenExplained.setBounds(25, 175, 600, 16);
		labelKeepOpenExplained.setFont(Fonts.fontReg);
		behaviorPanel.add(labelKeepOpenExplained);
		
		switchKeepOpen = new JToggleButton("");
		switchKeepOpen.setBounds(680, 160, 30, 23);
		switchKeepOpen.setFocusPainted(false);
		behaviorPanel.add(switchKeepOpen);
		switchKeepOpen.setSelected(Settings.keepOpen);
		switchKeepOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.keepOpenChangeEvent(_action);
			}
		});
		
		JSeparator sep2 = new JSeparator();
		sep2.setBounds(25, 205, 690, 16);
		behaviorPanel.add(sep2);
		
		JLabel labelShortcut = new JLabel(Language.getValue("m.create_shortcut"));
		labelShortcut.setBounds(25, 220, 225, 18);
		labelShortcut.setFont(Fonts.fontRegBig);
		behaviorPanel.add(labelShortcut);
		
		JLabel labelShortcutExplained = new JLabel(Language.getValue("m.create_shortcut_explained"));
		labelShortcutExplained.setBounds(25, 240, 600, 16);
		labelShortcutExplained.setFont(Fonts.fontReg);
		behaviorPanel.add(labelShortcutExplained);
		
		switchShortcut = new JToggleButton("");
		switchShortcut.setBounds(680, 225, 30, 23);
		switchShortcut.setFocusPainted(false);
		behaviorPanel.add(switchShortcut);
		switchShortcut.setSelected(Settings.createShortcut);
		switchShortcut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.createShortcutChangeEvent(_action);
			}
		});
		
		return behaviorPanel;
	}
	
	protected JPanel createGamePanel() {
		JPanel gamePanel = new JPanel();
		gamePanel.setLayout(null);
		
		JLabel headerLabel = new JLabel(Language.getValue("tab.game"));
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		headerLabel.setBounds(25, 11, 450, 50);
		headerLabel.setFont(Fonts.fontMedGiant);
		gamePanel.add(headerLabel);
		
		JLabel labelStyle = new JLabel(Language.getValue("m.platform"));
		labelStyle.setBounds(25, 90, 125, 18);
		labelStyle.setFont(Fonts.fontRegBig);
		gamePanel.add(labelStyle);
		
		choicePlatform = new JComboBox<String>();
		choicePlatform.setBounds(25, 115, 150, 20);
		choicePlatform.setFont(Fonts.fontReg);
		choicePlatform.setFocusable(false);
		gamePanel.add(choicePlatform);
		choicePlatform.addItem(Language.getValue("o.steam"));
		choicePlatform.addItem(Language.getValue("o.standalone"));
		choicePlatform.setSelectedItem(Settings.gamePlatform);
		choicePlatform.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				SettingsEventHandler.platformChangeEvent(event);
			}
		});
		
		JLabel labelMemory = new JLabel(Language.getValue("m.allocated_memory"));
		labelMemory.setBounds(225, 90, 125, 18);
		labelMemory.setFont(Fonts.fontRegBig);
		gamePanel.add(labelMemory);
		
		choiceMemory = new JComboBox<String>();
		choiceMemory.setBounds(225, 115, 150, 20);
		choiceMemory.setFocusable(false);
		choiceMemory.setFont(Fonts.fontReg);
		gamePanel.add(choiceMemory);
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
		
		JSeparator sep = new JSeparator();
		sep.setBounds(25, 180, 690, 16);
		gamePanel.add(sep);
		
		JLabel labelStringDedup = new JLabel(Language.getValue("m.use_string_deduplication"));
		labelStringDedup.setBounds(25, 195, 225, 18);
		labelStringDedup.setFont(Fonts.fontRegBig);
		gamePanel.add(labelStringDedup);
		
		JLabel labelStringDedupExplained = new JLabel(Language.getValue("m.string_deduplication_explained"));
		labelStringDedupExplained.setBounds(25, 215, 600, 16);
		labelStringDedupExplained.setFont(Fonts.fontReg);
		gamePanel.add(labelStringDedupExplained);
		
		switchStringDedup = new JToggleButton("");
		switchStringDedup.setBounds(680, 200, 30, 23);
		switchStringDedup.setFocusPainted(false);
		gamePanel.add(switchStringDedup);
		switchStringDedup.setSelected(Settings.gameUseStringDeduplication);
		switchStringDedup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.useStringDeduplicationChangeEvent(_action);
			}
		});
		
		JSeparator sep2 = new JSeparator();
		sep2.setBounds(25, 245, 690, 16);
		gamePanel.add(sep2);
		
		JLabel labelUseG1GC = new JLabel(Language.getValue("m.use_g1gc"));
		labelUseG1GC.setBounds(25, 260, 275, 18);
		labelUseG1GC.setFont(Fonts.fontRegBig);
		gamePanel.add(labelUseG1GC);
		
		JLabel labelUseG1GCExplained = new JLabel(Language.getValue("m.use_g1gc_explained"));
		labelUseG1GCExplained.setBounds(25, 280, 600, 16);
		labelUseG1GCExplained.setFont(Fonts.fontReg);
		gamePanel.add(labelUseG1GCExplained);
		
		switchUseG1GC = new JToggleButton("");
		switchUseG1GC.setBounds(680, 265, 30, 23);
		switchUseG1GC.setFocusPainted(false);
		gamePanel.add(switchUseG1GC);
		switchUseG1GC.setSelected(Settings.gameUseG1GC);
		switchUseG1GC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.useG1GCChangeEvent(_action);
			}
		});
		
		JSeparator sep3 = new JSeparator();
		sep3.setBounds(25, 310, 690, 16);
		gamePanel.add(sep3);
		
		JLabel labelExplicitGC = new JLabel(Language.getValue("m.disable_explicit_gc"));
		labelExplicitGC.setBounds(25, 325, 275, 18);
		labelExplicitGC.setFont(Fonts.fontRegBig);
		gamePanel.add(labelExplicitGC);
		
		JLabel labelExplicitGCExplained = new JLabel(Language.getValue("m.explicit_gc_explained"));
		labelExplicitGCExplained.setBounds(25, 345, 600, 16);
		labelExplicitGCExplained.setFont(Fonts.fontReg);
		gamePanel.add(labelExplicitGCExplained);
		
		switchExplicitGC = new JToggleButton("");
		switchExplicitGC.setBounds(680, 330, 30, 23);
		switchExplicitGC.setFocusPainted(false);
		gamePanel.add(switchExplicitGC);
		switchExplicitGC.setSelected(Settings.gameDisableExplicitGC);
		switchExplicitGC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.disableExplicitGCChangeEvent(_action);
			}
		});
		
		JSeparator sep4 = new JSeparator();
		sep4.setBounds(25, 375, 690, 16);
		gamePanel.add(sep4);
		
		JLabel labelUndecoratedWindow = new JLabel(Language.getValue("m.undecorated_window"));
		labelUndecoratedWindow.setBounds(25, 390, 225, 18);
		labelUndecoratedWindow.setFont(Fonts.fontRegBig);
		gamePanel.add(labelUndecoratedWindow);
		
		JLabel labelUndecoratedWindowExplained = new JLabel(Language.getValue("m.undecorated_window_explained"));
		labelUndecoratedWindowExplained.setBounds(25, 410, 600, 16);
		labelUndecoratedWindowExplained.setFont(Fonts.fontReg);
		gamePanel.add(labelUndecoratedWindowExplained);
		
		switchUndecoratedWindow = new JToggleButton("");
		switchUndecoratedWindow.setBounds(680, 395, 30, 23);
		switchUndecoratedWindow.setFocusPainted(false);
		gamePanel.add(switchUndecoratedWindow);
		switchUndecoratedWindow.setSelected(Settings.gameUndecoratedWindow);
		switchUndecoratedWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsEventHandler.undecoratedWindowChangeEvent(_action);
			}
		});
		
		return gamePanel;
	}
	
	protected JPanel createFilesPanel() {
		JPanel filesPanel = new JPanel();
		filesPanel.setLayout(null);
		
		JLabel headerLabel = new JLabel(Language.getValue("tab.files"));
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		headerLabel.setBounds(25, 11, 450, 50);
		headerLabel.setFont(Fonts.fontMedGiant);
		filesPanel.add(headerLabel);
		
		JLabel labelFileClean = new JLabel(Language.getValue("b.force_rebuild"));
		labelFileClean.setBounds(25, 90, 275, 18);
		labelFileClean.setFont(Fonts.fontRegBig);
		filesPanel.add(labelFileClean);
		
		JLabel labelFileCleanExplained = new JLabel(Language.getValue("m.clean_files_explained"));
		labelFileCleanExplained.setBounds(25, 110, 600, 16);
		labelFileCleanExplained.setFont(Fonts.fontReg);
		filesPanel.add(labelFileCleanExplained);
		
		Icon startIcon = IconFontSwing.buildIcon(FontAwesome.SHARE, 16, ColorUtil.getForegroundColor());
		JButton forceRebuildButton = new JButton(startIcon);
		forceRebuildButton.setBounds(680, 95, 30, 23);
		forceRebuildButton.setFocusPainted(false);
		forceRebuildButton.setFocusable(false);
		filesPanel.add(forceRebuildButton);
		forceRebuildButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsGUI.settingsGUIFrame.setVisible(false);
				LauncherGUI.launcherGUIFrame.setVisible(true);
				SettingsEventHandler.forceRebuildEvent();
			}
		});
		
		return filesPanel;
	}
	
	protected JPanel createExtraPanel() {
		JPanel extraPanel = new JPanel();
		extraPanel.setLayout(null);
		
		JLabel headerLabel = new JLabel(Language.getValue("tab.extratxt"));
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		headerLabel.setBounds(25, 11, 450, 50);
		headerLabel.setFont(Fonts.fontMedGiant);
		extraPanel.add(headerLabel);
		
		JLabel labelArguments = new JLabel(Language.getValue("m.extratxt_write_arguments"));
		labelArguments.setBounds(25, 90, 600, 18);
		labelArguments.setFont(Fonts.fontRegBig);
		extraPanel.add(labelArguments);
		
		argumentsPane = new JEditorPane();
		argumentsPane.setFont(Fonts.fontReg);
		argumentsPane.setBounds(25, 120, 255, 85);
		extraPanel.add(argumentsPane);
		argumentsPane.setText(Settings.gameAdditionalArgs);
		
		JScrollPane scrollBar = new JScrollPane(argumentsPane);
		scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollBar.setBounds(25, 120, 690, 305);
		extraPanel.add(scrollBar);
		
		return extraPanel;
	}
	
	protected JPanel createModsPanel() {
		JPanel modsPanel = new JPanel();
		modsPanel.setLayout(null);
		
		JLabel headerLabel = new JLabel(Language.getValue("tab.mods"));
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		headerLabel.setBounds(25, 11, 450, 50);
		headerLabel.setFont(Fonts.fontMedGiant);
		modsPanel.add(headerLabel);
		
		JLabel soonLabel = new JLabel(Language.getValue("m.coming_soon"));
		soonLabel.setHorizontalAlignment(SwingConstants.LEFT);
		soonLabel.setBounds(25, 90, 450, 50);
		soonLabel.setFont(Fonts.fontRegBig);
		modsPanel.add(soonLabel);
		
		return modsPanel;
	}
	
	protected JPanel createConnectionPanel() {
		JPanel connectionPanel = new JPanel();
		connectionPanel.setLayout(null);
		
		JLabel headerLabel = new JLabel(Language.getValue("tab.connection"));
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		headerLabel.setBounds(25, 11, 450, 50);
		headerLabel.setFont(Fonts.fontMedGiant);
		connectionPanel.add(headerLabel);
		
		JLabel soonLabel = new JLabel(Language.getValue("m.coming_soon"));
		soonLabel.setHorizontalAlignment(SwingConstants.LEFT);
		soonLabel.setBounds(25, 90, 450, 50);
		soonLabel.setFont(Fonts.fontRegBig);
		connectionPanel.add(soonLabel);
		
		return connectionPanel;
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
