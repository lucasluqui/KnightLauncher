package com.luuqui.launcher.editor;

import com.luuqui.launcher.*;
import com.luuqui.launcher.mods.ModListEventHandler;
import com.luuqui.launcher.mods.ModLoader;
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

public class EditorsGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame editorsGUIFrame;
  public static JPanel editorsPanel;

  public EditorsGUI(LauncherApp app) {
    super();
    this.app = app;
    initialize();
  }

  @SuppressWarnings("static-access")
  public void switchVisibility() {
    this.editorsGUIFrame.setVisible(!this.editorsGUIFrame.isVisible());
  }

  private void initialize() {
    editorsGUIFrame = new JFrame();
    editorsGUIFrame.setVisible(false);
    editorsGUIFrame.setTitle("Editors");
    editorsGUIFrame.setBounds(100, 100, 385, 460);
    editorsGUIFrame.setResizable(false);
    editorsGUIFrame.setUndecorated(true);
    editorsGUIFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    editorsGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    editorsGUIFrame.getContentPane().setLayout(null);
    editorsPanel = (JPanel) editorsGUIFrame.getContentPane();

    JButton startModelViewerButton = new JButton("Start Model Viewer");
    startModelViewerButton.setBounds(30, 15, 170, 25);
    startModelViewerButton.setFont(Fonts.fontMed);
    startModelViewerButton.setFocusPainted(false);
    startModelViewerButton.setFocusable(false);
    startModelViewerButton.setToolTipText(Locale.getValue("Get mods via Discord"));
    editorsGUIFrame.getContentPane().add(startModelViewerButton);
    startModelViewerButton.addActionListener(EditorsEventHandler::startModelViewer);

  }

}
