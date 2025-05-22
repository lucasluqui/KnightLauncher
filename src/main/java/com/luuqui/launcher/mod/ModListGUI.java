package com.luuqui.launcher.mod;

import com.formdev.flatlaf.FlatClientProperties;
import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.*;
import com.luuqui.launcher.mod.data.JarMod;
import com.luuqui.launcher.mod.data.Mod;
import com.luuqui.launcher.mod.data.Modpack;
import com.luuqui.launcher.mod.data.ZipMod;
import com.luuqui.util.ColorUtil;
import com.luuqui.util.ImageUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import static com.luuqui.launcher.mod.Log.log;

public class ModListGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame modListGUIFrame;

  protected static JButton warningNotice = new JButton();
  protected static String currentWarning = "";

  private JLabel labelModCountText;

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
    modListGUIFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    modListGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    modListGUIFrame.getContentPane().setLayout(null);
    modListPanel = (JPanel) modListGUIFrame.getContentPane();

    labelModCount = new JLabel();
    labelModCount.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCount.setBounds(25, 2, 188, 40);
    labelModCount.setFont(Fonts.fontMedGiant);
    modListGUIFrame.getContentPane().add(labelModCount);

    labelModCountText = new JLabel(Locale.getValue("m.mods_installed"));
    labelModCountText.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCountText.setBounds(26, 47, 188, 14);
    labelModCountText.setFont(Fonts.fontReg);
    modListGUIFrame.getContentPane().add(labelModCountText);

    Icon warningNoticeIcon = IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, 16, Color.WHITE);
    warningNotice = new JButton(warningNoticeIcon);
    warningNotice.setBounds(125, 24, 36, 36);
    warningNotice.setToolTipText(Locale.getValue("m.warning_notice"));
    warningNotice.setFocusPainted(false);
    warningNotice.setFocusable(false);
    warningNotice.setBorderPainted(false);
    warningNotice.setForeground(Color.WHITE);
    warningNotice.setBackground(CustomColors.MID_RED);
    warningNotice.setFont(Fonts.fontMed);
    warningNotice.setVisible(false);
    warningNotice.addActionListener(l -> {
      Dialog.push(currentWarning, Locale.getValue("m.warning_notice"), JOptionPane.ERROR_MESSAGE);
    });
    modListGUIFrame.getContentPane().add(warningNotice);


    Icon modStoreIcon = IconFontSwing.buildIcon(FontAwesome.SEARCH, 10, Color.WHITE);
    JButton modStoreButton = new JButton(Locale.getValue("b.mod_store"));
    modStoreButton.setIcon(modStoreIcon);
    modStoreButton.setBounds(240, 5, 175, 25);
    modStoreButton.setFont(Fonts.fontMed);
    modStoreButton.setFocusPainted(false);
    modStoreButton.setFocusable(false);
    modStoreButton.setEnabled(false);
    modStoreButton.setToolTipText(Locale.getValue("m.coming_soon"));
    modListGUIFrame.getContentPane().add(modStoreButton);
    //getModsButton.addActionListener(ModListEventHandler::openModStore);

    JButton getModsButton = new JButton(Locale.getValue("b.get_mods"));
    getModsButton.setBounds(420, 5, 175, 25);
    getModsButton.setFont(Fonts.fontMed);
    getModsButton.setFocusPainted(false);
    getModsButton.setFocusable(false);
    getModsButton.setToolTipText(Locale.getValue("b.get_mods"));
    modListGUIFrame.getContentPane().add(getModsButton);
    getModsButton.addActionListener(ModListEventHandler::getModsEvent);

    JButton modFolderButton = new JButton(Locale.getValue("b.open_mods_folder"));
    modFolderButton.setBounds(600, 5, 175, 25);
    modFolderButton.setFont(Fonts.fontMed);
    modFolderButton.setFocusPainted(false);
    modFolderButton.setFocusable(false);
    modFolderButton.setToolTipText(Locale.getValue("b.open_mods_folder"));
    modListGUIFrame.getContentPane().add(modFolderButton);
    modFolderButton.addActionListener(ModListEventHandler::openModsFolderEvent);

    enableAllModsButton = new JButton(Locale.getValue("b.enable_all_mods"));
    enableAllModsButton.setBounds(420, 35, 175, 25 );
    enableAllModsButton.setFont(Fonts.fontMed);
    enableAllModsButton.setFocusPainted(false);
    enableAllModsButton.setFocusable(false);
    enableAllModsButton.setToolTipText(Locale.getValue("b.enable_all_mods"));
    modListGUIFrame.getContentPane().add(enableAllModsButton);
    enableAllModsButton.addActionListener(ModListEventHandler::enableAllModsEvent);

    disableAllModsButton = new JButton(Locale.getValue("b.disable_all_mods"));
    disableAllModsButton.setBounds(600, 35, 175, 25);
    disableAllModsButton.setFont(Fonts.fontMed);
    disableAllModsButton.setFocusPainted(false);
    disableAllModsButton.setFocusable(false);
    disableAllModsButton.setToolTipText(Locale.getValue("b.disable_all_mods"));
    modListGUIFrame.getContentPane().add(disableAllModsButton);
    disableAllModsButton.addActionListener(ModListEventHandler::disableAllModsEvent);

    JSeparator separator = new JSeparator();
    separator.setBounds(25, 75, 750, 2);
    modListGUIFrame.getContentPane().add(separator);

    addModButton = new JButton(IconFontSwing.buildIcon(FontAwesome.PLUS, 18, CustomColors.MID_GREEN));
    addModButton.setBounds(25, 86, 25, 25);
    addModButton.setFocusPainted(false);
    addModButton.setFocusable(false);
    addModButton.setBackground(null);
    addModButton.setBorder(null);
    addModButton.setToolTipText(Locale.getValue("b.add_mod"));
    modListGUIFrame.getContentPane().add(addModButton);
    addModButton.addActionListener(ModListEventHandler::addModEvent);

    refreshButton = new JButton(IconFontSwing.buildIcon(FontAwesome.REFRESH, 16, Color.WHITE));
    refreshButton.setBounds(55, 86, 25, 25);
    refreshButton.setFocusPainted(false);
    refreshButton.setFocusable(false);
    refreshButton.setBackground(null);
    refreshButton.setBorder(null);
    refreshButton.setToolTipText(Locale.getValue("b.refresh"));
    modListGUIFrame.getContentPane().add(refreshButton);
    refreshButton.addActionListener(ModListEventHandler::refreshEvent);

    refreshProgressBar = new JProgressBar();
    refreshProgressBar.setBounds(110, 90, 100, 16);
    refreshProgressBar.setStringPainted(true);
    refreshProgressBar.setVisible(false);
    modListGUIFrame.getContentPane().add(refreshProgressBar);

    searchBox = new JTextField();
    searchBox.setBounds(250, 85, 300, 27);
    searchBox.setFont(Fonts.fontCodeReg);
    searchBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Locale.getValue("m.modlist_searchbox_placeholder"));
    modListGUIFrame.getContentPane().add(searchBox);
    searchBox.addActionListener(l -> ModListEventHandler.searchMod());
    searchBox.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) { ModListEventHandler.searchMod(); }
    });

    JButton searchButton = new JButton(Locale.getValue("m.search"));
    searchButton.setBounds(475, 85, 80, 26);
    modListGUIFrame.getContentPane().add(searchButton);
    searchButton.addActionListener(l -> ModListEventHandler.searchMod());
    searchButton.setVisible(false);

    JLabel enabledLabel = new JLabel(Locale.getValue("m.enabled"));
    enabledLabel.setBounds(636, 86, 100, 25);
    enabledLabel.setFont(Fonts.fontMed);
    modListGUIFrame.getContentPane().add(enabledLabel);

    modListPane = new JPanel();
    modListPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    modListPaneScrollBar = new JScrollPane(modListPane);
    modListPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    modListPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    modListPaneScrollBar.setBounds(25, 120, 750, Math.min(325, ModLoader.getModCount() * 76));
    modListPaneScrollBar.setBorder(null);
    modListPaneScrollBar.getVerticalScrollBar().setUnitIncrement(16);
    modListGUIFrame.getContentPane().add(modListPaneScrollBar);

    displayedModsLabel = new JLabel();
    displayedModsLabel.setBounds(25, 451, 300, 15);
    displayedModsLabel.setFont(Fonts.fontReg);
    displayedModsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    displayedModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    modListGUIFrame.getContentPane().add(displayedModsLabel);

    viewingModsLabel = new JLabel(Locale.getValue("m.viewing_mods", "Official"));
    viewingModsLabel.setBounds(25, 451, 740, 15);
    viewingModsLabel.setFont(Fonts.fontReg);
    viewingModsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    viewingModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    modListGUIFrame.getContentPane().add(viewingModsLabel);

    updateModList(null);
  }

  public static void updateModList(String searchString) {

    Color[] backgroundColors = { CustomColors.INTERFACE_MODLIST_BACKGROUND_LIGHT, CustomColors.INTERFACE_MODLIST_BACKGROUND_DARK };
    int count = 0;

    if(modListPane == null) {
      log.warning("What? how? why?! modListPane is null");
      return;
    }

    modListPane.removeAll();

    for (Mod mod : ModLoader.getModList()) {

      if(searchString != null) {
        // mod name or author doesn't match the search string, sayonara.
        if(!mod.getDisplayName().toLowerCase().contains(searchString.toLowerCase())
          && !mod.getAuthor().toLowerCase().contains(searchString.toLowerCase())) continue;
      }

      JPanel modPane = new JPanel();
      modPane.setLayout(null);
      modPane.setBounds(0, 0, 750, 76);
      modPane.setBackground((count & 1) == 0 ? backgroundColors[0] : backgroundColors[1]);

      JLabel modImage = new JLabel();
      BufferedImage defaultImage = ImageUtil.loadImageWithinJar("/img/default-64.png");
      BufferedImage image = null;
      if (mod.getImage() != null) {
        image = ImageUtil.loadImageFromBase64(mod.getImage());
        image = ImageUtil.resizeImagePreserveTransparency(image, 64, 64);
      }
      modImage.setIcon(new ImageIcon(ImageUtil.addRoundedCorners(image == null ? defaultImage : image, 25)));
      modImage.setBounds(6, 6, 64, 64);
      modPane.add(modImage);

      JLabel modName = new JLabel();
      modName.setText(mod.getDisplayName());
      modName.setToolTipText(mod.getDisplayName());
      modName.setFont(Fonts.fontMed);
      modName.setBounds(81, 6, 250, 25);
      modPane.add(modName);

      JLabel modDescription = new JLabel();
      modDescription.setText(mod.getDescription());
      modDescription.setToolTipText(mod.getDescription());
      modDescription.setFont(Fonts.fontReg);
      modDescription.setBounds(81, 24, 450, 25);
      modPane.add(modDescription);

      JLabel modFooter = new JLabel();
      modFooter.setText(Locale.getValue("m.mod_footer", new String[] { mod.getVersion(), mod.getAuthor() }));
      modFooter.setFont(Fonts.fontRegSmall);
      modFooter.setBounds(81, 32, 150, 55);
      modPane.add(modFooter);

      JLabel modBadge = new JLabel();
      modBadge.setBounds(241, 50, 86, 18);
      modBadge.setHorizontalAlignment(SwingConstants.CENTER);
      modBadge.setFont(Fonts.fontRegSmall);
      if(mod instanceof ZipMod) {
        modBadge.setText(Locale.getValue("m.resource_mod"));
        modBadge.putClientProperty(FlatClientProperties.STYLE,
          "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_RESOURCE_BACKGROUND)
            + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_RESOURCE_FOREGROUND)
            + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_RESOURCE_BACKGROUND));
        modBadge.setBounds(
          modBadge.getX(),
          modBadge.getY(),
          modBadge.getWidth() + 19,
          modBadge.getHeight()
        );
      } else if (mod instanceof JarMod) {
        modBadge.setText(Locale.getValue("m.code_mod"));
        modBadge.putClientProperty(FlatClientProperties.STYLE,
          "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND)
            + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_FOREGROUND)
            + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND));
      } else if (mod instanceof Modpack) {
        modBadge.setText(Locale.getValue("m.modpack"));
        modBadge.putClientProperty(FlatClientProperties.STYLE,
          "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_PACK_BACKGROUND)
            + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_PACK_FOREGROUND)
            + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_PACK_BACKGROUND));
      }
      modBadge.setVisible(true);
      modBadge.setToolTipText(modBadge.getText());
      modPane.add(modBadge);

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

      JButton removeButton = new JButton(IconFontSwing.buildIcon(FontAwesome.TRASH, 18, CustomColors.BUTTON_FOREGROUND_DANGER));
      removeButton.setBounds(680, 25, 25, 25);
      removeButton.setFocusPainted(false);
      removeButton.setFocusable(false);
      removeButton.setBackground(null);
      removeButton.setBorder(null);
      removeButton.setToolTipText(Locale.getValue("b.remove_mod", mod.getDisplayName()));
      removeButton.addActionListener(l -> ModListEventHandler.removeModEvent(mod));

      modPane.add(removeButton);

      if(mod instanceof JarMod) {
        if(!((JarMod) mod).isJDKCompatible() || !((JarMod) mod).isPXCompatible()) {
          JLabel jarModIncompatBadge = new JLabel();
          jarModIncompatBadge.setBounds(340, 50, 100, 18);
          jarModIncompatBadge.setHorizontalAlignment(SwingConstants.CENTER);
          jarModIncompatBadge.setFont(Fonts.fontRegSmall);
          jarModIncompatBadge.setText(Locale.getValue("m.code_mod_jdkincompatible"));
          jarModIncompatBadge.setToolTipText(jarModIncompatBadge.getText());
          jarModIncompatBadge.setVisible(true);
          jarModIncompatBadge.putClientProperty(FlatClientProperties.STYLE,
                  "background:" + ColorUtil.colorToHexString(CustomColors.BRIGHT_RED)
                          + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.MID_RED)
                          + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.BRIGHT_RED));
          modPane.add(jarModIncompatBadge);

          enabledCheckbox.setSelected(false);
          enabledCheckbox.setEnabled(false);
        }
      }

      modListPane.add(modPane);
      count++;
    }

    GridLayout layout = new GridLayout(count, 1);
    layout.setVgap(0);
    modListPane.setLayout(layout);

    modListPane.setPreferredSize(new Dimension(750, count * 76));

    modListPaneScrollBar.setBounds(
      modListPaneScrollBar.getX(),
      modListPaneScrollBar.getY(),
      modListPaneScrollBar.getWidth(),
      Math.min(325, count * 76)
    );

    displayedModsLabel.setText(Locale.getValue("m.modlist_footer", new String[] { String.valueOf(count), String.valueOf(ModLoader.getModCount()) }));
    displayedModsLabel.setBounds(
      displayedModsLabel.getX(),
      modListPaneScrollBar.getHeight() + modListPaneScrollBar.getY() + 6,
      displayedModsLabel.getWidth(),
      displayedModsLabel.getHeight()
    );

    viewingModsLabel.setBounds(
      viewingModsLabel.getX(),
      modListPaneScrollBar.getHeight() + modListPaneScrollBar.getY() + 6,
      viewingModsLabel.getWidth(),
      viewingModsLabel.getHeight()
    );

    modListPane.updateUI();
  }

  public static JPanel modListPanel;
  public static JPanel modListPane = new JPanel();
  public static JScrollPane modListPaneScrollBar = new JScrollPane();
  public static JLabel labelModCount;
  public static JButton addModButton;
  public static JButton refreshButton;
  public static JButton enableAllModsButton;
  public static JButton disableAllModsButton;
  public static JLabel labelRefreshing;
  public static JProgressBar refreshProgressBar = new JProgressBar();
  public static JTextField searchBox;
  public static JLabel displayedModsLabel = new JLabel();
  public static JLabel viewingModsLabel = new JLabel();

}
