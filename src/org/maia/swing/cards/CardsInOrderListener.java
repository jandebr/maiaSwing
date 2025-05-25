package org.maia.swing.cards;

import org.maia.swing.cards.CardsInOrderPanel.Card;

public interface CardsInOrderListener {

	void cardAddedToPanel(CardsInOrderPanel panel, Card card);

	void cardRemovedFromPanel(CardsInOrderPanel panel, Card card);

	void cardsRearrangedInPanel(CardsInOrderPanel panel);

	void cardsChangedInPanel(CardsInOrderPanel panel);

}