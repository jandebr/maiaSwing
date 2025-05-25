package org.maia.swing.text;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.border.Border;

import org.maia.graphics2d.image.GradientImageFactory;
import org.maia.graphics2d.image.GradientImageFactory.GradientFunction;
import org.maia.swing.SwingUtils;
import org.maia.swing.border.ClippingBorder;
import org.maia.swing.layout.FillMode;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.util.ColorUtils;

@SuppressWarnings("serial")
public class TextLabel extends JComponent {

	private String text;

	private FillMode fillMode = FillMode.NONE;

	private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;

	private VerticalTextAlignment verticalAlignment = VerticalTextAlignment.CENTER;

	private double relativeBaselineHeight = -1.0; // between 0 and 1, measured from the top (-1 when unset)

	private Insets fixedInsets; // takes precedence over Border insets, if any

	private TextOverflowMode textOverflowMode = TextOverflowMode.CLIP;

	private int textOverflowThresholdInPixels = 0;

	private String textOverflowAbbreviation = "..";

	private String fadeWidthReferenceString = "mm";

	private Image pristineLeadingFadeImage;

	private static final int absoluteMaximumFadeWidth = 200;

	public TextLabel(String text, Font font, Dimension preferredSize) {
		this.text = text;
		setFont(font);
		setPreferredSize(preferredSize);
		setBackground(null);
	}

	public static TextLabel createSizedLabel(String text, Font font, Dimension size) {
		TextLabel label = new TextLabel(text, font, size);
		SwingUtils.fixSize(label, size);
		return label;
	}

	public static TextLabel createCompactLabel(String text, Font font) {
		return createCompactLabel(text, font, null);
	}

	public static TextLabel createCompactLabel(String text, Font font, Insets insets) {
		TextLabel label = new TextLabel(text, font, getCompactSize(text, font, insets));
		label.setFixedInsets(insets);
		return label;
	}

	private static Dimension getCompactSize(String text, Font font, Insets insets) {
		Graphics2D g = createCompatibleGraphics(font);
		Dimension size = getTextSize(text, g);
		g.dispose();
		if (insets != null) {
			size.setSize(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom);
		}
		return size;
	}

	public static TextLabel createLineLabel(String text, Font font, HorizontalAlignment hAlignment) {
		return createLineLabel(text, font, hAlignment, null);
	}

	public static TextLabel createLineLabel(String text, Font font, HorizontalAlignment hAlignment, Insets insets) {
		return createLineLabel(text, font, -1, hAlignment, insets);
	}

	public static TextLabel createLineLabel(String text, Font font, int lineWidth, HorizontalAlignment hAlignment) {
		return createLineLabel(text, font, lineWidth, hAlignment, null);
	}

	public static TextLabel createLineLabel(String text, Font font, int lineWidth, HorizontalAlignment hAlignment,
			Insets insets) {
		int width = lineWidth;
		if (width < 0) {
			width = getCompactSize(text, font, insets).width;
		} else if (insets != null) {
			width += insets.left + insets.right;
		}
		int height = getLineHeight(font);
		if (insets != null) {
			height += insets.top + insets.bottom;
		}
		TextLabel label = new TextLabel(text, font, new Dimension(width, height));
		label.setHorizontalAlignment(hAlignment);
		label.setVerticalAlignment(VerticalTextAlignment.BASELINE);
		label.setFixedInsets(insets);
		return label;
	}

	public static FontRenderContext getFontRenderContext(Font font) {
		Graphics2D g = createCompatibleGraphics(font);
		FontRenderContext frc = g.getFontRenderContext();
		g.dispose();
		return frc;
	}

	protected static Graphics2D createCompatibleGraphics(Font font) {
		Graphics2D g = SwingUtils.getDefaultGraphics();
		g.setFont(font);
		initGraphics(g);
		return g;
	}

