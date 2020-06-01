package xyz.lucasallegri.launcher.mods;

import xyz.lucasallegri.launcher.DefaultColors;
import xyz.lucasallegri.launcher.Fonts;
import xyz.lucasallegri.launcher.Language;
import xyz.lucasallegri.launcher.LauncherConstants;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.ColorUtil;
import xyz.lucasallegri.util.DesktopUtil;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import mdlaf.utils.MaterialBorders;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JSeparator;

public class ModListGUI {

	public static JFrame modListGUIFrame;
	public static List modListContainer;
	public static JLabel labelModCount;
	private JLabel labelModCountText;
	private JButton refreshButton;
	private JButton enableButton;
	private JButton disableButton;
	private JLabel labelName;
	private JLabel labelDescription;
	private JLabel labelVersion;
	private JLabel labelCompatibility;
	private JLabel labelAuthor;
	
	int pY, pX;

	public static void compose() {
		EventQueue.invokeLater(new Runnable() {

			@SuppressWarnings("static-access")
			public void run() {
				try {
					ModListGUI window = new ModListGUI();
					window.modListGUIFrame.setVisible(true);
				} catch (Exception e) {
					KnightLog.logException(e);
				}
			}
		});
	}

	public ModListGUI() {
		LauncherGUI.modButton.setEnabled(false);
		initialize();
	}

