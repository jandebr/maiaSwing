package org.maia.swing.animate.imageslide.path;

import java.awt.Dimension;
import java.awt.Image;

public interface SlidingImagePathEvaluatorBuilder {

	SlidingImagePathEvaluator buildEvaluator(Image image, Dimension viewportSize);

}