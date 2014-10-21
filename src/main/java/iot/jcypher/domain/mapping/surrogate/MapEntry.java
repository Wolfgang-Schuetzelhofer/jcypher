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

import iot.jcypher.domain.mapping.IMapEntry;


public class MapEntry implements IMapEntry {

	private Object key;
	private Object value;
	
	public MapEntry(Object key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public MapEntry() {
		super();
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
