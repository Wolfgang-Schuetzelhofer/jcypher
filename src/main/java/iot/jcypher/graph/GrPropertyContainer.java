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
import iot.jcypher.result.util.ResultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GrPropertyContainer extends GrElement {

	private long id;
	private List<GrProperty> properties;
	private List<GrProperty> removedProperties;
	private PropertyChangeListener propertyChangeListener;

	GrPropertyContainer(ResultHandler resultHandler, long id, int rowIdx) {
		super(resultHandler, rowIdx);
		this.id = id;
		this.syncState = SyncState.NEW;
	}

	public long getId() {
		return id;
	}

	/**
	 * @return an unmodifiable list of properties
	 */
	public List<GrProperty> getProperties() {
		if (this.properties == null) {
			this.properties = resolveProperties();
			if (this.propertyChangeListener == null)
				this.propertyChangeListener = new PropertyChangeListener();
			for (GrProperty prop : this.properties) {
				prop.addChangeListener(this.propertyChangeListener);
			}
		}
		
		// Build a new Array
			// to allow iterating over the properties with a for loop and to remove properties.
			// That is done by calling remove() on the property,
			// which leads to removing the property from this.properties
			// That may not break the for loop
		ArrayList<GrProperty> list = new ArrayList<GrProperty>(this.properties);
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * add a new property, throw a RuntimeException if the property already exists
	 * @param name of the property
	 * @param value of the property
	 * @return
	 */
	public GrProperty addProperty(String name, Object value) {
		GrProperty prop = GrAccess.createProperty(name);
		prop.setValue(value);
		// make sure that properties are initialized
		getProperties();
		if (!containsProperty(this.properties, prop)) {
			this.properties.add(prop);
			prop.addChangeListener(this.propertyChangeListener);
			prop.notifyState();
			return prop;
		}
		throw new RuntimeException("property: " + name + " already exists");
	}

	private boolean containsProperty(List<GrProperty> properties2,
			GrProperty prop) {
		String nm = prop.getName();
		for (GrProperty p : properties2) {
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
	
	/**
	 * check all properties, if their state is one of the given states
	 * @param states to check against
	 * @return null, if all properties have a state out of the requested states,
	 * else return the first property state that differs
	 */
	private SyncState checkForPropertyStates(SyncState... states) {
		if (this.properties != null) {
			for (GrProperty prop : this.properties) {
				for (SyncState state : states) {
					if (prop.getSyncState() != state)
						return prop.getSyncState();
				}
			}
		}
		return null;
	}

	/********************************************/
	private class PropertyChangeListener implements ChangeListener {

		@Override
		public void changed(Object theChanged, SyncState oldState,
				SyncState newState) {
			if (newState == SyncState.REMOVED || newState == SyncState.NEW_REMOVED) {
				properties.remove(theChanged);
				if (newState == SyncState.REMOVED) {
					if (removedProperties == null)
						removedProperties = new ArrayList<GrProperty>();
					if (!removedProperties.contains(theChanged))
						removedProperties.add((GrProperty)theChanged);
				}
			}
			
			SyncState myOldState = syncState;
			if (syncState == SyncState.SYNC) {
				if (newState != SyncState.SYNC)
					syncState = SyncState.CHANGED;
			// possibly reverts the CHANGED state
			} else if (syncState == SyncState.CHANGED && newState == SyncState.NEW_REMOVED) {
				SyncState st = checkForPropertyStates(SyncState.SYNC);
				if (st == null)
					syncState = SyncState.SYNC;
			}
			
			if (myOldState != syncState)
				fireChanged(myOldState, syncState);
		}
	}
	
}
