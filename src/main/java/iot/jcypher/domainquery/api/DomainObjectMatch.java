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

package iot.jcypher.domainquery.api;

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.MappingUtil;
import iot.jcypher.domainquery.internal.QueryExecutor.MappingInfo;
import iot.jcypher.query.values.JcBoolean;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcProperty;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueElement;


public class DomainObjectMatch<T> implements IPredicateOperand1 {

	private static final String separator = "_";
	private static final String msg_1 = "attributes used in WHERE clauses must be of simple type." +
									" Not true for attribute [";
	
	private DomainObjectMatch<?> traversalSource;
	
	// a collectExpressionOwner is the DomaiObjectMatch which
	// is produced by a collection expression
	private List<DomainObjectMatch<?>> collectExpressionOwner;
	private Class<T> domainObjectType;
	private String baseNodeName;
	private List<JcNode> nodes;
	private List<Class<?>> typeList;
	private MappingInfo mappingInfo;
	private int pageOffset;
	private int pageLength;
	private boolean pageChanged;
	
	DomainObjectMatch(Class<T> targetType, int num,
			MappingInfo mappingInfo) {
		super();
		this.domainObjectType = targetType;
		this.mappingInfo = mappingInfo;
		this.pageLength = -1;
		this.pageOffset = 0;
		this.pageChanged = false;
		init(num);
	}
	
	private void init(int num) {
		this.baseNodeName =APIAccess.nodePrefix.concat(String.valueOf(num));
		this.typeList = this.mappingInfo.getCompoundTypesFor(this.domainObjectType);
		this.nodes = new ArrayList<JcNode>(this.typeList.size());
		for (int i = 0; i < this.typeList.size(); i++) {
			this.nodes.add(new JcNode(this.baseNodeName.concat(separator).concat(String.valueOf(i))));
		}
	}
	
	/**
	 * <b>Note:</b> this expression is only valid within a collection expression.
	 * <br/>It constrains a set by applying the COUNT expression on another set which must be directly derived via a traversal expression. 
	 * <br/>'addresses' below has been directly derived from 'persons' via traversal.
	 * <br/>e.g. q.SELECT_FROM(persons).ELEMENTS( q.WHERE(addresses.COUNT()).EQUALS(3));
	 */
	public Count COUNT() {
		return APIAccess.createCount(this);
	}
	
	/**
	 * Access an attribute, don't rely on a specific attribute type
	 * @param name the attribute name
	 * @return
	 */
	public JcProperty atttribute(String name) {
		return checkField_getJcVal(name, JcProperty.class);
	}
	
	/**
	 * Access a string attribute
	 * @param name the attribute name
	 * @return a JcString
	 */
	public JcString stringAtttribute(String name) {
		return checkField_getJcVal(name, JcString.class);
	}
	
	/**
	 * Access a number attribute
	 * @param name the attribute name
	 * @return a JcNumber
	 */
	public JcNumber numberAtttribute(String name) {
		return checkField_getJcVal(name, JcNumber.class);
	}
	
	/**
	 * Access a boolean attribute
	 * @param name the attribute name
	 * @return a JcBoolean
	 */
	public JcBoolean booleanAtttribute(String name) {
		return checkField_getJcVal(name, JcBoolean.class);
	}
	
	/**
	 * Access a collection attribute
	 * @param name the attribute name
	 * @return a JcCollection
	 */
	public JcCollection collectionAtttribute(String name) {
		return checkField_getJcVal(name, JcCollection.class);
	}
	
	/**
	 * For pagination support, set offset (start) and length of the set of matching objects to be
	 * returned with respect to the total number of matching objects.
	 * @param offset
	 * @param length
	 */
	public void setPage(int offset, int length) {
		boolean changed = this.pageOffset != offset || this.pageLength != length;
		if (changed) {
			this.pageOffset = offset;
			this.pageLength = length;
			this.pageChanged = true;
		}
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
	
	boolean isPageChanged() {
		return pageChanged;
	}

	void setPageChanged(boolean pageChanged) {
		this.pageChanged = pageChanged;
	}

	int getPageOffset() {
		return pageOffset;
	}

	int getPageLength() {
		return pageLength;
	}
	
	Class<?> getTypeForNodeName(String nodeName) {
		int idx = -1;
		for (int i = 0; i < this.nodes.size(); i++) {
			if (ValueAccess.getName(this.nodes.get(i)).equals(nodeName)) {
				idx = i;
				break;
			}
		}
		if (idx != -1)
			return this.typeList.get(idx);
		return null;
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
	
	DomainObjectMatch<?> getTraversalSource() {
		return traversalSource;
	}

	void setTraversalSource(DomainObjectMatch<?> traversalSource) {
		this.traversalSource = traversalSource;
	}

	List<DomainObjectMatch<?>> getCollectExpressionOwner() {
		return collectExpressionOwner;
	}
	
	void addCollectExpressionOwner(DomainObjectMatch<?> dom) {
		if (this.collectExpressionOwner == null)
			this.collectExpressionOwner = new ArrayList<DomainObjectMatch<?>>();
		if (!this.collectExpressionOwner.contains(dom))
			this.collectExpressionOwner.add(dom);
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
	
	@SuppressWarnings("unchecked")
	private <E> E checkField_getJcVal(String name, Class<E> attributeType) {
		E ret = null;
		List<JcNode> validFor = new ArrayList<JcNode>();
		for (int i = 0; i < this.typeList.size(); i++) {
			FieldMapping fm = this.mappingInfo.getFieldMapping(name, typeList.get(i));
			if (fm != null) {
				if (needsRelation(fm))
					throw new RuntimeException(msg_1 + name + "] " +
							"in domain object type: [" + domainObjectType.getName() + "]");
				validFor.add(this.nodes.get(i));
				if (ret == null) {
					String propName = this.getPropertyOrRelationName(fm);
					JcNode n = this.nodes.get(i);
					if (attributeType.equals(JcProperty.class))
						ret = (E) n.property(propName);
					else if (attributeType.equals(JcString.class))
						ret = (E) n.stringProperty(propName);
					else if (attributeType.equals(JcBoolean.class))
						ret = (E) n.booleanProperty(propName);
					else if (attributeType.equals(JcNumber.class))
						ret = (E) n.numberProperty(propName);
					else if (attributeType.equals(JcCollection.class))
						ret = (E) n.collectionProperty(propName);
					ValueAccess.setHint((ValueElement)ret, APIAccess.hintKey_validNodes, validFor);
					ValueAccess.setHint((ValueElement)ret, APIAccess.hintKey_dom, this);
				}
			}
		}
		if (ret == null)
			throw new RuntimeException("attribute: [" + name + "] does not exist " +
							"in domain object type: [" + domainObjectType.getName() + "]");
		return ret;
	}
}
