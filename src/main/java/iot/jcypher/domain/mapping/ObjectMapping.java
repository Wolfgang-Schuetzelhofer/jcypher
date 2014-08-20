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
import java.util.List;

import iot.jcypher.graph.GrNode;

public class ObjectMapping {

	private List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
	private NodeLabelMapping nodeLabelMapping;
	
	public void mapFromObject(Object domainObject, GrNode rNode) {
		if (this.nodeLabelMapping != null)
			this.nodeLabelMapping.mapLabels(domainObject, rNode);
			
		for (FieldMapping fm : getFieldMappings()) {
			fm.mapFromField(domainObject, rNode);
		}
	}
	
	public void mapToObject(Object domainObject, GrNode rNode) {
		for (FieldMapping fm : getFieldMappings()) {
			fm.mapToField(domainObject, rNode);
		}
	}

	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public void setNodeLabelMapping(NodeLabelMapping nodeLabelMapping) {
		this.nodeLabelMapping = nodeLabelMapping;
	}
}
