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
			if (value instanceof Date) {
				value = MappingUtil.dateToString((Date) value);
			}
			GrProperty prop = rNode.getProperty(this.propertyName);
			if (prop != null) {
				if (!prop.getValue().equals(value)) {
					prop.remove();
					prop = null;
				}
			}
			if (prop == null)
				rNode.addProperty(this.propertyName, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void mapToField(Object domainObject, GrNode rNode) {
		try {
			prepare(domainObject);
			
			Object value = this.field.get(domainObject);
			GrProperty prop = rNode.getProperty(this.propertyName);
			if (prop != null) {
				Object propValue = prop.getValue();
				if (Date.class.isAssignableFrom(this.field.getType())) {
					propValue = MappingUtil.stringToDate(propValue.toString());
				}
				if (!propValue.equals(value)) {
					this.field.set(domainObject, propValue);
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
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
