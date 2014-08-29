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
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.ObjectMapping;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.pattern.Node;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
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
		return this.domainConfigHandler.store(domainObjects);
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
		private static final String RelationPrefix = "r_";
		private IDBAccess dbAccess;
		private Map<Object, Long> objectToIdMap;
		private Map<Relation, Long> relationToIdMap;
		private Map<Long, List<Object>> idToObjectsMap;
		private Map<Class<?>, ObjectMapping> mappings;

		private DomainConfigHandler(IDBAccess dbAccess) {
			super();
			this.dbAccess = dbAccess;
			this.objectToIdMap = new HashMap<Object, Long>();
			this.relationToIdMap = new HashMap<Relation, Long>();
			this.idToObjectsMap = new HashMap<Long, List<Object>>();
			this.mappings = new HashMap<Class<?>, ObjectMapping>();
		}
		
		<T> List<T> loadByIds(Class<T> domainObjectClass, long... ids) {
			List<T> resultList;
			
			ClosureQueryContext context = new ClosureQueryContext(domainObjectClass);
			new ClosureCalculator().calculateClosureQuery(context);
			boolean repeat = context.matchClauses != null && context.matchClauses.size() > 0;
			
			if (repeat) { // has one or more match clauses
				resultList = loadByIdsWithMatches(domainObjectClass, context, ids);
			} else { // only simple start by id clauses are needed
				resultList = loadByIdsSimple(domainObjectClass, ids);
			}

			return resultList;
		}
		
		List<JcError> store(List<Object> domainObjects) {
			UpdateContext context = this.updateLocalGraph(domainObjects);
			List<JcError> errors = context.graph.store();
			Iterator<Entry<Object, GrNode>> it = context.domObj2Node.entrySet().iterator();
			
			while(it.hasNext()) {
				Entry<Object, GrNode> entry = it.next();
				objectToIdMap.put(entry.getKey(), entry.getValue().getId());
				addToIdToObjectsMap(entry.getKey(), entry.getValue().getId());
			}
			
			for (DomRelation2ResultRelation d2r : context.domRelation2Relations) {
				relationToIdMap.put(d2r.domRelation, d2r.resultRelation.getId());
			}
			
			return errors;
		}
		
		private UpdateContext updateLocalGraph(List<Object> domainObjects) {
			UpdateContext context = new UpdateContext();
			new ClosureCalculator().calculateClosure(domainObjects,
					context);
			Graph graph = null;
			Object domainObject;
			Map<Integer, QueryNode2ResultNode> nodeIndexMap = null;
			List<IClause> clauses = null;
			for (int i = 0; i < context.domainObjects.size(); i++) {
				domainObject = context.domainObjects.get(i);
				Long id = this.objectToIdMap.get(domainObject);
				if (id != null) { // object exists in graphdb
					JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
					QueryNode2ResultNode n2n = new QueryNode2ResultNode();
					n2n.queryNode = n;
					if (nodeIndexMap == null)
						nodeIndexMap = new HashMap<Integer, QueryNode2ResultNode>();
					nodeIndexMap.put(new Integer(i), n2n);
					if (clauses == null)
						clauses = new ArrayList<IClause>();
					clauses.add(START.node(n).byId(id.longValue()));
				}
			}
			
			Map<Integer, QueryRelation2ResultRelation> relationIndexMap = null;
			for (int i = 0; i < context.relations.size(); i++) {
				Relation relat = context.relations.get(i);
				Long id = this.relationToIdMap.get(relat);
				if (id != null) { // relation exists in graphdb
					JcRelation r = new JcRelation(RelationPrefix.concat(String.valueOf(i)));
					QueryRelation2ResultRelation r2r = new QueryRelation2ResultRelation();
					r2r.queryRelation = r;
					if (relationIndexMap == null)
						relationIndexMap = new HashMap<Integer, QueryRelation2ResultRelation>();
					relationIndexMap.put(new Integer(i), r2r);
					clauses.add(START.relation(r).byId(id.longValue()));
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
				if (nodeIndexMap != null) {
					Iterator<Entry<Integer, QueryNode2ResultNode>> nit = nodeIndexMap.entrySet().iterator();
					while (nit.hasNext()) {
						Entry<Integer, QueryNode2ResultNode> entry = nit.next();
						entry.getValue().resultNode = result.resultOf(entry.getValue().queryNode).get(0);
					}
				}
				if (relationIndexMap != null) {
					Iterator<Entry<Integer, QueryRelation2ResultRelation>> rit = relationIndexMap.entrySet().iterator();
					while (rit.hasNext()) {
						Entry<Integer, QueryRelation2ResultRelation> entry = rit.next();
						entry.getValue().resultRelation = result.resultOf(entry.getValue().queryRelation).get(0);
					}
				}
			}
			// up to here, objects existing as nodes in the graphdb have been loaded
			
			if (graph == null) // no nodes loaded from db
				graph = Graph.create(this.dbAccess);
			
			context.domObj2Node = new HashMap<Object, GrNode>(
					context.domainObjects.size());
			context.domRelation2Relations = new ArrayList<DomRelation2ResultRelation>();
			for (int i = 0; i < context.domainObjects.size(); i++) {
				GrNode rNode = null;
				if (nodeIndexMap != null) {
					rNode = nodeIndexMap.get(i).resultNode;
				}
				if (rNode == null)
					rNode = graph.createNode();
				
				context.domObj2Node.put(context.domainObjects.get(i), rNode);
				updateFromObject(context.domainObjects.get(i), rNode);
			}
			
			for (int i = 0; i < context.relations.size(); i++) {
				GrRelation rRelation = null;
				if (relationIndexMap != null) {
					rRelation = relationIndexMap.get(i).resultRelation;
				}
				if (rRelation == null) {
					Relation relat = context.relations.get(i);
					rRelation = graph.createRelation(relat.type,
							context.domObj2Node.get(relat.start), context.domObj2Node.get(relat.end));
					DomRelation2ResultRelation d2r = new DomRelation2ResultRelation();
					d2r.domRelation = relat;
					d2r.resultRelation = rRelation;
					context.domRelation2Relations.add(d2r);
				}
			}
			context.graph = graph;
			return context;
		}
		
		/**
		 * has one or more match clauses
		 * @param domainObjectClass
		 * @param context
		 * @param ids
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private <T> List<T> loadByIdsWithMatches(Class<T> domainObjectClass,
				ClosureQueryContext context, long... ids) {
			List<T> resultList = new ArrayList<T>();
			JcQuery query;
			String nm = NodePrefix.concat(String.valueOf(0));
			List<JcQuery> queries = new ArrayList<JcQuery>();
			Map<Long, JcQueryResult> id2QueryResult = new HashMap<Long, JcQueryResult>();
			List<Long> queryIds = new ArrayList<Long>();
			Map<Long, T> id2Object = new HashMap<Long, T>();
			for (int i = 0; i < ids.length; i++) {
				T obj = null;
				// synchronize for reentrancy
				synchronized (domainConfigHandler.idToObjectsMap) {
					// check if domain objects have already been loaded
					obj = (T) checkForMappedObject(domainObjectClass, ids[i]);
				}
				if (obj != null) {
					id2Object.put(ids[i], obj);
				} else {
					query = new JcQuery();
					JcNode n = new JcNode(nm);
					List<IClause> clauses = new ArrayList<IClause>();
					clauses.add(START.node(n).byId(ids[i]));
					clauses.addAll(context.matchClauses);
					clauses.add(RETURN.ALL());
					IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
					query.setClauses(clausesArray);
					queries.add(query);
					queryIds.add(ids[i]);
				}
			}
			
			if (queries.size() > 0) { // at least one node has to be loaded
//				Util.printQueries(queries, "CLOSURE", Format.PRETTY_1);
				List<JcQueryResult> results = this.dbAccess.execute(queries);
				List<JcError> errors = Util.collectErrors(results);
				if (errors.size() > 0) {
					throw new JcResultException(errors);
				}
//				Util.printResults(results, "CLOSURE", Format.PRETTY_1);
				for (int i = 0; i < queries.size(); i++) {
					id2QueryResult.put(queryIds.get(i), results.get(i));
				}
			}
			
			for (int i = 0; i < ids.length; i++) {
				T obj = id2Object.get(ids[i]);
				if (obj == null) { // need to load
					FillModelContext<T> fContext = new FillModelContext<T>(domainObjectClass,
							id2QueryResult.get(ids[i]));
					new ClosureCalculator().fillModel(fContext);
					obj = fContext.domainObject;
				}
				resultList.add(obj);
			}
			return resultList;
		}
		
		/**
		 * has start by id clauses only
		 * @param domainObjectClass
		 * @param ids
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private <T> List<T> loadByIdsSimple(Class<T> domainObjectClass, long... ids) {
			List<T> resultList = new ArrayList<T>();
			List<IClause> clauses = new ArrayList<IClause>();
			Map<Long, JcNode> id2QueryNode = new HashMap<Long, JcNode>();
			Map<Long, T> id2Object = new HashMap<Long, T>();
			for (int i = 0; i < ids.length; i++) {
				T obj = null;
				// synchronize for reentrancy
				synchronized (domainConfigHandler.idToObjectsMap) {
					// check if domain objects have already been loaded
					obj = (T) checkForMappedObject(domainObjectClass, ids[i]);
				}
				if (obj != null) {
					id2Object.put(ids[i], obj);
				} else {
					JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
					id2QueryNode.put(ids[i], n);
					clauses.add(START.node(n).byId(ids[i]));
				}
			}
			
			JcQueryResult result = null;
			if (clauses.size() > 0) { // one or more nodes are to be loaded
				clauses.add(RETURN.ALL());
				JcQuery query = new JcQuery();
				IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
				query.setClauses(clausesArray);
				result = this.dbAccess.execute(query);
				if (result.hasErrors()) {
					List<JcError> errors = Util.collectErrors(result);
					throw new JcResultException(errors);
				}
			}
			
			for (int i = 0; i < ids.length; i++) {
				T obj = id2Object.get(ids[i]);
				if (obj == null) { // need to load
					GrNode rNode = result.resultOf(id2QueryNode.get(ids[i])).get(0);
					obj = createAndMapProperties(domainObjectClass, rNode);
					addToIdToObjectsMap(obj, ids[i]);
					this.objectToIdMap.put(obj, ids[i]);
				}
				resultList.add(obj);
			}
			return resultList;
		}

		private <T> T createAndMapProperties(Class<T> domainObjectClass, GrNode rNode) {
			ObjectMapping objectMapping = getObjectMappingFor(domainObjectClass);
			T domainObject;
			try {
				domainObject = domainObjectClass.newInstance();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			
			objectMapping.mapPropertiesToObject(domainObject, rNode);
			
			return domainObject;
		}

		private void updateFromObject(Object domainObject, GrNode rNode) {
			ObjectMapping objectMapping = getObjectMappingFor(domainObject.getClass());
			objectMapping.mapPropertiesFromObject(domainObject, rNode);
		}
		
		private ObjectMapping getObjectMappingFor(Class<?> domainObjectClass) {
			ObjectMapping objectMapping = this.mappings.get(domainObjectClass);
			if (objectMapping == null) {
				objectMapping = DefaultObjectMappingCreator.createObjectMapping(domainObjectClass);
				this.mappings.put(domainObjectClass, objectMapping);
			}
			return objectMapping;
		}

		private Object checkForMappedObject (Class<?> doClass, Long id) {
			List<Object> objs = idToObjectsMap.get(id);
			if (objs != null) {
				for (Object obj : objs) {
					if (obj.getClass().equals(doClass)) {
						return obj;
					}
				}
			} else {
				objs = new ArrayList<Object>();
				idToObjectsMap.put(id, objs);
			}
			return null;
		}
		
		private void addToIdToObjectsMap(Object obj, Long id) {
			synchronized (idToObjectsMap) {
				List<Object> objs = idToObjectsMap.get(id);
				if (objs == null) {
					objs = new ArrayList<Object>();
					idToObjectsMap.put(id, objs);
				}
				if (!objs.contains(obj))
					objs.add(obj);
			}
		}
	}
	
	/**********************************************/
	private class ClosureCalculator {
		
		private <T> void fillModel(FillModelContext<T> context) {
			Step step = new Step();
			step.fillModel(context, null, -1, -1);
		}
		
		private void calculateClosureQuery(ClosureQueryContext context) {
			boolean isDone = false;
			Step step = new Step();
			while (!isDone) {
				isDone = step.calculateQuery(null, -1, context, -1);
				if (context.currentMatchClause != null) {
					context.addMatchClause(context.currentMatchClause);
					context.currentMatchClause = null;
				}
			}
		}

		private void calculateClosure(List<Object> domainObjects, UpdateContext context) {
			for (Object domainObject : domainObjects) {
				recursiveCalculateClosure(domainObject, context);
			}
		}
		
		private void recursiveCalculateClosure(Object domainObject, UpdateContext context) {
			if (!context.domainObjects.contains(domainObject)) { // avoid infinite loops
				context.domainObjects.add(domainObject);
				ObjectMapping objectMapping = domainConfigHandler.getObjectMappingFor(domainObject.getClass());
				List<FieldMapping> fMappings = objectMapping.getFieldMappings();
				for (FieldMapping fm : fMappings) {
					Object obj = fm.getObjectNeedingRelation(domainObject);
					if (obj != null) {
						Relation relat = new Relation(fm.getPropertyOrRelationName(), domainObject, obj);
						context.relations.add(relat);
						recursiveCalculateClosure(obj, context);
					}
				}
			}
		}
		
		/**********************************************/
		private class Step {
			
			private int subPathIndex = -1;
			private Step next;
			
			/**
			 * @param context
			 * @param fm
			 * @param fieldIndex
			 * @param level
			 * @return true, if this leads to a null value
			 */
			@SuppressWarnings("unchecked")
			private <T> boolean fillModel(FillModelContext<T> context, FieldMapping fm,
					int fieldIndex, int level) {
				boolean isNullNode = false;
				String nnm = this.buildNodeName(fieldIndex, level);
				JcNode n = new JcNode(nnm);
				List<GrNode> resList = context.qResult.resultOf(n);
				if (resList.size() > 0) { // a result node exists for this pattern
					GrNode rNode = resList.get(0);
					if (rNode != null) { // null values are supported
						Class<?> doClass;
						if (fm == null)
							doClass = context.domainObjectClass;
						else
							doClass = fm.getFieldType();
						boolean performMapping = false;
						Object domainObject = null;
						// synchronize for reentrancy
						synchronized (domainConfigHandler.idToObjectsMap) {
							// check if a domain object has already been mapped to this node
							domainObject = domainConfigHandler.checkForMappedObject(doClass, rNode.getId());
							
							if (domainObject == null) {
								try {
									domainObject = doClass.newInstance();
								} catch (Throwable e) {
									throw new RuntimeException(e);
								}
								List<Object> objs = domainConfigHandler.idToObjectsMap.get(rNode.getId());
								objs.add(domainObject);
								domainConfigHandler.objectToIdMap.put(domainObject, rNode.getId());
								performMapping = true;
							}
						}
						
						if (fm == null) { // we are at the root level
							context.domainObject = (T) domainObject;
						}
						
						if (performMapping) {
							ObjectMapping objectMapping = domainConfigHandler.getObjectMappingFor(doClass);
							List<FieldMapping> fMappings = objectMapping.getFieldMappings();
							int idx = -1;
							for (FieldMapping fMap : fMappings) {
								idx++;
								if (fMap.needsRelation()) {
									context.currentObject = null;
									boolean nodeIsNull = this.fillModel(context, fMap, idx, level + 1);
									if (!nodeIsNull && context.currentObject != null)
										fMap.setField(domainObject, context.currentObject);
								} else {
									fMap.mapPropertyToField(domainObject, rNode);
								}
							}
						}
						// set the object mapped to the actual field (fm)
						context.currentObject = domainObject;
					} else {
						isNullNode = true;
					}
				}
				return isNullNode;
			}
			
			/**
			 * @param fm, null for the root step
			 * @param context
			 * @return true, if calculating query for the current path is done
			 */
			private boolean calculateQuery(FieldMapping fm, int fieldIndex, ClosureQueryContext context, int level) {
				if (fm != null) // don't add a match for the start node itself
					this.addToQuery(fm, fieldIndex, context, level);
				Class<?> doClass;
				if (fm == null)
					doClass = context.domainObjectClass;
				else
					doClass = fm.getFieldType();
				ObjectMapping objectMapping = domainConfigHandler.getObjectMappingFor(doClass);
				List<FieldMapping> fMappings = objectMapping.getFieldMappings();
				boolean walkedToIndex = this.subPathIndex == -1;
				boolean subPathWalked = false;
				int idx = -1;
				for (FieldMapping fMap : fMappings) {
					idx++;
					if (!walkedToIndex) {
						if (idx != this.subPathIndex) // until subPathIndex is reached
							continue;
						else
							walkedToIndex = true;
					}
					
					if (fMap.needsRelation()) {
						boolean needToComeBack = false;
						if (!subPathWalked) {
							if (this.next == null)
								this.next = new Step();
							boolean isDone = this.next.calculateQuery(fMap, idx, context, level + 1);
							if (!isDone) { // sub path not finished
								needToComeBack = true;
							} else {
								this.next = null;
								subPathWalked = true;
							}
						} else {
							needToComeBack = true;
						}
						
						if (needToComeBack) {
							this.subPathIndex = idx;
							return false;
						}
					}
				}
				return true;
			}
			
			private void addToQuery(FieldMapping fm, int fieldIndex, ClosureQueryContext context, int level) {
				if (context.currentMatchClause == null) {
					JcNode n = new JcNode(DomainConfigHandler.NodePrefix.concat(String.valueOf(0)));
					context.currentMatchClause = OPTIONAL_MATCH.node(n);
				}
				
				JcNode n = new JcNode(this.buildNodeName(fieldIndex, level));
				context.currentMatchClause.relation().out().type(fm.getPropertyOrRelationName())
				.node(n);
			}
			
			private String buildNodeName(int fieldIndex, int level) {
				// format of node name: n_x_y_z
				// x: index of the match clause
				// y: level of the current step within the query path
				// z: index of the field within the parent
				StringBuilder sb = new StringBuilder();
				sb.append(DomainConfigHandler.NodePrefix);
				sb.append(0);
				if (level >= 0) {
					sb.append('_');
					sb.append(level);
					sb.append('_');
					sb.append(fieldIndex);
				}
				
				return sb.toString();
			}
		}
	}
	
	/***********************************/
	private class FillModelContext<T> {
		private JcQueryResult qResult;
		private Class<T> domainObjectClass;
		private T domainObject;
		private Object currentObject;
		
		FillModelContext(Class<T> domainObjectClass, JcQueryResult qResult) {
			super();
			this.domainObjectClass = domainObjectClass;
			this.qResult = qResult;
		}
	}
	
	/***********************************/
	private class ClosureQueryContext {
		private Class<?> domainObjectClass;
		private List<IClause> matchClauses;
		private Node currentMatchClause;
		
		ClosureQueryContext(Class<?> domainObjectClass) {
			super();
			this.domainObjectClass = domainObjectClass;
		}
		
		private int getClauseIndex() {
			if(this.matchClauses == null)
				return 0;
			return this.matchClauses.size();
		}
		
		private void addMatchClause(IClause clause) {
			if (this.matchClauses == null)
				this.matchClauses = new ArrayList<IClause>();
			this.matchClauses.add(clause);
		}
	}
	
	/***********************************/
	private class UpdateContext {
		private List<Object> domainObjects = new ArrayList<Object>();
		private List<Relation> relations = new ArrayList<Relation>();
		private Map<Object, GrNode> domObj2Node;
		private List<DomRelation2ResultRelation> domRelation2Relations;
		private Graph graph;
	}
	
	/***********************************/
	private class DomRelation2ResultRelation {
		private Relation domRelation;
		private GrRelation resultRelation;
	}
	
	/***********************************/
	private class QueryNode2ResultNode {
		private JcNode queryNode;
		private GrNode resultNode;
	}
	
	/***********************************/
	private class QueryRelation2ResultRelation {
		private JcRelation queryRelation;
		private GrRelation resultRelation;
	}
	
	/***********************************/
	private class Relation {
		private String type;
		private Object start;
		private Object end;
		
		private Relation(String type, Object start, Object end) {
			super();
			this.type = type;
			this.start = start;
			this.end = end;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((end == null) ? 0 : end.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Relation other = (Relation) obj;
			if (end == null) {
				if (other.end != null)
					return false;
			} else if (!end.equals(other.end))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Relation [type=" + type + ", start=" + start + ", end="
					+ end + "]";
		}
	}
}
