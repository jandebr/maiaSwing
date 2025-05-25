package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.maia.swing.animate.itemslide.outline.SolidFillOutlineRenderer;
import org.maia.swing.animate.overlay.ColorOverlayComponent;
import org.maia.swing.animate.textslide.SlidingTextComponent;
import org.maia.swing.animate.textslide.SlidingTextComponentBuilder;
import org.maia.swing.animate.textslide.SlidingTextComponent.SlidingTextOutline;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.util.SystemUtils;

public class SlidingTextDemo extends KeyAdapter {

	private SlidingTextComponent textComponent;

	private SlidingTextOutline textOutline;

	private ColorOverlayComponent overlay;

	private static Color bgColor = Color.BLACK;

	private static Color textColor = Color.WHITE;

	private static Font textFont = Font.decode("Century-PLAIN-24");

	public static void main(String[] args) throws IOException {
		new SlidingTextDemo().startDemo();
	}

	private void startDemo() throws IOException {
		setTextComponent(createSlidingTextComponent());
		setTextOutline(createSlidingTextOutline());
		setOverlay(new ColorOverlayComponent(bgColor, getTextComponent()));
		showFrame();
		SystemUtils.sleep(100L);
		getTextComponent().getUI().grabFocus();
		getTextComponent().getUI().addKeyListener(this);
	}

	private SlidingTextComponent createSlidingTextComponent() throws IOException {
		String text = readText("org/maia/swing/resources/poem.txt");
		SlidingTextComponentBuilder builder = new SlidingTextComponentBuilder(text);
		return builder.withFont(textFont).withTextColor(textColor).withBackgroundColor(bgColor).withLineWidth(600)
				.withInterParagraphSpacing(2).withHorizontalAlignment(HorizontalAlignment.CENTER)
				.withTextDimension(builder.deriveTextDimensionForComponentHeight(300, 18f, 30f)).build();
	}

	private SlidingTextOutline createSlidingTextOutline() {
		SlidingTextOutline outline = getTextComponent().createOutline(20);
		outline.setBorder(BorderFactory.createLineBorder(new Color(38, 35, 30), 1));
		outline.setExtentMargin(new Insets(0, 1, 0, 1));
		outline.setExtentBorder(BorderFactory.createLineBorder(new Color(66, 60, 44), 1));
		outline.setExtentRenderer(new SolidFillOutlineRenderer(new Color(92, 83, 63)));
		return outline;
	}

	private void showFrame() {
		JFrame frame = new JFrame("Sliding text");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(bgColor);
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
		panel.add(getOverlay().getUI(), BorderLayout.CENTER);
		panel.add(getTextOutline(), BorderLayout.WEST);
		return panel;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		int code = event.getKeyCode();
		if (code == KeyEvent.VK_UP) {
			getTextComponent().slideToPreviousItem();
		} else if (code == KeyEvent.VK_DOWN) {
			getTextComponent().slideToNextItem();
		} else if (code == KeyEvent.VK_PAGE_UP) {
			getTextComponent().moveToPreviousPage();
		} else if (code == KeyEvent.VK_PAGE_DOWN) {
			getTextComponent().moveToNextPage();
		} else if (code == KeyEvent.VK_HOME) {
			getTextComponent().moveToFirstItem();
		} else if (code == KeyEvent.VK_END) {
			getTextComponent().moveToLastItem();
		} else if (code == KeyEvent.VK_SPACE) {
			if (getOverlay().isFullyOpaque() || getOverlay().isAnimatingToFullOpacity()) {
				getOverlay().animateToFullTranslucency(3000L);
			} else {
				getOverlay().animateToFullOpacity(3000L);
			}
		}
	}

	private String readText(String resourcePath) throws IOException {
		StringBuilder sb = new StringBuilder(2048);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				ClassLoader.getSystemResource(resourcePath).openStream(), Charset.forName("UTF-8")));
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		reader.close();
		return sb.toString();
	}

	private SlidingTextComponent getTextComponent() {
		return textComponent;
	}

	private void setTextComponent(SlidingTextComponent textComponent) {
		this.textComponent = textComponent;
	}

	private SlidingTextOutline getTextOutline() {
		return textOutline;
	}

	private void setTextOutline(SlidingTextOutline textOutline) {
		this.textOutline = textOutline;
	}

	private ColorOverlayComponent getOverlay() {
		return overlay;
	}

	private void setOverlay(ColorOverlayComponent overlay) {
		this.overlay = overlay;
	}

}