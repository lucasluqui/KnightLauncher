package com.lucasluqui.swing;

import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

/**
 * Table on a {@link Card}.
 */
public class CardTable extends Card
{
  public CardTable (String... headers)
  {
    super(new GridLayout(0, Math.max(headers.length, 1)));
    if (headers.length == 0) throw new IllegalArgumentException("A table needs at least one column.");
    _headers = new JLabel[headers.length];
    for (int i = 0; i < headers.length; i++) _headers[i] = createCell(headers[i], Font.BOLD);

    _emptyLabel = new JLabel("", SwingConstants.CENTER);
    _emptyLabel.setFont(Fonts.getFont("default", 12.0f, Font.ITALIC));
    _emptyLabel.setForeground(CustomColors.INTERFACE_DEFAULT);

    rebuild();
  }

  /**
   * Adds a row below the others. Cells past the last column are left out and missing ones are left blank.
   *
   * @return The row's cells, to color or otherwise change further.
   */
  public JLabel[] addRow (String... texts)
  {
    JLabel[] row = new JLabel[_headers.length];
    for (int i = 0; i < row.length; i++) {
      row[i] = createCell(i < texts.length ? texts[i] : "", i == 0 && _boldFirstColumn ? Font.BOLD : Font.PLAIN);
    }
    _rows.add(row);
    rebuild();
    return row;
  }

  public void clearRows ()
  {
    _rows.clear();
    rebuild();
  }

  /**
   * Whether the first cell of each row is bold like the headers, to name the row.
   * Applies to rows added after the fact.
   * Off by default.
   */
  public void setBoldFirstColumn (boolean boldFirstColumn)
  {
    _boldFirstColumn = boldFirstColumn;
  }

  /**
   * What to say instead of showing the table when it has too few rows.
   */
  public void setEmptyText (String emptyText)
  {
    _emptyLabel.setText(emptyText);
  }

  /**
   * How many rows the table needs before it's worth showing. With fewer, it shows its empty text instead.
   * One by default.
   */
  public void setMinimumRows (int minimumRows)
  {
    if (minimumRows < 0) throw new IllegalArgumentException("Can't need fewer than no rows.");
    _minimumRows = minimumRows;
    rebuild();
  }

  /**
   * Swaps between the table and its empty text, depending on how many rows there are.
   */
  private void rebuild ()
  {
    removeAll();
    _showingRows = _rows.size() >= _minimumRows;
    if (_showingRows) {
      setLayout(new GridLayout(0, _headers.length));
      setBorder(new EmptyBorder(Scale.px(4), Scale.px(10), Scale.px(4), Scale.px(10)));
      for (JLabel header : _headers) add(header);
      for (JLabel[] row : _rows) for (JLabel cell : row) add(cell);
    } else {
      setLayout(new BorderLayout());
      setBorder(new EmptyBorder(Scale.px(30), 0, Scale.px(30), 0));
      add(_emptyLabel);
    }
    revalidate();
    repaint();
  }

  private JLabel createCell (String text, int style)
  {
    JLabel cell = new JLabel(text);
    cell.setFont(Fonts.getFont("default", 12.0f, style));
    cell.setForeground(Color.WHITE);
    cell.setBorder(new EmptyBorder(Scale.px(7), Scale.px(10), Scale.px(7), Scale.px(10)));
    return cell;
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    super.paintComponent(g);
    if (!_showingRows) return;

    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND_FOCUS_DARK);

    // Every other row below the header, shaded through the card's shape so the last row's corners stay rounded too.
    int columns = _headers.length;
    for (int i = columns; i < getComponentCount(); i += 2 * columns) {
      Rectangle row = getComponent(i).getBounds();
      Area stripe = new Area(shape());
      stripe.intersect(new Area(new Rectangle(0, row.y, getWidth(), row.height)));
      g2d.fill(stripe);
    }
    g2d.dispose();
  }

  private final JLabel[] _headers;
  private final JLabel _emptyLabel;
  private final List<JLabel[]> _rows = new ArrayList<>();
  private boolean _boldFirstColumn;
  private boolean _showingRows;
  private int _minimumRows = 1;
}
