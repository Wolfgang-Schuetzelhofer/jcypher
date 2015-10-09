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

import iot.jcypher.domain.genericmodel.internal.InternalAccess;
import iot.jcypher.domain.internal.DomainAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class DomainObject {

	private DOType domainObjectType;
	private Object rawObject;

	public DomainObject(DOType doType) {
		this(doType, true);
	}
	
	DomainObject(DOType doType, boolean toNursery) {
		super();
		if (doType == null)
			throw new RuntimeException("a domain object must be constructed with a domain object type");
		this.domainObjectType = doType;
		if (toNursery)
			this.domainObjectType.getDomainModel().addNurseryObject(getRawObject(), this);
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
		Object val = value;
		if (value instanceof DomainObject)
			val = ((DomainObject)value).getRawObject();
		field.setValue(this.getRawObject(), val);
	}
	
	/**
	 * Add a value to a list or array field
	 * @param fieldName
	 * @param value
	 */
	public void addFieldValue(String fieldName, Object value) {
		Object val = value;
		if (value instanceof DomainObject)
			val = ((DomainObject)value).getRawObject();
		Object lst = getFieldValue(fieldName);
		
		DOField fld = getDomainObjectType().getFieldByName(fieldName);
		String ctn = fld.getComponentTypeName();
		Class<?> clazz;
		try {
			clazz = getDomainObjectType().getDomainModel().getClassForName(ctn);
			if (!clazz.isAssignableFrom(val.getClass()))
				throw new RuntimeException("value must be of type or subtype of: [" + clazz.getName() + "]");
			lst = tryInitListOrArray(lst, fld, clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		if (lst instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>)lst;
			list.add(val);
		} else if (lst != null && lst.getClass().isArray()) {
			int len = Array.getLength(lst);
			Object array = Array.newInstance(clazz, len + 1);
			for (int i = 0; i < len; i++) {
				Array.set(array, i, Array.get(lst, i));
			}
			Array.set(array, len, val);
		} else {
			if (!getDomainObjectType().getFieldByName(fieldName).isListOrArray())
				throw new RuntimeException("field: [" + fieldName + "] is neither list nor array");
			if (lst == null)
				throw new RuntimeException("field: [" + fieldName + "] has not been initialized as list or array");
		}
	}
	
	/**
	 * Add a value to a list or array field at the given index.
	 * <br/>Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
	 * @param fieldName
	 * @param index
	 * @param value
	 */
	public void addFieldValue(String fieldName, int index, Object value) {
		Object val = value;
		if (value instanceof DomainObject)
			val = ((DomainObject)value).getRawObject();
		Object lst = getFieldValue(fieldName);
		
		DOField fld = getDomainObjectType().getFieldByName(fieldName);
		String ctn = fld.getComponentTypeName();
		Class<?> clazz;
		try {
			clazz = getDomainObjectType().getDomainModel().getClassForName(ctn);
			if (!clazz.isAssignableFrom(val.getClass()))
				throw new RuntimeException("value must be of type or subtype of: [" + clazz.getName() + "]");
			lst = tryInitListOrArray(lst, fld, clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		if (lst instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>)lst;
			list.add(index, val);
		} else if (lst != null && lst.getClass().isArray()) {
			int len = Array.getLength(lst);
			Object array = Array.newInstance(clazz, len + 1);
			for (int i = 0; i < index; i++) {
				Array.set(array, i, Array.get(lst, i));
			}
			Array.set(array, index, val);
			for (int i = index + 1; i < len + 1; i++) {
				Array.set(array, i, Array.get(lst, i - 1));
			}
		} else {
			if (!getDomainObjectType().getFieldByName(fieldName).isListOrArray())
				throw new RuntimeException("field: [" + fieldName + "] is neither list nor array");
			if (lst == null)
				throw new RuntimeException("field: [" + fieldName + "] has not been initialized as list or array");
		}
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
		} else {
			if (!getDomainObjectType().getFieldByName(fieldName).isListOrArray())
				throw new RuntimeException("field: [" + fieldName + "] is neither list nor array");
			if (val == null)
				throw new RuntimeException("list or array field: [" + fieldName + "] is null");
		}
		return ret;
	}
	
	private Object tryInitListOrArray(Object list, DOField fld, Class<?> componentType) throws ClassNotFoundException {
		Object ret = list;
		if (list == null) { // try to initialize list or array field with empty list / array
			if (fld.isBuidInType()) {
				Class<?> lstClazz = getDomainObjectType().getDomainModel().getClassForName(fld.getTypeName());
				if (List.class.isAssignableFrom(lstClazz))
					ret = new ArrayList<Object>();
				else if (lstClazz.isArray())
					ret = Array.newInstance(componentType, 0);
				if (ret != null)
					fld.setValue(this.getRawObject(), ret);
			}
		}
		return ret;
	}
	
	private DomainObject getForRawObject(Object raw) {
		DomainObject gdo = null;
		if (raw != null) {
			DomainAccess da = InternalAccess.getDomainAccess(this.domainObjectType.getDomainModel());
			gdo = ((IIntDomainAccess)da).getInternalDomainAccess().getGenericDomainObject(raw);
			if (gdo == null)
				gdo = this.domainObjectType.getDomainModel().getNurseryObject(raw);
		}
		return gdo;
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
