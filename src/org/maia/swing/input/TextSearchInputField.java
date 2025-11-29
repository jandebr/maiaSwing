package org.maia.swing.input;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.button.IconActionButton;
import org.maia.util.AsyncSerialTaskWorker;
import org.maia.util.AsyncSerialTaskWorker.AsyncTask;

@SuppressWarnings("serial")
public abstract class TextSearchInputField extends JPanel {

	private JComponent searchField;

	private IconActionButton searchButton;

	private TextSearchInputCommand searchCommand;

	private List<TextSearchInputFieldListener> inputFieldListeners;

	private String previousSearchString;

	private static SearchCommandTaskWorker searchCommandTaskWorker;

	public static int MINIMUM_WIDTH = 240;

	protected TextSearchInputField() {
		super(new BorderLayout(4, 0));
		this.searchField = createSearchField();
		this.searchButton = createSearchButton();
		this.inputFieldListeners = new Vector<TextSearchInputFieldListener>();
		addListener(new SearchCommandController());
		buildUI();
	}

	public static TextSearchInputField createField() {
		return new SimpleTextSearchInputField();
	}

	public static TextSearchInputField createFieldWithHistory() {
		return createFieldWithHistory(TextHistoryInputField.defaultMaximumHistoricalValues);
	}

	public static TextSearchInputField createFieldWithHistory(int maximumHistoricalValues) {
		HistoryTextSearchInputField.maximumHistoricalValues = maximumHistoricalValues;
		return new HistoryTextSearchInputField();
	}

	public void addListener(TextSearchInputFieldListener listener) {
		getInputFieldListeners().add(listener);
	}

	public void removeListener(TextSearchInputFieldListener listener) {
		getInputFieldListeners().remove(listener);
	}

	protected abstract JComponent createSearchField();

