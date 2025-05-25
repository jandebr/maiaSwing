package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.cards.CardsInOrderPanel;
import org.maia.swing.cards.CardsInOrderPanel.Card;
import org.maia.swing.cards.CardsOfChoicePanel;

public class CardsInOrderDemo {

	public static void main(String[] args) {
		CardsInOrderPanel orderPanel = createCardsInOrderPanel();
		CardsOfChoicePanel choicePanel = createCardsOfChoicePanel(orderPanel);
		JFrame frame = new JFrame("Cards");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(choicePanel, BorderLayout.NORTH);
		frame.add(orderPanel, BorderLayout.SOUTH);
		frame.pack();
		orderPanel.setPanelMinimumWidth(choicePanel.getWidth());
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static CardsInOrderPanel createCardsInOrderPanel() {
		CardsInOrderPanel panel = new CardsInOrderPanel(400, 100);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		panel.setEmptyMessage("Click cards to add and reorder by dragging");
		panel.setEnabled(true);
		return panel;
	}

	private static CardsOfChoicePanel createCardsOfChoicePanel(CardsInOrderPanel orderPanel) {
		CardsOfChoicePanel panel = new CardsOfChoicePanel(createCards(), orderPanel);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		panel.setEnabled(true);
		return panel;
	}

	private static List<Card> createCards() {
		Icon icon = ImageUtils.getIcon("org/maia/swing/icons/text/textedit32.png");
		List<Card> cards = new Vector<Card>();
		cards.add(new Card("ape", icon, new Color(150, 201, 224)));
		cards.add(new Card("finding Nemo", icon, new Color(242, 196, 58)));
		cards.add(new Card("frog in the pond", icon, new Color(94, 189, 104)));
		cards.add(new Card("cat", icon, new Color(235, 160, 170)));
		return cards;
	}

}