package com.luuqui.launcher.mod;

import com.google.inject.Inject;
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

public class ModListGUI extends BaseGUI
{
  @Inject public ModListEventHandler eventHandler;

  @Inject protected ModManager _modManager;
  @Inject protected LocaleManager _localeManager;

  protected String currentWarning = "";

  @Inject
  public ModListGUI ()
  {
    super(385, 460, false);
  }

  public void init ()
  {
    compose();
  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.mods"));
    guiFrame.setBounds(100, 100, this.width, this.height);
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.getContentPane().setLayout(null);
    modListPanel = (JPanel) guiFrame.getContentPane();

    labelModCount = new JLabel();
    labelModCount.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCount.setBounds(25, 2, 188, 40);
    labelModCount.setFont(Fonts.fontMedGiant);
    guiFrame.getContentPane().add(labelModCount);

    labelModCountText = new JLabel(_localeManager.getValue("m.mods_installed"));
    labelModCountText.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCountText.setBounds(26, 47, 188, 14);
    labelModCountText.setFont(Fonts.fontReg);
    guiFrame.getContentPane().add(labelModCountText);

    Icon warningNoticeIcon = IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, 16, Color.WHITE);
    warningNotice = new JButton(warningNoticeIcon);
    warningNotice.setBounds(125, 24, 36, 36);
    warningNotice.setToolTipText(_localeManager.getValue("m.warning_notice"));
    warningNotice.setFocusPainted(false);
    warningNotice.setFocusable(false);
    warningNotice.setBorderPainted(false);
    warningNotice.setForeground(Color.WHITE);
    warningNotice.setBackground(CustomColors.MID_RED);
    warningNotice.setFont(Fonts.fontMed);
    warningNotice.setVisible(false);
    warningNotice.addActionListener(l -> {
      Dialog.push(currentWarning, _localeManager.getValue("m.warning_notice"), JOptionPane.ERROR_MESSAGE);
    });
    guiFrame.getContentPane().add(warningNotice);


    Icon modStoreIcon = IconFontSwing.buildIcon(FontAwesome.SEARCH, 10, Color.WHITE);
    JButton modStoreButton = new JButton(_localeManager.getValue("b.mod_store"));
    modStoreButton.setIcon(modStoreIcon);
    modStoreButton.setBounds(240, 5, 175, 25);
    modStoreButton.setFont(Fonts.fontMed);
    modStoreButton.setFocusPainted(false);
    modStoreButton.setFocusable(false);
    modStoreButton.setEnabled(false);
    modStoreButton.setToolTipText(_localeManager.getValue("m.coming_soon"));
    guiFrame.getContentPane().add(modStoreButton);
    //getModsButton.addActionListener(this.eventHandler::openModStore);

    JButton getModsButton = new JButton(_localeManager.getValue("b.get_mods"));
    getModsButton.setBounds(420, 5, 175, 25);
    getModsButton.setFont(Fonts.fontMed);
    getModsButton.setFocusPainted(false);
    getModsButton.setFocusable(false);
    getModsButton.setToolTipText(_localeManager.getValue("b.get_mods"));
    guiFrame.getContentPane().add(getModsButton);
    getModsButton.addActionListener(this.eventHandler::getModsEvent);

    JButton modFolderButton = new JButton(_localeManager.getValue("b.open_mods_folder"));
    modFolderButton.setBounds(600, 5, 175, 25);
    modFolderButton.setFont(Fonts.fontMed);
    modFolderButton.setFocusPainted(false);
    modFolderButton.setFocusable(false);
    modFolderButton.setToolTipText(_localeManager.getValue("b.open_mods_folder"));
    guiFrame.getContentPane().add(modFolderButton);
    modFolderButton.addActionListener(this.eventHandler::openModsFolderEvent);

    enableAllModsButton = new JButton(_localeManager.getValue("b.enable_all_mods"));
    enableAllModsButton.setBounds(420, 35, 175, 25 );
    enableAllModsButton.setFont(Fonts.fontMed);
    enableAllModsButton.setFocusPainted(false);
    enableAllModsButton.setFocusable(false);
    enableAllModsButton.setToolTipText(_localeManager.getValue("b.enable_all_mods"));
    guiFrame.getContentPane().add(enableAllModsButton);
    enableAllModsButton.addActionListener(this.eventHandler::enableAllModsEvent);

    disableAllModsButton = new JButton(_localeManager.getValue("b.disable_all_mods"));
    disableAllModsButton.setBounds(600, 35, 175, 25);
    disableAllModsButton.setFont(Fonts.fontMed);
    disableAllModsButton.setFocusPainted(false);
    disableAllModsButton.setFocusable(false);
    disableAllModsButton.setToolTipText(_localeManager.getValue("b.disable_all_mods"));
    guiFrame.getContentPane().add(disableAllModsButton);
    disableAllModsButton.addActionListener(this.eventHandler::disableAllModsEvent);

    JSeparator separator = new JSeparator();
    separator.setBounds(25, 75, 750, 2);
    guiFrame.getContentPane().add(separator);

