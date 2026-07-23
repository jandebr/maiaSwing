package org.maia.swing.animate.imageslide;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.border.Border;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.animate.BaseAnimatedComponent;
import org.maia.swing.animate.imageslide.path.SlidingImagePath;
import org.maia.util.GenericListenerList;

public class SlidingImageComponent extends BaseAnimatedComponent {

	private Image image;

	private Image imageOverlay;

	private Composite imageOverlayComposite;

	private Composite borderComposite;

	private SlidingImageState state;

	private SlidingImageState startState;

	private SlidingImageState targetState;

	private long startTimeNanos;

	private long targetTimeNanos;

	private long fadeInDurationMillis;

	private long fadeOutDurationMillis;

	private float imageOpacity;

	private boolean lastAnimating;

	private boolean imageAlwaysCoveringUi; // rendering hint, only set to true when guaranteed

	private GenericListenerList<SlidingImageListener> listeners;

	public SlidingImageComponent(Dimension size, Color background) {
		super(size, background);
		this.listeners = new GenericListenerList<SlidingImageListener>();
		resetImage();
	}

	@Override
	protected AnimatedPanel createAnimatedPanel(Dimension size, Color background) {
		return new SlidingImagePanel(size, background);
	}

	public void addListener(SlidingImageListener listener) {
		getListeners().addListener(listener);
	}

	public void removeListener(SlidingImageListener listener) {
		getListeners().removeListener(listener);
	}

	public void removeAllListeners() {
		getListeners().removeAllListeners();
	}

	public void slideToLeftInImageCoordinates(double distance, long durationMillis) {
		animateTo(getState().createTranslation(-distance, 0), durationMillis);
	}

	public void slideToLeftInViewCoordinates(double distance, long durationMillis) {
		animateTo(getState().createTranslationOverAngle(distance / getState().getZoomFactor(),
				getState().getAngleInRadians() + Math.PI), durationMillis);
	}

	public void slideToRightInImageCoordinates(double distance, long durationMillis) {
		animateTo(getState().createTranslation(distance, 0), durationMillis);
	}

	public void slideToRightInViewCoordinates(double distance, long durationMillis) {
		animateTo(getState().createTranslationOverAngle(distance / getState().getZoomFactor(),
				getState().getAngleInRadians()), durationMillis);
	}

	public void slideToTopInImageCoordinates(double distance, long durationMillis) {
		animateTo(getState().createTranslation(0, -distance), durationMillis);
	}

	public void slideToTopInViewCoordinates(double distance, long durationMillis) {
		animateTo(getState().createTranslationOverAngle(distance / getState().getZoomFactor(),
				getState().getAngleInRadians() + Math.PI / 2), durationMillis);
	}

	public void slideToBottomInImageCoordinates(double distance, long durationMillis) {
		animateTo(getState().createTranslation(0, distance), durationMillis);
	}

	public void slideToBottomInViewCoordinates(double distance, long durationMillis) {
		animateTo(getState().createTranslationOverAngle(distance / getState().getZoomFactor(),
				getState().getAngleInRadians() - Math.PI / 2), durationMillis);
	}

	public void rotateOverCenter(double angleInRadians, long durationMillis) {
		animateTo(getState().createRotation(angleInRadians), durationMillis);
	}

	public void zoom(double zoomFactor, long durationMillis) {
		animateTo(getState().createZoom(zoomFactor), durationMillis);
	}

	public void animatePath(SlidingImagePath path, long durationMillis) {
		moveTo(path.getStartState());
		animateTo(path.getEndState(), durationMillis);
	}

	public void animateTo(SlidingImageState state, long durationMillis) {
		synchronized (this) {
			long now = System.nanoTime();
			setStartTimeNanos(now);
			setStartState(getState().clone());
			setTargetTimeNanos(now + Math.max(durationMillis, 0L) * 1000000);
			setTargetState(state);
		}
	}

	public void stopAnimating() {
		moveTo(getState());
	}

