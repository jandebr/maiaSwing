package org.maia.swing.animate.textslide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.maia.swing.animate.itemslide.impl.SlidingShadeFactory;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.swing.text.TextLabel;
import org.maia.util.StringUtils;

public class SlidingTextComponentBuilder implements Cloneable {

	private String text;

	private Font font;

	private Color textColor;

	private Color backgroundColor;

	private Insets padding;

	private HorizontalAlignment horizontalAlignment;

	private int headSpacing; // is >= 0

	private int tailSpacing; // is >= 0

	private int interParagraphSpacing; // is >= 0

	private int interLineSpacing; // is >= 0

	private int lineWidth; // is > 0

	private float linesInView; // is > 0

	private float linesInOverflowGradient; // no gradient when <= 0

	private Dimension maximumSize; // nullable

	private boolean repaintClientDriven;

	public static Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 16);

	public static Insets defaultPadding = new Insets(4, 4, 4, 4);

	public SlidingTextComponentBuilder(String text) {
		withText(text);
		withFont(defaultFont);
		withTextColor(Color.BLACK);
		withBackgroundColor(Color.LIGHT_GRAY);
		withPadding(defaultPadding);
		withHorizontalAlignment(HorizontalAlignment.LEFT);
		withHeadSpacing(0);
		withTailSpacing(0);
		withInterParagraphSpacing(6);
		withInterLineSpacing(0);
		withLineWidth(400);
		withLinesInView(10);
		withLinesInOverflowGradient(2);
		withRepaintClientDriven(false);
	}

	@Override
	public SlidingTextComponentBuilder clone() {
		SlidingTextComponentBuilder clone = new SlidingTextComponentBuilder(getText());
		clone.withFont(getFont());
		clone.withTextColor(getTextColor());
		clone.withBackgroundColor(getBackgroundColor());
		clone.withPadding((Insets) getPadding().clone());
		clone.withHorizontalAlignment(getHorizontalAlignment());
		clone.withHeadSpacing(getHeadSpacing());
		clone.withTailSpacing(getTailSpacing());
		clone.withInterParagraphSpacing(getInterParagraphSpacing());
		clone.withInterLineSpacing(getInterLineSpacing());
		clone.withLineWidth(getLineWidth());
		clone.withLinesInView(getLinesInView());
		clone.withLinesInOverflowGradient(getLinesInOverflowGradient());
		if (getMaximumSize() != null)
			clone.withMaximumSize(new Dimension(getMaximumSize()));
		clone.withRepaintClientDriven(isRepaintClientDriven());
		return clone;
	}

	public SlidingTextComponent build() {
		Insets padding = getPadding();
		int lineWidth = getLineWidth();
		int lineHeight = TextLabel.getLineHeight(getFont()) + getInterLineSpacing();
		int viewportHeight = Math.round(lineHeight * getLinesInView());
		int compWidth = lineWidth + padding.left + padding.right;
		int compHeight = viewportHeight + padding.top + padding.bottom;
		Dimension maxSize = getMaximumSize();
		if (maxSize != null) {
			compWidth = Math.min(compWidth, maxSize.width);
			compHeight = Math.min(compHeight, maxSize.height);
			lineWidth = compWidth - padding.left - padding.right;
			viewportHeight = compHeight - padding.top - padding.bottom;
		}
		Dimension size = new Dimension(compWidth, compHeight);
		SlidingTextComponent comp = new SlidingTextComponent(size, padding, getBackgroundColor());
		if (getLinesInOverflowGradient() > 0) {
			int shadeLength = Math.min(Math.round(lineHeight * getLinesInOverflowGradient()), viewportHeight / 2);
			comp.setShade(SlidingShadeFactory.createGradientShadeAbsoluteLength(comp, shadeLength));
		}
		List<TextLabel> lineLabels = createWrappedLineLabels(lineWidth);
		for (int i = 0; i < lineLabels.size(); i++) {
			TextLabel lineLabel = lineLabels.get(i);
			lineLabel.setBackground(getBackgroundColor());
			lineLabel.setForeground(getTextColor());
			comp.addTextLine(lineLabel);
		}
		comp.fitSlidingRangeToText();
		comp.setRepaintClientDriven(isRepaintClientDriven());
		return comp;
	}

	private List<TextLabel> createWrappedLineLabels(int lineWidth) {
		List<TextLabel> lineLabels = new Vector<TextLabel>();
		List<String> paragraphs = StringUtils.splitOnNewlines(getText());
		int n = paragraphs.size();
		for (int i = 0; i < n; i++) {
			String paragraph = paragraphs.get(i);
			boolean firstParagraph = i == 0;
			boolean lastParagraph = i == n - 1;
			int spaceBefore = firstParagraph ? getHeadSpacing() : getInterParagraphSpacing();
			int spaceAfter = lastParagraph ? getTailSpacing() : 0;
			lineLabels.addAll(createWrappedParagraphLineLabels(paragraph, spaceBefore, spaceAfter, lineWidth));
		}
		return lineLabels;
	}

	private List<TextLabel> createWrappedParagraphLineLabels(String paragraph, int paragraphSpaceBefore,
			int paragraphSpaceAfter, int lineWidth) {
		Font font = getFont();
		HorizontalAlignment hAlign = getHorizontalAlignment();
		if (!paragraph.isEmpty()) {
			List<TextLabel> lineLabels = new Vector<TextLabel>();
			FontRenderContext frc = TextLabel.getFontRenderContext(font);
			AttributedString styledParagraph = new AttributedString(paragraph);
			styledParagraph.addAttribute(TextAttribute.FONT, font);
			LineBreakMeasurer measurer = new LineBreakMeasurer(styledParagraph.getIterator(), frc);
			while (measurer.getPosition() < paragraph.length()) {
				int p0 = measurer.getPosition();
				measurer.nextLayout(lineWidth);
				int p1 = measurer.getPosition();
				String line = paragraph.substring(p0, p1);
				boolean firstLineInParagraph = lineLabels.isEmpty();
				boolean lastLineInParagraph = p1 == paragraph.length();
				int spaceBefore = firstLineInParagraph ? paragraphSpaceBefore : getInterLineSpacing();
				int spaceAfter = lastLineInParagraph ? paragraphSpaceAfter : 0;
				Insets insets = createLineInsets(spaceBefore, spaceAfter);
				TextLabel lineLabel = TextLabel.createLineLabel(line, font, lineWidth, hAlign, insets);
				lineLabels.add(lineLabel);
			}
			return lineLabels;
		} else {
			Insets insets = createLineInsets(paragraphSpaceBefore, paragraphSpaceAfter);
			TextLabel lineLabel = TextLabel.createLineLabel(paragraph, font, lineWidth, hAlign, insets);
			return Collections.singletonList(lineLabel);
		}
	}

	private Insets createLineInsets(int spaceBefore, int spaceAfter) {
		return new Insets(spaceBefore, 0, spaceAfter, 0);
	}

	public TextDimension deriveTextDimensionForComponentHeight(int componentHeight, float minFontSize,
			float maxFontSize) {
		Insets padding = getPadding();
		int viewportHeight = componentHeight - padding.top - padding.bottom;
		int lineHeight = Math.round(viewportHeight / getLinesInView());
		int lineSpacing = getInterLineSpacing();
		float fontSize = TextLabel.getFontSizeForLineHeight(getFont(), lineHeight - lineSpacing);
		fontSize = Math.max(Math.min(fontSize, maxFontSize), minFontSize);
		lineHeight = TextLabel.getLineHeight(getFont().deriveFont(fontSize)) + lineSpacing;
		float linesInView = viewportHeight / (float) lineHeight;
		return new TextDimension(fontSize, linesInView);
	}

	public SlidingTextComponentBuilder withTextDimension(TextDimension textDimension) {
		if (textDimension == null)
			throw new NullPointerException("text dimension is null");
		withFont(getFont().deriveFont(textDimension.getFontSize()));
		withLinesInView(textDimension.getLinesInView());
		return this;
	}

	public SlidingTextComponentBuilder withText(String text) {
		if (text == null)
			throw new NullPointerException("text is null");
		this.text = text;
		return this;
	}

	public SlidingTextComponentBuilder withFont(Font font) {
		if (font == null)
			throw new NullPointerException("font is null");
		this.font = font;
		return this;
	}

	public SlidingTextComponentBuilder withTextColor(Color textColor) {
		if (textColor == null)
			throw new NullPointerException("text color is null");
		this.textColor = textColor;
		return this;
	}

	public SlidingTextComponentBuilder withBackgroundColor(Color backgroundColor) {
		if (backgroundColor == null)
			throw new NullPointerException("background color is null");
		this.backgroundColor = backgroundColor;
		return this;
	}

	public SlidingTextComponentBuilder withPadding(Insets padding) {
		if (padding == null)
			throw new NullPointerException("padding is null");
		this.padding = padding;
		return this;
	}

	public SlidingTextComponentBuilder withHorizontalAlignment(HorizontalAlignment hAlignment) {
		if (hAlignment == null)
			throw new NullPointerException("horizontal alignment is null");
		this.horizontalAlignment = hAlignment;
		return this;
	}

	public SlidingTextComponentBuilder withHeadSpacing(int spacing) {
		if (spacing < 0)
			throw new IllegalArgumentException("head spacing must be positive (" + spacing + ")");
		this.headSpacing = spacing;
		return this;
	}

	public SlidingTextComponentBuilder withTailSpacing(int spacing) {
		if (spacing < 0)
			throw new IllegalArgumentException("tail spacing must be positive (" + spacing + ")");
		this.tailSpacing = spacing;
		return this;
	}

	public SlidingTextComponentBuilder withInterParagraphSpacing(int spacing) {
		if (spacing < 0)
			throw new IllegalArgumentException("inter paragraph spacing must be positive (" + spacing + ")");
		this.interParagraphSpacing = spacing;
		return this;
	}

	public SlidingTextComponentBuilder withInterLineSpacing(int spacing) {
		if (spacing < 0)
			throw new IllegalArgumentException("inter line spacing must be positive (" + spacing + ")");
		this.interLineSpacing = spacing;
		return this;
	}

	public SlidingTextComponentBuilder withLineWidth(int width) {
		if (width <= 0)
			throw new IllegalArgumentException("line width must be strictly positive (" + width + ")");
		this.lineWidth = width;
		return this;
	}

	public SlidingTextComponentBuilder withLinesInView(float linesInView) {
		if (linesInView <= 0)
			throw new IllegalArgumentException("linesInView must be strictly positive (" + linesInView + ")");
		this.linesInView = linesInView;
		return this;
	}

	public SlidingTextComponentBuilder withLinesInOverflowGradient(float lines) {
		this.linesInOverflowGradient = lines;
		return this;
	}

	public SlidingTextComponentBuilder withMaximumSize(Dimension size) {
		this.maximumSize = size;
		return this;
	}

	public SlidingTextComponentBuilder withRepaintClientDriven(boolean clientDriven) {
		this.repaintClientDriven = clientDriven;
		return this;
	}

	public String getText() {
		return text;
	}

	public Font getFont() {
		return font;
	}

	public Color getTextColor() {
		return textColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Insets getPadding() {
		return padding;
	}

	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public int getHeadSpacing() {
		return headSpacing;
	}

	public int getTailSpacing() {
		return tailSpacing;
	}

	public int getInterParagraphSpacing() {
		return interParagraphSpacing;
	}

	public int getInterLineSpacing() {
		return interLineSpacing;
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public float getLinesInView() {
		return linesInView;
	}

	public float getLinesInOverflowGradient() {
		return linesInOverflowGradient;
	}

	public Dimension getMaximumSize() {
		return maximumSize;
	}

	public boolean isRepaintClientDriven() {
		return repaintClientDriven;
	}

	public static class TextDimension {

		private float fontSize;

		private float linesInView;

		public TextDimension(float fontSize, float linesInView) {
			this.fontSize = fontSize;
			this.linesInView = linesInView;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TextDimension [fontSize=");
			builder.append(fontSize);
			builder.append(", linesInView=");
			builder.append(linesInView);
			builder.append("]");
			return builder.toString();
		}

		public float getFontSize() {
			return fontSize;
		}

		public float getLinesInView() {
			return linesInView;
		}

	}

}