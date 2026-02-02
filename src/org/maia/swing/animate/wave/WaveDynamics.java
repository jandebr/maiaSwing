package org.maia.swing.animate.wave;

public interface WaveDynamics {

	void updateWave(Wave wave, int waveIndex, WavesComponent component, long elapsedTimeMillis);

}