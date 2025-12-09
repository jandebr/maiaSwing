package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JFrame;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.animate.imageslide.show.SlidingImageCollectionIterator;
import org.maia.swing.animate.imageslide.show.SlidingImageShow;
import org.maia.swing.animate.imageslide.show.SlidingImageShowBuilder;
import org.maia.util.SystemUtils;

public class SlidingImageShowDemo extends KeyAdapter {

	private SlidingImageShow show;

	public static void main(String[] args) {
		new SlidingImageShowDemo().startDemo();
	}

	private void startDemo() {
		setShow(createShow());
		showFrame();
		SystemUtils.sleep(100L);
		getShow().getUI().grabFocus();
		getShow().getUI().addKeyListener(this);
	}

	private SlidingImageShow createShow() {
		SlidingImageCollectionIterator iterator = SlidingImageCollectionIterator.createPermutedRepeatingIterator();
		iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/toystory.jpg"));
		iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/toystory2.jpg"));
		iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/toystory3.jpg"));
		iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/toystory4.jpg"));
		iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/toystory5.jpg"));
		iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/toystory6.jpg"));
		SlidingImageShowBuilder builder = new SlidingImageShowBuilder(new Dimension(960, 540));
		builder.withImageIterator(iterator);
		builder.withMaxToMinZoomFactorRatio(2.0);
		/*
		 * iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/screenshot1.png"));
		 * iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/screenshot2.png"));
		 * iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/screenshot3.png"));
		 * iterator.addImage(ImageUtils.readFromResource("org/maia/swing/resources/screenshot4.png"));
		 * builder.withMaxToMinZoomFactorRatio(3.0);
		 */
		return builder.build();
	}

	private void showFrame() {
		JFrame frame = new JFrame("Sliding image show");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.add(getShow().getUI(), BorderLayout.CENTER);
		frame.add(Box.createVerticalStrut(40), BorderLayout.NORTH);
		frame.add(Box.createVerticalStrut(40), BorderLayout.SOUTH);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.WEST);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.EAST);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	@Override
	public void keyPressed(KeyEvent event) {
		int code = event.getKeyCode();
		if (code == KeyEvent.VK_ENTER) {
			getShow().startAnimating();
		} else if (code == KeyEvent.VK_END) {
			getShow().stopAnimating();
		}
	}

	private SlidingImageShow getShow() {
		return show;
	}

	private void setShow(SlidingImageShow show) {
		this.show = show;
	}

}