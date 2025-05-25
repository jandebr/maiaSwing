package org.maia.swing.animate.imageslide.path;

import java.awt.Dimension;
import java.awt.Image;

public interface SlidingImagePathGeneratorBuilder {

	SlidingImagePathGenerator buildGenerator(Image image, Dimension viewportSize);

}