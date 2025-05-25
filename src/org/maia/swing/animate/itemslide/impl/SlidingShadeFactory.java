package org.maia.swing.animate.itemslide.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;

import org.maia.graphics2d.image.GradientImageFactory;
import org.maia.graphics2d.image.ImageUtils;
import org.maia.graphics2d.image.GradientImageFactory.GradientFunction;
import org.maia.swing.animate.itemslide.SlidingItemListComponent;
import org.maia.swing.animate.itemslide.SlidingShade;

public class SlidingShadeFactory {

	private SlidingShadeFactory() {
	}

	public static SlidingShade createGradientShade(SlidingItemListComponent component) {
		return createGradientShadeRelativeLength(component, 0.1);
	}

	public static SlidingShade createGradientShadeRelativeLength(SlidingItemListComponent component,
			double targetLengthRatio) {
		return createGradientShadeRelativeLength(component, targetLengthRatio, 20, 200);
	}

	public static SlidingShade createGradientShadeRelativeLength(SlidingItemListComponent component,
			double targetLengthRatio, int minLength, int maxLength) {
		int vp = component.isHorizontalLayout() ? component.getViewportWidth() : component.getViewportHeight();
		int length = Math.min(Math.max(Math.min((int) Math.round(vp * targetLengthRatio), maxLength), minLength),
				vp / 2);
		return createGradientShadeAbsoluteLength(component, length);
	}

	public static SlidingShade createGradientShadeAbsoluteLength(SlidingItemListComponent component, int length) {
		Color c1 = component.getBackground();
		Color c2 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 0);
		GradientFunction gf = GradientImageFactory.createSigmoidGradientFunction(0.5, 2.0);
		if (component.isHorizontalLayout()) {
			return new SlidingShadeImage(GradientImageFactory
					.createLeftToRightGradientImage(new Dimension(length, component.getViewportHeight()), c1, c2, gf));
		} else {
			return new SlidingShadeImage(GradientImageFactory
					.createTopToBottomGradientImage(new Dimension(component.getViewportWidth(), length), c1, c2, gf));
		}
	}

	private static class SlidingShadeImage implements SlidingShade {

		private Image leadingImage;

		public SlidingShadeImage(Image leadingImage) {
			this.leadingImage = leadingImage;
		}

		@Override
		public void renderAsLeading(Graphics2D g, int width, int height, SlidingItemListComponent component) {
			g.drawImage(getLeadingImage(), 0, 0, width, height, null);
		}

		@Override
		public int getWidth() {
			return ImageUtils.getWidth(getLeadingImage());
		}

		@Override
		public int getHeight() {
			return ImageUtils.getHeight(getLeadingImage());
		}

		public Image getLeadingImage() {
			return leadingImage;
		}

	}

}