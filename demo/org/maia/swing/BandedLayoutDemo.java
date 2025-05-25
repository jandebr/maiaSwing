package org.maia.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JPanel;

import org.maia.swing.layout.BandedLayout;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.swing.layout.VerticalAlignment;
import org.maia.swing.layout.BandedLayout.Band;

public class BandedLayoutDemo {

	public static void main(String[] args) {
		new BandedLayoutDemo().startDemo();
	}

	private void startDemo() {
		Component box1 = Box.createVerticalStrut(200);
		Component box2a = Box.createRigidArea(new Dimension(200, 100));
		Component box2b = Box.createRigidArea(new Dimension(300, 150));
		Component box3 = Box.createVerticalStrut(100);
		JPanel panel = new JPanel();
		panel.add(box2a);
		panel.add(box2b);
		BandedLayout lm = new BandedLayout();
		Band root = lm.createVerticalContainerBand(1f);
		Band mid = lm.createHorizontalContainerBand(1f, panel, HorizontalAlignment.RIGHT);
		mid.setMargin(new Insets(10, 20, 10, 20));
		mid.addSubBand(lm.createComponentBand(1f, box2a, VerticalAlignment.TOP));
		mid.addSubBand(lm.createComponentBand(1f, box2b, VerticalAlignment.BOTTOM));
		root.addSubBand(lm.createComponentBand(1f, box1));
		root.addSubBand(mid);
		root.addSubBand(lm.createComponentBand(1f, box3));
		lm.layout(root, new Dimension(800, 600));
		System.out.println("box1 : " + box1.getBounds());
		System.out.println("panel : " + panel.getBounds());
		System.out.println("box2a : " + box2a.getBounds());
		System.out.println("box2b : " + box2b.getBounds());
		System.out.println("box3 : " + box3.getBounds());
	}

}