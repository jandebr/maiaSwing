package org.maia.swing.animate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.maia.swing.SwingUtils;

public abstract class BaseAnimatedComponent implements AnimatedComponent {

	private AnimatedPanel panel;

	private boolean higherQualityRenderingEnabled = true;

	/**
	 * Note: <em>optimized rendering</em> not necessarily implies faster rendering. In general "optimized" refers to
	 * less drawing operations and may involve the use of a back buffer, whose performance is system dependent
	 */
	private boolean optimizedRenderingEnabled = true;

	private boolean obscured;

	private boolean repaintFully;

	private boolean repaintClientDriven;

	private int refreshRate;

	private AnimatedComponentPainter animatedPainter;

	public static boolean logMetrics = false;

	protected BaseAnimatedComponent(Dimension size, Color background) {
		this.panel = createAnimatedPanel(size, background);
		setRepaintFully(true);
		setRefreshRate(AnimatedComponentPainter.defaultPaintsPerSecond);
	}

	protected abstract AnimatedPanel createAnimatedPanel(Dimension size, Color background);

	@Override
	public void refreshUI() {
		setRepaintFully(true);
		if (getUI().isShowing() && !isObscured()) {
			getUI().repaint();
		}
	}

	@Override
	public void notifyObscured(boolean obscured) {
		setObscured(obscured);
	}

	private synchronized void updateRepaintMechanism() {
		if (isRepaintClientDriven() || getUI() == null || !getUI().isShowing() || getRefreshRate() <= 0) {
			unregisterWithAnimatedPainter();
		} else {
			registerWithAnimatedPainter();
		}
	}

	private synchronized void unregisterWithAnimatedPainter() {
		AnimatedComponentPainter painter = getAnimatedPainter();
		if (painter != null) {
			painter.removeComponent(this);
			setAnimatedPainter(null);
		}
	}

	private synchronized void registerWithAnimatedPainter() {
		AnimatedComponentPainter painter = getAnimatedPainter();
		int refreshRate = getRefreshRate();
		if (painter != null && painter.getPaintsPerSecond() != refreshRate) {
			unregisterWithAnimatedPainter();
			painter = null;
		}
		if (painter == null) {
			painter = AnimatedComponentPainter.getPainter(refreshRate);
			painter.addComponent(this);
			setAnimatedPainter(painter);
		}
	}

	public int getWidth() {
		return getPanel().getWidth();
	}

	public int getHeight() {
		return getPanel().getHeight();
	}

	public Color getBackground() {
		return getPanel().getBackground();
	}

	public void setBackground(Color color) {
		getPanel().setBackground(color);
		refreshUI();
	}

	@Override
	public JComponent getUI() {
		return getPanel();
	}

	protected AnimatedPanel getPanel() {
		return panel;
	}

	public boolean isHigherQualityRenderingEnabled() {
		return higherQualityRenderingEnabled;
	}

	public void setHigherQualityRenderingEnabled(boolean enabled) {
		this.higherQualityRenderingEnabled = enabled;
		refreshUI();
	}

	public boolean isOptimizedRenderingEnabled() {
		return optimizedRenderingEnabled;
	}

	public void setOptimizedRenderingEnabled(boolean enabled) {
		this.optimizedRenderingEnabled = enabled;
	}

	@Override
	public boolean isObscured() {
		return obscured;
	}

	private void setObscured(boolean obscured) {
		this.obscured = obscured;
	}

	protected boolean isRepaintFully() {
		return repaintFully;
	}

	protected void setRepaintFully(boolean repaintFully) {
		this.repaintFully = repaintFully;
	}

	@Override
	public boolean isRepaintClientDriven() {
		return repaintClientDriven;
	}

	@Override
	public void setRepaintClientDriven(boolean clientDriven) {
		this.repaintClientDriven = clientDriven;
		updateRepaintMechanism();
	}

	@Override
	public int getRefreshRate() {
		return refreshRate;
	}

