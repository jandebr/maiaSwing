package org.maia.swing.animate.overlay;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import org.maia.swing.animate.AnimatedComponent;
import org.maia.swing.animate.BaseAnimatedComponent;
import org.maia.util.GenericListenerList;

public class ColorOverlayComponent extends BaseAnimatedComponent {

	private Color overlayColor;

	private Content content;

	private double previousTranslucency;

	private double translucency;

	private double startTranslucency;

	private double targetTranslucency;

	private long startTime;

	private long targetTime;

	private boolean lastAnimating;

	private GenericListenerList<ColorOverlayListener> listeners;

	private static final double MINIMUM_TRANSLUCENCY = 0; // opaque color

	private static final double MAXIMUM_TRANSLUCENCY = 1.0; // transparently showing content

	public ColorOverlayComponent(Color overlayColor, JComponent component) {
		this(component.getBackground(), overlayColor, component);
	}

	public ColorOverlayComponent(Color background, Color overlayColor, JComponent component) {
		this(new Dimension(component.getWidth(), component.getHeight()), background, overlayColor, component);
	}

	public ColorOverlayComponent(Dimension size, Color background, Color overlayColor, JComponent component) {
		this(size, background, overlayColor, new JComponentContent(component));
	}

	public ColorOverlayComponent(Color overlayColor, AnimatedComponent component) {
		this(component.getUI().getBackground(), overlayColor, component);
	}

	public ColorOverlayComponent(Color background, Color overlayColor, AnimatedComponent component) {
		this(new Dimension(component.getUI().getWidth(), component.getUI().getHeight()), background, overlayColor,
				component);
	}

	public ColorOverlayComponent(Dimension size, Color background, Color overlayColor, AnimatedComponent component) {
		this(size, background, overlayColor, new AnimatedComponentContent(component));
	}

	private ColorOverlayComponent(Dimension size, Color background, Color overlayColor, Content content) {
		super(size, background);
		this.overlayColor = overlayColor;
		this.content = content;
		this.listeners = new GenericListenerList<ColorOverlayListener>();
		getPanel().add(getContentComponent(), BorderLayout.CENTER);
	}

	@Override
	protected AnimatedPanel createAnimatedPanel(Dimension size, Color fadeOutColor) {
		return new ColorOverlayPanel(size, fadeOutColor);
	}

	public void addListener(ColorOverlayListener listener) {
		getListeners().addListener(listener);
	}

	public void removeListener(ColorOverlayListener listener) {
		getListeners().removeListener(listener);
	}

	public void removeAllListeners() {
		getListeners().removeAllListeners();
	}

	@Override
	public void notifyObscured(boolean obscured) {
		super.notifyObscured(obscured);
		getContent().update(this);
	}

	public void animateToFullTranslucency(long durationMillis) {
		animateToTranslucency(MAXIMUM_TRANSLUCENCY, durationMillis);
	}

	public void animateToFullOpacity(long durationMillis) {
		animateToTranslucency(MINIMUM_TRANSLUCENCY, durationMillis);
	}

	public void animateToTranslucency(double translucency, long durationMillis) {
		if (translucency != getTranslucency()) {
			synchronized (this) {
				long now = System.nanoTime();
				setStartTime(now);
				setStartTranslucency(getTranslucency());
				setTargetTime(now + Math.max(durationMillis, 0L) * 1000000L);
				setTargetTranslucency(translucency);
			}
		}
	}

	public void makeFullyTranslucent() {
		makeTranslucency(MAXIMUM_TRANSLUCENCY);
	}

	public void makeFullyOpaque() {
		makeTranslucency(MINIMUM_TRANSLUCENCY);
	}

	public void makeTranslucency(double translucency) {
		if (translucency != getTranslucency()) {
			synchronized (this) {
				setTargetTime(System.nanoTime());
				setTargetTranslucency(translucency);
			}
			changeTranslucency(translucency);
			refreshUI();
		}
	}

	private void updateStateOverTime() {
		double newTranslucency = 0;
		synchronized (this) {
			long now = System.nanoTime();
			long tt = getTargetTime();
			if (now >= tt) {
				newTranslucency = getTargetTranslucency();
			} else {
				long st = getStartTime();
				double r = (now - st) / (double) (tt - st);
				newTranslucency = getStartTranslucency() + r * (getTargetTranslucency() - getStartTranslucency());
			}
		}
		changeTranslucency(newTranslucency);
	}

	private void changeTranslucency(double translucency) {
		double translucencyBefore = getTranslucency();
		if (translucency != translucencyBefore) {
			setTranslucency(translucency);
			if (isFullyTranslucent()) {
				fireFullyTranslucent();
			} else if (isFullyOpaque()) {
				fireFullyOpaque();
			}
		}
		boolean animatingBefore = isLastAnimating();
		boolean animating = isAnimating();
		if (!animatingBefore && animating) {
			fireStartAnimating();
		} else if (animatingBefore && !animating) {
			fireStopAnimating();
		}
		setLastAnimating(animating);
	}

	private void fireFullyTranslucent() {
		for (ColorOverlayListener listener : getListeners()) {
			listener.notifyFullyTranslucent(this);
		}
	}

	private void fireFullyOpaque() {
		for (ColorOverlayListener listener : getListeners()) {
			listener.notifyFullyOpaque(this);
		}
	}

