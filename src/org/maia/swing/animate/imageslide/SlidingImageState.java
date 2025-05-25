package org.maia.swing.animate.imageslide;

import java.awt.geom.AffineTransform;

public class SlidingImageState implements Cloneable {

	private double centerX;

	private double centerY;

	private double angleInRadians;

	private double zoomFactor = 1.0;

	public SlidingImageState() {
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SlidingImageState[c=(");
		builder.append(toStringRound(getCenterX()));
		builder.append(",");
		builder.append(toStringRound(getCenterY()));
		builder.append("), a=");
		builder.append(toStringRound(getAngleInRadians()));
		builder.append(", z=");
		builder.append(toStringRound(getZoomFactor()));
		builder.append("]");
		return builder.toString();
	}

	private static double toStringRound(double v) {
		return Math.round(v * 10.0) / 10.0;
	}

	@Override
	public SlidingImageState clone() {
		SlidingImageState clone = new SlidingImageState();
		clone.setCenterX(getCenterX());
		clone.setCenterY(getCenterY());
		clone.setAngleInRadians(getAngleInRadians());
		clone.setZoomFactor(getZoomFactor());
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (!(obj instanceof SlidingImageState)) {
			return false;
		} else {
			SlidingImageState other = (SlidingImageState) obj;
			return getCenterX() == other.getCenterX() && getCenterY() == other.getCenterY()
					&& getAngleInRadians() == other.getAngleInRadians() && getZoomFactor() == other.getZoomFactor();
		}
	}

	public SlidingImageState createTranslationOverAngle(double distance, double theta) {
		double tx = distance * Math.cos(theta);
		double ty = -distance * Math.sin(theta);
		return createTranslation(tx, ty);
	}

	public SlidingImageState createTranslation(double tx, double ty) {
		SlidingImageState state = clone();
		state.setCenterX(state.getCenterX() + tx);
		state.setCenterY(state.getCenterY() + ty);
		return state;
	}

	public SlidingImageState createRotation(double theta) {
		SlidingImageState state = clone();
		state.setAngleInRadians(state.getAngleInRadians() + theta);
		return state;
	}

	public SlidingImageState createZoom(double zoomFactor) {
		SlidingImageState state = clone();
		state.setZoomFactor(state.getZoomFactor() * zoomFactor);
		return state;
	}

	public SlidingImageState createInterpolation(SlidingImageState other, double r) {
		if (r == 0) {
			return this;
		} else if (r == 1.0) {
			return other;
		} else {
			double r1 = 1.0 - r;
			SlidingImageState state = new SlidingImageState();
			state.setCenterX(r1 * getCenterX() + r * other.getCenterX());
			state.setCenterY(r1 * getCenterY() + r * other.getCenterY());
			state.setAngleInRadians(r1 * getAngleInRadians() + r * other.getAngleInRadians());
			state.setZoomFactor(r1 * getZoomFactor() + r * other.getZoomFactor());
			return state;
		}
	}

	public AffineTransform getTransform() {
		AffineTransform t = new AffineTransform();
		if (getZoomFactor() != 1.0) {
			t.scale(getZoomFactor(), getZoomFactor());
		}
		if (getAngleInRadians() != 0) {
			t.rotate(getAngleInRadians());
		}
		t.translate(-getCenterX(), -getCenterY());
		return t;
	}

	public AffineTransform getInverseTransform() {
		AffineTransform t = new AffineTransform();
		t.translate(getCenterX(), getCenterY());
		if (getAngleInRadians() != 0) {
			t.rotate(-getAngleInRadians());
		}
		if (getZoomFactor() != 1.0) {
			double s = 1.0 / getZoomFactor();
			t.scale(s, s);
		}
		return t;
	}

	public void setCenter(double centerX, double centerY) {
		setCenterX(centerX);
		setCenterY(centerY);
	}

	public double getCenterX() {
		return centerX;
	}

	public void setCenterX(double centerX) {
		this.centerX = centerX;
	}

	public double getCenterY() {
		return centerY;
	}

	public void setCenterY(double centerY) {
		this.centerY = centerY;
	}

	public double getAngleInRadians() {
		return angleInRadians;
	}

	public void setAngleInRadians(double angleInRadians) {
		this.angleInRadians = angleInRadians;
	}

	public double getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(double zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

}