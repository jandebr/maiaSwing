package org.maia.swing.animate;

import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.maia.util.SystemUtils;

public class AnimatedComponentPainter extends Thread {

	private int paintsPerSecond;

	private List<AnimatedComponent> components;

	private boolean stop;

	public static int defaultPaintsPerSecond = 25;

	private static Map<Integer, AnimatedComponentPainter> reusableAnimatedPainters; // indexed on paintsPerSecond

	static {
		reusableAnimatedPainters = new HashMap<Integer, AnimatedComponentPainter>();
	}

	private AnimatedComponentPainter(int paintsPerSecond) {
		super("Animated Component Painter @" + paintsPerSecond + "fps");
		this.paintsPerSecond = paintsPerSecond;
		this.components = new Vector<AnimatedComponent>();
		setDaemon(true);
		setPriority(Thread.NORM_PRIORITY + 1);
	}

	public static AnimatedComponentPainter getPainter(int paintsPerSecond) {
		AnimatedComponentPainter painter = null;
		synchronized (reusableAnimatedPainters) {
			painter = reusableAnimatedPainters.get(paintsPerSecond);
			if (painter == null) {
				painter = new AnimatedComponentPainter(paintsPerSecond);
				reusableAnimatedPainters.put(paintsPerSecond, painter);
			}
		}
		return painter;
	}

	public void addComponent(AnimatedComponent component) {
		synchronized (getComponents()) {
			if (!getComponents().contains(component)) {
				getComponents().add(component);
				if (!isAlive()) {
					start();
				}
			}
		}
	}

	public void removeComponent(AnimatedComponent component) {
		synchronized (getComponents()) {
			getComponents().remove(component);
			if (!hasComponents()) {
				stopPainting();
				synchronized (reusableAnimatedPainters) {
					reusableAnimatedPainters.remove(getPaintsPerSecond());
				}
			}
		}
	}

	public void removeAllComponents() {
		synchronized (getComponents()) {
			getComponents().clear();
		}
	}

	@Override
	public void run() {
		long intervalMillis = 1000L / getPaintsPerSecond();
		long intervalNanos = intervalMillis * 1000000L;
		while (!isStopped()) {
			long t0 = System.nanoTime();
			if (hasComponents()) {
				paintComponents();
			}
			SystemUtils.sleepNanos(intervalNanos - (System.nanoTime() - t0));
		}
	}

	private void paintComponents() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					synchronized (getComponents()) {
						for (AnimatedComponent component : getComponents()) {
							JComponent ui = component.getUI();
							if (ui.isShowing()) {
								ui.paintImmediately(0, 0, ui.getWidth(), ui.getHeight());
							}
						}
					}
					Toolkit.getDefaultToolkit().sync();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
		}
	}

	public boolean hasComponents() {
		return !getComponents().isEmpty();
	}

	public void stopPainting() {
		stop = true;
	}

	public boolean isStopped() {
		return stop;
	}

	public int getPaintsPerSecond() {
		return paintsPerSecond;
	}

	private List<AnimatedComponent> getComponents() {
		return components;
	}

}