package org.maia.swing.text.pte.search;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.maia.swing.input.TextHistoryInputField;
import org.maia.swing.input.TextHistoryInputFieldListener;
import org.maia.swing.input.TextSearchInputField;
import org.maia.swing.input.TextSearchInputFieldListener;
import org.maia.swing.text.pte.PlainTextDocumentEditor;

@SuppressWarnings("serial")
public class FindReplacePanel extends JPanel {

	private List<FindReplacePanelListener> listeners;

	private TextSearchInputField searchField;

	private TextHistoryInputField replaceField;

	private JCheckBox caseSensitiveCheckBox;

	private JCheckBox regexCheckBox;

	private JCheckBox wrapAroundCheckBox;

	private JButton nextButton;

	private JButton previousButton;

	private JButton replaceButton;

	private JButton replaceAllButton;

	private JLabel resultsLabel;

	private PlainTextDocumentEditor documentEditor;

	private TextSearchCommand searchCommand;

	private StyledTextSearchResult searchResult;

	private TextSearchResultStyling searchResultStyling;

	private boolean searchResultReplaced;

	public FindReplacePanel() {
		super(new BorderLayout());
		this.listeners = new Vector<FindReplacePanelListener>();
		this.searchField = createSearchField();
		this.replaceField = createReplaceField();
		this.caseSensitiveCheckBox = createCaseSensitiveCheckBox();
		this.regexCheckBox = createRegexCheckBox();
		this.wrapAroundCheckBox = createWrapAroundCheckBox();
		this.nextButton = createNextButton();
		this.previousButton = createPreviousButton();
		this.replaceButton = createReplaceButton();
		this.replaceAllButton = createReplaceAllButton();
		this.resultsLabel = createResultsLabel();
		buildUI();
	}

	private TextSearchInputField createSearchField() {
		TextSearchInputField field = TextSearchInputField.createFieldWithHistory();
		field.addListener(new TextSearchInputFieldListener() {

			@Override
			public void textSearchStringChanged(TextSearchInputField inputField) {
				performSearch();
			}

			@Override
			public void textSearchEscaped(TextSearchInputField inputField) {
				fireEscape();
			}
		});
		return field;
	}

	private TextHistoryInputField createReplaceField() {
		TextHistoryInputField field = new TextHistoryInputField();
		field.addListener(new TextHistoryInputFieldListener() {

			@Override
			public void textHistoryValueChanged(TextHistoryInputField inputField) {
				// no action
			}

			@Override
			public void textHistoryEscaped(TextHistoryInputField inputField) {
				fireEscape();
			}
		});
		return field;
	}

