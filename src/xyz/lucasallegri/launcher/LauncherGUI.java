package xyz.lucasallegri.launcher;

import xyz.lucasallegri.launcher.EventHandler;
import xyz.lucasallegri.util.ImageUtil;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Choice;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JProgressBar;

public class LauncherGUI {

	private JFrame launcherGUIForm;
	public static JLabel tweetsContainer;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LauncherGUI window = new LauncherGUI();
					window.launcherGUIForm.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
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
		
		Boot.onBootStart();
		
		launcherGUIForm = new JFrame();
		launcherGUIForm.setTitle("KnightLauncher — " + LauncherConstants.VERSION);
		launcherGUIForm.setBounds(100, 100, 750, 450);
		launcherGUIForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIForm.getContentPane().setLayout(null);
		
		JButton launchButton = new JButton("LAUNCH");
		launchButton.setBounds(17, 350, 155, 48);
		launcherGUIForm.getContentPane().add(launchButton);
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				EventHandler.launchEvent(_action);
			}
		});
		
		JLabel imageContainer = new JLabel(new ImageIcon(ImageUtil.getImageFromURL("https://content.spiralknights.com/images/glacial/glacial_uplink-lrg_en.png")));
		imageContainer.setBounds(10, 10, 514, 311);
		launcherGUIForm.getContentPane().add(imageContainer);
		
		JButton modButton = new JButton("Mods");
		modButton.setBounds(630, 346, 89, 23);
		launcherGUIForm.getContentPane().add(modButton);
		
		JButton settingsButton = new JButton("Settings");
		settingsButton.setBounds(630, 375, 89, 23);
		launcherGUIForm.getContentPane().add(settingsButton);
		
		JLabel labelTweets = new JLabel("<html>Latest on <b>@SpiralKnights</b></html>");
		labelTweets.setBounds(534, 10, 127, 14);
		launcherGUIForm.getContentPane().add(labelTweets);
		
		tweetsContainer = new JLabel("");
		tweetsContainer.setBounds(535, 39, 189, 270);
		launcherGUIForm.getContentPane().add(tweetsContainer);
		
		JProgressBar launchProgressBar = new JProgressBar();
		launchProgressBar.setBounds(182, 375, 342, 23);
		launchProgressBar.setVisible(false);
		launcherGUIForm.getContentPane().add(launchProgressBar);
		
		JLabel launchState = new JLabel("You shouldn't be seeing this");
		launchState.setBounds(183, 356, 342, 14);
		launchState.setVisible(false);
		launcherGUIForm.getContentPane().add(launchState);
		
		JLabel versionLabel = new JLabel(LauncherConstants.VERSION);
		versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		versionLabel.setBounds(561, 382, 58, 14);
		launcherGUIForm.getContentPane().add(versionLabel);
		
		Boot.onBootEnd();
		
	}
}
