package org.maia.swing.cards;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class CardsInOrderPanel extends JPanel {

	public static int CENTER_ALIGNMENT = 0;

	public static int LEFT_ALIGNMENT = 1;

	public static int RIGHT_ALIGNMENT = 2;

	private int cardsAlignment;

	private int cardsSpacing;

	private int cardsMinimumWidth;

	private int cardsMaximumWidth;

	private int panelMinimumWidth;

	private int panelHeight;

	private double cardsTransparencyWhileMoving;

	private String emptyMessage;

	private Color emptyMessageColor;

	private List<CardComponent> cardComponentsInOrder;

	private List<CardsInOrderListener> cardsListeners;

	public CardsInOrderPanel(int minimumWidth, int height) {
		this(minimumWidth, height, LEFT_ALIGNMENT);
	}

	public CardsInOrderPanel(int minimumWidth, int height, int cardsAlignment) {
		super(null, true);
		this.cardsAlignment = cardsAlignment;
		this.cardsSpacing = 10;
		this.cardsMinimumWidth = 100;
		this.cardsMaximumWidth = 400;
		this.panelMinimumWidth = minimumWidth;
		this.panelHeight = height;
		this.cardsTransparencyWhileMoving = 0.2;
		this.emptyMessageColor = Color.GRAY;
		this.cardComponentsInOrder = new Vector<CardComponent>();
		this.cardsListeners = new Vector<CardsInOrderListener>();
		sizing();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (CardComponent comp : getCardComponentsInOrder()) {
			comp.setEnabled(enabled);
		}
	}

	public void addListener(CardsInOrderListener listener) {
		getCardsListeners().add(listener);
	}

	public void removeListener(CardsInOrderListener listener) {
		getCardsListeners().remove(listener);
	}

	public synchronized boolean hasCard(Card card) {
		return findComponentFor(card) != null;
	}

	public synchronized void addCard(Card card) {
		if (getCardsAlignment() == RIGHT_ALIGNMENT) {
			addCardAtBegin(card);
		} else {
			addCardAtEnd(card);
		}
	}

	public synchronized void addCardAtBegin(Card card) {
		if (!hasCard(card)) {
			addCardComponent(new CardComponent(card), 0);
		}
	}

	public synchronized void addCardAtEnd(Card card) {
		if (!hasCard(card)) {
			addCardComponent(new CardComponent(card), getCardsCount());
		}
	}

	public synchronized void removeCard(Card card) {
		CardComponent comp = findComponentFor(card);
		if (comp != null) {
			removeCardComponent(comp);
		}
	}

	public synchronized void clear() {
		while (!isEmpty()) {
			removeCardComponent(getCardComponentsInOrder().get(getCardsCount() - 1));
		}
	}

	private void addCardComponent(CardComponent comp, int index) {
		getCardComponentsInOrder().add(index, comp);
		for (int i = 0; i < getCardsCount(); i++) {
			getCardComponentsInOrder().get(i).setIndex(i);
		}
		comp.setEnabled(isEnabled());
		add(comp); // invalidates
		validate();
		for (CardsInOrderListener listener : getCardsListeners()) {
			listener.cardAddedToPanel(this, comp.getCard());
		}
	}

	private void removeCardComponent(CardComponent comp) {
		getCardComponentsInOrder().remove(comp);
		for (int i = comp.getIndex(); i < getCardsCount(); i++) {
			getCardComponentsInOrder().get(i).setIndex(i);
		}
		remove(comp); // invalidates
		validate();
		for (CardsInOrderListener listener : getCardsListeners()) {
			listener.cardRemovedFromPanel(this, comp.getCard());
		}
	}

	private CardComponent findComponentFor(Card card) {
		for (CardComponent comp : getCardComponentsInOrder()) {
			if (comp.getCard().equals(card))
				return comp;
		}
		return null;
	}

	@Override
	public synchronized void validate() {
		super.validate();
		sizing();
		layoutCards();
		repaint();
	}

	private void sizing() {
		int width = Math.max(getCardsLineupWidth(), getPanelMinimumWidth());
		int height = getPanelHeight();
		Dimension size = new Dimension(width, height);
		setSize(size);
		setPreferredSize(size);
	}

	private int getCardsLineupWidth() {
		int width = (getCardsCount() - 1) * getCardsSpacing();
		for (CardComponent comp : getCardComponentsInOrder()) {
			width += comp.getWidth();
		}
		return width;
	}

	private void layoutCards() {
		Insets insets = getInsets();
		int x = 0;
		int y = insets.top;
		int align = getCardsAlignment();
		if (align == LEFT_ALIGNMENT) {
			x = insets.left;
		} else if (align == RIGHT_ALIGNMENT) {
			x = getWidth() - insets.right - getCardsLineupWidth();
		} else if (align == CENTER_ALIGNMENT) {
			x = (getWidth() - getCardsLineupWidth()) / 2;
		}
		for (CardComponent comp : getCardComponentsInOrder()) {
			comp.setLocation(x, y);
			x += comp.getWidth() + getCardsSpacing();
		}
		for (CardsInOrderListener listener : getCardsListeners()) {
			listener.cardsChangedInPanel(this);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (isEmpty()) {
			String msg = getEmptyMessage();
			if (msg != null) {
				FontMetrics fm = g.getFontMetrics();
				int x = (getWidth() - fm.stringWidth(msg)) / 2;
				int y = (getHeight() + fm.getAscent()) / 2;
				g.setColor(getEmptyMessageColor());
				g.drawString(msg, x, y);
			}
		}
	}

	private synchronized void rearrangeCardsWithMovingCard(CardComponent comp) {
		int x = comp.getCenterX();
		int i = comp.getIndex();
		boolean rearranged = false;
		if (i > 0 && x < getCardComponentsInOrder().get(i - 1).getCenterX()) {
			cardShiftingLeft(comp);
			rearranged = true;
		} else if (i < getCardsCount() - 1 && x > getCardComponentsInOrder().get(i + 1).getCenterX()) {
			cardShiftingRight(comp);
			rearranged = true;
		}
		if (rearranged) {
			for (CardsInOrderListener listener : getCardsListeners()) {
				listener.cardsRearrangedInPanel(this);
			}
		}
	}

	private void cardShiftingLeft(CardComponent comp) {
		List<CardComponent> cards = getCardComponentsInOrder();
		int x = comp.getCenterX();
		int i = comp.getIndex();
		int j = i - 1;
		while (j > 0 && x < cards.get(j - 1).getCenterX())
			j--;
		int x0 = cards.get(j).getX();
		cards.add(j, cards.remove(i));
		for (int k = j; k <= i; k++) {
			CardComponent cardK = cards.get(k);
			cardK.setIndex(k);
			if (cardK != comp) {
				cardK.setLocation(x0, cardK.getY());
			}
			x0 += cardK.getWidth() + getCardsSpacing();
		}
	}

	private void cardShiftingRight(CardComponent comp) {
		List<CardComponent> cards = getCardComponentsInOrder();
		int x = comp.getCenterX();
		int i = comp.getIndex();
		int j = i + 1;
		while (j < getCardsCount() - 1 && x > cards.get(j + 1).getCenterX())
			j++;
		int x1 = cards.get(j).getX() + cards.get(j).getWidth() - 1;
		cards.add(j, cards.remove(i));
		for (int k = j; k >= i; k--) {
			CardComponent cardK = cards.get(k);
			cardK.setIndex(k);
			if (cardK != comp) {
				cardK.setLocation(x1 - cardK.getWidth() + 1, cardK.getY());
			}
			x1 -= cardK.getWidth() + getCardsSpacing();
		}
	}

	private boolean isInRemovalArea(CardComponent comp) {
		int x = comp.getCenterX();
		if (x < 0 || x > getWidth())
			return false;
		int y = comp.getCenterY();
		return y < 0 || y > getHeight();
	}

	public synchronized boolean isEmpty() {
		return getCardComponentsInOrder().isEmpty();
	}

	public synchronized int getCardsCount() {
		return getCardComponentsInOrder().size();
	}

	public synchronized List<Card> getCardsInOrder() {
		List<Card> cards = new Vector<Card>(getCardsCount());
		for (CardComponent comp : getCardComponentsInOrder()) {
			cards.add(comp.getCard());
		}
		return cards;
	}

	private List<CardComponent> getCardComponentsInOrder() {
		return cardComponentsInOrder;
	}

	public int getCardsAlignment() {
		return cardsAlignment;
	}

	public void setCardsAlignment(int alignment) {
		this.cardsAlignment = alignment;
		revalidate();
	}

	public int getCardsSpacing() {
		return cardsSpacing;
	}

	public void setCardsSpacing(int spacing) {
		this.cardsSpacing = spacing;
		revalidate();
	}

	public int getCardsMinimumWidth() {
		return cardsMinimumWidth;
	}

	public void setCardsMinimumWidth(int width) {
		this.cardsMinimumWidth = width;
		revalidate();
	}

	public int getCardsMaximumWidth() {
		return cardsMaximumWidth;
	}

	public void setCardsMaximumWidth(int width) {
		this.cardsMaximumWidth = width;
		revalidate();
	}

	public int getPanelMinimumWidth() {
		return panelMinimumWidth;
	}

	public void setPanelMinimumWidth(int width) {
		this.panelMinimumWidth = width;
		revalidate();
	}

	public int getPanelHeight() {
		return panelHeight;
	}

	public void setPanelHeight(int panelHeight) {
		this.panelHeight = panelHeight;
		revalidate();
	}

	public double getCardsTransparencyWhileMoving() {
		return cardsTransparencyWhileMoving;
	}

	public void setCardsTransparencyWhileMoving(double transparency) {
		if (transparency < 0 || transparency > 1.0)
			throw new IllegalArgumentException("Transparency level must be between 0 and 1: " + transparency);
		this.cardsTransparencyWhileMoving = transparency;
	}

	public String getEmptyMessage() {
		return emptyMessage;
	}

	public void setEmptyMessage(String message) {
		this.emptyMessage = message;
		if (isEmpty())
			repaint();
	}

	public Color getEmptyMessageColor() {
		return emptyMessageColor;
	}

	public void setEmptyMessageColor(Color color) {
		this.emptyMessageColor = color;
		if (isEmpty())
			repaint();
	}

	private List<CardsInOrderListener> getCardsListeners() {
		return cardsListeners;
	}

	private class CardComponent extends JLabel implements MouseMotionListener, MouseListener {

		private Card card;

		private int index;

		private boolean dragged;

		private Point locationBeforeDragging;

		private Point mouseAnchorLocation;

		private float[] rgbaComps = new float[4];

		public CardComponent(Card card) {
			super(card.getLabel(), card.getIcon(), card.getHorizontalAlignment());
			setBackground(card.getBackgroundColor());
			setOpaque(true);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
					BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			sizing();
			addMouseListener(this);
			addMouseMotionListener(this);
			this.card = card;
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			setBackground(enabled ? getCard().getBackgroundColor() : null);
		}

		private void sizing() {
			Insets insets = CardsInOrderPanel.this.getInsets();
			int width = Math.min(Math.max(getPreferredSize().width, getCardsMinimumWidth()), getCardsMaximumWidth());
			int height = getPanelHeight() - insets.top - insets.bottom;
			Dimension size = new Dimension(width, height);
			setSize(size);
			setPreferredSize(size);
		}

		private void setBackgroundTransparency(double transparency) {
			getCard().getBackgroundColor().getRGBColorComponents(rgbaComps);
			rgbaComps[3] = (float) (1.0 - transparency);
			setBackground(new Color(rgbaComps[0], rgbaComps[1], rgbaComps[2], rgbaComps[3]));
		}

		@Override
		public void mouseClicked(MouseEvent event) {
			if (isEnabled() && event.getButton() != MouseEvent.BUTTON1) {
				synchronized (CardsInOrderPanel.this) {
					removeCardComponent(this);
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			if (isEnabled()) {
				if (!isDragged()) {
					setDragged(true);
					setLocationBeforeDragging(getLocation());
					setMouseAnchorLocation(event.getLocationOnScreen());
					setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					setBackgroundTransparency(getCardsTransparencyWhileMoving());
					CardsInOrderPanel.this.setComponentZOrder(this, 0);
				}
				Point p0 = getLocationBeforeDragging();
				Point m0 = getMouseAnchorLocation();
				int dx = event.getXOnScreen() - m0.x;
				int dy = event.getYOnScreen() - m0.y;
				setLocation(p0.x + dx, p0.y + dy);
				rearrangeCardsWithMovingCard(this);
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (isEnabled() && isDragged()) {
				setDragged(false);
				setBackgroundTransparency(0);
				setCursor(Cursor.getDefaultCursor());
				synchronized (CardsInOrderPanel.this) {
					if (isInRemovalArea(this)) {
						removeCardComponent(this);
					} else {
						layoutCards();
					}
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent event) {
		}

		@Override
		public void mouseEntered(MouseEvent event) {
		}

		@Override
		public void mouseExited(MouseEvent event) {
		}

		@Override
		public void mousePressed(MouseEvent event) {
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (isInRemovalArea(this)) {
				FontMetrics fm = g.getFontMetrics();
				int w = fm.charWidth('x');
				int x = (getWidth() - w) / 2;
				g.setColor(getBackground().darker());
				g.fillOval(x - 3, 3, w + 5, w + 5);
				g.fillOval(x - 3, getHeight() - w - 9, w + 5, w + 5);
				g.setColor(Color.WHITE);
				g.drawString("x", x, fm.getAscent());
				g.drawString("x", x, getHeight() - 6);
			}
		}

		public int getCenterX() {
			return getX() + getWidth() / 2;
		}

		public int getCenterY() {
			return getY() + getHeight() / 2;
		}

		public Card getCard() {
			return card;
		}

		private int getIndex() {
			return index;
		}

		private void setIndex(int index) {
			this.index = index;
		}

		private boolean isDragged() {
			return dragged;
		}

		private void setDragged(boolean dragged) {
			this.dragged = dragged;
		}

		private Point getLocationBeforeDragging() {
			return locationBeforeDragging;
		}

		private void setLocationBeforeDragging(Point location) {
			this.locationBeforeDragging = location;
		}

		private Point getMouseAnchorLocation() {
			return mouseAnchorLocation;
		}

		private void setMouseAnchorLocation(Point location) {
			this.mouseAnchorLocation = location;
		}

	}

	public static class Card {

		private String label;

		private Icon icon;

		private int horizontalAlignment;

		private Color backgroundColor;

		public Card(String label, Color backgroundColor) {
			this(label, null, backgroundColor);
		}

		public Card(String label, Icon icon, Color backgroundColor) {
			this(label, icon, SwingConstants.CENTER, backgroundColor);
		}

		public Card(String label, Icon icon, int horizontalAlignment, Color backgroundColor) {
			this.label = label;
			this.icon = icon;
			this.horizontalAlignment = horizontalAlignment;
			this.backgroundColor = backgroundColor == null ? Color.WHITE : backgroundColor;
		}

		@Override
		public String toString() {
			return getLabel();
		}

		@Override
		public int hashCode() {
			return getLabel().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Card other = (Card) obj;
			if (getLabel() == null) {
				if (other.getLabel() != null)
					return false;
			} else if (!getLabel().equals(other.getLabel()))
				return false;
			return true;
		}

		public String getLabel() {
			return label;
		}

		public Icon getIcon() {
			return icon;
		}

		public int getHorizontalAlignment() {
			return horizontalAlignment;
		}

		public Color getBackgroundColor() {
			return backgroundColor;
		}

	}

}