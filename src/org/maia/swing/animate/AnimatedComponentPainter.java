package org.maia.swing.animate;

import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.maia.util.SystemUtils;

public class AnimatedComponentPainter extends Thread {

	private int paintsPerSecond;

	private List<AnimatedComponent> components;

	private boolean stop;

	public static int defaultPaintsPerSecond = 25;

	public AnimatedComponentPainter() {
		this(defaultPaintsPerSecond);
	}

	public AnimatedComponentPainter(int paintsPerSecond) {
		super("Animated Component Painter @" + paintsPerSecond + "fps");
		this.paintsPerSecond = paintsPerSecond;
		this.components = new Vector<AnimatedComponent>();
		setDaemon(true);
		setPriority(Thread.NORM_PRIORITY + 1);
	}

	public void addComponent(AnimatedComponent component) {
		synchronized (getComponents()) {
			if (!getComponents().contains(component)) {
				getComponents().add(component);
			}
		}
	}

	public void removeComponent(AnimatedComponent component) {
		synchronized (getComponents()) {
			getComponents().remove(component);
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