	protected IconActionButton createSearchButton() {
		IconActionButton button = new IconActionButton(getSearchIcon(), new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				submitSearch();
				getSearchField().grabFocus();
			}
		});
		button.setToolTipText("Search");
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return button;
	}

	private void buildUI() {
		add(getSearchField(), BorderLayout.CENTER);
		add(getSearchButton(), BorderLayout.EAST);
		if (getMinimumSize().width < MINIMUM_WIDTH) {
			setMinimumSize(new Dimension(MINIMUM_WIDTH, getMinimumSize().height));
			setPreferredSize(getMinimumSize());
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		getSearchField().setEnabled(enabled);
		getSearchButton().setEnabled(enabled);
	}

	protected void submitSearch() {
		String previous = getPreviousSearchString();
		String current = getSearchString();
		if (previous == null || !previous.equals(current)) {
			setPreviousSearchString(current);
			fireSearchStringChanged();
		}
	}

	protected void escapeSearch() {
		fireEscaped();
	}

	private void fireSearchStringChanged() {
		for (TextSearchInputFieldListener listener : getInputFieldListeners()) {
			listener.textSearchStringChanged(this);
		}
	}

	private void fireEscaped() {
		for (TextSearchInputFieldListener listener : getInputFieldListeners()) {
			listener.textSearchEscaped(this);
		}
	}

	public abstract void selectSearchStringForEditing();

	public abstract String getSearchString();

	public final void setSearchString(String value) {
		setPreviousSearchString(null); // force events
		replaceSearchString(value);
		submitSearch();
	}

	protected abstract void replaceSearchString(String value);

	protected Icon getSearchIcon() {
		return ImageUtils.getIcon("org/maia/swing/icons/text/search16.png");
	}

	protected JComponent getSearchField() {
		return searchField;
	}

	protected IconActionButton getSearchButton() {
		return searchButton;
	}

	public TextSearchInputCommand getSearchCommand() {
		return searchCommand;
	}

	public void setSearchCommand(TextSearchInputCommand searchCommand) {
		this.searchCommand = searchCommand;
	}

	private List<TextSearchInputFieldListener> getInputFieldListeners() {
		return inputFieldListeners;
	}

	private String getPreviousSearchString() {
		return previousSearchString;
	}

	private void setPreviousSearchString(String value) {
		this.previousSearchString = value;
	}

	private static synchronized SearchCommandTaskWorker getSearchCommandTaskWorker() {
		if (searchCommandTaskWorker == null) {
			searchCommandTaskWorker = new SearchCommandTaskWorker();
			searchCommandTaskWorker.start();
		}
		return searchCommandTaskWorker;
	}

	private class SearchCommandController implements TextSearchInputFieldListener {

		public SearchCommandController() {
		}

		@Override
		public void textSearchStringChanged(TextSearchInputField inputField) {
			TextSearchInputCommand command = getSearchCommand();
			if (command != null) {
				SearchCommandTask task = new SearchCommandTask(inputField, command);
				getSearchCommandTaskWorker().addTask(task);
			}
		}

		@Override
		public void textSearchEscaped(TextSearchInputField inputField) {
			// no action
		}

	}

	private static class SearchCommandTask implements AsyncTask {

		private TextSearchInputField searchInputField;

		private TextSearchInputCommand searchCommand;

		public SearchCommandTask(TextSearchInputField searchInputField, TextSearchInputCommand searchCommand) {
			this.searchInputField = searchInputField;
			this.searchCommand = searchCommand;
		}

		@Override
		public void process() {
			getSearchCommand().execute(getSearchInputField());
		}

		private TextSearchInputField getSearchInputField() {
			return searchInputField;
		}

		private TextSearchInputCommand getSearchCommand() {
			return searchCommand;
		}

	}

	/**
	 * Keeps a backlog of at most 1 search task (most recently added) + at most 1 search task in progress
	 */
	private static class SearchCommandTaskWorker extends AsyncSerialTaskWorker<SearchCommandTask> {

		public SearchCommandTaskWorker() {
			super("Search command task worker");
		}

		@Override
		protected void addTaskToQueue(SearchCommandTask task, Queue<SearchCommandTask> queue) {
			SearchCommandTask currentTask = queue.peek();
			queue.clear(); // discard any backlog
			if (currentTask != null) {
				queue.add(currentTask); // likely this task is in progress, keep it
			}
			queue.add(task);
		}

	}

	private static class SimpleTextSearchInputField extends TextSearchInputField {

		public SimpleTextSearchInputField() {
		}

		@Override
		protected JComponent createSearchField() {
			JTextField field = new JTextField();
			field.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					submitSearch();
				}
			});
			field.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent e) {
					submitSearch();
				}

			});
			field.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						escapeSearch();
					}
				}
			});
			return field;
		}

		@Override
		public void selectSearchStringForEditing() {
			getSearchField().grabFocus();
			getSearchField().selectAll();
		}

		@Override
		public String getSearchString() {
			return getSearchField().getText();
		}

		@Override
		protected void replaceSearchString(String value) {
			getSearchField().setText(value);
		}

		@Override
		protected JTextField getSearchField() {
			return (JTextField) super.getSearchField();
		}

	}

	private static class HistoryTextSearchInputField extends TextSearchInputField {

		public static int maximumHistoricalValues = TextHistoryInputField.defaultMaximumHistoricalValues;

		public HistoryTextSearchInputField() {
		}

		@Override
		protected JComponent createSearchField() {
			TextHistoryInputField field = new TextHistoryInputField(maximumHistoricalValues);
			field.addListener(new TextHistoryInputFieldListener() {

				@Override
				public void textHistoryValueChanged(TextHistoryInputField inputField) {
					submitSearch();
				}

				@Override
				public void textHistoryEscaped(TextHistoryInputField inputField) {
					escapeSearch();
				}
			});
			return field;
		}

		@Override
		public void selectSearchStringForEditing() {
			getSearchField().grabFocus();
			getSearchField().selectTextForEditing();
		}

		@Override
		public String getSearchString() {
			return getSearchField().getTextValue();
		}

		@Override
		protected void replaceSearchString(String value) {
			getSearchField().setTextValue(value);
		}

		@Override
		protected TextHistoryInputField getSearchField() {
			return (TextHistoryInputField) super.getSearchField();
		}

	}

}