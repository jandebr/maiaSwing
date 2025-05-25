package org.maia.swing.animate.imageslide.path;

public interface SlidingImagePathEvaluator {

	/**
	 * Evaluates the given path and assigns a score between 0 to 1
	 * 
	 * @param path
	 *            The path
	 * @return The score of <code>path</code> as a number between 0 and 1. Returns the special value -1 when certain
	 *         path constraints are violated
	 */
	double evaluatePath(SlidingImagePath path);

}