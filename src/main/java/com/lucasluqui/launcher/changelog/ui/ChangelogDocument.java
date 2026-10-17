package com.lucasluqui.launcher.changelog.ui;

import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.swing.Scale;
import com.lucasluqui.util.ColorUtil;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lucasluqui.launcher.changelog.ui.Log.log;

/**
 * A styled document holding a release changelog, rendered from markdown.
 */
public class ChangelogDocument extends DefaultStyledDocument
{
  public ChangelogDocument ()
  {
    super(new FontContext());
  }

  public void appendMarkdown (String markdown)
  {
    PARSER.parse(markdown).accept(new Renderer());
    finishParagraph();
  }

  /**
   * Returns the link URL under the given point of a pane showing this document, or null if there's none.
   */
  public static String getLinkAt (JTextPane pane, Point point)
  {
    int pos = pane.viewToModel(point);
    if (pos < 0) return null;

    StyledDocument doc = pane.getStyledDocument();
    try {
      // The model position is the closest character boundary, the character under the point is either side of it.
      Rectangle boundary = pane.modelToView(pos);
      int index = boundary != null && point.x < boundary.x ? pos - 1 : pos;
      if (index < 0 || index >= doc.getLength()) return null;

      Rectangle start = pane.modelToView(index);
      Rectangle end = pane.modelToView(index + 1);
      if (start == null || point.y < start.y || point.y > start.y + start.height) return null;
      if (point.x < start.x) return null;
      if (end != null && end.y == start.y && point.x > end.x) return null;

      Object link = doc.getCharacterElement(index).getAttributes().getAttribute(LINK);
      return link == null ? null : link.toString();
    } catch (BadLocationException e) {
      return null;
    }
  }

  private void startParagraph (MutableAttributeSet attrs)
  {
    finishParagraph();
    if (getLength() > 0) {
      append("\n", text(FAMILY_DEFAULT, BODY_SIZE, false, ColorUtil.getForegroundColor()));
    } else {
      // Nothing to space the first block from.
      StyleConstants.setSpaceAbove(attrs, 0);
    }
    _paragraphStart = getLength();
    _paragraphAttrs = attrs;
  }

  private void finishParagraph ()
  {
    if (_paragraphAttrs == null) return;

    int end = getLength();
    setParagraphAttributes(_paragraphStart, end - _paragraphStart, _paragraphAttrs, false);

    // Line breaks inside a block split it into several paragraphs. Keep them spaced as one block,
    // and only let the first line hang the list marker.
    for (int offset = _paragraphStart; offset <= end; ) {
      Element paragraph = getParagraphElement(offset);
      boolean first = paragraph.getStartOffset() <= _paragraphStart;
      boolean last = paragraph.getEndOffset() > end;

      if (!first || !last) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        if (!first) {
          StyleConstants.setSpaceAbove(attrs, 0);
          StyleConstants.setFirstLineIndent(attrs, 0);
        }
        if (!last) {
          StyleConstants.setSpaceBelow(attrs, 0);
        }
        setParagraphAttributes(paragraph.getStartOffset(), 0, attrs, false);
      }

      offset = paragraph.getEndOffset();
    }

