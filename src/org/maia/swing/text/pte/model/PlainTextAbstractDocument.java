package org.maia.swing.text.pte.model;

import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.text.pte.PlainTextDocumentCancellation;
import org.maia.swing.text.pte.PlainTextDocumentException;

public abstract class PlainTextAbstractDocument implements PlainTextDocument {

	private boolean editable = true;

	private List<PlainTextDocumentListener> listeners;

	protected PlainTextAbstractDocument() {
		this.listeners = new Vector<PlainTextDocumentListener>();
	}

	@Override
	public void addListener(PlainTextDocumentListener listener) {
		getListeners().add(listener);
	}

	@Override
	public void removeListener(PlainTextDocumentListener listener) {
		getListeners().remove(listener);
	}

	@Override
	public final void writeText(String text) throws PlainTextDocumentException, PlainTextDocumentCancellation {
		if (!isEditable())
			throw new PlainTextDocumentException(this, "Document is not editable");
		doWriteText(text);
	}

	protected abstract void doWriteText(String text) throws PlainTextDocumentException, PlainTextDocumentCancellation;

	protected void fireEditableChanged() {
		for (PlainTextDocumentListener listener : getListeners()) {
			listener.documentEditableChanged(this);
		}
	}

	protected void fireNameChanged() {
		for (PlainTextDocumentListener listener : getListeners()) {
			listener.documentNameChanged(this);
		}
	}

	@Override
	public Icon getSmallDocumentIcon() {
		return ImageUtils.getIcon("org/maia/swing/icons/text/textdoc16.png");
	}

	@Override
	public Icon getLargeDocumentIcon() {
		return ImageUtils.getIcon("org/maia/swing/icons/text/textdoc32.png");
	}

	@Override
	public String getLongDocumentName() {
		return getShortDocumentName();
	}

	@Override
	public boolean isDraft() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		boolean changed = this.editable != editable;
		this.editable = editable;
		if (changed)
			fireEditableChanged();
	}

	protected List<PlainTextDocumentListener> getListeners() {
		return listeners;
	}

}