/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.domain.genericmodel;

import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainModel {
	
	private static final String PackagePrefix = "i_o_t";
	private static final String TypeNodePostfix = "_mdl";
	private static final String typeName = "typeName";
	private static final String superTypeName = "superTypeName";
	private static final String fields = "fields";
	
	private String domainName;
	private String typeNodeName;
	private Map<String, DOType> doTypes;
	private List<DOType> unsaved;

	public DomainModel(String domainName) {
		super();
		this.domainName = domainName;
		this.typeNodeName = domainName.concat(TypeNodePostfix);
		this.doTypes = new HashMap<String, DOType>();
		this.unsaved = new ArrayList<DOType>();
	}

	public boolean needCreateModel(Class<?> clazz) {
		String name = clazz.getName();
		if (!name.startsWith(PackagePrefix)) {
			return this.doTypes.get(name) == null;
		}
		return false;
	}
	
	public void addDOType(DOType doType) {
		this.doTypes.put(doType.getName(), doType);
		if (doType.getNodeId() == -1) {
			this.unsaved.add(doType);
		}
	}
	
	public DOType getDOType(String typeName) {
		return this.doTypes.get(typeName);
	}
	
	public String getDomainName() {
		return domainName;
	}

	public String getTypeNodeName() {
		return this.typeNodeName;
	}
	
	public void loadFrom(List<GrNode> mdlInfos) {
		for(GrNode nd : mdlInfos) {
			if (nd != null) {
				GrProperty typ = nd.getProperty(typeName);
				GrProperty superTyp = nd.getProperty(superTypeName);
				GrProperty flds = nd.getProperty(fields);
			}
		}
	}
	
	public boolean hasChanges() {
		return this.unsaved.size() > 0;
	}
}
