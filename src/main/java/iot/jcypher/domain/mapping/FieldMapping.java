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

import iot.jcypher.domain.mapping.CompoundObjectType.CType;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;

import java.util.Collection;
import java.util.Map;

public class FieldMapping {

	private IField field;
	private String fieldName;
	protected String propertyName;
	private String classFieldName;
	
	public FieldMapping(IField field) {
		this(field, field.getName());
	}
	
	public FieldMapping(IField field, String propertyName) {
		super();
		this.field = field;
		this.field.setAccessible(true);
		this.propertyName = propertyName;
	}
	
	public void mapPropertyFromField(Object domainObject, GrNode rNode) {
		intMapPropertyFromField(domainObject, rNode);
	}

	protected Object intMapPropertyFromField(Object domainObject, GrNode rNode) {
		Object ret = null;
		try {
			prepare(domainObject);
			
			if (getObjectNeedingRelation(domainObject) == null) { // also checks against DomainInfo
				// we can map to a property
				Object value = this.field.get(domainObject);
				ret = value;
				value = MappingUtil.convertToProperty(value);
				GrProperty prop = rNode.getProperty(this.propertyName);
				if (value != null) {
					if (prop != null) {
						Object propValue = MappingUtil.convertFromProperty(prop.getValue(), value.getClass(),
								getComponentType(), getConcreteFieldType());
						if (!propValue.equals(value)) {
							addSimpleListComponentType2DomainInfo(value);
							prop.setValue(value);
						}
					} else
						rNode.addProperty(this.propertyName, value);
				} else {
					if (prop != null) // remove the property
						prop.setValue(null);
				}
			} else {
				// a previously empty collection might have been mapped to a property
				// we need to remove the property
				if (Collection.class.isAssignableFrom(getFieldType())) {
					GrProperty prop = rNode.getProperty(this.propertyName);
					if (prop != null)
						prop.setValue(null);
				}
				
				// a previously empty map might have been mapped to a property
				// we need to remove the property
				if (Map.class.isAssignableFrom(getFieldType())) {
					GrProperty prop = rNode.getProperty(this.propertyName);
					if (prop != null)
						prop.setValue(null);
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return ret;
	}
	
	/**
	 * @param domainObject
	 * @param rNode
	 * @return true if the property exists in the node
	 */
	public boolean mapPropertyToField(Object domainObject, GrNode rNode) {
		try {
			prepare(domainObject);
			
			Object value = this.field.get(domainObject);
			GrProperty prop = rNode.getProperty(this.propertyName);
			boolean hasProperty = false;
			if (prop != null) {
				hasProperty = true;
				Object propValue = prop.getValue();
				if (propValue != null) { // allow null values in properties
					Class<?> typ = getFieldTypeInt(rNode);
					propValue = MappingUtil.convertFromProperty(propValue, typ,
							getComponentType(), getConcreteFieldType());
					if (!propValue.equals(value)) {
						this.field.set(domainObject, propValue);
					}
				}
			}
			return hasProperty;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setFieldValue(Object domainObject, Object value) {
		try {
			prepare(domainObject);
			this.field.set(domainObject, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public Object getFieldValue(Object domainObject) {
		try {
			prepare(domainObject);
			return this.field.get(domainObject);
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
			value = this.field.get(domainObject);
			if (value != null) {
				if (MappingUtil.isSimpleType(value.getClass())) { // value is of primitive or simple type
					value = null;
				} else {
					// check for list (collection) containing primitive or simple types
					if (Collection.class.isAssignableFrom(this.field.getType())) {
						Collection coll = (Collection) this.field.getType().cast(value);
						if (coll.size() > 0) {
							Object elem = coll.iterator().next();
							Class<?> type = elem.getClass();
							// test the first element,
							// assuming all elements are of the same type !!!
							if (MappingUtil.isSimpleType(type)) { // elements are of primitive or simple type
								// to return null
								value = null;
							}
						} else { // empty lists are mapped to a property
							value = null;
						}
					}
					
					if (Map.class.isAssignableFrom(this.field.getType())) {
						Map map = (Map) this.field.getType().cast(value);
						if (map.isEmpty()) // empty maps are mapped to a property
							value = null;
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return value;
	}
	
	/**
	 * @return true if the field type is Object.class or a map or list, because at runtime this
	 * can lead to a simple type (e.g. Integer), or to an empty or simple type array which can be mapped to a property,
	 * or it can lead to a complex type which requires a relation in the graph.
	 * It is therefore necessary to look at properties and relations.
	 */
	public boolean needsRelationOrProperty() {
		return this.field.getType().equals(Object.class) ||
				this.getFieldKind() == FieldKind.MAP ||
				this.getFieldKind() == FieldKind.COLLECTION;
	}
	
	/**
	 * 
	 * @return true, if this field cannot be mapped to a property,
	 * but must be mapped to a seperate node connected via a relation, else return false.
	 */
	public boolean needsRelation() {
		boolean needRelation = !MappingUtil.mapsToProperty(this.field.getType());
		if (needRelation) { // check DomainInfo
			String classField = getClassFieldName();
			CompoundObjectType cType = MappingUtil.internalDomainAccess.get()
					.getConcreteFieldType(classField);
			if (cType != null && cType.getCType() == CType.SIMPLE)
				return false;
			else {
				// check for list (collection) containing primitive or simple types
							// in DomainInfo
				if (Collection.class.isAssignableFrom(this.field.getType())) {
					cType = MappingUtil.internalDomainAccess.get()
						.getFieldComponentType(classField);
					// if cType == null, false will be returned
					if (cType != null) {
						needRelation = cType.getCType() != CType.SIMPLE;
					} else
						needRelation = true; // cannot determine if the component type is simple
										   // so return true and leave the decision for later,
										   // when a concrete component is available
				}
			}
		}
		return needRelation;
	}
	
	@SuppressWarnings("rawtypes")
	private void addSimpleListComponentType2DomainInfo(Object value) {
		// check for list (collection) containing primitive or simple types
		if (Collection.class.isAssignableFrom(this.field.getType())) {
			Collection coll = (Collection) this.field.getType().cast(value);
			if (coll.size() > 0) {
				Object elem = coll.iterator().next();
				Class<?> type = elem.getClass();
				// test the first element,
				// assuming all elements are of the same type !!!
				if (MappingUtil.isSimpleType(type)) { // elements are of primitive or simple type
					// store component type in DomainInfo
					MappingUtil.internalDomainAccess.get()
						.addFieldComponentType(getClassFieldName(), elem.getClass());
				}
			}
		}
	}
	
	/**
	 * only called when to check for a concrete simple component type
	 * @return
	 */
	private Class<?> getComponentType() {
		if (getFieldKind() == FieldKind.COLLECTION) {
			String classField = getClassFieldName();
			CompoundObjectType cType = MappingUtil.internalDomainAccess.get()
				.getFieldComponentType(classField);
			if (cType != null)
				return cType.getType();
		}
		return null;
	}
	
	private Class<?> getConcreteFieldType() {
		if (getFieldKind() == FieldKind.MAP) {
			String classField = getClassFieldName();
			CompoundObjectType cType = MappingUtil.internalDomainAccess.get()
				.getConcreteFieldType(classField);
			if (cType != null)
				return cType.getType();
		}
		return null;
	}
	
	public String getPropertyOrRelationName() {
		return this.propertyName;
	}
	
	public IField getField() {
		return this.field;
	}
	
	public Class<?> getFieldType () {
		return this.field.getType();
	}
	
	protected Class<?> getFieldTypeInt (GrNode rNode) {
		return this.field.getType();
	}
	
	public String getFieldName() {
		if (this.fieldName == null)
			this.fieldName = this.field.getName();
		return this.fieldName;
	}

	private void prepare(Object domainObject) throws NoSuchFieldException, SecurityException {
		if (this.fieldName == null)
			this.fieldName = this.field.getName();
	}
	
	public String getClassFieldName() {
		if (this.classFieldName == null) {
			this.classFieldName = createClassFieldName();
		}
		return this.classFieldName;
	}
	
	private String createClassFieldName() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.field.getDeclaringClass().getName());
		sb.append('_');
		sb.append(this.getFieldName());
		return sb.toString();
	}

	public FieldKind getFieldKind() {
		Class<?> typ = this.field.getType();
		return getFieldKind(typ);
	}
	
	public static FieldKind getFieldKind(Class<?> typ) {
		return Collection.class.isAssignableFrom(typ) ? FieldKind.COLLECTION :
			Map.class.isAssignableFrom(typ) ? FieldKind.MAP : FieldKind.SINGLE;
	}
	
	protected String getDOClassFieldName() {
		if (this.classFieldName == null) {
			this.classFieldName = createClassFieldName();
		}
		return this.classFieldName;
	}
	
	protected String getDOPropertyOrRelationName() {
		return this.propertyName;
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FieldMapping))
			return false;
		FieldMapping other = (FieldMapping) obj;
		if (field == null) {
			if (other.getField() != null)
				return false;
		} else if (!field.equals(other.getField()))
			return false;
		return true;
	}
	
	/***********************************/
	public static enum FieldKind {
		SINGLE, COLLECTION, MAP
	}
}
