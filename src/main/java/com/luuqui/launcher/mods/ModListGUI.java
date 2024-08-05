package com.luuqui.launcher.mods;

import com.luuqui.launcher.*;
import com.luuqui.launcher.mods.data.Mod;
import com.luuqui.util.ColorUtil;
import com.luuqui.util.DesktopUtil;
import com.luuqui.util.ImageUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.luuqui.launcher.mods.Log.log;

public class ModListGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame modListGUIFrame;
  public static JPanel modListPanel;
  public static JPanel modListPane;
  public static JLabel labelModCount;
  public static JLabel labelRefreshing;
  public static JProgressBar refreshProgressBar;
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
    modListGUIFrame.getContentPane().setBackground(Colors.INTERFACE_MAINPANE_BACKGROUND);
    modListGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    modListGUIFrame.getContentPane().setLayout(null);
    modListPanel = (JPanel) modListGUIFrame.getContentPane();

    labelModCount = new JLabel(String.valueOf(ModLoader.getModList().size()));
    labelModCount.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCount.setBounds(25, 2, 188, 40);
    labelModCount.setFont(Fonts.fontMedGiant);
    modListGUIFrame.getContentPane().add(labelModCount);

    labelModCountText = new JLabel(Locale.getValue("m.mods_installed"));
    labelModCountText.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCountText.setBounds(26, 47, 188, 14);
    labelModCountText.setFont(Fonts.fontReg);
    modListGUIFrame.getContentPane().add(labelModCountText);

    JSeparator separator = new JSeparator();
    separator.setBounds(25, 75, 750, 2);
    modListGUIFrame.getContentPane().add(separator);

    refreshButton = new JButton(IconFontSwing.buildIcon(FontAwesome.REFRESH, 16, Color.WHITE));
    refreshButton.setBounds(25, 86, 25, 25);
    refreshButton.setFocusPainted(false);
    refreshButton.setFocusable(false);
    refreshButton.setBackground(null);
    refreshButton.setBorder(null);
    refreshButton.setToolTipText(Locale.getValue("b.refresh"));
    modListGUIFrame.getContentPane().add(refreshButton);
    refreshButton.addActionListener(ModListEventHandler::refreshEvent);

    labelRefreshing = new JLabel("Refreshing...");
    labelRefreshing.setBounds(10, 86, 160, 25);
    labelRefreshing.setFont(Fonts.fontReg);
    labelRefreshing.setHorizontalAlignment(SwingConstants.CENTER);
    labelRefreshing.setVerticalAlignment(SwingConstants.CENTER);
    labelRefreshing.setVisible(false);
    modListGUIFrame.getContentPane().add(labelRefreshing);

    refreshProgressBar = new JProgressBar();
    refreshProgressBar.setBounds(135, 94, 100, 10);
    refreshProgressBar.setVisible(false);
    modListGUIFrame.getContentPane().add(refreshProgressBar);

    JLabel enabledLabel = new JLabel("Enabled");
    enabledLabel.setBounds(636, 86, 100, 25);
    enabledLabel.setFont(Fonts.fontMed);
    modListGUIFrame.getContentPane().add(enabledLabel);

    JButton modFolderButton = new JButton(Locale.getValue("b.open_mods_folder"));
    modFolderButton.setBounds(600, 35, 170, 25);
    modFolderButton.setFont(Fonts.fontMed);
    modFolderButton.setFocusPainted(false);
    modFolderButton.setFocusable(false);
    modFolderButton.setToolTipText(Locale.getValue("b.open_mods_folder"));
    modListGUIFrame.getContentPane().add(modFolderButton);
    modFolderButton.addActionListener(action -> DesktopUtil.openDir(LauncherGlobals.USER_DIR + "/mods"));

    JButton getModsButton = new JButton("Get mods via Discord");
    getModsButton.setBounds(425, 35, 170, 25 );
    getModsButton.setFont(Fonts.fontMed);
    getModsButton.setFocusPainted(false);
    getModsButton.setFocusable(false);
    getModsButton.setToolTipText(Locale.getValue("Get mods via Discord"));
    modListGUIFrame.getContentPane().add(getModsButton);
    getModsButton.addActionListener(ModListEventHandler::getModsEvent);

    forceApplyButton = new JButton("Force apply");
    forceApplyButton.setBounds(280, 35, 160, 25);
    forceApplyButton.setFont(Fonts.fontMed);
    forceApplyButton.setFocusPainted(false);
    forceApplyButton.setFocusable(false);
    forceApplyButton.setToolTipText("Force apply");
    //modListGUIFrame.getContentPane().add(forceApplyButton);
    forceApplyButton.addActionListener(ModListEventHandler::forceApplyEvent);

    Icon modStoreIcon = IconFontSwing.buildIcon(FontAwesome.SEARCH, 12, ColorUtil.getForegroundColor());
    JButton modStoreButton = new JButton(Locale.getValue("Browse Mod Store"));
    modStoreButton.setIcon(modStoreIcon);
    modStoreButton.setBounds(250, 35, 170, 25);
    modStoreButton.setFont(Fonts.fontMed);
    modStoreButton.setFocusPainted(false);
    modStoreButton.setFocusable(false);
    modStoreButton.setEnabled(false);
    modStoreButton.setToolTipText("Not currently available");
    modListGUIFrame.getContentPane().add(modStoreButton);
    //getModsButton.addActionListener(ModListEventHandler::openModStore);

    modListPane = new JPanel();
    modListPane.setBackground(Colors.INTERFACE_MAINPANE_BACKGROUND);
    GridLayout layout = new GridLayout(ModLoader.getModCount(), 1);
    layout.setVgap(0);
    modListPane.setLayout(layout);
    modListPane.setPreferredSize(new Dimension(750, ModLoader.getModCount() * 76));

    updateModList();

    JScrollPane scrollBar = new JScrollPane(modListPane);
    scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollBar.setBounds(25, 120, 750, 340);
    scrollBar.setBorder(null);
    scrollBar.getVerticalScrollBar().setUnitIncrement(16);
    modListGUIFrame.getContentPane().add(scrollBar);
  }

  public static void updateModList() {

    Color[] backgroundColors = { Colors.INTERFACE_MODLIST_BACKGROUND_LIGHT, Colors.INTERFACE_MODLIST_BACKGROUND_DARK };
    int count = 0;

    if(modListPane == null) {
      log.error("What? how? why?! modListPane is null");
      return;
    }

    modListPane.removeAll();
    for (Mod mod : ModLoader.getModList()) {
      JPanel modPane = new JPanel();
      modPane.setLayout(null);
      modPane.setBounds(0, 0, 750, 76);
      modPane.setBackground((count & 1) == 0 ? backgroundColors[0] : backgroundColors[1]);

      JLabel modImage = new JLabel();
      BufferedImage defaultImage = ImageUtil.loadImageWithinJar("/img/mod-default-64.png");
      BufferedImage image = null;
      if (mod.getImage() != null) {
        image = ImageUtil.loadImageFromBase64(mod.getImage());
        image = ImageUtil.resizeImage(image, 64, 64);
      }
      modImage.setIcon(new ImageIcon(ImageUtil.addRoundedCorners(image == null ? defaultImage : image, 25)));
      modImage.setBounds(6, 6, 64, 64);
      modPane.add(modImage);

      JLabel modName = new JLabel();
      modName.setText(mod.getDisplayName());
      modName.setToolTipText(mod.getDisplayName());
      modName.setFont(Fonts.fontMed);
      modName.setBounds(81, 6, 150, 25);
      modPane.add(modName);

      JLabel modDescription = new JLabel();
      modDescription.setText(mod.getDescription());
      modDescription.setToolTipText(mod.getDescription());
      modDescription.setFont(Fonts.fontReg);
      modDescription.setBounds(81, 24, 450, 25);
      modPane.add(modDescription);

      JLabel modFooter = new JLabel();
      modFooter.setText("v" + mod.getVersion() + ", author: " + mod.getAuthor());
      modFooter.setFont(Fonts.fontRegSmall);
      modFooter.setBounds(81, 32, 400, 55);
      modPane.add(modFooter);

      JCheckBox enabledCheckbox = new JCheckBox();
      enabledCheckbox.setEnabled(true);
      enabledCheckbox.setVisible(true);
      enabledCheckbox.setFocusable(false);
      enabledCheckbox.setFocusPainted(false);
      enabledCheckbox.setBounds(625, 25, 25, 25);
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
      count++;
    }
    modListPane.updateUI();
  }
}
