package org.maia.swing.animate.itemslide;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;

public class SlidingItemComponentAdapter implements SlidingItem {

	private Component component;

	private Insets margin;

	public static Insets defaultMargin = new Insets(4, 4, 4, 4);

	public SlidingItemComponentAdapter(Component component) {
		this(component, defaultMargin);
	}

	public SlidingItemComponentAdapter(Component component, Insets margin) {
		this.component = component;
		this.margin = margin;
	}

	@Override
	public void render(Graphics2D g, SlidingItemListComponent component) {
		getComponent().paint(g);
	}

	@Override
	public int getWidth(Graphics2D g) {
		return getComponent().getWidth();
	}

	@Override
	public int getHeight(Graphics2D g) {
		return getComponent().getHeight();
	}

	public Component getComponent() {
		return component;
	}

	@Override
	public Insets getMargin() {
		return margin;
	}

}