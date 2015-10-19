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

package iot.jcypher.domain;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides general informations about domains.
 *
 */
public class DomainInformation {
	
	private static final String DomainInfoNodeLabel = "DomainInfo";
	private static final String DomainInfoNameProperty = "name";
	private static final String DomainInfoLabel2ClassProperty = "label2ClassMap";
	
	private IDBAccess dbAccess;
	private String domainName;
	private Long infoNodeId;
	private IDomainAccess domainAccess;

	private DomainInformation(IDBAccess dbAccess, String domainName) {
		super();
		this.dbAccess = dbAccess;
		this.domainName = domainName;
	}

	/**
	 * answer the names of available domains.
	 * @param dbAccess
	 * @return a list of names of available domains
	 * in the graph database accessed through dbAccess.
	 */
	public static List<String> availableDomains(IDBAccess dbAccess) {
		List<GrNode> resultList = loadAllDomainInfoNodes(dbAccess);
		List<String> domains = new ArrayList<String>();
		for (GrNode rNode : resultList) {
			domains.add(rNode.getProperty(DomainInfoNameProperty).getValue().toString());
		}
		return domains;
	}
	
	/**
	 * create a DomainInformation object for a domain with name 'domainName'
	 * stored in a graphdatabase accessed via 'dbAccess'
	 * @param dbAccess
	 * @param domainName
	 * @return a DomainInformation object
	 */
	public static DomainInformation forDomain(IDBAccess dbAccess, String domainName) {
		return new DomainInformation(dbAccess, domainName);
	}
	
	/**
	 * answer a list of DomainObjectTypes stored in the domain graph
	 * @return a list of DomainObjectTypes
	 */
	public List<DomainObjectType> getDomainObjectTypes() {
		List<DomainObjectType> resultList = new ArrayList<DomainObjectType>();
		GrNode infoNode = loadDomainInfoNode();
		GrProperty prop = infoNode.getProperty(DomainInfoLabel2ClassProperty);
		if (prop != null) {
			@SuppressWarnings("unchecked")
			List<String> val = (List<String>) prop.getValue();
			for (String str : val) {
				String[] c2l = str.split("=");
				resultList.add(new DomainObjectType(c2l[1], c2l[0]));
			}
		}
		return resultList;
	}
	
	/**
	 * answer a list of names of DomainObjectTypes stored in the domain graph
	 * @return a list of DomainObjectTypes
	 */
	public List<String> getDomainObjectTypeNames() {
		List<DomainObjectType> types = getDomainObjectTypes();
		List<String> typeNames = new ArrayList<String>(types.size());
		for (DomainObjectType typ : types) {
			typeNames.add(typ.getTypeName());
		}
		return typeNames;
	}
	
	/**
	 * answer the raw types (Java classes) of the list of DomainObjectTypes.
	 * <br/>Note: this may raise a ClassNotFoundException
	 * @param types list of DomainObjectTypes
	 * @return list of raw types (Java classes)
	 */
	public List<Class<?>> getRawTypes(List<DomainObjectType> types) {
		List<Class<?>> resultList = new ArrayList<Class<?>>();
		for (DomainObjectType type : types) {
			resultList.add(type.getType());
		}
		return resultList;
	}
	
	/**
	 * answer a domain access object (IDomainAccess) to access (store, retrieve domain objects) this domain
	 * @return a domain access object (IDomainAccess)
	 */
	public IDomainAccess getDomainAccess() {
		if (this.domainAccess == null) {
			this.domainAccess = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		}
		return this.domainAccess;
	}
	
	/**
	 * answer a domain access object (IGenericDomainAccess) to access (store, retrieve domain objects) this domain.
	 * The returned domain access object works with a generic domain model.
	 * @return a domain access object (IDomainAccess)
	 */
	public IGenericDomainAccess getGenericDomainAccess() {
		return getDomainAccess().getGenericDomainAccess();
	}
	
	private GrNode loadDomainInfoNode() {
		GrNode infoNode = null;
		if (this.infoNodeId == null) {
			List<GrNode> resultList = loadAllDomainInfoNodes(dbAccess);
			for (GrNode rNode : resultList) {
				if (domainName.equals(rNode.getProperty(DomainInfoNameProperty).getValue().toString())) {
					infoNode = rNode;
					infoNodeId = new Long(rNode.getId());
				}
			}
		} else {
			JcNode n = new JcNode("n");
			JcQuery query = new JcQuery();
			query.setClauses(new IClause[] {
					START.node(n).byId(infoNodeId.longValue()),
					RETURN.value(n)
			});
			JcQueryResult result = dbAccess.execute(query);
			List<JcError> errors = Util.collectErrors(result);
			if (errors.size() > 0) {
				throw new JcResultException(errors);
			}
			List<GrNode> resultList = result.resultOf(n);
			// there must be exactly one node
			infoNode = resultList.get(0);
		}
		return infoNode;
	}
	
	private static List<GrNode> loadAllDomainInfoNodes (IDBAccess dbAccess) {
		JcNode n = new JcNode("n");
		JcQuery query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(n).label(DomainInfoNodeLabel),
				RETURN.value(n)
		});
		JcQueryResult result = dbAccess.execute(query);
		List<JcError> errors = Util.collectErrors(result);
		if (errors.size() > 0) {
			throw new JcResultException(errors);
		}
		return result.resultOf(n);
	}
	
	/*******************************************/
	public class DomainObjectType {
		private String typeName;
		private String nodeLabel;
		private Class<?> type;
		
		private DomainObjectType(String typeName, String nodeLabel) {
			super();
			this.typeName = typeName;
			this.nodeLabel = nodeLabel;
		}

		/**
		 * Answer the fully qualified name of the java type.
		 * @return the fully qualified name of the java type.
		 */
		public String getTypeName() {
			return typeName;
		}

		/**
		 * Answer label of nodes to which domain objects of that type are mapped.
		 * @return a node label
		 */
		public String getNodeLabel() {
			return nodeLabel;
		}

		/**
		 * Answer the java type (Class).
		 * Note: this may raise a ClassNotFoundException
		 * @return the java type
		 */
		public Class<?> getType() {
			if (this.type == null) {
				try {
					this.type = Class.forName(this.typeName);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			return type;
		}

		@Override
		public String toString() {
			return "DomainObjectType [typeName=" + typeName + "]";
		}
	}
}
