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

import java.util.Map;

import iot.jcypher.domain.mapping.FieldMapping;

public class Map2DO extends AbstractDeferred {

	private FieldMapping fieldMapping;
	private Map<Object, Object> map;
	private Object domainObject;
	
	public Map2DO(FieldMapping fieldMapping, Map<Object, Object> map,
			Object domainObject) {
		super();
		this.fieldMapping = fieldMapping;
		this.map = map;
		this.domainObject = domainObject;
	}

	@Override
	public void performUpdate() {
		if (!this.map.isEmpty()) // empty maps have been mapped to a property
			this.fieldMapping.setFieldValue(this.domainObject, this.map);
		modifyNextUp();
	}
	
	public Map<Object, Object> getMap() {
		return map;
	}
}
