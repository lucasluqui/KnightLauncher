package com.lucasluqui.launcher.mod.ui;

import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.launcher.ui.BaseUI;
import com.lucasluqui.launcher.mod.ui.handler.ModListEventHandler;
import com.lucasluqui.launcher.mod.ModManager;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.mod.data.JarMod;
import com.lucasluqui.launcher.mod.data.Mod;
import com.lucasluqui.launcher.mod.data.ModGroup;
import com.lucasluqui.launcher.mod.data.Modpack;
import com.lucasluqui.launcher.mod.data.ZipMod;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.swing.SlimProgressBarUI;
import com.lucasluqui.swing.SmoothProgressBar;
import com.lucasluqui.swing.SmoothScrollPane;
import com.lucasluqui.swing.FontIcon;
import com.lucasluqui.swing.HiDpi;
import com.lucasluqui.swing.FractionalMetrics;
import com.lucasluqui.swing.FractionalText;
import com.lucasluqui.swing.HandCursor;
import com.lucasluqui.swing.Scale;
import com.lucasluqui.util.ImageUtil;
import com.lucasluqui.util.ThreadingUtil;

import jiconfont.icons.font_awesome.FontAwesome;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.google.inject.Inject;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.GrayscaleFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // Everything on this screen, controls included, scrolls as one so the mod cards can use the whole
    // height once the controls are scrolled past. A floating search bar takes over from there.
    screenPane = new JPanel();
    screenPane.setLayout(null);
    screenPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    labelModCount = new JLabel();
    labelModCount.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCount.setBounds(Scale.rect(25, 2, 188, 40));
    labelModCount.setFont(Fonts.getFont("default", 40.0f, Font.BOLD));
    screenPane.add(labelModCount);

    labelModCountText = new JLabel(_localeManager.getValue("m.mods_installed"));
    labelModCountText.setHorizontalAlignment(SwingConstants.LEFT);
    labelModCountText.setBounds(Scale.rect(26, 47, 188, 14));
    labelModCountText.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    screenPane.add(labelModCountText);

    globalWarningButton = new JButton(_localeManager.getValue("m.warning"));
    globalWarningButton.setIcon(FontIcon.of(FontAwesome.EXCLAMATION_TRIANGLE, Scale.px(13), Color.BLACK));
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
    screenPane.add(globalWarningButton);

    JPanel modStoreButtonPane = new JPanel()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        super.paintComponent(g);
        // The image is made at the display's resolution, so it's fit into the button's size.
        HiDpi.drawBuffer(g, modStoreButtonImage, 0, 0, Scale.px(165), Scale.px(55));
      }
    };

    modStoreButtonPane.setLayout(null);
    modStoreButtonPane.setBounds(Scale.rect(250, 5, 165, 55));
    modStoreButtonPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    modStoreButtonPane.setToolTipText(_localeManager.getValue("m.coming_soon"));

    JLabel modStoreButtonLabel = new JLabel();
    modStoreButtonLabel.setIcon(FontIcon.of(FontAwesome.SHOPPING_CART, Scale.px(16), Color.WHITE));
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
    screenPane.add(modStoreButtonPane);

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
    screenPane.add(getModsButton);
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
    screenPane.add(modFolderButton);
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
    screenPane.add(enableAllModsButton);
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
    screenPane.add(disableAllModsButton);
    disableAllModsButton.addActionListener(this.eventHandler::disableAllModsEvent);

    JSeparator separator = new JSeparator();
    separator.setBounds(Scale.rect(25, 75, 750, 2));
    screenPane.add(separator);

    addModButton = new JButton(_localeManager.getValue("b.add"));
    addModButton.setIcon(FontIcon.of(FontAwesome.PLUS, Scale.px(13), Color.WHITE));
    addModButton.setBounds(Scale.rect(25, 87, 80, 23));
    addModButton.setFocusPainted(false);
    addModButton.setFocusable(false);
    addModButton.setForeground(Color.WHITE);
    addModButton.setBackground(CustomColors.GREEN);
    addModButton.setToolTipText(_localeManager.getValue("m.add_mod_tooltip"));
    addModButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    screenPane.add(addModButton);
    addModButton.addActionListener(this.eventHandler::addModEvent);

    refreshButton = new JButton(_localeManager.getValue("b.refresh"));
    refreshButton.setIcon(FontIcon.of(FontAwesome.REFRESH, Scale.px(13), Color.WHITE));
    refreshButton.setBounds(Scale.rect(110, 87, 105, 23));
    refreshButton.setFocusPainted(false);
    refreshButton.setFocusable(false);
    refreshButton.setForeground(Color.WHITE);
    refreshButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    refreshButton.setToolTipText(_localeManager.getValue("m.refresh_tooltip"));
    refreshButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    screenPane.add(refreshButton);
    refreshButton.addActionListener(this.eventHandler::refreshEvent);

    searchBox = new FractionalText.Field();
    searchBox.setBounds(Scale.rect(250, 85, 300, 27));
    searchBox.setFont(Fonts.getFont("code", 12.0f, Font.ITALIC));
    searchBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, _localeManager.getValue("m.modlist_searchbox_placeholder"));
    searchBox.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    searchBox.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
      new FlatSearchIcon());
    screenPane.add(searchBox);
    searchBox.addActionListener(l -> eventHandler.searchMod());
    searchBox.addKeyListener(new KeyAdapter()
    {
      public void keyReleased (KeyEvent e)
      {
        eventHandler.searchMod();
      }
    });

    modListPane = new JPanel();
    modListPane.setLayout(null);
    modListPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    modListPane.setBounds(Scale.rect(25, LIST_Y, CARD_WIDTH, 0));
    screenPane.add(modListPane);

    displayedModsLabel = new JLabel();
    displayedModsLabel.setBounds(Scale.rect(25, 451, 300, 15));
    displayedModsLabel.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    displayedModsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    displayedModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    screenPane.add(displayedModsLabel);

    viewingModsLabel = new JLabel(_localeManager.getValue("m.viewing_mods", "Official"));
    viewingModsLabel.setBounds(Scale.rect(25, 451, 740, 15));
    viewingModsLabel.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    viewingModsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    viewingModsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    screenPane.add(viewingModsLabel);

    setupScreenScrolling();
    setupModDropping();

    dragTimer = new Timer(15, _ -> stepDrag());

    updateModList(null);
  }

  /**
   * Puts the whole screen in a scroll pane, with a floating search bar and a back to top button over it that
   * show up once the controls at the top have been scrolled out of view.
   */
  private void setupScreenScrolling ()
  {
    screenScrollPane = new SmoothScrollPane(screenPane);
    screenScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    screenScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    screenScrollPane.setBorder(null);
    screenScrollPane.getViewport().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    screenScrollPane.getVerticalScrollBar().setUnitIncrement(Scale.px(20));
    screenScrollPane.setBounds(Scale.rect(0, 0, SCREEN_WIDTH, VIEW_HEIGHT));

    floatingBar = new JPanel()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2d.dispose();
      }
    };
    floatingBar.setLayout(null);
    floatingBar.setOpaque(false);
    floatingBar.setBounds(Scale.rect((SCREEN_WIDTH - FLOATING_BAR_WIDTH) / 2, 8, FLOATING_BAR_WIDTH, 39));
    floatingBar.setVisible(false);
    // Wheel scrolling over the bar still scrolls the list underneath.
    floatingBar.addMouseWheelListener(e ->
      screenScrollPane.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, screenScrollPane)));

    // Shares its text with the regular search box, so whatever's typed shows up in both.
    floatingSearchBox = new FractionalText.Field();
    floatingSearchBox.setDocument(searchBox.getDocument());
    floatingSearchBox.setBounds(Scale.rect(6, 6, 300, 27));
    floatingSearchBox.setFont(searchBox.getFont());
    floatingSearchBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, _localeManager.getValue("m.modlist_searchbox_placeholder"));
    floatingSearchBox.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    floatingSearchBox.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON,
      new FlatSearchIcon());
    floatingSearchBox.addActionListener(l -> eventHandler.searchMod());
    floatingSearchBox.addKeyListener(new KeyAdapter()
    {
      public void keyReleased (KeyEvent e)
      {
        eventHandler.searchMod();
      }
    });
    floatingBar.add(floatingSearchBox);

    scrollToTopButton = new JButton(FontIcon.centered(FontAwesome.ARROW_UP, Scale.px(12), Color.WHITE));
    scrollToTopButton.setBounds(Scale.rect(312, 6, 27, 27));
    scrollToTopButton.setFocusPainted(false);
    scrollToTopButton.setFocusable(false);
    scrollToTopButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    scrollToTopButton.setToolTipText(_localeManager.getValue("m.scroll_to_top"));
    scrollToTopButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0; margin: 0,0,0,0");
    scrollToTopButton.addActionListener(l -> screenScrollPane.smoothScrollTo(0));
    floatingBar.add(scrollToTopButton);

    // A thin line along the bottom of the view. Bounds include the room the bar's glow needs around it.
    refreshProgressBar = new SmoothProgressBar();
    refreshProgressBar.setUI(new SlimProgressBarUI(CustomColors.LAUNCH, 3f));
    refreshProgressBar.setOpaque(false);
    refreshProgressBar.setBorder(null);
    refreshProgressBar.setBounds(Scale.rect(25 - SlimProgressBarUI.GLOW_MARGIN, VIEW_HEIGHT - 12, 750 + SlimProgressBarUI.GLOW_MARGIN * 2, 10));
    refreshProgressBar.setShouldPaintString(false);
    refreshProgressBar.setVisible(false);

    // Holds the scroll pane and what floats over it. Children overlap, so it can't use optimized drawing,
    // otherwise repainting the list while scrolling would paint over the floating bar.
    JPanel screenHost = new JPanel()
    {
      @Override
      public boolean isOptimizedDrawingEnabled ()
      {
        return false;
      }
    };
    screenHost.setLayout(null);
    screenHost.setOpaque(false);
    screenHost.setBounds(Scale.rect(0, 0, SCREEN_WIDTH, VIEW_HEIGHT));
    screenHost.add(refreshProgressBar);
    screenHost.add(floatingBar);
    screenHost.add(screenScrollPane);
    panel.add(screenHost);

    screenScrollPane.getViewport().addChangeListener(e -> updateFloatingBar());
  }

  /**
   * Shows the floating bar once the regular search box has been scrolled out of view, and hides it again
   * when it comes back. Whichever search box was being typed in hands focus over to the other.
   */
  private void updateFloatingBar ()
  {
    boolean show = screenScrollPane.getViewport().getViewPosition().y >= floatingBarThreshold();
    if (show == floatingBar.isVisible()) return;

    floatingBar.setVisible(show);
    if (show) {
      // Ready to type into straight away, picking up after anything already typed.
      floatingSearchBox.requestFocusInWindow();
      floatingSearchBox.setCaretPosition(floatingSearchBox.getDocument().getLength());
    } else if (floatingSearchBox.isFocusOwner()) {
      searchBox.requestFocusInWindow();
    }
  }

  private int floatingBarThreshold ()
  {
    return searchBox.getY() + searchBox.getHeight();
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
        FractionalMetrics.apply(g2d);

        // The screen can be taller than the window showing it, so stick to the part that's actually visible.
        Rectangle area = getVisibleRect();

        Color background = CustomColors.INTERFACE_MAINPANE_BACKGROUND;
        g2d.setColor(new Color(background.getRed(), background.getGreen(), background.getBlue(), 220));
        g2d.fillRect(area.x, area.y, area.width, area.height);

        // Dashed outline around the drop area.
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(Scale.px(2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{ Scale.px(10f), Scale.px(8f) }, 0f));
        g2d.drawRoundRect(area.x + Scale.px(20), area.y + Scale.px(20), area.width - Scale.px(40), area.height - Scale.px(40), Scale.px(30), Scale.px(30));

        Icon icon = FontIcon.of(FontAwesome.PLUS_CIRCLE, Scale.px(48), Color.WHITE);
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
    HandCursor.passThrough(dropOverlay);
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
          ThreadingUtil.start("ModDrop", () -> eventHandler.addModFiles(files));
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
    // Mod checks and refreshes call this from their own threads, rebuilding the list has to happen on the EDT.
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> updateModList(searchString));
      return;
    }

    int count = 0;

    if (modListPane == null) {
      log.warning("What? how? why?! modListPane is null");
      return;
    }

    modListPane.removeAll();
    stopDrag();
    slots.clear();
    slotTops.clear();
    slotGroups.clear();
    cardMods.clear();
    cardY.clear();
    collisionButtons.clear();
    compactCards = Settings.compactModsList;

    BufferedImage defaultModImageBuffImg = ImageUtil.loadImageWithinJar("/rsrc/img/icon-default.png");
    defaultModImageBuffImg = ImageUtil.resizeImagePreserveTransparency(
      defaultModImageBuffImg, HiDpi.px(Scale.px(128)), HiDpi.px(Scale.px(128)));

    int position = 0;
    int y = 0;
    ModGroup group = null;
    ModGroupHeader header = null;
    for (Mod mod : _modManager.getModList()) {
      position++;

      if (searchString != null) {
        // mod name or author doesn't match the search string, sayonara.
        if (!mod.getDisplayName().toLowerCase().contains(searchString.toLowerCase())
          && !mod.getAuthor().toLowerCase().contains(searchString.toLowerCase())) continue;
      }

      // Display the mods ordered by their respective groups.
      if (ModGroup.of(mod) != group) {
        if (group != null) y += GROUP_GAP;
        group = ModGroup.of(mod);
        header = new ModGroupHeader(_localeManager.getValue(group.getTitleKey()), group.getColor());
        header.setBounds(Scale.rect(0, y, CARD_WIDTH, GROUP_HEADER_HEIGHT));
        modListPane.add(header);
        y += GROUP_HEADER_HEIGHT;
      }
      header.addMod();

      ModCard modCard = buildModCard(mod, defaultModImageBuffImg, position);
      modCard.setBounds(Scale.rect(0, y, CARD_WIDTH, ModCard.height(compactCards)));
      modListPane.add(modCard);
      slots.add(modCard);
      slotTops.add(y);
      slotGroups.add(group);
      cardMods.put(modCard, mod);
      cardY.put(modCard, (float) modCard.getY());
      y += cardStride();
      count++;
    }
    refreshCollisions();

    int listHeight = count == 0 ? 0 : y - cardGap();
    modListPane.setBounds(Scale.rect(25, LIST_Y, CARD_WIDTH, listHeight));

    int footerY = LIST_Y + listHeight + 6;
    displayedModsLabel.setText(_localeManager.getValue("m.modlist_footer", new String[]{String.valueOf(count), String.valueOf(_modManager.getModCount())}));
    displayedModsLabel.setBounds(Scale.rect(25, footerY, 300, 15));
    viewingModsLabel.setBounds(Scale.rect(25, footerY, 740, 15));

    // Sized to exactly what's shown, so there's only something to scroll when it doesn't all fit.
    screenPane.setPreferredSize(Scale.dim(SCREEN_WIDTH, footerY + 15 + 9));
    screenPane.revalidate();

    modListPane.revalidate();
    modListPane.repaint();

    // Back to the top after every update, searches included. If the search was typed in the floating bar,
    // it hides and the regular search box takes over the focus.
    if (screenScrollPane != null) {
      screenScrollPane.validate();
      screenScrollPane.scrollToImmediately(0);
    }
  }

  /**
   * @param position The mod's place in the list, starting at 1.
   */
  private ModCard buildModCard (Mod mod, BufferedImage defaultModImageBuffImg, int position)
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
      // Made at the display's resolution, the cards draw it into buffers that are too.
      modImageBuffImg = ImageUtil.resizeImagePreserveTransparency(modImageBuffImg, HiDpi.px(Scale.px(128)), HiDpi.px(Scale.px(128)));
    }

    // The groups already say what type each mod is, so the badge isn't shown.
    ModCard modCard = new ModCard(mod.getDisplayName(), SHOW_TYPE_BADGE ? badgeText : null, accent, badgeForeground, mod.getDescription(),
      _localeManager.getValue("m.mod_footer", new String[]{mod.getVersion(), mod.getAuthor()}),
      modImageBuffImg == null ? defaultModImageBuffImg : modImageBuffImg, compactCards);
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
      refreshCollisions();
    });
    modCard.setToggle(enabledCheckbox);

    // Grabbed to move the card around, showing where in the list it is.
    JLabel handle = new JLabel(String.valueOf(position), FontIcon.of(FontAwesome.BARS, Scale.px(12), new Color(150, 155, 170)), SwingConstants.CENTER);
    handle.setFont(Fonts.getFont("default", 13.0f, Font.BOLD));
    handle.setForeground(Color.WHITE);
    handle.setVerticalTextPosition(SwingConstants.TOP);
    handle.setHorizontalTextPosition(SwingConstants.CENTER);
    handle.setIconTextGap(Scale.px(6));
    handle.setEnabled(!isSearching());
    handle.setToolTipText(_localeManager.getValue(isSearching() ? "m.modlist_order_hint_search" : "m.modlist_order_hint"));
    modCard.setHandle(handle);
    installHandleDrag(modCard, handle);

    // Icon-only actions, from right to left.
    JButton removeButton = createCardActionButton(FontAwesome.TRASH, Color.WHITE, CustomColors.DANGER,
      _localeManager.getValue("m.remove_mod", mod.getDisplayName()));
    removeButton.addActionListener(l -> eventHandler.removeModEvent(mod));
    modCard.addAction(removeButton);

    JButton collisionButton = createCardActionButton(FontAwesome.FILES_O, Color.BLACK, CustomColors.WARNING, null);
    collisionButton.setVisible(false);
    collisionButton.addActionListener(l -> eventHandler.showConflictsEvent(mod));
    modCard.addAction(collisionButton);
    collisionButtons.put(mod, collisionButton);

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

  /**
   * Shows the conflict button on the cards of enabled mods sharing files with other enabled mods.
   */
  private void refreshCollisions ()
  {
    for (Map.Entry<Mod, JButton> entry : collisionButtons.entrySet()) {
      Map<Mod, List<String>> collisions = _modManager.getCollisions(entry.getKey());
      List<String> names = new ArrayList<>();
      for (Mod other : collisions.keySet()) names.add(other.getDisplayName());

      JButton button = entry.getValue();
      button.setVisible(!collisions.isEmpty());
      button.setToolTipText(_localeManager.getValue("m.mod_conflicts_tooltip", String.join(", ", names)));
      button.getParent().revalidate();
      button.getParent().repaint();
    }
  }

  private boolean isSearching ()
  {
    return searchBox != null && !searchBox.getText().trim().isEmpty();
  }

  /**
   * Dragging the handle moves the card around the list to change the mount priority.
   */
  private void installHandleDrag (ModCard card, JComponent handle)
  {
    // The handle sits at the card's top left, so its coordinates are the card's.
    if (handle.isEnabled()) handle.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    MouseAdapter drag = new MouseAdapter()
    {
      @Override
      public void mousePressed (MouseEvent e)
      {
        if (SwingUtilities.isLeftMouseButton(e) && canReorder()) {
          startDrag(card, e.getY());
          dragMouse = e.getLocationOnScreen();
        }
      }

      @Override
      public void mouseDragged (MouseEvent e)
      {
        if (dragCard == card) dragMouse = e.getLocationOnScreen();
      }

      @Override
      public void mouseReleased (MouseEvent e)
      {
        if (dragCard == card) endDrag();
      }
    };
    handle.addMouseListener(drag);
    handle.addMouseMotionListener(drag);
  }

  /**
   * Only the whole list can be reordered, a filtered one leaves it unclear where the hidden mods would go.
   */
  private boolean canReorder ()
  {
    return slots.size() > 1 && !isSearching() && addModButton.isEnabled();
  }

  private void startDrag (ModCard card, int grabY)
  {
    if (droppedCard != null) droppedCard.setLifted(false);
    droppedCard = null;
    dragCard = card;
    dragGrabY = grabY;
    card.setLifted(true);
    // Drawn over the others.
    modListPane.setComponentZOrder(card, 0);
    dragTimer.start();
  }

  private void endDrag ()
  {
    droppedCard = dragCard;
    dragCard = null;
    dragMouse = null;

    List<Mod> order = new ArrayList<>();
    for (ModCard card : slots) order.add(cardMods.get(card));
    if (!order.equals(_modManager.getModList())) {
      eventHandler.reorderMods(order);
      // Which mod's files win depends on the order.
      refreshCollisions();
    }
  }

  private void stopDrag ()
  {
    if (dragTimer != null) dragTimer.stop();
    dragCard = null;
    dragMouse = null;
    droppedCard = null;
  }

  /**
   * Dragging "frame", picked up card rendering, and re-formats the list to accommodate.
   */
  private void stepDrag ()
  {
    if (dragCard != null && dragMouse != null) {
      autoScroll();
      Point mouse = new Point(dragMouse);
      SwingUtilities.convertPointFromScreen(mouse, modListPane);
      // Kept within the card's own group, whose slots are all next to each other.
      ModGroup group = slotGroups.get(slots.indexOf(dragCard));
      int first = slotGroups.indexOf(group);
      float y = Math.max(slotY(first), Math.min(mouse.y - dragGrabY, slotY(slotGroups.lastIndexOf(group))));
      cardY.put(dragCard, y);

      int slot = first + Math.round((y - slotY(first)) / Scale.px((float) cardStride()));
      if (slot != slots.indexOf(dragCard)) {
        slots.remove(dragCard);
        slots.add(slot, dragCard);
        // Only the whole list can be dragged, so slots are mount positions.
        for (int i = 0; i < slots.size(); i++) ((JLabel) slots.get(i).getHandle()).setText(String.valueOf(i + 1));
      }
    }

    boolean settled = true;
    for (int i = 0; i < slots.size(); i++) {
      ModCard card = slots.get(i);
      float y = cardY.get(card);
      if (card != dragCard) {
        float target = slotY(i);
        y += (target - y) * 0.3f;
        if (Math.abs(target - y) < 0.5f) {
          y = target;
        } else {
          settled = false;
        }
        cardY.put(card, y);
      }
      card.setLocation(card.getX(), Math.round(y));
    }

    if (dragCard == null && settled) {
      dragTimer.stop();
      if (droppedCard != null) droppedCard.setLifted(false);
      droppedCard = null;
    }
  }

  /**
   * Distance from one card's top to the next one's, unscaled.
   */
  private int cardStride ()
  {
    return ModCard.height(compactCards) + cardGap();
  }

  private int cardGap ()
  {
    return compactCards ? COMPACT_CARD_GAP : CARD_GAP;
  }

  private float slotY (int slot)
  {
    return Scale.px(slotTops.get(slot));
  }

  /**
   * Scrolls while the mouse is near the top or bottom of the view, faster the closer it gets.
   */
  private void autoScroll ()
  {
    JViewport viewport = screenScrollPane.getViewport();
    Point mouse = new Point(dragMouse);
    SwingUtilities.convertPointFromScreen(mouse, viewport);

    int edge = Scale.px(60);
    float speed = 0;
    if (mouse.y < edge) {
      speed = -Math.min(1f, (edge - mouse.y) / (float) edge);
    } else if (mouse.y > viewport.getHeight() - edge) {
      speed = Math.min(1f, (mouse.y - viewport.getHeight() + edge) / (float) edge);
    }
    if (speed != 0) screenScrollPane.scrollToImmediately(viewport.getViewPosition().y + Math.round(speed * Scale.px(16)));
  }

  private JButton createCardActionButton (FontAwesome icon, Color foreground, Color background, String tooltip)
  {
    JButton button = new JButton(FontIcon.of(icon, Scale.px(14), foreground));
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
    // Made at the display's resolution, with the blur and corners scaled to match.
    double scale = HiDpi.scale();
    modStoreButtonImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/mod-store-btn.png"),
      HiDpi.px(Scale.px(165), scale), HiDpi.px(Scale.px(55), scale));
    modStoreButtonImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(
      new GaussianFilter((float) (Scale.px(5f) * scale)).filter(modStoreButtonImageFocused, null), HiDpi.px(Scale.px(25), scale));
    modStoreButtonImageUnfocused = new GrayscaleFilter().filter(modStoreButtonImageFocused, null);
    modStoreButtonImage = modStoreButtonImageUnfocused;
  }

  public void toggleElementsBlock (boolean block)
  {
    super.toggleElementsBlock(block);
    this.searchBox.setEnabled(!block);
    this.floatingSearchBox.setEnabled(!block);
  }

  @Inject public ModListEventHandler eventHandler;

  @Inject protected ModManager _modManager;
  @Inject protected LocaleManager _localeManager;

  public String globalWarningMessage = "";

  // Covers the whole screen to receive dropped mod files, and shows a hint while they're dragged over it.
  private JComponent dropOverlay;

  // The whole screen scrolls, with the floating search bar over it once the controls are out of view.
  private JPanel screenPane;
  private SmoothScrollPane screenScrollPane;
  private JPanel floatingBar;
  private JButton scrollToTopButton;
  private boolean draggingFiles = false;

  // The list's cards in their current slots, and where each is drawn while they move around.
  private final List<ModCard> slots = new ArrayList<>();

  // Where each slot is, unscaled, and which group it belongs to. Neither changes while dragging.
  private final List<Integer> slotTops = new ArrayList<>();
  private final List<ModGroup> slotGroups = new ArrayList<>();

  /** Whether the cards currently in the list are compact ones, fixed until it's rebuilt. */
  private boolean compactCards = false;

  private final Map<ModCard, Mod> cardMods = new HashMap<>();
  private final Map<ModCard, Float> cardY = new HashMap<>();
  private final Map<Mod, JButton> collisionButtons = new LinkedHashMap<>();
  private Timer dragTimer;
  private ModCard dragCard;
  private ModCard droppedCard;
  private int dragGrabY;
  private Point dragMouse;

  public JPanel modListPane = new JPanel();
  public JTextField floatingSearchBox = new FractionalText.Field();
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

  private static final int SCREEN_WIDTH = 800;
  private static final int VIEW_HEIGHT = 475;

  // Where the mod cards start, below the controls.
  private static final int LIST_Y = 120;

  private static final int FLOATING_BAR_WIDTH = 345;

  // Mod cards that are stacked one after the other.
  private static final int CARD_WIDTH = 750;
  private static final int CARD_GAP = 8;
  private static final int COMPACT_CARD_GAP = 6;

  // Mod group header shenanigans.
  private static final int GROUP_HEADER_HEIGHT = 28;
  private static final int GROUP_GAP = 12;

  // Whether cards show a badge with their mod's type next to its name.
  private static final boolean SHOW_TYPE_BADGE = false;

  /**
   * A simple divider over a group of mods.
   */
  private static class ModGroupHeader extends JPanel
  {
    public ModGroupHeader (String title, Color accent)
    {
      this.title = title;
      this.accent = accent;
      setLayout(null);
      setOpaque(false);

      titleLabel.setFont(Fonts.getFont("default", 14.0f, Font.BOLD | Font.ITALIC));
      add(titleLabel);
      add(separator);
    }

    /**
     * Counts the mods under this header.
     */
    public void addMod ()
    {
      titleLabel.setText(title + " (" + ++count + ")");
      revalidate();
    }

    @Override
    public void doLayout ()
    {
      int x = BAR_WIDTH + Scale.px(10);
      Dimension size = titleLabel.getPreferredSize();
      titleLabel.setBounds(x, (getHeight() - size.height) / 2, size.width, size.height);
      x += size.width + Scale.px(12);

      size = separator.getPreferredSize();
      separator.setBounds(x, (getHeight() - size.height) / 2, Math.max(0, getWidth() - x), size.height);
    }

    @Override
    protected void paintComponent (Graphics g)
    {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(accent);
      int barHeight = titleLabel.getHeight();
      g2.fillRoundRect(0, (getHeight() - barHeight) / 2, BAR_WIDTH, barHeight, BAR_WIDTH, BAR_WIDTH);
      g2.dispose();
    }

    private final String title;
    private final Color accent;
    private final JLabel titleLabel = new JLabel();
    private final JSeparator separator = new JSeparator();
    private int count = 0;

    private static final int BAR_WIDTH = Scale.px(3);
  }
}
