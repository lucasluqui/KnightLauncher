package xyz.lucasallegri.launcher;

import xyz.lucasallegri.dialog.DialogError;
import xyz.lucasallegri.launcher.LauncherEventHandler;
import xyz.lucasallegri.launcher.mods.ModListGUI;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.FileUtil;
import xyz.lucasallegri.util.ImageUtil;
import xyz.lucasallegri.util.SteamUtil;
import xyz.lucasallegri.util.WinRegistry;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
		
		/*
		 * Checking if we're being ran inside the game's directory, "getdown.txt" should always be present if so.
		 */
		if(!FileUtil.fileExists("getdown.txt")) {
			DialogError.push("You need to place this .jar inside your Spiral Knights main directory."
					+ System.lineSeparator() + SteamUtil.getGamePathWindows());
			return;
		}
		
		try {
			KnightLog.setup();
			SettingsProperties.setup();
		} catch (IOException ex) {
			KnightLog.logException(ex);
		}
		
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
		
		Boot.onBootStart();
		
		launcherGUIFrame = new JFrame();
		launcherGUIFrame.setTitle("KnightLauncher (" + LauncherConstants.VERSION + ")");
		launcherGUIFrame.setResizable(false);
		launcherGUIFrame.setBounds(100, 100, 750, 450);
		launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIFrame.getContentPane().setLayout(null);
		
		launchButton = new JButton("LAUNCH");
		launchButton.setBounds(17, 350, 155, 48);
		launchButton.setFont(Fonts.fontMedBig);
		launchButton.setFocusPainted(false);
		launchButton.setFocusable(false);
		launcherGUIFrame.getContentPane().add(launchButton);
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				LauncherEventHandler.launchEvent(_action);
			}
		});
		
		imageContainer = new JLabel(new ImageIcon(ImageUtil.getImageFromURL("http://px-api.lucasallegri.xyz/event.png")));
		imageContainer.setBounds(10, 10, 514, 311);
		launcherGUIFrame.getContentPane().add(imageContainer);
		
		modButton = new JButton("Mods");
		modButton.setBounds(537, 375, 89, 23);
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
		
		settingsButton = new JButton("Settings");
		settingsButton.setBounds(632, 375, 89, 23);
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
		
		JLabel labelTweets = new JLabel("<html>Latest on <b>@SpiralKnights</b></html>");
		labelTweets.setBounds(534, 12, 127, 28);
		labelTweets.setFont(Fonts.fontReg);
		launcherGUIFrame.getContentPane().add(labelTweets);
		
		tweetsContainer = new JLabel("Retrieving tweets...");
		tweetsContainer.setHorizontalAlignment(SwingConstants.CENTER);
		tweetsContainer.setBounds(535, 48, 189, 261);
		tweetsContainer.setFont(Fonts.fontReg);
		launcherGUIFrame.getContentPane().add(tweetsContainer);
		
		launchProgressBar = new JProgressBar();
		launchProgressBar.setBounds(182, 375, 342, 23);
		launchProgressBar.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchProgressBar);
		
		launchState = new JLabel("");
		launchState.setBounds(183, 356, 325, 14);
		launchState.setFont(Fonts.fontReg);
		launchState.setVisible(false);
		launcherGUIFrame.getContentPane().add(launchState);
		
		launcherGUIFrame.setLocationRelativeTo(null);
		
		Boot.onBootEnd();
		
	}
}
