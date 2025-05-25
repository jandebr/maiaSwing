package org.maia.swing.animate.imageslide.path;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import org.maia.swing.animate.imageslide.SlidingImageState;

public abstract class AbstractPathGenerator implements SlidingImagePathGenerator {

	private Dimension imageSize;

	private Dimension viewportSize;

	protected AbstractPathGenerator(Dimension imageSize, Dimension viewportSize) {
		this.imageSize = imageSize;
		this.viewportSize = viewportSize;
	}

	protected Rectangle2D getViewportCenterBoundsInsideImage(double angleInRadians, double zoomFactor) {
		Rectangle2D vp = getTransformedViewportBounds(angleInRadians, zoomFactor);
		double x1 = -vp.getMinX();
		double y1 = -vp.getMinY();
		double x2 = getImageSize().getWidth() - vp.getMaxX();
		double y2 = getImageSize().getHeight() - vp.getMaxY();
		return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
	}

	private Rectangle2D getTransformedViewportBounds(double angleInRadians, double zoomFactor) {
		SlidingImageState state = new SlidingImageState();
		state.setAngleInRadians(angleInRadians);
		state.setZoomFactor(zoomFactor);
		return state.getInverseTransform().createTransformedShape(getNormalizedViewportBounds()).getBounds2D();
	}

	private Rectangle2D getNormalizedViewportBounds() {
		double w = getViewportSize().getWidth();
		double h = getViewportSize().getHeight();
		return new Rectangle2D.Double(-w / 2, -h / 2, w, h);
	}

	public Dimension getImageSize() {
		return imageSize;
	}

	public Dimension getViewportSize() {
		return viewportSize;
	}

}