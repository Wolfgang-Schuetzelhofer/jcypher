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
import iot.jcypher.domain.mapping.DomainState;
import iot.jcypher.domain.mapping.DomainState.Relation;
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.ObjectMapping;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.pattern.Node;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
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

public class DomainAccess {
	
	private DomainAccessHandler domainAccessHandler;

	public DomainAccess(IDBAccess dbAccess) {
		super();
		this.domainAccessHandler = new DomainAccessHandler(dbAccess);
	}

	public List<JcError> store(Object domainObject) {
		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(domainObject);
		return this.store(domainObjects);
	}
	
	public List<JcError> store(List<Object> domainObjects) {
		return this.domainAccessHandler.store(domainObjects);
	}
	
	public <T> T loadById(Class<T> domainObjectClass, long id) {
		long[] ids = new long[] {id};
		List<T> ret = this.domainAccessHandler.loadByIds(domainObjectClass, ids);
		return ret.get(0);
	}
	
	public <T> List<T> loadByIds(Class<T> domainObjectClass, long... ids) {
		return this.domainAccessHandler.loadByIds(domainObjectClass, ids);
	}
	
	/**********************************************************************/
	private class DomainAccessHandler {
		private static final String NodePrefix = "n_";
		private static final String RelationPrefix = "r_";
		
		/**
		 * defines at which recursion occurrence building a query is stopped
		 */
		private int maxRecursionCount = 1;
		private IDBAccess dbAccess;
		private DomainState domainState;
		private Map<Class<?>, ObjectMapping> mappings;

