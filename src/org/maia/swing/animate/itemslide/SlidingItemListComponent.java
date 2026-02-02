package org.maia.swing.animate.itemslide;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.VolatileImage;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.maia.swing.SwingUtils;
import org.maia.swing.animate.BaseAnimatedComponent;
import org.maia.swing.animate.itemslide.impl.SlidingCursorFactory;
import org.maia.swing.animate.itemslide.impl.SlidingDynamicsFactory;
import org.maia.swing.animate.itemslide.impl.SlidingItemInList;
import org.maia.swing.animate.itemslide.impl.SlidingItemLayoutManagerFactory;
import org.maia.swing.animate.itemslide.impl.SlidingItemList;
import org.maia.swing.animate.itemslide.impl.SlidingShadeDynamicsFactory;
import org.maia.swing.animate.itemslide.impl.SlidingState;
import org.maia.swing.animate.itemslide.outline.SlidingItemListOutline;
import org.maia.swing.animate.itemslide.outline.SlidingItemListOutline.Range;
import org.maia.swing.animate.itemslide.outline.SlidingItemListOutlineView;
import org.maia.swing.layout.Orientation;
import org.maia.swing.layout.VerticalAlignment;
import org.maia.util.GenericListenerList;

public class SlidingItemListComponent extends BaseAnimatedComponent implements KeyListener {

	private Insets padding;

	private SlidingItemList itemList;

	private SlidingItemLayoutManager layoutManager;

	private SlidingCursorMovement cursorMovement;

	private SlidingCursor cursor;

	private SlidingDynamics slidingDynamics;

	private SlidingShade shade;

	private SlidingShadeDynamics shadeDynamics;

	private SlidingState state;

	private SlidingState targetState;

	private SlidingItemRange itemSelectionRange;

	private GenericListenerList<SlidingItemListListener> listeners;

	private int selectedItemIndex = -1;

	private int lastLandedItemIndex = -1;

	private long landingTimeMillis;

	private boolean landed;

	private boolean lastSliding;

	private boolean validatedLayout;

	private double maxItemWidth;

	private double maxItemHeight;

	private Graphics2D graphics2D;

	private boolean anyKeyPressed;

	private long steadyLandingMinimumTimeDelayMillis = 200L;

	private long steadyLandingMaximumTimeDelayMillis = 500L;

	public static Color defaultCursorColor = Color.YELLOW;

	public static Insets defaultPadding = new Insets(8, 8, 8, 8);

	public SlidingItemListComponent(Dimension size, Color background) {
		this(size, background, SlidingCursorMovement.EAGER);
	}

	public SlidingItemListComponent(Dimension size, Color background, SlidingCursorMovement cursorMovement) {
		this(size, defaultPadding, background, cursorMovement);
	}

	public SlidingItemListComponent(Dimension size, Insets padding, Color background,
			SlidingCursorMovement cursorMovement) {
		super(size, background);
		this.padding = padding;
		this.itemList = new SlidingItemList();
		this.layoutManager = createDefaultLayoutManager();
		this.cursorMovement = cursorMovement;
		this.cursor = createDefaultSlidingCursor();
		this.slidingDynamics = createDefaultSlidingDynamics();
		this.shadeDynamics = createDefaultSlidingShadeDynamics();
		this.state = new SlidingState();
		this.targetState = this.state;
		this.listeners = new GenericListenerList<SlidingItemListListener>();
		getUI().addKeyListener(this);
	}

	@Override
	protected AnimatedPanel createAnimatedPanel(Dimension size, Color background) {
		return new SlidingItemListPanel(size, background);
	}

	protected SlidingItemLayoutManager createDefaultLayoutManager() {
		return SlidingItemLayoutManagerFactory.createHorizontallySlidingLeftAlignedLayout(this,
				VerticalAlignment.CENTER);
	}

	protected SlidingCursor createDefaultSlidingCursor() {
		return SlidingCursorFactory.createSolidOutlineCursor(defaultCursorColor, 6, 1, true);
	}

	protected SlidingDynamics createDefaultSlidingDynamics() {
		return SlidingDynamicsFactory.createAdaptiveSpeedDynamics(this);
	}

	protected SlidingShadeDynamics createDefaultSlidingShadeDynamics() {
		return SlidingShadeDynamicsFactory.createOverflowToggleShadeDynamics();
	}

	public SlidingItemListOutlineView createOutlineViewMatchingOrientationAndLength(int thickness) {
		return createOutlineViewMatchingOrientationAndLength(thickness, getBackground());
	}

