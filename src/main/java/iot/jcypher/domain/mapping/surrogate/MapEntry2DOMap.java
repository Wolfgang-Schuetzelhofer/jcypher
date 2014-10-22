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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapEntry2DOMap extends AbstractDeferred {
	private MapEntry mapEntry;
	private Map<Object, Object> map;

	public MapEntry2DOMap(MapEntry mapEntry, Map<Object, Object> map) {
		super();
		this.mapEntry = mapEntry;
		this.map = map;
	}

	@Override
	public void performUpdate() {
		this.map.put(this.mapEntry.getKey(), this.mapEntry.getValue());
		modifyNextUp();
	}

	public MapEntry getMapEntry() {
		return mapEntry;
	}
	
	public Map<Object, Object> getMap() {
		return map;
	}

	@Override
	public int hashCode() {
		return mapEntry.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapEntry2DOMap other = (MapEntry2DOMap) obj;
		return this.mapEntry == other.mapEntry;
	}
}
