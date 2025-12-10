package org.maia.swing.animate.imageslide.show;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.border.Border;

import org.maia.graphics2d.image.GradientImageFactory;
import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.animate.imageslide.SlidingImageAdapter;
import org.maia.swing.animate.imageslide.SlidingImageComponent;
import org.maia.swing.animate.imageslide.path.PathAngleEvaluator;
import org.maia.swing.animate.imageslide.path.PathDistanceEvaluator;
import org.maia.swing.animate.imageslide.path.PathEntropyEvaluator;
import org.maia.swing.animate.imageslide.path.PathInsidenessEvaluator;
import org.maia.swing.animate.imageslide.path.RandomPathGenerator;
import org.maia.swing.animate.imageslide.path.SlidingImagePath;
import org.maia.swing.animate.imageslide.path.SlidingImagePathEvaluator;
import org.maia.swing.animate.imageslide.path.SlidingImagePathEvaluatorBuilder;
import org.maia.swing.animate.imageslide.path.SlidingImagePathGenerator;
import org.maia.swing.animate.imageslide.path.SlidingImagePathGeneratorBuilder;
import org.maia.swing.animate.imageslide.path.WeightedScorePathEvaluator;
import org.maia.swing.animate.overlay.ColorOverlayComponent;
import org.maia.util.SystemUtils;

public class SlidingImageShowBuilder implements Cloneable {

	private Dimension size;

	private Color backgroundColor;

	private Border border; // nullable

	private Composite borderComposite; // nullable

	private SlidingImageIterator imageIterator; // nullable

	private Image imageOverlay; // nullable

	private Composite imageOverlayComposite; // nullable

	private SlidingImagePathGeneratorBuilder pathGeneratorBuilder;

	private SlidingImagePathEvaluatorBuilder pathEvaluatorBuilder;

	private double maxToMinZoomFactorRatio;

	private int imageSlidingVelocity; // in pixels per second

	private long minimumImageDisplayTimeMillis;

	private long maximumImageDisplayTimeMillis;

	private long imageFadeInTimeMillis;

	private long imageFadeOutTimeMillis;

	private long timeMillisBetweenImages;

	private int refreshRate;

	private int pathGenerationAttemptsPerImage;

	private boolean higherQualityRenderingEnabled;

	private boolean repaintClientDriven;

	private static boolean logPaths = false;

	public SlidingImageShowBuilder() {
		this(new Dimension(600, 600));
	}

	public SlidingImageShowBuilder(Dimension size) {
		this(size, Color.BLACK);
	}

	public SlidingImageShowBuilder(Dimension size, Color background) {
		withSize(size);
		withBackgroundColor(background);
		withBorderComposite(AlphaComposite.Src);
		withImageOverlay(GradientImageFactory.createGradientBorderImage(size, background, 20));
		withImageOverlayComposite(AlphaComposite.SrcOver);
		withPathGeneratorBuilder(new PathGeneratorBuilderImpl());
		withPathEvaluatorBuilder(new PathEvaluatorBuilderImpl());
		withMaxToMinZoomFactorRatio(2.0);
		withImageSlidingVelocity(40);
		withMinimumImageDisplayTimeMillis(12000L);
		withMaximumImageDisplayTimeMillis(16000L);
		withImageFadeInTimeMillis(4000L);
		withImageFadeOutTimeMillis(4000L);
		withTimeMillisBetweenImages(1000L);
		withRefreshRate(25);
		withPathGenerationAttemptsPerImage(3);
		withHigherQualityRenderingEnabled(true);
		withRepaintClientDriven(false);
	}

	@Override
	public SlidingImageShowBuilder clone() {
		SlidingImageShowBuilder clone = new SlidingImageShowBuilder();
		clone.withSize(getSize());
		clone.withBackgroundColor(getBackgroundColor());
		clone.withBorder(getBorder());
		clone.withBorderComposite(getBorderComposite());
		clone.withImageIterator(getImageIterator());
		clone.withImageOverlay(getImageOverlay());
		clone.withImageOverlayComposite(getImageOverlayComposite());
		clone.withPathGeneratorBuilder(getPathGeneratorBuilder());
		clone.withPathEvaluatorBuilder(getPathEvaluatorBuilder());
		clone.withMaxToMinZoomFactorRatio(getMaxToMinZoomFactorRatio());
		clone.withImageSlidingVelocity(getImageSlidingVelocity());
		clone.withMinimumImageDisplayTimeMillis(getMinimumImageDisplayTimeMillis());
		clone.withMaximumImageDisplayTimeMillis(getMaximumImageDisplayTimeMillis());
		clone.withImageFadeInTimeMillis(getImageFadeInTimeMillis());
		clone.withImageFadeOutTimeMillis(getImageFadeOutTimeMillis());
		clone.withTimeMillisBetweenImages(getTimeMillisBetweenImages());
		clone.withRefreshRate(getRefreshRate());
		clone.withPathGenerationAttemptsPerImage(getPathGenerationAttemptsPerImage());
		clone.withHigherQualityRenderingEnabled(isHigherQualityRenderingEnabled());
		clone.withRepaintClientDriven(isRepaintClientDriven());
		return clone;
	}

