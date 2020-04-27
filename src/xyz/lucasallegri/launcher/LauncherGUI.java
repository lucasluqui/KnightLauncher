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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.JPanel;

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
		
		Boot.onBootEnd();
	}

	public LauncherGUI() {
		initialize();
	}

	private void initialize() {
		
		launcherGUIFrame = new JFrame();
		launcherGUIFrame.setTitle(Language.getValue("t.main", LauncherConstants.VERSION));
		launcherGUIFrame.setResizable(false);
		launcherGUIFrame.setBounds(100, 100, 745, 440);
		launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIFrame.setUndecorated(true);
		launcherGUIFrame.getContentPane().setLayout(null);
		
		launchButton = new JButton(Language.getValue("b.launch").toUpperCase());
		launchButton.setBounds(15, 377, 155, 48);
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
		imageContainer.setBounds(10, 37, 515, 300);
		imageContainer.setFont(Fonts.fontRegBig);
		imageContainer.setHorizontalAlignment(SwingConstants.CENTER);
		launcherGUIFrame.getContentPane().add(imageContainer);
		
		modButton = new JButton(Language.getValue("b.mods"));
		modButton.setBounds(536, 402, 89, 23);
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
		settingsButton.setBounds(630, 402, 100, 23);
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
		labelTweets.setBounds(534, 39, 127, 28);
		labelTweets.setFont(Fonts.fontReg);
		launcherGUIFrame.getContentPane().add(labelTweets);
		
		tweetsContainer = new JTextPane();
		tweetsContainer.setText(Language.getValue("m.twitter_load"));
		tweetsContainer.setBounds(539, 75, 189, 261);
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
//		JScrollPane tweetsJsp = new JScrollPane(tweetsContainer);
//		tweetsJsp.setBounds(539, 75, 189, 261);
//		LauncherGUI.launcherGUIFrame.getContentPane().add(tweetsJsp);
		
		launchProgressBar = new JProgressBar();
		launchProgressBar.setBounds(180, 402, 346, 23);
		launchProgressBar.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchProgressBar);
		
		launchState = new JLabel("");
		launchState.setBounds(181, 383, 345, 14);
		launchState.setFont(Fonts.fontReg);
		launchState.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchState);
		
		updateButton = new JButton(Language.getValue("b.update_available"));
		updateButton.setFont(Fonts.fontMedIta);
		updateButton.setFocusPainted(false);
		updateButton.setFocusable(false);
		updateButton.setForeground(Settings.launcherStyle.equals("dark") ? DefaultColors.BRIGHT_GREEN : DefaultColors.DARK_GREEN);
		updateButton.setVisible(false);
		updateButton.setBounds(536, 372, 194, 25);
		launcherGUIFrame.getContentPane().add(updateButton);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				DesktopUtil.openWebpage(LauncherConstants.RELEASES_URL);
			}
		});
		
		playerCountLabel = new JLabel(Language.getValue("m.player_count_load"));
		playerCountLabel.setFont(Fonts.fontReg);
		playerCountLabel.setForeground(Settings.launcherStyle.equals("dark") ? DefaultColors.BRIGHT_GREEN : DefaultColors.DARK_GREEN);
		playerCountLabel.setBounds(16, 355, 507, 14);
		launcherGUIFrame.getContentPane().add(playerCountLabel);
		
		JPanel titleBar = new JPanel();
		titleBar.setBounds(0, 0, 745, 20);
		titleBar.setBackground(Color.BLACK);
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
		
		launcherGUIFrame.setLocationRelativeTo(null);
		
		launcherGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        DiscordInstance.stop();
		    }
		});
		
	}
}