	private void fireStartAnimating() {
		for (ColorOverlayListener listener : getListeners()) {
			listener.notifyStartAnimating(this);
		}
	}

	private void fireStopAnimating() {
		for (ColorOverlayListener listener : getListeners()) {
			listener.notifyStopAnimating(this);
		}
	}

	@Override
	public boolean isAnimating() {
		return getTranslucency() != getTargetTranslucency();
	}

	public boolean isAnimatingToFullTranslucency() {
		return isAnimating() && getTargetTranslucency() == MAXIMUM_TRANSLUCENCY;
	}

	public boolean isAnimatingToFullOpacity() {
		return isAnimating() && getTargetTranslucency() == MINIMUM_TRANSLUCENCY;
	}

	public boolean isFullyOpaque() {
		return getTranslucency() == MINIMUM_TRANSLUCENCY;
	}

	public boolean isFullyTranslucent() {
		return getTranslucency() == MAXIMUM_TRANSLUCENCY;
	}

	private boolean wasFullyOpaque() {
		return getPreviousTranslucency() == MINIMUM_TRANSLUCENCY;
	}

	private boolean wasFullyTranslucent() {
		return getPreviousTranslucency() == MAXIMUM_TRANSLUCENCY;
	}

	public Color getOverlayColor() {
		return overlayColor;
	}

	public void setOverlayColor(Color overlayColor) {
		this.overlayColor = overlayColor;
		refreshUI();
	}

	public JComponent getContentComponent() {
		return getContent().getUI();
	}

	private Content getContent() {
		return content;
	}

	private double getPreviousTranslucency() {
		return previousTranslucency;
	}

	private void setPreviousTranslucency(double translucency) {
		this.previousTranslucency = translucency;
	}

	public double getTranslucency() {
		return translucency;
	}

	private void setTranslucency(double translucency) {
		this.translucency = translucency;
	}

	private double getStartTranslucency() {
		return startTranslucency;
	}

	private void setStartTranslucency(double startTranslucency) {
		this.startTranslucency = startTranslucency;
	}

	private double getTargetTranslucency() {
		return targetTranslucency;
	}

	private void setTargetTranslucency(double targetTranslucency) {
		this.targetTranslucency = targetTranslucency;
	}

	private long getStartTime() {
		return startTime;
	}

	private void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	private long getTargetTime() {
		return targetTime;
	}

	private void setTargetTime(long targetTime) {
		this.targetTime = targetTime;
	}

	private boolean isLastAnimating() {
		return lastAnimating;
	}

	private void setLastAnimating(boolean animating) {
		this.lastAnimating = animating;
	}

	protected GenericListenerList<ColorOverlayListener> getListeners() {
		return listeners;
	}

	@SuppressWarnings("serial")
	private class ColorOverlayPanel extends AnimatedPanel {

		private boolean paintOptimized;

		public ColorOverlayPanel(Dimension size, Color background) {
			super(size, background);
		}

		@Override
		protected void initializePaint(Graphics2D g) {
			super.initializePaint(g);
			if (isHigherQualityRenderingEnabled()) {
				g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
						RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			} else {
				g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
						RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			}
		}

		@Override
		protected void updateStateBetweenPaints(Graphics2D g, double elapsedTimeMillis) {
			setPreviousTranslucency(getTranslucency());
			updateStateOverTime();
		}

		@Override
		protected void doPaintComponent(Graphics2D g) {
			boolean optimized = isPaintOptimized(g);
			if (!optimized || isFullyOpaque()) {
				paintBackground(g);
			}
			float contentAlpha = (float) getTranslucency();
			if (contentAlpha == 0f) {
				g.setComposite(AlphaComposite.Dst);
			} else {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, contentAlpha));
			}
			setPaintOptimized(optimized);
			getContent().update(ColorOverlayComponent.this);
			// content will be painted via paintChildren()
		}

		@Override
		protected boolean isPaintOptimized(Graphics2D g) {
			return super.isPaintOptimized(g)
					&& ((wasFullyOpaque() && isFullyOpaque()) || (wasFullyTranslucent() && isFullyTranslucent()));
		}

		public boolean isPaintOptimized() {
			return paintOptimized;
		}

		private void setPaintOptimized(boolean optimized) {
			this.paintOptimized = optimized;
		}

	}

	private static abstract class Content {

		private JComponent ui;

		protected Content(JComponent ui) {
			this.ui = ui;
			ui.setOpaque(false); // ui paint (with an AlphaComposite) requires overlay panel paint
		}

		public void update(ColorOverlayComponent overlay) {
			// Subclasses to override
		}

		public JComponent getUI() {
			return ui;
		}

	}

	private static class JComponentContent extends Content {

		public JComponentContent(JComponent component) {
			super(component);
		}

	}

	private static class AnimatedComponentContent extends Content {

		private AnimatedComponent component;

		public AnimatedComponentContent(AnimatedComponent component) {
			super(component.getUI());
			this.component = component;
			component.setRepaintClientDriven(true); // overlay will delegate painting
		}

		@Override
		public void update(ColorOverlayComponent overlay) {
			boolean obscured = overlay.isObscured() || !((ColorOverlayPanel) overlay.getPanel()).isPaintOptimized();
			getComponent().notifyObscured(obscured); // when true, causes full repaint
		}

		public AnimatedComponent getComponent() {
			return component;
		}

	}

}