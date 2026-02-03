package org.maia.swing.animate.wave;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.maia.graphics2d.function.Function2D;
import org.maia.graphics2d.function.PerpetualApproximatingFunction2D;
import org.maia.graphics2d.function.PerpetualApproximatingFunction2D.ControlValueGenerator;
import org.maia.graphics2d.image.ImageUtils;

public class PixelatedWavesComponent extends WavesComponent {

	private boolean antialiasingPixels;

	public PixelatedWavesComponent(Dimension size, Color background) {
		this(size, background, 1, 12);
	}

	public PixelatedWavesComponent(Dimension size, Color background, int minimumPixelSize, int maximumPixelSize) {
		this(size, background, minimumPixelSize, maximumPixelSize, 0, 3000L);
	}

	/**
	 * Creates a new pixelated waves component
	 * 
	 * @param size
	 *            The size of the component
	 * @param background
	 *            The background color of the component
	 * @param minimumPixelSize
	 *            The minimum pixel size of the waves rendering
	 * @param maximumPixelSize
	 *            The maximum pixel size of the waves rendering
	 * @param pixelSizeInclination
	 *            The inclination towards the minimum or maximum pixel size. A value of 0 has no bias. Larger positive
	 *            values have a stronger bias towards the maximum. Lesser negative values have a stronger bias towards
	 *            the minimum. Although there is no upper bound to this parameter, a practical range is [-3.0, 3.0]
	 * @param maximumWaveLagMillis
	 *            The maximum lag of every wave compared to the others, in milliseconds
	 */
	public PixelatedWavesComponent(Dimension size, Color background, int minimumPixelSize, int maximumPixelSize,
			double pixelSizeInclination, long maximumWaveLagMillis) {
		super(size, background);
		setHigherQualityRenderingEnabled(false);
		setAntialiasingPixels(true);
		getPanel().setPixelSizeFunction(PerpetualApproximatingFunction2D.createCubicApproximatingFunction(
				new PixelSizeGenerator(minimumPixelSize, maximumPixelSize, pixelSizeInclination)));
		getPanel().setMaximumWaveLagMillis(maximumWaveLagMillis);
	}

	@Override
	protected AnimatedPanel createAnimatedPanel(Dimension size, Color background) {
		return new PixelatedWavesPanel(size, background);
	}

	@Override
	protected PixelatedWavesPanel getPanel() {
		return (PixelatedWavesPanel) super.getPanel();
	}

	public boolean isAntialiasingPixels() {
		return antialiasingPixels;
	}

	public void setAntialiasingPixels(boolean antialiasing) {
		this.antialiasingPixels = antialiasing;
		refreshUI();
	}

	@SuppressWarnings("serial")
	protected class PixelatedWavesPanel extends WavesPanel {

		private Function2D pixelSizeFunction; // evolves with time

		private Map<Wave, Function2D> timeModulatorFunctions; // time lag per wave, evolves with time

		private Map<Integer, BufferedImage> canvasMap; // indexed by pixel size

		private long maximumWaveLagMillis;

		private long elapsedTimeMillis;

		public PixelatedWavesPanel(Dimension size, Color background) {
			super(size, background);
			this.timeModulatorFunctions = new HashMap<Wave, Function2D>();
			this.canvasMap = new HashMap<Integer, BufferedImage>();
		}

		@Override
		protected void updateStateBetweenPaints(Graphics2D g, long elapsedTimeMillis) {
			super.updateStateBetweenPaints(g, elapsedTimeMillis);
			setElapsedTimeMillis(getElapsedTimeMillis() + elapsedTimeMillis);
		}

		@Override
		protected void paintWave(Graphics2D g, Wave wave) {
			BufferedImage canvas = getCanvasForPixelSize(getPixelSize(wave));
			ImageUtils.makeFullyTransparent(canvas);
			Graphics2D cg = canvas.createGraphics();
			if (isAntialiasingPixels()) {
				cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			} else {
				cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
			cg.scale(canvas.getWidth(), canvas.getHeight());
			paintWaveNormalized(cg, wave);
			cg.dispose();
			g.drawImage(canvas, 0, 0, getWidth(), getHeight(), null);
		}

		protected int getPixelSize(Wave wave) {
			if (getPixelSizeFunction() != null) {
				double timeSeconds = getElapsedTimeMillis() / 1000.0;
				double timeModulation = getTimeModulatorFunction(wave).evaluate(timeSeconds);
				return Math.max(1, (int) Math.round(getPixelSizeFunction().evaluate(timeSeconds + timeModulation)));
			} else {
				return 1;
			}
		}

		private Function2D getTimeModulatorFunction(Wave wave) {
			Function2D func = getTimeModulatorFunctions().get(wave);
			if (func == null) {
				func = PerpetualApproximatingFunction2D
						.createCubicPrimedApproximatingFunction(new ControlValueGenerator() {

							@Override
							public double generateControlValue() {
								return Math.random() * getMaximumWaveLagMillis() / 1000.0;
							}
						});
				getTimeModulatorFunctions().put(wave, func);
			}
			return func;
		}

		private BufferedImage getCanvasForPixelSize(int pixelSize) {
			BufferedImage image = getCanvasMap().get(pixelSize);
			if (image == null) {
				image = createCanvas(pixelSize);
				getCanvasMap().put(pixelSize, image);
			}
			return image;
		}

		private BufferedImage createCanvas(int pixelSize) {
			int canvasWidth = Math.round(getWidth() / (float) pixelSize);
			int canvasHeight = Math.round(getHeight() / (float) pixelSize);
			return ImageUtils.createImage(canvasWidth, canvasHeight);
		}

		public Function2D getPixelSizeFunction() {
			return pixelSizeFunction;
		}

		public void setPixelSizeFunction(Function2D pixelSizeFunction) {
			this.pixelSizeFunction = pixelSizeFunction;
		}

		private Map<Wave, Function2D> getTimeModulatorFunctions() {
			return timeModulatorFunctions;
		}

		private Map<Integer, BufferedImage> getCanvasMap() {
			return canvasMap;
		}

		public long getMaximumWaveLagMillis() {
			return maximumWaveLagMillis;
		}

		public void setMaximumWaveLagMillis(long millis) {
			this.maximumWaveLagMillis = millis;
		}

		private long getElapsedTimeMillis() {
			return elapsedTimeMillis;
		}

		private void setElapsedTimeMillis(long elapsedTimeMillis) {
			this.elapsedTimeMillis = elapsedTimeMillis;
		}

	}

	private static class PixelSizeGenerator implements ControlValueGenerator {

		private int minimumPixelSize;

		private int maximumPixelSize;

		private double pixelSizeInclination;

		public PixelSizeGenerator(int minimumPixelSize, int maximumPixelSize, double pixelSizeInclination) {
			this.minimumPixelSize = minimumPixelSize;
			this.maximumPixelSize = maximumPixelSize;
			this.pixelSizeInclination = pixelSizeInclination;
		}

		@Override
		public double generateControlValue() {
			int min = getMinimumPixelSize();
			int max = getMaximumPixelSize();
			double inc = getPixelSizeInclination();
			double r = Math.random();
			if (inc > 0) {
				r = Math.pow(r, 1.0 / (1.0 + inc));
			} else if (inc < 0) {
				r = Math.pow(r, 1.0 - inc);
			}
			return min + r * (max - min);
		}

		public int getMinimumPixelSize() {
			return minimumPixelSize;
		}

		public int getMaximumPixelSize() {
			return maximumPixelSize;
		}

		public double getPixelSizeInclination() {
			return pixelSizeInclination;
		}

	}

}