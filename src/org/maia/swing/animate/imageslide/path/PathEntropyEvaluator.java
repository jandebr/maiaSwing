package org.maia.swing.animate.imageslide.path;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.animate.imageslide.SlidingImageState;

public class PathEntropyEvaluator extends AbstractPathEvaluator {

	private Dimension thumbnailImageSize;

	private BufferedImage scaledLuminanceImage;

	private static final double ASSUMED_MAX_ZOOM_FACTOR = 4.0;

	private static final int THUMBNAIL_SAMPLES = 100;

	private static final int THUMBNAIL_BITS_PER_SAMPLE = 4;

	private static final int PATH_INTERPOLATIONS = 7;

	public PathEntropyEvaluator(Image image, Dimension viewportSize) {
		super(image, viewportSize);
		this.thumbnailImageSize = createThumbnailImageSize();
		this.scaledLuminanceImage = createScaledLuminanceImage(computeScaleOfLuminanceImage());
	}

	@Override
	public double evaluatePath(SlidingImagePath path) {
		double score = 0;
		int n = 0;
		int[] histogram = new int[1 << THUMBNAIL_BITS_PER_SAMPLE];
		List<BufferedImage> thumbs = createThumbnailImages(path, PATH_INTERPOLATIONS);
		for (int i = 0; i < thumbs.size(); i++) {
			// Intra image entropy
			if (i > 0 && i < thumbs.size() - 1) {
				// omit the ends to allow soft start and end
				createThumbnailSamplesHistogram(thumbs.get(i), histogram);
				score += computeEntropyScore(histogram);
				n++;
			}
			// Inter image entropy
			if (i > 0) {
				createThumbnailDiffSamplesHistogram(thumbs.get(i - 1), thumbs.get(i), histogram);
				double e = computeEntropyScore(histogram);
				score += e * e;
				n++;
			}
		}
		return score / n;
	}

	private List<BufferedImage> createThumbnailImages(SlidingImagePath path, int count) {
		List<BufferedImage> thumbs = new Vector<BufferedImage>(count);
		SlidingImageState startState = path.getStartState();
		SlidingImageState endState = path.getEndState();
		for (int i = 0; i < count; i++) {
			double r = count == 1 ? 0 : i / (double) (count - 1);
			SlidingImageState state = startState.createInterpolation(endState, r);
			thumbs.add(createThumbnailImage(state));
		}
		return thumbs;
	}

	private BufferedImage createThumbnailImage(SlidingImageState state) {
		double iscale = getScaledLuminanceImage().getWidth() / getImageSize().getWidth();
		double vscale = getThumbnailImageSize().getWidth() / getViewportSize().getWidth();
		SlidingImageState thumbState = state.clone();
		thumbState.setCenterX(thumbState.getCenterX() * iscale);
		thumbState.setCenterY(thumbState.getCenterY() * iscale);
		thumbState.setZoomFactor(thumbState.getZoomFactor() / iscale * vscale);
		Dimension thumbSize = getThumbnailImageSize();
		BufferedImage thumb = ImageUtils.createImage(thumbSize);
		Graphics2D g = thumb.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.translate(thumbSize.width / 2, thumbSize.height / 2);
		g.transform(thumbState.getTransform());
		g.drawImage(getScaledLuminanceImage(), 0, 0, null);
		g.dispose();
		return thumb;
	}

	private Dimension createThumbnailImageSize() {
		double r = getViewportSize().getHeight() / getViewportSize().getWidth();
		double x = Math.sqrt(THUMBNAIL_SAMPLES / r);
		double y = r * x;
		int tw = (int) Math.round(x);
		int th = (int) Math.round(y);
		return new Dimension(tw, th);
	}

	private void createThumbnailSamplesHistogram(BufferedImage thumbnail, int[] histogram) {
		Arrays.fill(histogram, 0);
		Dimension size = getThumbnailImageSize();
		int bps = THUMBNAIL_BITS_PER_SAMPLE;
		int bitMask = ((1 << bps) - 1) << (8 - bps);
		for (int y = 0; y < size.height; y++) {
			for (int x = 0; x < size.width; x++) {
				int gray = thumbnail.getRGB(x, y) & 0xff;
				int value = (gray & bitMask) >> (8 - bps);
				histogram[value]++;
			}
		}
	}

	private void createThumbnailDiffSamplesHistogram(BufferedImage firstThumbnail, BufferedImage secondThumbnail,
			int[] histogram) {
		Arrays.fill(histogram, 0);
		Dimension size = getThumbnailImageSize();
		int bps = THUMBNAIL_BITS_PER_SAMPLE;
		int bitMask = ((1 << bps) - 1) << (8 - bps);
		for (int y = 0; y < size.height; y++) {
			for (int x = 0; x < size.width; x++) {
				int gray1 = firstThumbnail.getRGB(x, y) & 0xff;
				int gray2 = secondThumbnail.getRGB(x, y) & 0xff;
				int value1 = (gray1 & bitMask) >> (8 - bps);
				int value2 = (gray2 & bitMask) >> (8 - bps);
				int value = Math.abs(value2 - value1);
				histogram[value]++;
			}
		}
	}

	private double computeEntropyScore(int[] histogram) {
		double entropy = computeEntropy(histogram);
		double maxEntropy = Math.log(histogram.length);
		return entropy / maxEntropy;
	}

	private double computeEntropy(int[] histogram) {
		double entropy = 0;
		int sum = computeSumOfValues(histogram);
		if (sum > 0) {
			for (int i = 0; i < histogram.length; i++) {
				double p = histogram[i] / (double) sum;
				if (p > 0) {
					entropy += -p * Math.log(p);
				}
			}
		}
		return entropy;
	}

	private int computeSumOfValues(int[] histogram) {
		int sum = 0;
		for (int i = 0; i < histogram.length; i++)
			sum += histogram[i];
		return sum;
	}

	private double computeScaleOfLuminanceImage() {
		double s = getThumbnailImageSize().getWidth() / getViewportSize().getWidth() * ASSUMED_MAX_ZOOM_FACTOR;
		return Math.min(s, 1.0);
	}

	private BufferedImage createScaledLuminanceImage(double scale) {
		BufferedImage scaledImage = ImageUtils.scale(ImageUtils.convertToBufferedImage(getImage()), scale);
		return ImageUtils.convertToGrayscale(scaledImage);
	}

	private Dimension getThumbnailImageSize() {
		return thumbnailImageSize;
	}

	private BufferedImage getScaledLuminanceImage() {
		return scaledLuminanceImage;
	}

}