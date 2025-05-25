package org.maia.swing.animate.imageslide;

import org.maia.util.GenericListener;

public interface SlidingImageListener extends GenericListener {

	void notifyImageChanged(SlidingImageComponent component);
	
	void notifyStateChanged(SlidingImageComponent component);

	void notifyStartAnimating(SlidingImageComponent component);

	void notifyStopAnimating(SlidingImageComponent component);

}