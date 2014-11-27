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

import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.MappingUtil;
import iot.jcypher.domainquery.internal.QueryExecutor.MappingInfo;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcProperty;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.ValueAccess;


public class DomainObjectMatch<T> implements IPredicateOperand1 {

	private static final String nodePrefix = "n_";
	private static final String separator = "_";
	private static final String msg_1 = "attributes used in WHERE clauses must be of simple type." +
									" Not true for attribute [";
	
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
	 * Access an attribute
	 * @param name the attribute name
	 * @return
	 */
	public JcProperty atttribute(String name) {
		JcProperty ret = null;
		List<JcNode> validFor = new ArrayList<JcNode>();
		for (int i = 0; i < this.typeList.size(); i++) {
			FieldMapping fm = this.mappingInfo.getFieldMapping(name, typeList.get(i));
			String propName = this.getPropertyOrRelationName(fm);
			if (propName != null) {
				if (needsRelation(fm))
					throw new RuntimeException(msg_1 + name + "] " +
							"in domain object type: [" + domainObjectType.getName() + "]");
				validFor.add(this.nodes.get(i));
				if (ret == null) {
					ret = this.nodes.get(i).property(propName);
					ValueAccess.setHint(ret, validFor);
				}
			}
		}
		if (ret == null)
			throw new RuntimeException("attribute: [" + name + "] does not exist " +
							"in domain object type: [" + domainObjectType.getName() + "]");
		return ret;
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
			FieldMapping fm = this.mappingInfo.getFieldMapping(name, typeList.get(i));
			String propName = this.getPropertyOrRelationName(fm);
			if (propName != null) {
				if (needsRelation(fm))
					throw new RuntimeException(msg_1 + name + "] " +
							"in domain object type: [" + domainObjectType.getName() + "]");
				validFor.add(this.nodes.get(i));
				if (ret == null) {
					ret = this.nodes.get(i).stringProperty(propName);
					ValueAccess.setHint(ret, validFor);
				}
			}
		}
		if (ret == null)
			throw new RuntimeException("attribute: [" + name + "] does not exist " +
							"in domain object type: [" + domainObjectType.getName() + "]");
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
	
	/**
	 * may return null
	 * @param attribName
	 * @param type
	 * @return
	 */
	private String getPropertyOrRelationName(FieldMapping fm) {
		if (fm != null)
			return fm.getPropertyOrRelationName();
		return null;
	}
	
	private boolean needsRelation(FieldMapping fm) {
		boolean ret;
		InternalDomainAccess internalAccess = null;
		try {
			internalAccess = MappingUtil.internalDomainAccess.get();
			MappingUtil.internalDomainAccess.set(this.mappingInfo.getInternalDomainAccess());
			ret = fm.needsRelation();
		} finally {
			if (internalAccess != null)
				MappingUtil.internalDomainAccess.set(internalAccess);
			else
				MappingUtil.internalDomainAccess.remove();
		}
		return ret;
	}
}
