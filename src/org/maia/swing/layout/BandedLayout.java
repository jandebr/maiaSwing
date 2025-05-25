package org.maia.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.List;
import java.util.Vector;

public class BandedLayout {

	private HorizontalAlignment defaultHorizontalAlignment;

	private VerticalAlignment defaultVerticalAlignment;

	public BandedLayout() {
		this(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
	}

	public BandedLayout(HorizontalAlignment defaultHorizontalAlignment,
			VerticalAlignment defaultVerticalAlignment) {
		this.defaultHorizontalAlignment = defaultHorizontalAlignment;
		this.defaultVerticalAlignment = defaultVerticalAlignment;
	}

	public Band createHorizontalContainerBand(float weight) {
		return createHorizontalContainerBand(weight, getDefaultHorizontalAlignment());
	}

	public Band createHorizontalContainerBand(float weight, HorizontalAlignment horizontalAlignment) {
		return createHorizontalContainerBand(weight, null, horizontalAlignment);
	}

	public Band createHorizontalContainerBand(float weight, Container container) {
		return createHorizontalContainerBand(weight, container, getDefaultHorizontalAlignment());
	}

	public Band createHorizontalContainerBand(float weight, Container container,
			HorizontalAlignment horizontalAlignment) {
		return createHorizontalContainerBand(weight, container, horizontalAlignment, getDefaultVerticalAlignment());
	}

	public Band createHorizontalContainerBand(float weight, Container container,
			HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		return createContainerBand(weight, container, horizontalAlignment, verticalAlignment, Orientation.HORIZONTAL);
	}

	public Band createVerticalContainerBand(float weight) {
		return createVerticalContainerBand(weight, getDefaultVerticalAlignment());
	}

	public Band createVerticalContainerBand(float weight, VerticalAlignment verticalAlignment) {
		return createVerticalContainerBand(weight, null, verticalAlignment);
	}

	public Band createVerticalContainerBand(float weight, Container container) {
		return createVerticalContainerBand(weight, container, getDefaultVerticalAlignment());
	}

	public Band createVerticalContainerBand(float weight, Container container, VerticalAlignment verticalAlignment) {
		return createVerticalContainerBand(weight, container, getDefaultHorizontalAlignment(), verticalAlignment);
	}

	public Band createVerticalContainerBand(float weight, Container container, HorizontalAlignment horizontalAlignment,
			VerticalAlignment verticalAlignment) {
		return createContainerBand(weight, container, horizontalAlignment, verticalAlignment, Orientation.VERTICAL);
	}

	private Band createContainerBand(float weight, Container container, HorizontalAlignment horizontalAlignment,
			VerticalAlignment verticalAlignment, Orientation subBandsOrientation) {
		Band band = new Band(weight, horizontalAlignment, verticalAlignment, subBandsOrientation);
		band.setComponent(container);
		return band;
	}

	public Band createComponentBand(float weight, Component component) {
		return createComponentBand(weight, component, getDefaultHorizontalAlignment(), getDefaultVerticalAlignment());
	}

	public Band createComponentBand(float weight, Component component, HorizontalAlignment horizontalAlignment) {
		return createComponentBand(weight, component, horizontalAlignment, getDefaultVerticalAlignment());
	}

	public Band createComponentBand(float weight, Component component, VerticalAlignment verticalAlignment) {
		return createComponentBand(weight, component, getDefaultHorizontalAlignment(), verticalAlignment);
	}

	public Band createComponentBand(float weight, Component component, HorizontalAlignment horizontalAlignment,
			VerticalAlignment verticalAlignment) {
		Band band = new Band(weight, horizontalAlignment, verticalAlignment, Orientation.HORIZONTAL);
		band.setComponent(component);
		return band;
	}

	public Band createFlexibleSpacerBand(float weight) {
		return createSpacerBand(weight, 0, Integer.MAX_VALUE);
	}

	public Band createConstantSpacerBand(float weight, int size) {
		return createSpacerBand(weight, size, size);
	}

	public Band createMinimumSpacerBand(float weight, int minimumSize) {
		return createSpacerBand(weight, minimumSize, Integer.MAX_VALUE);
	}

	public Band createMaximumSpacerBand(float weight, int maximumSize) {
		return createSpacerBand(weight, 0, maximumSize);
	}

	public Band createSpacerBand(float weight, int minimumSize, int maximumSize) {
		Band band = new Band(weight, getDefaultHorizontalAlignment(), getDefaultVerticalAlignment(),
				Orientation.HORIZONTAL); // orientation does not matter since it should be a leaf band
		band.setSizeRange(minimumSize, maximumSize);
		return band;
	}

	public void layout(Band band, Dimension size) {
		layout(band, new Rectangle(size));
	}

	public void layout(Band band, Rectangle bounds) {
		Insets margin = band.getMargin();
		Rectangle insideBounds = new Rectangle(bounds.x + margin.left, bounds.y + margin.top,
				bounds.width - margin.left - margin.right, bounds.height - margin.top - margin.bottom);
		band.setBounds(insideBounds);
		if (!band.isLeaf()) {
			layoutSubBands(band);
		}
	}

	private void layoutSubBands(Band band) {
		int[] subBandSizes = deriveSubBandSizes(band);
		Dimension innerSize = band.getBounds().getSize();
		if (band.isHorizontal()) {
			innerSize.width = sum(subBandSizes);
		} else {
			innerSize.height = sum(subBandSizes);
		}
		Rectangle innerArea = positionInBounds(innerSize, band.getBounds(), band.getHorizontalAlignment(),
				band.getVerticalAlignment());
		int x = innerArea.x;
		int y = innerArea.y;
		for (int i = 0; i < band.getSubBandCount(); i++) {
			int subBandWidth = band.isHorizontal() ? subBandSizes[i] : innerArea.width;
			int subBandHeight = band.isHorizontal() ? innerArea.height : subBandSizes[i];
			Rectangle subBounds = new Rectangle(x, y, subBandWidth, subBandHeight);
			layout(band.getSubBand(i), subBounds);
			if (band.isHorizontal()) {
				x += subBandWidth;
			} else {
				y += subBandHeight;
			}
		}
	}

	private int[] deriveSubBandSizes(Band band) {
		List<Band> subBands = band.getSubBands();
		int n = subBands.size();
		int[] subSizes = new int[n];
		boolean[] settled = new boolean[n];
		boolean[] reachedMin = new boolean[n];
		boolean[] reachedMax = new boolean[n];
		Rectangle bounds = band.getBounds();
		int availableSize = band.isHorizontal() ? bounds.width : bounds.height;
		do {
			float weightSum = sumOfUnsettledWeights(subBands, settled);
			int deltaSize = availableSize;
			int cumulSize = 0;
			int lasti = lastUnsettledIndex(settled);
			for (int i = 0; i < n; i++) {
				if (!settled[i]) {
					// weighted size
					Band subBand = subBands.get(i);
					int size = subSizes[i];
					if (i == lasti) {
						size += availableSize - cumulSize;
					} else {
						float ratio = weightSum > 0f ? subBand.getWeight() / weightSum : 1f;
						int allocatedSize = Math.round(ratio * availableSize);
						size += allocatedSize;
						cumulSize += allocatedSize;
					}
					// size constraints
					int minSize = subBand.getMinimumSize();
					int maxSize = subBand.getMaximumSize();
					float minAr = subBand.getMinimumAspectRatio();
					float maxAr = subBand.getMaximumAspectRatio();
					if (band.isHorizontal()) {
						minSize = Math.max(minSize, Math.round(bounds.height * minAr));
						if (maxAr < Float.MAX_VALUE) {
							maxSize = Math.min(maxSize, Math.round(bounds.height * maxAr));
						}
					} else {
						if (minAr > 0f) {
							maxSize = Math.min(maxSize, Math.round(bounds.width / minAr));
						}
						if (maxAr > 0f && maxAr < Float.MAX_VALUE) {
							minSize = Math.max(minSize, Math.round(bounds.width / maxAr));
						}
					}
					size = Math.max(Math.min(size, maxSize), minSize);
					reachedMin[i] = size == minSize;
					reachedMax[i] = size == maxSize;
					// consolidate
					deltaSize -= size - subSizes[i];
					subSizes[i] = size;
				}
			}
			if (deltaSize != 0) {
				for (int i = 0; i < n; i++) {
					if (deltaSize > 0) {
						settled[i] = settled[i] || reachedMax[i];
					} else {
						settled[i] = settled[i] || reachedMin[i];
					}
				}
			}
			availableSize = deltaSize;
		} while (availableSize != 0 && !allSettled(settled));
		return subSizes;
	}

	private float sumOfUnsettledWeights(List<Band> bands, boolean[] settled) {
		float sum = 0f;
		for (int i = 0; i < bands.size(); i++) {
			if (!settled[i]) {
				sum += bands.get(i).getWeight();
			}
		}
		return sum;
	}

	private int lastUnsettledIndex(boolean[] settled) {
		for (int i = settled.length - 1; i >= 0; i--) {
			if (!settled[i])
				return i;
		}
		return -1;
	}

	private boolean allSettled(boolean[] settled) {
		for (int i = 0; i < settled.length; i++) {
			if (!settled[i])
				return false;
		}
		return true;
	}

	private int sum(int[] values) {
		int sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		return sum;
	}

	private static Rectangle positionInBounds(Dimension box, Rectangle bounds, HorizontalAlignment hAlign,
			VerticalAlignment vAlign) {
		int dx = bounds.width - box.width;
		int dy = bounds.height - box.height;
		int x = bounds.x;
		if (HorizontalAlignment.CENTER.equals(hAlign)) {
			x += dx / 2;
		} else if (HorizontalAlignment.RIGHT.equals(hAlign)) {
			x += dx;
		}
		int y = bounds.y;
		if (VerticalAlignment.CENTER.equals(vAlign)) {
			y += dy / 2;
		} else if (VerticalAlignment.BOTTOM.equals(vAlign)) {
			y += dy;
		}
		return new Rectangle(x, y, box.width, box.height);
	}

	public HorizontalAlignment getDefaultHorizontalAlignment() {
		return defaultHorizontalAlignment;
	}

	public void setDefaultHorizontalAlignment(HorizontalAlignment hAlignment) {
		this.defaultHorizontalAlignment = hAlignment;
	}

	public VerticalAlignment getDefaultVerticalAlignment() {
		return defaultVerticalAlignment;
	}

	public void setDefaultVerticalAlignment(VerticalAlignment vAlignment) {
		this.defaultVerticalAlignment = vAlignment;
	}

	public static class Band {

		private float weight;

		private int minimumSize = 0;

		private int maximumSize = Integer.MAX_VALUE;

		private float minimumAspectRatio = 0f;

		private float maximumAspectRatio = Float.MAX_VALUE;

		private Insets margin = new Insets(0, 0, 0, 0);

		private Rectangle bounds; // as set by the layout manager

		private Component component;

		private HorizontalAlignment horizontalAlignment;

		private VerticalAlignment verticalAlignment;

		private Orientation subBandsOrientation;

		private List<Band> subBands;

		private Band parentBand;

		private Band(float weight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment,
				Orientation subBandsOrientation) {
			this.weight = weight;
			this.horizontalAlignment = horizontalAlignment;
			this.verticalAlignment = verticalAlignment;
			this.subBandsOrientation = subBandsOrientation;
			this.subBands = new Vector<Band>();
		}

		public Band addSubBand(Band band) {
			getSubBands().add(band);
			band.setParentBand(this);
			Component comp = band.getComponent();
			if (comp != null) {
				int minCompSize = isHorizontal() ? comp.getMinimumSize().width : comp.getMinimumSize().height;
				int maxCompSize = isHorizontal() ? comp.getMaximumSize().width : comp.getMaximumSize().height;
				band.setMinimumSize(Math.max(band.getMinimumSize(), minCompSize));
				band.setMaximumSize(Math.min(band.getMaximumSize(), maxCompSize));
			}
			return band;
		}

		public void setSizeRange(int minimumSize, int maximumSize) {
			setMinimumSize(minimumSize);
			setMaximumSize(maximumSize);
		}

		public void setAspectRatioRange(float minimumAspectRatio, float maximumAspectRatio) {
			setMinimumAspectRatio(minimumAspectRatio);
			setMaximumAspectRatio(maximumAspectRatio);
		}

		public void setAspectRatioSquare() {
			setAspectRatioRange(1f, 1f);
		}

		public boolean isHorizontal() {
			return Orientation.HORIZONTAL.equals(getSubBandsOrientation());
		}

		public boolean isVertical() {
			return !isHorizontal();
		}

		public boolean isRoot() {
			return getParentBand() == null;
		}

		public boolean isLeaf() {
			return getSubBandCount() == 0;
		}

		public int getSubBandCount() {
			return getSubBands().size();
		}

		public Band getSubBand(int index) {
			return getSubBands().get(index);
		}

		public float getWeight() {
			return weight;
		}

		public void setWeight(float weight) {
			this.weight = weight;
		}

		public int getMinimumSize() {
			return minimumSize;
		}

		public void setMinimumSize(int minimumSize) {
			this.minimumSize = minimumSize;
		}

		public int getMaximumSize() {
			return maximumSize;
		}

		public void setMaximumSize(int maximumSize) {
			this.maximumSize = maximumSize;
		}

		public float getMinimumAspectRatio() {
			return minimumAspectRatio;
		}

		public void setMinimumAspectRatio(float minimumAspectRatio) {
			this.minimumAspectRatio = minimumAspectRatio;
		}

		public float getMaximumAspectRatio() {
			return maximumAspectRatio;
		}

		public void setMaximumAspectRatio(float maximumAspectRatio) {
			this.maximumAspectRatio = maximumAspectRatio;
		}

		public Insets getMargin() {
			return margin;
		}

		public void setMargin(Insets margin) {
			this.margin = margin;
		}

		public Rectangle getBounds() {
			return bounds;
		}

		void setBounds(Rectangle bounds) {
			this.bounds = bounds;
			Component comp = getComponent();
			if (comp != null) {
				int w = Math.min(Math.max(comp.getMinimumSize().width, bounds.width), comp.getMaximumSize().width);
				int h = Math.min(Math.max(comp.getMinimumSize().height, bounds.height), comp.getMaximumSize().height);
				Dimension compSize = new Dimension(w, h);
				comp.setBounds(positionInBounds(compSize, bounds, getHorizontalAlignment(), getVerticalAlignment()));
			}
		}

		public Component getComponent() {
			return component;
		}

		public void setComponent(Component component) {
			this.component = component;
		}

		public HorizontalAlignment getHorizontalAlignment() {
			return horizontalAlignment;
		}

		public void setHorizontalAlignment(HorizontalAlignment hAlignment) {
			this.horizontalAlignment = hAlignment;
		}

		public VerticalAlignment getVerticalAlignment() {
			return verticalAlignment;
		}

		public void setVerticalAlignment(VerticalAlignment vAlignment) {
			this.verticalAlignment = vAlignment;
		}

		public Orientation getSubBandsOrientation() {
			return subBandsOrientation;
		}

		List<Band> getSubBands() {
			return subBands;
		}

		public Band getParentBand() {
			return parentBand;
		}

		private void setParentBand(Band parent) {
			this.parentBand = parent;
		}

	}

}