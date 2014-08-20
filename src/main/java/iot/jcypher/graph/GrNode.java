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

import iot.jcypher.graph.internal.GrId;
import iot.jcypher.result.util.ResultHandler;

import java.util.List;

public class GrNode extends GrPropertyContainer {

	private LabelsContainer labelsContainer;
	
	GrNode(ResultHandler resultHandler, GrId id, int rowIdx) {
		super(resultHandler, id, rowIdx);
	}

	/**
	 * @return an unmodifiable list of node labels
	 */
	public List<GrLabel> getLabels() {
		return getLabelsContainer().getElements();
	}
	
	/**
	 * return a label
	 * @param labelName
	 * @return a GrLabel
	 */
	public GrLabel getLabel(String labelName) {
		for (GrLabel lab : getLabels()) {
			if (lab.getName().equals(labelName))
				return lab;
		}
		return null;
	}
	
	/**
	 * add a new label, throw a RuntimeException if the label already exists
	 * @param name of the label
	 * @return the added label
	 */
	public GrLabel addLabel(String name) {
		GrLabel lab = GrAccess.createLabel(name);
		return getLabelsContainer().addElement(lab);
	}
	
	@Override
	protected boolean testForSyncState() {
		if (super.testForSyncState()) {
			if (this.labelsContainer != null) {
				return this.labelsContainer.checkForSyncState();
			}
			return true;
		}
		return false;
	}

	private List<GrLabel> resolveLabels() {
		return this.resultHandler.getNodeLabels(getId(), this.rowIndex);
	}
	
	private boolean containslabel(List<GrLabel> list,
			GrLabel lab) {
		String nm = lab.getName();
		for (GrLabel l : list) {
			if (l.getName().equals(nm))
				return true;
		}
		return false;
	}
	
	LabelsContainer getLabelsContainer() {
		if (this.labelsContainer == null)
			this.labelsContainer = new LabelsContainer();
		return this.labelsContainer;
	}
	
	@Override
	void setToSynchronized() {
		if (this.labelsContainer != null)
			this.labelsContainer.setToSynchronized();
		super.setToSynchronized();
		setSyncState(SyncState.SYNC);
	}
	
	/********************************************/
	private class LabelsContainer extends PersistableItemsContainer<GrLabel> {

		@Override
		SyncState getContainerSyncState() {
			return getSyncState();
		}

		@Override
		void setContainerSyncState(SyncState syncState) {
			setSyncState(syncState);
		}

		@Override
		protected void fireContainerChanged(SyncState oldState,
				SyncState newState) {
			fireChanged(oldState, newState);
		}

		@Override
		protected boolean checkContainerForSyncState() {
			return testForSyncState();
		}

		@Override
		protected List<GrLabel> resolveElements() {
			return resolveLabels();
		}

		@Override
		protected boolean containsElement(List<GrLabel> elems, GrLabel elem) {
			return containslabel(elems, elem);
		}
	}
}
