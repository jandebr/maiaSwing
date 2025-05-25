package org.maia.swing.animate.imageslide.path;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import org.maia.swing.SwingUtils;
import org.maia.swing.animate.imageslide.SlidingImageState;

public class RandomPathGenerator extends AbstractPathGenerator {

	private double minimumAngleInDegrees;

	private double maximumAngleInDegrees;

	private double stepAngleInDegrees;

	private double zeroAngleBias; // probability between 0 and 1

	private double minimumZoomFactor;

	private double maximumZoomFactor;

	private double unityZoomBias; // probability between 0 and 1

	public RandomPathGenerator(Dimension imageSize, Dimension viewportSize) {
		super(imageSize, viewportSize);
		setMinimumAngleInDegrees(-40.0);
		setMaximumAngleInDegrees(Math.abs(getMinimumAngleInDegrees()));
		setStepAngleInDegrees(10.0);
		setMinimumZoomFactor(computeAbsoluteMinimumZoomFactor() * 1.1);
		setMaximumZoomFactor(getMinimumZoomFactor() * 2.0);
	}

	@Override
	public SlidingImagePath generatePath() {
		double angleInRadians = SwingUtils.degreesToRadians(generateAngleInDegrees());
		double zoomFactor = generateZoomFactor();
		Rectangle2D rect = getViewportCenterBoundsInsideImage(angleInRadians, zoomFactor);
		double cx1 = drawRandomNumber(rect.getMinX(), rect.getMaxX());
		double cx2 = drawRandomNumber(rect.getMinX(), rect.getMaxX());
		double cy1 = drawRandomNumber(rect.getMinY(), rect.getMaxY());
		double cy2 = drawRandomNumber(rect.getMinY(), rect.getMaxY());
		SlidingImageState start = new SlidingImageState();
		start.setAngleInRadians(angleInRadians);
		start.setZoomFactor(zoomFactor);
		start.setCenter(cx1, cy1);
		SlidingImageState end = start.clone();
		end.setCenter(cx2, cy2);
		return new SlidingImagePath(start, end, !rect.isEmpty());
	}

	protected double generateAngleInDegrees() {
		double degrees = drawRandomBiasedNumber(getMinimumAngleInDegrees(), getMaximumAngleInDegrees(), 0,
				getZeroAngleBias());
		double step = getStepAngleInDegrees();
		return step * Math.round(degrees / step);
	}

	protected double generateZoomFactor() {
		return drawRandomBiasedNumber(getMinimumZoomFactor(), getMaximumZoomFactor(), 1.0, getUnityZoomBias());
	}

	protected double drawRandomBiasedNumber(double minimum, double maximum, double biasedValue,
			double biasedProbability) {
		if (biasedValue >= minimum && biasedValue <= maximum && Math.random() < biasedProbability) {
			return biasedValue;
		} else {
			return drawRandomNumber(minimum, maximum);
		}
	}

	protected double drawRandomNumber(double minimum, double maximum) {
		if (Math.random() < 0.5) {
			return minimum + Math.random() * (maximum - minimum);
		} else {
			return maximum - Math.random() * (maximum - minimum);
		}
	}

	private double computeAbsoluteMinimumZoomFactor() {
		double zx = getViewportSize().getWidth() / getImageSize().getWidth();
		double zy = getViewportSize().getHeight() / getImageSize().getHeight();
		return Math.max(zx, zy);
	}

	public double getMinimumAngleInDegrees() {
		return minimumAngleInDegrees;
	}

	public void setMinimumAngleInDegrees(double angleInDegrees) {
		this.minimumAngleInDegrees = angleInDegrees;
	}

	public double getMaximumAngleInDegrees() {
		return maximumAngleInDegrees;
	}

	public void setMaximumAngleInDegrees(double angleInDegrees) {
		this.maximumAngleInDegrees = angleInDegrees;
	}

	public double getStepAngleInDegrees() {
		return stepAngleInDegrees;
	}

	public void setStepAngleInDegrees(double angleInDegrees) {
		this.stepAngleInDegrees = angleInDegrees;
	}

	public double getZeroAngleBias() {
		return zeroAngleBias;
	}

	public void setZeroAngleBias(double bias) {
		this.zeroAngleBias = bias;
	}

	public double getMinimumZoomFactor() {
		return minimumZoomFactor;
	}

	public void setMinimumZoomFactor(double zoomFactor) {
		this.minimumZoomFactor = zoomFactor;
	}

	public double getMaximumZoomFactor() {
		return maximumZoomFactor;
	}

	public void setMaximumZoomFactor(double zoomFactor) {
		this.maximumZoomFactor = zoomFactor;
	}

	public double getUnityZoomBias() {
		return unityZoomBias;
	}

	public void setUnityZoomBias(double bias) {
		this.unityZoomBias = bias;
	}

}