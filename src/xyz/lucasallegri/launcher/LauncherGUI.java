package xyz.lucasallegri.launcher;

import xyz.lucasallegri.discord.DiscordInstance;
import xyz.lucasallegri.launcher.LauncherEventHandler;
import xyz.lucasallegri.launcher.mods.ModListGUI;
import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.ImageUtil;

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
import javax.swing.SwingConstants;

public class LauncherGUI {

	public static JFrame launcherGUIFrame;
	public static JButton launchButton;
	public static JButton settingsButton;
	public static JButton modButton;
	public static JLabel tweetsContainer;
	public static JLabel launchState;
	public static JProgressBar launchProgressBar;
	public static JLabel imageContainer;

	public static void main(String[] args) {
		
		Boot.onBootStart();
		
		EventQueue.invokeLater(new Runnable() {
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
		launcherGUIFrame.setBounds(100, 100, 750, 450);
		launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIFrame.getContentPane().setLayout(null);
		
		launchButton = new JButton(Language.getValue("b.launch"));
		launchButton.setBounds(14, 350, 155, 48);
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
		Image eventImage = ImageUtil.getImageFromURL(LauncherConstants.EVENT_QUERY_URL + eventImageLang + ".png", 514, 311);
		if(eventImage == null) {
			imageContainer = new JLabel(Language.getValue("error.event_image_missing"));
		} else {
			imageContainer = new JLabel(new ImageIcon(eventImage));
		}
		imageContainer.setBounds(10, 10, 514, 311);
		imageContainer.setFont(Fonts.fontRegBig);
		imageContainer.setHorizontalAlignment(SwingConstants.CENTER);
		launcherGUIFrame.getContentPane().add(imageContainer);
		
		modButton = new JButton(Language.getValue("b.mods"));
		modButton.setBounds(529, 375, 89, 23);
		modButton.setFont(Fonts.fontMed);
		modButton.setFocusPainted(false);
		modButton.setFocusable(false);
		launcherGUIFrame.getContentPane().add(modButton);
		modButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				modButton.setEnabled(false);
				ModListGUI.compose();
			}
		});
		
		settingsButton = new JButton(Language.getValue("b.settings"));
		settingsButton.setBounds(622, 375, 100, 23);
		settingsButton.setFont(Fonts.fontMed);
		settingsButton.setFocusPainted(false);
		settingsButton.setFocusable(false);
		launcherGUIFrame.getContentPane().add(settingsButton);
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				settingsButton.setEnabled(false);
				SettingsGUI.compose();
			}
		});
		
		JLabel labelTweets = new JLabel(Language.getValue("m.twitter_title"));
		labelTweets.setBounds(534, 12, 127, 28);
		labelTweets.setFont(Fonts.fontReg);
		launcherGUIFrame.getContentPane().add(labelTweets);
		
		tweetsContainer = new JLabel(Language.getValue("m.twitter_load"));
		tweetsContainer.setHorizontalAlignment(SwingConstants.CENTER);
		tweetsContainer.setBounds(535, 48, 189, 261);
		tweetsContainer.setFont(Fonts.fontReg);
		launcherGUIFrame.getContentPane().add(tweetsContainer);
		
		launchProgressBar = new JProgressBar();
		launchProgressBar.setBounds(179, 375, 342, 23);
		launchProgressBar.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchProgressBar);
		
		launchState = new JLabel("");
		launchState.setBounds(180, 356, 325, 14);
		launchState.setFont(Fonts.fontReg);
		launchState.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchState);
		
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
