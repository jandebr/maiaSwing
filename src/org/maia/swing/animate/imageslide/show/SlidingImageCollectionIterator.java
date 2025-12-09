package org.maia.swing.animate.imageslide.show;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class SlidingImageCollectionIterator implements SlidingImageIterator {

	private ImageSequencer imageSequencer;

	private List<ImageElement> imageElements;

	private List<ImageElement> forwardBuffer;

	private boolean sequenced;

	private boolean avoidSuccessiveRepeats = true;

	private ImageElement previousImageElement;

	private SlidingImageCollectionIterator(ImageSequencer imageSequencer) {
		this.imageSequencer = imageSequencer;
		this.imageElements = new Vector<ImageElement>();
		this.forwardBuffer = new Vector<ImageElement>();
	}

	public static SlidingImageCollectionIterator createLinearRepeatingIterator() {
		return new SlidingImageCollectionIterator(new LinearSequencer(true));
	}

	public static SlidingImageCollectionIterator createLinearNonRepeatingIterator() {
		return new SlidingImageCollectionIterator(new LinearSequencer(false));
	}

	public static SlidingImageCollectionIterator createPermutedRepeatingIterator() {
		return new SlidingImageCollectionIterator(new PermutedSequencer(true));
	}

	public static SlidingImageCollectionIterator createPermutedNonRepeatingIterator() {
		return new SlidingImageCollectionIterator(new PermutedSequencer(false));
	}

	public static SlidingImageCollectionIterator createRandomRepeatingIterator() {
		return new SlidingImageCollectionIterator(new RandomSequencer());
	}

	public synchronized void addImage(Image image) {
		addImageElement(new ImageWrapper(image));
	}

	public synchronized void addImageElement(ImageElement element) {
		getImageElements().add(element);
	}

	@Override
	public synchronized boolean hasNext() {
		feedForwardBuffer();
		return !getForwardBuffer().isEmpty();
	}

	@Override
	public synchronized Image next() {
		if (hasNext()) {
			ImageElement element = getForwardBuffer().remove(0);
			setPreviousImageElement(element);
			return element.getImage();
		} else {
			throw new NoSuchElementException();
		}
	}

	private void feedForwardBuffer() {
		List<ImageElement> forwardBuffer = getForwardBuffer();
		while (forwardBuffer.isEmpty() && getUniqueImageCount() > 0) {
			if (getImageSequencer().isRepeating() || !isSequenced()) {
				forwardBuffer.addAll(nextSequence()); // contains at least one element
				if (isAvoidSuccessiveRepeats() && forwardBuffer.get(0).equals(getPreviousImageElement())
						&& getUniqueImageCount() > 1) {
					ImageElement first = forwardBuffer.remove(0);
					if (!forwardBuffer.isEmpty()) {
						int i = 1 + (int) Math.floor(Math.random() * forwardBuffer.size());
						forwardBuffer.add(i, first);
					}
				}
			} else {
				break;
			}
		}
	}

	private List<ImageElement> nextSequence() {
		setSequenced(true);
		return getImageSequencer().nextSequence(getImageElements());
	}

	@Override
	public int getUniqueImageCount() {
		return getImageElements().size();
	}

	private ImageSequencer getImageSequencer() {
		return imageSequencer;
	}

	private List<ImageElement> getImageElements() {
		return imageElements;
	}

	private List<ImageElement> getForwardBuffer() {
		return forwardBuffer;
	}

	private boolean isSequenced() {
		return sequenced;
	}

	private void setSequenced(boolean sequenced) {
		this.sequenced = sequenced;
	}

	public boolean isAvoidSuccessiveRepeats() {
		return avoidSuccessiveRepeats;
	}

	public void setAvoidSuccessiveRepeats(boolean avoid) {
		this.avoidSuccessiveRepeats = avoid;
	}

	private ImageElement getPreviousImageElement() {
		return previousImageElement;
	}

	private void setPreviousImageElement(ImageElement previous) {
		this.previousImageElement = previous;
	}

	public static interface ImageElement {

		Image getImage();

	}

	private static class ImageWrapper implements ImageElement {

		private Image image;

		public ImageWrapper(Image image) {
			this.image = image;
		}

		@Override
		public Image getImage() {
			return image;
		}

	}

	private abstract static class ImageSequencer {

		private boolean repeating;

		protected ImageSequencer(boolean repeating) {
			this.repeating = repeating;
		}

		public abstract List<ImageElement> nextSequence(List<ImageElement> allImageElements);

		public boolean isRepeating() {
			return repeating;
		}

	}

	private static class LinearSequencer extends ImageSequencer {

		public LinearSequencer(boolean repeating) {
			super(repeating);
		}

		@Override
		public List<ImageElement> nextSequence(List<ImageElement> allImageElements) {
			return allImageElements;
		}

	}

	private static class PermutedSequencer extends ImageSequencer {

		public PermutedSequencer(boolean repeating) {
			super(repeating);
		}

		@Override
		public List<ImageElement> nextSequence(List<ImageElement> allImageElements) {
			List<ImageElement> sequence = new Vector<ImageElement>(allImageElements);
			Collections.shuffle(sequence);
			return sequence;
		}

	}

	private static class RandomSequencer extends ImageSequencer {

		public RandomSequencer() {
			super(true); // always repeating
		}

		@Override
		public List<ImageElement> nextSequence(List<ImageElement> allImageElements) {
			List<ImageElement> sequence = new Vector<ImageElement>(1);
			int n = allImageElements.size();
			if (n > 0) {
				int i = (int) Math.floor(Math.random() * n);
				sequence.add(allImageElements.get(i));
			}
			return sequence;
		}

	}

}