	public SlidingImageShow build() {
		return clone().buildImpl();
	}

	private SlidingImageShow buildImpl() {
		SlidingImageComponent component = createSlidingImageComponent();
		ColorOverlayComponent overlay = createColorOverlayComponent(component);
		return new SlidingImageShowImpl(component, overlay);
	}

	private SlidingImageComponent createSlidingImageComponent() {
		SlidingImageComponent comp = new SlidingImageComponent(getSize(), getBackgroundColor());
		comp.setBorder(getBorder());
		comp.setBorderComposite(getBorderComposite());
		comp.setImageOverlay(getImageOverlay());
		comp.setImageOverlayComposite(getImageOverlayComposite());
		comp.setHigherQualityRenderingEnabled(isHigherQualityRenderingEnabled());
		comp.setRepaintClientDriven(isRepaintClientDriven());
		return comp;
	}

	private ColorOverlayComponent createColorOverlayComponent(SlidingImageComponent component) {
		ColorOverlayComponent overlay = new ColorOverlayComponent(getBackgroundColor(), component);
		overlay.setRefreshRate(getRefreshRate());
		overlay.setRepaintClientDriven(isRepaintClientDriven());
		overlay.makeFullyOpaque();
		return overlay;
	}

	public SlidingImageShowBuilder withSize(Dimension size) {
		if (size == null)
			throw new NullPointerException("size is null");
		this.size = size;
		return this;
	}

	public SlidingImageShowBuilder withBackgroundColor(Color backgroundColor) {
		if (backgroundColor == null)
			throw new NullPointerException("background color is null");
		this.backgroundColor = backgroundColor;
		return this;
	}

	public SlidingImageShowBuilder withBorder(Border border) {
		this.border = border;
		return this;
	}

	public SlidingImageShowBuilder withBorderComposite(Composite composite) {
		this.borderComposite = composite;
		return this;
	}

	public SlidingImageShowBuilder withImageIterator(SlidingImageIterator imageIterator) {
		this.imageIterator = imageIterator;
		return this;
	}

	public SlidingImageShowBuilder withImageOverlay(Image imageOverlay) {
		this.imageOverlay = imageOverlay;
		return this;
	}

	public SlidingImageShowBuilder withImageOverlayComposite(Composite composite) {
		this.imageOverlayComposite = composite;
		return this;
	}

	public SlidingImageShowBuilder withPathGeneratorBuilder(SlidingImagePathGeneratorBuilder pathGeneratorBuilder) {
		if (pathGeneratorBuilder == null)
			throw new NullPointerException("path generator builder is null");
		this.pathGeneratorBuilder = pathGeneratorBuilder;
		return this;
	}

	public SlidingImageShowBuilder withPathEvaluatorBuilder(SlidingImagePathEvaluatorBuilder pathEvaluatorBuilder) {
		if (pathEvaluatorBuilder == null)
			throw new NullPointerException("path evaluator builder is null");
		this.pathEvaluatorBuilder = pathEvaluatorBuilder;
		return this;
	}

	public SlidingImageShowBuilder withMaxToMinZoomFactorRatio(double ratio) {
		if (ratio < 1.0)
			throw new IllegalArgumentException("Ratio must be >= 1 (" + ratio + ")");
		this.maxToMinZoomFactorRatio = ratio;
		return this;
	}

	public SlidingImageShowBuilder withImageSlidingVelocity(int velocity) {
		if (velocity <= 0)
			throw new IllegalArgumentException("Velocity must be > 0 (" + velocity + ")");
		this.imageSlidingVelocity = velocity;
		return this;
	}

	public SlidingImageShowBuilder withMinimumImageDisplayTimeMillis(long timeMillis) {
		if (timeMillis <= 0)
			throw new IllegalArgumentException("Minimum image display time must be > 0 (" + timeMillis + ")");
		this.minimumImageDisplayTimeMillis = timeMillis;
		return this;
	}

	public SlidingImageShowBuilder withMaximumImageDisplayTimeMillis(long timeMillis) {
		if (timeMillis <= 0)
			throw new IllegalArgumentException("Maximum image display time must be > 0 (" + timeMillis + ")");
		this.maximumImageDisplayTimeMillis = timeMillis;
		return this;
	}

	public SlidingImageShowBuilder withImageFadeInTimeMillis(long timeMillis) {
		this.imageFadeInTimeMillis = timeMillis;
		return this;
	}