		private DomainAccessHandler(IDBAccess dbAccess) {
			super();
			this.dbAccess = dbAccess;
			this.domainState = new DomainState();
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
			if (errors.isEmpty()) {
				for (Relation relat : context.relationsToRemove) {
					domainState.removeRelation(relat);
				}
				
				Iterator<Entry<Object, GrNode>> it = context.domObj2Node.entrySet().iterator();
				while(it.hasNext()) {
					Entry<Object, GrNode> entry = it.next();
					this.domainState.add_Id2Object(entry.getKey(), entry.getValue().getId(), ResolutionDepth.DEEP);
				}
				
				for (DomRelation2ResultRelation d2r : context.domRelation2Relations) {
					this.domainState.add_Id2Relation(d2r.domRelation, d2r.resultRelation.getId());
				}
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
			List<IClause> removeStartClauses = null;
			List<IClause> removeClauses = null;
			for (int i = 0; i < context.domainObjects.size(); i++) {
				domainObject = context.domainObjects.get(i);
				Long id = this.domainState.getIdFrom_Object2IdMap(domainObject);
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
				Long id = this.domainState.getFrom_Relation2IdMap(relat);
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
			
			// relations to remove
			if (context.relationsToRemove.size() > 0) {
				removeStartClauses = new ArrayList<IClause>();
				removeClauses = new ArrayList<IClause>();
				for (int i = 0; i < context.relationsToRemove.size(); i++) {
					Relation relat = context.relationsToRemove.get(i);
					// relation must exist in db
					Long id = this.domainState.getFrom_Relation2IdMap(relat);
					JcRelation r = new JcRelation(RelationPrefix.concat(String.valueOf(i)));
					removeStartClauses.add(START.relation(r).byId(id.longValue()));
					removeClauses.add(DO.DELETE(r));
				}
			}
			
			if (clauses != null || removeStartClauses != null) {
				JcQuery query;
				List<JcQuery> queries = new ArrayList<JcQuery>();
				if (clauses != null) {
					clauses.add(RETURN.ALL());
					IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
					query = new JcQuery();
					query.setClauses(clausesArray);
					queries.add(query);
				}
				if (removeStartClauses != null) {
					removeStartClauses.addAll(removeClauses);
					query = new JcQuery();
					query.setClauses(removeStartClauses.toArray(new IClause[removeStartClauses.size()]));
					queries.add(query);
				}
//				Util.printQueries(queries, "CLOSURE", Format.PRETTY_1);
				List<JcQueryResult> results = this.dbAccess.execute(queries);
				List<JcError> errors = Util.collectErrors(results);
				if (errors.size() > 0) {
					throw new JcResultException(errors);
				}
				
				if (clauses != null) {
					JcQueryResult result = results.get(0);
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
			}
			// up to here, objects existing as nodes in the graphdb as well as relations have been loaded
			// and relations that should be removed have been removed from the db
			
			if (graph == null) // no nodes loaded from db
				graph = Graph.create(this.dbAccess);
			
			context.domObj2Node = new HashMap<Object, GrNode>(
					context.domainObjects.size());
			context.domRelation2Relations = new ArrayList<DomRelation2ResultRelation>();
			for (int i = 0; i < context.domainObjects.size(); i++) {
				GrNode rNode = null;
				if (nodeIndexMap != null && nodeIndexMap.get(i) != null) {
					rNode = nodeIndexMap.get(i).resultNode;
				}
				if (rNode == null)
					rNode = graph.createNode();
				
				context.domObj2Node.put(context.domainObjects.get(i), rNode);
				updateFromObject(context.domainObjects.get(i), rNode);
			}
			
			for (int i = 0; i < context.relations.size(); i++) {
				GrRelation rRelation = null;
				if (relationIndexMap != null && relationIndexMap.get(i) != null) {
					rRelation = relationIndexMap.get(i).resultRelation;
				}
				if (rRelation == null) {
					Relation relat = context.relations.get(i);
					rRelation = graph.createRelation(relat.getType(),
							context.domObj2Node.get(relat.getStart()), context.domObj2Node.get(relat.getEnd()));
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
				// check if domain objects have already been loaded
				T obj = (T) this.domainState.checkForMappedObject(domainObjectClass, ids[i]);
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
							id2QueryResult.get(ids[i]), context.recursionEndNodes);
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
				// check if domain objects have already been loaded
				T obj = (T) this.domainState.checkForMappedObject(domainObjectClass, ids[i]);
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
					this.domainState.add_Id2Object(obj, ids[i], ResolutionDepth.DEEP);
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
	}
	
	/**********************************************/
	private class ClosureCalculator {
		
		private <T> void fillModel(FillModelContext<T> context) {
			Step step = new Step();
			step.fillModel(context, null);
		}
		
		private void calculateClosureQuery(ClosureQueryContext context) {
			boolean isDone = false;
			Step step = new Step();
			while (!isDone) {
				isDone = step.calculateQuery(null, context);
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
				ObjectMapping objectMapping = domainAccessHandler.getObjectMappingFor(domainObject.getClass());
				List<FieldMapping> fMappings = objectMapping.getFieldMappings();
				for (FieldMapping fm : fMappings) {
					if (fm.needsRelation()) {
						boolean checkRemoval = false;
						Object obj = fm.getObjectNeedingRelation(domainObject);
						if (obj != null) {
							Relation relat = new Relation(fm.getPropertyOrRelationName(), domainObject, obj);
							if (!domainAccessHandler.domainState.existsRelation(relat)) {
								context.relations.add(relat); // relation not in db
								checkRemoval = true;
							}
							recursiveCalculateClosure(obj, context);
						} else {
							checkRemoval = true;
						}
						if (checkRemoval) {
							Relation relat = domainAccessHandler.domainState.findRelation(domainObject,
									fm.getPropertyOrRelationName());
							if (relat != null) {
								context.relationsToRemove.add(relat);
							}
						}
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
			private <T> boolean fillModel(FillModelContext<T> context, FieldMapping fm) {
				boolean isNullNode = false;
				String nnm = this.buildNodeOrRelationName(context.path,
						DomainAccessHandler.NodePrefix);
				
				Class<?> doClass;
				if (fm == null)
					doClass = context.domainObjectClass;
				else
					doClass = fm.getFieldType();
				
				// prepare for navigation to next node
				context.path.add(new PathElement(doClass));
				
				boolean resolveDeep = true;
				if (context.recursionEndNodes.contains(nnm)) { // exit recursion
					resolveDeep = false;
				}
				
				JcNode n = new JcNode(nnm);
				List<GrNode> resList = context.qResult.resultOf(n);
				if (resList.size() > 0) { // a result node exists for this pattern
					GrNode rNode = resList.get(0);
					if (rNode != null) { // null values are supported
						boolean performMapping = false;
						boolean mapProperties = true;
						Object domainObject = null;
						// check if a domain object has already been mapped to this node
						domainObject =
								domainAccessHandler.domainState.checkForMappedObject(doClass, rNode.getId());
						
						if (domainObject == null) {
							try {
								domainObject = doClass.newInstance();
							} catch (Throwable e) {
								throw new RuntimeException(e);
							}
							domainAccessHandler.domainState.add_Id2Object(domainObject, rNode.getId(),
									resolveDeep ? ResolutionDepth.DEEP : ResolutionDepth.SHALLOW);
							performMapping = true;
						} else {
							if (resolveDeep &&
									domainAccessHandler.domainState.getResolutionDepth(domainObject) !=
										ResolutionDepth.DEEP) {
								performMapping = true;
								mapProperties = false; // properties have already been mapped
							}
						}
						
						if (fm == null) { // we are at the root level
							context.domainObject = (T) domainObject;
						}
						
						if (performMapping) {
							ObjectMapping objectMapping = domainAccessHandler.getObjectMappingFor(doClass);
							List<FieldMapping> fMappings = objectMapping.getFieldMappings();
							int idx = 0;
							for (FieldMapping fMap : fMappings) {
								idx++; // index starts with 1 so as not to mix with the root node (n_0)
								if (fMap.needsRelation() && resolveDeep) {
									context.currentObject = null;
									PathElement pe = context.getLastPathElement();
									pe.fieldIndex = idx;
									pe.fieldName = fMap.getFieldName();
									boolean nodeIsNull = this.fillModel(context, fMap);
									if (!nodeIsNull && context.currentObject != null) {
										fMap.setField(domainObject, context.currentObject);
										String rnm = this.buildNodeOrRelationName(context.path,
												DomainAccessHandler.RelationPrefix);
										JcRelation r = new JcRelation(rnm);
										List<GrRelation> relList = context.qResult.resultOf(r);
										// relation must exist, because the related object exists 
										GrRelation rel = relList.get(0);
										domainAccessHandler.domainState.add_Id2Relation(
												new Relation(fMap.getPropertyOrRelationName(),
														domainObject,
														context.currentObject), rel.getId());
									}
								} else {
									if (mapProperties)
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
				context.path.remove(context.path.size() - 1); // remove the last one
				return isNullNode;
			}
			
			/**
			 * @param fm, null for the root step
			 * @param context
			 * @return true, if calculating query for the current path is done
			 */
			private boolean calculateQuery(FieldMapping fm, ClosureQueryContext context) {
				boolean ret = true;
				Class<?> doClass;
				if (fm == null)
					doClass = context.domainObjectClass;
				else
					doClass = fm.getFieldType();
				
				if (fm != null) // don't add a match for the start node itself
					this.addToQuery(fm, context); // navigate to this node
				
				boolean resolveDeep = true;
				boolean walkedToIndex = this.subPathIndex == -1;
				// do the following check to avoid infinite loops
				if (walkedToIndex) { // we are visiting the first time
					if (context.getRecursionCount() >= domainAccessHandler.maxRecursionCount)
						resolveDeep = false;
				}
				
				if (!resolveDeep) { // recursion ends here
					String nm = this.buildNodeOrRelationName(context.path,
							DomainAccessHandler.NodePrefix);
					context.recursionEndNodes.add(nm);
				}
				
				// prepare for navigation to next node
				context.path.add(new PathElement(doClass));
				
				ObjectMapping objectMapping = domainAccessHandler.getObjectMappingFor(doClass);
				List<FieldMapping> fMappings = objectMapping.getFieldMappings();
				boolean subPathWalked = false;
				int idx = 0;
				for (FieldMapping fMap : fMappings) {
					idx++; // index starts with 1 so as not to mix with the root node (n_0)
					if (!walkedToIndex) {
						if (idx != this.subPathIndex) // until subPathIndex is reached
							continue;
						else
							walkedToIndex = true;
					}
					
					if (fMap.needsRelation() && resolveDeep) {
						boolean needToComeBack = false;
						if (!subPathWalked) {
							if (this.next == null)
								this.next = new Step();
							PathElement pe = context.getLastPathElement();
							pe.fieldIndex = idx;
							pe.fieldName = fMap.getFieldName();
							boolean isDone = this.next.calculateQuery(fMap, context);
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
							ret = false;
							break;
						}
					}
				}
				context.path.remove(context.path.size() - 1); // remove the last one
				return ret;
			}
			
			private void addToQuery(FieldMapping fm, ClosureQueryContext context) {
				if (context.currentMatchClause == null) {
					JcNode n = new JcNode(DomainAccessHandler.NodePrefix.concat(String.valueOf(0)));
					context.currentMatchClause = OPTIONAL_MATCH.node(n);
					if (context.matchClauses != null && context.matchClauses.size() > 0) {
						context.matchClauses.add(SEPARATE.nextClause());
					}
				}
				
				JcNode n = new JcNode(this.buildNodeOrRelationName(context.path,
						DomainAccessHandler.NodePrefix));
				JcRelation r = new JcRelation(this.buildNodeOrRelationName(context.path,
						DomainAccessHandler.RelationPrefix));
				context.currentMatchClause.relation(r).out().type(fm.getPropertyOrRelationName())
				.node(n);
			}
			
			private String buildNodeOrRelationName(List<PathElement> path, String prefix) {
				// format of node name: n_idx1_idx2_idx3...
				StringBuilder sb = new StringBuilder();
				sb.append(prefix);
				if (path.size() > 0) {
					for (int i = 0; i < path.size(); i++) {
						if (i > 0)
							sb.append('_');
						sb.append(path.get(i).fieldIndex);
					}
				} else
					sb.append(0); // root node name
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
		private List<PathElement> path;
		private List<String> recursionEndNodes;
		
		FillModelContext(Class<T> domainObjectClass, JcQueryResult qResult,
				List<String> recursionEndNds) {
			super();
			this.domainObjectClass = domainObjectClass;
			this.qResult = qResult;
			this.path = new ArrayList<PathElement>();
			this.recursionEndNodes = recursionEndNds;
		}
		
		private PathElement getLastPathElement() {
			if (this.path.size() > 0)
				return this.path.get(this.path.size() - 1);
			return null;
		}
	}
	
	/***********************************/
	private class ClosureQueryContext {
		private Class<?> domainObjectClass;
		private List<IClause> matchClauses;
		private Node currentMatchClause;
		private List<PathElement> path;
		private List<String> recursionEndNodes;
		
		ClosureQueryContext(Class<?> domainObjectClass) {
			super();
			this.domainObjectClass = domainObjectClass;
			this.path = new ArrayList<PathElement>();
			this.recursionEndNodes = new ArrayList<String>();
		}
		
		private void addMatchClause(IClause clause) {
			if (this.matchClauses == null)
				this.matchClauses = new ArrayList<IClause>();
			this.matchClauses.add(clause);
		}
		
		private PathElement getLastPathElement() {
			if (this.path.size() > 0)
				return this.path.get(this.path.size() - 1);
			return null;
		}
		
		private int getRecursionCount() {
			int count = 0;
			int sz = this.path.size();
			if (sz > 0) {
				PathElement peComp = this.path.get(sz - 1);
				for (int i = sz - 2; i >= 0; i--) {
					PathElement pe = this.path.get(i);
					if (pe.sourceType.equals(peComp.sourceType) && pe.fieldName.equals(peComp.fieldName))
						count++;
				}
			}
			return count;
		}
	}
	
	/*********************************/
	private static class PathElement {
		private Class<?> sourceType;
		private String fieldName;
		private int fieldIndex;
		
		private PathElement(Class<?> sourceType) {
			super();
			this.sourceType = sourceType;
		}
	}
	
	/***********************************/
	private class UpdateContext {
		private List<Object> domainObjects = new ArrayList<Object>();
		private List<Relation> relations = new ArrayList<Relation>();
		private List<Relation> relationsToRemove = new ArrayList<Relation>();
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
}
