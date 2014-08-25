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
import java.util.Date;

public class FieldMapping {

	private Field field;
	private String fieldName;
	private String propertyName;
	
	public FieldMapping(Field field) {
		this(field, field.getName());
	}
	
	public FieldMapping(Field field, String propertyName) {
		super();
		this.field = field;
		this.field.setAccessible(true);
		this.propertyName = propertyName;
	}

	public FieldMapping(String fieldName, String propertyName) {
		super();
		this.fieldName = fieldName;
		this.propertyName = propertyName;
	}
	
	public void mapFromField(Object domainObject, GrNode rNode) {
		try {
			prepare(domainObject);
			
			Object value = this.field.get(domainObject);
			Class<?> typ = this.field.getType();
			if (MappingUtil.mapsToProperty(typ)) {
				value = MappingUtil.convertToProperty(value);
				GrProperty prop = rNode.getProperty(this.propertyName);
				if (prop != null) {
					if (!prop.getValue().equals(value)) {
						prop.remove();
						prop = null;
					}
				}
				if (prop == null)
					rNode.addProperty(this.propertyName, value);
			}
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void mapToField(Object domainObject, GrNode rNode) {
		try {
			prepare(domainObject);
			
			Object value = this.field.get(domainObject);
			GrProperty prop = rNode.getProperty(this.propertyName);
			if (prop != null && !this.needsRelation()) {
				Object propValue = prop.getValue();
				Class<?> typ = this.field.getType();
				propValue = MappingUtil.convertFromProperty(propValue, typ);
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
	public Object getObjectNeedingRelation(Object domainObject) {
		try {
			prepare(domainObject);
			Class<?> typ = this.field.getType();
			if (!MappingUtil.mapsToProperty(typ)) {
				Object value = this.field.get(domainObject);
				return value;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/**
	 * 
	 * @return true, if this firled cannot be mapped to a property,
	 * but must be mapped to a seperate node connected via a relation, else return false.
	 */
	public boolean needsRelation() {
		Class<?> typ = this.field.getType();
		return !MappingUtil.mapsToProperty(typ);
	}
	
	public String getPropertyOrRelationName() {
		return this.propertyName;
	}
	
	public Class<?> getFieldType () {
		return this.field.getType();
	}

	private void prepare(Object domainObject) throws NoSuchFieldException, SecurityException {
		if (this.field == null) {
			this.field = domainObject.getClass().getDeclaredField(this.fieldName);
			this.field.setAccessible(true);
		}
		
		if (this.fieldName == null)
			this.fieldName = this.field.getName();
		
	}
}
