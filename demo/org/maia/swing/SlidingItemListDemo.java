package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.maia.swing.animate.itemslide.SlidingCursorMovement;
import org.maia.swing.animate.itemslide.SlidingItem;
import org.maia.swing.animate.itemslide.SlidingItemListAdapter;
import org.maia.swing.animate.itemslide.SlidingItemListComponent;
import org.maia.swing.animate.itemslide.impl.SlidingItemLayoutManagerFactory;
import org.maia.swing.animate.itemslide.impl.SlidingShadeFactory;
import org.maia.swing.animate.itemslide.outline.SlidingItemListOutlineView;
import org.maia.swing.animate.itemslide.outline.SolidFillOutlineRenderer;
import org.maia.swing.border.CurvedCornerLineBorder;
import org.maia.swing.layout.VerticalAlignment;
import org.maia.util.SystemUtils;

public class SlidingItemListDemo extends SlidingItemListAdapter implements KeyListener {

	private SlidingItemListComponent component;

	private SlidingItemListOutlineView outlineView;

	public static void main(String[] args) {
		new SlidingItemListDemo().startDemo();
	}

	private void startDemo() {
		setComponent(createSlidingItemListComponent());
		setOutlineView(createOutlineView());
		showFrame();
		SystemUtils.sleep(100L);
		getComponent().addListener(this);
		getComponent().getUI().grabFocus();
		getComponent().getUI().addKeyListener(this);
	}

	private SlidingItemListComponent createSlidingItemListComponent() {
		SlidingItemListComponent comp = new SlidingItemListComponent(new Dimension(1200, 300), Color.BLACK,
				SlidingCursorMovement.LAZY);
		comp.setLayoutManager(SlidingItemLayoutManagerFactory.createHorizontallySlidingCenterAlignedLayout(comp,
				VerticalAlignment.CENTER));
		comp.setShade(SlidingShadeFactory.createGradientShadeRelativeLength(comp, 0.15));
		for (int i = 0; i < 26; i++) {
			char c = (char) (65 + i);
			int width = 80 + (int) Math.round(Math.random() * 80);
			int height = 140 + (int) Math.round(Math.random() * 40);
			comp.addItem(new SampleItem(c, width, height));
		}
		return comp;
	}

	private SlidingItemListOutlineView createOutlineView() {
		SlidingItemListOutlineView view = getComponent().createOutlineViewMatchingOrientationAndLength(20);
		view.setBorder(new CurvedCornerLineBorder.Absolute(new Color(38, 35, 30), 5f));
		view.setExtentMargin(new Insets(1, 0, 1, 0));
		view.setExtentRenderer(null);
		view.setCursorBorder(new CurvedCornerLineBorder.Absolute(new Color(66, 60, 44), 5f));
		view.setCursorRenderer(new SolidFillOutlineRenderer(new Color(92, 83, 63)));
		return view;
	}

	private void showFrame() {
		JFrame frame = new JFrame("Sliding item list");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.add(createPanel(), BorderLayout.CENTER);
		frame.add(Box.createVerticalStrut(40), BorderLayout.NORTH);
		frame.add(Box.createVerticalStrut(40), BorderLayout.SOUTH);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.WEST);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.EAST);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(getComponent().getUI(), BorderLayout.CENTER);
		panel.add(getOutlineView(), BorderLayout.SOUTH);
		return panel;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		int code = event.getKeyCode();
		if (code == KeyEvent.VK_LEFT) {
			getComponent().slideToPreviousItem();
		} else if (code == KeyEvent.VK_RIGHT) {
			getComponent().slideToNextItem();
		} else if (code == KeyEvent.VK_PAGE_DOWN) {
			getComponent().moveToNextPage();
		} else if (code == KeyEvent.VK_PAGE_UP) {
			getComponent().moveToPreviousPage();
		} else if (code == KeyEvent.VK_HOME) {
			getComponent().moveToFirstItem();
		} else if (code == KeyEvent.VK_END) {
			getComponent().moveToLastItem();
		} else if (code == KeyEvent.VK_ENTER) {
			SampleItem item = (SampleItem) getComponent().getSelectedItem();
			if (item != null) {
				System.out.println("Selected: " + item.getMnemonic());
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void notifyItemSelectionLanded(SlidingItemListComponent component, SlidingItem selectedItem,
			int selectedItemIndex) {
		char mnemonic = selectedItem != null ? ((SampleItem) selectedItem).getMnemonic() : ' ';
		System.out.println("Landed on: " + mnemonic);
	}

	private SlidingItemListComponent getComponent() {
		return component;
	}

	private void setComponent(SlidingItemListComponent component) {
		this.component = component;
	}

	private SlidingItemListOutlineView getOutlineView() {
		return outlineView;
	}

	private void setOutlineView(SlidingItemListOutlineView outlineView) {
		this.outlineView = outlineView;
	}

	private static class SampleItem implements SlidingItem {

		private char mnemonic;

		private int width;

		private int height;

		public static Insets margin = new Insets(10, 10, 10, 10);

		public static Font mnemonicFont = Font.decode("Century-PLAIN-40");

		public static Font mnemonicFontSelected = Font.decode("Century-PLAIN-80");

		public static Color mnemonicColor = new Color(255, 255, 255, 150);

		public static Color mnemonicColorSelected = new Color(255, 255, 255, 255);

		public static Color bgColor = new Color(40, 40, 40);

		public SampleItem(char mnemonic, int width, int height) {
			this.mnemonic = mnemonic;
			this.width = width;
			this.height = height;
		}

		@Override
		public void render(Graphics2D g, SlidingItemListComponent component) {
			boolean selected = component.getSelectedItem().equals(this);
			int width = getWidth(g);
			int height = getHeight(g);
			String label = String.valueOf(getMnemonic());
			g.setColor(bgColor);
			g.fillRect(0, 0, width, height - 20);
			g.fillRect(0, height - 16, width, 16);
			g.setColor(selected ? mnemonicColorSelected : mnemonicColor);
			g.setFont(selected ? mnemonicFontSelected : mnemonicFont);
			int labelWidth = g.getFontMetrics().getStringBounds(label, g).getBounds().width;
			g.drawString(label, (width - labelWidth) / 2, (height + 40) / 2);
		}

		@Override
		public int getWidth(Graphics2D g) {
			return width;
		}

		@Override
		public int getHeight(Graphics2D g) {
			return height;
		}

		@Override
		public Insets getMargin() {
			return margin;
		}

		public char getMnemonic() {
			return mnemonic;
		}

	}

}