package xyz.lucasallegri.launcher;

import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.mods.ModListGUI;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.ColorUtil;
import xyz.lucasallegri.util.DesktopUtil;
import xyz.lucasallegri.util.ImageUtil;
import xyz.lucasallegri.util.SystemUtil;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import jiconfont.swing.IconFontSwing;
import mdlaf.utils.MaterialBorders;
import jiconfont.icons.font_awesome.FontAwesome;

public class LauncherGUI {

	public static JFrame launcherGUIFrame;
	public static JButton launchButton;
	public static JButton settingsButton;
	public static JButton modButton;
	public static JButton updateButton;
	public static JTextPane tweetsContainer;
	public static JLabel launchState;
	public static JProgressBar launchProgressBar;
	public static JLabel imageContainer;
	public static JLabel playerCountLabel;
	
	int pX, pY;

	public static void main(String[] args) {
		
		Boot.onBootStart();
		
		if(SystemUtil.is64Bit() && SystemUtil.isWindows() && !Settings.jvmPatched) {
			JVMPatcher.compose();
		} else {
			EventQueue.invokeLater(new Runnable() {
				@SuppressWarnings("static-access")
				public void run() {
					try {
						LauncherGUI window = new LauncherGUI();
						window.launcherGUIFrame.setVisible(true);
					} catch (Exception e) {
						KnightLog.logException(e);
					}
				}
			});
		}
	}

	public LauncherGUI() {
		initialize();
		Boot.onBootEnd();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		
		launcherGUIFrame = new JFrame();
		launcherGUIFrame.setTitle(Language.getValue("t.main", LauncherConstants.VERSION));
		launcherGUIFrame.setResizable(false);
		launcherGUIFrame.setBounds(100, 100, 850, 475);
		launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIFrame.setUndecorated(true);
		launcherGUIFrame.setIconImage(ImageUtil.loadImageWithinJar("/img/icon-128.png"));
		launcherGUIFrame.getContentPane().setLayout(null);
		
		Icon launchIcon = IconFontSwing.buildIcon(FontAwesome.PLAY_CIRCLE_O, 49, ColorUtil.getForegroundColor());
		launchButton = new JButton(launchIcon);
		launchButton.setBounds(21, 400, 52, 52);
		launchButton.setFont(Fonts.fontMedBig);
		launchButton.setFocusPainted(false);
		launchButton.setFocusable(false);
		launchButton.setToolTipText(Language.getValue("b.launch"));
		launcherGUIFrame.getContentPane().add(launchButton);
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				LauncherEventHandler.launchGameEvent(_action);
			}
		});
		
		imageContainer = new JLabel("");
		imageContainer.setBounds(23, 48, 525, 305);
		imageContainer.setFont(Fonts.fontRegBig);
		imageContainer.setHorizontalAlignment(SwingConstants.CENTER);
		launcherGUIFrame.getContentPane().add(imageContainer);
		
		Icon modsIcon = IconFontSwing.buildIcon(FontAwesome.PUZZLE_PIECE, 16, ColorUtil.getForegroundColor());
		modButton = new JButton(modsIcon);
		modButton.setBounds(80, 401, 30, 25);
		modButton.setFont(Fonts.fontMed);
		modButton.setFocusPainted(false);
		modButton.setFocusable(false);
		modButton.setToolTipText(Language.getValue("b.mods"));
		launcherGUIFrame.getContentPane().add(modButton);
		modButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				ModListGUI.compose();
			}
		});
		
		Icon settingsIcon = IconFontSwing.buildIcon(FontAwesome.COGS, 16, ColorUtil.getForegroundColor());
		settingsButton = new JButton(settingsIcon);
		settingsButton.setBounds(80, 427, 30, 25);
		settingsButton.setFont(Fonts.fontMed);
		settingsButton.setFocusPainted(false);
		settingsButton.setFocusable(false);
		settingsButton.setToolTipText(Language.getValue("b.settings"));
		launcherGUIFrame.getContentPane().add(settingsButton);
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsGUI.compose();
			}
		});
		
		JLabel labelTweets = new JLabel("<html>" + Language.getValue("m.twitter_title") + "</html>");
		labelTweets.setBounds(567, 36, 127, 28);
		labelTweets.setFont(Fonts.fontReg);
		launcherGUIFrame.getContentPane().add(labelTweets);
		
		tweetsContainer = new JTextPane();
		tweetsContainer.setText(Language.getValue("m.twitter_load"));
		tweetsContainer.setBounds(567, 75, 260, 297);
		tweetsContainer.setEditable(false);
		tweetsContainer.setContentType("text/html");
		tweetsContainer.setFont(Fonts.fontReg);
		tweetsContainer.setBackground(ColorUtil.getBackgroundColor());
		tweetsContainer.setForeground(Color.WHITE);
		launcherGUIFrame.getContentPane().add(tweetsContainer);
		
		/*
		 * Comment the following three lines to preview this GUI with WindowBuilder
		 * I have no idea why they're conflicting with it, but without doing so
		 * you won't be able to see anything, throwing errors on Language.getValue()
		 * during t.main and b.launch parsing. Java is fun :)
		 */