	public SlidingImageShowBuilder withImageFadeOutTimeMillis(long timeMillis) {
		this.imageFadeOutTimeMillis = timeMillis;
		return this;
	}

	public SlidingImageShowBuilder withTimeMillisBetweenImages(long timeMillis) {
		this.timeMillisBetweenImages = timeMillis;
		return this;
	}

	public SlidingImageShowBuilder withRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
		return this;
	}

	public SlidingImageShowBuilder withPathGenerationAttemptsPerImage(int attempts) {
		if (attempts <= 0)
			throw new IllegalArgumentException("Path generation attempts per image must be > 0 (" + attempts + ")");
		this.pathGenerationAttemptsPerImage = attempts;
		return this;
	}

	public SlidingImageShowBuilder withHigherQualityRenderingEnabled(boolean enabled) {
		this.higherQualityRenderingEnabled = enabled;
		return this;
	}

	public SlidingImageShowBuilder withRepaintClientDriven(boolean clientDriven) {
		this.repaintClientDriven = clientDriven;
		return this;
	}

	public Dimension getSize() {
		return size;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Border getBorder() {
		return border;
	}

	public Composite getBorderComposite() {
		return borderComposite;
	}

	public SlidingImageIterator getImageIterator() {
		return imageIterator;
	}

	public Image getImageOverlay() {
		return imageOverlay;
	}

	public Composite getImageOverlayComposite() {
		return imageOverlayComposite;
	}

	public SlidingImagePathGeneratorBuilder getPathGeneratorBuilder() {
		return pathGeneratorBuilder;
	}

	public SlidingImagePathEvaluatorBuilder getPathEvaluatorBuilder() {
		return pathEvaluatorBuilder;
	}

	public double getMaxToMinZoomFactorRatio() {
		return maxToMinZoomFactorRatio;
	}

	public int getImageSlidingVelocity() {
		return imageSlidingVelocity;
	}

	public long getMinimumImageDisplayTimeMillis() {
		return minimumImageDisplayTimeMillis;
	}

	public long getMaximumImageDisplayTimeMillis() {
		return maximumImageDisplayTimeMillis;
	}

	public long getImageFadeInTimeMillis() {
		return imageFadeInTimeMillis;
	}

	public long getImageFadeOutTimeMillis() {
		return imageFadeOutTimeMillis;
	}

	public long getTimeMillisBetweenImages() {
		return timeMillisBetweenImages;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public int getPathGenerationAttemptsPerImage() {
		return pathGenerationAttemptsPerImage;
	}

	public boolean isHigherQualityRenderingEnabled() {
		return higherQualityRenderingEnabled;
	}

	public boolean isRepaintClientDriven() {
		return repaintClientDriven;
	}

	private class PathGeneratorBuilderImpl implements SlidingImagePathGeneratorBuilder {

		public PathGeneratorBuilderImpl() {
		}

		@Override
		public SlidingImagePathGenerator buildGenerator(Image image, Dimension viewportSize) {
			RandomPathGenerator generator = new RandomPathGenerator(ImageUtils.getSize(image), viewportSize);
			generator.setMaximumZoomFactor(generator.getMinimumZoomFactor() * getMaxToMinZoomFactorRatio());
			return generator;
		}

	}

	private class PathEvaluatorBuilderImpl implements SlidingImagePathEvaluatorBuilder {

		public PathEvaluatorBuilderImpl() {
		}

		@Override
		public SlidingImagePathEvaluator buildEvaluator(Image image, Dimension viewportSize) {
			WeightedScorePathEvaluator evaluator = new WeightedScorePathEvaluator();
			evaluator.addEvaluator(new PathInsidenessEvaluator(), 0);
			evaluator.addEvaluator(new PathDistanceEvaluator(image, viewportSize), 0.3);
			evaluator.addEvaluator(new PathAngleEvaluator(), 0.2);
			evaluator.addEvaluator(new PathEntropyEvaluator(image, viewportSize), 0.5);
			return evaluator;
		}

	}

	private class SlidingImageShowImpl extends SlidingImageAdapter implements SlidingImageShow {

		private SlidingImageComponent component;

		private ColorOverlayComponent overlay;

		private boolean started;

		private boolean stopped;

		private long timeToStartFadeOut = Long.MAX_VALUE;

		private long imageDisplayEndTime;

		public SlidingImageShowImpl(SlidingImageComponent component, ColorOverlayComponent overlay) {
			this.component = component;
			this.overlay = overlay;
			component.addListener(this);
		}

		@Override
		public synchronized void startAnimating() {
			if (!isStarted()) {
				setStarted(true);
				animateNextImage();
			} else if (isStopped()) {
				setStopped(false);
				animateNextImage();
			}
		}

		@Override
		public synchronized void stopAnimating() {
			if (isStarted() && !isStopped()) {
				setStopped(true);
				getComponent().stopAnimating();
			}
		}

		private void animateNextImage() {
			animateNextImage(0L);
		}

		private void animateNextImage(long minimumDelayTimeMillis) {
			if (getImageIterator() != null && getImageIterator().hasNext()) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						long t0 = System.currentTimeMillis();
						int maxAttemptsPerImage = getPathGenerationAttemptsPerImage();
						int maxAttempts = maxAttemptsPerImage * getImageIterator().getUniqueImageCount();
						int attempts = 0;
						Image image = null;
						SlidingImagePath path = null;
						do {
							if (attempts++ % maxAttemptsPerImage == 0) {
								image = getNextImage();
								if (image == null)
									break;
							}
							path = generatePath(image);
						} while (path == null && attempts < maxAttempts);
						if (path != null) {
							SystemUtils.sleep(minimumDelayTimeMillis - (System.currentTimeMillis() - t0));
							animateImageOverPath(image, path);
						}
					}
				}).start();
			}
		}

		private void animateImageOverPath(Image image, SlidingImagePath path) {
			long displayTime = getImageDisplayTimeMillis(path);
			long fadeInTime = Math.min(getImageFadeInTimeMillis(), displayTime);
			long fadeOutTime = Math.min(getImageFadeOutTimeMillis(), displayTime - fadeInTime);
			getComponent().setImageAlwaysCoveringUI(path.isInsideImage());
			getComponent().changeImage(image);
			getComponent().animatePath(path, displayTime);
			getOverlay().animateToFullTranslucency(fadeInTime);
			setImageDisplayEndTime(System.currentTimeMillis() + displayTime);
			setTimeToStartFadeOut(getImageDisplayEndTime() - fadeOutTime);
		}

		private Image getNextImage() {
			Image image = null;
			if (getImageIterator() != null && getImageIterator().hasNext()) {
				image = getImageIterator().next();
			}
			return image;
		}

		private SlidingImagePath generatePath(Image image) {
			SlidingImagePath bestPath = null;
			if (image != null) {
				SlidingImagePathGenerator generator = getPathGeneratorBuilder().buildGenerator(image, getSize());
				SlidingImagePathEvaluator evaluator = getPathEvaluatorBuilder().buildEvaluator(image, getSize());
				double bestScore = 0;
				for (int i = 0; i < 10; i++) {
					SlidingImagePath path = generator.generatePath();
					double score = evaluator.evaluatePath(path);
					if (score > bestScore && (bestPath == null || Math.random() <= 0.8)) {
						bestPath = path;
						bestScore = score;
					}
				}
				if (logPaths) {
					if (bestPath != null) {
						System.out.println(bestPath + " score:" + Math.round(bestScore * 1000.0) / 1000.0 + " distance:"
								+ Math.round(bestPath.getDistanceInViewCoordinates()));
					} else {
						System.out.println("No good path found");
					}
				}
			}
			return bestPath;
		}

		private long getImageDisplayTimeMillis(SlidingImagePath path) {
			long time = Math.round(path.getDistanceInViewCoordinates() / getImageSlidingVelocity() * 1000);
			return Math.max(Math.min(time, getMaximumImageDisplayTimeMillis()), getMinimumImageDisplayTimeMillis());
		}

		@Override
		public synchronized void notifyStateChanged(SlidingImageComponent component) {
			if (System.currentTimeMillis() >= getTimeToStartFadeOut()) {
				getOverlay().animateToFullOpacity(getImageDisplayEndTime() - System.currentTimeMillis());
				setTimeToStartFadeOut(Long.MAX_VALUE);
			}
		}

		@Override
		public synchronized void notifyStopAnimating(SlidingImageComponent component) {
			if (!isStopped()) {
				getOverlay().makeFullyOpaque();
				animateNextImage(getTimeMillisBetweenImages());
			}
		}

		@Override
		public JComponent getUI() {
			return getOverlay().getUI();
		}

		private SlidingImageComponent getComponent() {
			return component;
		}

		private ColorOverlayComponent getOverlay() {
			return overlay;
		}

		private boolean isStarted() {
			return started;
		}

		private void setStarted(boolean started) {
			this.started = started;
		}

		private boolean isStopped() {
			return stopped;
		}

		private void setStopped(boolean stopped) {
			this.stopped = stopped;
		}

		private long getTimeToStartFadeOut() {
			return timeToStartFadeOut;
		}

		private void setTimeToStartFadeOut(long time) {
			this.timeToStartFadeOut = time;
		}

		private long getImageDisplayEndTime() {
			return imageDisplayEndTime;
		}

		private void setImageDisplayEndTime(long time) {
			this.imageDisplayEndTime = time;
		}

	}

}