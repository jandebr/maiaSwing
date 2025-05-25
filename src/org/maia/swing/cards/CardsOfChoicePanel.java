package org.maia.swing.cards;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.maia.swing.cards.CardsInOrderPanel.Card;

@SuppressWarnings("serial")
public class CardsOfChoicePanel extends JPanel implements CardsInOrderListener {

	private CardsInOrderPanel orderPanel;

	private List<Card> cardsOfChoice;

	private Map<Card, CardOfChoiceAction> cardsActionMap;

	public CardsOfChoicePanel(List<Card> cardsOfChoice, CardsInOrderPanel orderPanel) {
		this(cardsOfChoice, orderPanel, 1, 0);
	}

	public CardsOfChoicePanel(List<Card> cardsOfChoice, CardsInOrderPanel orderPanel, int rows, int cols) {
		super(new GridLayout(rows, cols));
		this.orderPanel = orderPanel;
		this.cardsOfChoice = cardsOfChoice;
		this.cardsActionMap = new HashMap<Card, CardOfChoiceAction>();
		buildUI();
		orderPanel.addListener(this);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Map<Card, CardOfChoiceAction> map = getCardsActionMap();
		if (enabled) {
			List<Card> cardsSelected = getOrderPanel().getCardsInOrder();
			for (Card card : getCardsOfChoice()) {
				map.get(card).setEnabled(!cardsSelected.contains(card));
			}
		} else {
			for (Card card : getCardsOfChoice()) {
				map.get(card).setEnabled(false);
			}
		}
	}

	private void buildUI() {
		for (Card card : getCardsOfChoice()) {
			CardOfChoiceAction action = new CardOfChoiceAction(card);
			JButton button = new JButton(action);
			add(button);
			getCardsActionMap().put(card, action);
		}
	}

	@Override
	public void cardAddedToPanel(CardsInOrderPanel panel, Card card) {
		CardOfChoiceAction action = getCardsActionMap().get(card);
		if (action != null) {
			action.setEnabled(false);
		}
	}

	@Override
	public void cardRemovedFromPanel(CardsInOrderPanel panel, Card card) {
		CardOfChoiceAction action = getCardsActionMap().get(card);
		if (action != null) {
			action.setEnabled(this.isEnabled());
		}
	}

	@Override
	public void cardsRearrangedInPanel(CardsInOrderPanel panel) {
	}

	@Override
	public void cardsChangedInPanel(CardsInOrderPanel panel) {
		// System.out.println(panel.getCardsInOrder());
	}

	public List<Card> getCardsOfChoice() {
		return cardsOfChoice;
	}

	private Map<Card, CardOfChoiceAction> getCardsActionMap() {
		return cardsActionMap;
	}

	private CardsInOrderPanel getOrderPanel() {
		return orderPanel;
	}

	private class CardOfChoiceAction extends AbstractAction {

		private Card card;

		public CardOfChoiceAction(Card card) {
			super(card.getLabel(), card.getIcon());
			this.card = card;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getOrderPanel().addCard(getCard());
		}

		public Card getCard() {
			return card;
		}

	}

}