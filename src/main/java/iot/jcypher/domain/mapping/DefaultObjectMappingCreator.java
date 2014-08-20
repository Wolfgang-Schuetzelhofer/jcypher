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

import java.lang.reflect.Field;

public class DefaultObjectMappingCreator {

	public static ObjectMapping createObjectMapping(Class<?> toMap) {
		ObjectMapping objectMapping = new ObjectMapping();
		
		NodeLabelMapping labelMapping = DefaultObjectMappingCreator.createLabelMapping(toMap);
		objectMapping.setNodeLabelMapping(labelMapping);
		
		Field[] fields = toMap.getDeclaredFields();
		for (int i = 0;i < fields.length; i++) {
			FieldMapping fieldMapping = new FieldMapping(fields[i]);
			objectMapping.getFieldMappings().add(fieldMapping);
		}
		
		return objectMapping;
	}
	
	public static NodeLabelMapping createLabelMapping(Class<?> toMap) {
		NodeLabelMapping labelMapping = new NodeLabelMapping();
		labelMapping.getLabels().add(toMap.getSimpleName());
		return labelMapping;
	}
}
