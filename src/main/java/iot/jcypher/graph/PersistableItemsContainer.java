package iot.jcypher.graph;

import iot.jcypher.graph.internal.ChangeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PersistableItemsContainer<T extends PersistableItem> {

	private List<T> elements;
	private List<T> removedElements;
	private ElementChangeListener elementChangeListener;

	abstract SyncState getContainerSyncState();

	abstract void setContainerSyncState(SyncState syncState);

	protected abstract void fireContainerChanged(SyncState oldState,
			SyncState newState);

	/**
	 * calculate the container's state, check if it is sync
	 * 
	 * @return true, if the container's calculated state is sync
	 */
	protected abstract boolean checkContainerForSyncState();

	/**
	 * Resolve and initialize the appropriate elements from the underlying
	 * QueryResult JsonObject.
	 * 
	 * @return
	 */
	protected abstract List<T> resolveElements();
	/**
	 * @param elems
	 * @param elem
	 * @return true, if list 'elems' contains 'elem'
	 */
	protected abstract boolean containsElement(List<T> elems, T elem);

	/**
	 * @return an unmodifiable list of elements
	 */
	public List<T> getElements() {
		if (this.elements == null) {
			this.elements = resolveElements();
			if (this.elementChangeListener == null)
				this.elementChangeListener = new ElementChangeListener();
			for (T elem : this.elements) {
				elem.addChangeListener(this.elementChangeListener);
			}
		}

		// Build a new Array
		// to allow iterating over the elements with a for loop and to remove
		// elements.
		// That is done by calling remove() on the element,
		// which leads to removing the element from this.elements
		// That may not break the for loop
		ArrayList<T> list = new ArrayList<T>(this.elements);
		return Collections.unmodifiableList(list);
	}

	/**
	 * add a new element, throw a RuntimeException if the element already exists
	 * @param element
	 * @return the added element
	 */
	public T addElement(T element) {
		// make sure that elements are initialized
		getElements();
		if (!containsElement(this.elements, element)) {
			this.elements.add(element);
			element.addChangeListener(this.elementChangeListener);
			element.notifyState();
			return element;
		}
		throw new RuntimeException(element.toString() + " already exists");
	}
	
	public boolean checkForSyncState() {
		SyncState st = this.checkForElementStates(SyncState.SYNC);
		if (st == null)
			return this.removedElements == null || this.removedElements.isEmpty();
		return false;
	}
	
	/**
	 * check all elements, if their state is one of the given states
	 * @param states to check against
	 * @return null, if all elements have a state out of the requested states,
	 * else return the first element state that differs
	 */
	private SyncState checkForElementStates(SyncState... states) {
		if (this.elements != null) {
			for (T elem : this.elements) {
				for (SyncState state : states) {
					if (elem.getSyncState() != state)
						return elem.getSyncState();
				}
			}
		}
		return null;
	}

	/********************************************/
	private class ElementChangeListener implements ChangeListener {

		@SuppressWarnings("unchecked")
		@Override
		public void changed(Object changedElement, SyncState oldState,
				SyncState newState) {
			if (newState == SyncState.REMOVED
					|| newState == SyncState.NEW_REMOVED) {
				elements.remove(changedElement);
				if (newState == SyncState.REMOVED) {
					if (removedElements == null)
						removedElements = new ArrayList<T>();
					if (!containsElement(removedElements, (T)changedElement))
						removedElements.add((T) changedElement);
				}
			}

			SyncState myOldContainerState = getContainerSyncState();
			if (getContainerSyncState() == SyncState.SYNC) {
				if (newState != SyncState.SYNC)
					setContainerSyncState(SyncState.CHANGED);
				// possibly reverts the CHANGED state
			} else if (getContainerSyncState() == SyncState.CHANGED
					&& newState == SyncState.NEW_REMOVED) {
				if (checkContainerForSyncState())
					setContainerSyncState(SyncState.SYNC);
			}

			// notify if the element container's state has changed
			if (myOldContainerState != getContainerSyncState())
				fireContainerChanged(myOldContainerState,
						getContainerSyncState());
		}
	}
}
