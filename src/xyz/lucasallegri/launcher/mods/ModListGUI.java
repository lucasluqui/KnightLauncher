package xyz.lucasallegri.launcher.mods;

import java.awt.EventQueue;

import javax.swing.JFrame;

import xyz.lucasallegri.launcher.Fonts;
import xyz.lucasallegri.launcher.Language;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.DesktopUtil;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;

public class ModListGUI {

	public static JFrame modListGUIFrame;
	public static List modListContainer;
	public static JLabel labelModCount;
	private JLabel labelModCountText;
	private JButton refreshButton;

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
		modListGUIFrame.setTitle(Language.getValue("t.mods"));
		modListGUIFrame.setBounds(100, 100, 380, 400);
		modListGUIFrame.setResizable(false);
		modListGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		modListGUIFrame.getContentPane().setLayout(null);
		
		modListContainer = new List();
		modListContainer.setBounds(10, 10, 162, 326);
		modListContainer.setFont(Fonts.fontMed);
		modListContainer.setFocusable(false);
		modListGUIFrame.getContentPane().add(modListContainer);
		for(Mod mod : ModList.installedMods) { modListContainer.add(mod.getDisplayName()); }
		
		labelModCount = new JLabel(""+ModList.installedMods.size());
		labelModCount.setHorizontalAlignment(SwingConstants.CENTER);
		labelModCount.setBounds(178, 43, 176, 40);
		labelModCount.setFont(Fonts.fontMedGiant);
		modListGUIFrame.getContentPane().add(labelModCount);
		
		labelModCountText = new JLabel(Language.getValue("m.mods_installed"));
		labelModCountText.setHorizontalAlignment(SwingConstants.CENTER);
		labelModCountText.setBounds(178, 92, 176, 14);
		labelModCountText.setFont(Fonts.fontReg);
		modListGUIFrame.getContentPane().add(labelModCountText);
		
		JButton modFolderButton = new JButton(Language.getValue("b.open_mods_folder"));
		modFolderButton.setBounds(101, 342, 136, 23);
		modFolderButton.setFont(Fonts.fontMed);
		modFolderButton.setFocusPainted(false);
		modFolderButton.setFocusable(false);
		modListGUIFrame.getContentPane().add(modFolderButton);
		modFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				DesktopUtil.openDir(System.getProperty("user.dir") + "/mods");
			}
		});
		
		JButton getModsButton = new JButton(Language.getValue("b.get_mods"));
		getModsButton.setBounds(240, 342, 126, 23);
		getModsButton.setFont(Fonts.fontMed);
		getModsButton.setFocusPainted(false);
		getModsButton.setFocusable(false);
		modListGUIFrame.getContentPane().add(getModsButton);
		getModsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				ModListEventHandler.getModsEvent(_action);
			}
		});
		
		refreshButton = new JButton(Language.getValue("b.refresh"));
		refreshButton.setBounds(9, 342, 89, 23);
		refreshButton.setFont(Fonts.fontMed);
		refreshButton.setFocusPainted(false);
		refreshButton.setFocusable(false);
		modListGUIFrame.getContentPane().add(refreshButton);
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				ModListEventHandler.refreshEvent(_action);
			}
		});
		
		modListGUIFrame.setLocationRelativeTo(null);
		
		modListGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        LauncherGUI.modButton.setEnabled(true);
		    }
		});
		
	}
}
