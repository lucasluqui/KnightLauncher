/*
package com.lucasluqui.launcher.ui;

import com.google.inject.Inject;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.LocaleManager;

import javax.swing.*;

public class TemplateUI extends BaseUI
{
  @Inject
  public TemplateUI ()
  {
    super(800, 455, false);
  }

  public void init ()
  {
    super.init();
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
    guiFrame.setTitle(_localeManager.getValue("t.main"));
    guiFrame.setBounds(100, 100, this.width, this.height);
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.getContentPane().setLayout(null);
    panel = (JPanel) guiFrame.getContentPane();
  }

  public void selectedServerChanged ()
  {
    this.eventHandler.selectedServerChanged();
  }

  public void specialKeyPressed ()
  {

  }

  public void specialKeyReleased ()
  {

  }

  public void toggleElementsBlock (boolean block)
  {

  }

  // custom methods here.

  @Inject public EventHandler eventHandler;
  @Inject protected LauncherContext _ctx;
  @Inject protected LocaleManager _localeManager;

  // custom attributes, fields, and swing components here.
}
*/