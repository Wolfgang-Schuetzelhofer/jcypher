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

package iot.jcypher.domainquery.api;

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.domainquery.internal.QueryExecutor.MappingInfo;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.ValueAccess;


public class DomainObjectMatch<T> {

	private static final String nodePrefix = "n_";
	private static final String separator = "_";
	
	private Class<T> domainObjectType;
	private String baseNodeName;
	private List<JcNode> nodes;
	private List<Class<?>> typeList;
	private MappingInfo mappingInfo;
	
	DomainObjectMatch(Class<T> targetType, int num,
			MappingInfo mappingInfo) {
		super();
		this.domainObjectType = targetType;
		this.mappingInfo = mappingInfo;
		init(num);
	}
	
	private void init(int num) {
		this.baseNodeName = nodePrefix.concat(String.valueOf(num));
		this.typeList = this.mappingInfo.getCompoundTypesFor(this.domainObjectType);
		this.nodes = new ArrayList<JcNode>(this.typeList.size());
		for (int i = 0; i < this.typeList.size(); i++) {
			this.nodes.add(new JcNode(this.baseNodeName.concat(separator).concat(String.valueOf(i))));
		}
	}
	
	/**
	 * Access a string attribute
	 * @param name the attribute name
	 * @return a JcString
	 */
	public JcString stringAtttribute(String name) {
		JcString ret = null;
		List<JcNode> validFor = new ArrayList<JcNode>();
		for (int i = 0; i < this.typeList.size(); i++) {
			String propName = this.mappingInfo.attribute2Property(name, this.typeList.get(i));
			if (propName != null) {
				validFor.add(this.nodes.get(i));
				if (ret == null) {
					ret = this.nodes.get(i).stringProperty(propName);
					ValueAccess.setHint(ret, validFor);
				}
			}
		}
		if (ret == null)
			throw new RuntimeException("attribute: [" + name + "] does not exist");
		return ret;
	}

	Class<T> getDomainObjectType() {
		return domainObjectType;
	}

	List<JcNode> getNodes() {
		return nodes;
	}

	List<Class<?>> getTypeList() {
		return typeList;
	}

	MappingInfo getMappingInfo() {
		return mappingInfo;
	}

	String getBaseNodeName() {
		return baseNodeName;
	}
	
}
