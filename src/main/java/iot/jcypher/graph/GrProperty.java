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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;


public class GrProperty extends PersistableItem {

	private String name;
	private Object value;

	GrProperty(String name) {
		super();
		this.name = name;
		this.syncState = SyncState.NEW;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		Object val = value;
		if (val != null && val.getClass().isArray()) {
			int length = Array.getLength(val);
			ArrayList<Object> list = new ArrayList<>();
		    for (int i = 0; i < length; i ++) {
		        Object arrayElement = Array.get(val, i);
		        list.add(arrayElement);
		    }
		    val = list;
		}
		Object oldVal = this.value;
		this.value = val;
		// don't change syncState NEW on first setting a property value
		if (oldVal != null && oldVal != this.value) {
			SyncState oldState = this.syncState;
			if (this.syncState == SyncState.SYNC)
				this.syncState = SyncState.CHANGED;
			if (oldState != this.syncState)
				fireChanged(oldState, this.syncState);
		}
	}
	
	@Override
	void setToSynchronized() {
		setSyncState(SyncState.SYNC);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Property: ");
		sb.append(this.name);
		sb.append(" = ");
		if (this.value != null)
			sb.append(this.value);
		else
			sb.append("null");
		return sb.toString();
	}
	
}
