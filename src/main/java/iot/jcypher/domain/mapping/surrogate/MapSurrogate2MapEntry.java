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

package iot.jcypher.domain.mapping.surrogate;


public class MapSurrogate2MapEntry extends AbstractDeferred {

	public static final String keyField = "key";
	public static final String valueField = "value";
	
	private String field;
	private MapEntry mapEntry;
	private Map mapSurrogate;
	
	public MapSurrogate2MapEntry(String field, MapEntry domainObject,
			Map mapSurrogate) {
		super();
		this.field = field;
		this.mapEntry = domainObject;
		this.mapSurrogate = mapSurrogate;
	}

	@Override
	public void performUpdate() {
		if (this.field.equals(keyField)) {
			this.mapEntry.setKey(this.mapSurrogate.getContent());
			modifyNextUp();
		} else if (this.field.equals(valueField)) {
			this.mapEntry.setValue(this.mapSurrogate.getContent());
			modifyNextUp();
		}
	}

	public MapEntry getMapEntry() {
		return mapEntry;
	}
	
	public Map getMapSurrogate() {
		return mapSurrogate;
	}

	public boolean isKey () {
		return this.field.equals(keyField);
	}
	
	public boolean isValue () {
		return this.field.equals(valueField);
	}

	@Override
	public void addNextUpInTree(IDeferred deferred) {
		if (!upInTree.isEmpty())
			throw new RuntimeException("can only have one parent!");
		super.addNextUpInTree(deferred);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mapEntry == null) ? 0 : mapEntry.hashCode());
		result = prime * result
				+ ((field == null) ? 0 : field.hashCode());
		result = prime * result
				+ ((mapSurrogate == null) ? 0 : mapSurrogate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapSurrogate2MapEntry other = (MapSurrogate2MapEntry) obj;
		if (mapEntry == null) {
			if (other.mapEntry != null)
				return false;
		} else if (!mapEntry.equals(other.mapEntry))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (mapSurrogate != other.mapSurrogate)
			return false;
		return true;
	}
	
}
