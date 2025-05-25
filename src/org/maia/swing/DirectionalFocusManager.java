package org.maia.swing;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import org.maia.util.GenericListener;
import org.maia.util.GenericListenerList;

public class DirectionalFocusManager extends KeyAdapter {

	private Set<DirectionalFocusTransfer> focusTransfers;

	private Map<Component, Set<DirectionalFocusTransfer>> focusTransfersIndexedOnSource;

	private KeyEventMapper keyEventMapper;

	private Component focusOwner;

	private boolean focusTransferingLocked;

	private boolean requestFocusOnComponents = true;

	private GenericListenerList<FocusListener> listeners;

	public DirectionalFocusManager() {
		this.focusTransfers = new HashSet<DirectionalFocusTransfer>();
		this.focusTransfersIndexedOnSource = new HashMap<Component, Set<DirectionalFocusTransfer>>();
		this.keyEventMapper = new DefaultKeyEventMapper();
		this.listeners = new GenericListenerList<FocusListener>();
	}

	public void addListener(FocusListener listener) {
		getListeners().addListener(listener);
	}

	public void removeListener(FocusListener listener) {
		getListeners().removeListener(listener);
	}

	public void addFocusTransferBidirectional(Component source, Direction direction, Component destination) {
		addFocusTransfer(source, direction, destination);
		addFocusTransfer(destination, direction.getOpposite(), source);
	}

	public synchronized void addFocusTransfer(Component source, Direction direction, Component destination) {
		DirectionalFocusTransfer transfer = new DirectionalFocusTransfer(source, direction, destination);
		if (getFocusTransfers().add(transfer)) {
			Set<DirectionalFocusTransfer> transfers = getFocusTransfersIndexedOnSource().get(source);
			if (transfers == null) {
				transfers = new HashSet<DirectionalFocusTransfer>();
				getFocusTransfersIndexedOnSource().put(source, transfers);
			}
			transfers.add(transfer);
		}
	}

	public synchronized void removeAllFocusTransfers() {
		getFocusTransfers().clear();
		getFocusTransfersIndexedOnSource().clear();
	}

	public void removeFocusTransferBidirectional(Component source, Direction direction, Component destination) {
		removeFocusTransfer(source, direction, destination);
		removeFocusTransfer(destination, direction.getOpposite(), source);
	}

	public synchronized void removeFocusTransfer(Component source, Direction direction, Component destination) {
		removeFocusTransfer(new DirectionalFocusTransfer(source, direction, destination));
	}

	public synchronized void removeComponent(Component component) {
		for (DirectionalFocusTransfer transfer : new Vector<DirectionalFocusTransfer>(getFocusTransfers())) {
			if (transfer.getSource().equals(component) || transfer.getDestination().equals(component)) {
				removeFocusTransfer(transfer);
			}
		}
	}