	public void setRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
		updateRepaintMechanism();
	}

	private AnimatedComponentPainter getAnimatedPainter() {
		return animatedPainter;
	}

	private void setAnimatedPainter(AnimatedComponentPainter painter) {
		animatedPainter = painter;
	}

	private static boolean isLogMetrics() {
		return logMetrics;
	}

	@SuppressWarnings("serial")
	protected abstract class AnimatedPanel extends JPanel {

		private long lastTimePainted = -1L;

		private long paintMetricsStartTime = -1L;

		private PaintMetrics paintMetrics;

		private AffineTransform previousPaintTransform;

		protected AnimatedPanel(Dimension size, Color background) {
			this(size, background, true);
		}

		protected AnimatedPanel(Dimension size, Color background, boolean doubleBuffered) {
			super(new BorderLayout(), doubleBuffered);
			SwingUtils.fixSize(this, size);
			setBackground(background);
			setOpaque(true);
			setName(BaseAnimatedComponent.this.getClass().getSimpleName());
			setPaintMetrics(createPaintMetrics());
			addAncestorListener(new RepaintMechanismUpdater());
		}

		protected PaintMetrics createPaintMetrics() {
			return new PaintMetrics();
		}

		@Override
		protected final void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			initializePaint(g2);
			long t = System.currentTimeMillis();
			if (!isFirstTimePainted()) {
				updateStateBetweenPaints(g2, t - getLastTimePainted());
			}
			doPaintComponent(g2);
			setRepaintFully(!isOptimizedRenderingEnabled());
			setPreviousPaintTransform(g2.getTransform());
			setLastTimePainted(t);
			updateMetricsAfterPainting(getPaintMetrics(), t);
			g2.dispose();
		}

		protected void initializePaint(Graphics2D g) {
			if (isHigherQualityRenderingEnabled()) {
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			} else {
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			}
		}

		protected abstract void updateStateBetweenPaints(Graphics2D g, double elapsedTimeMillis);

		protected abstract void doPaintComponent(Graphics2D g);

		protected void paintBackground(Graphics2D g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		protected boolean isPaintOptimized(Graphics2D g) {
			return isOptimizedRenderingEnabled() && !isObscured() && !isRepaintFully()
					&& getPreviousPaintTransform() != null && getPreviousPaintTransform().equals(g.getTransform());
		}

		private void updateMetricsAfterPainting(PaintMetrics metrics, long paintStartTime) {
			metrics.incrementPaintCount();
			if (getPaintMetricsStartTime() >= 0) {
				long timePassed = System.currentTimeMillis() - getPaintMetricsStartTime();
				if (timePassed >= 1000L) {
					logMetrics(metrics, timePassed);
					setPaintMetricsStartTime(paintStartTime);
					metrics.reset();
				}
			} else {
				setPaintMetricsStartTime(paintStartTime);
			}
		}

		private void logMetrics(PaintMetrics metrics, long timePassed) {
			if (isLogMetrics()) {
				System.out.println("[" + timePassed + "ms] [" + getName() + "] " + metrics);
			}
		}

		protected boolean isFirstTimePainted() {
			return getLastTimePainted() < 0L;
		}

		private long getLastTimePainted() {
			return lastTimePainted;
		}

		private void setLastTimePainted(long time) {
			this.lastTimePainted = time;
		}

		private long getPaintMetricsStartTime() {
			return paintMetricsStartTime;
		}

		private void setPaintMetricsStartTime(long time) {
			this.paintMetricsStartTime = time;
		}

		protected PaintMetrics getPaintMetrics() {
			return paintMetrics;
		}

		private void setPaintMetrics(PaintMetrics metrics) {
			this.paintMetrics = metrics;
		}

		private AffineTransform getPreviousPaintTransform() {
			return previousPaintTransform;
		}

		private void setPreviousPaintTransform(AffineTransform transform) {
			this.previousPaintTransform = transform;
		}

	}

	private class RepaintMechanismUpdater implements AncestorListener {

		public RepaintMechanismUpdater() {
		}

		@Override
		public void ancestorAdded(AncestorEvent event) {
			updateRepaintMechanism();
			setRepaintFully(true);
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			unregisterWithAnimatedPainter();
			setRepaintFully(true);
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			// no action
		}

	}

	protected static class PaintMetrics {

		private int paintCount;

		public PaintMetrics() {
		}

		public void incrementPaintCount() {
			setPaintCount(getPaintCount() + 1);
		}

		public void reset() {
			setPaintCount(0);
		}

		@Override
		public String toString() {
			return "Paints: " + getPaintCount();
		}

		public int getPaintCount() {
			return paintCount;
		}

		private void setPaintCount(int paintCount) {
			this.paintCount = paintCount;
		}

	}

}