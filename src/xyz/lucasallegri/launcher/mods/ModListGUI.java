package xyz.lucasallegri.launcher.mods;

import xyz.lucasallegri.launcher.Fonts;
import xyz.lucasallegri.launcher.Language;
import xyz.lucasallegri.launcher.LauncherGUI;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.DesktopUtil;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JSeparator;

public class ModListGUI {

	public static JFrame modListGUIFrame;
	public static List modListContainer;
	public static JLabel labelModCount;
	private JLabel labelModCountText;
	private JButton refreshButton;
	private JButton enableButton;
	private JButton disableButton;
	private JLabel labelName;
	private JLabel labelDescription;
	private JLabel labelVersion;
	private JLabel labelCompatibility;
	private JLabel labelAuthor;

	public static void compose() {
		EventQueue.invokeLater(new Runnable() {

			@SuppressWarnings("static-access")
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
		LauncherGUI.modButton.setEnabled(false);
		initialize();
	}

	private void initialize() {
		modListGUIFrame = new JFrame();
		modListGUIFrame.setTitle(Language.getValue("t.mods"));
		modListGUIFrame.setBounds(100, 100, 385, 400);
		modListGUIFrame.setResizable(false);
		modListGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		modListGUIFrame.getContentPane().setLayout(null);
		
		modListContainer = new List();
		modListContainer.setBounds(10, 10, 162, 326);
		modListContainer.setFont(Fonts.fontMed);
		modListContainer.setBackground(new Color(45, 48, 56));
		modListContainer.setForeground(Color.WHITE);
		modListContainer.setFocusable(false);
		modListGUIFrame.getContentPane().add(modListContainer);
		for(Mod mod : ModList.installedMods) { modListContainer.add(mod.getDisplayName()); }
		modListContainer.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				labelName.setText(ModList.installedMods.get(modListContainer.getSelectedIndex()).getDisplayName());
				labelDescription.setText("<html>" + ModList.installedMods.get(modListContainer.getSelectedIndex()).getDescription() + "</html>");
				labelVersion.setText(Language.getValue("m.mod_version", ModList.installedMods.get(modListContainer.getSelectedIndex()).getVersion()));
				labelCompatibility.setText(Language.getValue("m.mod_compatibility", ModList.installedMods.get(modListContainer.getSelectedIndex()).getCompatibilityVersion()));
				labelAuthor.setText(Language.getValue("m.mod_author", ModList.installedMods.get(modListContainer.getSelectedIndex()).getAuthor()));
				enableButton.setEnabled(true);
				disableButton.setEnabled(true);
			}
		});
		
		labelModCount = new JLabel(String.valueOf(ModList.installedMods.size()));
		labelModCount.setHorizontalAlignment(SwingConstants.CENTER);
		labelModCount.setBounds(178, 31, 188, 40);
		labelModCount.setFont(Fonts.fontMedGiant);
		modListGUIFrame.getContentPane().add(labelModCount);
		
		labelModCountText = new JLabel(Language.getValue("m.mods_installed"));
		labelModCountText.setHorizontalAlignment(SwingConstants.CENTER);
		labelModCountText.setBounds(178, 80, 188, 14);
		labelModCountText.setFont(Fonts.fontReg);
		modListGUIFrame.getContentPane().add(labelModCountText);
		
		refreshButton = new JButton(Language.getValue("b.refresh"));
		refreshButton.setBounds(9, 342, 89, 23);
		refreshButton.setFont(Fonts.fontMed);
		refreshButton.setFocusPainted(false);
		refreshButton.setFocusable(false);
		refreshButton.setToolTipText(Language.getValue("b.refresh"));
		modListGUIFrame.getContentPane().add(refreshButton);
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				ModListEventHandler.refreshEvent(_action);
			}
		});
		
		JButton modFolderButton = new JButton(Language.getValue("b.open_mods_folder"));
		modFolderButton.setBounds(104, 342, 136, 23);
		modFolderButton.setFont(Fonts.fontMed);
		modFolderButton.setFocusPainted(false);
		modFolderButton.setFocusable(false);
		modFolderButton.setToolTipText(Language.getValue("b.open_mods_folder"));
		modListGUIFrame.getContentPane().add(modFolderButton);
		modFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				DesktopUtil.openDir(System.getProperty("user.dir") + "/mods");
			}
		});
		
		JButton getModsButton = new JButton(Language.getValue("b.get_mods"));
		getModsButton.setBounds(245, 342, 126, 23);
		getModsButton.setFont(Fonts.fontMed);
		getModsButton.setFocusPainted(false);
		getModsButton.setFocusable(false);
		getModsButton.setToolTipText(Language.getValue("b.get_mods"));
		modListGUIFrame.getContentPane().add(getModsButton);
		getModsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				ModListEventHandler.getModsEvent(_action);
			}
		});
		
		JSeparator separator = new JSeparator();
		separator.setBounds(178, 117, 195, 2);
		modListGUIFrame.getContentPane().add(separator);
		
		labelName = new JLabel("");
		labelName.setFont(Fonts.fontMed);
		labelName.setHorizontalAlignment(SwingConstants.CENTER);
		labelName.setBounds(178, 135, 188, 14);
		modListGUIFrame.getContentPane().add(labelName);
		
		labelAuthor = new JLabel("");
		labelAuthor.setFont(Fonts.fontReg);
		labelAuthor.setHorizontalAlignment(SwingConstants.CENTER);
		labelAuthor.setBounds(178, 152, 188, 14);
		modListGUIFrame.getContentPane().add(labelAuthor);
		
		labelDescription = new JLabel("");
		labelDescription.setFont(Fonts.fontReg);
		labelDescription.setHorizontalAlignment(SwingConstants.LEADING);
		labelDescription.setVerticalAlignment(SwingConstants.TOP);
		labelDescription.setBounds(188, 183, 178, 70);
		modListGUIFrame.getContentPane().add(labelDescription);
		
		labelVersion = new JLabel("");
		labelVersion.setFont(Fonts.fontReg);
		labelVersion.setBounds(188, 261, 178, 14);
		modListGUIFrame.getContentPane().add(labelVersion);
		
		labelCompatibility = new JLabel("");
		labelCompatibility.setFont(Fonts.fontReg);
		labelCompatibility.setBounds(188, 282, 178, 14);
		modListGUIFrame.getContentPane().add(labelCompatibility);
		
		enableButton = new JButton(Language.getValue("b.enable"));
		enableButton.setFont(Fonts.fontMed);
		enableButton.setForeground(new Color(0, 194, 65));
		enableButton.setEnabled(false);
		enableButton.setFocusable(false);
		enableButton.setFocusPainted(false);
		enableButton.setBounds(180, 309, 89, 23);
		modListGUIFrame.getContentPane().add(enableButton);
		
		disableButton = new JButton(Language.getValue("b.disable"));
		disableButton.setFont(Fonts.fontMed);
		disableButton.setForeground(new Color(194, 0, 0));
		disableButton.setEnabled(false);
		disableButton.setFocusable(false);
		disableButton.setFocusPainted(false);
		disableButton.setBounds(281, 309, 89, 23);
		modListGUIFrame.getContentPane().add(disableButton);
		
		modListGUIFrame.setLocationRelativeTo(null);
		
		modListGUIFrame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosed(WindowEvent windowEvent) {
		        LauncherGUI.modButton.setEnabled(true);
		    }
		});
		
	}
}
