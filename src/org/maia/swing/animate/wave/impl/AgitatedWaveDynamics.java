package org.maia.swing.animate.wave.impl;

import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.function.Function2D;
import org.maia.graphics2d.function.PerpetualApproximatingFunction2D;
import org.maia.graphics2d.function.PerpetualApproximatingFunction2D.ControlValueGenerator;
import org.maia.graphics2d.function.SigmoidFunction2D;
import org.maia.swing.animate.wave.Wave;
import org.maia.swing.animate.wave.WaveDynamics;
import org.maia.swing.animate.wave.WavesComponent;
import org.maia.util.Randomizer;

public class AgitatedWaveDynamics implements WaveDynamics {

	// all normalized units in [0,1] interval

	// all modulator functions have as domain the zero-based wave index

	private float baseline;

	private float elevationMaximum = 0.4f;

	private float perspectiveLiftMaximum = 0.2f; // difference between back to front wave's peak under same amplitude

	private Function2D perspectiveLiftModulator;

	private ValueRange wavelengthRange = new ValueRange(2.0f, 5.0f); // inverse proportionate to agitation

	private Function2D wavelengthModulator;

	private ValueRange amplitudeRange = new ValueRange(0.05f, 0.3f); // proportionate to agitation

	private Function2D amplitudeModulator;

	private ValueRange velocityRange = new ValueRange(0.4f, 1.0f); // proportionate to agitation, in wavelenghts per
																	// second

	private List<AgitationLevel> agitationLevels = new Vector<AgitationLevel>();

	private Function2D agitationFunction; // evolves within the current agitation level's range

	private AgitationLevelProgression agitationLevelProgression;

	private List<Function2D> timeModulatorFunctions = new Vector<Function2D>();

	private long elapsedTimeMillis;

	private int waveCount;

	private Randomizer randomizer = new Randomizer();

	public AgitatedWaveDynamics(WavesComponent component) {
		this(component, 0.7f);
	}

	public AgitatedWaveDynamics(WavesComponent component, float baseline) {
		this(component, baseline, 4, new TimeRange(3000L, 6000L), createRandomLevelProgression(), 1000L);
	}

	public AgitatedWaveDynamics(WavesComponent component, float baseline, int agitationLevelCount,
			TimeRange agitationTimeRange, AgitationLevelProgression agitationLevelProgression,
			long maximumWaveLagMillis) {
		setBaseline(baseline);
		setAgitationLevelProgression(agitationLevelProgression);
		init(component, agitationLevelCount, agitationTimeRange, maximumWaveLagMillis);
	}

	private void init(WavesComponent component, int agitationLevelCount, TimeRange agitationTimeRange,
			long maximumWaveLagMillis) {
		int n = component.getWaveCount();
		double n1 = Math.max(n - 1.0, 1.0);
		setWaveCount(n);
		setPerspectiveLiftModulator(SigmoidFunction2D.createCappedFunction(-1.0, 1.0, n1 * 2, 0));
		setWavelengthModulator(SigmoidFunction2D.createCappedFunction(0.5, 1.0, 0, n1));
		setAmplitudeModulator(SigmoidFunction2D.createCappedFunction(0.5, 1.0, 0, n1));
		initAgitationLevels(agitationLevelCount, agitationTimeRange);
		initAgitationFunction();
		initWaves(component, maximumWaveLagMillis);
	}

	private void initAgitationLevels(int levelCount, TimeRange timeRange) {
		clearAgitationLevels();
		int n = Math.max(levelCount, 1);
		float step = 1f / n;
		for (int i = 0; i < n; i++) {
			float min = i * step;
			float max = min + Math.min(1f, step);
			addAgitationLevel(new AgitationLevel(new ValueRange(min, max), timeRange));
		}
	}

	private void initAgitationFunction() {
		setAgitationFunction(
				PerpetualApproximatingFunction2D.createCubicApproximatingFunction(new AgitationValueGenerator()));
	}

	private void initWaves(WavesComponent component, long maximumWaveLagMillis) {
		for (int waveIndex = 0; waveIndex < getWaveCount(); waveIndex++) {
			getTimeModulatorFunctions().add(PerpetualApproximatingFunction2D
					.createCubicPrimedApproximatingFunction(new ControlValueGenerator() {

						@Override
						public double generateControlValue() {
							return getRandomizer().drawDoubleUnitNumber() * maximumWaveLagMillis / 1000.0;
						}
					}));
			Wave wave = component.getWave(waveIndex);
			wave.setLength(computeWavelength(waveIndex));
			wave.setAmplitude(computeWaveAmplitude(waveIndex));
			wave.setTranslationY(computeWaveBaseline(waveIndex));
			wave.setTranslationX(getRandomizer().drawFloatUnitNumber());
		}
	}

