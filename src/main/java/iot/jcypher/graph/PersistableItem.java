/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package iot.jcypher.graph;

import iot.jcypher.graph.internal.ChangeListener;

import java.util.ArrayList;
import java.util.List;

public abstract class PersistableItem {
	
	private List<ChangeListener> changeListeners;
	protected SyncState syncState;
	
	/**
	 * removes this item
	 */
	public void remove() {
		SyncState oldState = this.syncState;
		if (this.syncState == SyncState.NEW || this.syncState == SyncState.NEW_REMOVED)
			this.syncState = SyncState.NEW_REMOVED;
		else
			this.syncState = SyncState.REMOVED;
		if (oldState != this.syncState)
			fireChanged(oldState, this.syncState);
	}
	
	void addChangeListener(ChangeListener listener) {
		if (this.changeListeners == null)
			this.changeListeners = new ArrayList<ChangeListener>();
		if (!this.changeListeners.contains(listener))
			this.changeListeners.add(listener);
	}
	
	void removeChangeListener(ChangeListener listener) {
		if (this.changeListeners != null)
			this.changeListeners.remove(listener);
	}
	
	protected void fireChanged(SyncState oldState, SyncState newState) {
		if (this.changeListeners != null) {
			ArrayList<ChangeListener> lstnrs = new ArrayList<ChangeListener>();
			lstnrs.addAll(this.changeListeners);
			for (ChangeListener lstnr : lstnrs) {
				lstnr.changed(this, oldState, newState);
			}
		}
	}
	
	void notifyState() {
		if (this.changeListeners != null) {
			ArrayList<ChangeListener> lstnrs = new ArrayList<ChangeListener>();
			lstnrs.addAll(this.changeListeners);
			for (ChangeListener lstnr : lstnrs) {
				lstnr.changed(this, this.syncState, this.syncState);
			}
		}
	}
	
	SyncState getSyncState() {
		return syncState;
	}

	void setSyncState(SyncState syncState) {
		this.syncState = syncState;
	}
	
	abstract void setToSynchronized();
}
