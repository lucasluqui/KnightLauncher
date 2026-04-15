package com.lucasluqui.launcher.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LauncherContext;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.ui.handler.ChangelogEventHandler;
import com.lucasluqui.util.ColorUtil;
import org.apache.commons.text.WordUtils;

import javax.swing.*;
import java.awt.*;

import static com.lucasluqui.launcher.ui.Log.log;

public class ChangelogUI extends BaseUI
{
  @Inject
  public ChangelogUI ()
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
    String changelogText = _ctx.getApp().getLatestReleaseChangelog();
    String latestReleaseText = _ctx.getApp().getLatestRelease();

    changelogText = changelogText.replaceAll("\\*\\*", "");
    changelogText = changelogText.replaceAll("__", "");
    changelogText = changelogText.replaceAll("-", "•");

    this.changelogArea.setText(WordUtils.wrap(changelogText, 160));
    this.releaseLabel.setText("Knight Launcher " + latestReleaseText);

    this.loadingLabel.setVisible(false);
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

    loadingLabel = new JLabel(_localeManager.getValue("m.loading"));
    loadingLabel.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    loadingLabel.setBounds(0, 0, this.width, this.height);
    loadingLabel.setFont(Fonts.getFont("defaultMedium", 14.0f, Font.PLAIN));
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loadingLabel.setVerticalAlignment(SwingConstants.CENTER);
    panel.add(loadingLabel);
    panel.setComponentZOrder(loadingLabel, 0);

    releaseLabel = new JLabel("");
    releaseLabel.setHorizontalAlignment(SwingConstants.LEFT);
    releaseLabel.setBounds(25, 0, this.width - 180, 60);
    releaseLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    panel.add(releaseLabel);

    changelogArea = new JTextArea();
    changelogArea.setBounds(25, 65, this.width - 180, this.height);
    changelogArea.setText("");
    changelogArea.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    changelogArea.setEditable(false);
    changelogArea.setEnabled(false);
    changelogArea.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    panel.add(changelogArea);
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

  @Inject public ChangelogEventHandler eventHandler;
  @Inject protected LauncherContext _ctx;
  @Inject protected LocaleManager _localeManager;

  public JLabel loadingLabel;
  public JLabel releaseLabel;
  public JTextArea changelogArea;
}