	public void clearAgitationLevels() {
		getAgitationLevels().clear();
	}

	public void addAgitationLevel(AgitationLevel level) {
		getAgitationLevels().add(level);
	}

	@Override
	public void updateWave(Wave wave, int waveIndex, WavesComponent component, long elapsedTimeMillis) {
		if (component.getWaveCount() != getWaveCount())
			throw new IllegalStateException("Number of waves has been altered since initialization");
		if (waveIndex == 0) {
			setElapsedTimeMillis(getElapsedTimeMillis() + elapsedTimeMillis);
		}
		float velocity = computeWaveVelocity(waveIndex);
		float wl = computeWavelength(waveIndex);
		float delta = velocity * wl * (float) elapsedTimeMillis / 1000f;
		float tx = wave.getTranslationX() + delta;
		wave.setTranslationX(tx - wl * (float) Math.floor(tx / wl));
		wave.setLength(wl);
		wave.setAmplitude(computeWaveAmplitude(waveIndex));
		wave.setTranslationY(computeWaveBaseline(waveIndex));
	}

	private float computeWaveVelocity(int waveIndex) {
		return getVelocityRange().interpolate(getAgitationValue(waveIndex));
	}

	private float computeWavelength(int waveIndex) {
		float length = getWavelengthRange().interpolate(1f - getAgitationValue(waveIndex));
		length *= getWavelengthModulator().evaluate(waveIndex);
		return length;
	}

	private float computeWaveAmplitude(int waveIndex) {
		float amplitude = getAmplitudeRange().interpolate(getAgitationValue(waveIndex));
		amplitude *= getAmplitudeModulator().evaluate(waveIndex);
		return amplitude;
	}

	private float computeWaveBaseline(int waveIndex) {
		float baseline = getBaseline();
		baseline -= computeWaveElevation(waveIndex);
		baseline -= computeWavePerspectiveLift(waveIndex);
		return baseline;
	}

	private float computeWaveElevation(int waveIndex) {
		return getElevationMaximum() * getAgitationValue(waveIndex);
	}

	private float computeWavePerspectiveLift(int waveIndex) {
		return getPerspectiveLiftMaximum() * (float) getPerspectiveLiftModulator().evaluate(waveIndex);
	}

	private float getAgitationValue(int waveIndex) {
		double timeSeconds = getElapsedTimeMillis() / 1000.0;
		double timeModulation = getTimeModulatorFunctions().get(waveIndex).evaluate(timeSeconds);
		return (float) getAgitationFunction().evaluate(timeSeconds + timeModulation);
	}

	public static AgitationLevelProgression createRandomLevelProgression() {
		return createRandomLevelProgression(true);
	}

	public static AgitationLevelProgression createRandomLevelProgression(boolean startAtLevelZero) {
		return createRandomLevelProgression(startAtLevelZero, 0.4f);
	}

	public static AgitationLevelProgression createRandomLevelProgression(boolean startAtLevelZero,
			float probabilityLevelUp) {
		return new AgitationLevelProgression() {

			@Override
			public int getInitialLevelIndex(int levelCount, Randomizer rnd) {
				if (startAtLevelZero) {
					return 0;
				} else {
					return rnd.drawIntegerNumber(0, levelCount - 1);
				}
			}

			@Override
			public int getNextLevelIndex(int currentLevelIndex, int levelCount, Randomizer rnd) {
				int iNext = currentLevelIndex;
				if (levelCount > 1) {
					if (currentLevelIndex == 0) {
						iNext = 1;
					} else if (currentLevelIndex == levelCount - 1) {
						iNext = currentLevelIndex - 1;
					} else if (rnd.drawFloatUnitNumber() < probabilityLevelUp) {
						iNext = currentLevelIndex + 1;
					} else {
						iNext = currentLevelIndex - 1;
					}
				}
				return iNext;
			}
		};
	}