	private void initialize() {
		modListGUIFrame = new JFrame();
		modListGUIFrame.setTitle(Language.getValue("t.mods"));
		modListGUIFrame.setBounds(100, 100, 385, 400);
		modListGUIFrame.setResizable(false);
		modListGUIFrame.setUndecorated(true);
		modListGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		modListGUIFrame.getContentPane().setLayout(null);
		
		modListContainer = new List();
		modListContainer.setBounds(10, 26, 162, 326);
		modListContainer.setFont(Fonts.fontMed);
		modListContainer.setBackground(ColorUtil.getBackgroundColor());
		modListContainer.setForeground(ColorUtil.getForegroundColor());
		modListContainer.setFocusable(false);
		modListGUIFrame.getContentPane().add(modListContainer);
		for(Mod mod : ModList.installedMods) { modListContainer.add(mod.getDisplayName()); }
		modListContainer.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				labelName.setText(ModList.installedMods.get(modListContainer.getSelectedIndex()).getDisplayName());
				labelDescription.setText("<html>" + ModList.installedMods.get(modListContainer.getSelectedIndex()).getDescription() + "</html>");
				labelVersion.setText(Language.getValue("m.mod_version", ModList.installedMods.get(modListContainer.getSelectedIndex()).getVersion()));
				labelCompatibility.setText(Language.getValue("m.mod_compatibility", ModList.installedMods.get(modListContainer.getSelectedIndex()).getCompatibilityVersion()));
				labelAuthor.setText(Language.getValue("m.mod_author", ModList.installedMods.get(modListContainer.getSelectedIndex()).getAuthor()));
				enableButton.setEnabled(true);
				disableButton.setEnabled(true);
			}
		});
		
		labelModCount = new JLabel(String.valueOf(ModList.installedMods.size()));
		labelModCount.setHorizontalAlignment(SwingConstants.CENTER);
		labelModCount.setBounds(178, 44, 188, 40);
		labelModCount.setFont(Fonts.fontMedGiant);
		modListGUIFrame.getContentPane().add(labelModCount);
		
		labelModCountText = new JLabel(Language.getValue("m.mods_installed"));
		labelModCountText.setHorizontalAlignment(SwingConstants.CENTER);
		labelModCountText.setBounds(178, 93, 188, 14);
		labelModCountText.setFont(Fonts.fontReg);
		modListGUIFrame.getContentPane().add(labelModCountText);
		
		refreshButton = new JButton(Language.getValue("b.refresh"));
		refreshButton.setBounds(9, 370, 89, 23);
		refreshButton.setFont(Fonts.fontMed);
		refreshButton.setFocusPainted(false);
		refreshButton.setFocusable(false);
		refreshButton.setToolTipText(Language.getValue("b.refresh"));
		modListGUIFrame.getContentPane().add(refreshButton);
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				ModListEventHandler.refreshEvent(_action);
			}
		});
		
		JButton modFolderButton = new JButton(Language.getValue("b.open_mods_folder"));
		modFolderButton.setBounds(104, 370, 136, 23);
		modFolderButton.setFont(Fonts.fontMed);
		modFolderButton.setFocusPainted(false);
		modFolderButton.setFocusable(false);
		modFolderButton.setToolTipText(Language.getValue("b.open_mods_folder"));
		modListGUIFrame.getContentPane().add(modFolderButton);
		modFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				DesktopUtil.openDir(LauncherConstants.USER_DIR + "/mods");
			}
		});
		
		JButton getModsButton = new JButton(Language.getValue("b.get_mods"));
		getModsButton.setBounds(245, 370, 126, 23);
		getModsButton.setFont(Fonts.fontMed);
		getModsButton.setFocusPainted(false);
		getModsButton.setFocusable(false);
		getModsButton.setToolTipText(Language.getValue("b.get_mods"));
		modListGUIFrame.getContentPane().add(getModsButton);
		getModsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				ModListEventHandler.getModsEvent(_action);
			}
		});
		
		JSeparator separator = new JSeparator();
		separator.setBounds(178, 130, 195, 2);
		modListGUIFrame.getContentPane().add(separator);
		
		labelName = new JLabel("");
		labelName.setFont(Fonts.fontMed);
		labelName.setHorizontalAlignment(SwingConstants.CENTER);
		labelName.setBounds(178, 148, 188, 14);
		modListGUIFrame.getContentPane().add(labelName);
		
		labelAuthor = new JLabel("");
		labelAuthor.setFont(Fonts.fontReg);
		labelAuthor.setHorizontalAlignment(SwingConstants.CENTER);
		labelAuthor.setBounds(178, 165, 188, 14);
		modListGUIFrame.getContentPane().add(labelAuthor);
		
		labelDescription = new JLabel("");
		labelDescription.setFont(Fonts.fontReg);
		labelDescription.setHorizontalAlignment(SwingConstants.LEADING);
		labelDescription.setVerticalAlignment(SwingConstants.TOP);
		labelDescription.setBounds(188, 196, 178, 70);
		modListGUIFrame.getContentPane().add(labelDescription);
		
		labelVersion = new JLabel("");
		labelVersion.setFont(Fonts.fontReg);
		labelVersion.setBounds(188, 274, 178, 14);
		modListGUIFrame.getContentPane().add(labelVersion);
		
		labelCompatibility = new JLabel("");
		labelCompatibility.setFont(Fonts.fontReg);
		labelCompatibility.setBounds(188, 295, 178, 14);
		modListGUIFrame.getContentPane().add(labelCompatibility);
		
		enableButton = new JButton(Language.getValue("b.enable"));
		enableButton.setFont(Fonts.fontMed);
		enableButton.setForeground(ColorUtil.getGreenForegroundColor());
		enableButton.setEnabled(false);
		enableButton.setFocusable(false);
		enableButton.setFocusPainted(false);
		enableButton.setBounds(183, 326, 89, 23);
		modListGUIFrame.getContentPane().add(enableButton);
		
		disableButton = new JButton(Language.getValue("b.disable"));
		disableButton.setFont(Fonts.fontMed);
		disableButton.setForeground(ColorUtil.getRedForegroundColor());
		disableButton.setEnabled(false);
		disableButton.setFocusable(false);
		disableButton.setFocusPainted(false);
		disableButton.setBounds(281, 326, 89, 23);
		modListGUIFrame.getContentPane().add(disableButton);
		
		JPanel titleBar = new JPanel();
		titleBar.setBounds(0, 0, modListGUIFrame.getWidth(), 20);
		titleBar.setBackground(ColorUtil.getTitleBarColor());
		modListGUIFrame.getContentPane().add(titleBar);
		
		
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
		
		    	modListGUIFrame.setLocation(modListGUIFrame.getLocation().x + me.getX() - pX,
		    	modListGUIFrame.getLocation().y + me.getY() - pY);
		    }
		});
		titleBar.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent me) {
		
				modListGUIFrame.setLocation(modListGUIFrame.getLocation().x + me.getX() - pX,
				modListGUIFrame.getLocation().y + me.getY() - pY);
		    }
		
			@Override
			public void mouseMoved(MouseEvent arg0) {
				// Auto-generated method stub
			}
		});
		titleBar.setLayout(null);
		
		JLabel windowTitle = new JLabel(Language.getValue("t.mods"));
		windowTitle.setFont(Fonts.fontMed);
		windowTitle.setBounds(10, 0, modListGUIFrame.getWidth() - 100, 20);
		titleBar.add(windowTitle);
		
		Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_CLOSE_O, 14, ColorUtil.getForegroundColor());
		JButton closeButton = new JButton(closeIcon);
		closeButton.setBounds(modListGUIFrame.getWidth() - 22, 0, 20, 20);
		closeButton.setFocusPainted(false);
		closeButton.setFocusable(false);
		closeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
		closeButton.setFont(Fonts.fontMed);
		titleBar.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		    	modListGUIFrame.dispose();
		    }
		});
		
		Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_MINIMIZE, 14, ColorUtil.getForegroundColor());
		JButton minimizeButton = new JButton(minimizeIcon);
		minimizeButton.setBounds(modListGUIFrame.getWidth() - 42, 0, 20, 20);
		minimizeButton.setFocusPainted(false);
		minimizeButton.setFocusable(false);
		minimizeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
		minimizeButton.setFont(Fonts.fontMed);
		titleBar.add(minimizeButton);
		minimizeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		    	modListGUIFrame.setState(Frame.ICONIFIED);
		    }
		});
		
		modListGUIFrame.setLocationRelativeTo(null);
		
		modListGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        LauncherGUI.modButton.setEnabled(true);
		    }
		});
		
	}
}
