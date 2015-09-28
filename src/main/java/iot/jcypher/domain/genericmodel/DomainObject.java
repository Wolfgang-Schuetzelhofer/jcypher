/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.domain.genericmodel;


public class DomainObject {

	private DOType domainObjectType;
	private Object rawObject;

	public DomainObject(DOType doType) {
		super();
		if (doType == null)
			throw new RuntimeException("a domain object must be constructed with a domain object type");
		this.domainObjectType = doType;
	}

	/**
	 * Answer the type of this generic domain object
	 * @return a DOType
	 */
	public DOType getDomainObjectType() {
		return domainObjectType;
	}
	
	public void setFieldValue(String fieldName, Object value) {
		DOField field = this.domainObjectType.getFieldByName(fieldName);
		if (field == null)
			throw new RuntimeException("field: " + fieldName + " not found in: " + this.domainObjectType.getName());
		field.setValue(this.getRawObject(), value);
	}
	
	public Object getFieldValue(String fieldName) {
		DOField field = this.domainObjectType.getFieldByName(fieldName);
		if (field == null)
			throw new RuntimeException("field: " + fieldName + " not found in: " + this.domainObjectType.getName());
		return field.getValue(this.getRawObject());
	}

	void setRawObject(Object rawObject) {
		this.rawObject = rawObject;
	}
	
	Object getRawObject() {
		if (this.rawObject == null) {
			try {
				this.rawObject = this.domainObjectType.getRawType().newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return this.rawObject;
	}
	
}
