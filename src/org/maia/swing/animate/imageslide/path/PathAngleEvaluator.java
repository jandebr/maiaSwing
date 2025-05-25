package org.maia.swing.animate.imageslide.path;

public class PathAngleEvaluator implements SlidingImagePathEvaluator {

	public PathAngleEvaluator() {
	}

	@Override
	public double evaluatePath(SlidingImagePath path) {
		return computePathScore(path);
	}

	private double computePathScore(SlidingImagePath path) {
		double theta = path.getAverageAngleInRadians();
		if (theta != 0) {
			double tx = Math.cos(theta);
			double ty = Math.sin(theta);
			double dx = path.getEndState().getCenterX() - path.getStartState().getCenterX();
			double dy = path.getEndState().getCenterY() - path.getStartState().getCenterY();
			double cos = Math.abs((tx * dx + ty * dy) / Math.sqrt(dx * dx + dy * dy));
			double acos = Math.acos(cos) / Math.PI * 2.0;
			return Math.pow(Math.max(acos, 1.0 - acos) * 2.0 - 1.0, 3.0);
		} else {
			return 1.0;
		}
	}

}