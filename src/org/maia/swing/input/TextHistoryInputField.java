package org.maia.swing.input;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class TextHistoryInputField extends JComboBox<String> {

	private int maximumHistoricalValues;

	public static int defaultMaximumHistoricalValues = 10;

	private List<TextHistoryInputFieldListener> inputFieldListeners;

	public TextHistoryInputField() {
		this(defaultMaximumHistoricalValues);
	}

	public TextHistoryInputField(int maximumHistoricalValues) {
		this.maximumHistoricalValues = maximumHistoricalValues;
		this.inputFieldListeners = new Vector<TextHistoryInputFieldListener>();
		setEditable(true);
		addActionListener(new InputActionListener());
		getEditor().getEditorComponent().addKeyListener(new EscapeKeyListener());
	}

	public void addListener(TextHistoryInputFieldListener listener) {
		getInputFieldListeners().add(listener);
	}

	public void removeListener(TextHistoryInputFieldListener listener) {
		getInputFieldListeners().remove(listener);
	}

	public void addTextValueToHistory(String value) {
		if (!value.isEmpty()) {
			boolean currentValue = getTextValue().equals(value);
			removeItem(value);
			insertItemAt(value, 0);
			truncateHistory();
			if (currentValue) {
				setSelectedItem(value);
			}
		}
	}

	public void selectTextForEditing() {
		getEditor().selectAll();
	}

	private void truncateHistory() {
		while (getItemCount() > getMaximumHistoricalValues()) {
			removeItemAt(getItemCount() - 1);
		}
	}

	private void fireValueChanged() {
		for (TextHistoryInputFieldListener listener : getInputFieldListeners()) {
			listener.textHistoryValueChanged(this);
		}
	}

	private void fireEscaped() {
		for (TextHistoryInputFieldListener listener : getInputFieldListeners()) {
			listener.textHistoryEscaped(this);
		}
	}

	public String getTextValue() {
		return getEditor().getItem().toString();
	}

	public void setTextValue(String value) {
		setSelectedItem(value);
	}

	public int getMaximumHistoricalValues() {
		return maximumHistoricalValues;
	}

	public void setMaximumHistoricalValues(int values) {
		this.maximumHistoricalValues = values;
		truncateHistory();
	}

	private List<TextHistoryInputFieldListener> getInputFieldListeners() {
		return inputFieldListeners;
	}

	private class InputActionListener implements ActionListener {

		public InputActionListener() {
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("comboBoxChanged")) {
				addTextValueToHistory(getTextValue());
				fireValueChanged();
			}
		}

	}

	private class EscapeKeyListener extends KeyAdapter {

		public EscapeKeyListener() {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				fireEscaped();
			}
		}

	}

}