package org.maia.swing.animate.wave;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.Vector;

import org.maia.swing.animate.BaseAnimatedComponent;
import org.maia.swing.animate.wave.impl.SimpleSlidingWaveDynamics;

public class WavesComponent extends BaseAnimatedComponent {

	private WaveDynamics waveDynamics;

	private WavesOverlay wavesOverlay;

	private List<Wave> waves; // ordered back to front

	public WavesComponent(Dimension size, Color background) {
		super(size, background);
		this.waveDynamics = createDefaultWaveDynamics();
		this.waves = new Vector<Wave>();
	}

	@Override
	protected AnimatedPanel createAnimatedPanel(Dimension size, Color background) {
		return new WavesPanel(size, background);
	}

	protected WaveDynamics createDefaultWaveDynamics() {
		return new SimpleSlidingWaveDynamics();
	}

	public void clearWaves() {
		synchronized (getWaves()) {
			getWaves().clear();
		}
		refreshUI();
	}

	public void addWave(Wave wave) {
		synchronized (getWaves()) {
			getWaves().add(wave);
		}
		refreshUI();
	}

	public int getWaveCount() {
		return getWaves().size();
	}

	public Wave getWave(int index) {
		return getWaves().get(index);
	}

	@Override
	public boolean isAnimating() {
		return true;
	}

	public WaveDynamics getWaveDynamics() {
		return waveDynamics;
	}

	public void setWaveDynamics(WaveDynamics waveDynamics) {
		this.waveDynamics = waveDynamics;
		refreshUI();
	}

	public WavesOverlay getWavesOverlay() {
		return wavesOverlay;
	}

	public void setWavesOverlay(WavesOverlay overlay) {
		this.wavesOverlay = overlay;
		refreshUI();
	}

	private List<Wave> getWaves() {
		return waves;
	}

	@SuppressWarnings("serial")
	protected class WavesPanel extends AnimatedPanel {

		public WavesPanel(Dimension size, Color background) {
			super(size, background);
		}

		@Override
		protected void updateStateBetweenPaints(Graphics2D g, long elapsedTimeMillis) {
			WaveDynamics dynamics = getWaveDynamics();
			synchronized (getWaves()) {
				for (int i = 0; i < getWaveCount(); i++) {
					Wave wave = getWaves().get(i);
					dynamics.updateWave(wave, i, WavesComponent.this, elapsedTimeMillis);
				}
			}
		}

		@Override
		protected void doPaintComponent(Graphics2D g) {
			paintBack(g);
			paintWaves(g);
		}

		protected void paintBack(Graphics2D g) {
			paintBackground(g);
			WavesOverlay overlay = getWavesOverlay();
			if (overlay != null) {
				overlay.paintOverBackground(g, WavesComponent.this);
			}
		}

		protected void paintWaves(Graphics2D g) {
			WavesOverlay overlay = getWavesOverlay();
			for (int i = 0; i < getWaveCount(); i++) {
				paintWave(g, getWave(i));
				if (overlay != null) {
					overlay.paintOverWave(g, i, WavesComponent.this);
				}
			}
		}

		protected void paintWave(Graphics2D g, Wave wave) {
			Graphics2D gNorm = (Graphics2D) g.create();
			gNorm.scale(getWidth(), getHeight());
			paintWaveNormalized(gNorm, wave);
			gNorm.dispose();
		}

		protected void paintWaveNormalized(Graphics2D gNorm, Wave wave) {
			gNorm.setColor(wave.getColor());
			gNorm.fill(createShapeNormalized(wave));
		}

		private Shape createShapeNormalized(Wave wave) {
			float fco = getFirstCurveOffset(wave);
			float wo = fco;
			float wa = wave.getAmplitude();
			float wl = wave.getLength();
			float ty = wave.getTranslationY();
			GeneralPath path = new GeneralPath();
			path.moveTo(wo, ty);
			do {
				float ctrly = 0.550f * wa * 2f * (float) Math.PI;
				float ctrlx1 = wo + 0.477f * wl;
				float ctrly1 = ty - ctrly;
				float ctrlx2 = wo + wl - 0.477f * wl;
				float ctrly2 = ty + ctrly;
				path.curveTo(ctrlx1, ctrly1, ctrlx2, ctrly2, wo + wl, ty);
				wo += wl;
			} while (wo < 1f);
			float bottom = Math.max(1f, ty + wa + 4f / getHeight());
			path.lineTo(wo, bottom);
			path.lineTo(fco, bottom);
			path.closePath();
			return path;
		}

		private float getFirstCurveOffset(Wave wave) {
			float tx = wave.getTranslationX();
			float wl = wave.getLength();
			if (tx == 0f) {
				return 0f;
			} else if (tx > 0f) {
				return tx - wl * (float) Math.ceil(tx / wl);
			} else {
				return tx + wl * (float) Math.floor(-tx / wl);
			}
		}

	}

}