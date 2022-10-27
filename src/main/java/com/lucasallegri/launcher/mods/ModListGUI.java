package com.lucasallegri.launcher.mods;

import com.lucasallegri.launcher.*;
import com.lucasallegri.launcher.mods.data.Mod;
import com.lucasallegri.launcher.settings.SettingsEventHandler;
import com.lucasallegri.util.ColorUtil;
import com.lucasallegri.util.DesktopUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import mdlaf.utils.MaterialBorders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ModListGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame modListGUIFrame;
  public static List modListContainer;
  public static JLabel labelModCount;
  public static JLabel labelForceApplyState;
  private JLabel labelModCountText;
  private JButton refreshButton;
  private JButton forceApplyButton;
  private JButton enableButton;
  private JButton disableButton;
  private JLabel labelName;
  private JLabel labelDescription;
  private JLabel labelVersion;
  private JLabel labelAuthor;

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
    modListGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    modListGUIFrame.getContentPane().setLayout(null);

    modListContainer = new List();
    modListContainer.setBounds(10, 26, 162, 326);
    modListContainer.setFont(Fonts.fontMed);
    modListContainer.setBackground(ColorUtil.getBackgroundColor());
    modListContainer.setForeground(ColorUtil.getForegroundColor());
    modListContainer.setFocusable(false);
    modListGUIFrame.getContentPane().add(modListContainer);
    updateModList();
    modListContainer.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        Mod currentMod = ModLoader.getModList().get(modListContainer.getSelectedIndex());
        labelName.setText(currentMod.getDisplayName());
        labelDescription.setText("<html>" + currentMod.getDescription() + "</html>");
        labelVersion.setText(Locale.getValue("m.mod_version", currentMod.getVersion()));
        labelAuthor.setText(Locale.getValue("m.mod_author", currentMod.getAuthor()));
        if(currentMod.isEnabled()) {
          enableButton.setVisible(false);
          enableButton.setVisible(false);
          disableButton.setVisible(true);
          disableButton.setEnabled(true);
        } else {
          enableButton.setVisible(true);
          enableButton.setEnabled(true);
          disableButton.setVisible(false);
          disableButton.setEnabled(false);
        }
      }
    });

    labelModCount = new JLabel(String.valueOf(ModLoader.getModList().size()));
    labelModCount.setHorizontalAlignment(SwingConstants.CENTER);
    labelModCount.setBounds(178, 44, 188, 40);
    labelModCount.setFont(Fonts.fontMedGiant);
    modListGUIFrame.getContentPane().add(labelModCount);

    labelModCountText = new JLabel(Locale.getValue("m.mods_installed"));
    labelModCountText.setHorizontalAlignment(SwingConstants.CENTER);
    labelModCountText.setBounds(178, 93, 188, 14);
    labelModCountText.setFont(Fonts.fontReg);
    modListGUIFrame.getContentPane().add(labelModCountText);

    refreshButton = new JButton(Locale.getValue("b.refresh"));
    refreshButton.setBounds(12, 430, 89, 23);
    refreshButton.setFont(Fonts.fontMed);
    refreshButton.setFocusPainted(false);
    refreshButton.setFocusable(false);
    refreshButton.setToolTipText(Locale.getValue("b.refresh"));
    modListGUIFrame.getContentPane().add(refreshButton);
    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent action) {
        ModListEventHandler.refreshEvent(action);
      }
    });

    JButton modFolderButton = new JButton(Locale.getValue("b.open_mods_folder"));
    modFolderButton.setBounds(107, 430, 136, 23);
    modFolderButton.setFont(Fonts.fontMed);
    modFolderButton.setFocusPainted(false);
    modFolderButton.setFocusable(false);
    modFolderButton.setToolTipText(Locale.getValue("b.open_mods_folder"));
    modListGUIFrame.getContentPane().add(modFolderButton);
    modFolderButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        DesktopUtil.openDir(LauncherGlobals.USER_DIR + "/mods");
      }
    });

    JButton getModsButton = new JButton(Locale.getValue("b.get_mods"));
    getModsButton.setBounds(248, 430, 126, 23);
    getModsButton.setFont(Fonts.fontMed);
    getModsButton.setFocusPainted(false);
    getModsButton.setFocusable(false);
    getModsButton.setToolTipText(Locale.getValue("b.get_mods"));
    modListGUIFrame.getContentPane().add(getModsButton);
    getModsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        ModListEventHandler.getModsEvent(_action);
      }
    });

    forceApplyButton = new JButton("Force apply");
    forceApplyButton.setBounds(12, 360, 125, 23);
    forceApplyButton.setFont(Fonts.fontMed);
    forceApplyButton.setFocusPainted(false);
    forceApplyButton.setFocusable(false);
    forceApplyButton.setToolTipText("Force apply");
    modListGUIFrame.getContentPane().add(forceApplyButton);
    forceApplyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        ModListEventHandler.forceApplyEvent(_action);
      }
    });

    labelForceApplyState = new JLabel("");
    labelForceApplyState.setBounds(145, 358, 125, 25);
    labelForceApplyState.setFont(Fonts.fontReg);
    modListGUIFrame.getContentPane().add(labelForceApplyState);

    JSeparator separator = new JSeparator();
    separator.setBounds(178, 130, 195, 2);
    modListGUIFrame.getContentPane().add(separator);

    labelName = new JLabel("");
    labelName.setFont(Fonts.fontMed);
    labelName.setHorizontalAlignment(SwingConstants.CENTER);
    labelName.setBounds(178, 148, 188, 14);
    modListGUIFrame.getContentPane().add(labelName);

    labelAuthor = new JLabel("");
    labelAuthor.setFont(Fonts.fontReg);
    labelAuthor.setHorizontalAlignment(SwingConstants.CENTER);
    labelAuthor.setBounds(178, 165, 188, 14);
    modListGUIFrame.getContentPane().add(labelAuthor);

    labelDescription = new JLabel("");
    labelDescription.setFont(Fonts.fontReg);
    labelDescription.setHorizontalAlignment(SwingConstants.LEADING);
    labelDescription.setVerticalAlignment(SwingConstants.TOP);
    labelDescription.setBounds(188, 196, 178, 70);
    modListGUIFrame.getContentPane().add(labelDescription);

    labelVersion = new JLabel("");
    labelVersion.setFont(Fonts.fontReg);
    labelVersion.setBounds(188, 274, 178, 14);
    modListGUIFrame.getContentPane().add(labelVersion);

    enableButton = new JButton(Locale.getValue("b.enable"));
    enableButton.setFont(Fonts.fontMed);
    enableButton.setForeground(ColorUtil.getGreenForegroundColor());
    enableButton.setEnabled(false);
    enableButton.setFocusable(false);
    enableButton.setFocusPainted(false);
    enableButton.setBounds(183, 326, 89, 23);
    modListGUIFrame.getContentPane().add(enableButton);
    enableButton.setVisible(false);
    enableButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int idx = modListContainer.getSelectedIndex();
        ModListEventHandler.enableMod(ModLoader.getModList().get(idx));
        modListContainer.select(idx);
        enableButton.setVisible(false);
        disableButton.setVisible(true);
        disableButton.setEnabled(true);
      }
    });

    disableButton = new JButton(Locale.getValue("b.disable"));
    disableButton.setFont(Fonts.fontMed);
    disableButton.setForeground(ColorUtil.getRedForegroundColor());
    disableButton.setEnabled(false);
    disableButton.setFocusable(false);
    disableButton.setFocusPainted(false);
    disableButton.setBounds(183, 326, 89, 23);
    modListGUIFrame.getContentPane().add(disableButton);
    disableButton.setVisible(false);
    disableButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int idx = modListContainer.getSelectedIndex();
        ModListEventHandler.disableMod(ModLoader.getModList().get(idx));
        modListContainer.select(idx);
        disableButton.setVisible(false);
        enableButton.setVisible(true);
        enableButton.setEnabled(true);
      }
    });

    JPanel titleBar = new JPanel();
    titleBar.setBounds(0, 0, modListGUIFrame.getWidth(), 20);
    titleBar.setBackground(ColorUtil.getTitleBarColor());
    modListGUIFrame.getContentPane().add(titleBar);


    /*
     * Based on Paul Samsotha's reply @ StackOverflow
     * link: https://stackoverflow.com/questions/24476496/drag-and-resize-undecorated-jframe
     */
    titleBar.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me) {

        pX = me.getX();
        pY = me.getY();
      }
    });
    titleBar.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent me) {

        pX = me.getX();
        pY = me.getY();
      }

      @Override
      public void mouseDragged(MouseEvent me) {

        modListGUIFrame.setLocation(modListGUIFrame.getLocation().x + me.getX() - pX,
                modListGUIFrame.getLocation().y + me.getY() - pY);
      }
    });
    titleBar.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent me) {

        modListGUIFrame.setLocation(modListGUIFrame.getLocation().x + me.getX() - pX,
                modListGUIFrame.getLocation().y + me.getY() - pY);
      }

      @Override
      public void mouseMoved(MouseEvent arg0) {
        // Auto-generated method stub
      }
    });
    titleBar.setLayout(null);

    JLabel windowTitle = new JLabel(Locale.getValue("t.mods"));
    windowTitle.setFont(Fonts.fontMed);
    windowTitle.setBounds(10, 0, modListGUIFrame.getWidth() - 100, 20);
    titleBar.add(windowTitle);

    Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 14, ColorUtil.getForegroundColor());
    JButton closeButton = new JButton(closeIcon);
    closeButton.setBounds(modListGUIFrame.getWidth() - 20, 1, 20, 21);
    closeButton.setToolTipText(Locale.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
    closeButton.setFont(Fonts.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        modListGUIFrame.setVisible(false);
      }
    });

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.CHEVRON_DOWN, 14, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(modListGUIFrame.getWidth() - 40, 1, 20, 21);
    minimizeButton.setToolTipText(Locale.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
    minimizeButton.setFont(Fonts.fontMed);
    titleBar.add(minimizeButton);
    minimizeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        modListGUIFrame.setState(Frame.ICONIFIED);
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

  public static void updateModList() {
    int idx = modListContainer.getSelectedIndex();
    modListContainer.removeAll();
    for (Mod mod : ModLoader.getModList()) {
      modListContainer.add(mod.isEnabled() ? "(✓) " + mod.getDisplayName(): "(X) " + mod.getDisplayName());
    }
    modListContainer.select(idx);
  }
}
