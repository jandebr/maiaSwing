package org.maia.swing.animate.textslide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;

import javax.swing.border.Border;

import org.maia.swing.SwingUtils;
import org.maia.swing.animate.BaseAnimatedComponent;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.swing.text.TextLabel;
import org.maia.swing.text.TextLabel.TextOverflowMode;
import org.maia.swing.text.VerticalTextAlignment;

public class SlidingTextLabel extends BaseAnimatedComponent {

	private AnimatedTextLabel textLabel;

	private double slidingSpeed = 50.0; // in pixels per second, on average

	private int slidingOvershoot = 4; // translation overshoot, in pixels

	private double minimumTranslation;

	private double maximumTranslation;

	private double translationRadialCoord; // translation coordinate, in radials

	private double translationRadialVelocity; // in radials per second, derived from 'slidingSpeed'

	private long suspensionAtEndsMillis = 500L; // translation suspension at either ends, in milliseconds

	private SlidingTextLabel(AnimatedTextLabel textLabel) {
		super(textLabel.getSize(), textLabel.getBackground());
		setTextLabel(textLabel);
		setFadedTextOverflow(true);
		updateSlidingBounds();
		resetTranslationRadialCoord();
	}

	public static SlidingTextLabel createSized(String text, Font font, Dimension size, Color background,
			Color textColor) {
		return createSized(text, font, size, background, textColor, HorizontalAlignment.CENTER,
				VerticalTextAlignment.CENTER);
	}

	public static SlidingTextLabel createSized(String text, Font font, Dimension size, Color background,
			Color textColor, HorizontalAlignment hAlignment, VerticalTextAlignment vAlignment) {
		AnimatedTextLabel textLabel = new AnimatedTextLabel(text, font, size, background, textColor);
		textLabel.setHorizontalAlignment(hAlignment);
		textLabel.setVerticalAlignment(vAlignment);
		return new SlidingTextLabel(textLabel);
	}

	public static SlidingTextLabel createLine(String text, Font font, int lineWidth, HorizontalAlignment hAlignment,
			Color background, Color textColor) {
		return createLine(text, font, lineWidth, hAlignment, background, textColor, null);
	}

	public static SlidingTextLabel createLine(String text, Font font, int lineWidth, HorizontalAlignment hAlignment,
			Color background, Color textColor, Insets insets) {
		if (lineWidth <= 0)
			throw new IllegalArgumentException("Line width should be strictly positive (" + lineWidth + ")");
		int width = lineWidth;
		if (insets != null) {
			width += insets.left + insets.right;
		}
		int height = TextLabel.getLineHeight(font);
		if (insets != null) {
			height += insets.top + insets.bottom;
		}
		Dimension size = new Dimension(width, height);
		AnimatedTextLabel textLabel = new AnimatedTextLabel(text, font, size, background, textColor);
		textLabel.setHorizontalAlignment(hAlignment);
		textLabel.setVerticalAlignment(VerticalTextAlignment.BASELINE);
		textLabel.setFixedInsets(insets);
		return new SlidingTextLabel(textLabel);
	}

	@Override
	protected AnimatedPanel createAnimatedPanel(Dimension size, Color background) {
		return new SlidingTextPanel(size, background);
	}

	private void updateSlidingBounds() {
		Insets insets = getTextLabel().getInsets();
		int width = getTextLabel().getWidth() - insets.left - insets.right;
		double minT = 0, maxT = 0;
		double overflow = Math.max(getTextLabel().getTextWidth() - width, 0);
		if (overflow > 0) {
			int overshoot = getSlidingOvershoot();
			setTranslationRadialVelocity(Math.PI * getSlidingSpeed() / overflow);
			HorizontalAlignment hAlign = getTextLabel().getHorizontalAlignment();
			if (HorizontalAlignment.LEFT.equals(hAlign)) {
				minT = -overflow - overshoot;
			} else if (HorizontalAlignment.RIGHT.equals(hAlign)) {
				maxT = overflow + overshoot;
			} else if (HorizontalAlignment.CENTER.equals(hAlign)) {
				minT = -overflow / 2.0 - overshoot;
				maxT = -minT;
			}
		} else {
			setTranslationRadialVelocity(0);
		}
		setMinimumTranslation(minT);
		setMaximumTranslation(maxT);
	}

	private void resetTranslationRadialCoord() {
		double c = 0;
		HorizontalAlignment hAlign = getTextLabel().getHorizontalAlignment();
		if (HorizontalAlignment.LEFT.equals(hAlign)) {
			c = Math.PI / 2.0;
		} else if (HorizontalAlignment.RIGHT.equals(hAlign)) {
			double cs = getSuspensionAtEndsMillis() / 1000.0 * getTranslationRadialVelocity();
			c = Math.PI * 1.5 + cs;
		} else if (HorizontalAlignment.CENTER.equals(hAlign)) {
			c = 0;
		}
		setTranslationRadialCoord(c);
	}

	@Override
	public boolean isAnimating() {
		return getMinimumTranslation() != 0 || getMaximumTranslation() != 0;
	}

	public void setForeground(Color color) {
		getTextLabel().setForeground(color);
		refreshUI();
	}

	@Override
	public void setBackground(Color color) {
		getTextLabel().setBackground(color);
		super.setBackground(color);
	}

	public void setBorder(Border border) {
		getTextLabel().setBorder(border);
		updateSlidingBounds(); // insets may have changed
		refreshUI();
	}

	public void setText(String text) {
		getTextLabel().setText(text);
		updateSlidingBounds();
		refreshUI();
	}

	private AnimatedTextLabel getTextLabel() {
		return textLabel;
	}

	private void setTextLabel(AnimatedTextLabel textLabel) {
		this.textLabel = textLabel;
	}

