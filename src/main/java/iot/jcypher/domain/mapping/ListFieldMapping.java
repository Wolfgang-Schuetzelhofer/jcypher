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

import java.util.Arrays;
import java.util.Collection;

public class ListFieldMapping extends FieldMapping {

	private static final String TypePostfix = "Type";
	
	public ListFieldMapping(IField field, String propertyName) {
		super(field, propertyName);
	}

	public ListFieldMapping(IField field) {
		super(field);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void storeSimpleListComponentType(Object value, GrNode rNode) {
		// only called when a collection or an array is mapped to a property
		Collection coll;
		if (value instanceof Collection<?>)
			coll = (Collection) this.getFieldType().cast(value);
		else
			coll = Arrays.asList((Object[])value);
		if (coll.size() > 0) {
			Object elem = coll.iterator().next();
			Class<?> type = elem.getClass();
			// test the first element,
			// assuming all elements are of the same type !!!
			String propName = getPropertyOrRelationName().concat(TypePostfix);
			GrProperty prop = rNode.getProperty(propName);
			String t_value = type.getName();
			if (prop != null) {
				Object propValue = prop.getValue(); // String need not be converted
				if (!t_value.equals(propValue)) {
					prop.setValue(t_value);
				}
			} else
				rNode.addProperty(propName, t_value);
		}
	}

	@Override
	protected void clearAdditionalProperties(GrNode rNode) {
		String propName = getPropertyOrRelationName().concat(TypePostfix);
		GrProperty prop = rNode.getProperty(propName);
		if (prop != null)
			prop.setValue(null);
	}

	@Override
	protected Class<?> getComponentType(GrNode rNode) {
		String propName = getPropertyOrRelationName().concat(TypePostfix);
		Class<?> clazz = getTypeFromProperty(rNode, propName);
		return clazz;
	}

}
