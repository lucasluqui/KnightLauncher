package xyz.lucasallegri.launcher;

import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.LauncherEventHandler;
import xyz.lucasallegri.launcher.mods.ModListGUI;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.DesktopUtil;
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

	public LauncherGUI() {
		initialize();
		Boot.onBootEnd();
	}

	private void initialize() {
		
		IconFontSwing.register(FontAwesome.getIconFont());
		
		launcherGUIFrame = new JFrame();
		launcherGUIFrame.setTitle(Language.getValue("t.main", LauncherConstants.VERSION));
		launcherGUIFrame.setResizable(false);
		launcherGUIFrame.setBounds(100, 100, 850, 475);
		launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIFrame.setUndecorated(true);
		launcherGUIFrame.getContentPane().setLayout(null);
		
		launchButton = new JButton(Language.getValue("b.launch").toUpperCase());
		launchButton.setBounds(10, 411, 155, 48);
		launchButton.setFont(Fonts.fontMedBig);
		launchButton.setFocusPainted(false);
		launchButton.setFocusable(false);
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
		
		modButton = new JButton(Language.getValue("b.mods"));
		modButton.setBounds(175, 434, 89, 25);
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
		
		settingsButton = new JButton(Language.getValue("b.settings"));
		settingsButton.setBounds(273, 434, 100, 25);
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
		tweetsContainer.setBackground(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_PRIMARY_DARK : Color.WHITE);
		tweetsContainer.setForeground(Color.WHITE);
		launcherGUIFrame.getContentPane().add(tweetsContainer);
		
		/*
		 * Comment the following three lines to preview this GUI with WindowBuilder
		 * I have no idea why they're conflicting with it, but without doing so
		 * you won't be able to see anything, throwing errors on Language.getValue()
		 * during t.main and b.launch parsing. Java is fun :)
		 */
		JScrollPane tweetsJsp = new JScrollPane(tweetsContainer);
		tweetsJsp.setBounds(567, 75, 260, 297);
		LauncherGUI.launcherGUIFrame.getContentPane().add(tweetsJsp);
		
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
		
		Icon cloudIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, 20, Settings.launcherStyle.equals("dark") ? DefaultColors.BRIGHT_GREEN : DefaultColors.DARK_GREEN);
		updateButton = new JButton(Language.getValue("b.update_available"));
		updateButton.setHorizontalAlignment(SwingConstants.CENTER);
		updateButton.setIcon(cloudIcon);
		updateButton.setFont(Fonts.fontMedIta);
		updateButton.setFocusPainted(false);
		updateButton.setFocusable(false);
		updateButton.setForeground(Settings.launcherStyle.equals("dark") ? DefaultColors.BRIGHT_GREEN : DefaultColors.DARK_GREEN);
		updateButton.setVisible(false);
		updateButton.setBounds(381, 434, 180, 25);
		launcherGUIFrame.getContentPane().add(updateButton);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				DesktopUtil.openWebpage(LauncherConstants.RELEASES_URL);
			}
		});
		
		playerCountLabel = new JLabel(Language.getValue("m.player_count_load"));
		playerCountLabel.setFont(Fonts.fontReg);
		playerCountLabel.setForeground(Settings.launcherStyle.equals("dark") ? DefaultColors.BRIGHT_GREEN : DefaultColors.DARK_GREEN);
		playerCountLabel.setBounds(12, 389, 507, 14);
		launcherGUIFrame.getContentPane().add(playerCountLabel);
		
		JPanel titleBar = new JPanel();
		titleBar.setBounds(0, 0, launcherGUIFrame.getWidth(), 20);
		titleBar.setBackground(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT);
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
		
		JLabel windowTitle = new JLabel(Language.getValue("t.main", LauncherConstants.VERSION));
		windowTitle.setFont(Fonts.fontMed);
		windowTitle.setBounds(10, 0, launcherGUIFrame.getWidth() - 200, 20);
		titleBar.add(windowTitle);
		
		JButton closeButton = new JButton("x");
		closeButton.setBounds(launcherGUIFrame.getWidth() - 22, 0, 20, 20);
		closeButton.setFocusPainted(false);
		closeButton.setFocusable(false);
		closeButton.setBorder(BorderFactory.createLineBorder(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT));
		closeButton.setFont(Fonts.fontMed);
		titleBar.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       System.exit(0);
		    }
		});
		
		JButton minimizeButton = new JButton("_");
		minimizeButton.setBounds(launcherGUIFrame.getWidth() - 42, 0, 20, 20);
		minimizeButton.setFocusPainted(false);
		minimizeButton.setFocusable(false);
		minimizeButton.setBorder(BorderFactory.createLineBorder(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT));
		minimizeButton.setFont(Fonts.fontMed);
		titleBar.add(minimizeButton);
		minimizeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       launcherGUIFrame.setState(Frame.ICONIFIED);
		    }
		});
		
		Icon discordIcon = IconFontSwing.buildIcon(FontAwesome.SLIDESHARE, 16, DefaultColors.DISCORD_PRIMARY);
		JButton discordButton = new JButton(discordIcon);
		discordButton.setBounds(launcherGUIFrame.getWidth() - 67, 0, 20, 20);
		discordButton.setToolTipText("Discord");
		discordButton.setFocusPainted(false);
		discordButton.setFocusable(false);
		discordButton.setBorder(BorderFactory.createLineBorder(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT));
		discordButton.setFont(Fonts.fontMed);
		titleBar.add(discordButton);
		discordButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       DesktopUtil.openWebpage("https://discord.gg/RAf499a");
		    }
		});
		
		Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, 16, Settings.launcherStyle.equals("dark") ? DefaultColors.BRIGHT_RED : DefaultColors.DARK_RED);
		JButton bugButton = new JButton(bugIcon);
		bugButton.setBounds(launcherGUIFrame.getWidth() - 89, 0, 20, 20);
		bugButton.setToolTipText("Report a Bug");
		bugButton.setFocusPainted(false);
		bugButton.setFocusable(false);
		bugButton.setBorder(BorderFactory.createLineBorder(Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT));
		bugButton.setFont(Fonts.fontMed);
		titleBar.add(bugButton);
		bugButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)
		    {
		       DesktopUtil.openWebpage(LauncherConstants.BUG_REPORT_URL);
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
