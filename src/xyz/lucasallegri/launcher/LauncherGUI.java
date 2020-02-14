package xyz.lucasallegri.launcher;

import xyz.lucasallegri.launcher.LauncherEventHandler;
import xyz.lucasallegri.launcher.settings.SettingsGUI;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.ImageUtil;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class LauncherGUI {

	public static JFrame launcherGUIForm;
	public static JButton launchButton;
	public static JLabel tweetsContainer;
	public static JLabel launchState;
	public static JProgressBar launchProgressBar;
	public static JLabel imageContainer;
	public static Font fontReg = null;
	public static Font fontRegBig = null;
	public static Font fontMed = null;
	public static Font fontMedBig = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			KnightLog.setup();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LauncherGUI window = new LauncherGUI();
					window.launcherGUIForm.setVisible(true);
				} catch (Exception e) {
					KnightLog.log.severe(e.getLocalizedMessage());
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LauncherGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		InputStream fontRegIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Regular.ttf");
		InputStream fontRegBigIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Regular.ttf");
		InputStream fontMedIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Medium.ttf");
		InputStream fontMedBigIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Medium.ttf");
		fontReg = null;
		fontRegBig = null;
		fontMed = null;
		fontMedBig = null;
		try {
			fontReg = Font.createFont(Font.TRUETYPE_FONT, fontRegIs);
			fontReg = fontReg.deriveFont(11.0f);
			fontReg = fontReg.deriveFont(Font.ITALIC);
			
			fontRegBig = Font.createFont(Font.TRUETYPE_FONT, fontRegBigIs);
			fontRegBig = fontRegBig.deriveFont(14.0f);
			
			fontMed = Font.createFont(Font.TRUETYPE_FONT, fontMedIs);
			fontMed = fontMed.deriveFont(11.0f);
			
			fontMedBig = Font.createFont(Font.TRUETYPE_FONT, fontMedBigIs);
			fontMedBig = fontMedBig.deriveFont(14.0f);
		} catch (FontFormatException | IOException e) {
			KnightLog.log.severe(e.getLocalizedMessage());
		}
		
		Boot.onBootStart();
		
		launcherGUIForm = new JFrame();
		launcherGUIForm.setTitle("KnightLauncher (" + LauncherConstants.VERSION + ")");
		launcherGUIForm.setResizable(false);
		launcherGUIForm.setBounds(100, 100, 750, 450);
		launcherGUIForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIForm.getContentPane().setLayout(null);
		
		launchButton = new JButton("LAUNCH");
		launchButton.setBounds(17, 350, 155, 48);
		launchButton.setFont(fontMedBig);
		launcherGUIForm.getContentPane().add(launchButton);
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				LauncherEventHandler.launchEvent(_action);
			}
		});
		
		imageContainer = new JLabel(new ImageIcon(ImageUtil.getImageFromURL("http://px-api.lucasallegri.xyz/event.png")));
		imageContainer.setBounds(10, 10, 514, 311);
		launcherGUIForm.getContentPane().add(imageContainer);
		
		JButton modButton = new JButton("Mods");
		modButton.setBounds(537, 375, 89, 23);
		modButton.setFont(fontMed);
		launcherGUIForm.getContentPane().add(modButton);
		
		JButton settingsButton = new JButton("Settings");
		settingsButton.setBounds(630, 375, 89, 23);
		settingsButton.setFont(fontMed);
		launcherGUIForm.getContentPane().add(settingsButton);
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				SettingsGUI.compose();
			}
		});
		
		JLabel labelTweets = new JLabel("<html>Latest on <b>@SpiralKnights</b></html>");
		labelTweets.setBounds(534, 12, 127, 28);
		labelTweets.setFont(fontReg);
		launcherGUIForm.getContentPane().add(labelTweets);
		
		tweetsContainer = new JLabel("Retrieving tweets...");
		tweetsContainer.setHorizontalAlignment(SwingConstants.CENTER);
		tweetsContainer.setBounds(535, 48, 189, 261);
		tweetsContainer.setFont(fontReg);
		launcherGUIForm.getContentPane().add(tweetsContainer);
		
		launchProgressBar = new JProgressBar();
		launchProgressBar.setBounds(182, 375, 342, 23);
		launchProgressBar.setVisible(false);
		launcherGUIForm.getContentPane().add(launchProgressBar);
		
		launchState = new JLabel("");
		launchState.setBounds(183, 356, 325, 14);
		launchState.setFont(fontReg);
		launchState.setVisible(false);
		launcherGUIForm.getContentPane().add(launchState);
		
		launcherGUIForm.setLocationRelativeTo(null);
		
		Boot.onBootEnd();
		
	}
}
