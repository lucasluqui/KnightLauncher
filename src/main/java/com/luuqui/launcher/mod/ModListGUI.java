package com.luuqui.launcher.mod;

import com.google.inject.Inject;
import com.formdev.flatlaf.FlatClientProperties;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.GrayscaleFilter;
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
import org.apache.commons.text.WordUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import static com.luuqui.launcher.mod.Log.log;

public class ModListGUI extends BaseGUI
{
  @Inject public ModListEventHandler eventHandler;
  @Inject protected ModManager _modManager;
  @Inject protected LocaleManager _localeManager;
  protected String globalWarningMessage = "";

  @Inject
  public ModListGUI ()
  {
    super(385, 460, false);
  }

  public void init ()
  {
    setupImages();
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
    labelModCount.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    guiFrame.getContentPane().add(labelModCount);

    labelModCountText = new JLabel(_localeManager.getValue("m.mods_installed"));
    labelModCountText.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCountText.setBounds(26, 47, 188, 14);
    labelModCountText.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    guiFrame.getContentPane().add(labelModCountText);

    globalWarningButton = new JButton(_localeManager.getValue("m.warning"));
    globalWarningButton.setIcon(IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, 13, Color.BLACK));
    globalWarningButton.setBounds(650, 87, 110, 23);
    globalWarningButton.setToolTipText(_localeManager.getValue("m.warning"));
    globalWarningButton.setFocusPainted(false);
    globalWarningButton.setFocusable(false);
    globalWarningButton.setForeground(Color.BLACK);
    globalWarningButton.setBackground(CustomColors.WARNING);
    globalWarningButton.putClientProperty(FlatClientProperties.STYLE,
        "arc: 999; borderWidth: 0");
    globalWarningButton.setVisible(false);
    globalWarningButton.addActionListener(l -> {
      Dialog.push(globalWarningMessage, _localeManager.getValue("t.warning"), JOptionPane.ERROR_MESSAGE);
    });
    guiFrame.getContentPane().add(globalWarningButton);