	public void moveTo(SlidingImageState state) {
		synchronized (this) {
			setTargetTimeNanos(System.nanoTime());
			setTargetState(state);
		}
		changeState(state);
		refreshUI();
	}

	private void updateImageOpacityOverTime() {
		float opacity = 1f;
		long now = System.nanoTime();
		long st = getStartTimeNanos();
		long tt = getTargetTimeNanos();
		long dur = tt - st;
		long fin = getFadeInDurationMillis() * 1000000L;
		long fout = getFadeOutDurationMillis() * 1000000L;
		long fdur = fin + fout;
		if (fdur > dur) {
			double s = dur / (double) fdur;
			fin = (long) Math.floor(fin * s);
			fout = (long) Math.floor(fout * s);
		}
		if (fin > 0L && now < st + fin) {
			opacity = (now - st) / (float) fin;
		} else if (fout > 0L && now > tt - fout) {
			opacity = -(now - tt) / (float) fout;
		}
		opacity = Math.max(Math.min(opacity, 1f), 0f);
		setImageOpacity(opacity);
	}

	private void updateStateOverTime() {
		SlidingImageState newState = null;
		synchronized (this) {
			long now = System.nanoTime();
			long tt = getTargetTimeNanos();
			if (now >= tt) {
				newState = getTargetState();
			} else {
				long st = getStartTimeNanos();
				double r = (now - st) / (double) (tt - st);
				newState = getStartState().createInterpolation(getTargetState(), r);
			}
		}
		changeState(newState);
	}

	private void changeState(SlidingImageState newState) {
		SlidingImageState oldState = getState();
		setState(newState);
		if (!newState.equals(oldState)) {
			fireStateChanged();
		}
		boolean animatingBefore = isLastAnimating();
		boolean animating = isAnimating();
		setLastAnimating(animating);
		if (!animatingBefore && animating) {
			fireStartAnimating();
		} else if (animatingBefore && !animating) {
			fireStopAnimating();
		}
	}

	private void fireImageChanged() {
		for (SlidingImageListener listener : getListeners()) {
			listener.notifyImageChanged(this);
		}
	}

	private void fireStateChanged() {
		for (SlidingImageListener listener : getListeners()) {
			listener.notifyStateChanged(this);
		}
	}

	private void fireStartAnimating() {
		for (SlidingImageListener listener : getListeners()) {
			listener.notifyStartAnimating(this);
		}
	}

	private void fireStopAnimating() {
		for (SlidingImageListener listener : getListeners()) {
			listener.notifyStopAnimating(this);
		}
	}

	@Override
	public boolean isAnimating() {
		return !getState().equals(getTargetState());
	}

	public void resetImage() {
		changeImage(null, new SlidingImageState());
	}

	public void changeImage(Image image) {
		changeImage(image, createInitialState(image));
	}

	public void changeImage(Image image, SlidingImageState initialState) {
		setImage(image);
		moveTo(initialState);
		fireImageChanged();
	}

	public SlidingImageState createInitialState(Image image) {
		SlidingImageState state = new SlidingImageState();
		state.setCenterX(ImageUtils.getWidth(image) / 2.0);
		state.setCenterY(ImageUtils.getHeight(image) / 2.0);
		return state;
	}

	public boolean hasImage() {
		return getImage() != null;
	}

	public Image getImage() {
		return image;
	}

	private void setImage(Image image) {
		this.image = image;
	}

	public Image getImageOverlay() {
		return imageOverlay;
	}

	public void setImageOverlay(Image overlay) {
		this.imageOverlay = overlay;
		refreshUI();
	}

	public Composite getImageOverlayComposite() {
		return imageOverlayComposite;
	}

	public void setImageOverlayComposite(Composite composite) {
		this.imageOverlayComposite = composite;
		refreshUI();
	}

	public Border getBorder() {
		return getPanel().getBorder();
	}

	public void setBorder(Border border) {
		getPanel().setBorder(border);
		refreshUI();
	}

