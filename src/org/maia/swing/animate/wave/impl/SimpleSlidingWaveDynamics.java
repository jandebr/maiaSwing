package org.maia.swing.animate.wave.impl;

import org.maia.swing.animate.wave.Wave;
import org.maia.swing.animate.wave.WaveDynamics;
import org.maia.swing.animate.wave.WavesComponent;

public class SimpleSlidingWaveDynamics implements WaveDynamics {

	public float frontWaveScreenSlideMillis = 1000f;

	public float backWaveScreenSlideMillis = 2000f;

	public SimpleSlidingWaveDynamics() {
	}

	@Override
	public void updateWave(Wave wave, int waveIndex, WavesComponent component, long elapsedTimeMillis) {
		float relDepth = 0;
		int n = component.getWaveCount();
		if (n > 1) {
			relDepth = (n - 1 - waveIndex) / (float) (n - 1);
		}
		float screenSlideMillis = (1f - relDepth) * frontWaveScreenSlideMillis + relDepth * backWaveScreenSlideMillis;
		float tx = elapsedTimeMillis / screenSlideMillis;
		wave.translate(tx, 0f);
	}

}