package org.maia.swing.animate.wave;

import java.awt.Graphics2D;

public interface WavesOverlay {

	void paintOverBackground(Graphics2D g, WavesComponent component);

	void paintOverWave(Graphics2D g, int waveIndex, WavesComponent component);

}