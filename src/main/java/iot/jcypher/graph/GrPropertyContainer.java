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

public abstract class GrPropertyContainer extends GrElement {

	private GrId id;
	private PropertiesContainer propertiesContainer;
	
	GrPropertyContainer(ResultHandler resultHandler, GrId id, int rowIdx) {
		super(resultHandler, rowIdx);
		this.id = id;
		this.syncState = SyncState.NEW;
	}
	
	GrId getGrId() {
		return this.id;
	}
	
	void setGrId(GrId grId) {
		this.id = grId;
	}

	public long getId() {
		return this.id.getId();
	}

	/**
	 * @return an unmodifiable list of properties
	 */
	public List<GrProperty> getProperties() {
		return getPropertiesContainer().getElements();
	}
	
	/**
	 * return a property
	 * @param propertyName
	 * @return a GrProperty
	 */
	public GrProperty getProperty(String propertyName) {
		for (GrProperty prop : getProperties()) {
			if (prop.getName().equals(propertyName))
				return prop;
		}
		return null;
	}
	
	/**
	 * add a new property, throw a RuntimeException if the property already exists
	 * @param name of the property
	 * @param value of the property
	 * @return the added property
	 */
	public GrProperty addProperty(String name, Object value) {
		GrProperty prop = GrAccess.createProperty(name);
		prop.setValue(value);
		return getPropertiesContainer().addElement(prop);
	}
	
	public Graph getGraph() {
		return this.resultHandler.getGraph();
	}
	
	PropertiesContainer getPropertiesContainer() {
		if (this.propertiesContainer == null)
			this.propertiesContainer = new PropertiesContainer();
		return this.propertiesContainer;
	}

	private boolean containsProperty(List<GrProperty> list,
			GrProperty prop) {
		String nm = prop.getName();
		for (GrProperty p : list) {
			if (p.getName().equals(nm))
				return true;
		}
		return false;
	}

	private List<GrProperty> resolveProperties() {
		if (this instanceof GrNode)
			return this.resultHandler.getNodeProperties(this.id, this.rowIndex);
		else if (this instanceof GrRelation)
			return this.resultHandler.getRelationProperties(this.id, this.rowIndex);
		return null;
	}
	
	protected boolean testForSyncState() {
		if (this.propertiesContainer != null) {
			return this.propertiesContainer.checkForSyncState();
		}
		return true;
	}
	
	@Override
	void setToSynchronized() {
		if (this.propertiesContainer != null)
			this.propertiesContainer.setToSynchronized();
	}

	/********************************************/
	private class PropertiesContainer extends PersistableItemsContainer<GrProperty> {

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
		protected List<GrProperty> resolveElements() {
			return resolveProperties();
		}

		@Override
		protected boolean containsElement(List<GrProperty> elems,
				GrProperty elem) {
			return containsProperty(elems, elem);
		}
		
	}
}
