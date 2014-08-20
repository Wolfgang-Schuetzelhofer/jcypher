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

package iot.jcypher.domain;

import iot.jcypher.JcQuery;
import iot.jcypher.JcQueryResult;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.mapping.DefaultObjectMappingCreator;
import iot.jcypher.domain.mapping.ObjectMapping;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.Graph;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.values.JcBoolean;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.result.JcError;
import iot.jcypher.result.JcResultException;
import iot.jcypher.result.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DomainConfig {
	
	private DomainConfigHandler domainConfigHandler;

	public DomainConfig(IDBAccess dbAccess) {
		super();
		this.domainConfigHandler = new DomainConfigHandler(dbAccess);
	}

	public List<JcError> store(Object domainObject) {
		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(domainObject);
		return this.store(domainObjects);
	}
	
	public List<JcError> store(List<Object> domainObjects) {
		Graph graph = this.domainConfigHandler.updateLocalGraph(domainObjects);
		List<JcError> errors = graph.store();
		return errors;
	}
	
	public <T> T loadById(Class<T> domainObjectClass, long id) {
		long[] ids = new long[] {id};
		List<T> ret = this.domainConfigHandler.loadByIds(domainObjectClass, ids);
		return ret.get(0);
	}
	
	public <T> List<T> loadByIds(Class<T> domainObjectClass, long... ids) {
		return this.domainConfigHandler.loadByIds(domainObjectClass, ids);
	}
	
	/**********************************************************************/
	private class DomainConfigHandler {
		private static final String NodePrefix = "n_";
		private IDBAccess dbAccess;
		private Map<Object, Long> objectToIdMap;
		private Map<Class<?>, ObjectMapping> mappings;

		private DomainConfigHandler(IDBAccess dbAccess) {
			super();
			this.dbAccess = dbAccess;
			this.objectToIdMap = new HashMap<Object, Long>();
			this.mappings = new HashMap<Class<?>, ObjectMapping>();
		}
		
		private <T> List<T> loadByIds(Class<T> domainObjectClass, long... ids) {
			List<JcNode> nodes = new ArrayList<JcNode>(ids.length);
			IClause[] clauses = new IClause[ids.length + 1];
			
			for (int i = 0; i < ids.length; i++) {
				JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
				nodes.add(n);
				clauses[i] = START.node(n).byId(ids[i]);
			}
			clauses[ids.length] = RETURN.ALL();
			
			JcQuery query = new JcQuery();
			query.setClauses(clauses);
			JcQueryResult result = this.dbAccess.execute(query);
			if (result.hasErrors()) {
				List<JcError> errors = Util.collectErrors(result);
				throw new JcResultException(errors);
			}

			List<T> ret = new ArrayList<T>(nodes.size());
			for (int i = 0; i < nodes.size(); i++) {
				GrNode rNode = result.resultOf(nodes.get(i)).get(0);
				ret.add(createFromGraph(domainObjectClass, rNode));
			}

			return ret;
		}
		
		private Graph updateLocalGraph(List<Object> domainObjects) {
			Graph graph = null;
			Object domainObject;
			Map<Integer, JcNode> nIndexMap = null;
			Map<Integer, GrNode> rIndexMap = null;
			List<IClause> clauses = null;
			for (int i = 0; i < domainObjects.size(); i++) {
				domainObject = domainObjects.get(i);
				Long id = this.objectToIdMap.get(domainObject);
				if (id != null) { // object exists in graphdb
					JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
					if (nIndexMap == null)
						nIndexMap = new HashMap<Integer, JcNode>();
					nIndexMap.put(new Integer(i), n);
					if (clauses == null)
						clauses = new ArrayList<IClause>();
					clauses.add(START.node(n).byId(id.longValue()));
				}
			}
			if (clauses != null) {
				clauses.add(RETURN.ALL());
				IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
				JcQuery query = new JcQuery();
				query.setClauses(clausesArray);
				JcQueryResult result = this.dbAccess.execute(query);
				if (result.hasErrors()) {
					List<JcError> errors = Util.collectErrors(result);
					throw new JcResultException(errors);
				}
				graph = result.getGraph();
				rIndexMap = new HashMap<Integer, GrNode>(nIndexMap.size());
				Iterator<Entry<Integer, JcNode>> nit = nIndexMap.entrySet().iterator();
				while (nit.hasNext()) {
					Entry<Integer, JcNode> entry = nit.next();
					List<GrNode> rns = result.resultOf(entry.getValue());
					rIndexMap.put(entry.getKey(), rns.get(0));
				}
			}
			// up to here, objects existing as nodes in the graphdb have been loaded
			
			if (graph == null) // no nodes loaded from db
				graph = Graph.create(this.dbAccess);
			for (int i = 0; i < domainObjects.size(); i++) {
				GrNode rNode = null;
				if (rIndexMap != null) {
					rNode = rIndexMap.get(i);
				}
				if (rNode == null)
					rNode = graph.createNode();
				
				updateFromObject(domainObjects.get(i), rNode);
			}
			return graph;
		}

		private <T> T createFromGraph(Class<T> domainObjectClass, GrNode rNode) {
			ObjectMapping objectMapping = getObjectMappingFor(domainObjectClass);
			T domainObject;
			try {
				domainObject = domainObjectClass.newInstance();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			
			objectMapping.mapToObject(domainObject, rNode);
			
			return domainObject;
		}

		private void updateFromObject(Object domainObject, GrNode rNode) {
			ObjectMapping objectMapping = getObjectMappingFor(domainObject.getClass());
			objectMapping.mapFromObject(domainObject, rNode);
		}
		
		private ObjectMapping getObjectMappingFor(Class<?> domainObjectClass) {
			ObjectMapping objectMapping = this.mappings.get(domainObjectClass);
			if (objectMapping == null) {
				objectMapping = DefaultObjectMappingCreator.createObjectMapping(domainObjectClass);
				this.mappings.put(domainObjectClass, objectMapping);
			}
			return objectMapping;
		}
	}
}
