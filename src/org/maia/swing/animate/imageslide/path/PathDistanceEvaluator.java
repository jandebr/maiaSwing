package org.maia.swing.animate.imageslide.path;

import java.awt.Dimension;
import java.awt.Image;

public class PathDistanceEvaluator extends AbstractPathEvaluator {

	private double minimumDistanceInViewCoordinates;

	public PathDistanceEvaluator(Image image, Dimension viewportSize) {
		super(image, viewportSize);
		setMinimumDistanceInViewCoordinates(computeMinimumDistanceInViewCoordinates());
	}

	@Override
	public double evaluatePath(SlidingImagePath path) {
		if (path.getDistanceInViewCoordinates() >= getMinimumDistanceInViewCoordinates()) {
			return computePathScore(path);
		} else {
			return -1.0;
		}
	}

	private double computePathScore(SlidingImagePath path) {
		double min = getMinimumDistanceInViewCoordinates();
		double max = computeMaximumDistanceInViewCoordinates(path);
		return Math.min((path.getDistanceInViewCoordinates() - min) / (max - min), 1.0);
	}

	private double computeMaximumDistanceInViewCoordinates(SlidingImagePath path) {
		Dimension is = getImageSize();
		Dimension vs = getViewportSize();
		double zoom = path.getAverageZoomFactor();
		double maxx = zoom * is.getWidth() - vs.getWidth();
		double maxy = zoom * is.getHeight() - vs.getHeight();
		return Math.sqrt(maxx * maxx + maxy * maxy);
	}

	private double computeMinimumDistanceInViewCoordinates() {
		return (getViewportSize().getWidth() + getViewportSize().getHeight()) * 0.2;
	}

	public double getMinimumDistanceInViewCoordinates() {
		return minimumDistanceInViewCoordinates;
	}

	public void setMinimumDistanceInViewCoordinates(double distance) {
		this.minimumDistanceInViewCoordinates = distance;
	}

}