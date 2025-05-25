package org.maia.swing.animate.overlay;

import org.maia.util.GenericListener;

public interface ColorOverlayListener extends GenericListener {

	void notifyFullyTranslucent(ColorOverlayComponent overlay);

	void notifyFullyOpaque(ColorOverlayComponent overlay);

	void notifyStartAnimating(ColorOverlayComponent overlay);

	void notifyStopAnimating(ColorOverlayComponent overlay);

}