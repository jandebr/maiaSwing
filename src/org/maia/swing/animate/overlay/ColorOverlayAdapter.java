package org.maia.swing.animate.overlay;

public abstract class ColorOverlayAdapter implements ColorOverlayListener {

	protected ColorOverlayAdapter() {
	}

	@Override
	public void notifyFullyTranslucent(ColorOverlayComponent overlay) {
		// Subclasses can override this method
	}

	@Override
	public void notifyFullyOpaque(ColorOverlayComponent overlay) {
		// Subclasses can override this method
	}

	@Override
	public void notifyStartAnimating(ColorOverlayComponent overlay) {
		// Subclasses can override this method
	}

	@Override
	public void notifyStopAnimating(ColorOverlayComponent overlay) {
		// Subclasses can override this method
	}

}