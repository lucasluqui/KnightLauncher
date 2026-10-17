package com.lucasluqui.launcher.changelog.ui;

import com.lucasluqui.swing.Scale;
import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LauncherContext;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.changelog.data.Release;
import com.lucasluqui.launcher.changelog.ui.handler.ChangelogEventHandler;
import com.lucasluqui.launcher.ui.BaseUI;
import com.lucasluqui.swing.LoadingIcon;
import com.lucasluqui.swing.ScrollablePanel;
import com.lucasluqui.swing.SmoothScrollPane;
import com.lucasluqui.util.BuildConfig;

import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
    List<Release> releases = _ctx.getApp().getReleases();

    if (releases.isEmpty()) {
      SwingUtilities.invokeLater(() -> {
        this.loadingLabel.setIcon(null);
        this.loadingLabel.setText(_localeManager.getValue("m.changelog_unavailable"));
      });
      return;
    }

    // Releases above the installed one are new. If it isn't among them we can't tell, so mark none.
    int installedIndex = -1;
    for (int i = 0; i < releases.size(); i++) {
      if (releases.get(i).getTag().equalsIgnoreCase(BuildConfig.getVersion())) {
        installedIndex = i;
        break;
      }
    }

    // Parsing can stay off the EDT, the documents aren't shown yet.
    List<ChangelogDocument> documents = new ArrayList<>();
    for (Release release : releases) {
      ChangelogDocument document = new ChangelogDocument();
      document.appendMarkdown(release.getBody());
      documents.add(document);
    }

    final int newCount = Math.max(installedIndex, 0);
    SwingUtilities.invokeLater(() -> {
      for (int i = 0; i < releases.size(); i++) {
        if (i > 0) this.changelogList.add(Box.createVerticalStrut(Scale.px(22)));
        String badge = i < newCount ? _localeManager.getValue("m.changelog_new") : null;
        this.changelogList.add(new ChangelogEntry(releases.get(i), documents.get(i), badge, eventHandler::openLink));
      }

      this.changelogList.add(Box.createVerticalStrut(Scale.px(22)));
      this.changelogList.add(createReleasesButtonPane());

      this.changelogList.revalidate();
      this.changelogList.repaint();
      this.loadingLabel.setVisible(false);
    });
  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.main"));
    guiFrame.setBounds(Scale.rect(100, 100, this.width, this.height));
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.getContentPane().setLayout(null);
    panel = (JPanel) guiFrame.getContentPane();

    loadingLabel = new JLabel(_localeManager.getValue("m.loading"));
    loadingLabel.setIcon(new LoadingIcon());
    loadingLabel.setBounds(Scale.rect(0, 0, this.width, this.height));
    loadingLabel.setFont(Fonts.getFont("default", 14.0f, Font.BOLD));
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loadingLabel.setVerticalAlignment(SwingConstants.CENTER);
    panel.add(loadingLabel);
    panel.setComponentZOrder(loadingLabel, 0);

    titleLabel = new JLabel(_localeManager.getValue("m.changelog_title"));
    titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
    titleLabel.setBounds(Scale.rect(25, 0, this.width - 100, 60));
    titleLabel.setFont(Fonts.getFont("default", 40.0f, Font.BOLD));
    panel.add(titleLabel);

    changelogList = new ScrollablePanel();
    changelogList.setLayout(new BoxLayout(changelogList, BoxLayout.Y_AXIS));
    changelogList.setOpaque(false);
    changelogList.setBorder(new EmptyBorder(0, 0, Scale.px(15), Scale.px(15)));

    changelogListScroll = new SmoothScrollPane(changelogList);
    changelogListScroll.setBounds(Scale.rect(25, 65, this.width - 35, this.height - 75));
    changelogListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    changelogListScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    changelogListScroll.setBorder(null);
    changelogListScroll.putClientProperty(FlatClientProperties.STYLE, "border:0,0,0,0");
    changelogListScroll.setOpaque(false);
    changelogListScroll.getViewport().setOpaque(false);
    changelogListScroll.getVerticalScrollBar().setOpaque(false);
    panel.add(changelogListScroll);
  }

  private JPanel createReleasesButtonPane ()
  {
    JButton releasesButton = new JButton(_localeManager.getValue("b.changelog_releases"));
    releasesButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    releasesButton.setFocusPainted(false);
    releasesButton.setFocusable(false);
    releasesButton.setForeground(Color.WHITE);
    releasesButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    releasesButton.setPreferredSize(Scale.dim(260, 30));
    releasesButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    releasesButton.addActionListener(this.eventHandler::openReleasesEvent);

    JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    buttonPane.setOpaque(false);
    buttonPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    buttonPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Scale.px(30)));
    buttonPane.add(releasesButton);
    return buttonPane;
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
  public JLabel titleLabel;
  public ScrollablePanel changelogList;
  public SmoothScrollPane changelogListScroll;
}