    addModButton = new JButton(IconFontSwing.buildIcon(FontAwesome.PLUS, 18, CustomColors.MID_GREEN));
    addModButton.setBounds(25, 86, 25, 25);
    addModButton.setFocusPainted(false);
    addModButton.setFocusable(false);
    addModButton.setBackground(null);
    addModButton.setBorder(null);
    addModButton.setToolTipText(_localeManager.getValue("b.add_mod"));
    guiFrame.getContentPane().add(addModButton);
    addModButton.addActionListener(this.eventHandler::addModEvent);

    refreshButton = new JButton(IconFontSwing.buildIcon(FontAwesome.REFRESH, 16, Color.WHITE));
    refreshButton.setBounds(55, 86, 25, 25);
    refreshButton.setFocusPainted(false);
    refreshButton.setFocusable(false);
    refreshButton.setBackground(null);
    refreshButton.setBorder(null);
    refreshButton.setToolTipText(_localeManager.getValue("b.refresh"));
    guiFrame.getContentPane().add(refreshButton);
    refreshButton.addActionListener(this.eventHandler::refreshEvent);

    refreshProgressBar = new JProgressBar();
    refreshProgressBar.setBounds(110, 90, 100, 16);
    refreshProgressBar.setStringPainted(true);
    refreshProgressBar.setVisible(false);
    guiFrame.getContentPane().add(refreshProgressBar);

