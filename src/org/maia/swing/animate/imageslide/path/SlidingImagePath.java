package org.maia.swing.animate.imageslide.path;

import org.maia.swing.animate.imageslide.SlidingImageState;

public class SlidingImagePath {

	private SlidingImageState startState;

	private SlidingImageState endState;

	private boolean insideImage;

	public SlidingImagePath(SlidingImageState startState, SlidingImageState endState, boolean insideImage) {
		this.startState = startState;
		this.endState = endState;
		this.insideImage = insideImage;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SlidingImagePath from ");
		builder.append(getStartState());
		builder.append(" to ");
		builder.append(getEndState());
		return builder.toString();
	}

	public double getDistanceInImageCoordinates() {
		double dx = getEndState().getCenterX() - getStartState().getCenterX();
		double dy = getEndState().getCenterY() - getStartState().getCenterY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	public double getDistanceInViewCoordinates() {
		return getAverageZoomFactor() * getDistanceInImageCoordinates();
	}

	public double getAverageAngleInRadians() {
		return (getStartState().getAngleInRadians() + getEndState().getAngleInRadians()) / 2.0;
	}

	public double getAverageZoomFactor() {
		return (getStartState().getZoomFactor() + getEndState().getZoomFactor()) / 2.0;
	}

	public SlidingImageState getStartState() {
		return startState;
	}

	public SlidingImageState getEndState() {
		return endState;
	}

	public boolean isInsideImage() {
		return insideImage;
	}

}