	public static AgitationLevelProgression createCyclicalLevelProgression() {
		return new AgitationLevelProgression() {

			private int direction = 1;

			@Override
			public int getInitialLevelIndex(int levelCount, Randomizer rnd) {
				return 0;
			}

			@Override
			public int getNextLevelIndex(int currentLevelIndex, int levelCount, Randomizer rnd) {
				int iNext = currentLevelIndex + direction;
				if (iNext == levelCount || iNext < 0) {
					direction *= -1;
					iNext = currentLevelIndex + direction;
				}
				return iNext;
			}
		};
	}

	public static AgitationLevelProgression createUpwardLevelProgression() {
		return new AgitationLevelProgression() {

			@Override
			public int getInitialLevelIndex(int levelCount, Randomizer rnd) {
				return 0;
			}

			@Override
			public int getNextLevelIndex(int currentLevelIndex, int levelCount, Randomizer rnd) {
				return Math.min(currentLevelIndex + 1, levelCount - 1);
			}
		};
	}

	public static AgitationLevelProgression createDownwardLevelProgression() {
		return new AgitationLevelProgression() {

			@Override
			public int getInitialLevelIndex(int levelCount, Randomizer rnd) {
				return levelCount - 1;
			}

			@Override
			public int getNextLevelIndex(int currentLevelIndex, int levelCount, Randomizer rnd) {
				return Math.max(currentLevelIndex - 1, 0);
			}
		};
	}

	public float getBaseline() {
		return baseline;
	}

	public void setBaseline(float baseline) {
		this.baseline = baseline;
	}

	public float getElevationMaximum() {
		return elevationMaximum;
	}

	public void setElevationMaximum(float elevationMaximum) {
		this.elevationMaximum = elevationMaximum;
	}

	public float getPerspectiveLiftMaximum() {
		return perspectiveLiftMaximum;
	}

	public void setPerspectiveLiftMaximum(float perspectiveLiftMaximum) {
		this.perspectiveLiftMaximum = perspectiveLiftMaximum;
	}

	private Function2D getPerspectiveLiftModulator() {
		return perspectiveLiftModulator;
	}

	private void setPerspectiveLiftModulator(Function2D perspectiveLiftModulator) {
		this.perspectiveLiftModulator = perspectiveLiftModulator;
	}

	public ValueRange getWavelengthRange() {
		return wavelengthRange;
	}

	private Function2D getWavelengthModulator() {
		return wavelengthModulator;
	}

	private void setWavelengthModulator(Function2D wavelengthModulator) {
		this.wavelengthModulator = wavelengthModulator;
	}

	public ValueRange getAmplitudeRange() {
		return amplitudeRange;
	}

	private Function2D getAmplitudeModulator() {
		return amplitudeModulator;
	}

	private void setAmplitudeModulator(Function2D amplitudeModulator) {
		this.amplitudeModulator = amplitudeModulator;
	}

	public ValueRange getVelocityRange() {
		return velocityRange;
	}

	private List<AgitationLevel> getAgitationLevels() {
		return agitationLevels;
	}

	private Function2D getAgitationFunction() {
		return agitationFunction;
	}

	private void setAgitationFunction(Function2D function) {
		this.agitationFunction = function;
	}

	public AgitationLevelProgression getAgitationLevelProgression() {
		return agitationLevelProgression;
	}

	public void setAgitationLevelProgression(AgitationLevelProgression progression) {
		this.agitationLevelProgression = progression;
	}

	private List<Function2D> getTimeModulatorFunctions() {
		return timeModulatorFunctions;
	}

	private long getElapsedTimeMillis() {
		return elapsedTimeMillis;
	}

	private void setElapsedTimeMillis(long timeMillis) {
		this.elapsedTimeMillis = timeMillis;
	}

	private int getWaveCount() {
		return waveCount;
	}

	private void setWaveCount(int waveCount) {
		this.waveCount = waveCount;
	}

	private Randomizer getRandomizer() {
		return randomizer;
	}

	public static class ValueRange {

		private float minimum;

		private float maximum;

		public ValueRange(float minimum, float maximum) {
			this.minimum = minimum;
			this.maximum = maximum;
		}

		public float interpolate(float r) {
			return (1f - r) * getMinimum() + r * getMaximum();
		}

		public float drawRandomValue() {
			return getMinimum() + new Randomizer().drawFloatUnitNumber() * (getMaximum() - getMinimum());
		}

		public void setRange(float minimum, float maximum) {
			setMinimum(minimum);
			setMaximum(maximum);
		}

		public float getMinimum() {
			return minimum;
		}

		public void setMinimum(float minimum) {
			this.minimum = minimum;
		}

