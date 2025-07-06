package org.maia.swing.animate.itemslide.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.maia.swing.animate.itemslide.SlidingCursor;
import org.maia.swing.animate.itemslide.SlidingItemListComponent;

public class SlidingCursorFactory {

	private SlidingCursorFactory() {
	}

	public static SlidingCursor createSolidOutlineCursor(Color color, int thickness) {
		return createSolidOutlineCursor(color, thickness, 0);
	}

	public static SlidingCursor createSolidOutlineCursor(Color color, int thickness, int spacing) {
		return createSolidOutlineCursor(color, thickness, spacing, false);
	}

	public static SlidingCursor createSolidOutlineCursor(Color color, int thickness, int spacing,
			boolean roundedCorners) {
		return new SolidOutlineCursor(color, thickness, spacing, roundedCorners);
	}

	public static class SolidOutlineCursor implements SlidingCursor {

		private Color color;

		private int thickness;

		private int spacing;

		private boolean roundedCorners;

		public SolidOutlineCursor(Color color, int thickness, int spacing) {
			this(color, thickness, spacing, false);
		}

		public SolidOutlineCursor(Color color, int thickness, int spacing, boolean roundedCorners) {
			this.color = color;
			this.thickness = thickness;
			this.spacing = spacing;
			this.roundedCorners = roundedCorners;
		}

		@Override
		public void renderUnderItems(Graphics2D g, Rectangle innerBounds, SlidingItemListComponent component) {
			// nothing
		}

		@Override
		public void renderAboveItems(Graphics2D g, Rectangle innerBounds, SlidingItemListComponent component) {
			g.setColor(getSlidingColor(component));
			if (isRoundedCorners()) {
				renderRoundedCorners(g, innerBounds);
			} else {
				renderSquareCorners(g, innerBounds);
			}
		}

		protected void renderRoundedCorners(Graphics2D g, Rectangle innerBounds) {
			int w = innerBounds.width;
			int h = innerBounds.height;
			int s = getSpacing();
			int t = getThickness();
			g.fillArc(-s - t, -s - t, 2 * t, 2 * t, 90, 90);
			g.fillArc(w + s - t, -s - t, 2 * t, 2 * t, 0, 90);
			g.fillArc(-s - t, h + s - t, 2 * t, 2 * t, 180, 90);
			g.fillArc(w + s - t, h + s - t, 2 * t, 2 * t, 270, 90);
			g.fillRect(-s - t, -s, t, h + 2 * s);
			g.fillRect(w + s, -s, t, h + 2 * s);
			g.fillRect(-s, -s - t, w + 2 * s, t);
			g.fillRect(-s, h + s, w + 2 * s, t);
		}

		protected void renderSquareCorners(Graphics2D g, Rectangle innerBounds) {
			for (int i = 0; i < getThickness(); i++) {
				int j = 1 + i + getSpacing();
				g.drawRect(-j, -j, innerBounds.width + 2 * j - 1, innerBounds.height + 2 * j - 1);
			}
		}

		protected Color getSlidingColor(SlidingItemListComponent component) {
			Color c = getColor();
			if (component.isSliding()) {
				int alpha = 100 + (int) Math.round(155 * (1.0 - component.getSlidingSpeed()));
				c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
			}
			return c;
		}

		public Color getColor() {
			return color;
		}

		public int getThickness() {
			return thickness;
		}

		public int getSpacing() {
			return spacing;
		}

		public boolean isRoundedCorners() {
			return roundedCorners;
		}

	}

}