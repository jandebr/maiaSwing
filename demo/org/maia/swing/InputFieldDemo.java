package org.maia.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.maia.swing.input.FileInputField;
import org.maia.swing.input.FolderInputField;
import org.maia.swing.input.GenericFileInputField;
import org.maia.swing.input.GenericFileInputFieldListener;
import org.maia.swing.input.TextHistoryInputField;
import org.maia.swing.input.TextHistoryInputFieldListener;
import org.maia.swing.input.TextSearchInputCommand;
import org.maia.swing.input.TextSearchInputField;

public class InputFieldDemo {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Input fields");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(createInputPanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static JPanel createInputPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(4, 4, 4, 4);
		c.gridy = 0;
		c.gridx = 0;
		panel.add(new JLabel("File of choice:"), c);
		c.gridx++;
		c.weightx = 1.0;
		panel.add(createFileInputField(), c);
		c.gridy = 1;
		c.gridx = 0;
		c.weightx = 0;
		panel.add(new JLabel("Folder of choice:"), c);
		c.gridx++;
		panel.add(createFolderInputField(), c);
		c.gridy = 2;
		c.gridx = 0;
		c.weightx = 0;
		panel.add(new JLabel("Text value:"), c);
		c.gridx++;
		panel.add(createTextHistoryInputField(), c);
		c.gridy = 3;
		c.gridx = 0;
		c.weightx = 0;
		panel.add(new JLabel("Search:"), c);
		c.gridx++;
		panel.add(createTextSearchInputField(), c);
		return panel;
	}

	private static FileInputField createFileInputField() {
		FileInputField field = new FileInputField();
		field.setShowAbsolutePath(false);
		field.setFileChooserDialogTitle("Choose a file");
		field.setFileChooserFilter(new FileNameExtensionFilter("JPG & GIF Images", "jpg", "gif"));
		field.addListener(new GenericFileInputFieldListener() {

			@Override
			public void fileSelectionChanged(GenericFileInputField inputField) {
				System.out.println("File changed: " + inputField.getFile());
			}
		});
		return field;
	}

	private static FolderInputField createFolderInputField() {
		FolderInputField field = new FolderInputField();
		field.setShowAbsolutePath(true);
		field.setFileChooserDialogTitle("Choose a folder");
		field.addListener(new GenericFileInputFieldListener() {

			@Override
			public void fileSelectionChanged(GenericFileInputField inputField) {
				System.out.println("Folder changed: " + inputField.getFile());
			}
		});
		field.disableClear();
		return field;
	}

	private static TextHistoryInputField createTextHistoryInputField() {
		TextHistoryInputField field = new TextHistoryInputField(3);
		field.addListener(new TextHistoryInputFieldListener() {

			@Override
			public void textHistoryValueChanged(TextHistoryInputField inputField) {
				System.out.println("Value changed: " + inputField.getTextValue());
			}

			@Override
			public void textHistoryEscaped(TextHistoryInputField inputField) {
				System.out.println("Escaped");
			}
		});
		field.addTextValueToHistory("lego");
		field.addTextValueToHistory("movie");
		return field;
	}

	private static TextSearchInputField createTextSearchInputField() {
		TextSearchInputField field = TextSearchInputField.createFieldWithHistory(3);
		field.setSearchCommand(new TextSearchInputCommand() {

			@Override
			public void execute(TextSearchInputField inputField) {
				String text = inputField.getSearchString();
				System.out.println("SEARCH *" + text + "*");
			}
		});
		return field;
	}

}