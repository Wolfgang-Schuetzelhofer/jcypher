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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import iot.jcypher.domain.genericmodel.internal.InternalAccess;
import iot.jcypher.domain.genericmodel.internal.DOWalker.IndexedField;
import iot.jcypher.domain.internal.DomainAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;


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
		Object raw = field.getValue(this.getRawObject());
		DomainObject gdo = getForRawObject(raw);
		if (gdo != null)
			return gdo;
		return raw;
	}
	
	/**
	 * if the field is a list or array, answer the value at the given index.
	 * @param fieldName
	 * @param index
	 * @return
	 */
	public Object getFieldValue(String fieldName, int index) {
		Object ret = null;
		Object val = getFieldValue(fieldName);
		if (val instanceof List<?>) {
			List<?> list = (List<?>)val;
			Object cval = list.get(index);
			DomainObject gdo = getForRawObject(cval);
			if (gdo != null)
				ret = gdo;
			else
				ret = cval;
		} else if (val != null && val.getClass().isArray()) {
			Object aval = Array.get(val, index);
			DomainObject gdo = getForRawObject(aval);
			if (gdo != null)
				ret = gdo;
			else
				ret = aval;
		}
		return ret;
	}
	
	private DomainObject getForRawObject(Object raw) {
		if (raw != null) {
			DomainAccess da = InternalAccess.getDomainAccess(this.domainObjectType.getDomainModel());
			DomainObject gdo = ((IIntDomainAccess)da).getInternalDomainAccess().getGenericDomainObject(raw);
			if (gdo != null)
				return gdo;
		}
		return null;
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