    searchBox = new JTextField();
    searchBox.setBounds(250, 85, 300, 27);
    searchBox.setFont(Fonts.fontCodeReg);
    searchBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, _localeManager.getValue("m.modlist_searchbox_placeholder"));
    guiFrame.getContentPane().add(searchBox);
    searchBox.addActionListener(l -> eventHandler.searchMod());
    searchBox.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) { eventHandler.searchMod(); }
    });

    JButton searchButton = new JButton(_localeManager.getValue("m.search"));
    searchButton.setBounds(475, 85, 80, 26);
    guiFrame.getContentPane().add(searchButton);
    searchButton.addActionListener(l -> this.eventHandler.searchMod());
    searchButton.setVisible(false);

    JLabel enabledLabel = new JLabel(_localeManager.getValue("m.enabled"));
    enabledLabel.setBounds(636, 86, 100, 25);
    enabledLabel.setFont(Fonts.fontMed);
    guiFrame.getContentPane().add(enabledLabel);

    modListPane = new JPanel();
    modListPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    modListPaneScrollBar = new JScrollPane(modListPane);
    modListPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    modListPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    modListPaneScrollBar.setBounds(25, 120, 750, Math.min(325, _modManager.getModCount() * 76));
    modListPaneScrollBar.setBorder(null);
    modListPaneScrollBar.getVerticalScrollBar().setUnitIncrement(16);
    guiFrame.getContentPane().add(modListPaneScrollBar);

    displayedModsLabel = new JLabel();
    displayedModsLabel.setBounds(25, 451, 300, 15);
    displayedModsLabel.setFont(Fonts.fontReg);
    displayedModsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    displayedModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    guiFrame.getContentPane().add(displayedModsLabel);

    viewingModsLabel = new JLabel(_localeManager.getValue("m.viewing_mods", "Official"));
    viewingModsLabel.setBounds(25, 451, 740, 15);
    viewingModsLabel.setFont(Fonts.fontReg);
    viewingModsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    viewingModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    guiFrame.getContentPane().add(viewingModsLabel);

    updateModList(null);
  }

  public void updateModList (String searchString)
  {
    Color[] backgroundColors = { CustomColors.INTERFACE_MODLIST_BACKGROUND_LIGHT, CustomColors.INTERFACE_MODLIST_BACKGROUND_DARK };
    int count = 0;

    if (modListPane == null) {
      log.warning("What? how? why?! modListPane is null");
      return;
    }

    modListPane.removeAll();

    for (Mod mod : _modManager.getModList()) {

      if (searchString != null) {
        // mod name or author doesn't match the search string, sayonara.
        if (!mod.getDisplayName().toLowerCase().contains(searchString.toLowerCase())
          && !mod.getAuthor().toLowerCase().contains(searchString.toLowerCase())) continue;
      }

      JPanel modPane = new JPanel();
      modPane.setLayout(null);
      modPane.setBounds(0, 0, 750, 76);
      modPane.setBackground((count & 1) == 0 ? backgroundColors[0] : backgroundColors[1]);

      JLabel modImage = new JLabel();
      BufferedImage defaultImage = ImageUtil.loadImageWithinJar("/rsrc/img/default-64.png");
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
      modFooter.setText(_localeManager.getValue("m.mod_footer", new String[] { mod.getVersion(), mod.getAuthor() }));
      modFooter.setFont(Fonts.fontRegSmall);
      modFooter.setBounds(81, 32, 150, 55);
      modPane.add(modFooter);

      JLabel modBadge = new JLabel();
      modBadge.setBounds(241, 50, 86, 18);
      modBadge.setHorizontalAlignment(SwingConstants.CENTER);
      modBadge.setFont(Fonts.fontRegSmall);
      if (mod instanceof ZipMod) {
        ZipMod zipMod = (ZipMod) mod;

        if (zipMod.getType() == null) {
          modBadge.setText(_localeManager.getValue("m.resource_mod"));
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
        } else if (zipMod.getType().equalsIgnoreCase("class")) {
          modBadge.setText(_localeManager.getValue("m.class_mod"));
          modBadge.putClientProperty(FlatClientProperties.STYLE,
              "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CLASS_BACKGROUND)
                  + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CLASS_FOREGROUND)
                  + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CLASS_BACKGROUND));
          modBadge.setBounds(
              modBadge.getX(),
              modBadge.getY(),
              modBadge.getWidth() + 19,
              modBadge.getHeight()
          );
        }
      } else if (mod instanceof JarMod) {
        modBadge.setText(_localeManager.getValue("m.code_mod"));
        modBadge.putClientProperty(FlatClientProperties.STYLE,
          "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND)
            + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_FOREGROUND)
            + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND));
      } else if (mod instanceof Modpack) {
        modBadge.setText(_localeManager.getValue("m.modpack"));
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
          eventHandler.enableMod(mod);
        } else {
          eventHandler.disableMod(mod);
        }
      });

      modPane.add(enabledCheckbox);

      JButton removeButton = new JButton(IconFontSwing.buildIcon(FontAwesome.TRASH, 18, CustomColors.BUTTON_FOREGROUND_DANGER));
      removeButton.setBounds(680, 25, 25, 25);
      removeButton.setFocusPainted(false);
      removeButton.setFocusable(false);
      removeButton.setBackground(null);
      removeButton.setBorder(null);
      removeButton.setToolTipText(_localeManager.getValue("b.remove_mod", mod.getDisplayName()));
      removeButton.addActionListener(l -> eventHandler.removeModEvent(mod));

      modPane.add(removeButton);

      if (mod instanceof ZipMod) {
        ZipMod zipMod = (ZipMod) mod;
        if (zipMod.getType() != null && zipMod.getType().equalsIgnoreCase("class")) {
          boolean isPXCompatible = zipMod.isPXCompatible();

          if (!isPXCompatible) {
            JLabel incompatBadge = new JLabel();
            incompatBadge.setBounds(340, 50, 125, 18);
            incompatBadge.setHorizontalAlignment(SwingConstants.CENTER);
            incompatBadge.setFont(Fonts.fontRegSmall);
            incompatBadge.setText(_localeManager.getValue("m.mod_incompatible", "PX"));
            incompatBadge.setToolTipText(incompatBadge.getText());
            incompatBadge.setVisible(true);
            incompatBadge.putClientProperty(FlatClientProperties.STYLE,
                "background:" + ColorUtil.colorToHexString(CustomColors.BRIGHT_RED)
                    + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.MID_RED)
                    + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.BRIGHT_RED));
            modPane.add(incompatBadge);

            enabledCheckbox.setSelected(false);
            enabledCheckbox.setEnabled(false);
          }
        }
      }

      if (mod instanceof JarMod) {
        JarMod jarMod = (JarMod) mod;
        boolean isJDKCompatible = jarMod.isJDKCompatible();
        boolean isPXCompatible = jarMod.isPXCompatible();

        if (!isJDKCompatible || !isPXCompatible) {
          JLabel incompatBadge = new JLabel();
          incompatBadge.setBounds(340, 50, 125, 18);
          incompatBadge.setHorizontalAlignment(SwingConstants.CENTER);
          incompatBadge.setFont(Fonts.fontRegSmall);
          incompatBadge.setText(_localeManager.getValue("m.mod_incompatible", !isPXCompatible ? "PX" : "JDK"));
          incompatBadge.setToolTipText(incompatBadge.getText());
          incompatBadge.setVisible(true);
          incompatBadge.putClientProperty(FlatClientProperties.STYLE,
                  "background:" + ColorUtil.colorToHexString(CustomColors.BRIGHT_RED)
                          + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.MID_RED)
                          + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.BRIGHT_RED));
          modPane.add(incompatBadge);

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

    displayedModsLabel.setText(_localeManager.getValue("m.modlist_footer", new String[] { String.valueOf(count), String.valueOf(_modManager.getModCount()) }));
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

  public JPanel modListPanel;
  public JPanel modListPane = new JPanel();
  public JScrollPane modListPaneScrollBar = new JScrollPane();
  public JLabel labelModCount;
  public JButton addModButton;
  public JButton refreshButton;
  public JButton enableAllModsButton;
  public JButton disableAllModsButton;
  public JProgressBar refreshProgressBar = new JProgressBar();
  public JTextField searchBox;
  public JLabel displayedModsLabel = new JLabel();
  public JLabel viewingModsLabel = new JLabel();
  public JLabel labelModCountText;
  public JButton warningNotice = new JButton();

}