	private JCheckBox createCaseSensitiveCheckBox() {
		JCheckBox box = new JCheckBox("Match case");
		box.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				performSearch();
			}
		});
		return box;
	}

	private JCheckBox createRegexCheckBox() {
		JCheckBox box = new JCheckBox("Regular expression");
		box.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				performSearch();
			}
		});
		return box;
	}

	private JCheckBox createWrapAroundCheckBox() {
		JCheckBox box = new JCheckBox("Wrap around");
		box.setSelected(true);
		box.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateButtonEnablement();
			}
		});
		return box;
	}

	private JButton createNextButton() {
		JButton button = new JButton("Next");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				nextMatch();
			}
		});
		return button;
	}

	private JButton createPreviousButton() {
		JButton button = new JButton("Previous");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				previousMatch();
			}
		});
		return button;
	}

	private JButton createReplaceButton() {
		JButton button = new JButton("Replace");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				replaceCurrentMatch();
			}
		});
		return button;
	}

	private JButton createReplaceAllButton() {
		JButton button = new JButton("Replace all");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				replaceAllMatches();
			}
		});
		return button;
	}

	private JLabel createResultsLabel() {
		JLabel label = new JLabel("no matches");
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
		return label;
	}

	private void buildUI() {
		Box box = Box.createVerticalBox();
		box.add(createFieldsPanel());
		box.add(createOptionsPanel());
		box.add(createResultsPanel());
		box.add(createButtonsPanel());
		add(box, BorderLayout.CENTER);
	}

	private JComponent createFieldsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(4, 4, 4, 4);
		c.gridy = 0;
		c.gridx = 0;
		panel.add(new JLabel("Find"), c);
		c.gridx++;
		c.weightx = 1.0;
		panel.add(getSearchField(), c);
		c.weightx = 0;
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Replace with"), c);
		c.gridx++;
		panel.add(getReplaceField(), c);
		return panel;
	}

	private JComponent createOptionsPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 1, 0, 0));
		panel.add(getCaseSensitiveCheckBox());
		panel.add(getRegexCheckBox());
		panel.add(getWrapAroundCheckBox());
		return panel;
	}

	private JComponent createButtonsPanel() {
		JPanel grid = new JPanel(new GridLayout(2, 2, 8, 8));
		grid.add(getPreviousButton());
		grid.add(getNextButton());
		grid.add(getReplaceButton());
		grid.add(getReplaceAllButton());
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(grid);
		box.add(Box.createHorizontalGlue());
		return box;
	}

	private JComponent createResultsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(getResultsLabel(), BorderLayout.WEST);
		panel.add(Box.createVerticalStrut(16), BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
		return panel;
	}

	public void addListener(FindReplacePanelListener listener) {
		getListeners().add(listener);
	}

	public void removeListener(FindReplacePanelListener listener) {
		getListeners().remove(listener);
	}

	public void init(PlainTextDocumentEditor documentEditor) {
		setDocumentEditor(documentEditor);
		setSearchResultStyling(documentEditor.createDefaultSearchResultStyling());
		clearSearchResult();
		repeatSearch();
		getSearchField().selectSearchStringForEditing();
	}

	public void end() {
		clearSearchResult();
	}

	public synchronized void performSearch() {
		clearSearchResult();
		TextSearchCommand command = new TextSearchCommand(getSearchString(), isCaseSensitive(), isRegex());
		setSearchCommand(command);
		if (getDocumentEditor() != null && !command.getSearchString().isEmpty()) {
			StyledTextSearchResult result = getDocumentEditor().search(command);
			result.applyStyling(getSearchResultStyling());
			result.showCurrentMatchInFocus();
			setSearchResult(result);
			setSearchResultReplaced(false);
			updateResultsLabel();
			updateButtonEnablement();
		}
	}

	public synchronized void nextMatch() {
		StyledTextSearchResult result = getSearchResult();
		if (result != null) {
			if (isSearchResultReplaced()) {
				result.end();
				result = getDocumentEditor().search(getSearchCommand(), TextSearchResultPlacement.MATCH_AFTER_CARET);
				result.applyStyling(getSearchResultStyling());
				setSearchResult(result);
				setSearchResultReplaced(false);
				updateResultsLabel();
			} else {
				result.nextMatch(isWrapAround());
			}
			result.showCurrentMatchInFocus();
			updateButtonEnablement();
		}
	}

	public synchronized void previousMatch() {
		StyledTextSearchResult result = getSearchResult();
		if (result != null) {
			if (isSearchResultReplaced()) {
				result.end();
				result = getDocumentEditor().search(getSearchCommand(), TextSearchResultPlacement.MATCH_BEFORE_CARET);
				result.applyStyling(getSearchResultStyling());
				setSearchResult(result);
				setSearchResultReplaced(false);
				updateResultsLabel();
			} else {
				result.previousMatch(isWrapAround());
			}
			result.showCurrentMatchInFocus();
			updateButtonEnablement();
		}
	}

	public synchronized void replaceCurrentMatch() {
		if (hasMatches()) {
			StyledTextSearchResult result = getSearchResult();
			TextSearchResultStyling styling = getSearchResultStyling();
			result.replaceCurrentMatchWith(getReplacementString());
			setSearchResultReplaced(true);
			if (result.hasMatches()) {
				// conceal match advancement
				if (styling.isHighlightAnyMatch()) {
					result.currentMatch().highlight(styling.getAnyMatchHighlightColor());
				} else {
					result.currentMatch().removeHighlight();
				}
				getReplaceButton().setEnabled(false);
				getReplaceAllButton().setEnabled(false);
			} else {
				updateButtonEnablement(); // disable all
			}
			updateResultsLabel();
		}
	}

	public synchronized void replaceAllMatches() {
		if (hasMatches()) {
			getSearchResult().replaceAllMatchesWith(getReplacementString());
			clearSearchResult();
		}
	}

	protected void repeatSearch() {
		getSearchField().setSearchString(getSearchString());
	}

	protected void clearSearchResult() {
		StyledTextSearchResult result = getSearchResult();
		if (result != null) {
			result.end();
			setSearchResult(null);
			setSearchResultReplaced(false);
		}
		updateResultsLabel();
		updateButtonEnablement();
	}

	protected void updateButtonEnablement() {
		StyledTextSearchResult result = getSearchResult();
		if (result != null) {
			boolean wrapAround = isWrapAround();
			boolean editable = getDocumentEditor().isEditable();
			getPreviousButton().setEnabled(result.hasPreviousMatch(wrapAround));
			getNextButton().setEnabled(result.hasNextMatch(wrapAround));
			getReplaceButton().setEnabled(editable && result.hasMatches() && !isSearchResultReplaced());
			getReplaceAllButton().setEnabled(editable && result.hasMatches() && !isSearchResultReplaced());
		} else {
			getPreviousButton().setEnabled(false);
			getNextButton().setEnabled(false);
			getReplaceButton().setEnabled(false);
			getReplaceAllButton().setEnabled(false);
		}
	}

	protected void updateResultsLabel() {
		String text = "";
		StyledTextSearchResult result = getSearchResult();
		if (result != null) {
			int n = result.getNumberOfMatches();
			if (n == 0) {
				text = "no matches";
			} else if (n == 1) {
				text = "1 match";
			} else {
				text = n + " matches";
			}
		}
		getResultsLabel().setText(text);
	}

	private void fireEscape() {
		for (FindReplacePanelListener listener : getListeners()) {
			listener.notifyEscape();
		}
	}

	public boolean hasMatches() {
		StyledTextSearchResult result = getSearchResult();
		return result != null && result.hasMatches();
	}

	protected String getSearchString() {
		return getSearchField().getSearchString();
	}

	protected String getReplacementString() {
		return getReplaceField().getTextValue();
	}

	protected boolean isCaseSensitive() {
		return getCaseSensitiveCheckBox().isSelected();
	}

	protected boolean isRegex() {
		return getRegexCheckBox().isSelected();
	}

	protected boolean isWrapAround() {
		return getWrapAroundCheckBox().isSelected();
	}

	private List<FindReplacePanelListener> getListeners() {
		return listeners;
	}

	protected TextSearchInputField getSearchField() {
		return searchField;
	}

	protected TextHistoryInputField getReplaceField() {
		return replaceField;
	}

	protected JCheckBox getCaseSensitiveCheckBox() {
		return caseSensitiveCheckBox;
	}

	protected JCheckBox getRegexCheckBox() {
		return regexCheckBox;
	}

	protected JCheckBox getWrapAroundCheckBox() {
		return wrapAroundCheckBox;
	}

	protected JButton getNextButton() {
		return nextButton;
	}

	protected JButton getPreviousButton() {
		return previousButton;
	}

	protected JButton getReplaceButton() {
		return replaceButton;
	}

	protected JButton getReplaceAllButton() {
		return replaceAllButton;
	}

	protected JLabel getResultsLabel() {
		return resultsLabel;
	}

	protected PlainTextDocumentEditor getDocumentEditor() {
		return documentEditor;
	}

	private void setDocumentEditor(PlainTextDocumentEditor documentEditor) {
		this.documentEditor = documentEditor;
	}

	protected TextSearchCommand getSearchCommand() {
		return searchCommand;
	}

	private void setSearchCommand(TextSearchCommand searchCommand) {
		this.searchCommand = searchCommand;
	}

	protected StyledTextSearchResult getSearchResult() {
		return searchResult;
	}

	private void setSearchResult(StyledTextSearchResult searchResult) {
		this.searchResult = searchResult;
	}

	public TextSearchResultStyling getSearchResultStyling() {
		return searchResultStyling;
	}

	public void setSearchResultStyling(TextSearchResultStyling styling) {
		this.searchResultStyling = styling;
	}

	private boolean isSearchResultReplaced() {
		return searchResultReplaced;
	}

	private void setSearchResultReplaced(boolean replaced) {
		this.searchResultReplaced = replaced;
	}

}