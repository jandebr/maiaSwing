package org.maia.swing.animate.imageslide.path;

import java.awt.Dimension;
import java.awt.Image;

import org.maia.graphics2d.image.ImageUtils;

public abstract class AbstractPathEvaluator implements SlidingImagePathEvaluator {

	private Image image;

	private Dimension viewportSize;

	protected AbstractPathEvaluator(Image image, Dimension viewportSize) {
		this.image = image;
		this.viewportSize = viewportSize;
	}

	public Dimension getImageSize() {
		return ImageUtils.getSize(getImage());
	}

	public Image getImage() {
		return image;
	}

	public Dimension getViewportSize() {
		return viewportSize;
	}

}