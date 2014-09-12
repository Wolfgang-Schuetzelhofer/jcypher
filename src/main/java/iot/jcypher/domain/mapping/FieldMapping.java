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

package iot.jcypher.domain.mapping;

import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

public class FieldMapping {

	private Field field;
	private String fieldName;
	private String propertyName;
	private String classFieldName;
	
	public FieldMapping(Field field) {
		this(field, field.getName());
	}
	
	public FieldMapping(Field field, String propertyName) {
		super();
		this.field = field;
		this.field.setAccessible(true);
		this.propertyName = propertyName;
	}

	public void mapPropertyFromField(Object domainObject, GrNode rNode) {
		try {
			prepare(domainObject);
			
			if (getObjectNeedingRelation(domainObject) == null) { // also checks against DomainInfo
				// we can map to a property
				Object value = this.field.get(domainObject);
				value = MappingUtil.convertToProperty(value);
				GrProperty prop = rNode.getProperty(this.propertyName);
				if (value != null) {
					if (prop != null) {
						Object propValue = MappingUtil.convertFromProperty(prop.getValue(), value.getClass(),
								getListComponentType());
						if (!propValue.equals(value)) {
							prop.setValue(value);
						}
					} else
						rNode.addProperty(this.propertyName, value);
				} else {
					if (prop != null)
						prop.setValue(null);
				}
			}
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void mapPropertyToField(Object domainObject, GrNode rNode) {
		try {
			prepare(domainObject);
			
			Object value = this.field.get(domainObject);
			GrProperty prop = rNode.getProperty(this.propertyName);
			if (prop != null) {
				Object propValue = prop.getValue();
				Class<?> typ = this.field.getType();
				propValue = MappingUtil.convertFromProperty(propValue, typ,
						getListComponentType());
				if (!propValue.equals(value)) {
					this.field.set(domainObject, propValue);
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setField(Object domainObject, Object value) {
		try {
			prepare(domainObject);
			this.field.set(domainObject, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * @return the value of the field, if this value cannot be mapped to a property,
	 * but must be mapped to a seperate node connected via a relation, else return null.
	 */
	@SuppressWarnings("rawtypes")
	public Object getObjectNeedingRelation(Object domainObject) {
		Object value = null;
		try {
			prepare(domainObject);
			if (needsRelation()) { // also checks against DominInfo
				value = this.field.get(domainObject);
				// check for list (collection) containing primitive or simple types
				if (value != null && Collection.class.isAssignableFrom(this.field.getType())) {
					Collection coll = (Collection) this.field.getType().cast(value);
					if (coll.size() > 0) {
						Object elem = coll.iterator().next();
						// test the first element,
						// assuming all elements are of the same type !!!
						Class<?> type = elem.getClass();
						if (MappingUtil.isSimpleType(type)) { // elements are of primitive or simple type
							// store that info in DomainInfo
							String classField = getClassFieldName();
							MappingUtil.internalDomainAccess.get()
								.addFieldComponentType(classField, type);
							// to return null
							value = null;
						}
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return value;
	}
	
	/**
	 * 
	 * @return true, if this field cannot be mapped to a property,
	 * but must be mapped to a seperate node connected via a relation, else return false.
	 */
	public boolean needsRelation() {
		boolean ret = !MappingUtil.mapsToProperty(this.field.getGenericType());
		if (ret) { // check for list (collection) containing primitive or simple types
						// in DomainInfo
			if (Collection.class.isAssignableFrom(this.field.getType())) {
				String classField = getClassFieldName();
				Class<?> cType = MappingUtil.internalDomainAccess.get()
					.getFieldComponentType(classField);
				// if cType == null, false will be returned
				ret = !MappingUtil.mapsToProperty(cType);
			}
		}
		return ret;
	}
	
	private Class<?> getListComponentType() {
		Type typ = MappingUtil.getListComponentType(this.field.getGenericType());
		if (typ == null) { // check for list (collection) containing primitive or simple types
										// in DomainInfo
			if (Collection.class.isAssignableFrom(this.field.getType())) {
				String classField = getClassFieldName();
				Class<?> cType = MappingUtil.internalDomainAccess.get()
					.getFieldComponentType(classField);
				return cType;
			}
		} else if (typ instanceof Class<?>)
			return (Class<?>)typ;
		return null;
	}
	
	public String getPropertyOrRelationName() {
		return this.propertyName;
	}
	
	public Class<?> getFieldType () {
		return this.field.getType();
	}
	
	public String getFieldName() {
		if (this.fieldName == null)
			this.fieldName = this.field.getName();
		return this.fieldName;
	}

	private void prepare(Object domainObject) throws NoSuchFieldException, SecurityException {
		if (this.field == null) {
			this.field = domainObject.getClass().getDeclaredField(this.fieldName);
			this.field.setAccessible(true);
		}
		
		if (this.fieldName == null)
			this.fieldName = this.field.getName();
		
	}
	
	private String getClassFieldName() {
		if (this.classFieldName == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(this.field.getDeclaringClass().getName());
			sb.append('_');
			sb.append(this.field.getName());
			this.classFieldName = sb.toString();
		}
		return this.classFieldName;
	}
}