	private static void initGraphics(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	public static int getLineHeight(Font font) {
		Graphics2D g = createCompatibleGraphics(font);
		int lineHeight = g.getFontMetrics().getHeight();
		g.dispose();
		return lineHeight;
	}

	public static int getLineWidth(Font font, String text) {
		Graphics2D g = createCompatibleGraphics(font);
		int lineWidth = getTextSize(text, g).width;
		g.dispose();
		return lineWidth;
	}

	public static int getAdvanceOfSpaceCharacter(Font font) {
		Graphics2D g = createCompatibleGraphics(font);
		int advance = font.getStringBounds(" ", g.getFontRenderContext()).getBounds().width;
		g.dispose();
		return advance;
	}

	public static float getFontSizeForLineHeight(Font font, int lineHeight) {
		float ratio = getLineHeight(font.deriveFont(100f)) / 100f;
		float fontSize = Math.round(lineHeight / ratio);
		float stepSize = 1f;
		int lh = getLineHeight(font.deriveFont(fontSize));
		if (lh < lineHeight) {
			do {
				fontSize += stepSize;
				lh = getLineHeight(font.deriveFont(fontSize));
			} while (lh < lineHeight);
			if (lh > lineHeight)
				fontSize -= stepSize;
		} else if (lh > lineHeight) {
			do {
				fontSize -= stepSize;
				lh = getLineHeight(font.deriveFont(fontSize));
			} while (lh > lineHeight);
		}
		return fontSize;
	}

	public static float getFontSizeForLineWidth(Font font, String text, int lineWidth) {
		float ratio = getLineWidth(font.deriveFont(100f), text) / 100f;
		float fontSize = Math.round(lineWidth / ratio);
		float stepSize = 1f;
		int lw = getLineWidth(font.deriveFont(fontSize), text);
		if (lw < lineWidth) {
			do {
				fontSize += stepSize;
				lw = getLineWidth(font.deriveFont(fontSize), text);
			} while (lw < lineWidth);
			if (lw > lineWidth)
				fontSize -= stepSize;
		} else if (lw > lineWidth) {
			do {
				fontSize -= stepSize;
				lw = getLineWidth(font.deriveFont(fontSize), text);
			} while (lw > lineWidth);
		}
		return fontSize;
	}

	protected static Dimension getTextSize(String text, Graphics2D g) {
		Dimension size = new Dimension();
		if (!text.isEmpty()) {
			Rectangle2D bounds = getTextBounds(text, g);
			size.setSize(bounds.getWidth() + 1.5, bounds.getHeight() + 1.0);
		}
		return size;
	}

	protected static Rectangle2D getTextBounds(String text, Graphics2D g) {
		return g.getFont().createGlyphVector(g.getFontRenderContext(), text).getVisualBounds();
	}

	protected double deriveRelativeBaselineHeight(Font font) {
		Graphics2D g = createCompatibleGraphics(font);
		FontMetrics fm = g.getFontMetrics();
		double ry = Math.min(fm.getMaxAscent() / (double) fm.getHeight(), 1.0);
		g.dispose();
		return ry;
	}

	public void refreshUI() {
		if (isShowing())
			repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		initGraphics(g2);
		clipBorder(g2);
		paintBackground(g2);
		paintText(g2);
		g2.dispose();
	}

	private void paintBackground(Graphics2D g) {
		Color bg = getBackground();
		if (bg != null) {
			g.setColor(bg);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	private void paintText(Graphics2D g) {
		if (!getText().isEmpty()) {
			Insets insets = getInsets();
			int width = getWidth() - insets.left - insets.right;
			int height = getHeight() - insets.top - insets.bottom;
			Graphics2D g2 = (Graphics2D) g.create(insets.left, insets.top, width, height);
			g2.setFont(getFont());
			g2.setColor(getForeground());
			paintText(g2, width, height);
			g2.dispose();
		}
	}

	private void paintText(Graphics2D g, int width, int height) {
		String text = getText();
		boolean op = isTextOverflowPossible();
		if (op && isTextOverflowModeAbbreviation()) {
			text = getAbbreviatedTextToFit(g, text, width, height);
		}
		AffineTransform transform = createTextTransform(g, text, width, height);
		paintText(g, transform, text);
		if (op && isTextOverflowModeFade() && ColorUtils.isFullyOpaque(getBackground())) {
			paintOverflowFades(g, transform, text, width, height);
		}
	}

	private void paintText(Graphics2D g, AffineTransform transform, String text) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.transform(transform);
		g2.drawGlyphVector(g.getFont().createGlyphVector(g.getFontRenderContext(), text), 0.5f, 0.5f);
		g2.dispose();
	}

	private void paintOverflowFades(Graphics2D g, AffineTransform transform, String text, int width, int height) {
		Insets overflow = getTextOverflow(g, transform, text, width, height, getTextOverflowThresholdInPixels(), null);
		if (overflow.left > 0) {
			int fadeWidth = getFadeWidthLeft(g, transform, width, overflow);
			if (fadeWidth > 0) {
				Graphics2D g2 = (Graphics2D) g.create(0, 0, fadeWidth, height);
				paintOverflowFadeAsLeading(g2, fadeWidth, height);
				g2.dispose();
			}
		}
		if (overflow.right > 0) {
			int fadeWidth = getFadeWidthRight(g, transform, width, overflow);
			if (fadeWidth > 0) {
				Graphics2D g2 = (Graphics2D) g.create(width - fadeWidth, 0, fadeWidth, height);
				g2.translate(fadeWidth, 0);
				g2.scale(-1.0, 1.0);
				paintOverflowFadeAsLeading(g2, fadeWidth, height);
				g2.dispose();
			}
		}
	}

	private void paintOverflowFadeAsLeading(Graphics2D g, int fadeWidth, int height) {
		g.setComposite(AlphaComposite.SrcOver);
		g.drawImage(getPristineLeadingFadeImage(), 0, 0, fadeWidth, height, null);
	}

	protected Image createPristineLeadingFadeImage(Color fadeColor) {
		Color c1 = fadeColor;
		Color c2 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 0);
		GradientFunction gf = GradientImageFactory.createSigmoidGradientFunction(0.5, 2.0);
		Dimension size = new Dimension(absoluteMaximumFadeWidth, 1);
		return GradientImageFactory.createLeftToRightGradientImage(size, c1, c2, gf);
	}

	protected int getFadeWidthLeft(Graphics2D g, AffineTransform transform, int width, Insets overflow) {
		return getFadeWidth(g, transform, width, overflow);
	}

	protected int getFadeWidthRight(Graphics2D g, AffineTransform transform, int width, Insets overflow) {
		return getFadeWidth(g, transform, width, overflow);
	}

	protected int getFadeWidth(Graphics2D g, AffineTransform transform, int width, Insets overflow) {
		int w = (int) Math.ceil(getTextSize(getFadeWidthReferenceString(), g).getWidth() * transform.getScaleX());
		int maxw = getMaximumFadeWidth(g, transform, width, overflow);
		return Math.min(w, maxw);
	}

	protected int getMaximumFadeWidth(Graphics2D g, AffineTransform transform, int width, Insets overflow) {
		int maxw = width;
		if (overflow.left > 0 && overflow.right > 0) {
			maxw = width / 2;
		}
		return Math.min(maxw, absoluteMaximumFadeWidth);
	}

	protected String getAbbreviatedTextToFit(Graphics2D g, String text, int width, int height) {
		String str;
		StringBuilder sb = new StringBuilder(text.length() + 6);
		TextOverflowMode mode = getResolvedTextOverflowMode();
		Insets overflow = null;
		int t = getTextOverflowThresholdInPixels();
		int i0 = 0, i1 = text.length();
		boolean emptyText, overflows;
		do {
			sb.setLength(0);
			if (i0 > 0) {
				sb.append(getTextOverflowAbbreviation());
			}
			emptyText = i1 == i0;
			if (!emptyText) {
				sb.append(text.substring(i0, i1));
			}
			if (i1 < text.length() && (!emptyText || i0 == 0)) {
				sb.append(getTextOverflowAbbreviation());
			}
			str = sb.toString();
			overflow = getTextOverflow(g, null, str, width, height, t, overflow);
			overflows = overflow.left + overflow.right > 0;
			if (overflows) {
				if (TextOverflowMode.ABBREVIATE_LEADING.equals(mode)) {
					i0++;
				} else if (TextOverflowMode.ABBREVIATE_TRAILING.equals(mode)) {
					i1--;
				} else {
					if ((text.length() - str.length()) % 2 == 0) {
						i1--;
					} else {
						i0++;
					}
				}
			}
			t = 0; // ensure abbreviated text fits regardless of threshold
		} while (overflows && !emptyText);
		return str;
	}

	private Insets getTextOverflow(Graphics2D g, AffineTransform transform, String text, int width, int height,
			int overflowThreshold, Insets overflow) {
		if (overflow == null)
			overflow = new Insets(0, 0, 0, 0);
		if (transform == null)
			transform = createTextTransform(g, text, width, height);
		Rectangle bounds = transform.createTransformedShape(getTextBounds(text, g)).getBounds();
		overflow.left = Math.max(-bounds.x - overflowThreshold, 0);
		overflow.right = Math.max(bounds.x + bounds.width - (width - 1) - overflowThreshold, 0);
		overflow.top = Math.max(-bounds.y - overflowThreshold, 0);
		overflow.bottom = Math.max(bounds.y + bounds.height - (height - 1) - overflowThreshold, 0);
		return overflow;
	}

	protected AffineTransform createTextTransform(Graphics2D g, String text, int width, int height) {
		AffineTransform transform = new AffineTransform();
		FillMode mode = getFillMode();
		Dimension textSize = getTextSize(text, g);
		double tw = textSize.getWidth();
		double th = textSize.getHeight();
		if (tw > 0 && th > 0) {
			Rectangle2D bounds = getTextBounds(text, g);
			double dx = -bounds.getMinX(), dy = -bounds.getMinY(), sx = 1.0, sy = 1.0;
			if (FillMode.STRETCH.equals(mode)) {
				sx = width / tw;
				sy = height / th;
			} else {
				if (FillMode.FIT.equals(mode) || FillMode.FIT_DOWNSCALE.equals(mode)
						|| FillMode.FIT_UPSCALE.equals(mode)) {
					sx = width / tw;
					sy = height / th;
					if (VerticalTextAlignment.BASELINE.equals(getVerticalAlignment())) {
						double above = getRelativeBaselineHeight() * height;
						double under = height - above;
						sy = above / -bounds.getMinY();
						if (bounds.getMaxY() > 0) {
							sy = Math.min(sy, under / bounds.getMaxY());
						}
					}
					double s = Math.min(sx, sy);
					if (FillMode.FIT_DOWNSCALE.equals(mode)) {
						s = Math.min(s, 1.0);
					} else if (FillMode.FIT_UPSCALE.equals(mode)) {
						s = Math.max(s, 1.0);
					}
					sx = sy = s;
				}
				if (HorizontalAlignment.CENTER.equals(getHorizontalAlignment())) {
					dx += (width - tw * sx) / 2 / sx;
				} else if (HorizontalAlignment.RIGHT.equals(getHorizontalAlignment())) {
					dx += (width - tw * sx) / sx;
				}
				if (VerticalTextAlignment.CENTER.equals(getVerticalAlignment())) {
					dy += (height - th * sy) / 2 / sy;
				} else if (VerticalTextAlignment.BOTTOM.equals(getVerticalAlignment())) {
					dy += (height - th * sy) / sy;
				} else if (VerticalTextAlignment.BASELINE.equals(getVerticalAlignment())) {
					dy = getRelativeBaselineHeight() * height / sy;
				}
			}
			transform.scale(sx, sy);
			transform.translate(dx, dy);
		}
		return transform;
	}

	private void clipBorder(Graphics2D g) {
		Border border = getBorder();
		if (border instanceof ClippingBorder) {
			g.clip(((ClippingBorder) border).getInteriorClip(this, g, 0, 0, getWidth(), getHeight()));
		}
	}

	@Override
	public Insets getInsets() {
		Insets fixed = getFixedInsets();
		if (fixed != null) {
			return fixed;
		} else {
			return super.getInsets();
		}
	}

	@Override
	public Insets getInsets(Insets insets) {
		Insets fixed = getFixedInsets();
		if (fixed != null) {
			if (insets != null) {
				insets.set(fixed.top, fixed.left, fixed.bottom, fixed.right);
				return insets;
			} else {
				return fixed;
			}
		} else {
			return super.getInsets(insets);
		}
	}

	@Override
	public void setBackground(Color bg) {
		setOpaque(bg != null);
		setPristineLeadingFadeImage(null); // invalidate
		super.setBackground(bg); // calls repaint
	}

	protected boolean isTextOverflowPossible() {
		if (getText().isEmpty()) {
			return false;
		} else {
			FillMode mode = getFillMode();
			return FillMode.NONE.equals(mode) || FillMode.FIT_UPSCALE.equals(mode);
		}
	}

	protected boolean isTextOverflowModeFade() {
		return TextOverflowMode.FADE.equals(getTextOverflowMode());
	}

	protected boolean isTextOverflowModeAbbreviation() {
		TextOverflowMode mode = getResolvedTextOverflowMode();
		return TextOverflowMode.ABBREVIATE_LEADING.equals(mode) || TextOverflowMode.ABBREVIATE_TRAILING.equals(mode)
				|| TextOverflowMode.ABBREVIATE_BOTH_ENDS.equals(mode);
	}

	protected TextOverflowMode getResolvedTextOverflowMode() {
		TextOverflowMode mode = getTextOverflowMode();
		if (TextOverflowMode.ABBREVIATE_AUTO.equals(mode)) {
			if (HorizontalAlignment.RIGHT.equals(getHorizontalAlignment())) {
				mode = TextOverflowMode.ABBREVIATE_LEADING;
			} else {
				mode = TextOverflowMode.ABBREVIATE_TRAILING;
			}
		}
		return mode;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		refreshUI();
	}

	public FillMode getFillMode() {
		return fillMode;
	}

	public void setFillMode(FillMode fillMode) {
		if (fillMode == null)
			throw new NullPointerException("fill mode is null");
		this.fillMode = fillMode;
		refreshUI();
	}

	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(HorizontalAlignment hAlign) {
		if (hAlign == null)
			throw new NullPointerException("horizontal alignment is null");
		this.horizontalAlignment = hAlign;
		refreshUI();
	}

	public VerticalTextAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	public void setVerticalAlignment(VerticalTextAlignment vAlign) {
		if (vAlign == null)
			throw new NullPointerException("vertical alignment is null");
		this.verticalAlignment = vAlign;
		refreshUI();
	}

	public double getRelativeBaselineHeight() {
		if (relativeBaselineHeight < 0) {
			relativeBaselineHeight = deriveRelativeBaselineHeight(getFont());
		}
		return relativeBaselineHeight;
	}

	public void setRelativeBaselineHeight(double ry) {
		if (ry < 0 || ry > 1.0)
			throw new IllegalArgumentException("relative baseline height must be between 0 and 1 (" + ry + ")");
		this.relativeBaselineHeight = ry;
		refreshUI();
	}

	private Insets getFixedInsets() {
		return fixedInsets;
	}

	protected void setFixedInsets(Insets insets) {
		this.fixedInsets = insets;
	}

	public TextOverflowMode getTextOverflowMode() {
		return textOverflowMode;
	}

	public void setTextOverflowMode(TextOverflowMode mode) {
		if (mode == null)
			throw new NullPointerException("text overflow mode is null");
		this.textOverflowMode = mode;
		refreshUI();
	}

	public int getTextOverflowThresholdInPixels() {
		return textOverflowThresholdInPixels;
	}

	public void setTextOverflowThresholdInPixels(int thresholdInPixels) {
		this.textOverflowThresholdInPixels = thresholdInPixels;
		refreshUI();
	}

	public String getTextOverflowAbbreviation() {
		return textOverflowAbbreviation;
	}

	public void setTextOverflowAbbreviation(String abbreviation) {
		if (abbreviation == null)
			throw new NullPointerException("abbreviation is null");
		this.textOverflowAbbreviation = abbreviation;
		refreshUI();
	}

	public String getFadeWidthReferenceString() {
		return fadeWidthReferenceString;
	}

	public void setFadeWidthReferenceString(String referenceStr) {
		if (referenceStr == null)
			throw new NullPointerException("reference string is null");
		this.fadeWidthReferenceString = referenceStr;
	}

	private Image getPristineLeadingFadeImage() {
		if (pristineLeadingFadeImage == null) {
			pristineLeadingFadeImage = createPristineLeadingFadeImage(getBackground());
		}
		return pristineLeadingFadeImage;
	}

	private void setPristineLeadingFadeImage(Image image) {
		this.pristineLeadingFadeImage = image;
	}

	public static enum TextOverflowMode {

		CLIP,

		FADE,

		ABBREVIATE_AUTO,

		ABBREVIATE_LEADING,

		ABBREVIATE_TRAILING,

		ABBREVIATE_BOTH_ENDS;

	}

}