package org.maia.swing.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Shape;

public class CompoundClippingBorder implements ClippingBorder {

	private ClippingBorder outsideBorder;

	private ClippingBorder insideBorder;

	public CompoundClippingBorder(ClippingBorder outsideBorder, ClippingBorder insideBorder) {
		this.outsideBorder = outsideBorder;
		this.insideBorder = insideBorder;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Insets out = getOutsideBorder().getBorderInsets(c);
		getOutsideBorder().paintBorder(c, g, x, y, width, height);
		getInsideBorder().paintBorder(c, g, x + out.left, y + out.top, width - out.left - out.right,
				height - out.top - out.bottom);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		Insets out = getOutsideBorder().getBorderInsets(c);
		Insets in = getInsideBorder().getBorderInsets(c);
		return new Insets(out.top + in.top, out.left + in.left, out.bottom + in.bottom, out.right + in.right);
	}

	@Override
	public boolean isBorderOpaque() {
		return getOutsideBorder().isBorderOpaque() && getInsideBorder().isBorderOpaque();
	}

	@Override
	public Shape getInteriorClip(Component c, Graphics g, int x, int y, int width, int height) {
		Insets out = getOutsideBorder().getBorderInsets(c);
		return getInsideBorder().getInteriorClip(c, g, x + out.left, y + out.top, width - out.left - out.right,
				height - out.top - out.bottom);
	}

	public ClippingBorder getOutsideBorder() {
		return outsideBorder;
	}

	public ClippingBorder getInsideBorder() {
		return insideBorder;
	}

}