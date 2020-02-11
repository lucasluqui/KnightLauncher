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

public class LauncherGUI {

	private JFrame launcherGUIForm;
	public static Choice platformChoice;

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
		launcherGUIForm.setBounds(100, 100, 605, 350);
		launcherGUIForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		launcherGUIForm.getContentPane().setLayout(null);
		
		JButton launchButton = new JButton("LAUNCH");
		launchButton.setBounds(217, 248, 155, 48);
		launcherGUIForm.getContentPane().add(launchButton);
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				EventHandler.launchEvent(_action);
			}
		});
		
		platformChoice = new Choice();
		platformChoice.setBounds(10, 270, 88, 20);
		launcherGUIForm.getContentPane().add(platformChoice);
		
		JLabel platformLabel = new JLabel("Platform");
		platformLabel.setHorizontalAlignment(SwingConstants.CENTER);
		platformLabel.setBounds(10, 250, 88, 14);
		launcherGUIForm.getContentPane().add(platformLabel);
		
		JLabel imageContainer = new JLabel(new ImageIcon(ImageUtil.getImageFromURL("https://content.spiralknights.com/images/glacial/glacial_uplink-lrg_en.png")));
		imageContainer.setBounds(0, 0, 589, 234);
		launcherGUIForm.getContentPane().add(imageContainer);
		
		JButton modButton = new JButton("Mods");
		modButton.setBounds(490, 248, 89, 23);
		launcherGUIForm.getContentPane().add(modButton);
		
		JButton settingsButton = new JButton("Settings");
		settingsButton.setBounds(490, 273, 89, 23);
		launcherGUIForm.getContentPane().add(settingsButton);
		
		Boot.onBootEnd();
		
	}
}