	public SlidingItemListOutlineView createOutlineViewMatchingOrientationAndLength(int thickness,
			Color slidingLaneColor) {
		return new SlidingItemListOutlineView(this, thickness, slidingLaneColor);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		setAnyKeyPressed(true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setAnyKeyPressed(e.getModifiersEx() != 0);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// no action
	}

	public void addListener(SlidingItemListListener listener) {
		getListeners().addListener(listener);
	}

	public void removeListener(SlidingItemListListener listener) {
		getListeners().removeListener(listener);
	}

	public void removeAllListeners() {
		getListeners().removeAllListeners();
	}

	public void addItemsFrom(SlidingItemListComponent otherComp) {
		for (int i = 0; i < otherComp.getItemCount(); i++) {
			addItem(otherComp.getItem(i));
		}
	}

	public void addItem(SlidingItem item) {
		SlidingItemList list = getItemList();
		SlidingItemInList itemInList = new SlidingItemInList(item, list);
		if (!list.contains(itemInList)) {
			list.addItem(itemInList);
			invalidateLayout();
			fireItemsChanged();
		}
	}

	public void insertItemsFrom(SlidingItemListComponent otherComp, int index) {
		for (int i = otherComp.getItemCount() - 1; i >= 0; i--) {
			insertItem(otherComp.getItem(i), index);
		}
	}

	public void insertItem(SlidingItem item, int index) {
		SlidingItemList list = getItemList();
		SlidingItemInList itemInList = new SlidingItemInList(item, list);
		if (!list.contains(itemInList)) {
			list.insertItem(itemInList, index);
			invalidateLayout();
			fireItemsChanged();
		}
	}

	public void removeItem(SlidingItem item) {
		SlidingItemList list = getItemList();
		SlidingItemInList itemInList = new SlidingItemInList(item, list);
		if (list.contains(itemInList)) {
			list.removeItem(itemInList);
			invalidateLayout();
			fireItemsChanged();
		}
	}

	public void removeItem(int index) {
		if (index >= 0 && index < getItemCount()) {
			removeItem(getItem(index));
		}
	}

	public void removeAllItems() {
		SlidingItemList list = getItemList();
		if (!list.isEmpty()) {
			list.removeAllItems();
			invalidateLayout();
			fireItemsChanged();
		}
	}

	protected void invalidateLayout() {
		setMaxItemWidth(-1.0);
		setMaxItemHeight(-1.0);
		invalidateSelectedItemIndex();
		setValidatedLayout(false);
		refreshUI();
	}

	protected void validateLayout() {
		validateLayout(getGraphics2D());
	}

	private void validateLayout(Graphics2D g) {
		if (!isValidatedLayout()) {
			if (hasItems()) {
				getLayoutManager().layoutItems(getItemList(), g);
				setState(new SlidingState());
				invalidateSelectedItemIndex();
				int initialIndex = getSelectedItemIndex();
				int initialIndexInRange = Math.max(Math.min(initialIndex, getMaximumItemSelectionIndex()),
						getMinimumItemSelectionIndex());
				setTargetState(getLayoutManager().getItemState(getItemList().getItem(initialIndexInRange), g));
				changeState(getTargetState());
			}
			setValidatedLayout(true);
		}
	}

	private void invalidateSelectedItemIndex() {
		setSelectedItemIndex(-1);
	}

	public void slideToPreviousItem() {
		gotoPreviousItem(true);
	}

	public void slideToPreviousPage() {
		gotoPreviousPage(true);
	}

	public void slideToNextItem() {
		gotoNextItem(true);
	}

	public void slideToNextPage() {
		gotoNextPage(true);
	}

	public void slideToFirstItem() {
		gotoFirstItem(true);
	}

	public void slideToLastItem() {
		gotoLastItem(true);
	}

	public void slideToItem(SlidingItem item) {
		gotoItem(item, true);
	}

	public void slideToItemIndex(int index) {
		if (index >= 0 && index < getItemCount()) {
			gotoItem(getItem(index), true);
		}
	}

	public void moveToPreviousItem() {
		gotoPreviousItem(false);
	}

	public void moveToPreviousPage() {
		gotoPreviousPage(false);
	}

	public void moveToNextItem() {
		gotoNextItem(false);
	}

	public void moveToNextPage() {
		gotoNextPage(false);
	}

	public void moveToFirstItem() {
		gotoFirstItem(false);
	}

	public void moveToLastItem() {
		gotoLastItem(false);
	}

	public void moveToItem(SlidingItem item) {
		gotoItem(item, false);
	}

	public void moveToItemIndex(int index) {
		if (index >= 0 && index < getItemCount()) {
			gotoItem(getItem(index), false);
		}
	}

	private void gotoPreviousItem(boolean slide) {
		if (hasItems()) {
			int i = getSelectedItemIndex();
			if (i > getMinimumItemSelectionIndex()) {
				gotoItem(getItemList().getItem(i - 1), slide);
			}
		}
	}

	private void gotoPreviousPage(boolean slide) {
		if (hasItems()) {
			int i = getSelectedItemIndex();
			if (i > getMinimumItemSelectionIndex()) {
				int j = getItemList().getIndexOf(getItemNearestToCursor(-getPageLengthInPixels()));
				j = Math.max(Math.min(j, i - 1), getMinimumItemSelectionIndex());
				gotoItem(getItemList().getItem(j), slide);
			}
		}
	}

	private void gotoNextItem(boolean slide) {
		if (hasItems()) {
			int i = getSelectedItemIndex();
			if (i < getMaximumItemSelectionIndex()) {
				gotoItem(getItemList().getItem(i + 1), slide);
			}
		}
	}

	private void gotoNextPage(boolean slide) {
		if (hasItems()) {
			int i = getSelectedItemIndex();
			if (i < getMaximumItemSelectionIndex()) {
				int j = getItemList().getIndexOf(getItemNearestToCursor(getPageLengthInPixels()));
				j = Math.min(Math.max(j, i + 1), getMaximumItemSelectionIndex());
				gotoItem(getItemList().getItem(j), slide);
			}
		}
	}

	private void gotoFirstItem(boolean slide) {
		if (hasItems()) {
			int i = getSelectedItemIndex();
			if (i > getMinimumItemSelectionIndex()) {
				gotoItem(getItemList().getItem(getMinimumItemSelectionIndex()), slide);
			}
		}
	}

	private void gotoLastItem(boolean slide) {
		if (hasItems()) {
			int i = getSelectedItemIndex();
			if (i < getMaximumItemSelectionIndex()) {
				gotoItem(getItemList().getItem(getMaximumItemSelectionIndex()), slide);
			}
		}
	}

	private void gotoItem(SlidingItem item, boolean slide) {
		int i = getItemList().getIndexOf(item);
		if (i >= 0) {
			i = Math.max(Math.min(i, getMaximumItemSelectionIndex()), getMinimumItemSelectionIndex());
			if (i != getSelectedItemIndex()) {
				gotoItem(getItemInList(i), slide);
			}
		}
	}

	private void gotoItem(SlidingItemInList itemInList, boolean slide) {
		validateLayout();
		setTargetState(getLayoutManager().getItemState(itemInList, getGraphics2D()));
		if (!slide) {
			changeState(getTargetState());
			refreshUI();
		}
	}

	private void updateStateOverTime(Graphics2D g, double elapsedTimeMillis) {
		changeState(getSlidingDynamics().getUpdatedStateTowardsTarget(getState(), getTargetState(), getItemList(), g,
				elapsedTimeMillis));
	}

	private void changeState(SlidingState newState) {
		// state change
		SlidingState oldState = getState();
		int siBefore = getSelectedItemIndex();
		setState(newState);
		if (!newState.equals(oldState)) {
			invalidateSelectedItemIndex();
			fireSlidingStateChanged();
		}
		// selection change
		int siAfter = getSelectedItemIndex();
		if (siAfter != siBefore || !isValidatedLayout()) {
			fireItemSelectionChanged(siAfter);
		}
		// landing change
		boolean slide = isSliding();
		if (slide) {
			setLanded(false);
			setLandingTimeMillis(Long.MAX_VALUE);
			setLastLandedItemIndex(-1);
		} else {
			if (siAfter != getLastLandedItemIndex() || !isValidatedLayout()) {
				setLanded(false);
				setLandingTimeMillis(System.currentTimeMillis());
				setLastLandedItemIndex(siAfter);
			} else if (isSteadyLanding()) {
				setLanded(true); // landed
				setLandingTimeMillis(Long.MAX_VALUE);
				fireItemSelectionLanded(siAfter);
			}
		}
		// sliding change
		if (isValidatedLayout()) {
			if (!isLastSliding() && slide) {
				fireStartSliding();
			} else if (isLastSliding() && !slide) {
				fireStopSliding();
			}
			setLastSliding(slide);
		} else {
			if (isLastSliding()) {
				fireStopSliding();
				setLastSliding(false);
			}
		}
	}

	private void fireItemsChanged() {
		for (SlidingItemListListener listener : getListeners()) {
			listener.notifyItemsChanged(this);
		}
	}

	private void fireItemSelectionChanged(int selectedItemIndex) {
		SlidingItem selectedItem = selectedItemIndex >= 0 ? getItemList().getItem(selectedItemIndex).getItem() : null;
		for (SlidingItemListListener listener : getListeners()) {
			listener.notifyItemSelectionChanged(this, selectedItem, selectedItemIndex);
		}
	}

	private void fireItemSelectionLanded(int selectedItemIndex) {
		SlidingItem selectedItem = selectedItemIndex >= 0 ? getItemList().getItem(selectedItemIndex).getItem() : null;
		for (SlidingItemListListener listener : getListeners()) {
			listener.notifyItemSelectionLanded(this, selectedItem, selectedItemIndex);
		}
	}

	private void fireSlidingStateChanged() {
		for (SlidingItemListListener listener : getListeners()) {
			listener.notifySlidingStateChanged(this);
		}
	}

	private void fireStartSliding() {
		for (SlidingItemListListener listener : getListeners()) {
			listener.notifyStartSliding(this);
		}
	}

	private void fireStopSliding() {
		for (SlidingItemListListener listener : getListeners()) {
			listener.notifyStopSliding(this);
		}
	}

	protected int getPageLengthInPixels() {
		if (isHorizontalLayout()) {
			return getViewportWidth() / 2;
		} else {
			return getViewportHeight() / 2;
		}
	}

	public SlidingItemListOutline getOutline() {
		SlidingItemListOutline outline = null;
		Graphics2D g = getGraphics2D();
		Rectangle ilb = getItemListBoundsInViewportCoords();
		if (ilb != null) {
			Rectangle cur = getLayoutManager().getCursorInnerBoundsInViewportCoords(getState(), g);
			Rectangle vp = getViewportBounds().intersection(ilb);
			double i0 = 0, i1 = 0, v0 = 0, v1 = 0, c0, c1;
			if (isHorizontalLayout()) {
				i0 = ilb.getMinX();
				i1 = ilb.getMaxX();
				v0 = vp.getMinX();
				v1 = vp.getMaxX();
				c0 = cur.getMinX();
				c1 = cur.getMaxX();
			} else {
				i0 = ilb.getMinY();
				i1 = ilb.getMaxY();
				v0 = vp.getMinY();
				v1 = vp.getMaxY();
				c0 = cur.getMinY();
				c1 = cur.getMaxY();
			}
			double vr0 = Math.max(Math.min((v0 - i0) / (i1 - i0), 1.0), 0);
			double vr1 = Math.max(Math.min((v1 - i0) / (i1 - i0), 1.0), 0);
			double cr0 = Math.max(Math.min((c0 - v0) / (v1 - v0), 1.0), 0);
			double cr1 = Math.max(Math.min((c1 - v0) / (v1 - v0), 1.0), 0);
			outline = new SlidingItemListOutline(new Range(vr0, vr1), new Range(cr0, cr1));
		}
		return outline;
	}

	public int getItemListLengthInPixels() {
		int length = 0;
		Rectangle ilb = getItemListBoundsInViewportCoords();
		if (ilb != null) {
			if (isHorizontalLayout()) {
				length = ilb.width;
			} else {
				length = ilb.height;
			}
		}
		return length;
	}

	public Rectangle getItemBoundsInComponent(SlidingItem item) {
		return getItemBoundsInComponent(getItemList().getIndexOf(item));
	}

	public Rectangle getItemBoundsInComponent(int index) {
		Rectangle bounds = getItemBoundsInViewportCoords(getItemInList(index));
		bounds.translate(getPadding().left, getPadding().top);
		return bounds;
	}

	protected Rectangle getItemListBoundsInViewportCoords() {
		Rectangle bounds = null;
		Graphics2D g = getGraphics2D();
		if (hasItems()) {
			validateLayout(g);
			Rectangle il1 = getItemBoundsInViewportCoords(getItemList().getFirstItem(), g);
			Rectangle il2 = getItemBoundsInViewportCoords(getItemList().getLastItem(), g);
			bounds = il1.union(il2);
		}
		return bounds;
	}

	protected Rectangle getItemBoundsInViewportCoords(SlidingItemInList item) {
		return getItemBoundsInViewportCoords(item, getGraphics2D());
	}

	protected Rectangle getItemBoundsInViewportCoords(SlidingItemInList item, Graphics2D g) {
		return getLayoutManager().getItemBoundsInViewportCoords(item, g);
	}

	protected Rectangle getItemBounds(SlidingItemInList item) {
		return getItemBounds(item, getGraphics2D());
	}

	protected Rectangle getItemBounds(SlidingItemInList item, Graphics2D g) {
		return getLayoutManager().getItemBounds(item, g).getBounds();
	}

	public Rectangle getCursorInnerBoundsInComponent() {
		Rectangle bounds = getLayoutManager().getCursorInnerBoundsInViewportCoords(getState(), getGraphics2D());
		bounds.translate(getPadding().left, getPadding().top);
		return bounds;
	}

	public Rectangle getCursorOuterBoundsInComponent() {
		Rectangle bounds = getCursorInnerBoundsInComponent();
		if (bounds != null) {
			Insets margin = getState().getCursorMargin();
			if (margin != null) {
				bounds.setBounds(bounds.x - margin.left, bounds.y - margin.top,
						bounds.width + margin.left + margin.right, bounds.height + margin.top + margin.bottom);
			}
		}
		return bounds;
	}

	public Orientation getOrientation() {
		return isHorizontalLayout() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
	}

	public boolean isHorizontalLayout() {
		return getLayoutManager().isHorizontalLayout();
	}

	public boolean isVerticalLayout() {
		return !isHorizontalLayout();
	}

	@Override
	public boolean isAnimating() {
		return isSliding();
	}

	public boolean isSliding() {
		return !isStationary();
	}

	public boolean isStationary() {
		return getState().equals(getTargetState());
	}

	private boolean isSteadyLanding() {
		long t = getLandingTimeMillis();
		if (t < Long.MAX_VALUE) {
			long now = System.currentTimeMillis();
			if (now >= t + getSteadyLandingMaximumTimeDelayMillis())
				return true;
			if (!isAnyKeyPressed() && now >= t + getSteadyLandingMinimumTimeDelayMillis())
				return true;
		}
		return false;
	}

	/**
	 * Returns the current speed of sliding
	 * 
	 * @return A relative sliding speed, between 0 (stationary) and 1 (maximum speed)
	 * @see #isStationary()
	 */
	public double getSlidingSpeed() {
		return getSlidingDynamics().getRelativeVelocity();
	}

	public boolean isItemListFitsInsideViewport() {
		if (hasItems()) {
			Rectangle ilb = getItemListBoundsInViewportCoords();
			return getViewportBounds().contains(ilb);
		} else {
			return true;
		}
	}

	public boolean isItemShowingInViewport(SlidingItem item) {
		return isItemShowingInViewport(getItemList().getIndexOf(item));
	}

	public boolean isItemShowingInViewport(int index) {
		if (index >= 0 && index < getItemCount()) {
			validateLayout();
			SlidingItemInList itemInList = getItemInList(index);
			return getItemBoundsInViewportCoords(itemInList).intersects(getViewportBounds());
		} else {
			return false;
		}
	}

	public boolean isSelectedItem(SlidingItem item) {
		SlidingItem selected = getSelectedItem();
		if (selected == null) {
			return false;
		} else {
			return selected.equals(item);
		}
	}

	public SlidingItem getSelectedItem() {
		int i = getSelectedItemIndex();
		if (i >= 0) {
			return getItemList().getItem(i).getItem();
		} else {
			return null;
		}
	}

	public int getSelectedItemIndex() {
		if (hasItems()) {
			if (selectedItemIndex < 0) {
				selectedItemIndex = getItemList().getIndexOf(getItemNearestToCursor());
			}
			return selectedItemIndex;
		} else {
			return -1;
		}
	}

	private void setSelectedItemIndex(int index) {
		this.selectedItemIndex = index;
	}

	private SlidingItemInList getItemNearestToCursor() {
		return getItemNearestToCursor(0);
	}

	private SlidingItemInList getItemNearestToCursor(int cursorOffset) {
		SlidingItemInList winner = null;
		if (hasItems()) {
			SlidingItemList itemList = getItemList();
			winner = itemList.getFirstItem();
			double itra = getState().getItemTranslation();
			double ccen = getLayoutManager().getCursorCenterPosition(getState()) + cursorOffset;
			double minDistance = Math.abs(winner.getPosition() + itra - ccen);
			for (int i = 1; i < itemList.getItemCount(); i++) {
				SlidingItemInList itemInList = itemList.getItem(i);
				double distance = Math.abs(itemInList.getPosition() + itra - ccen);
				if (distance < minDistance) {
					winner = itemInList;
					minDistance = distance;
				}
			}
		}
		return winner;
	}

	public boolean hasItems() {
		return !getItemList().isEmpty();
	}

	public int getItemCount() {
		return getItemList().getItemCount();
	}

	public SlidingItem getItem(int index) {
		if (index >= 0 && index < getItemCount()) {
			return getItemInList(index).getItem();
		} else {
			return null;
		}
	}

	protected SlidingItemInList getItemInList(int index) {
		return getItemList().getItem(index);
	}

	private Rectangle getViewportBounds() {
		return new Rectangle(0, 0, getViewportWidth(), getViewportHeight());
	}

	public int getViewportWidth() {
		return getWidth() - getPadding().left - getPadding().right;
	}

	public int getViewportHeight() {
		return getHeight() - getPadding().top - getPadding().bottom;
	}

	@Override
	public void setBackground(Color color) {
		getUI().setBackground(color);
		super.setBackground(color);
	}

	public Insets getPadding() {
		return padding;
	}

	private SlidingItemList getItemList() {
		return itemList;
	}

	public SlidingItemLayoutManager getLayoutManager() {
		return layoutManager;
	}

	public void setLayoutManager(SlidingItemLayoutManager layoutManager) {
		this.layoutManager = layoutManager;
		invalidateLayout();
	}

	public SlidingCursorMovement getCursorMovement() {
		return cursorMovement;
	}

	public void setCursorMovement(SlidingCursorMovement movement) {
		this.cursorMovement = movement;
		invalidateLayout();
	}

	public SlidingCursor getSlidingCursor() {
		return cursor;
	}

	public void setSlidingCursor(SlidingCursor cursor) {
		this.cursor = cursor;
		refreshUI();
	}

	public SlidingDynamics getSlidingDynamics() {
		return slidingDynamics;
	}

	public void setSlidingDynamics(SlidingDynamics slidingDynamics) {
		this.slidingDynamics = slidingDynamics;
	}

	public SlidingShade getShade() {
		return shade;
	}

	public void setShade(SlidingShade shade) {
		this.shade = shade;
		refreshUI();
	}

	public SlidingShadeDynamics getShadeDynamics() {
		return shadeDynamics;
	}

	public void setShadeDynamics(SlidingShadeDynamics shadeDynamics) {
		this.shadeDynamics = shadeDynamics;
		refreshUI();
	}

	public SlidingState getState() {
		return state;
	}

	private void setState(SlidingState state) {
		this.state = state;
	}

	private SlidingState getTargetState() {
		return targetState;
	}

	private void setTargetState(SlidingState targetState) {
		this.targetState = targetState;
	}

	public int getMinimumItemSelectionIndex() {
		SlidingItemRange range = getItemSelectionRange();
		if (range == null) {
			return 0;
		} else {
			return Math.max(Math.min(range.getInclusiveFromIndex(), getItemCount() - 1), 0);
		}
	}

	public int getMaximumItemSelectionIndex() {
		SlidingItemRange range = getItemSelectionRange();
		if (range == null) {
			return Math.max(getItemCount() - 1, 0);
		} else {
			return Math.max(Math.min(range.getInclusiveToIndex(), getItemCount() - 1), 0);
		}
	}

	public SlidingItemRange getItemSelectionRange() {
		return itemSelectionRange;
	}

	public void setItemSelectionRange(SlidingItemRange range) {
		this.itemSelectionRange = range;
		if (range != null && hasItems()) {
			int i = getSelectedItemIndex();
			int j = Math.max(Math.min(i, getMaximumItemSelectionIndex()), getMinimumItemSelectionIndex());
			if (j != i) {
				moveToItemIndex(j);
			}
		}
	}

	protected GenericListenerList<SlidingItemListListener> getListeners() {
		return listeners;
	}

	private int getLastLandedItemIndex() {
		return lastLandedItemIndex;
	}

	private void setLastLandedItemIndex(int index) {
		this.lastLandedItemIndex = index;
	}

	private long getLandingTimeMillis() {
		return landingTimeMillis;
	}

	private void setLandingTimeMillis(long timeMillis) {
		this.landingTimeMillis = timeMillis;
	}

	public boolean isLanded() {
		return landed;
	}

	private void setLanded(boolean landed) {
		this.landed = landed;
	}

	private boolean isLastSliding() {
		return lastSliding;
	}

	private void setLastSliding(boolean sliding) {
		this.lastSliding = sliding;
	}

	private boolean isValidatedLayout() {
		return validatedLayout;
	}

	private void setValidatedLayout(boolean validated) {
		this.validatedLayout = validated;
	}

	public double getMaxItemWidth(Graphics2D g) {
		if (maxItemWidth < 0) {
			maxItemWidth = getItemList().getMaxItemWidth(g);
		}
		return maxItemWidth;
	}

	private void setMaxItemWidth(double width) {
		this.maxItemWidth = width;
	}

	public double getMaxItemHeight(Graphics2D g) {
		if (maxItemHeight < 0) {
			maxItemHeight = getItemList().getMaxItemHeight(g);
		}
		return maxItemHeight;
	}

	private void setMaxItemHeight(double height) {
		this.maxItemHeight = height;
	}

	private Graphics2D getGraphics2D() {
		if (graphics2D == null) {
			graphics2D = SwingUtils.getDefaultGraphics();
		}
		return graphics2D;
	}

	private void setGraphics2D(Graphics2D g) {
		graphics2D = g;
	}

	private boolean isAnyKeyPressed() {
		return anyKeyPressed;
	}

	private void setAnyKeyPressed(boolean anyKeyPressed) {
		this.anyKeyPressed = anyKeyPressed;
	}

	public long getSteadyLandingMinimumTimeDelayMillis() {
		return steadyLandingMinimumTimeDelayMillis;
	}

	public void setSteadyLandingMinimumTimeDelayMillis(long millis) {
		this.steadyLandingMinimumTimeDelayMillis = millis;
	}

	public long getSteadyLandingMaximumTimeDelayMillis() {
		return steadyLandingMaximumTimeDelayMillis;
	}

	public void setSteadyLandingMaximumTimeDelayMillis(long millis) {
		this.steadyLandingMaximumTimeDelayMillis = millis;
	}

	@SuppressWarnings("serial")
	private class SlidingItemListPanel extends AnimatedPanel {

		private PaintState previousPaintState;

		private VolatileImage volatileBackBuffer;

		public SlidingItemListPanel(Dimension size, Color background) {
			super(size, background, false);
		}

		@Override
		protected PaintMetrics createPaintMetrics() {
			return new SlidingPaintMetrics();
		}

		@Override
		protected void initializePaint(Graphics2D g) {
			initializeGraphics(g);
			setGraphics2D(g);
			validateLayout(g);
		}

		private void initializeGraphics(Graphics2D g) {
			super.initializePaint(g);
		}

		@Override
		protected void updateStateBetweenPaints(Graphics2D g, long elapsedTimeMillis) {
			updateStateOverTime(g, (double) elapsedTimeMillis);
		}

		@Override
		protected void doPaintComponent(Graphics2D g) {
			PaintState paintState = createCurrentPaintState(g);
			SlidingPaintMetrics paintMetrics = getPaintMetrics();
			if (isOptimizedRenderingEnabled()) {
				VolatileImage backBuffer = getVolatileBackBuffer();
				int w = getWidth();
				int h = getHeight();
				if (backBuffer == null || backBuffer.getWidth() != w || backBuffer.getHeight() != h) {
					if (backBuffer != null) {
						backBuffer.flush();
					} else {
						// first use of back buffer
						setRepaintFully(true);
					}
					backBuffer = createVolatileImage(w, h);
				}
				boolean redo = false;
				do {
					int val = backBuffer.validate(getGraphicsConfiguration());
					if (val == VolatileImage.IMAGE_RESTORED) {
						// contents need to be restored
						setRepaintFully(true);
					} else if (val == VolatileImage.IMAGE_INCOMPATIBLE) {
						// old back buffer doesn't work with new GraphicsConfig; re-create it
						backBuffer = createVolatileImage(w, h);
						setRepaintFully(true);
					}
					Graphics2D gImg = backBuffer.createGraphics();
					initializeGraphics(gImg);
					createPaintStrategy(g, paintState).paint(gImg, paintMetrics);
					g.drawImage(backBuffer, 0, 0, null);
					gImg.dispose();
					redo = backBuffer.contentsLost();
					if (redo)
						setRepaintFully(true);
				} while (redo);
				setVolatileBackBuffer(backBuffer);
			} else {
				new FullPaintStrategy(getPreviousPaintState(), paintState).paint(g, paintMetrics);
			}
			setPreviousPaintState(paintState);
		}

		private PaintState createCurrentPaintState(Graphics2D g) {
			PaintState ps = new PaintState();
			SlidingItemListComponent comp = SlidingItemListComponent.this;
			// first item
			if (hasItems()) {
				ps.setFirstItemBounds(getItemBoundsInViewportCoords(getItemList().getFirstItem(), g));
			}
			// cursor
			if (getSlidingCursor() != null) {
				ps.setCursorMargin(getState().getCursorMargin());
				ps.setCursorInnerBounds(getLayoutManager().getCursorInnerBoundsInViewportCoords(getState(), g));
			}
			// shades
			SlidingShade shade = getShade();
			SlidingShadeDynamics shadeDyn = getShadeDynamics();
			if (shade != null) {
				int extLeading = 0, extTrailing = 0;
				if (shadeDyn != null) {
					SlidingItemList itemList = getItemList();
					extLeading = shadeDyn.getShadeExteriorLength(shade, SlidingShadeEnd.LEADING, comp, itemList, g);
					extTrailing = shadeDyn.getShadeExteriorLength(shade, SlidingShadeEnd.TRAILING, comp, itemList, g);
				}
				ps.setLeadingShadeBounds(
						getLayoutManager().getShadeBoundsInViewportCoords(shade, SlidingShadeEnd.LEADING, extLeading));
				ps.setTrailingShadeBounds(getLayoutManager().getShadeBoundsInViewportCoords(shade,
						SlidingShadeEnd.TRAILING, extTrailing));
			}
			return ps;
		}

		private PaintStrategy createPaintStrategy(Graphics2D g, PaintState paintState) {
			if (isPaintOptimized(g, paintState)) {
				return new OptimizedPaintStrategy(getPreviousPaintState(), paintState);
			} else {
				return new FullPaintStrategy(getPreviousPaintState(), paintState);
			}
		}

		private boolean isPaintOptimized(Graphics2D g, PaintState paintState) {
			return super.isPaintOptimized(g) && getPreviousPaintState() != null
					&& getPreviousPaintState().hasItems() == paintState.hasItems();
		}

		@Override
		protected SlidingPaintMetrics getPaintMetrics() {
			return (SlidingPaintMetrics) super.getPaintMetrics();
		}

		private PaintState getPreviousPaintState() {
			return previousPaintState;
		}

		private void setPreviousPaintState(PaintState ps) {
			this.previousPaintState = ps;
		}

		private VolatileImage getVolatileBackBuffer() {
			return volatileBackBuffer;
		}

		private void setVolatileBackBuffer(VolatileImage image) {
			this.volatileBackBuffer = image;
		}

	}

	private static class SlidingPaintMetrics extends PaintMetrics {

		private int itemsPainted;

		public SlidingPaintMetrics() {
		}

		public void incrementItemsPaintedCount() {
			setItemsPainted(getItemsPainted() + 1);
		}

		@Override
		public void reset() {
			super.reset();
			setItemsPainted(0);
		}

		@Override
		public String toString() {
			float ipp = getPaintCount() == 0 ? 0 : (getItemsPainted() * 10 / getPaintCount()) / 10f;
			return super.toString() + " | Average items per paint: " + ipp;
		}

		public int getItemsPainted() {
			return itemsPainted;
		}

		private void setItemsPainted(int itemsPainted) {
			this.itemsPainted = itemsPainted;
		}

	}

	private static class PaintState {

		private Rectangle firstItemBounds;

		private Rectangle cursorInnerBounds;

		private Insets cursorMargin;

		private Rectangle leadingShadeBounds;

		private Rectangle trailingShadeBounds;

		public PaintState() {
		}

		public boolean hasItems() {
			return getFirstItemBounds() != null;
		}

		public boolean hasCursor() {
			return getCursorInnerBounds() != null;
		}

		public boolean hasLeadingShade() {
			return getLeadingShadeBounds() != null;
		}

		public boolean hasTrailingShade() {
			return getTrailingShadeBounds() != null;
		}

		public Rectangle getFirstItemBounds() {
			return firstItemBounds;
		}

		public void setFirstItemBounds(Rectangle bounds) {
			this.firstItemBounds = bounds;
		}

		public Rectangle getCursorInnerBounds() {
			return cursorInnerBounds;
		}

		public void setCursorInnerBounds(Rectangle bounds) {
			this.cursorInnerBounds = bounds;
		}

		public Rectangle getCursorOuterBounds() {
			Rectangle bounds = getCursorInnerBounds();
			if (bounds != null) {
				Insets margin = getCursorMargin();
				if (margin != null) {
					return new Rectangle(bounds.x - margin.left, bounds.y - margin.top,
							bounds.width + margin.left + margin.right, bounds.height + margin.top + margin.bottom);
				}
			}
			return bounds;
		}

		public Insets getCursorMargin() {
			return cursorMargin;
		}

		public void setCursorMargin(Insets margin) {
			this.cursorMargin = margin;
		}

		public Rectangle getLeadingShadeBounds() {
			return leadingShadeBounds;
		}

		public void setLeadingShadeBounds(Rectangle bounds) {
			this.leadingShadeBounds = bounds;
		}

		public Rectangle getTrailingShadeBounds() {
			return trailingShadeBounds;
		}

		public void setTrailingShadeBounds(Rectangle bounds) {
			this.trailingShadeBounds = bounds;
		}

	}

	private abstract class PaintStrategy {

		private PaintState previousPaintState;

		private PaintState paintState;

		protected PaintStrategy(PaintState previousPaintState, PaintState paintState) {
			this.previousPaintState = previousPaintState;
			this.paintState = paintState;
		}

		public abstract void paint(Graphics2D g, SlidingPaintMetrics metrics);

		protected void paintBackground(Graphics2D g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		protected void paintItems(Graphics2D vpg, SlidingPaintMetrics metrics) {
			paintItems(vpg, createSingletonViewportClip(), metrics);
		}

		protected void paintItems(Graphics2D vpg, List<Rectangle> viewPortClips, SlidingPaintMetrics metrics) {
			for (Rectangle clip : viewPortClips) {
				int fromIndex = 0;
				int toIndex = getItemCount() - 1;
				if (getItemCount() >= 20) {
					int[] i = findItemRangeCoveringClip(clip, vpg);
					fromIndex = i[0];
					toIndex = i[1];
				}
				if (toIndex >= fromIndex) {
					Graphics2D gclip = (Graphics2D) vpg.create();
					gclip.clipRect(clip.x, clip.y, clip.width, clip.height);
					for (int i = fromIndex; i <= toIndex; i++) {
						SlidingItemInList itemInList = getItemInList(i);
						Rectangle bounds = getItemBoundsInViewportCoords(itemInList, vpg);
						if (bounds.intersects(clip)) {
							paintItem(vpg, itemInList, bounds, metrics);
						}
					}
					gclip.dispose();
				}
			}
		}

		protected void paintItem(Graphics2D vpg, SlidingItemInList itemInList, Rectangle bounds,
				SlidingPaintMetrics metrics) {
			Graphics2D g2 = (Graphics2D) vpg.create(bounds.x, bounds.y, bounds.width, bounds.height);
			itemInList.getItem().render(g2, getComponent());
			metrics.incrementItemsPaintedCount();
			g2.dispose();
		}

		protected void paintShades(Graphics2D vpg) {
			paintShades(vpg, createSingletonViewportClip());
		}

		protected void paintShades(Graphics2D vpg, List<Rectangle> viewPortClips) {
			if (getPaintState().hasLeadingShade()) {
				paintShade(vpg, viewPortClips, getPaintState().getLeadingShadeBounds(), true);
			}
			if (getPaintState().hasTrailingShade()) {
				paintShade(vpg, viewPortClips, getPaintState().getTrailingShadeBounds(), false);
			}
		}

		protected void paintShade(Graphics2D vpg, List<Rectangle> viewPortClips, Rectangle bounds, boolean leading) {
			for (Rectangle clip : viewPortClips) {
				if (bounds.intersects(clip)) {
					Graphics2D g2 = (Graphics2D) vpg.create(bounds.x, bounds.y, bounds.width, bounds.height);
					g2.clipRect(clip.x - bounds.x, clip.y - bounds.y, clip.width, clip.height);
					if (!leading) {
						g2.transform(getLayoutManager().getTrailingShadeTransform(getShade()));
					}
					g2.setComposite(AlphaComposite.SrcOver);
					getShade().renderAsLeading(g2, bounds.width, bounds.height, getComponent());
					g2.dispose();
				}
			}
		}

		protected void paintCursor(Graphics2D g, CursorLayer layer) {
			if (getPaintState().hasCursor()) {
				Insets margin = getPaintState().getCursorMargin();
				Rectangle bounds = getPaintState().getCursorInnerBounds();
				Graphics2D g2 = (Graphics2D) g.create();
				g2.translate(getPadding().left + bounds.x, getPadding().top + bounds.y);
				g2.clipRect(-margin.left, -margin.top, bounds.width + margin.left + margin.right,
						bounds.height + margin.top + margin.bottom);
				if (CursorLayer.UNDER_ITEMS.equals(layer)) {
					getSlidingCursor().renderUnderItems(g2, bounds, getComponent());
				} else if (CursorLayer.ABOVE_ITEMS.equals(layer)) {
					getSlidingCursor().renderAboveItems(g2, bounds, getComponent());
				}
				g2.dispose();
			}
		}

		private int[] findItemRangeCoveringClip(Rectangle clip, Graphics2D vpg) {
			return findItemRangeCoveringClip(clip, 0, getItemCount() - 1, 0, 2, vpg);
		}

		private int[] findItemRangeCoveringClip(Rectangle clip, int fromIndex, int toIndex, int recurseDepth,
				int maxRecurseDepth, Graphics2D vpg) {
			while (toIndex > fromIndex + 1) {
				int midIndex = (fromIndex + toIndex) / 2;
				SlidingItemInList itemInList = getItemList().getItem(midIndex);
				Rectangle bounds = getItemBoundsInViewportCoords(itemInList, vpg);
				if (bounds.intersects(clip)) {
					if (recurseDepth < maxRecurseDepth) {
						int nextDepth = recurseDepth + 1;
						int[] a = findItemRangeCoveringClip(clip, fromIndex, midIndex - 1, nextDepth, maxRecurseDepth,
								vpg);
						int[] b = findItemRangeCoveringClip(clip, midIndex + 1, toIndex, nextDepth, maxRecurseDepth,
								vpg);
						return new int[] { a[0], b[1] };
					} else {
						return new int[] { fromIndex, toIndex };
					}
				} else if (isOutsideOnLeadingSide(bounds, clip)) {
					fromIndex = midIndex + 1;
				} else if (isOutsideOnTrailingSide(bounds, clip)) {
					toIndex = midIndex - 1;
				} else {
					return new int[] { fromIndex, toIndex };
				}
			}
			return new int[] { fromIndex, toIndex };
		}

		private boolean isOutsideOnLeadingSide(Rectangle rect, Rectangle clip) {
			if (isHorizontalLayout()) {
				return rect.x + rect.width <= clip.x;
			} else {
				return rect.y + rect.height <= clip.y;
			}
		}

		private boolean isOutsideOnTrailingSide(Rectangle rect, Rectangle clip) {
			if (isHorizontalLayout()) {
				return rect.x >= clip.x + clip.width;
			} else {
				return rect.y >= clip.y + clip.height;
			}
		}

		protected List<Rectangle> createSingletonViewportClip() {
			return Collections.singletonList(getViewportBounds());
		}

		protected SlidingItemListComponent getComponent() {
			return SlidingItemListComponent.this;
		}

		public PaintState getPreviousPaintState() {
			return previousPaintState;
		}

		public PaintState getPaintState() {
			return paintState;
		}

	}

	private class FullPaintStrategy extends PaintStrategy {

		public FullPaintStrategy(PaintState previousPaintState, PaintState paintState) {
			super(previousPaintState, paintState);
		}

		@Override
		public void paint(Graphics2D g, SlidingPaintMetrics metrics) {
			paintBackground(g);
			if (hasItems()) {
				Graphics2D vpg = (Graphics2D) g.create(getPadding().left, getPadding().top, getViewportWidth(),
						getViewportHeight());
				paintCursor(g, CursorLayer.UNDER_ITEMS);
				paintItems(vpg, metrics);
				paintShades(vpg);
				paintCursor(g, CursorLayer.ABOVE_ITEMS);
				vpg.dispose();
			}
		}

	}

	/**
	 * Precondition : previous and current <code>PaintState</code> must have same (number of) items
	 */
	private class OptimizedPaintStrategy extends PaintStrategy {

		public OptimizedPaintStrategy(PaintState previousPaintState, PaintState paintState) {
			super(previousPaintState, paintState);
		}

		@Override
		public void paint(Graphics2D g, SlidingPaintMetrics metrics) {
			if (hasItems()) {
				Graphics2D vpg = (Graphics2D) g.create(getPadding().left, getPadding().top, getViewportWidth(),
						getViewportHeight());
				OptimizedPaintJob job = createPaintJob();
				shiftRegions(vpg, job);
				fillDirtyRegions(vpg, job);
				if (job.isDirtyCursor()) {
					paintPadding(g);
					paintCursor(g, CursorLayer.UNDER_ITEMS);
				}
				paintItems(vpg, job.getDirtyRegions(), metrics);
				paintShades(vpg, job.getDirtyRegions());
				if (job.isDirtyCursor()) {
					paintCursor(g, CursorLayer.ABOVE_ITEMS);
				}
				vpg.dispose();
			}
		}

		protected void shiftRegions(Graphics2D vpg, OptimizedPaintJob job) {
			int t = job.getShiftLength();
			vpg.setColor(getBackground());
			for (Rectangle r : job.getShiftRegions()) {
				if (isHorizontalLayout()) {
					vpg.copyArea(r.x - Math.min(t, 0), r.y, r.width - Math.abs(t), r.height, t, 0);
				} else {
					vpg.copyArea(r.x, r.y - Math.min(t, 0), r.width, r.height - Math.abs(t), 0, t);
				}
			}
		}

		protected void fillDirtyRegions(Graphics2D vpg, OptimizedPaintJob job) {
			vpg.setColor(getBackground());
			for (Rectangle r : job.getDirtyRegions()) {
				vpg.fillRect(r.x, r.y, r.width, r.height);
			}
		}

		protected void paintPadding(Graphics2D g) {
			Insets padding = getPadding();
			int w = getWidth();
			int h = getHeight();
			g.setColor(getBackground());
			g.fillRect(0, 0, w, padding.top);
			g.fillRect(0, h - padding.bottom, w, padding.bottom);
			g.fillRect(0, 0, padding.left, h);
			g.fillRect(w - padding.right, 0, padding.right, h);
		}

		protected OptimizedPaintJob createPaintJob() {
			OptimizedPaintJob job = new OptimizedPaintJob();
			Rectangle leading = expandAcrossViewport(getLeadingShadeDirtyRegion());
			Rectangle cursor = expandAcrossViewport(getCursorDirtyRegion());
			Rectangle trailing = expandAcrossViewport(getTrailingShadeDirtyRegion());
			int t = getShiftTranslation();
			boolean translated = t != 0;
			if (isModifiedLeadingShade() || translated) {
				job.addDirtyRegion(extendTrailingSide(leading, Math.max(t, 0)));
			}
			if ((isModifiedCursor() || translated) && !cursor.isEmpty()) {
				job.addDirtyRegion(extendLeadingSide(extendTrailingSide(cursor, Math.max(t, 0)), -Math.min(t, 0)));
				job.setDirtyCursor(true);
			}
			if (isModifiedTrailingShade() || translated) {
				job.addDirtyRegion(extendLeadingSide(trailing, -Math.min(t, 0)));
			}
			if (translated) {
				if (cursor.isEmpty()) {
					job.addShiftRegion(between(leading, trailing));
				} else {
					job.addShiftRegion(between(leading, cursor));
					job.addShiftRegion(between(cursor, trailing));
				}
			}
			job.setShiftLength(t);
			return job;
		}

		protected Rectangle getLeadingShadeDirtyRegion() {
			return clipToViewport(
					union(getPreviousPaintState().getLeadingShadeBounds(), getPaintState().getLeadingShadeBounds()));
		}

		protected Rectangle getTrailingShadeDirtyRegion() {
			Rectangle rect = clipToViewport(
					union(getPreviousPaintState().getTrailingShadeBounds(), getPaintState().getTrailingShadeBounds()));
			if (rect.isEmpty()) {
				if (isHorizontalLayout()) {
					rect.x = getViewportWidth();
				} else {
					rect.y = getViewportHeight();
				}
			}
			return rect;
		}

		protected Rectangle getCursorDirtyRegion() {
			return clipToViewport(
					union(getPreviousPaintState().getCursorOuterBounds(), getPaintState().getCursorOuterBounds()));
		}

		protected boolean isModifiedLeadingShade() {
			PaintState ps = getPaintState();
			PaintState pps = getPreviousPaintState();
			if (!ps.hasLeadingShade() && !pps.hasLeadingShade()) {
				return false;
			} else if (ps.hasLeadingShade() && !pps.hasLeadingShade()) {
				return true;
			} else if (!ps.hasLeadingShade() && pps.hasLeadingShade()) {
				return true;
			} else {
				return !ps.getLeadingShadeBounds().equals(pps.getLeadingShadeBounds());
			}
		}

		protected boolean isModifiedTrailingShade() {
			PaintState ps = getPaintState();
			PaintState pps = getPreviousPaintState();
			if (!ps.hasTrailingShade() && !pps.hasTrailingShade()) {
				return false;
			} else if (ps.hasTrailingShade() && !pps.hasTrailingShade()) {
				return true;
			} else if (!ps.hasTrailingShade() && pps.hasTrailingShade()) {
				return true;
			} else {
				return !ps.getTrailingShadeBounds().equals(pps.getTrailingShadeBounds());
			}
		}

		protected boolean isModifiedCursor() {
			PaintState ps = getPaintState();
			PaintState pps = getPreviousPaintState();
			if (!ps.hasCursor() && !pps.hasCursor()) {
				return false;
			} else if (ps.hasCursor() && !pps.hasCursor()) {
				return true;
			} else if (!ps.hasCursor() && pps.hasCursor()) {
				return true;
			} else {
				return !ps.getCursorOuterBounds().equals(pps.getCursorOuterBounds());
			}
		}

		protected int getShiftTranslation() {
			if (isHorizontalLayout()) {
				return getPaintState().getFirstItemBounds().x - getPreviousPaintState().getFirstItemBounds().x;
			} else {
				return getPaintState().getFirstItemBounds().y - getPreviousPaintState().getFirstItemBounds().y;
			}
		}

		protected Rectangle extendLeadingSide(Rectangle rect, int length) {
			if (length == 0) {
				return rect;
			} else {
				if (isHorizontalLayout()) {
					return new Rectangle(rect.x - length, rect.y, rect.width + length, rect.height);
				} else {
					return new Rectangle(rect.x, rect.y - length, rect.width, rect.height + length);
				}
			}
		}

		protected Rectangle extendTrailingSide(Rectangle rect, int length) {
			if (length == 0) {
				return rect;
			} else {
				if (isHorizontalLayout()) {
					return new Rectangle(rect.x, rect.y, rect.width + length, rect.height);
				} else {
					return new Rectangle(rect.x, rect.y, rect.width, rect.height + length);
				}
			}
		}

		protected Rectangle between(Rectangle rectLeading, Rectangle rectTrailing) {
			if (isHorizontalLayout()) {
				return new Rectangle(rectLeading.x + rectLeading.width, rectLeading.y,
						rectTrailing.x - rectLeading.x - rectLeading.width, rectLeading.height);
			} else {
				return new Rectangle(rectLeading.x, rectLeading.y + rectLeading.height, rectLeading.width,
						rectTrailing.y - rectLeading.y - rectLeading.height);
			}
		}

		protected Rectangle union(Rectangle r1, Rectangle r2) {
			if (r1 == null || r1.isEmpty()) {
				return r2;
			} else if (r2 == null || r2.isEmpty()) {
				return r1;
			} else {
				return r1.union(r2);
			}
		}

		protected Rectangle expandAcrossViewport(Rectangle rect) {
			if (isHorizontalLayout() && rect.height != getViewportHeight()) {
				return new Rectangle(rect.x, 0, rect.width, getViewportHeight());
			} else if (isVerticalLayout() && rect.width != getViewportWidth()) {
				return new Rectangle(0, rect.y, getViewportWidth(), rect.height);
			} else {
				return rect;
			}
		}

		protected Rectangle clipToViewport(Rectangle rect) {
			if (isFullyInsideViewport(rect)) {
				return rect;
			} else if (isInsideViewport(rect)) {
				int x0 = Math.max(rect.x, 0);
				int y0 = Math.max(rect.y, 0);
				int x1 = Math.min(rect.x + rect.width, getViewportWidth());
				int y1 = Math.min(rect.y + rect.height, getViewportHeight());
				return new Rectangle(x0, y0, x1 - x0, y1 - y0);
			} else {
				return new Rectangle();
			}
		}

		protected boolean isInsideViewport(Rectangle rect) {
			if (rect == null)
				return false;
			if (rect.x + rect.width <= 0 || rect.x >= getViewportWidth())
				return false;
			if (rect.y + rect.height <= 0 || rect.y >= getViewportHeight())
				return false;
			return true;
		}

		protected boolean isFullyInsideViewport(Rectangle rect) {
			if (rect == null)
				return false;
			return rect.x >= 0 && rect.x + rect.width <= getViewportWidth() && rect.y >= 0
					&& rect.y + rect.height <= getViewportHeight();
		}

	}

	private class OptimizedPaintJob {

		private Stack<Rectangle> dirtyRegions;

		private Stack<Rectangle> shiftRegions;

		private int shiftLength;

		private boolean dirtyCursor;

		public OptimizedPaintJob() {
			this.dirtyRegions = new Stack<Rectangle>();
			this.shiftRegions = new Stack<Rectangle>();
		}

		public void addDirtyRegion(Rectangle region) {
			if (!region.isEmpty()) {
				if (getDirtyRegions().isEmpty()) {
					getDirtyRegions().push(region);
				} else if (gapBetween(region, getDirtyRegions().peek())) {
					getDirtyRegions().push(region);
				} else {
					getDirtyRegions().push(getDirtyRegions().pop().union(region));
				}
			}
		}

		public void addShiftRegion(Rectangle region) {
			if (!region.isEmpty()) {
				if (getShiftRegions().isEmpty()) {
					getShiftRegions().push(region);
				} else if (gapBetween(region, getShiftRegions().peek())) {
					getShiftRegions().push(region);
				} else {
					getShiftRegions().push(getShiftRegions().pop().union(region));
				}
			}
		}

		private boolean gapBetween(Rectangle r1, Rectangle r2) {
			if (isHorizontalLayout()) {
				return r1.x + r1.width < r2.x || r2.x + r2.width < r1.x;
			} else {
				return r1.y + r1.height < r2.y || r2.y + r2.height < r1.y;
			}
		}

		public Stack<Rectangle> getDirtyRegions() {
			return dirtyRegions;
		}

		public Stack<Rectangle> getShiftRegions() {
			return shiftRegions;
		}

		public int getShiftLength() {
			return shiftLength;
		}

		public void setShiftLength(int shiftLength) {
			this.shiftLength = shiftLength;
		}

		public boolean isDirtyCursor() {
			return dirtyCursor;
		}

		public void setDirtyCursor(boolean dirtyCursor) {
			this.dirtyCursor = dirtyCursor;
		}

	}

	private static enum CursorLayer {

		UNDER_ITEMS,

		ABOVE_ITEMS;

	}

}