	private void removeFocusTransfer(DirectionalFocusTransfer transfer) {
		if (getFocusTransfers().remove(transfer)) {
			Set<DirectionalFocusTransfer> transfers = getFocusTransfersIndexedOnSource().get(transfer.getSource());
			transfers.remove(transfer);
			if (transfers.isEmpty()) {
				getFocusTransfersIndexedOnSource().remove(transfer.getSource());
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent event) {
		Direction direction = getDirectionOf(event);
		if (direction != null) {
			transferFocus(direction);
		}
	}

	protected Direction getDirectionOf(KeyEvent event) {
		KeyEventMapper mapper = getKeyEventMapper();
		if (mapper != null) {
			return mapper.getDirectionOf(event);
		} else {
			return null;
		}
	}

	public boolean transferFocus(Direction direction) {
		Component source = getFocusOwner();
		if (source != null && direction != null && !isFocusTransferingLocked()) {
			DirectionalFocusTransfer transfer = findFocusTransfer(source, direction);
			if (transfer != null) {
				changeFocusOwner(transfer.getDestination());
				return true;
			}
		}
		return false;
	}

	public void clearFocusOwner() {
		changeFocusOwner(null);
	}

	public synchronized void changeFocusOwner(Component newFocusOwner) {
		Component oldFocusOwner = getFocusOwner();
		setFocusOwner(newFocusOwner);
		if (oldFocusOwner != null) {
			fireComponentLostFocus(oldFocusOwner);
		}
		if (newFocusOwner != null) {
			if (isRequestFocusOnComponents()) {
				newFocusOwner.requestFocus();
			}
			fireComponentGainedFocus(newFocusOwner);
		} else {
			fireFocusOwnerCleared();
		}
	}

	public void lockFocusTransfering() {
		setFocusTransferingLocked(true);
	}

	public void unlockFocusTransfering() {
		setFocusTransferingLocked(false);
	}

	private void fireComponentLostFocus(Component oldFocusOwner) {
		for (FocusListener listener : getListeners()) {
			listener.notifyComponentLostFocus(oldFocusOwner);
		}
	}

	private void fireComponentGainedFocus(Component newFocusOwner) {
		for (FocusListener listener : getListeners()) {
			listener.notifyComponentGainedFocus(newFocusOwner);
		}
	}

	private void fireFocusOwnerCleared() {
		for (FocusListener listener : getListeners()) {
			listener.notifyFocusOwnerCleared();
		}
	}

	private DirectionalFocusTransfer findFocusTransfer(Component source, Direction direction) {
		Set<DirectionalFocusTransfer> transfers = getFocusTransfersIndexedOnSource().get(source);
		if (transfers != null) {
			for (DirectionalFocusTransfer transfer : transfers) {
				if (transfer.getDirection().equals(direction))
					return transfer;
			}
		}
		return null;
	}

	private Set<DirectionalFocusTransfer> getFocusTransfers() {
		return focusTransfers;
	}

	private Map<Component, Set<DirectionalFocusTransfer>> getFocusTransfersIndexedOnSource() {
		return focusTransfersIndexedOnSource;
	}

	public KeyEventMapper getKeyEventMapper() {
		return keyEventMapper;
	}

	public void setKeyEventMapper(KeyEventMapper keyEventMapper) {
		this.keyEventMapper = keyEventMapper;
	}

	public boolean hasFocusOwner() {
		return getFocusOwner() != null;
	}

	public Component getFocusOwner() {
		return focusOwner;
	}

	private void setFocusOwner(Component focusOwner) {
		this.focusOwner = focusOwner;
	}

	public boolean isFocusTransferingLocked() {
		return focusTransferingLocked;
	}

	private void setFocusTransferingLocked(boolean locked) {
		this.focusTransferingLocked = locked;
	}

	public boolean isRequestFocusOnComponents() {
		return requestFocusOnComponents;
	}

	public void setRequestFocusOnComponents(boolean requestFocus) {
		this.requestFocusOnComponents = requestFocus;
	}

	private GenericListenerList<FocusListener> getListeners() {
		return listeners;
	}

	private static class DirectionalFocusTransfer {

		private Component source;

		private Direction direction;

		private Component destination;

		public DirectionalFocusTransfer(Component source, Direction direction, Component destination) {
			this.source = source;
			this.direction = direction;
			this.destination = destination;
		}

		@Override
		public int hashCode() {
			return Objects.hash(destination, direction, source);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DirectionalFocusTransfer other = (DirectionalFocusTransfer) obj;
			return Objects.equals(destination, other.destination) && direction == other.direction
					&& Objects.equals(source, other.source);
		}

		public Component getSource() {
			return source;
		}

		public Direction getDirection() {
			return direction;
		}

		public Component getDestination() {
			return destination;
		}

	}

	public static enum Direction {

		UP,

		DOWN,

		LEFT,

		RIGHT;

		public Direction getOpposite() {
			if (UP.equals(this)) {
				return DOWN;
			} else if (DOWN.equals(this)) {
				return UP;
			} else if (LEFT.equals(this)) {
				return RIGHT;
			} else if (RIGHT.equals(this)) {
				return LEFT;
			} else {
				return null;
			}
		}

	}

	public static interface KeyEventMapper {

		Direction getDirectionOf(KeyEvent event);

	}

	private static class DefaultKeyEventMapper implements KeyEventMapper {

		public DefaultKeyEventMapper() {
		}

		@Override
		public Direction getDirectionOf(KeyEvent event) {
			int code = event.getKeyCode();
			if (code == KeyEvent.VK_UP || code == KeyEvent.VK_KP_UP) {
				return Direction.UP;
			} else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_KP_DOWN) {
				return Direction.DOWN;
			} else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_KP_LEFT) {
				return Direction.LEFT;
			} else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_KP_RIGHT) {
				return Direction.RIGHT;
			} else {
				return null;
			}
		}

	}

	public static interface FocusListener extends GenericListener {

		void notifyComponentLostFocus(Component oldFocusOwner);

		void notifyComponentGainedFocus(Component newFocusOwner);

		void notifyFocusOwnerCleared();

	}

}