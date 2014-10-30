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

import java.util.Collection;
import java.util.Map;

import iot.jcypher.domain.mapping.FieldMapping;

public class Deferred2DO extends AbstractDeferred {

	private FieldMapping fieldMapping;
	// a map or a collection
	private Object deferred;
	private Object domainObject;
	
	public Deferred2DO(FieldMapping fieldMapping, Object deferred,
			Object domainObject) {
		super();
		this.fieldMapping = fieldMapping;
		this.deferred = deferred;
		this.domainObject = domainObject;
	}

	@Override
	public void performUpdate() {
		if (!this.isEmpty()) // empty maps or lists have been mapped to a property
			this.fieldMapping.setFieldValue(this.domainObject, this.deferred);
		modifyNextUp();
	}
	
	private boolean isEmpty() {
		if (this.deferred instanceof Map<?, ?>)
			return ((Map<?, ?>)this.deferred).isEmpty();
		else if (this.deferred instanceof Collection<?>)
			return ((Collection<?>)this.deferred).isEmpty();
		return this.deferred != null;
	}
	
	public Object getDeferred() {
		return this.deferred;
	}
}
