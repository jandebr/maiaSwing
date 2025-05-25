package org.maia.swing.text.pte.model;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.maia.util.io.IOUtils;
import org.maia.swing.text.pte.PlainTextDocumentCancellation;
import org.maia.swing.text.pte.PlainTextDocumentException;

public class PlainTextFileDocument extends PlainTextAbstractDocument {

	private File file;

	public static String NO_FILE_NAME = "new";

	private static File currentDirectory;

	private static FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("Text files (*.txt)",
			"txt");

	public PlainTextFileDocument() {
		this(null);
	}

	public PlainTextFileDocument(File file) {
		setFile(file);
	}

	@Override
	public String readText() throws PlainTextDocumentException {
		String text = "";
		if (fileExists()) {
			try {
				text = IOUtils.readTextFileContents(getFile()).toString();
			} catch (IOException e) {
				throw new PlainTextDocumentException(this,
						"Failed to load document from file '" + getFile().getName() + "'", e);
			}
		}
		return text;
	}

	@Override
	protected void doWriteText(String text) throws PlainTextDocumentException, PlainTextDocumentCancellation {
		if (!hasFile()) {
			setFile(selectFileToSave());
		}
		if (hasFile()) {
			try {
				IOUtils.writeTextFileContents(getFile(), text);
			} catch (IOException e) {
				throw new PlainTextDocumentException(this,
						"Failed to write document to file '" + getFile().getName() + "'", e);
			}
		} else {
			throw new PlainTextDocumentCancellation(this);
		}
	}

	public static File selectFileToOpen() {
		JFileChooser fileChooser = buildFileChooser(getCurrentDirectory());
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

	public static File selectFileToSave() {
		JFileChooser fileChooser = buildFileChooser(getCurrentDirectory());
		int returnValue = fileChooser.showSaveDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

	private static JFileChooser buildFileChooser(File currentDirectory) {
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(getFileNameExtensionFilter());
		return fileChooser;
	}

	@Override
	public String getShortDocumentName() {
		return hasFile() ? getFile().getName() : NO_FILE_NAME;
	}

	@Override
	public String getLongDocumentName() {
		return hasFile() ? getFile().getAbsolutePath() : NO_FILE_NAME;
	}

	@Override
	public boolean isDraft() {
		return !hasFile();
	}

	public boolean fileExists() {
		return hasFile() && getFile().exists();
	}

	public boolean hasFile() {
		return getFile() != null;
	}

	public File getFile() {
		return file;
	}

	private void setFile(File file) {
		if (file != null) {
			setCurrentDirectory(file.getParentFile());
			fireNameChanged();
		}
		this.file = file;
	}

	public static File getCurrentDirectory() {
		return currentDirectory;
	}

	public static void setCurrentDirectory(File directory) {
		currentDirectory = directory;
	}

	public static FileNameExtensionFilter getFileNameExtensionFilter() {
		return fileNameExtensionFilter;
	}

	public static void setFileNameExtensionFilter(FileNameExtensionFilter filter) {
		PlainTextFileDocument.fileNameExtensionFilter = filter;
	}

}