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

import iot.jcypher.domain.mapping.FieldMapping;

public class Deferred2DO extends AbstractDeferred {

	private FieldMapping fieldMapping;
	// a map or a collection
	private AbstractSurrogate deferred;
	private Object domainObject;
	
	public Deferred2DO(FieldMapping fieldMapping, AbstractSurrogate deferred,
			Object domainObject) {
		super();
		this.fieldMapping = fieldMapping;
		this.deferred = deferred;
		this.domainObject = domainObject;
	}

	@Override
	public void performUpdate() {
		this.fieldMapping.setFieldValue(this.domainObject, this.deferred);
		modifyNextUp();
	}
	
	public AbstractSurrogate getDeferred() {
		return this.deferred;
	}

	@Override
	public boolean isRoot() {
		return true;
	}

	@Override
	public void breakLoop() {
		for (IDeferred deferred : this.downInTree) {
			deferred.breakLoop();
		}
	}
	
}
