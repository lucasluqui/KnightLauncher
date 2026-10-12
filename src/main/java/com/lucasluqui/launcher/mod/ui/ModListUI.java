package com.lucasluqui.launcher.mod.ui;

import com.lucasluqui.swing.Scale;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.google.inject.Inject;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.launcher.ui.BaseUI;
import com.lucasluqui.launcher.mod.ui.handler.ModListEventHandler;
import com.lucasluqui.launcher.mod.ModManager;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.mod.data.JarMod;
import com.lucasluqui.launcher.mod.data.Mod;
import com.lucasluqui.launcher.mod.data.Modpack;
import com.lucasluqui.launcher.mod.data.ZipMod;
import com.lucasluqui.swing.SlimProgressBarUI;
import com.lucasluqui.swing.SmoothProgressBar;
import com.lucasluqui.swing.SmoothScrollPane;
import com.lucasluqui.util.ColorUtil;
import com.lucasluqui.util.ImageUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static com.lucasluqui.launcher.mod.ui.Log.log;

public class ModListUI extends BaseUI
{
  @Inject
  public ModListUI ()
  {
    super(385, 460, false);
  }

  public void init ()
  {
    super.init();
    setupImages();
    compose();
    initFinished();
  }

  public void initFinished ()
  {
    super.initFinished();
  }

  @Override
  public void loadOnline ()
  {

  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.mods"));
    guiFrame.setBounds(Scale.rect(100, 100, this.width, this.height));
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.getContentPane().setLayout(null);
    panel = (JPanel) guiFrame.getContentPane();

    labelModCount = new JLabel();
    labelModCount.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCount.setBounds(Scale.rect(25, 2, 188, 40));
    labelModCount.setFont(Fonts.getFont("default", 40.0f, Font.BOLD));
    guiFrame.getContentPane().add(labelModCount);

    labelModCountText = new JLabel(_localeManager.getValue("m.mods_installed"));
    labelModCountText.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCountText.setBounds(Scale.rect(26, 47, 188, 14));
    labelModCountText.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    guiFrame.getContentPane().add(labelModCountText);

    globalWarningButton = new JButton(_localeManager.getValue("m.warning"));
    globalWarningButton.setIcon(IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, Scale.px(13), Color.BLACK));
    globalWarningButton.setBounds(Scale.rect(650, 87, 110, 23));
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

