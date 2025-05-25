package org.maia.swing.animate.textslide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;

import org.maia.swing.animate.itemslide.SlidingCursorMovement;
import org.maia.swing.animate.itemslide.SlidingItem;
import org.maia.swing.animate.itemslide.SlidingItemComponentAdapter;
import org.maia.swing.animate.itemslide.SlidingItemListComponent;
import org.maia.swing.animate.itemslide.SlidingItemRange;
import org.maia.swing.animate.itemslide.impl.SlidingItemLayoutManagerFactory;
import org.maia.swing.animate.itemslide.outline.SlidingItemListOutlineView;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.swing.text.TextLabel;

public class SlidingTextComponent extends SlidingItemListComponent {

	SlidingTextComponent(Dimension size, Insets padding, Color background) {
		super(size, padding, background, SlidingCursorMovement.NONE);
		setLayoutManager(SlidingItemLayoutManagerFactory.createVerticallySlidingTopAlignedLayout(this,
				HorizontalAlignment.LEFT));
		setSlidingCursor(null);
	}

	public void removeAllTextLines() {
		removeAllItems();
		keepSlidingRangeFitToText();
	}

	public void addTextLine(TextLabel line) {
		addTextLine(line, null);
	}

	public void addTextLine(TextLabel line, TextSpacer spaceBefore) {
		addTextLine(line, spaceBefore, null);
	}

	public void addTextLine(TextLabel line, TextSpacer spaceBefore, TextSpacer spaceAfter) {
		addTextLineAsItem(line, spaceBefore, spaceAfter);
		keepSlidingRangeFitToText();
	}

	public void addTextLinesFrom(SlidingTextComponent otherComp) {
		addTextLinesFrom(otherComp, null);
	}

	public void addTextLinesFrom(SlidingTextComponent otherComp, TextSpacer spaceBefore) {
		addTextLinesFrom(otherComp, spaceBefore, null);
	}

	public void addTextLinesFrom(SlidingTextComponent otherComp, TextSpacer spaceBefore, TextSpacer spaceAfter) {
		int n = otherComp.getTextLineCount();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				TextLabel line = otherComp.getTextLine(i);
				TextSpacer sb = i == 0 ? spaceBefore : null;
				TextSpacer sa = i == n - 1 ? spaceAfter : null;
				addTextLineAsItem(line, sb, sa);
			}
			keepSlidingRangeFitToText();
		}
	}

	public void insertTextLine(TextLabel line, int index) {
		insertTextLine(line, index, null);
	}

	public void insertTextLine(TextLabel line, int index, TextSpacer spaceBefore) {
		insertTextLine(line, index, spaceBefore, null);
	}

	public void insertTextLine(TextLabel line, int index, TextSpacer spaceBefore, TextSpacer spaceAfter) {
		insertTextLineAsItem(line, index, spaceBefore, spaceAfter);
		keepSlidingRangeFitToText();
	}

	public void insertTextLinesFrom(SlidingTextComponent otherComp, int index) {
		insertTextLinesFrom(otherComp, index, null);
	}

	public void insertTextLinesFrom(SlidingTextComponent otherComp, int index, TextSpacer spaceBefore) {
		insertTextLinesFrom(otherComp, index, spaceBefore, null);
	}

	public void insertTextLinesFrom(SlidingTextComponent otherComp, int index, TextSpacer spaceBefore,
			TextSpacer spaceAfter) {
		int n = otherComp.getTextLineCount();
		if (n > 0) {
			for (int i = n - 1; i >= 0; i--) {
				TextLabel line = otherComp.getTextLine(i);
				TextSpacer sb = i == 0 ? spaceBefore : null;
				TextSpacer sa = i == n - 1 ? spaceAfter : null;
				insertTextLineAsItem(line, index, sb, sa);
			}
			keepSlidingRangeFitToText();
		}
	}

	private void addTextLineAsItem(TextLabel line, TextSpacer spaceBefore, TextSpacer spaceAfter) {
		if (line != null) {
			addItem(createTextLineItem(line, spaceBefore, spaceAfter));
		}
	}

	private void insertTextLineAsItem(TextLabel line, int index, TextSpacer spaceBefore, TextSpacer spaceAfter) {
		if (line != null) {
			insertItem(createTextLineItem(line, spaceBefore, spaceAfter), index);
		}
	}

	private SlidingItem createTextLineItem(TextLabel line, TextSpacer spaceBefore, TextSpacer spaceAfter) {
		if (line.getWidth() == 0 || line.getHeight() == 0) {
			line.setSize(line.getPreferredSize());
		}
		Insets margin = new Insets(0, 0, 0, 0);
		if (spaceBefore != null) {
			margin.top = spaceBefore.getSpaceInPixels(line.getFont());
		}
		if (spaceAfter != null) {
			margin.bottom = spaceAfter.getSpaceInPixels(line.getFont());
		}
		return new SlidingItemComponentAdapter(line, margin);
	}

	private void keepSlidingRangeFitToText() {
		if (getItemSelectionRange() != null) {
			fitSlidingRangeToText();
		}
	}

	public void fitSlidingRangeToText() {
		int yMax = getItemListLengthInPixels() - getViewportHeight();
		if (getItemCount() <= 1 || yMax <= 0) {
			setItemSelectionRange(new SlidingItemRange(0, 0));
		} else {
			double yTrans = getState().getItemTranslation();
			for (int i = getItemCount() - 2; i >= 0; i--) {
				Rectangle bounds = getItemBounds(getItemInList(i));
				int yItem = (int) Math.floor(bounds.getMinY() - yTrans);
				if (yItem < yMax) {
					setItemSelectionRange(new SlidingItemRange(0, i + 1));
					break;
				}
			}
		}
	}

	public boolean hasTextLines() {
		return hasItems();
	}

	public int getTextLineCount() {
		return getItemCount();
	}

	public TextLabel getTextLine(int index) {
		TextLabel line = null;
		SlidingItem item = getItem(index);
		if (item != null) {
			try {
				line = (TextLabel) ((SlidingItemComponentAdapter) item).getComponent();
			} catch (ClassCastException e) {
			}
		}
		return line;
	}

	public SlidingTextOutline createOutline(int thickness) {
		return new SlidingTextOutline(this, thickness);
	}

	@SuppressWarnings("serial")
	public static class SlidingTextOutline extends SlidingItemListOutlineView {

		private SlidingTextOutline(SlidingTextComponent component, int thickness) {
			super(component, thickness, component.getBackground());
			setCursorRenderer(null);
		}

		@Override
		public SlidingTextComponent getComponent() {
			return (SlidingTextComponent) super.getComponent();
		}

	}

	public static abstract class TextSpacer {

		protected TextSpacer() {
		}

		public abstract int getSpaceInPixels(Font font);

	}

	public static class LinesTextSpacer extends TextSpacer {

		private float spaceInLines;

		public LinesTextSpacer(float spaceInLines) {
			this.spaceInLines = spaceInLines;
		}

		@Override
		public int getSpaceInPixels(Font font) {
			return Math.round(getSpaceInLines() * TextLabel.getLineHeight(font));
		}

		public float getSpaceInLines() {
			return spaceInLines;
		}

	}

	public static class AbsoluteTextSpacer extends TextSpacer {

		private int spaceInPixels;

		public AbsoluteTextSpacer(int spaceInPixels) {
			this.spaceInPixels = spaceInPixels;
		}

		@Override
		public int getSpaceInPixels(Font font) {
			return spaceInPixels;
		}

	}

}