    JPanel modStoreButtonPane = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(modStoreButtonImage, 0, 0, null);
      }
    };

    modStoreButtonPane.setLayout(null);
    modStoreButtonPane.setBounds(250, 5, 165, 55);
    modStoreButtonPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    modStoreButtonPane.setToolTipText(_localeManager.getValue("m.coming_soon"));

    JLabel modStoreButtonLabel = new JLabel();
    modStoreButtonLabel.setIcon(IconFontSwing.buildIcon(FontAwesome.SHOPPING_CART, 16, Color.WHITE));
    modStoreButtonLabel.setBounds(0, 0, 165, 55);
    modStoreButtonLabel.setText(_localeManager.getValue("b.mod_store"));
    modStoreButtonLabel.setFont(Fonts.getFont("defaultMedium", 16.0f, Font.PLAIN));
    modStoreButtonLabel.setHorizontalAlignment(SwingConstants.CENTER);
    modStoreButtonLabel.setVerticalAlignment(SwingConstants.CENTER);
    modStoreButtonPane.add(modStoreButtonLabel);

    modStoreButtonPane.addMouseListener(new MouseListener() {
      @Override public void mouseClicked(MouseEvent e) { /* this.eventHandler.openModStore(); */ }
      @Override public void mousePressed(MouseEvent e) { /* this.eventHandler.openModStore(); */ }
      @Override public void mouseReleased(MouseEvent e) { /* this.eventHandler.openModStore(); */ }
      @Override public void mouseEntered(MouseEvent e) {
        /*
        modStoreButtonPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        modStoreButtonImage = modStoreButtonImageFocused;
        modStoreButtonPane.repaint();
         */
      }
      @Override public void mouseExited(MouseEvent e) {
        /*
        modStoreButtonPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        modStoreButtonImage = modStoreButtonImageUnfocused;
        modStoreButtonPane.repaint();
         */
      }
    });
    guiFrame.add(modStoreButtonPane);

    JButton getModsButton = new JButton(_localeManager.getValue("b.get_mods"));
    getModsButton.setBounds(420, 5, 175, 25);
    getModsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    getModsButton.setFocusPainted(false);
    getModsButton.setFocusable(false);
    getModsButton.setForeground(Color.WHITE);
    getModsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    getModsButton.setToolTipText(_localeManager.getValue("b.get_mods"));
    getModsButton.putClientProperty(FlatClientProperties.STYLE,
        "arc: 999; borderWidth: 0");
    guiFrame.getContentPane().add(getModsButton);
    getModsButton.addActionListener(this.eventHandler::getModsEvent);

    JButton modFolderButton = new JButton(_localeManager.getValue("b.open_mods_dir"));
    modFolderButton.setBounds(600, 5, 175, 25);
    modFolderButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    modFolderButton.setFocusPainted(false);
    modFolderButton.setFocusable(false);
    modFolderButton.setForeground(Color.WHITE);
    modFolderButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    modFolderButton.setToolTipText(_localeManager.getValue("b.open_mods_dir"));
    modFolderButton.putClientProperty(FlatClientProperties.STYLE,
        "arc: 999; borderWidth: 0");
    guiFrame.getContentPane().add(modFolderButton);
    modFolderButton.addActionListener(this.eventHandler::openModsFolderEvent);

    enableAllModsButton = new JButton(_localeManager.getValue("b.enable_all_mods"));
    enableAllModsButton.setBounds(420, 35, 175, 25 );
    enableAllModsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    enableAllModsButton.setFocusPainted(false);
    enableAllModsButton.setFocusable(false);
    enableAllModsButton.setForeground(Color.WHITE);
    enableAllModsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    enableAllModsButton.setToolTipText(_localeManager.getValue("b.enable_all_mods"));
    enableAllModsButton.putClientProperty(FlatClientProperties.STYLE,
        "arc: 999; borderWidth: 0");
    guiFrame.getContentPane().add(enableAllModsButton);
    enableAllModsButton.addActionListener(this.eventHandler::enableAllModsEvent);

    disableAllModsButton = new JButton(_localeManager.getValue("b.disable_all_mods"));
    disableAllModsButton.setBounds(600, 35, 175, 25);
    disableAllModsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    disableAllModsButton.setFocusPainted(false);
    disableAllModsButton.setFocusable(false);
    disableAllModsButton.setForeground(Color.WHITE);
    disableAllModsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    disableAllModsButton.setToolTipText(_localeManager.getValue("b.disable_all_mods"));
    disableAllModsButton.putClientProperty(FlatClientProperties.STYLE,
        "arc: 999; borderWidth: 0");
    guiFrame.getContentPane().add(disableAllModsButton);
    disableAllModsButton.addActionListener(this.eventHandler::disableAllModsEvent);

    JSeparator separator = new JSeparator();
    separator.setBounds(25, 75, 750, 2);
    guiFrame.getContentPane().add(separator);

    addModButton = new JButton(_localeManager.getValue("b.add"));
    addModButton.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 13, Color.WHITE));
    addModButton.setBounds(25, 87, 80, 23);
    addModButton.setFocusPainted(false);
    addModButton.setFocusable(false);
    addModButton.setForeground(Color.WHITE);
    addModButton.setBackground(CustomColors.GREEN);
    addModButton.setToolTipText(_localeManager.getValue("m.add_mod_tooltip"));
    addModButton.putClientProperty(FlatClientProperties.STYLE,
        "arc: 999; borderWidth: 0");
    guiFrame.getContentPane().add(addModButton);
    addModButton.addActionListener(this.eventHandler::addModEvent);

    refreshButton = new JButton(_localeManager.getValue("b.refresh"));
    refreshButton.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, 13, Color.WHITE));
    refreshButton.setBounds(110, 87, 105, 23);
    refreshButton.setFocusPainted(false);
    refreshButton.setFocusable(false);
    refreshButton.setForeground(Color.WHITE);
    refreshButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    refreshButton.setToolTipText(_localeManager.getValue("m.refresh_tooltip"));
    refreshButton.putClientProperty(FlatClientProperties.STYLE,
        "arc: 999; borderWidth: 0");
    guiFrame.getContentPane().add(refreshButton);
    refreshButton.addActionListener(this.eventHandler::refreshEvent);

    refreshProgressBar = new JProgressBar();
    refreshProgressBar.setBounds(25, 445, 740, 2);
    refreshProgressBar.setStringPainted(false);
    refreshProgressBar.setVisible(false);
    guiFrame.getContentPane().add(refreshProgressBar);

    searchBox = new JTextField();
    searchBox.setBounds(250, 85, 300, 27);
    searchBox.setFont(Fonts.getFont("codeRegular", 11.0f, Font.ITALIC));
    searchBox.setBackground(CustomColors.INTERFACE_TEXTFIELD_BACKGROUND);
    searchBox.setForeground(Color.WHITE);
    searchBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, _localeManager.getValue("m.modlist_searchbox_placeholder"));
    searchBox.putClientProperty(FlatClientProperties.STYLE,
        "arc: 999; borderWidth: 0");
    guiFrame.getContentPane().add(searchBox);
    searchBox.addActionListener(l -> eventHandler.searchMod());
    searchBox.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) { eventHandler.searchMod(); }
    });

    modListPane = new JPanel();
    modListPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    modListPaneScrollBar = new JScrollPane(modListPane);
    modListPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    modListPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    modListPaneScrollBar.setBounds(25, 120, 750, Math.min(325, _modManager.getModCount() * 143));
    modListPaneScrollBar.setBorder(null);
    modListPaneScrollBar.getVerticalScrollBar().setUnitIncrement(20);
    guiFrame.getContentPane().add(modListPaneScrollBar);

    displayedModsLabel = new JLabel();
    displayedModsLabel.setBounds(25, 451, 300, 15);
    displayedModsLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    displayedModsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    displayedModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    guiFrame.getContentPane().add(displayedModsLabel);

    viewingModsLabel = new JLabel(_localeManager.getValue("m.viewing_mods", "Official"));
    viewingModsLabel.setBounds(25, 451, 740, 15);
    viewingModsLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    viewingModsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    viewingModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    guiFrame.getContentPane().add(viewingModsLabel);

    updateModList(null);
  }

  public void updateModList (String searchString)
  {
    int count = 0;

    if (modListPane == null) {
      log.warning("What? how? why?! modListPane is null");
      return;
    }

    modListPane.removeAll();

    BufferedImage defaultModImageBuffImg = ImageUtil.loadImageWithinJar("/rsrc/img/icon-default.png");
    defaultModImageBuffImg = ImageUtil.resizeImagePreserveTransparency(
        defaultModImageBuffImg, 128, 128);

    for (Mod mod : _modManager.getModList()) {

      if (searchString != null) {
        // mod name or author doesn't match the search string, sayonara.
        if (!mod.getDisplayName().toLowerCase().contains(searchString.toLowerCase())
            && !mod.getAuthor().toLowerCase().contains(searchString.toLowerCase())) continue;
      }

      JPanel modPaneBackgroundLeft = new JPanel();
      modPaneBackgroundLeft.setLayout(null);
      modPaneBackgroundLeft.setBounds(0, count * 143, 715, 138);
      modPaneBackgroundLeft.putClientProperty(FlatClientProperties.STYLE, "border: 0,0,0,0; arc:25");
      modPaneBackgroundLeft.setBackground(CustomColors.INTERFACE_MODLIST_BADGE_RESOURCE_BACKGROUND);

      JPanel modPane = new JPanel();
      modPane.setLayout(null);
      modPane.setBounds(10, count * 143, 715, 138);
      modPane.setBackground(CustomColors.INTERFACE_MODLIST_BACKGROUND_LIGHT);

      JPanel modPaneBackgroundRight = new JPanel();
      modPaneBackgroundRight.setLayout(null);
      modPaneBackgroundRight.setBounds(20, count * 143, 715, 138);
      modPaneBackgroundRight.putClientProperty(FlatClientProperties.STYLE, "border: 0,0,0,0; arc:25");
      modPaneBackgroundRight.setBackground(CustomColors.INTERFACE_MODLIST_BACKGROUND_LIGHT);

      JLabel modImage = new JLabel();
      BufferedImage modImageBuffImg = null;
      if (mod.getImage() != null) {
        modImageBuffImg = ImageUtil.loadImageFromBase64(mod.getImage());
        modImageBuffImg = ImageUtil.resizeImagePreserveTransparency(modImageBuffImg, 128, 128);
      }
      modImage.setIcon(new ImageIcon(ImageUtil.addRoundedCorners(
          modImageBuffImg == null ? defaultModImageBuffImg : modImageBuffImg, 25)));
      modImage.setBounds(5, 5, 128, 128);
      modPane.add(modImage);

      JLabel modName = new JLabel();
      modName.setText(mod.getDisplayName());
      modName.setToolTipText(mod.getDisplayName());
      modName.setFont(Fonts.getFont("defaultMedium", 16.0f, Font.PLAIN));
      modName.setBounds(143, 8, 350, 25);
      modPane.add(modName);

      JTextArea modDescription = new JTextArea();
      modDescription.setBounds(137, 30, 455, 55);
      modDescription.setText(WordUtils.wrap(mod.getDescription(), 70));
      modDescription.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
      modDescription.setEditable(false);
      modDescription.setEnabled(false);
      modDescription.setBackground(CustomColors.INTERFACE_MODLIST_BACKGROUND_LIGHT);
      modPane.add(modDescription);

      JLabel modFooter = new JLabel();
      modFooter.setText(_localeManager.getValue("m.mod_footer", new String[]{mod.getVersion(), mod.getAuthor()}));
      modFooter.setFont(Fonts.getFont("defaultRegular", 9.0f, Font.ITALIC));
      modFooter.setBounds(253, 107, 250, 25);
      modPane.add(modFooter);

      JLabel modBadge = new JLabel();
      modBadge.setBounds(143, 110, 86, 18);
      modBadge.setHorizontalAlignment(SwingConstants.CENTER);
      modBadge.setFont(Fonts.getFont("defaultRegular", 9.0f, Font.ITALIC));
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
          modPaneBackgroundLeft.setBackground(CustomColors.INTERFACE_MODLIST_BADGE_CLASS_BACKGROUND);
        }
      } else if (mod instanceof JarMod) {
        modBadge.setText(_localeManager.getValue("m.code_mod"));
        modBadge.putClientProperty(FlatClientProperties.STYLE,
            "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND)
                + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_FOREGROUND)
                + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND));
        modPaneBackgroundLeft.setBackground(CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND);
      } else if (mod instanceof Modpack) {
        modBadge.setText(_localeManager.getValue("m.modpack"));
        modBadge.putClientProperty(FlatClientProperties.STYLE,
            "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_PACK_BACKGROUND)
                + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_PACK_FOREGROUND)
                + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_MODLIST_BADGE_PACK_BACKGROUND));
        modPaneBackgroundLeft.setBackground(CustomColors.INTERFACE_MODLIST_BADGE_PACK_BACKGROUND);
      }
      modBadge.setVisible(true);
      modBadge.setToolTipText(modBadge.getText());
      modPane.add(modBadge);

      JLabel enabledCheckboxLabel = new JLabel(_localeManager.getValue("m.enabled"));
      enabledCheckboxLabel.setBounds(495, 105, 75, 25);
      enabledCheckboxLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
      enabledCheckboxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      modPane.add(enabledCheckboxLabel);

      JCheckBox enabledCheckbox = new JCheckBox();
      enabledCheckbox.setBounds(575, 105, 25, 25);
      enabledCheckbox.setEnabled(true);
      enabledCheckbox.setVisible(true);
      enabledCheckbox.setFocusable(false);
      enabledCheckbox.setFocusPainted(false);
      enabledCheckbox.setSelected(mod.isEnabled());

      enabledCheckbox.addActionListener(l -> {
        if (enabledCheckbox.isSelected()) {
          eventHandler.enableMod(mod);
        } else {
          eventHandler.disableMod(mod);
        }
      });

      modPane.add(enabledCheckbox);

      JButton removeButton = new JButton(_localeManager.getValue("b.remove"));
      removeButton.setIcon(IconFontSwing.buildIcon(FontAwesome.TRASH, 13, Color.WHITE));
      removeButton.setBounds(605, 105, 110, 23);
      removeButton.setFocusPainted(false);
      removeButton.setFocusable(false);
      removeButton.setForeground(Color.WHITE);
      removeButton.setBackground(CustomColors.DANGER);
      removeButton.setToolTipText(_localeManager.getValue("m.remove_mod", mod.getDisplayName()));
      removeButton.putClientProperty(FlatClientProperties.STYLE,
          "arc: 999; borderWidth: 0");
      removeButton.addActionListener(l -> eventHandler.removeModEvent(mod));
      modPane.add(removeButton);

      JButton warningButton = new JButton(_localeManager.getValue("m.warning"));
      warningButton.setIcon(IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, 13, Color.BLACK));
      warningButton.setBounds(605, 75, 110, 23);
      warningButton.setToolTipText(_localeManager.getValue("m.warning"));
      warningButton.setFocusPainted(false);
      warningButton.setFocusable(false);
      warningButton.setForeground(Color.BLACK);
      warningButton.setBackground(CustomColors.WARNING);
      warningButton.putClientProperty(FlatClientProperties.STYLE,
          "arc: 999; borderWidth: 0");
      warningButton.setVisible(mod.showWarningMessage());
      warningButton.addActionListener(l -> eventHandler.showWarningEvent(mod));
      modPane.add(warningButton);

      modListPane.add(modPane);
      modListPane.add(modPaneBackgroundLeft);
      modListPane.add(modPaneBackgroundRight);
      modListPane.setComponentZOrder(modPane, 0);
      modListPane.setComponentZOrder(modPaneBackgroundLeft, 1);
      modListPane.setComponentZOrder(modPaneBackgroundRight, 2);
      count++;
    }

    modListPane.setLayout(null);

    modListPane.setPreferredSize(new Dimension(750, count * 143));

    modListPaneScrollBar.setBounds(
      modListPaneScrollBar.getX(),
      modListPaneScrollBar.getY(),
      modListPaneScrollBar.getWidth(),
      Math.min(325, count * 143)
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

    // Put the scroll bar back to the top after updating.
    SwingUtilities.invokeLater(() -> modListPaneScrollBar.getViewport().setViewPosition(new Point(0, 0)));
  }

  private void setupImages ()
  {
    modStoreButtonImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/mod-store-btn.png"), 165, 55);
    modStoreButtonImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(5f).filter(modStoreButtonImageFocused, null), 25);
    modStoreButtonImageUnfocused = new GrayscaleFilter().filter(modStoreButtonImageFocused, null);
    modStoreButtonImage = modStoreButtonImageUnfocused;
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
  public JButton globalWarningButton = new JButton();
  public BufferedImage modStoreButtonImage = null;
  public BufferedImage modStoreButtonImageFocused = null;
  public BufferedImage modStoreButtonImageUnfocused = null;

}