//		JScrollPane tweetsJsp = new JScrollPane(tweetsContainer);
//		tweetsJsp.setBounds(567, 75, 260, 297);
//		LauncherGUI.launcherGUIFrame.getContentPane().add(tweetsJsp);
		
		launchProgressBar = new JProgressBar();
		launchProgressBar.setBounds(0, 470, 850, 5);
		launchProgressBar.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchProgressBar);
		
		launchState = new JLabel("");
		launchState.setHorizontalAlignment(SwingConstants.RIGHT);
		launchState.setBounds(638, 443, 203, 23);
		launchState.setFont(Fonts.fontRegBig);
		launchState.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchState);
		
		Icon cloudIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, 20, ColorUtil.getGreenForegroundColor());
		updateButton = new JButton(Language.getValue("b.update_available"));
		updateButton.setHorizontalAlignment(SwingConstants.CENTER);
		updateButton.setIcon(cloudIcon);
		updateButton.setFont(Fonts.fontMedIta);
		updateButton.setFocusPainted(false);
		updateButton.setFocusable(false);
		updateButton.setForeground(ColorUtil.getGreenForegroundColor());
		updateButton.setVisible(false);
		updateButton.setBounds(120, 427, 180, 25);
		launcherGUIFrame.getContentPane().add(updateButton);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				DesktopUtil.openWebpage(LauncherConstants.RELEASES_URL);
			}
		});
		
		playerCountLabel = new JLabel(Language.getValue("m.player_count_load"));
		playerCountLabel.setFont(Fonts.fontReg);
		playerCountLabel.setForeground(ColorUtil.getGreenForegroundColor());
		playerCountLabel.setBounds(23, 378, 507, 14);
		launcherGUIFrame.getContentPane().add(playerCountLabel);
		
		JPanel titleBar = new JPanel();
		titleBar.setBounds(0, 0, launcherGUIFrame.getWidth(), 20);
		titleBar.setBackground(ColorUtil.getTitleBarColor());
		launcherGUIFrame.getContentPane().add(titleBar);
		
		
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
		
		        launcherGUIFrame.setLocation(launcherGUIFrame.getLocation().x + me.getX() - pX,
		        launcherGUIFrame.getLocation().y + me.getY() - pY);
		    }
		});
		titleBar.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent me) {
		
		        launcherGUIFrame.setLocation(launcherGUIFrame.getLocation().x + me.getX() - pX,
		        launcherGUIFrame.getLocation().y + me.getY() - pY);
		    }
		
			@Override
			public void mouseMoved(MouseEvent arg0) {
				// Auto-generated method stub
			}
		});
		titleBar.setLayout(null);
		
		JLabel windowTitle = new JLabel(Language.getValue("t.main", LauncherConstants.VERSION) + "    " + Language.getValue("t.powered_by"));
		windowTitle.setFont(Fonts.fontMed);
		windowTitle.setBounds(10, 0, launcherGUIFrame.getWidth() - 200, 20);
		titleBar.add(windowTitle);
		
		Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_CLOSE_O, 14, ColorUtil.getForegroundColor());
		JButton closeButton = new JButton(closeIcon);
		closeButton.setBounds(launcherGUIFrame.getWidth() - 22, 0, 20, 20);
		closeButton.setToolTipText("Close");
		closeButton.setFocusPainted(false);
		closeButton.setFocusable(false);
		closeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
		closeButton.setFont(Fonts.fontMed);
		titleBar.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       System.exit(0);
		    }
		});
		
		Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_MINIMIZE, 14, ColorUtil.getForegroundColor());
		JButton minimizeButton = new JButton(minimizeIcon);
		minimizeButton.setBounds(launcherGUIFrame.getWidth() - 42, 0, 20, 20);
		minimizeButton.setToolTipText("Minimize");
		minimizeButton.setFocusPainted(false);
		minimizeButton.setFocusable(false);
		minimizeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
		minimizeButton.setFont(Fonts.fontMed);
		titleBar.add(minimizeButton);
		minimizeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       launcherGUIFrame.setState(Frame.ICONIFIED);
		    }
		});
		
		JButton discordButton = new JButton(ImageUtil.imageStreamToIcon(LauncherGUI.class.getResourceAsStream("/img/discord-16.png")));
		discordButton.setBounds(launcherGUIFrame.getWidth() - 67, 1, 18, 18);
		discordButton.setToolTipText("Discord");
		discordButton.setFocusPainted(false);
		discordButton.setFocusable(false);
		discordButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
		discordButton.setFont(Fonts.fontMed);
		titleBar.add(discordButton);
		discordButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       DesktopUtil.openWebpage(LauncherConstants.DISCORD_URL);
		    }
		});
		
		Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, 16, ColorUtil.getForegroundColor());
		JButton bugButton = new JButton(bugIcon);
		bugButton.setBounds(launcherGUIFrame.getWidth() - 89, 1, 18, 18);
		bugButton.setToolTipText(Language.getValue("b.bug_report"));
		bugButton.setFocusPainted(false);
		bugButton.setFocusable(false);
		bugButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
		bugButton.setFont(Fonts.fontMed);
		titleBar.add(bugButton);
		bugButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       DesktopUtil.openWebpage(LauncherConstants.BUG_REPORT_URL);
		    }
		});
		
		Icon kofiIcon = IconFontSwing.buildIcon(FontAwesome.COFFEE, 16, DefaultColors.KOFI);
		JButton kofiButton = new JButton(kofiIcon);
		kofiButton.setBounds(launcherGUIFrame.getWidth() - 111, 1, 18, 18);
		kofiButton.setToolTipText("Support me on Ko-fi");
		kofiButton.setFocusPainted(false);
		kofiButton.setFocusable(false);
		kofiButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
		kofiButton.setFont(Fonts.fontMed);
		titleBar.add(kofiButton);
		kofiButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       DesktopUtil.openWebpage(LauncherConstants.KOFI_URL);
		    }
		});
		
		launcherGUIFrame.setLocationRelativeTo(null);
		
		launcherGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        DiscordInstance.stop();
		    }
		});
		
	}
}
