package xyz.lucasallegri.launcher;

import xyz.lucasallegri.launcher.EventHandler;
import xyz.lucasallegri.util.ImageUtil;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class LauncherGUI {

	public static JFrame launcherGUIForm;
	public static JButton launchButton;
	public static JLabel tweetsContainer;
	public static JLabel launchState;
	public static JProgressBar launchProgressBar;
	public static JLabel imageContainer;

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
		launcherGUIForm.setTitle("KnightLauncher (" + LauncherConstants.VERSION + ")");
		launcherGUIForm.setBounds(100, 100, 750, 450);
		launcherGUIForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIForm.getContentPane().setLayout(null);
		
		launchButton = new JButton("LAUNCH");
		launchButton.setBounds(17, 350, 155, 48);
		launcherGUIForm.getContentPane().add(launchButton);
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				EventHandler.launchEvent(_action);
			}
		});
		
		imageContainer = new JLabel(new ImageIcon(ImageUtil.getImageFromURL("http://px-api.lucasallegri.xyz/event.png")));
		imageContainer.setBounds(10, 10, 514, 311);
		launcherGUIForm.getContentPane().add(imageContainer);
		
		JButton modButton = new JButton("Mods");
		modButton.setBounds(537, 375, 89, 23);
		launcherGUIForm.getContentPane().add(modButton);
		
		JButton settingsButton = new JButton("Settings");
		settingsButton.setBounds(630, 375, 89, 23);
		launcherGUIForm.getContentPane().add(settingsButton);
		
		JLabel labelTweets = new JLabel("<html>Latest on <b>@SpiralKnights</b></html>");
		labelTweets.setBounds(534, 12, 127, 28);
		launcherGUIForm.getContentPane().add(labelTweets);
		
		tweetsContainer = new JLabel("Retrieving tweets...");
		tweetsContainer.setBounds(535, 48, 189, 261);
		launcherGUIForm.getContentPane().add(tweetsContainer);
		
		launchProgressBar = new JProgressBar();
		launchProgressBar.setBounds(182, 375, 342, 23);
		launchProgressBar.setVisible(false);
		launcherGUIForm.getContentPane().add(launchProgressBar);
		
		launchState = new JLabel("You shouldn't be seeing this");
		launchState.setBounds(183, 356, 325, 14);
		launchState.setVisible(false);
		launcherGUIForm.getContentPane().add(launchState);
		
		launcherGUIForm.setLocationRelativeTo(null);
		
		Boot.onBootEnd();
		
	}
}
