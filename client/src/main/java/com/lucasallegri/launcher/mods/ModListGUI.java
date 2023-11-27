package com.lucasallegri.launcher.mods;

import com.lucasallegri.launcher.*;
import com.lucasallegri.launcher.mods.data.Mod;
import com.lucasallegri.util.DesktopUtil;
import com.lucasallegri.util.ImageUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ModListGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame modListGUIFrame;
  public static JPanel modListPanel;
  public static JPanel modListPane;
  public static JLabel labelModCount;
  public static JLabel labelForceApplyState;
  private JLabel labelModCountText;
  private JButton refreshButton;
  private JButton forceApplyButton;

  public ModListGUI(LauncherApp app) {
    super();
    this.app = app;
    initialize();
  }

  @SuppressWarnings("static-access")
  public void switchVisibility() {
    this.modListGUIFrame.setVisible(!this.modListGUIFrame.isVisible());
  }

  private void initialize() {
    modListGUIFrame = new JFrame();
    modListGUIFrame.setVisible(false);
    modListGUIFrame.setTitle(Locale.getValue("t.mods"));
    modListGUIFrame.setBounds(100, 100, 385, 460);
    modListGUIFrame.setResizable(false);
    modListGUIFrame.setUndecorated(true);
    modListGUIFrame.getContentPane().setBackground(new Color(56, 60, 71));
    modListGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    modListGUIFrame.getContentPane().setLayout(null);
    modListPanel = (JPanel) modListGUIFrame.getContentPane();

    labelModCount = new JLabel(String.valueOf(ModLoader.getModList().size()));
    labelModCount.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCount.setBounds(25, 15, 188, 40);
    labelModCount.setFont(Fonts.fontMedGiant);
    modListGUIFrame.getContentPane().add(labelModCount);

    labelModCountText = new JLabel(Locale.getValue("m.mods_installed"));
    labelModCountText.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCountText.setBounds(73, 40, 188, 14);
    labelModCountText.setFont(Fonts.fontReg);
    modListGUIFrame.getContentPane().add(labelModCountText);

    JSeparator separator = new JSeparator();
    separator.setBounds(25, 75, 750, 2);
    modListGUIFrame.getContentPane().add(separator);

    refreshButton = new JButton(IconFontSwing.buildIcon(FontAwesome.REFRESH, 16, Color.WHITE));
    refreshButton.setBounds(745, 86, 25, 25);
    refreshButton.setFocusPainted(false);
    refreshButton.setFocusable(false);
    refreshButton.setBackground(null);
    refreshButton.setBorder(null);
    refreshButton.setToolTipText(Locale.getValue("b.refresh"));
    modListGUIFrame.getContentPane().add(refreshButton);
    refreshButton.addActionListener(ModListEventHandler::refreshEvent);

    JLabel enabledLabel = new JLabel("Enabled");
    enabledLabel.setBounds(638, 86, 100, 25);
    enabledLabel.setFont(Fonts.fontMed);
    modListGUIFrame.getContentPane().add(enabledLabel);

    JButton modFolderButton = new JButton(Locale.getValue("b.open_mods_folder"));
    modFolderButton.setBounds(465, 35, 150, 25);
    modFolderButton.setFont(Fonts.fontMed);
    modFolderButton.setFocusPainted(false);
    modFolderButton.setFocusable(false);
    modFolderButton.setToolTipText(Locale.getValue("b.open_mods_folder"));
    modListGUIFrame.getContentPane().add(modFolderButton);
    modFolderButton.addActionListener(action -> DesktopUtil.openDir(LauncherGlobals.USER_DIR + "/mods"));

    JButton getModsButton = new JButton(Locale.getValue("b.get_mods"));
    getModsButton.setBounds(620, 35, 150, 25);
    getModsButton.setFont(Fonts.fontMed);
    getModsButton.setFocusPainted(false);
    getModsButton.setFocusable(false);
    getModsButton.setToolTipText(Locale.getValue("b.get_mods"));
    modListGUIFrame.getContentPane().add(getModsButton);
    getModsButton.addActionListener(ModListEventHandler::getModsEvent);

    forceApplyButton = new JButton("Force apply");
    forceApplyButton.setBounds(310, 35, 150, 25);
    forceApplyButton.setFont(Fonts.fontMed);
    forceApplyButton.setFocusPainted(false);
    forceApplyButton.setFocusable(false);
    forceApplyButton.setToolTipText("Force apply");
    modListGUIFrame.getContentPane().add(forceApplyButton);
    forceApplyButton.addActionListener(ModListEventHandler::forceApplyEvent);

    labelForceApplyState = new JLabel("");
    labelForceApplyState.setBounds(310, 20, 150, 25);
    labelForceApplyState.setFont(Fonts.fontReg);
    modListGUIFrame.getContentPane().add(labelForceApplyState);

    modListPane = new JPanel();
    modListPane.setBackground(new Color(56, 60, 71));
    GridLayout layout = new GridLayout(ModLoader.getModCount(), 1);
    layout.setVgap(15);
    modListPane.setLayout(layout);
    modListPane.setPreferredSize(new Dimension(750, ModLoader.getModCount() * 75));

    updateModList();

    JScrollPane scrollBar = new JScrollPane(modListPane);
    scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollBar.setBounds(27, 120, 750, 340);
    scrollBar.setBorder(null);
    modListGUIFrame.getContentPane().add(scrollBar);
  }

  public static void updateModList() {
    modListPane.removeAll();
    for (Mod mod : ModLoader.getModList()) {
      JPanel modPane = new JPanel();
      modPane.setLayout(null);
      modPane.setBounds(0, 0, 750, 64);
      modPane.setBackground(new Color(56, 60, 71));

      JLabel modImage = new JLabel();
      BufferedImage image = ImageUtil.loadImageWithinJar("/img/mod-default-64.png");
      modImage.setIcon(new ImageIcon(ImageUtil.addRoundedCorners(image, 25)));
      modImage.setBounds(0, 0, 64, 64);
      modPane.add(modImage);

      JLabel modName = new JLabel();
      modName.setText(mod.getDisplayName());
      modName.setFont(Fonts.fontMed);
      modName.setBounds(75, 0, 150, 25);
      modPane.add(modName);

      JLabel modDescription = new JLabel();
      modDescription.setText(mod.getDescription());
      modDescription.setFont(Fonts.fontReg);
      modDescription.setBounds(75, 3, 400, 55);
      modPane.add(modDescription);

      JLabel modFooter = new JLabel();
      modFooter.setText("v" + mod.getVersion() + ", author: " + mod.getAuthor());
      modFooter.setFont(Fonts.fontRegSmall);
      modFooter.setBounds(75, 25, 400, 55);
      modPane.add(modFooter);

      JCheckBox enabledCheckbox = new JCheckBox();
      enabledCheckbox.setEnabled(true);
      enabledCheckbox.setVisible(true);
      enabledCheckbox.setFocusable(false);
      enabledCheckbox.setFocusPainted(false);
      enabledCheckbox.setBounds(625, 15, 25, 25);
      enabledCheckbox.setSelected(mod.isEnabled());

      enabledCheckbox.addActionListener(l -> {
        if (enabledCheckbox.isSelected()) {
          ModListEventHandler.enableMod(mod);
        } else {
          ModListEventHandler.disableMod(mod);
        }
      });

      modPane.add(enabledCheckbox);

      modListPane.add(modPane);
    }
    modListPane.updateUI();
  }
}