    _paragraphAttrs = null;
  }

  private void append (String str, AttributeSet attrs)
  {
    try {
      insertString(getLength(), str, attrs);
    } catch (BadLocationException e) {
      log.error(e);
    }
  }

  private static MutableAttributeSet paragraph (int spaceAbove, int spaceBelow, int leftIndent, float firstLineIndent)
  {
    MutableAttributeSet attrs = new SimpleAttributeSet();
    StyleConstants.setSpaceAbove(attrs, Scale.px(spaceAbove));
    StyleConstants.setSpaceBelow(attrs, Scale.px(spaceBelow));
    StyleConstants.setLeftIndent(attrs, Scale.px(leftIndent));
    StyleConstants.setFirstLineIndent(attrs, firstLineIndent);
    StyleConstants.setLineSpacing(attrs, LINE_SPACING);
    return attrs;
  }

  private static MutableAttributeSet text (String family, float size, boolean bold, Color foreground)
  {
    MutableAttributeSet attrs = new SimpleAttributeSet();
    attrs.addAttribute(FAMILY, family);
    attrs.addAttribute(SIZE, size);
    StyleConstants.setBold(attrs, bold);
    StyleConstants.setForeground(attrs, foreground);
    return attrs;
  }

  private static float measure (String str, float size)
  {
    Font font = Fonts.getFont(FAMILY_DEFAULT, size, Font.PLAIN);
    return (float) Math.ceil(font.getStringBounds(str, FRC).getWidth());
  }

  /**
   * Walks the markdown tree, appending each block as a paragraph
   * and each inline run with its own attributes.
   */
  private class Renderer extends AbstractVisitor
  {
    @Override
    public void visit (Heading heading)
    {
      startBlock(HEADING_SPACE_ABOVE, HEADING_SPACE_BELOW);
      float previousSize = _size;
      boolean previousBold = _bold;
      _size = heading.getLevel() == 1 ? H1_SIZE : heading.getLevel() == 2 ? H2_SIZE : H3_SIZE;
      _bold = true;
      visitChildren(heading);
      _size = previousSize;
      _bold = previousBold;
    }

    @Override
    public void visit (Paragraph paragraph)
    {
      startBlock(0, _lists.isEmpty() ? PARAGRAPH_SPACE_BELOW : LIST_ITEM_SPACE_BELOW);
      visitChildren(paragraph);
    }

    @Override
    public void visit (BulletList list)
    {
      _lists.push(new int[] { -1 });
      visitChildren(list);
      _lists.pop();
      _listEnded = _lists.isEmpty();
    }

    @Override
    public void visit (OrderedList list)
    {
      _lists.push(new int[] { list.getStartNumber() });
      visitChildren(list);
      _lists.pop();
      _listEnded = _lists.isEmpty();
    }

    @Override
    public void visit (ListItem item)
    {
      int[] list = _lists.peek();
      _pendingMarker = list[0] < 0 ? BULLET : (list[0]++) + ". ";
      visitChildren(item);

      // An empty item still shows its marker.
      if (_pendingMarker != null) {
        startBlock(0, LIST_ITEM_SPACE_BELOW);
      }
    }

    @Override
    public void visit (BlockQuote quote)
    {
      _quoteDepth++;
      visitChildren(quote);
      _quoteDepth--;
    }

    @Override
    public void visit (FencedCodeBlock block)
    {
      appendCodeBlock(block.getLiteral());
    }

    @Override
    public void visit (IndentedCodeBlock block)
    {
      appendCodeBlock(block.getLiteral());
    }

    @Override
    public void visit (ThematicBreak thematicBreak)
    {
      startBlock(0, PARAGRAPH_SPACE_BELOW);
    }

    @Override
    public void visit (Text text)
    {
      appendText(text.getLiteral());
    }

    @Override
    public void visit (Code code)
    {
      boolean previousCode = _code;
      _code = true;
      append(code.getLiteral(), current());
      _code = previousCode;
    }

    @Override
    public void visit (Emphasis emphasis)
    {
      boolean previousItalic = _italic;
      _italic = true;
      visitChildren(emphasis);
      _italic = previousItalic;
    }

    @Override
    public void visit (StrongEmphasis emphasis)
    {
      boolean previousBold = _bold;
      _bold = true;
      visitChildren(emphasis);
      _bold = previousBold;
    }

    @Override
    public void visit (CustomNode node)
    {
      if (!(node instanceof Strikethrough)) {
        visitChildren(node);
        return;
      }

      boolean previousStrike = _strike;
      _strike = true;
      visitChildren(node);
      _strike = previousStrike;
    }

    @Override
    public void visit (Link link)
    {
      appendLink(link, link.getDestination());
    }

    @Override
    public void visit (Image image)
    {
      // Never load remote images, link to them instead.
      appendLink(image, image.getDestination());
    }

    @Override
    public void visit (SoftLineBreak lineBreak)
    {
      // GitHub keeps single line breaks in release bodies.
      append("\n", current());
    }

    @Override
    public void visit (HardLineBreak lineBreak)
    {
      append("\n", current());
    }

    @Override
    public void visit (HtmlBlock html)
    {
      // Raw HTML is dropped, never rendered.
    }

    @Override
    public void visit (HtmlInline html)
    {
      // Raw HTML is dropped, never rendered.
    }

    private void startBlock (int spaceAbove, int spaceBelow)
    {
      int indent = _quoteDepth * QUOTE_INDENT + _lists.size() * LIST_INDENT;

      // List items are spaced tighter than paragraphs, give whatever follows a list some room.
      if (_listEnded) {
        spaceAbove = Math.max(spaceAbove, PARAGRAPH_SPACE_BELOW);
        _listEnded = false;
      }

      if (_pendingMarker == null) {
        startParagraph(paragraph(spaceAbove, spaceBelow, indent, 0));
        return;
      }

      // Hang the marker left of the item's text so wrapped lines line up with it.
      float markerWidth = measure(_pendingMarker, BODY_SIZE);
      startParagraph(paragraph(spaceAbove, spaceBelow, indent, -markerWidth));
      append(_pendingMarker, text(FAMILY_DEFAULT, BODY_SIZE, false, foreground()));
      _pendingMarker = null;
    }

    private void appendCodeBlock (String literal)
    {
      startBlock(0, PARAGRAPH_SPACE_BELOW);
      boolean previousCode = _code;
      _code = true;
      append(literal.endsWith("\n") ? literal.substring(0, literal.length() - 1) : literal, current());
      _code = previousCode;
    }

    private void appendLink (Node node, String destination)
    {
      String previousLink = _link;
      _link = destination;
      if (node.getFirstChild() == null) {
        append(destination, current());
      } else {
        visitChildren(node);
      }
      _link = previousLink;
    }

    /**
     * Appends plain text, turning GitHub's issue and user references into links like GitHub does.
     */
    private void appendText (String str)
    {
      if (_link != null) {
        append(str, current());
        return;
      }

      Matcher matcher = GITHUB_REFERENCE.matcher(str);
      int last = 0;
      while (matcher.find()) {
        append(str.substring(last, matcher.start()), current());
        _link = matcher.group(1) != null
          ? LauncherGlobals.GITHUB_URL_REPO + "issues/" + matcher.group(1)
          : LauncherGlobals.GITHUB_URL + matcher.group(2);
        append(matcher.group(), current());
        _link = null;
        last = matcher.end();
      }
      append(str.substring(last), current());
    }

    private Color foreground ()
    {
      return _quoteDepth > 0 ? MUTED : ColorUtil.getForegroundColor();
    }

    private AttributeSet current ()
    {
      MutableAttributeSet attrs = text(_code ? FAMILY_CODE : FAMILY_DEFAULT, _code ? CODE_SIZE : _size, _bold,
        _link != null ? CustomColors.CHANGELOG : foreground());
      StyleConstants.setItalic(attrs, _italic);
      StyleConstants.setStrikeThrough(attrs, _strike);
      if (_code) StyleConstants.setBackground(attrs, CODE_BACKGROUND);
      if (_link != null) attrs.addAttribute(LINK, _link);
      return attrs;
    }

    // Each entry is a list being rendered: its next item number, or -1 for bullet lists.
    private final Deque<int[]> _lists = new ArrayDeque<>();
    private String _pendingMarker;
    private boolean _listEnded;
    private int _quoteDepth;

    private float _size = BODY_SIZE;
    private boolean _bold;
    private boolean _italic;
    private boolean _strike;
    private boolean _code;
    private String _link;
  }

  /**
   * Resolves fonts through {@link Fonts}.
   */
  private static class FontContext extends StyleContext
  {
    @Override
    public Font getFont (AttributeSet attrs)
    {
      Object family = attrs.getAttribute(FAMILY);
      Object size = attrs.getAttribute(SIZE);
      int style = (StyleConstants.isBold(attrs) ? Font.BOLD : 0) | (StyleConstants.isItalic(attrs) ? Font.ITALIC : 0);
      return Fonts.getFont(family == null ? FAMILY_DEFAULT : family.toString(),
        size instanceof Float ? (Float) size : BODY_SIZE, style);
    }
  }

  private int _paragraphStart;
  private MutableAttributeSet _paragraphAttrs;

  private static final List<Extension> EXTENSIONS = Arrays.asList(
    AutolinkExtension.create(), StrikethroughExtension.create());
  private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

  // #123 for issues and pull requests, @name for users. Not when part of a word, path or email.
  private static final Pattern GITHUB_REFERENCE = Pattern.compile(
    "(?<![\\w/&#@.-])(?:#(\\d+)|@([A-Za-z0-9](?:[A-Za-z0-9-]{0,38})))(?![\\w-])");

  private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

  // own attribute keys, read back by FontContext and getLinkAt.
  private static final Object FAMILY = new Object();
  private static final Object SIZE = new Object();
  private static final Object LINK = new Object();

  private static final String FAMILY_DEFAULT = "default";
  private static final String FAMILY_CODE = "code";
  private static final String BULLET = "• ";

  private static final Color MUTED = CustomColors.INTERFACE_DEFAULT;
  private static final Color CODE_BACKGROUND = CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND;

  // for reference: sizes are for 100% UI scale.
  private static final float BODY_SIZE = 11.0f;
  private static final float CODE_SIZE = 12.0f;
  private static final float H1_SIZE = 15.0f;
  private static final float H2_SIZE = 13.5f;
  private static final float H3_SIZE = 12.0f;
  private static final float LINE_SPACING = 0.15f;

  private static final int HEADING_SPACE_ABOVE = 8;
  private static final int HEADING_SPACE_BELOW = 6;
  private static final int PARAGRAPH_SPACE_BELOW = 6;
  private static final int LIST_ITEM_SPACE_BELOW = 3;
  private static final int LIST_INDENT = 16;
  private static final int QUOTE_INDENT = 14;
}
