package xyz.lucasallegri.launcher.mods;

import java.awt.EventQueue;

import javax.swing.JFrame;

import xyz.lucasallegri.launcher.Fonts;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.logging.KnightLog;
import java.awt.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ModListGUI {

	public static JFrame modListGUIFrame;
	public static List modListContainer;
	public static JLabel labelModCount;
	private JLabel labelModCountText;

	public static void compose() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ModListGUI window = new ModListGUI();
					window.modListGUIFrame.setVisible(true);
				} catch (Exception e) {
					KnightLog.logException(e);
				}
			}
		});
	}

	public ModListGUI() {
		initialize();
	}

	private void initialize() {
		modListGUIFrame = new JFrame();
		modListGUIFrame.setTitle("KnightLauncher Mods");
		modListGUIFrame.setBounds(100, 100, 380, 400);
		modListGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		modListGUIFrame.getContentPane().setLayout(null);
		
		modListContainer = new List();
		modListContainer.setBounds(10, 10, 162, 341);
		modListContainer.setFont(Fonts.fontMed);
		modListContainer.setFocusable(false);
		modListGUIFrame.getContentPane().add(modListContainer);
		for(Mod mod : ModList.installedMods) { modListContainer.add(mod.getDisplayName()); }
		
		labelModCount = new JLabel(""+ModList.installedMods.size());
		labelModCount.setHorizontalAlignment(SwingConstants.CENTER);
		labelModCount.setBounds(178, 43, 176, 40);
		labelModCount.setFont(Fonts.fontMedGiant);
		modListGUIFrame.getContentPane().add(labelModCount);
		
		labelModCountText = new JLabel("Mods installed");
		labelModCountText.setHorizontalAlignment(SwingConstants.CENTER);
		labelModCountText.setBounds(178, 92, 176, 14);
		labelModCountText.setFont(Fonts.fontReg);
		modListGUIFrame.getContentPane().add(labelModCountText);
		
		modListGUIFrame.setLocationRelativeTo(null);
		
		modListGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        LauncherGUI.modButton.setEnabled(true);
		    }
		});
		
	}
}
