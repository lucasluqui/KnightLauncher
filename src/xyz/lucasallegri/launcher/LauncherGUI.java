package xyz.lucasallegri.launcher;

import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.LauncherEventHandler;
import xyz.lucasallegri.launcher.mods.ModListGUI;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.DesktopUtil;
import xyz.lucasallegri.util.INetUtil;
import xyz.lucasallegri.util.ImageUtil;
import xyz.lucasallegri.util.SteamUtil;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

public class LauncherGUI {

	public static JFrame launcherGUIFrame;
	public static JButton launchButton;
	public static JButton settingsButton;
	public static JButton modButton;
	public static JTextPane tweetsContainer;
	public static JLabel launchState;
	public static JProgressBar launchProgressBar;
	public static JLabel imageContainer;
	
	public static Boolean showUpdateButton = false;
	public static Boolean offlineMode = false;

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
	}

	private void initialize() {
		
		launcherGUIFrame = new JFrame();
		launcherGUIFrame.setTitle(Language.getValue("t.main", LauncherConstants.VERSION));
		launcherGUIFrame.setResizable(false);
		launcherGUIFrame.setBounds(100, 100, 745, 440);
		launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIFrame.getContentPane().setLayout(null);
		
		launchButton = new JButton(Language.getValue("b.launch").toUpperCase());
		launchButton.setBounds(15, 350, 155, 48);
		launchButton.setFont(Fonts.fontMedBig);
		launchButton.setFocusPainted(false);
		launchButton.setFocusable(false);
		launcherGUIFrame.getContentPane().add(launchButton);
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				LauncherEventHandler.launchGameEvent(_action);
			}
		});
		
		String eventImageLang = Settings.lang.startsWith("es") ? "es" : "en";
		Image eventImage = ImageUtil.getImageFromURL(LauncherConstants.EVENT_QUERY_URL + eventImageLang + ".png", 515, 300);
		if(offlineMode) {
			imageContainer = new JLabel(Language.getValue("error.event_image_missing"));
		} else {
			imageContainer = new JLabel(new ImageIcon(eventImage));
		}
		imageContainer.setBounds(10, 10, 515, 300);
		imageContainer.setFont(Fonts.fontRegBig);
		imageContainer.setHorizontalAlignment(SwingConstants.CENTER);
		launcherGUIFrame.getContentPane().add(imageContainer);
		
		modButton = new JButton(Language.getValue("b.mods"));
		modButton.setBounds(532, 375, 89, 23);
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
		settingsButton.setBounds(626, 375, 100, 23);
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
		
		JLabel labelTweets = new JLabel(Language.getValue("m.twitter_title"));
		labelTweets.setBounds(534, 12, 127, 28);
		labelTweets.setFont(Fonts.fontReg);
		launcherGUIFrame.getContentPane().add(labelTweets);
		
		tweetsContainer = new JTextPane();
		tweetsContainer.setText(Language.getValue("m.twitter_load"));
		tweetsContainer.setBounds(535, 48, 189, 261);
		tweetsContainer.setEditable(false);
		tweetsContainer.setContentType("text/html");
		tweetsContainer.setFont(Fonts.fontReg);
		tweetsContainer.setBackground(Color.WHITE);
		launcherGUIFrame.getContentPane().add(tweetsContainer);
		String tweets = INetUtil.getWebpageContent(LauncherConstants.TWEETS_URL);
		if(offlineMode) {
			tweetsContainer.setText(Language.getValue("error.tweets_retrieve"));
		} else {
			String parsedTweets = tweets.replaceFirst("FONT_FAMILY", tweetsContainer.getFont().getFamily());
			tweetsContainer.setText(parsedTweets);
			tweetsContainer.setCaretPosition(0);
			JScrollPane tweetsJsp = new JScrollPane(tweetsContainer);
			tweetsJsp.setBounds(535, 48, 189, 261);
			launcherGUIFrame.getContentPane().add(tweetsJsp);
		}
		
		launchProgressBar = new JProgressBar();
		launchProgressBar.setBounds(180, 375, 342, 23);
		launchProgressBar.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchProgressBar);
		
		launchState = new JLabel("");
		launchState.setBounds(181, 356, 325, 14);
		launchState.setFont(Fonts.fontReg);
		launchState.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchState);
		
		JButton updateAvailableButton = new JButton(Language.getValue("b.update_available"));
		updateAvailableButton.setFont(Fonts.fontMedIta);
		updateAvailableButton.setFocusPainted(false);
		updateAvailableButton.setFocusable(false);
		updateAvailableButton.setForeground(new Color(0, 102, 34));
		updateAvailableButton.setVisible(showUpdateButton);
		updateAvailableButton.setBounds(532, 345, 194, 25);
		launcherGUIFrame.getContentPane().add(updateAvailableButton);
		updateAvailableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				DesktopUtil.openWebpage(LauncherConstants.RELEASES_URL);
			}
		});
		
		JLabel playerCountLabel = new JLabel("");
		if(offlineMode) {
			playerCountLabel.setText(Language.getValue("error.get_player_count"));
		} else {
			playerCountLabel.setText(Language.getValue("m.player_count", new String[] { SteamUtil.getCurrentPlayersApproximateTotal("99900"), SteamUtil.getCurrentPlayers("99900") }));
		}
		playerCountLabel.setFont(Fonts.fontReg);
		playerCountLabel.setForeground(new Color(0, 102, 34));
		playerCountLabel.setBounds(16, 331, 507, 14);
		launcherGUIFrame.getContentPane().add(playerCountLabel);
		
		launcherGUIFrame.setLocationRelativeTo(null);
		
		launcherGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        DiscordInstance.stop();
		    }
		});
		
		Boot.onBootEnd();
		
	}
}
