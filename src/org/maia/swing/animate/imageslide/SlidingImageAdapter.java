package org.maia.swing.animate.imageslide;

public abstract class SlidingImageAdapter implements SlidingImageListener {

	protected SlidingImageAdapter() {
	}

	@Override
	public void notifyImageChanged(SlidingImageComponent component) {
		// Subclasses can override this method
	}

	@Override
	public void notifyStateChanged(SlidingImageComponent component) {
		// Subclasses can override this method
	}

	@Override
	public void notifyStartAnimating(SlidingImageComponent component) {
		// Subclasses can override this method
	}

	@Override
	public void notifyStopAnimating(SlidingImageComponent component) {
		// Subclasses can override this method
	}

}