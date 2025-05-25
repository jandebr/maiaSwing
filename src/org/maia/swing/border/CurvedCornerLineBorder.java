package org.maia.swing.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

public abstract class CurvedCornerLineBorder implements ClippingBorder {

	private Color color;

	private float thickness;

	protected CurvedCornerLineBorder(Color color) {
		this(color, 1f);
	}

	protected CurvedCornerLineBorder(Color color, float thickness) {
		this.color = color;
		this.thickness = thickness;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		if (getColor() != null && getThickness() > 0f) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor(getColor());
			g2.setStroke(new BasicStroke(getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.draw(getStrokeOutline(c, g, x, y, width, height));
			g2.dispose();
		}
	}

	@Override
	public Shape getInteriorClip(Component c, Graphics g, int x, int y, int width, int height) {
		return getStrokeOutline(c, g, x, y, width, height);
	}

	private RoundRectangle2D getStrokeOutline(Component c, Graphics g, int x, int y, int width, int height) {
		float t = getThickness();
		float t2 = t / 2;
		float o = isIntegerThickness() ? 0.5f : 0f; // raster offset
		float aw = 2f * getCornerWidth(c, g, x, y, width, height);
		float ah = 2f * getCornerHeight(c, g, x, y, width, height);
		return new RoundRectangle2D.Float(x + t2 - o, y + t2 - o, width - t + o, height - t + o, aw - t2, ah - t2);
	}

	protected abstract float getCornerWidth(Component c, Graphics g, int x, int y, int width, int height);

	protected abstract float getCornerHeight(Component c, Graphics g, int x, int y, int width, int height);

	private boolean isIntegerThickness() {
		return getThickness() == Math.floor(getThickness());
	}

	@Override
	public Insets getBorderInsets(Component c) {
		int t = (int) Math.floor(getThickness());
		return new Insets(t, t, t, t);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	public Color getColor() {
		return color;
	}

	public float getThickness() {
		return thickness;
	}

	public static class Absolute extends CurvedCornerLineBorder {

		private float cornerWidth;

		private float cornerHeight;

		public Absolute(Color color, float cornerSize) {
			this(color, cornerSize, cornerSize);
		}

		public Absolute(Color color, float cornerWidth, float cornerHeight) {
			this(color, cornerWidth, cornerHeight, 1f);
		}

		public Absolute(Color color, float cornerWidth, float cornerHeight, float thickness) {
			super(color, thickness);
			this.cornerWidth = cornerWidth;
			this.cornerHeight = cornerHeight;
		}

		@Override
		protected float getCornerWidth(Component c, Graphics g, int x, int y, int width, int height) {
			return getCornerWidth();
		}

		@Override
		protected float getCornerHeight(Component c, Graphics g, int x, int y, int width, int height) {
			return getCornerHeight();
		}

		public float getCornerWidth() {
			return cornerWidth;
		}

		public float getCornerHeight() {
			return cornerHeight;
		}

	}

	private static abstract class Relative extends CurvedCornerLineBorder {

		private float cornerRelativeWidth; // between 0 (square corner) and 1 (fully curved)

		private float cornerRelativeHeight; // between 0 (square corner) and 1 (fully curved)

		protected Relative(Color color, float cornerRelativeWidth, float cornerRelativeHeight, float thickness) {
			super(color, thickness);
			this.cornerRelativeWidth = Math.max(Math.min(cornerRelativeWidth, 1f), 0f);
			this.cornerRelativeHeight = Math.max(Math.min(cornerRelativeHeight, 1f), 0f);
		}

		public float getCornerRelativeWidth() {
			return cornerRelativeWidth;
		}

		public float getCornerRelativeHeight() {
			return cornerRelativeHeight;
		}

	}

	public static class Elliptic extends Relative {

		public Elliptic(Color color, float cornerRelativeWidth, float cornerRelativeHeight) {
			this(color, cornerRelativeWidth, cornerRelativeHeight, 1f);
		}

		public Elliptic(Color color, float cornerRelativeWidth, float cornerRelativeHeight, float thickness) {
			super(color, cornerRelativeWidth, cornerRelativeHeight, thickness);
		}

		@Override
		protected float getCornerWidth(Component c, Graphics g, int x, int y, int width, int height) {
			return getCornerRelativeWidth() * width / 2f;
		}

		@Override
		protected float getCornerHeight(Component c, Graphics g, int x, int y, int width, int height) {
			return getCornerRelativeHeight() * height / 2f;
		}

	}

	public static class Circular extends Relative {

		public Circular(Color color, float cornerRelativeSize) {
			this(color, cornerRelativeSize, 1f);
		}

		public Circular(Color color, float cornerRelativeSize, float thickness) {
			super(color, cornerRelativeSize, cornerRelativeSize, thickness);
		}

		@Override
		protected float getCornerWidth(Component c, Graphics g, int x, int y, int width, int height) {
			return getCornerRelativeSize() * Math.min(width, height) / 2f;
		}

		@Override
		protected float getCornerHeight(Component c, Graphics g, int x, int y, int width, int height) {
			return getCornerWidth(c, g, x, y, width, height);
		}

		public float getCornerRelativeSize() {
			return getCornerRelativeWidth();
		}

	}

}