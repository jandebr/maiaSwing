package org.maia.swing.animate.imageslide.path;

public class PathInsidenessEvaluator implements SlidingImagePathEvaluator {

	public PathInsidenessEvaluator() {
	}

	@Override
	public double evaluatePath(SlidingImagePath path) {
		if (path.isInsideImage()) {
			return 1.0;
		} else {
			return -1.0;
		}
	}

}