	public Composite getBorderComposite() {
		return borderComposite;
	}

	public void setBorderComposite(Composite composite) {
		this.borderComposite = composite;
		refreshUI();
	}

	public SlidingImageState getState() {
		return state;
	}

	private void setState(SlidingImageState state) {
		this.state = state;
	}

	private SlidingImageState getStartState() {
		return startState;
	}

	private void setStartState(SlidingImageState startState) {
		this.startState = startState;
	}

	private SlidingImageState getTargetState() {
		return targetState;
	}

	private void setTargetState(SlidingImageState targetState) {
		this.targetState = targetState;
	}

	private long getStartTimeNanos() {
		return startTimeNanos;
	}

	private void setStartTimeNanos(long startTimeNanos) {
		this.startTimeNanos = startTimeNanos;
	}

	private long getTargetTimeNanos() {
		return targetTimeNanos;
	}

	private void setTargetTimeNanos(long targetTimeNanos) {
		this.targetTimeNanos = targetTimeNanos;
	}

	public long getFadeInDurationMillis() {
		return fadeInDurationMillis;
	}

	public void setFadeInDurationMillis(long durationMillis) {
		this.fadeInDurationMillis = durationMillis;
	}

	public long getFadeOutDurationMillis() {
		return fadeOutDurationMillis;
	}

	public void setFadeOutDurationMillis(long durationMillis) {
		this.fadeOutDurationMillis = durationMillis;
	}

	private float getImageOpacity() {
		return imageOpacity;
	}

	private void setImageOpacity(float opacity) {
		this.imageOpacity = opacity;
	}

	private boolean isLastAnimating() {
		return lastAnimating;
	}

	private void setLastAnimating(boolean animating) {
		this.lastAnimating = animating;
	}

	public boolean isImageAlwaysCoveringUI() {
		return imageAlwaysCoveringUi;
	}

	public void setImageAlwaysCoveringUI(boolean covering) {
		this.imageAlwaysCoveringUi = covering;
	}

	protected GenericListenerList<SlidingImageListener> getListeners() {
		return listeners;
	}

	@SuppressWarnings("serial")
	private class SlidingImagePanel extends AnimatedPanel {

		public SlidingImagePanel(Dimension size, Color background) {
			super(size, background);
		}

		@Override
		protected void updateStateBetweenPaints(Graphics2D g, long elapsedTimeMillis) {
			updateImageOpacityOverTime();
			updateStateOverTime();
		}

		@Override
		protected void doPaintComponent(Graphics2D g) {
			float opacity = getImageOpacity();
			if (opacity == 0f || (isOpaque() && (!isImageAlwaysCoveringUI() || opacity < 1f))) {
				paintBackground(g);
			}
			Image image = getImage();
			if (image != null && opacity > 0f) {
				paintImage(g, image, getState(), opacity);
			}
			Image overlay = getImageOverlay();
			if (overlay != null) {
				paintImageOverlay(g, overlay);
			}
		}

		protected void paintImage(Graphics2D g, Image image, SlidingImageState state, float opacity) {
			Graphics2D gimg = (Graphics2D) g.create();
			gimg.translate(getWidth() / 2, getHeight() / 2);
			gimg.transform(state.getTransform());
			if (opacity < 1f) {
				gimg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			}
			gimg.drawImage(image, 0, 0, null);
			gimg.dispose();
		}

		protected void paintImageOverlay(Graphics2D g, Image overlay) {
			Composite previousComposite = g.getComposite();
			Composite composite = getImageOverlayComposite();
			if (composite != null)
				g.setComposite(composite);
			g.drawImage(overlay, 0, 0, null);
			g.setComposite(previousComposite);
		}

		@Override
		protected void paintBorder(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Composite previousComposite = g2.getComposite();
			Composite composite = getBorderComposite();
			if (composite != null)
				g2.setComposite(composite);
			super.paintBorder(g2);
			g2.setComposite(previousComposite);
		}

	}

}