	public boolean isFadedTextOverflow() {
		return TextOverflowMode.FADE.equals(getTextLabel().getTextOverflowMode());
	}

	public void setFadedTextOverflow(boolean faded) {
		if (faded) {
			getTextLabel().setTextOverflowMode(TextOverflowMode.FADE);
		} else {
			getTextLabel().setTextOverflowMode(TextOverflowMode.CLIP);
		}
	}

	/**
	 * Gets the text sliding speed
	 * 
	 * @return The text sliding speed, in pixels per second (on average)
	 */
	public double getSlidingSpeed() {
		return slidingSpeed;
	}

	/**
	 * Sets the text sliding speed
	 * 
	 * @param speed
	 *            The text sliding speed, in pixels per second (on average)
	 */
	public void setSlidingSpeed(double speed) {
		if (speed < 0)
			throw new IllegalArgumentException("speed cannot be negative (" + speed + ")");
		this.slidingSpeed = speed;
		updateSlidingBounds();
		resetTranslationRadialCoord();
	}

	public int getSlidingOvershoot() {
		return slidingOvershoot;
	}

	public void setSlidingOvershoot(int overshoot) {
		if (overshoot < 0)
			throw new IllegalArgumentException("overshoot cannot be negative (" + overshoot + ")");
		this.slidingOvershoot = overshoot;
		updateSlidingBounds();
	}

	private double getMinimumTranslation() {
		return minimumTranslation;
	}

	private void setMinimumTranslation(double t) {
		this.minimumTranslation = t;
	}

	private double getMaximumTranslation() {
		return maximumTranslation;
	}

	private void setMaximumTranslation(double t) {
		this.maximumTranslation = t;
	}

	private double getTranslationRadialCoord() {
		return translationRadialCoord;
	}

	private void setTranslationRadialCoord(double c) {
		this.translationRadialCoord = c;
	}

	private double getTranslationRadialVelocity() {
		return translationRadialVelocity;
	}

	private void setTranslationRadialVelocity(double v) {
		this.translationRadialVelocity = v;
	}

	public long getSuspensionAtEndsMillis() {
		return suspensionAtEndsMillis;
	}

	public void setSuspensionAtEndsMillis(long millis) {
		if (millis < 0)
			throw new IllegalArgumentException("suspension time cannot be negative (" + millis + ")");
		this.suspensionAtEndsMillis = millis;
		resetTranslationRadialCoord();
	}

	@SuppressWarnings("serial")
	private class SlidingTextPanel extends AnimatedPanel {

		public SlidingTextPanel(Dimension size, Color background) {
			super(size, background);
		}

		@Override
		protected void updateStateBetweenPaints(Graphics2D g, double elapsedTimeMillis) {
			double t = 0;
			if (isAnimating()) {
				double v = getTranslationRadialVelocity();
				double c = getTranslationRadialCoord();
				double cs = getSuspensionAtEndsMillis() / 1000.0 * v;
				double cNew = c + v * elapsedTimeMillis / 1000.0;
				double cycle = 2 * (Math.PI + cs);
				while (cNew >= cycle)
					cNew -= cycle;
				setTranslationRadialCoord(cNew);
				double cNorm = 0;
				if (cNew <= Math.PI / 2.0) {
					cNorm = cNew;
				} else if (cNew <= Math.PI / 2.0 + cs) {
					cNorm = Math.PI / 2.0;
				} else if (cNew <= Math.PI * 1.5 + cs) {
					cNorm = cNew - cs;
				} else if (cNew <= Math.PI * 1.5 + 2 * cs) {
					cNorm = Math.PI * 1.5;
				} else {
					cNorm = cNew - 2 * cs;
				}
				double r = (Math.sin(cNorm) + 1.0) / 2.0;
				t = (1.0 - r) * getMinimumTranslation() + r * getMaximumTranslation();
			}
			getTextLabel().setTextTranslation(t);
		}

		@Override
		protected void doPaintComponent(Graphics2D g) {
			getTextLabel().paint(g);
		}

	}

	@SuppressWarnings("serial")
	private static class AnimatedTextLabel extends TextLabel {

		private double textTranslation;

		public AnimatedTextLabel(String text, Font font, Dimension size, Color background, Color textColor) {
			super(text, font, size);
			SwingUtils.fixSize(this, size);
			setBackground(background);
			setForeground(textColor);
			setTextOverflowThresholdInPixels(2);
			setFadeWidthReferenceString("m");
		}

		@Override
		protected AffineTransform createTextTransform(Graphics2D g, String text, int width, int height) {
			AffineTransform transform = super.createTextTransform(g, text, width, height);
			transform.translate(getTextTranslation(), 0);
			return transform;
		}

		@Override
		protected int getFadeWidthLeft(Graphics2D g, AffineTransform transform, int width, Insets overflow) {
			int fadeWidth = super.getFadeWidthLeft(g, transform, width, overflow);
			fadeWidth = Math.min(fadeWidth, overflow.left);
			return fadeWidth;
		}

		@Override
		protected int getFadeWidthRight(Graphics2D g, AffineTransform transform, int width, Insets overflow) {
			int fadeWidth = super.getFadeWidthRight(g, transform, width, overflow);
			fadeWidth = Math.min(fadeWidth, overflow.right);
			return fadeWidth;
		}

		public double getTextWidth() {
			Graphics2D g = createCompatibleGraphics(getFont());
			double width = getTextBounds(getText(), g).getWidth();
			g.dispose();
			return width;
		}

		@Override
		public void setFixedInsets(Insets insets) {
			super.setFixedInsets(insets);
		}

		public double getTextTranslation() {
			return textTranslation;
		}

		public void setTextTranslation(double t) {
			this.textTranslation = t;
		}

	}

}