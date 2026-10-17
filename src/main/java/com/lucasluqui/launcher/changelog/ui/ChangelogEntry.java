package com.lucasluqui.launcher.changelog.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.changelog.data.Release;
import com.lucasluqui.swing.Scale;
import com.lucasluqui.util.ColorUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.function.Consumer;

/**
 * A single release in the changelog list.
 */
public class ChangelogEntry extends JPanel
{
  /**
   * @param badge Text for a badge next to the tag, or null for none. Releases with one get an accented bar.
   * @param onLink Called with the URL of any link clicked in the changelog.
   */
  public ChangelogEntry (Release release, ChangelogDocument document, String badge, Consumer<String> onLink)
  {
    _barColor = badge == null ? CustomColors.INTERFACE_MAINPANE_FOOTNOTE : CustomColors.CHANGELOG;

    setOpaque(false);
    setLayout(new BorderLayout(0, Scale.px(8)));
    setBorder(new EmptyBorder(Scale.px(2), BAR_WIDTH + Scale.px(14), Scale.px(2), 0));
    setAlignmentX(Component.LEFT_ALIGNMENT);

    // Plain labels, so the header can't be selected like the changelog text can.
    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    header.setOpaque(false);

    JLabel tagLabel = new JLabel(release.getTag());
    tagLabel.setFont(Fonts.getFont("default", 17.0f, Font.BOLD));
    tagLabel.setForeground(ColorUtil.getForegroundColor());
    header.add(tagLabel);

    if (badge != null) {
      header.add(Box.createHorizontalStrut(Scale.px(10)));
      JLabel badgeLabel = new JLabel(badge);
      badgeLabel.setFont(Fonts.getFont("default", 9.0f, Font.BOLD));
      badgeLabel.putClientProperty(FlatClientProperties.STYLE,
        "background:" + ColorUtil.colorToHexString(CustomColors.CHANGELOG)
          + "33; foreground:" + ColorUtil.colorToHexString(CustomColors.CHANGELOG)
          + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.CHANGELOG));
      header.add(badgeLabel);
    }

    String date = release.getFormattedDate();
    if (!date.isEmpty()) {
      header.add(Box.createHorizontalStrut(Scale.px(10)));
      JLabel dateLabel = new JLabel(date);
      dateLabel.setFont(Fonts.getFont("default", 10.0f, Font.PLAIN));
      dateLabel.setForeground(CustomColors.INTERFACE_DEFAULT);
      header.add(dateLabel);
    }

    add(header, BorderLayout.NORTH);

    JTextPane changelogPane = new JTextPane();
    changelogPane.setEditable(false);
    changelogPane.setOpaque(false);
    changelogPane.setBorder(null);
    changelogPane.setMargin(new Insets(0, 0, 0, Scale.px(10)));
    ((DefaultCaret) changelogPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    changelogPane.setStyledDocument(document);
    changelogPane.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked (MouseEvent e)
      {
        String link = ChangelogDocument.getLinkAt(changelogPane, e.getPoint());
        if (link != null) onLink.accept(link);
      }
    });
    changelogPane.addMouseMotionListener(new MouseMotionAdapter()
    {
      @Override
      public void mouseMoved (MouseEvent e)
      {
        boolean overLink = ChangelogDocument.getLinkAt(changelogPane, e.getPoint()) != null;
        changelogPane.setCursor(Cursor.getPredefinedCursor(overLink ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
      }
    });
    add(changelogPane, BorderLayout.CENTER);
  }

  @Override
  public Dimension getMaximumSize ()
  {
    // Fill the list's width, but never grow taller than the content.
    return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(_barColor);
    g2.fillRoundRect(0, 0, BAR_WIDTH, getHeight(), BAR_WIDTH, BAR_WIDTH);
    g2.dispose();
  }

  private final Color _barColor;

  private static final int BAR_WIDTH = Scale.px(3);
}
