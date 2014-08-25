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
import iot.jcypher.query.writer.Format;
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
		private static final String RelationPrefix = "r_";
		private IDBAccess dbAccess;
		private Map<Object, Long> objectToIdMap;
		private Map<Relation, Long> relationToIdMap;
		private Map<Class<?>, ObjectMapping> mappings;

		private DomainConfigHandler(IDBAccess dbAccess) {
			super();
			this.dbAccess = dbAccess;
			this.objectToIdMap = new HashMap<Object, Long>();
			this.relationToIdMap = new HashMap<Relation, Long>();
			this.mappings = new HashMap<Class<?>, ObjectMapping>();
		}
		
		<T> List<T> loadByIds(Class<T> domainObjectClass, long... ids) {
			List<T> resultList = new ArrayList<T>();
			
			ClosureQueryContext context = new ClosureQueryContext(domainObjectClass);
			new ClosureCalculator().calculateClosureQuery(context);
			boolean repeat = context.matchClauses != null && context.matchClauses.size() > 0;
			
			List<JcNode> nodes = new ArrayList<JcNode>(ids.length);
			
			if (repeat) {
				JcQuery query;
				String nm = NodePrefix.concat(String.valueOf(0));
				List<JcQuery> queries = new ArrayList<JcQuery>();
				for (int i = 0; i < ids.length; i++) {
					query = new JcQuery();
					JcNode n = new JcNode(nm);
					List<IClause> clauses = new ArrayList<IClause>();
					clauses.add(START.node(n).byId(ids[i]));
					clauses.addAll(context.matchClauses);
					clauses.add(RETURN.ALL());
					IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
					query.setClauses(clausesArray);
					queries.add(query);
				}
//				Util.printQueries(queries, "CLOSURE", Format.PRETTY_1);
				List<JcQueryResult> results = this.dbAccess.execute(queries);
				List<JcError> errors = Util.collectErrors(results);
				if (errors.size() > 0) {
					throw new JcResultException(errors);
				}
//				Util.printResults(results, "CLOSURE", Format.PRETTY_1);
				for (int i = 0; i < ids.length; i++) {
					FillModelContext<T> fContext = new FillModelContext<T>(domainObjectClass,
							results.get(i));
					new ClosureCalculator().fillModel(fContext);
					resultList.add(fContext.domainObject);
				}
			} else {
				List<IClause> clauses = new ArrayList<IClause>();
				for (int i = 0; i < ids.length; i++) {
					JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
					nodes.add(n);
					clauses.add(START.node(n).byId(ids[i]));
				}
				clauses.add(RETURN.ALL());
				JcQuery query = new JcQuery();
				IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
				query.setClauses(clausesArray);
				JcQueryResult result = this.dbAccess.execute(query);
				if (result.hasErrors()) {
					List<JcError> errors = Util.collectErrors(result);
					throw new JcResultException(errors);
				}
				for (int i = 0; i < nodes.size(); i++) {
					GrNode rNode = result.resultOf(nodes.get(i)).get(0);
					T obj = createFromGraph_OBSOLETE(domainObjectClass, rNode);
					resultList.add(obj);
					this.objectToIdMap.put(obj, rNode.getId());
				}
			}

			return resultList;
		}
		
		Graph updateLocalGraph(List<Object> domainObjects) {
			UpdateContext context = new UpdateContext();
			new ClosureCalculator().calculateClosure(domainObjects,
					context);
			Graph graph = null;
			Object domainObject;
			Map<Integer, JcNode> nIndexMap = null;
			Map<Integer, GrNode> rIndexMap = null;
			List<IClause> clauses = null;
			for (int i = 0; i < context.domainObjects.size(); i++) {
				domainObject = context.domainObjects.get(i);
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
			Map<Integer, JcRelation> rnIndexMap = null;
			Map<Integer, GrRelation> rrIndexMap = null;
			for (int i = 0; i < context.relations.size(); i++) {
				Relation relat = context.relations.get(i);
				Long id = this.relationToIdMap.get(relat);
				if (id != null) { // relation exists in graphdb
					JcRelation r = new JcRelation(RelationPrefix.concat(String.valueOf(i)));
					if (rnIndexMap == null)
						rnIndexMap = new HashMap<Integer, JcRelation>();
					rnIndexMap.put(new Integer(i), r);
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
				if (nIndexMap != null)
					rIndexMap = new HashMap<Integer, GrNode>(nIndexMap.size());
				Iterator<Entry<Integer, JcNode>> nit = nIndexMap.entrySet().iterator();
				while (nit.hasNext()) {
					Entry<Integer, JcNode> entry = nit.next();
					rIndexMap.put(entry.getKey(), result.resultOf(entry.getValue()).get(0));
				}
				if (rnIndexMap != null)
					rrIndexMap = new HashMap<Integer, GrRelation>(rnIndexMap.size());
				Iterator<Entry<Integer, JcRelation>> rit = rnIndexMap.entrySet().iterator();
				while (rit.hasNext()) {
					Entry<Integer, JcRelation> entry = rit.next();
					rrIndexMap.put(entry.getKey(), result.resultOf(entry.getValue()).get(0));
				}
			}
			// up to here, objects existing as nodes in the graphdb have been loaded
			
			if (graph == null) // no nodes loaded from db
				graph = Graph.create(this.dbAccess);
			
			Map<Object, GrNode> domObj2Node = new HashMap<Object, GrNode>(
					context.domainObjects.size());
			for (int i = 0; i < context.domainObjects.size(); i++) {
				GrNode rNode = null;
				if (rIndexMap != null) {
					rNode = rIndexMap.get(i);
				}
				if (rNode == null)
					rNode = graph.createNode();
				
				domObj2Node.put(context.domainObjects.get(i), rNode);
				updateFromObject(context.domainObjects.get(i), rNode);
			}
			
			for (int i = 0; i < context.relations.size(); i++) {
				GrRelation rRelation = null;
				if (rrIndexMap != null) {
					rRelation = rrIndexMap.get(i);
				}
				if (rRelation == null) {
					Relation relat = context.relations.get(i);
					rRelation = graph.createRelation(relat.type,
							domObj2Node.get(relat.start), domObj2Node.get(relat.end));
				}
			}
			return graph;
		}

		private <T> T createFromGraph_OBSOLETE(Class<T> domainObjectClass, GrNode rNode) {
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
	
	/**********************************************/
	private class ClosureCalculator {
		
		private <T> void fillModel(FillModelContext<T> context) {
			T domainObject = context.domainObject;
			if (domainObject == null) {
				try {
					domainObject = context.domainObjectClass.newInstance();
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
				context.domainObject = domainObject;
			}
			context.currentObject = domainObject;
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
			
			private <T> boolean fillModel(FillModelContext<T> context, FieldMapping fm,
					int fieldIndex, int level) {
				boolean isNullNode = false;
				if (!context.domainObjects.contains(context.currentObject)) {
					context.domainObjects.add(context.currentObject);
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
							ObjectMapping objectMapping = domainConfigHandler.getObjectMappingFor(doClass);
							List<FieldMapping> fMappings = objectMapping.getFieldMappings();
							int idx = -1;
							for (FieldMapping fMap : fMappings) {
								idx++;
								if (fMap.needsRelation()) {
									Object prev = context.currentObject;
									Object domainObject;
									try {
										domainObject = fMap.getFieldType().newInstance();
									} catch (Throwable e) {
										throw new RuntimeException(e);
									}
									context.currentObject = domainObject;
									boolean nodeIsNull = this.fillModel(context, fMap, idx, level + 1);
									if (!nodeIsNull)
										fMap.setField(prev, domainObject);
									context.currentObject = prev;
								} else {
									fMap.mapToField(context.currentObject, rNode);
								}
							}
						} else {
							context.domainObjects.remove(context.currentObject);
							isNullNode = true;
						}
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
		private List<Object> domainObjects = new ArrayList<Object>();
		
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
	}
}
