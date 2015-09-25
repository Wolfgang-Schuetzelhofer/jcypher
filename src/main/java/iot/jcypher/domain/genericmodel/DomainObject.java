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

import java.util.ArrayList;
import java.util.List;

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
	
	/**
	 * Answer the field- (attribute) definitions of this object's type and it' super types.
	 * <br/> I.e. all fields that can be set or retrieved on this domain object.
	 * @return a list of DOField
	 */
	public List<DOField> getFields() {
		List<DOField> ret = new ArrayList<DOField>();
		DOType typ = this.domainObjectType;
		while(typ != null) {
			ret.addAll(typ.getFields());
			typ = typ.getSuperType();
		}
		return ret;
	}
	
	/**
	 * Answer a list of all field names of this object's type and it' super types.
	 * @return
	 */
	public List<String> getFieldNames() {
		List<String> ret = new ArrayList<String>();
		DOType typ = this.domainObjectType;
		while(typ != null) {
			ret.addAll(typ.getFieldNames());
			typ = typ.getSuperType();
		}
		return ret;
	}
	
	/**
	 * Answer the field with the given name.
	 * <br/>Answer null if a field with the given name does not exist.
	 * @param fieldName
	 * @return
	 */
	public DOField getFieldByName(String fieldName) {
		int idx = this.getIndexOfField(fieldName);
		if (idx != -1)
			return this.getFieldByIndex(idx);
		return null;
	}
	
	/**
	 * Answer the field at the given index in the list of all fields.
	 * @param index
	 * @return
	 */
	public DOField getFieldByIndex(int index) {
		return this.getFields().get(index);
	}
	
	/**
	 * Answer the index of the field with the given name within the list of all fields.
	 * <br/>Answer -1 if a field with the given name does not exist.
	 * @param fieldName
	 * @return
	 */
	public int getIndexOfField(String fieldName) {
		List<String> fnms = this.getFieldNames();
		for (int i = 0; i < fnms.size(); i++) {
			if (fnms.get(i).equals(fieldName))
				return i;
		}
		return -1;
	}
	
	public void setFieldValue(String fieldName, Object value) {
		DOField field = getFieldByName(fieldName);
		if (field == null)
			throw new RuntimeException("field: " + fieldName + " not found in: " + this.domainObjectType.getName());
		field.setValue(this.getRawObject(), value);
	}
	
	public Object getFieldValue(String fieldName) {
		DOField field = getFieldByName(fieldName);
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