		public float getMaximum() {
			return maximum;
		}

		public void setMaximum(float maximum) {
			this.maximum = maximum;
		}

	}

	public static class TimeRange {

		private long minimumMillis;

		private long maximumMillis;

		public TimeRange(long minimumMillis, long maximumMillis) {
			this.minimumMillis = minimumMillis;
			this.maximumMillis = maximumMillis;
		}

		public long drawRandomMillis() {
			return new Randomizer().drawLongIntegerNumber(getMinimumMillis(), getMaximumMillis());
		}

		public void setRange(long minimumMillis, long maximumMillis) {
			setMinimumMillis(minimumMillis);
			setMaximumMillis(maximumMillis);
		}

		public long getMinimumMillis() {
			return minimumMillis;
		}

		public void setMinimumMillis(long minimumMillis) {
			this.minimumMillis = minimumMillis;
		}

		public long getMaximumMillis() {
			return maximumMillis;
		}

		public void setMaximumMillis(long maximumMillis) {
			this.maximumMillis = maximumMillis;
		}

	}

	public static class AgitationLevel {

		private ValueRange agitationRange;

		private TimeRange timeRange;

		public AgitationLevel(ValueRange agitationRange, TimeRange timeRange) {
			this.agitationRange = agitationRange;
			this.timeRange = timeRange;
		}

		public ValueRange getAgitationRange() {
			return agitationRange;
		}

		public TimeRange getTimeRange() {
			return timeRange;
		}

	}

	private class AgitationValueGenerator implements ControlValueGenerator {

		private int agitationLevelIndex;

		private long agitationLevelTimeOffsetMillis;

		private long agitationLevelTimeDurationMillis;

		private int sampleIndex;

		public AgitationValueGenerator() {
		}

		@Override
		public double generateControlValue() {
			// invoked for every new sample representing one second
			int si = getSampleIndex();
			if (si == 0) {
				initAgitationLevel();
			}
			long t = si * 1000L;
			long t0 = getAgitationLevelTimeOffsetMillis();
			long t1 = t0 + getAgitationLevelTimeDurationMillis();
			while (t > t1) {
				setAgitationLevelIndex(getNextAgitationLevelIndex());
				setAgitationLevelTimeOffsetMillis(t1);
				setAgitationLevelTimeDurationMillis(getAgitationLevel().getTimeRange().drawRandomMillis());
				t0 = t1;
				t1 = t0 + getAgitationLevelTimeDurationMillis();
			}
			double value = getAgitationLevel().getAgitationRange().drawRandomValue();
			setSampleIndex(si + 1);
			return value;
		}

		private void initAgitationLevel() {
			setAgitationLevelIndex(
					getAgitationLevelProgression().getInitialLevelIndex(getAgitationLevelCount(), getRandomizer()));
			setAgitationLevelTimeDurationMillis(getAgitationLevel().getTimeRange().drawRandomMillis());
		}

		private int getNextAgitationLevelIndex() {
			return getAgitationLevelProgression().getNextLevelIndex(getAgitationLevelIndex(), getAgitationLevelCount(),
					getRandomizer());
		}

		private AgitationLevel getAgitationLevel() {
			return getAgitationLevels().get(getAgitationLevelIndex());
		}

		private int getAgitationLevelCount() {
			return getAgitationLevels().size();
		}

		private int getAgitationLevelIndex() {
			return agitationLevelIndex;
		}

		private void setAgitationLevelIndex(int index) {
			this.agitationLevelIndex = index;
		}

		private long getAgitationLevelTimeOffsetMillis() {
			return agitationLevelTimeOffsetMillis;
		}

		private void setAgitationLevelTimeOffsetMillis(long offsetMillis) {
			this.agitationLevelTimeOffsetMillis = offsetMillis;
		}

		private long getAgitationLevelTimeDurationMillis() {
			return agitationLevelTimeDurationMillis;
		}

		private void setAgitationLevelTimeDurationMillis(long durationMillis) {
			this.agitationLevelTimeDurationMillis = durationMillis;
		}

		private int getSampleIndex() {
			return sampleIndex;
		}

		private void setSampleIndex(int sampleIndex) {
			this.sampleIndex = sampleIndex;
		}

	}

	public static interface AgitationLevelProgression {

		int getInitialLevelIndex(int levelCount, Randomizer rnd);

		int getNextLevelIndex(int currentLevelIndex, int levelCount, Randomizer rnd);

	}

}