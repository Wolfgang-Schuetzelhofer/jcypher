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

import java.util.Iterator;

public abstract class ObjectMapping {

	private NodeLabelMapping nodeLabelMapping;
	
	public void mapPropertiesFromObject(Object domainObject, GrNode rNode) {
		if (this.nodeLabelMapping != null)
			this.nodeLabelMapping.mapLabel(domainObject, rNode);
			
		Iterator<FieldMapping> it = fieldMappingsIterator();
		while(it.hasNext()) {
			FieldMapping fm = it.next();
			fm.mapPropertyFromField(domainObject, rNode);
		}
	}
	
	public void mapPropertiesToObject(Object domainObject, GrNode rNode) {
		Iterator<FieldMapping> it = fieldMappingsIterator();
		while(it.hasNext()) {
			FieldMapping fm = it.next();
			fm.mapPropertyToField(domainObject, rNode);
		}
	}

	public void setNodeLabelMapping(NodeLabelMapping nodeLabelMapping) {
		this.nodeLabelMapping = nodeLabelMapping;
	}

	public NodeLabelMapping getNodeLabelMapping() {
		return nodeLabelMapping;
	}
	
	public abstract Iterator<FieldMapping> fieldMappingsIterator();
	
	public boolean shouldPerformFieldMapping(FieldMapping fieldMapping) {
		return true;
	}
}
