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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleObjectMapping extends ObjectMapping {

	private List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
	
	public void addFieldMapping(FieldMapping fieldMapping) {
		this.fieldMappings.add(fieldMapping);
	}

	@Override
	public Iterator<FieldMapping> fieldMappingsIterator() {
		return this.fieldMappings.iterator();
	}

	@Override
	public FieldMapping getFieldMappingForField(String fieldName) {
		for (FieldMapping fm : this.fieldMappings) {
			if (fm.getFieldName().equals(fieldName))
				return fm;
		}
		return null;
	}
	
}
