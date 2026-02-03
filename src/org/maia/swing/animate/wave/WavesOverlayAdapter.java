package org.maia.swing.animate.wave;

import java.awt.Graphics2D;

public abstract class WavesOverlayAdapter implements WavesOverlay {

	protected WavesOverlayAdapter() {
	}

	@Override
	public void paintOverBackground(Graphics2D g, WavesComponent component) {
		// Subclasses to override
	}

	@Override
	public void paintOverWave(Graphics2D g, int waveIndex, WavesComponent component) {
		// Subclasses to override
	}

}