    JPanel modStoreButtonPane = new JPanel()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        super.paintComponent(g);
        g.drawImage(modStoreButtonImage, 0, 0, null);
      }
    };

    modStoreButtonPane.setLayout(null);
    modStoreButtonPane.setBounds(Scale.rect(250, 5, 165, 55));
    modStoreButtonPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    modStoreButtonPane.setToolTipText(_localeManager.getValue("m.coming_soon"));

    JLabel modStoreButtonLabel = new JLabel();
    modStoreButtonLabel.setIcon(IconFontSwing.buildIcon(FontAwesome.SHOPPING_CART, Scale.px(16), Color.WHITE));
    modStoreButtonLabel.setBounds(Scale.rect(0, 0, 165, 55));
    modStoreButtonLabel.setText(_localeManager.getValue("b.mod_store"));
    modStoreButtonLabel.setFont(Fonts.getFont("default", 16.0f, Font.BOLD));
    modStoreButtonLabel.setHorizontalAlignment(SwingConstants.CENTER);
    modStoreButtonLabel.setVerticalAlignment(SwingConstants.CENTER);
    modStoreButtonPane.add(modStoreButtonLabel);

    modStoreButtonPane.addMouseListener(new MouseListener()
    {
      @Override
      public void mouseClicked (MouseEvent e)
      { /* this.eventHandler.openModStore(); */ }

      @Override
      public void mousePressed (MouseEvent e)
      { /* this.eventHandler.openModStore(); */ }

      @Override
      public void mouseReleased (MouseEvent e)
      { /* this.eventHandler.openModStore(); */ }

      @Override
      public void mouseEntered (MouseEvent e)
      {
        /*
        modStoreButtonPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        modStoreButtonImage = modStoreButtonImageFocused;
        modStoreButtonPane.repaint();
         */
      }

      @Override
      public void mouseExited (MouseEvent e)
      {
        /*
        modStoreButtonPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        modStoreButtonImage = modStoreButtonImageUnfocused;
        modStoreButtonPane.repaint();
         */
      }
    });
    guiFrame.add(modStoreButtonPane);

    JButton getModsButton = new JButton(_localeManager.getValue("b.get_mods"));
    getModsButton.setBounds(Scale.rect(420, 5, 175, 25));
    getModsButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
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
    modFolderButton.setBounds(Scale.rect(600, 5, 175, 25));
    modFolderButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
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
    enableAllModsButton.setBounds(Scale.rect(420, 35, 175, 25));
    enableAllModsButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
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
    disableAllModsButton.setBounds(Scale.rect(600, 35, 175, 25));
    disableAllModsButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
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
    separator.setBounds(Scale.rect(25, 75, 750, 2));
    guiFrame.getContentPane().add(separator);

    addModButton = new JButton(_localeManager.getValue("b.add"));
    addModButton.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, Scale.px(13), Color.WHITE));
    addModButton.setBounds(Scale.rect(25, 87, 80, 23));
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
    refreshButton.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, Scale.px(13), Color.WHITE));
    refreshButton.setBounds(Scale.rect(110, 87, 105, 23));
    refreshButton.setFocusPainted(false);
    refreshButton.setFocusable(false);
    refreshButton.setForeground(Color.WHITE);
    refreshButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    refreshButton.setToolTipText(_localeManager.getValue("m.refresh_tooltip"));
    refreshButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    guiFrame.getContentPane().add(refreshButton);
    refreshButton.addActionListener(this.eventHandler::refreshEvent);

    // A thin line along the bottom of the list. Bounds include the room the bar's glow needs around it.
    refreshProgressBar = new SmoothProgressBar();
    refreshProgressBar.setUI(new SlimProgressBarUI(CustomColors.LAUNCH, 3f));
    refreshProgressBar.setOpaque(false);
    refreshProgressBar.setBorder(null);
    refreshProgressBar.setBounds(Scale.rect(25 - SlimProgressBarUI.GLOW_MARGIN, 446 - 5, 740 + SlimProgressBarUI.GLOW_MARGIN * 2, 10));
    refreshProgressBar.setShouldPaintString(false);
    refreshProgressBar.setVisible(false);
    guiFrame.getContentPane().add(refreshProgressBar);

    searchBox = new JTextField();
    searchBox.setBounds(Scale.rect(250, 85, 300, 27));
    searchBox.setFont(Fonts.getFont("code", 11.0f, Font.ITALIC));
    searchBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, _localeManager.getValue("m.modlist_searchbox_placeholder"));
    searchBox.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    searchBox.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
      new FlatSearchIcon());
    guiFrame.getContentPane().add(searchBox);
    searchBox.addActionListener(l -> eventHandler.searchMod());
    searchBox.addKeyListener(new KeyAdapter()
    {
      public void keyReleased (KeyEvent e)
      {
        eventHandler.searchMod();
      }
    });

    modListPane = new JPanel();
    modListPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    modListPaneScrollBar = new SmoothScrollPane(modListPane);
    modListPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    modListPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    modListPaneScrollBar.setBounds(Scale.rect(25, 120, 750, Math.min(MOD_LIST_MAX_HEIGHT, _modManager.getModCount() * CARD_STRIDE)));
    modListPaneScrollBar.setBorder(null);
    modListPaneScrollBar.getVerticalScrollBar().setUnitIncrement(Scale.px(20));
    guiFrame.getContentPane().add(modListPaneScrollBar);

    displayedModsLabel = new JLabel();
    displayedModsLabel.setBounds(Scale.rect(25, 451, 300, 15));
    displayedModsLabel.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    displayedModsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    displayedModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    guiFrame.getContentPane().add(displayedModsLabel);

    viewingModsLabel = new JLabel(_localeManager.getValue("m.viewing_mods", "Official"));
    viewingModsLabel.setBounds(Scale.rect(25, 451, 740, 15));
    viewingModsLabel.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    viewingModsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    viewingModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    guiFrame.getContentPane().add(viewingModsLabel);

    setupModDropping();

    updateModList(null);
  }

  /**
   * Lets files be dragged and dropped anywhere on this screen to add them as mods, same as the add button.
   */
  private void setupModDropping ()
  {
    dropOverlay = new JComponent()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        // Only shown while files are being dragged over.
        if (!draggingFiles) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // The screen can be taller than the window showing it, so stick to the part that's actually visible.
        Rectangle area = getVisibleRect();

        Color background = CustomColors.INTERFACE_MAINPANE_BACKGROUND;
        g2d.setColor(new Color(background.getRed(), background.getGreen(), background.getBlue(), 220));
        g2d.fillRect(area.x, area.y, area.width, area.height);

        // Dashed outline around the drop area.
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(Scale.px(2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{ Scale.px(10f), Scale.px(8f) }, 0f));
        g2d.drawRoundRect(area.x + Scale.px(20), area.y + Scale.px(20), area.width - Scale.px(40), area.height - Scale.px(40), Scale.px(30), Scale.px(30));

        Icon icon = IconFontSwing.buildIcon(FontAwesome.PLUS_CIRCLE, Scale.px(48), Color.WHITE);
        String text = _localeManager.getValue("m.drop_mods");
        g2d.setFont(Fonts.getFont("default", 16.0f, Font.BOLD));
        FontMetrics metrics = g2d.getFontMetrics();
        int top = area.y + (area.height - icon.getIconHeight() - Scale.px(16) - metrics.getHeight()) / 2;
        icon.paintIcon(this, g2d, area.x + (area.width - icon.getIconWidth()) / 2, top);
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, area.x + (area.width - metrics.stringWidth(text)) / 2, top + icon.getIconHeight() + Scale.px(16) + metrics.getAscent());

        g2d.dispose();
      }
    };
    // The overlay always covers the whole screen and owns the drop target, otherwise components with their
    // own drop target (like the search box) would take over the drag while hovering them. It has no mouse
    // listeners, so clicks and scrolling still go through to whatever is below it.
    panel.add(dropOverlay);
    panel.setComponentZOrder(dropOverlay, 0);
    dropOverlay.setBounds(0, 0, panel.getWidth(), panel.getHeight());
    panel.addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized (ComponentEvent e)
      {
        dropOverlay.setBounds(0, 0, panel.getWidth(), panel.getHeight());
      }
    });

    new DropTarget(dropOverlay, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter()
    {
      @Override
      public void dragEnter (DropTargetDragEvent event)
      {
        if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || !eventHandler.canAddMods()) {
          event.rejectDrag();
          return;
        }
        draggingFiles = true;
        dropOverlay.repaint();
      }

      @Override
      public void dragExit (DropTargetEvent event)
      {
        draggingFiles = false;
        dropOverlay.repaint();
      }

      @Override
      @SuppressWarnings("unchecked")
      public void drop (DropTargetDropEvent event)
      {
        draggingFiles = false;
        dropOverlay.repaint();

        if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || !eventHandler.canAddMods()) {
          event.rejectDrop();
          return;
        }

        try {
          event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
          List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
          event.dropComplete(true);

          // Off the drop itself, so the app the files were dragged from isn't kept waiting on us or a dialog.
          new Thread(() -> eventHandler.addModFiles(files)).start();
        } catch (Exception e) {
          log.error("Failed to receive dropped files", e);
          event.dropComplete(false);
        }
      }
    });
  }

  public void selectedServerChanged ()
  {
    this.eventHandler.selectedServerChanged();
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
      defaultModImageBuffImg, Scale.px(128), Scale.px(128));

    for (Mod mod : _modManager.getModList()) {

      if (searchString != null) {
        // mod name or author doesn't match the search string, sayonara.
        if (!mod.getDisplayName().toLowerCase().contains(searchString.toLowerCase())
          && !mod.getAuthor().toLowerCase().contains(searchString.toLowerCase())) continue;
      }

      ModCard modCard = buildModCard(mod, defaultModImageBuffImg);
      modCard.setBounds(Scale.rect(0, count * CARD_STRIDE, CARD_WIDTH, ModCard.HEIGHT));
      modListPane.add(modCard);
      count++;
    }

    modListPane.setLayout(null);

    int listHeight = Math.max(0, count * CARD_STRIDE - CARD_GAP);
    modListPane.setPreferredSize(Scale.dim(750, listHeight));

    modListPaneScrollBar.setBounds(
      modListPaneScrollBar.getX(),
      modListPaneScrollBar.getY(),
      modListPaneScrollBar.getWidth(),
      Scale.px(Math.min(MOD_LIST_MAX_HEIGHT, listHeight))
    );

    displayedModsLabel.setText(_localeManager.getValue("m.modlist_footer", new String[]{String.valueOf(count), String.valueOf(_modManager.getModCount())}));
    displayedModsLabel.setBounds(
      displayedModsLabel.getX(),
      modListPaneScrollBar.getHeight() + modListPaneScrollBar.getY() + Scale.px(6),
      displayedModsLabel.getWidth(),
      displayedModsLabel.getHeight()
    );

    viewingModsLabel.setBounds(
      viewingModsLabel.getX(),
      modListPaneScrollBar.getHeight() + modListPaneScrollBar.getY() + Scale.px(6),
      viewingModsLabel.getWidth(),
      viewingModsLabel.getHeight()
    );

    modListPane.updateUI();

    // Put the scroll bar back to the top after updating.
    SwingUtilities.invokeLater(() -> modListPaneScrollBar.getViewport().setViewPosition(new Point(0, 0)));
  }

  private ModCard buildModCard (Mod mod, BufferedImage defaultModImageBuffImg)
  {
    // Badge text and colors depend on the mod type.
    String badgeText = null;
    Color accent = CustomColors.INTERFACE_MODLIST_BADGE_RESOURCE_BACKGROUND;
    Color badgeForeground = CustomColors.INTERFACE_MODLIST_BADGE_RESOURCE_FOREGROUND;
    boolean hasConfigFolder = false;
    if (mod instanceof ZipMod) {
      ZipMod zipMod = (ZipMod) mod;
      if (zipMod.getType() == null) {
        badgeText = _localeManager.getValue("m.resource_mod");
      } else if (zipMod.isClassType()) {
        badgeText = _localeManager.getValue("m.class_mod");
        accent = CustomColors.INTERFACE_MODLIST_BADGE_CLASS_BACKGROUND;
        badgeForeground = CustomColors.INTERFACE_MODLIST_BADGE_CLASS_FOREGROUND;
        hasConfigFolder = true;
      }
    } else if (mod instanceof JarMod) {
      badgeText = _localeManager.getValue("m.code_mod");
      accent = CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND;
      badgeForeground = CustomColors.INTERFACE_MODLIST_BADGE_CODE_FOREGROUND;
    } else if (mod instanceof Modpack) {
      badgeText = _localeManager.getValue("m.modpack");
      accent = CustomColors.INTERFACE_MODLIST_BADGE_PACK_BACKGROUND;
      badgeForeground = CustomColors.INTERFACE_MODLIST_BADGE_PACK_FOREGROUND;
    }

    BufferedImage modImageBuffImg = null;
    if (mod.getImage() != null) {
      modImageBuffImg = ImageUtil.loadImageFromBase64(mod.getImage());
      modImageBuffImg = ImageUtil.resizeImagePreserveTransparency(modImageBuffImg, Scale.px(128), Scale.px(128));
    }

    ModCard modCard = new ModCard(mod.getDisplayName(), badgeText, accent, badgeForeground, mod.getDescription(),
      _localeManager.getValue("m.mod_footer", new String[]{mod.getVersion(), mod.getAuthor()}),
      modImageBuffImg == null ? defaultModImageBuffImg : modImageBuffImg);
    modCard.setDimmed(!mod.isEnabled());

    JCheckBox enabledCheckbox = new JCheckBox(_localeManager.getValue("m.enabled"));
    enabledCheckbox.setFont(Fonts.getFont("default", 11.0f, Font.PLAIN));
    enabledCheckbox.setHorizontalTextPosition(SwingConstants.LEADING);
    enabledCheckbox.setOpaque(false);
    enabledCheckbox.setEnabled(mod.isPXCompatible());
    enabledCheckbox.setFocusable(false);
    enabledCheckbox.setFocusPainted(false);
    enabledCheckbox.setSelected(mod.isEnabled());
    enabledCheckbox.addActionListener(l -> {
      if (enabledCheckbox.isSelected()) {
        eventHandler.enableMod(mod);
      } else {
        eventHandler.disableMod(mod);
      }
      modCard.setDimmed(!enabledCheckbox.isSelected());
    });
    modCard.setToggle(enabledCheckbox);

    // Icon-only actions, from right to left.
    JButton removeButton = createCardActionButton(FontAwesome.TRASH, Color.WHITE, CustomColors.DANGER,
      _localeManager.getValue("m.remove_mod", mod.getDisplayName()));
    removeButton.addActionListener(l -> eventHandler.removeModEvent(mod));
    modCard.addAction(removeButton);

    if (mod.showWarningMessage()) {
      JButton warningButton = createCardActionButton(FontAwesome.EXCLAMATION_TRIANGLE, Color.BLACK, CustomColors.WARNING,
        _localeManager.getValue("m.warning"));
      warningButton.addActionListener(l -> eventHandler.showWarningEvent(mod));
      modCard.addAction(warningButton);
    }

    if (hasConfigFolder) {
      JButton configFolderButton = createCardActionButton(FontAwesome.COG, Color.WHITE,
        CustomColors.INTERFACE_BUTTON_BACKGROUND, _localeManager.getValue("b.open_mod_config_dir"));
      configFolderButton.addActionListener(l -> eventHandler.openModConfigFolderEvent(mod));
      modCard.addAction(configFolderButton);
    }

    return modCard;
  }

  private JButton createCardActionButton (FontAwesome icon, Color foreground, Color background, String tooltip)
  {
    JButton button = new JButton(IconFontSwing.buildIcon(icon, Scale.px(14), foreground));
    button.setFocusPainted(false);
    button.setFocusable(false);
    button.setBackground(background);
    button.setToolTipText(tooltip);
    button.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0; margin: 0,0,0,0");
    return button;
  }

  private void setupImages ()
  {
    modStoreButtonImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/mod-store-btn.png"), Scale.px(165), Scale.px(55));
    modStoreButtonImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(Scale.px(5f)).filter(modStoreButtonImageFocused, null), Scale.px(25));
    modStoreButtonImageUnfocused = new GrayscaleFilter().filter(modStoreButtonImageFocused, null);
    modStoreButtonImage = modStoreButtonImageUnfocused;
  }

  public void toggleElementsBlock (boolean block)
  {
    super.toggleElementsBlock(block);
    this.searchBox.setEnabled(!block);
  }

  @Inject public ModListEventHandler eventHandler;
  @Inject protected ModManager _modManager;
  @Inject protected LocaleManager _localeManager;

  public String globalWarningMessage = "";

  public JPanel modListPane = new JPanel();
  public SmoothScrollPane modListPaneScrollBar = new SmoothScrollPane();
  public JLabel labelModCount;
  public JButton addModButton;
  public JButton refreshButton;
  public JButton enableAllModsButton;
  public JButton disableAllModsButton;
  public SmoothProgressBar refreshProgressBar = new SmoothProgressBar();
  public JTextField searchBox;
  public JLabel displayedModsLabel = new JLabel();
  public JLabel viewingModsLabel = new JLabel();
  public JLabel labelModCountText;
  public JButton globalWarningButton = new JButton();
  public BufferedImage modStoreButtonImage = null;
  public BufferedImage modStoreButtonImageFocused = null;
  public BufferedImage modStoreButtonImageUnfocused = null;

  private static final int MOD_LIST_MAX_HEIGHT = 325;

  // Mod cards are stacked one after the other.
  private static final int CARD_WIDTH = 735;
  private static final int CARD_GAP = 8;
  private static final int CARD_STRIDE = ModCard.HEIGHT + CARD_GAP;

  // Covers the whole screen to receive dropped mod files, and shows a hint while they're dragged over it.
  private JComponent dropOverlay;
  private boolean draggingFiles = false;

}
