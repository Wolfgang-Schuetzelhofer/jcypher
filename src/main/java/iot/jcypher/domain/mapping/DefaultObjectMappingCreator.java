/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

import iot.jcypher.domain.genericmodel.internal.DomainModel;
import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;
import iot.jcypher.domain.mapping.FieldMapping.FieldKind;
import iot.jcypher.domain.mapping.surrogate.Array;
import iot.jcypher.domain.mapping.surrogate.Collection;
import iot.jcypher.domain.mapping.surrogate.Map;
import iot.jcypher.domain.mapping.surrogate.MapEntry;
import iot.jcypher.domain.mapping.surrogate.SurrogateField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class DefaultObjectMappingCreator {

	private static final String ValueField = "value";
	
	public static ObjectMapping createObjectMapping(Class<?> toMap, DomainModel dm) {
		SimpleObjectMapping objectMapping = new SimpleObjectMapping();
		
		NodeLabelMapping labelMapping = DefaultObjectMappingCreator.createLabelMapping(toMap);
		objectMapping.setNodeLabelMapping(labelMapping);
		
		Class<?> clazz = toMap;
		while(!Object.class.equals(clazz)) {
			dm.addType(clazz);
			addFieldMappings(objectMapping, clazz);
			clazz = clazz.getSuperclass();
		}
		
		return objectMapping;
	}
	
	private static void addFieldMappings(SimpleObjectMapping objectMapping, Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0;i < fields.length; i++) {
			if (!Modifier.isTransient(fields[i].getModifiers())) {
				FieldMapping fieldMapping;
				IField field;
				FieldKind fieldKind = FieldMapping.getFieldKind(fields[i].getType());
				if (fieldKind == FieldKind.MAP && !fields[i].getDeclaringClass().equals(Map.class))
					field = new SurrogateField(fields[i]);
				else if (fieldKind == FieldKind.COLLECTION && !fields[i].getDeclaringClass().equals(Collection.class))
					field = new SurrogateField(fields[i]);
				else if (fieldKind == FieldKind.ARRAY && !fields[i].getDeclaringClass().equals(Array.class))
					field = new SurrogateField(fields[i]);
				else if (clazz.equals(Array.class))
					field = new ArraySurrogateField(fields[i]);
				else
					field = new DirectField(fields[i]);
				
				if (clazz.equals(MapEntry.class) && fields[i].getName().equals(ValueField))
					fieldMapping = new ValueAndTypeMapping(field);
				else if (clazz.equals(Collection.class)) // has only one field, don't need to test for field name
					fieldMapping = new ListFieldMapping(field);
				else if (clazz.equals(Array.class)) // has only one field, don't need to test for field name
					fieldMapping = new ListFieldMapping(field);
				else
					fieldMapping = new FieldMapping(field);
				objectMapping.addFieldMapping(fieldMapping);
			}
		}
	}
	
	public static NodeLabelMapping createLabelMapping(Class<?> toMap) {
		InternalDomainAccess dAccess = MappingUtil.internalDomainAccess.get();
		String label = dAccess.getLabelForClass(toMap);
		if (label == null) {
			String fullName = toMap.getName();
			StringBuilder sb = new StringBuilder();
			int idx = fullName.length() -1;
			do {
				if (sb.length() > 0 && idx >= 0) {
					sb.insert(0, '_');
					idx--;
				}
				char c;
				while(idx >= 0 && (c = fullName.charAt(idx)) != '.') {
					sb.insert(0, c);
					idx--;
				}
				label = sb.toString();
			} while(dAccess.existsLabel(label));
		}
		NodeLabelMapping labelMapping = new NodeLabelMapping(label);
		return labelMapping;
	}
}
