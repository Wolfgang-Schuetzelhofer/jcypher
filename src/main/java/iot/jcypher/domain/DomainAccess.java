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
import iot.jcypher.domain.mapping.CompoundObjectMapping;
import iot.jcypher.domain.mapping.CompoundObjectType;
import iot.jcypher.domain.mapping.DefaultObjectMappingCreator;
import iot.jcypher.domain.mapping.DomainState;
import iot.jcypher.domain.mapping.DomainState.IRelation;
import iot.jcypher.domain.mapping.DomainState.KeyedRelation;
import iot.jcypher.domain.mapping.DomainState.KeyedRelationToChange;
import iot.jcypher.domain.mapping.DomainState.LoadInfo;
import iot.jcypher.domain.mapping.DomainState.Relation;
import iot.jcypher.domain.mapping.DomainState.SourceField2TargetKey;
import iot.jcypher.domain.mapping.DomainState.SourceFieldKey;
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.MapEntry;
import iot.jcypher.domain.mapping.MapEntry.MapEntrySimple;
import iot.jcypher.domain.mapping.MapEntry.MapEntryComplex;
import iot.jcypher.domain.mapping.MappingUtil;
import iot.jcypher.domain.mapping.ObjectMapping;
import iot.jcypher.graph.GrLabel;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.pattern.Node;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.writer.Format;
import iot.jcypher.result.JcError;
import iot.jcypher.result.JcResultException;
import iot.jcypher.result.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DomainAccess {
	
	private DomainAccessHandler domainAccessHandler;

	/**
	 * @param dbAccess the graph database connection
	 * @param domainName
	 */
	public DomainAccess(IDBAccess dbAccess, String domainName) {
		super();
		this.domainAccessHandler = new DomainAccessHandler(dbAccess, domainName);
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
	
	public SyncInfo getSyncInfo(Object domainObject) {
		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(domainObject);
		List<SyncInfo> ret = this.domainAccessHandler.getSyncInfos(domainObjects);
		return ret.get(0);
	}
	
	public List<SyncInfo> getSyncInfos(List<Object> domainObjects) {
		return this.domainAccessHandler.getSyncInfos(domainObjects);
	}
	
	/**********************************************************************/
	private class DomainAccessHandler {
		private static final String NodePrefix = "n_";
		private static final String RelationPrefix = "r_";
		private static final String DomainInfoNodeLabel = "DomainInfo";
		private static final String DomainInfoNameProperty = "name";
		private static final String DomainInfoLabel2ClassProperty = "label2ClassMap";
		private static final String DomainInfoFieldComponentTypeProperty = "componentTypeMap";
		private static final String DomainInfoConcreteFieldTypeProperty = "fieldTypeMap";
		private static final String KeyProperty = "key";
		private static final String MapKeyPrefix = "-k";
		private static final String MapValuePrefix = "-v";
		
		private String domainName;
		/**
		 * defines at which recursion occurrence building a query is stopped
		 */
		private int maxRecursionCount = 1;
		private IDBAccess dbAccess;
		private DomainState domainState;
		private Map<Class<?>, ObjectMapping> mappings;
		
		// for a root level type in a query, all possible variants (subclasses) must be considered
		// in order to build a query completely resolving all paths of all possible variants.
		// That is important, if the root level type is an interface, an abstract class or simple a super class
		// of the object that has actually been stored in the graph.
		private Map<Class<?>, CompoundObjectType> type2CompoundTypeMap;
		private DomainInfo domainInfo;

		private DomainAccessHandler(IDBAccess dbAccess, String domainName) {
			super();
			this.domainName = domainName;
			this.dbAccess = new DBAccessWrapper(dbAccess);
			this.domainState = new DomainState();
			this.mappings = new HashMap<Class<?>, ObjectMapping>();
			this.type2CompoundTypeMap = new HashMap<Class<?>, CompoundObjectType>();
		}
		
		<T> List<T> loadByIds(Class<T> domainObjectClass, long... ids) {
			List<T> resultList;
			
			InternalDomainAccess internalAccess = null;
			ClosureQueryContext context = new ClosureQueryContext(domainObjectClass);
			try {
				internalAccess = MappingUtil.internalDomainAccess.get();
				MappingUtil.internalDomainAccess.set(new InternalDomainAccess());
				updateMappingsIfNeeded();
				new ClosureCalculator().calculateClosureQuery(context);
				boolean repeat = context.matchClauses != null && context.matchClauses.size() > 0;
				
				if (repeat) { // has one or more match clauses
					resultList = loadByIdsWithMatches(domainObjectClass, context, ids);
				} else { // only simple start by id clauses are needed
					resultList = loadByIdsSimple(domainObjectClass, ids);
				}
			} catch(Throwable e) {
				if (!(e instanceof RuntimeException))
					throw new RuntimeException(e);
				else
					throw e;
			} finally {
				if (internalAccess != null)
					MappingUtil.internalDomainAccess.set(internalAccess);
				else
					MappingUtil.internalDomainAccess.remove();
			}

			return resultList;
		}
		
		List<JcError> store(List<Object> domainObjects) {
			UpdateContext context;
			InternalDomainAccess internalAccess = null;
			try {
				internalAccess = MappingUtil.internalDomainAccess.get();
				MappingUtil.internalDomainAccess.set(new InternalDomainAccess());
				context = this.updateLocalGraph(domainObjects);
			} finally {
				if (internalAccess != null)
					MappingUtil.internalDomainAccess.set(internalAccess);
				else
					MappingUtil.internalDomainAccess.remove();
			}
			
			List<JcError> errors = context.graph.store();
			if (errors.isEmpty()) {
				for (IRelation relat : context.relationsToRemove) {
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
		
		List<SyncInfo> getSyncInfos(List<Object> domainObjects) {
			List<SyncInfo> ret = new ArrayList<SyncInfo>(domainObjects.size());
			for (Object obj : domainObjects) {
				LoadInfo li = this.domainState.getLoadInfoFrom_Object2IdMap(obj);
				if (li != null)
					ret.add(new SyncInfo(li.getId(), li.getResolutionDepth()));
				else
					ret.add(new SyncInfo(-1, null));
			}
			return ret;
		}
		
		private void updateMappingsIfNeeded() {
			if (this.domainInfo == null) {
				DomainInfo dInfo = loadDomainInfoIfNeeded();
				Set<Class<?>> classes = dInfo.getAllStoredDomainClasses();
				Iterator<Class<?>> it = classes.iterator();
				while(it.hasNext()) {
					Class<?> clazz = it.next();
					ObjectMapping objectMapping = this.mappings.get(clazz);
					if (objectMapping == null) {
						objectMapping = DefaultObjectMappingCreator.createObjectMapping(clazz);
						this.mappings.put(clazz, objectMapping);
						this.updateCompoundTypeMapWith(clazz);
					}
				}
			}
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
				IRelation relat = context.relations.get(i);
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
					IRelation relat = context.relationsToRemove.get(i);
					// relation must exist in db
					Long id = this.domainState.getFrom_Relation2IdMap(relat);
					JcRelation r = new JcRelation(RelationPrefix.concat(String.valueOf(i)));
					removeStartClauses.add(START.relation(r).byId(id.longValue()));
					removeClauses.add(DO.DELETE(r));
				}
			}
			
			// domain objects to remove
			if (context.domainObjectsToRemove.size() > 0) {
				if (removeStartClauses == null) {
					removeStartClauses = new ArrayList<IClause>();
					removeClauses = new ArrayList<IClause>();
				}
				for (int i = 0; i < context.domainObjectsToRemove.size(); i++) {
					Object dobj = context.domainObjectsToRemove.get(i);
					// relation must exist in db
					Long id = this.domainState.getIdFrom_Object2IdMap(dobj);
					JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
					removeStartClauses.add(START.node(n).byId(id.longValue()));
					removeClauses.add(DO.DELETE(n));
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
				updateGraphFromObject(context.domainObjects.get(i), rNode);
			}
			
			for (int i = 0; i < context.relations.size(); i++) {
				GrRelation rRelation = null;
				if (relationIndexMap != null && relationIndexMap.get(i) != null) {
					rRelation = relationIndexMap.get(i).resultRelation;
				}
				if (rRelation == null) {
					IRelation relat = context.relations.get(i);
					rRelation = graph.createRelation(relat.getType(),
							context.domObj2Node.get(relat.getStart()), context.domObj2Node.get(relat.getEnd()));
					DomRelation2ResultRelation d2r = new DomRelation2ResultRelation();
					d2r.domRelation = relat;
					d2r.resultRelation = rRelation;
					context.domRelation2Relations.add(d2r);
				}
				updateGraphFromRelation(context.relations.get(i), rRelation);
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
				T obj = (T) this.domainState.getFrom_Id2ObjectMap(ids[i]);
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
				Util.printQueries(queries, "CLOSURE", Format.PRETTY_1);
				List<JcQueryResult> results = this.dbAccess.execute(queries);
				List<JcError> errors = Util.collectErrors(results);
				if (errors.size() > 0) {
					throw new JcResultException(errors);
				}
				Util.printResults(results, "CLOSURE", Format.PRETTY_1);
				for (int i = 0; i < queries.size(); i++) {
					id2QueryResult.put(queryIds.get(i), results.get(i));
				}
			}
			
			for (int i = 0; i < ids.length; i++) {
				T obj = id2Object.get(ids[i]);
				if (obj == null) { // need to load
					FillModelContext<T> fContext = new FillModelContext<T>(domainObjectClass,
							id2QueryResult.get(ids[i]), context.queryEndNodes, context.recursionExitNodes);
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
				T obj = (T) this.domainState.getFrom_Id2ObjectMap(ids[i]);
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

		@SuppressWarnings("unchecked")
		private <T> T createAndMapProperties(Class<T> domainObjectClass, GrNode rNode) {
			Class<? extends T> concreteClass;
			Class<?> clazz = findClassToInstantiateFor(rNode);
			if (clazz != null) {
				if (!domainObjectClass.isAssignableFrom(clazz)) {
					throw new RuntimeException(clazz.getName() + " cannot be assigned to domain object class: " +
							domainObjectClass.getName());
				} else {
					concreteClass = (Class<? extends T>) clazz;
				}
			} else {
				throw new RuntimeException("node with label(s): " + rNode.getLabels() + " cannot be mapped to domain object class: " +
						domainObjectClass.getName());
			}
			
			T domainObject = (T) createInstance(concreteClass);
			ObjectMapping objectMapping = getObjectMappingFor(domainObject);
			objectMapping.mapPropertiesToObject(domainObject, rNode);
			
			return domainObject;
		}
		
		private Class<?> findClassToInstantiateFor(GrNode rNode) {
			Iterator<GrLabel> it = rNode.getLabels().iterator();
			while (it.hasNext()) {
				Class<?> clazz = this.domainInfo.getClassForLabel(it.next().getName());
				if (clazz != null) {
					return clazz;
				}
			}
			return null;
		}

		private void updateGraphFromObject(Object domainObject, GrNode rNode) {
			// a mapping for this concrete class has definitly been stored earlier
			ObjectMapping objectMapping = mappings.get(domainObject.getClass());
			objectMapping.mapPropertiesFromObject(domainObject, rNode);
		}
		
		private void updateGraphFromRelation(IRelation relat, GrRelation rRelation) {
			Object key = null;
			if (relat instanceof KeyedRelation)
				key = ((KeyedRelation)relat).getKey();
			else if (relat instanceof KeyedRelationToChange)
				key = ((KeyedRelationToChange)relat).getNewKey();
			
			if (key != null) {
				GrProperty prop = rRelation.getProperty(KeyProperty);
				if (prop != null) {
					Object propValue = MappingUtil.convertFromProperty(prop.getValue(), key.getClass(), null);
					if (!key.equals(propValue))
						prop.setValue(key);
				} else
					rRelation.addProperty(KeyProperty, key);
			}
		}
		
		private ObjectMapping getCompoundObjectMappingFor(CompoundObjectType cType, Class<?> filter) {
			return new CompoundObjectMapping(cType, this.mappings, filter);
		}
		
		private ObjectMapping getObjectMappingFor(Object domainObject) {
			Class<?> clazz = domainObject.getClass();
			ObjectMapping objectMapping = this.mappings.get(clazz);
			if (objectMapping == null) {
				objectMapping = DefaultObjectMappingCreator.createObjectMapping(clazz);
				addObjectMappingForClass(clazz, objectMapping);
			}
			return objectMapping;
		}
		
		private void addObjectMappingForClass(Class<?> domainObjectClass, ObjectMapping objectMapping) {
			this.mappings.put(domainObjectClass, objectMapping);
			this.updateCompoundTypeMapWith(domainObjectClass);
			getAvailableDomainInfo().addClassLabel(domainObjectClass, objectMapping.getNodeLabelMapping().getLabel());
		}
		
		private DomainInfo loadDomainInfoIfNeeded() {
			if (this.domainInfo == null) {
				JcQuery query = ((DBAccessWrapper)this.dbAccess).createDomainInfoSyncQuery();
				JcQueryResult result = ((DBAccessWrapper)this.dbAccess)
						.delegate.execute(query);
				List<JcError> errors = Util.collectErrors(result);
				if (errors.isEmpty()) {
					((DBAccessWrapper)this.dbAccess)
						.updateDomainInfo(result);
				} else
					throw new JcResultException(errors);
			}
			return this.domainInfo;
		}
		
		private DomainInfo getAvailableDomainInfo() {
			DomainInfo ret;
			if (this.domainInfo != null)
				ret = this.domainInfo;
			else {
				ret = ((DBAccessWrapper)this.dbAccess).temporaryDomainInfo;
				if (ret == null) {
					ret = new DomainInfo(-1);
					((DBAccessWrapper)this.dbAccess).temporaryDomainInfo = ret;
				}
			}
			return ret;
		}
		
		private CompoundObjectType getCompoundTypeFor(Class<?> clazz) {
			CompoundObjectType cType = this.type2CompoundTypeMap.get(clazz);
			if (cType == null) {
				cType = new CompoundObjectType(clazz);
				Iterator<Class<?>> it = this.mappings.keySet().iterator();
				while(it.hasNext()) {
					Class<?> typ = it.next();
					if (clazz.isAssignableFrom(typ))
						cType.addType(typ);
				}
				this.type2CompoundTypeMap.put(clazz, cType);
			}
			return cType;
		}
		
		private void updateCompoundTypeMapWith(Class<?> clazz) {
			Iterator<Entry<Class<?>, CompoundObjectType>> it = this.type2CompoundTypeMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<Class<?>, CompoundObjectType> entry = it.next();
				if (entry.getKey().isAssignableFrom(clazz))
					entry.getValue().addType(clazz);
			}
		}
		
		private Object createInstance(Class<?> clazz) {
			Object ret;
			try {
				ret = clazz.newInstance();
			} catch(Throwable e) {
				throw new RuntimeException(e);
			}
			return ret;
		}

		/****************************************/
		private class DBAccessWrapper implements IDBAccess {

			private IDBAccess delegate;
			private DomainInfo temporaryDomainInfo;
			
			private DBAccessWrapper(IDBAccess delegate) {
				super();
				this.delegate = delegate;
			}

			@Override
			public JcQueryResult execute(JcQuery query) {
				JcQuery infoQuery = createDomainInfoSyncQuery();
				if (infoQuery != null) {
					List<JcQuery> queries = new ArrayList<JcQuery>(2);
					queries.add(query);
					queries.add(infoQuery);
					List<JcQueryResult> results = this.delegate.execute(queries);
					List<JcError> errors = Util.collectErrors(results);
					if (errors.isEmpty()) {
						updateDomainInfo(results.get(1));
					}
					return results.get(0);
				} else
					return this.delegate.execute(query);
			}

			@Override
			public List<JcQueryResult> execute(List<JcQuery> queries) {
				JcQuery infoQuery = createDomainInfoSyncQuery();
				if (infoQuery != null) {
					List<JcQuery> extQueries = new ArrayList<JcQuery>(queries.size() + 1);
					extQueries.addAll(queries);
					extQueries.add(infoQuery);
					List<JcQueryResult> results = this.delegate.execute(extQueries);
//					Util.printResults(results, "DOMAIN INFO", Format.PRETTY_1);
					List<JcError> errors = Util.collectErrors(results);
					if (errors.isEmpty()) {
						updateDomainInfo(results.get(queries.size()));
					}
					return results.subList(0, queries.size());
				} else
					return this.delegate.execute(queries);
			}

			@Override
			public List<JcError> clearDatabase() {
				return this.delegate.clearDatabase();
			}

			@Override
			public boolean isDatabaseEmpty() {
				return this.delegate.isDatabaseEmpty();
			}

			@Override
			public void close() {
				this.delegate.close();
			}

			private void updateDomainInfo(JcQueryResult result) {
				if (DomainAccessHandler.this.domainInfo == null) { // initial load
					JcNode info = new JcNode("info");
					List<GrNode> rInfos = result.resultOf(info);
					DomainInfo dInfo;
					if (rInfos.size() > 0) { // DomainInfo was found in the graph
						GrNode rInfo = rInfos.get(0);
						dInfo = new DomainInfo(rInfo.getId());
						dInfo.initFrom(rInfo);
						DomainAccessHandler.this.domainInfo = dInfo;
					} else
						DomainAccessHandler.this.domainInfo = new DomainInfo(-1);
				} else if (DomainAccessHandler.this.domainInfo.isChanged()) { // update info to graph
					DomainAccessHandler.this.domainInfo.graphUdated();
					if (DomainAccessHandler.this.domainInfo.nodeId == -1) {
						// new DomainInfo node was stored in the db
						// we have to set the returned node id
						JcNumber nid = new JcNumber("NID");
						BigDecimal rNid = result.resultOf(nid).get(0);
						DomainAccessHandler.this.domainInfo.nodeId = rNid.longValue();
					}
				}
				
				if (this.temporaryDomainInfo != null) {
					DomainAccessHandler.this.domainInfo.updateFrom(this.temporaryDomainInfo);
					this.temporaryDomainInfo = null;
				}
				
				// make sure that stiil pending changes are committed to the database
				// can happen in certain scenarios with the first domain query executed
				JcQuery query = this.createDomainInfoSyncQuery();
				if (query != null) {
					JcQueryResult uResult = this.delegate.execute(query);
					List<JcError> errors = Util.collectErrors(uResult);
					if (errors.isEmpty()) {
						updateDomainInfo(uResult);
					} else {
						throw new JcResultException(errors, "Error on update of Domain Info!");
					}
				}
			}

			private JcQuery createDomainInfoSyncQuery() {
				JcQuery query = null;
				if (DomainAccessHandler.this.domainInfo == null) { // initial load
					JcNode info = new JcNode("info");
					query = new JcQuery();
					query.setClauses(new IClause[] {
							MATCH.node(info).label(DomainInfoNodeLabel),
							WHERE.valueOf(info.property(DomainInfoNameProperty))
								.EQUALS(DomainAccessHandler.this.domainName),
							RETURN.value(info)
					});
				} else if (DomainAccessHandler.this.domainInfo.isChanged()) { // update info to graph
					List<String> class2LabelList = DomainAccessHandler.this.domainInfo.getLabel2ClassNameStringList();
					List<String> fieldComponentTypeList =
							DomainAccessHandler.this.domainInfo.getFieldComponentTypeStringList();
					List<String> concreteFieldTypeList =
							DomainAccessHandler.this.domainInfo.getConcreteFieldTypeStringList();
					JcNode info = new JcNode("info");
					query = new JcQuery();
					if (DomainAccessHandler.this.domainInfo.nodeId != -1) { // DominInfo was loaded from graph
						query.setClauses(new IClause[] {
								START.node(info).byId(DomainAccessHandler.this.domainInfo.nodeId),
								DO.SET(info.property(DomainInfoLabel2ClassProperty)).to(class2LabelList),
								DO.SET(info.property(DomainInfoFieldComponentTypeProperty)).to(fieldComponentTypeList),
								DO.SET(info.property(DomainInfoConcreteFieldTypeProperty)).to(concreteFieldTypeList)
						});
					} else { // new DomainInfo node must be stored in the db
						JcNumber nid = new JcNumber("NID");
						query.setClauses(new IClause[] {
								CREATE.node(info).label(DomainInfoNodeLabel)
									.property(DomainInfoNameProperty).value(DomainAccessHandler.this.domainName)
									.property(DomainInfoLabel2ClassProperty).value(class2LabelList)
									.property(DomainInfoFieldComponentTypeProperty).value(fieldComponentTypeList)
									.property(DomainInfoConcreteFieldTypeProperty).value(concreteFieldTypeList),
								RETURN.value(info.id()).AS(nid)
						});
					}
				}
				if (query != null) {
//					Util.printQuery(query, "DOMAIN INFO", Format.PRETTY_1);
				}
				return query;
			}
			
		}
	}
	
	/**********************************************/
	private class ClosureCalculator {
		
		private <T> void fillModel(FillModelContext<T> context) {
			Step step = new Step();
			step.fillModel(context, null, null);
		}
		
		private void calculateClosureQuery(ClosureQueryContext context) {
			boolean isDone = false;
			Step step = new Step();
			int idx = -1;
			while (!isDone) {
				idx++;
				context.clauseRepetitionNumber = idx;
				isDone = step.calculateQuery(null, context);
				if (context.currentMatchClause != null) {
					context.addMatchClause(context.currentMatchClause);
					context.currentMatchClause = null;
				}
			}
		}

		private void calculateClosure(List<Object> domainObjects, UpdateContext context) {
			for (Object domainObject : domainObjects) {
				recursiveCalculateClosure(domainObject, context, false); // don't delete
			}
		}
		
		/**
		 * @param domainObject
		 * @param context
		 * @param prepareToDelete if true, delete outgoing relations from domainObject that later on itself will be deleted
		 */
		@SuppressWarnings("unchecked")
		private void recursiveCalculateClosure(Object domainObject, UpdateContext context,
				boolean prepareToDelete) {
			if (!context.domainObjects.contains(domainObject)) { // avoid infinite loops
				context.domainObjects.add(domainObject);
				ObjectMapping objectMapping = domainAccessHandler.getObjectMappingFor(domainObject);
				Iterator<FieldMapping> it = objectMapping.fieldMappingsIterator();
				while (it.hasNext()) {
					FieldMapping fm = it.next();
					Object obj = fm.getObjectNeedingRelation(domainObject);
					if (obj != null && !prepareToDelete) { // definitly need relation
						if (obj instanceof Collection<?>) { // collection with non-simple elements,
																					// we won't reach this spot with empty collections
							Collection<?> coll = (Collection<?>)obj;
							handleListInClosureCalc(coll, domainObject, context, fm);
						} else if (obj instanceof Map<?, ?>) {
							Map<Object, Object> map = (Map<Object, Object>)obj;
							handleMapInClosureCalc(map, domainObject, context, fm);
						}else {
							handleObjectInClosureCalc(obj, domainObject, context, fm);
						}
					} else {
						if (fm.needsRelation()) { // in case obj == null because it was not set
							// no relation --> check if an old relation needs to be removed
							if (fm.isCollection() || fm.isMap()) {
								// remove multiple relations if they exist
								List<MapEntry> mapEntriesToRemove = handleKeyedRelationsModification(null, context,
										new SourceFieldKey(domainObject, fm.getFieldName()));
								removeObjectsIfNeeded(context, mapEntriesToRemove);
							} else {
								// remove just a single relation if it exists
								IRelation relat = domainAccessHandler.domainState.findRelation(domainObject,
										fm.getPropertyOrRelationName());
								if (relat != null) {
									context.relationsToRemove.add(relat);
								}
							}
						}
					}
				}
			}
		}
		
		private void handleMapInClosureCalc(Map<Object, Object> map, Object domainObject,
				UpdateContext context, FieldMapping fm) {
			MapTerminator mapTerminator = new MapTerminator();
			String typ = fm.getPropertyOrRelationName();
			Map<SourceField2TargetKey, List<KeyedRelation>> keyedRelations =
					new HashMap<SourceField2TargetKey, List<KeyedRelation>>();
			List<MapEntry> mapEntries = new ArrayList<MapEntry>();
			// store concrete type in DomainInfo
			String classField = fm.getClassFieldName(null);
			MappingUtil.internalDomainAccess.get()
				.addConcreteFieldType(classField, map.getClass());
			Iterator<Entry<Object, Object>> it = map.entrySet().iterator();
			while(it.hasNext()) {
				Entry<Object, Object> entry = it.next();
				Object val = entry.getValue();
				Object key = entry.getKey();
				boolean keyMapsToProperty = MappingUtil.mapsToProperty(key.getClass());
				boolean valMapsToProperty = MappingUtil.mapsToProperty(val.getClass());
				Object target;
				Object relationKey;
				if (keyMapsToProperty) {
					target = valMapsToProperty ? mapTerminator : val;
					relationKey = entry.getKey();
				} else { // complex key always needs a MapEntry
					// handle it like a list for correct removal of removed entries
					MapEntry mapEntry;
					if (valMapsToProperty)
						mapEntry = new MapEntrySimple(key, val);
					else
						mapEntry = new MapEntryComplex(key, val);
					mapEntries.add(mapEntry);
					target = mapEntry;
					relationKey = mapEntry.hashCode();
				}
				SourceField2TargetKey s2tKey =
						new SourceField2TargetKey(domainObject, fm.getFieldName(), target);
				List<KeyedRelation> relats = keyedRelations.get(s2tKey);
				if (relats == null) {
					relats = new ArrayList<KeyedRelation>();
					keyedRelations.put(s2tKey, relats);
				}
				relats.add(new KeyedRelation(typ, relationKey, domainObject, target));
				
				// store component types in DomainInfo
				MappingUtil.internalDomainAccess.get()
					.addFieldComponentType(fm.getClassFieldName(DomainAccessHandler.MapKeyPrefix),
							entry.getKey().getClass());
				MappingUtil.internalDomainAccess.get()
				.addFieldComponentType(fm.getClassFieldName(DomainAccessHandler.MapValuePrefix),
						entry.getValue().getClass());
			}
			
			boolean complexKeys = mapEntries.size() > 0;
			if (complexKeys) {
				List<MapEntry> mapEntriesToRemove = handleKeyedRelationsModification(keyedRelations, context,
						new SourceFieldKey(domainObject, fm.getFieldName()));
				for (MapEntry mapEntry : mapEntries) {
					recursiveCalculateClosure(mapEntry, context, false);
				}
				removeObjectsIfNeeded(context, mapEntriesToRemove);
			} else { // simple keys
				handleKeyedRelationsModification(keyedRelations, context,
						new SourceFieldKey(domainObject, fm.getFieldName()));
				 it = map.entrySet().iterator();
				while(it.hasNext()) {
					Entry<Object, Object> entry = it.next();
					Object val = entry.getValue();
					boolean mapsToProperty = MappingUtil.mapsToProperty(val.getClass());
					if (!mapsToProperty)
						recursiveCalculateClosure(val, context, false); // don't delete
				}
			}
		}
		
		private void handleListInClosureCalc(Collection<?> coll, Object domainObject,
				UpdateContext context, FieldMapping fm) {
			String typ = fm.getPropertyOrRelationName();
			Map<SourceField2TargetKey, List<KeyedRelation>> keyedRelations =
					new HashMap<SourceField2TargetKey, List<KeyedRelation>>();
			// store concrete type in DomainInfo
			String classField = fm.getClassFieldName(null);
			MappingUtil.internalDomainAccess.get()
				.addConcreteFieldType(classField, coll.getClass());
			int idx = 0;
			for (Object elem : coll) {
				SourceField2TargetKey key =
						new SourceField2TargetKey(domainObject, fm.getFieldName(), elem);
				List<KeyedRelation> relats = keyedRelations.get(key);
				if (relats == null) {
					relats = new ArrayList<KeyedRelation>();
					keyedRelations.put(key, relats);
				}
				relats.add(new KeyedRelation(typ, idx, domainObject, elem));
				// store component type in DomainInfo
				MappingUtil.internalDomainAccess.get()
					.addFieldComponentType(classField, elem.getClass());
				idx++;
			}
			
			handleKeyedRelationsModification(keyedRelations, context,
					new SourceFieldKey(domainObject, fm.getFieldName()));
			for (Object elem : coll) {
				recursiveCalculateClosure(elem, context, false); // don't delete
			}
		}
		
		private void handleObjectInClosureCalc(Object relatedObject, Object domainObject,
				UpdateContext context, FieldMapping fm) {
			IRelation relat = new Relation(fm.getPropertyOrRelationName(), domainObject, relatedObject);
			if (!domainAccessHandler.domainState.existsRelation(relat)) {
				context.relations.add(relat); // relation not in db
				
				// check if an old relation (for the same field but to another object, which is now replaced
				// by the new relation) needs to be removed.
				relat = domainAccessHandler.domainState.findRelation(domainObject,
						fm.getPropertyOrRelationName());
				if (relat != null) {
					context.relationsToRemove.add(relat);
				}
			}
			// store concrete type in DomainInfo
			String classField = fm.getClassFieldName(null);
			MappingUtil.internalDomainAccess.get()
				.addConcreteFieldType(classField, relatedObject.getClass());
			recursiveCalculateClosure(relatedObject, context, false); // don't delete
		}
		
		private List<MapEntry> handleKeyedRelationsModification(Map<SourceField2TargetKey, List<KeyedRelation>> keyedRelations,
				UpdateContext context, SourceFieldKey fieldKey) {
			List<KeyedRelation> allExistingRels = new ArrayList<KeyedRelation>();
			List<KeyedRelation> allExist = domainAccessHandler.domainState.getKeyedRelations(fieldKey);
			if (allExist != null)
				allExistingRels.addAll(allExist);
			
			if (keyedRelations != null) {
				Iterator<Entry<SourceField2TargetKey, List<KeyedRelation>>> it = keyedRelations.entrySet().iterator();
				while(it.hasNext()) {
					Entry<SourceField2TargetKey, List<KeyedRelation>> entry = it.next();
					List<KeyedRelation> existingRels =
							domainAccessHandler.domainState.getKeyedRelations(entry.getKey());
					RelationsToModify toModify = calculateKeyedRelationsToModify(entry.getValue(), existingRels, allExistingRels);
					context.relations.addAll(toModify.toChange);
					context.relations.addAll(toModify.toCreate);
					context.relationsToRemove.addAll(toModify.toRemove);
				}
			}
			// in allExistingRels we have those which previously existed but don't exist in the collection or map any more
			context.relationsToRemove.addAll(allExistingRels);
			List<MapEntry> mapEntriesToRemove = new ArrayList<MapEntry>();
			for(KeyedRelation kRel : allExistingRels) { // they are to be removed
				Object end = kRel.getEnd();
				if (end instanceof MapEntry) { // remove MapEntry
					// TODO is the check really needed
					if (!mapEntriesToRemove.contains(end))
						mapEntriesToRemove.add((MapEntry) end);
				}
			}
			return mapEntriesToRemove;
		}
		
		private RelationsToModify calculateKeyedRelationsToModify(List<KeyedRelation> actual,
				List<KeyedRelation> existingInGraph, List<KeyedRelation> allExistingInGraph) {
			List<KeyedRelation> act = new ArrayList<KeyedRelation>();
			act.addAll(actual);
			List<KeyedRelation> existingOnes = new ArrayList<KeyedRelation>();
			if (existingInGraph != null)
				existingOnes.addAll(existingInGraph);
			List<KeyedRelation> unchanged = new ArrayList<KeyedRelation>();
			for (KeyedRelation exists : existingOnes) {
				for (KeyedRelation iRel : act) {
					if (exists.equals(iRel)) {
						unchanged.add(iRel);
						break;
					}
				}
			}
			for (KeyedRelation iRel : unchanged) {
				act.remove(iRel);
				existingOnes.remove(iRel);
				allExistingInGraph.remove(iRel);
			}
			// now we have filtered out those which do not need to be changed, added or removed
			
			int maxRemoveIndex = -1;
			List<KeyedRelationToChange> toChange = new ArrayList<KeyedRelationToChange>();
			int idx = 0;
			for (KeyedRelation iRel : act) {
				if (existingOnes.size() > idx) {
					toChange.add(new KeyedRelationToChange(existingOnes.get(idx), iRel.getKey()));
					maxRemoveIndex = idx;
				} else
					break;
				idx++;
			}
			if (maxRemoveIndex != -1) {
				for (int i = maxRemoveIndex; i >= 0; i--) {
					KeyedRelation removed = existingOnes.remove(i);
					act.remove(i);
					allExistingInGraph.remove(removed);
				}
			}
			// now we have filtered out those which need to be changed (they will get a new key)
			// they are in list 'toChange'.
			
			RelationsToModify ret = new RelationsToModify();
			ret.toChange = toChange;
			
			// the actual ones which exist in the graph and the actual ones which can be created
			// by changing existing ones, have been removed from act.
			// So in 'act' there are those who need to be created,
			// We now find them in list 'toCreate'
			ret.toCreate = act;
			
			// Those which really need to be removed are now found in list 'toRemove'
			ret.toRemove = existingOnes;
			
			// existing ones are already added to toRemove, avoid doing it again later
			allExistingInGraph.removeAll(existingOnes);
			
			return ret;
		}
		
		private void removeObjectsIfNeeded(UpdateContext context, List<MapEntry> mapEntriesToRemove) {
			if (mapEntriesToRemove.size() > 0) {
				for(Object obj : mapEntriesToRemove) { // remove MapEntry objects
					recursiveCalculateClosure(obj, context, true);
					context.domainObjectsToRemove.add(obj);
				}
			}
		}
		
		/**********************************************/
		private class RelationsToModify {
			private List<KeyedRelation> toCreate;
			private List<KeyedRelation> toRemove;
			private List<KeyedRelationToChange> toChange;
		}
		
		/**********************************************/
		private class Step {
			
			private int subPathIndex = -1;
			private Step next;
			
			/**
			 * @param context
			 * @param fm may be null
			 * @param nodeName may be null
			 * @return true, if this leads to a null value
			 */
			@SuppressWarnings("unchecked")
			private <T> boolean fillModel(FillModelContext<T> context, FieldMapping fm,
					String nodeName) {
				int prevClauseRepetitionNumber = context.clauseRepetitionNumber;
				boolean isNullNode = false;
				String nnm;
				if (nodeName != null)
					nnm = nodeName;
				else
					nnm = this.buildNodeOrRelationName(context.path,
							DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber);
				context.setTerminatesClause(nnm);
				
				CompoundObjectType compoundType;
				if (fm == null) // root type
					compoundType = domainAccessHandler.getCompoundTypeFor(context.domainObjectClass);
				else {
					String classFieldName = fm.getClassFieldName(null);
					compoundType = MappingUtil.internalDomainAccess.get()
							.getConcreteFieldType(classFieldName);
				}
				Class<? extends Object> pureType = fm != null ? fm.getFieldType() : context.domainObjectClass;
				
				if (compoundType != null) { // else this field cannot have been stored in the graph earlier
					// prepare for navigation to next node
					context.path.add(new PathElement(pureType));
					
					boolean resolveDeep = true;
					if (context.recursionExitNodes.contains(nnm)) { // exit recursion
						resolveDeep = false;
					}
					
					boolean isCollection = Collection.class.isAssignableFrom(pureType);
					//ObjectMapping objectMapping;
					Collection<Object> collection = null;
					if (isCollection) {
						String classFieldName = fm.getClassFieldName(null);
						// select the first concrete type in the CompoundType to instantiate.
						// Most certainly there will only be one type in the CompoundType,
						// anyway it must be instantiable as it has earlier been stored to the graph
						collection = (Collection<Object>) domainAccessHandler.createInstance(MappingUtil.internalDomainAccess.get()
								.getConcreteFieldType(classFieldName).getType());
						compoundType = MappingUtil.internalDomainAccess.get()
								.getFieldComponentType(classFieldName);
					}
					
					//objectMapping = domainAccessHandler.getObjectMappingFor(compoundType);
					
					JcNode n = new JcNode(nnm);
					List<GrNode> resList = context.qResult.resultOf(n);
					Object domainObject = null;
					if (resList.size() > 0) { // at least one result node exists for this pattern
						int initialMaxClauseRepetitionNumber = context.maxClauseRepetitionNumber;
						for (GrNode rNode : resList) {
							if (rNode != null) { // null values are supported
								boolean performMapping = false;
								boolean mapProperties = true;
								// check if a domain object has already been mapped to this node
								domainObject = domainAccessHandler.domainState.getFrom_Id2ObjectMap(rNode.getId());
								
								if (domainObject == null) {
									Class<?> clazz = domainAccessHandler.findClassToInstantiateFor(rNode);
									domainObject = domainAccessHandler.createInstance(clazz);
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
									// need to reset if we iterate through a list
									context.maxClauseRepetitionNumber = initialMaxClauseRepetitionNumber;
									ObjectMapping objectMapping = domainAccessHandler
											.getCompoundObjectMappingFor(compoundType, domainObject.getClass());
									Iterator<FieldMapping> it = objectMapping.fieldMappingsIterator();
									int idx = 0;
									while (it.hasNext()) {
										FieldMapping fMap = it.next();
										idx++; // index starts with 1 so as not to mix with the root node (n_0)
										if (!objectMapping.shouldPerformFieldMapping(fMap)) {
											if (fMap.needsRelation() && resolveDeep) {
												calculateMaxClauseRepetitionNumber(context, fMap, idx);
											}
											continue;
										}
										if (fMap.needsRelation() && resolveDeep) {
											context.currentObject = null;
											PathElement pe = context.getLastPathElement();
											pe.fieldIndex = idx;
											context.clauseRepetitionNumber = context.maxClauseRepetitionNumber;
											pe.fieldName = fMap.getFieldName();
											String ndName = this.buildNodeOrRelationName(context.path,
													DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber);
											boolean nodeIsNull = false; // to not accidentially enter test for empty collection
											boolean needToRepeat = false;
											if (isValidNodeName(ndName, context))
												nodeIsNull = this.fillModel(context, fMap, ndName);
											else
												needToRepeat = true; // need to repeat
											context.alreadyTested.clear();
											while(needToRepeat && morePathsToTest(context, fMap, idx)) {
												context.currentObject = null;
												pe = context.getLastPathElement();
												pe.fieldIndex = idx;
												context.clauseRepetitionNumber = context.maxClauseRepetitionNumber;
												pe.fieldName = fMap.getFieldName();
												ndName = this.buildNodeOrRelationName(context.path,
														DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber);
												needToRepeat = false;
												if (isValidNodeName(ndName, context))
													nodeIsNull = this.fillModel(context, fMap, ndName);
												else {
													if (moreClausesAvailable(ndName, context))
														needToRepeat = true; // need to repeat
												}
											}
											if (!nodeIsNull && context.currentObject != null) {
												fMap.setField(domainObject, context.currentObject);
												String rnm = this.buildNodeOrRelationName(context.path,
														DomainAccessHandler.RelationPrefix, context.clauseRepetitionNumber);
												JcRelation r = new JcRelation(rnm);
												List<GrRelation> relList = context.qResult.resultOf(r);
												// relation(s) must exist, because the related object(s) exists
												if (context.currentObject instanceof Collection) {
													addRelations(domainObject, relList, (Collection<?>)context.currentObject,
															fMap.getPropertyOrRelationName());
												} else {
													GrRelation rel = relList.get(0);
													domainAccessHandler.domainState.add_Id2Relation(
															new Relation(fMap.getPropertyOrRelationName(),
																	domainObject,
																	context.currentObject), rel.getId());
												}
											} else if (nodeIsNull && fMap.isCollection()) {
												// test for empty collection, which evantually was mapped to a property
												fMap.mapPropertyToField(domainObject, rNode);
											}
											context.updateMaxClauseRepetitionNumber();
										} else {
											if (mapProperties)
												fMap.mapPropertyToField(domainObject, rNode);
										}
									}
								}
								if (isCollection)
									collection.add(domainObject);
							} else {
								isNullNode = true;
							}
						}
					}
					
					// set the object mapped to the actual field (fm)
					if (isCollection)
						context.currentObject = collection;
					else
						context.currentObject = domainObject;
					context.path.remove(context.path.size() - 1); // remove the last one
				}
				context.clauseRepetitionNumber = prevClauseRepetitionNumber;
				return isNullNode;
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			private void addRelations(Object start, List<GrRelation> relList, Collection coll,
					String relType) {
				Iterator<?> cit = coll.iterator();
				Iterator<GrRelation> rit = relList.iterator();
				List<KeyedRelation> toResort = new ArrayList<KeyedRelation>();
				long prevIndex = -1;
				boolean needResort = false;
				while(rit.hasNext()) {
					GrRelation rel = rit.next();
					Object domainObject = cit.next();
					GrProperty prop = rel.getProperty(DomainAccessHandler.KeyProperty);
					long idx = (Long)MappingUtil.convertFromProperty(prop.getValue(), Long.class, null);
					if (idx <= prevIndex)
						needResort = true;
					prevIndex = idx;
					KeyedRelation irel = new KeyedRelation(relType, idx, start, domainObject);
					domainAccessHandler.domainState.add_Id2Relation(irel, rel.getId());
					toResort.add(irel);
				}
				
				if (needResort) {
					Collections.sort(toResort, new Comparator<KeyedRelation>() {
						@Override
						public int compare(KeyedRelation o1,
								KeyedRelation o2) {
							return Long.compare(((Long)o1.getKey()).longValue(),
									((Long)o2.getKey()).longValue());
						}
					});
					coll.clear();
					for (KeyedRelation irel : toResort) {
						coll.add(irel.getEnd());
					}
				}
			}
			
			private <T> boolean isValidNodeName(String nodeName, FillModelContext<T> context) {
				for (String endNode : context.queryEndNodes) {
					if (endNode.indexOf(nodeName) == 0)
						return true;
				}
				return false;
			}
			
			private <T> boolean moreClausesAvailable(String nodeName, FillModelContext<T> context) {
				int idx1 = nodeName.indexOf('_') + 1;
				int idx2 = nodeName.indexOf('_', idx1);
				String clauseNum = nodeName.substring(idx1, idx2);
				return Integer.parseInt(clauseNum) < context.queryEndNodes.size() - 1;
			}
			
			private <T> void calculateMaxClauseRepetitionNumber(FillModelContext<T> context, FieldMapping fMap,
					int fieldIndex) {
				String toCompare = pathToTest(context, fMap, fieldIndex);
				int increment = 0;
				for (String nodeName : context.queryEndNodes) {
					if (!context.alreadyTested.contains(nodeName)) {
						int idx = nodeName.indexOf('_', nodeName.indexOf('_') + 1);
						String other = nodeName.substring(idx + 1);
						if (other.indexOf(toCompare) == 0) { // is a node in the sub path
							increment++;
						}
					}
				}
				context.maxClauseRepetitionNumber = context.maxClauseRepetitionNumber + increment;
				return;
			}
			
			private <T> boolean morePathsToTest(FillModelContext<T> context, FieldMapping fMap,
					int fieldIndex) {
				String toCompare = pathToTest(context, fMap, fieldIndex);
				boolean goOn = false;
				for (String nodeName : context.queryEndNodes) {
					// step to where the last test ended
					if (!goOn && !context.alreadyTested.contains(nodeName)) {
						goOn = true;
					}
					if (goOn) {
						int idx = nodeName.indexOf('_', nodeName.indexOf('_') + 1);
						String other = nodeName.substring(idx + 1);
						if (other.indexOf(toCompare) == 0) { // is a node in the sub path
							context.alreadyTested.add(nodeName);
							context.maxClauseRepetitionNumber++;
							return true;
						}
					}
				}
				return false;
			}
			
			private <T> String pathToTest(FillModelContext<T> context, FieldMapping fMap,
					int fieldIndex) {
				PathElement pe = context.getLastPathElement();
				pe.fieldIndex = fieldIndex;
				context.clauseRepetitionNumber = context.maxClauseRepetitionNumber;
				pe.fieldName = fMap.getFieldName();
				String nnm = this.buildNodeOrRelationName(context.path,
						DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber);
				int idx = nnm.indexOf('_', nnm.indexOf('_') + 1);
				return nnm.substring(idx + 1);
			}
			
			/**
			 * @param fm, null for the root step
			 * @param context
			 * @return true, if calculating query for the current path is done
			 */
			private boolean calculateQuery(FieldMapping fm, ClosureQueryContext context) {
				boolean ret = true;
				CompoundObjectType compoundType;
				if (fm == null) // root type
					compoundType = domainAccessHandler.getCompoundTypeFor(context.domainObjectClass);
				else {
					String classFieldName = fm.getClassFieldName(null);
					compoundType = MappingUtil.internalDomainAccess.get()
							.getConcreteFieldType(classFieldName);
				}
				Class<? extends Object> pureType = fm != null ? fm.getFieldType() : context.domainObjectClass;
				
				if (fm != null) // don't add a match for the start node itself
					this.addToQuery(fm, context); // navigate to this node
				
				boolean resolveDeep = true;
				boolean walkedToIndex = this.subPathIndex == -1; // visiting the first time
				boolean subPathWalked = false;
				// do the following check to avoid infinite loops
				if (walkedToIndex) { // we are visiting the first time
					if (context.getRecursionCount() >= domainAccessHandler.maxRecursionCount)
						resolveDeep = false;
					// to make sure, that the node itself is added as an end  node to the query
					if (fm != null)
						subPathWalked = true;
				}
				
				// prepare for navigation to next node
				context.path.add(new PathElement(pureType));
				
				boolean isCollection = Collection.class.isAssignableFrom(pureType);
				if (isCollection) {
					String classFieldName = fm.getClassFieldName(null);
					compoundType = MappingUtil.internalDomainAccess.get()
							.getFieldComponentType(classFieldName);
				}
				
				boolean terminatesClause = true;
				if (compoundType != null) { // else no instance of that class has yet been stored in the database
					ObjectMapping objectMapping = domainAccessHandler.getCompoundObjectMappingFor(compoundType, null);
					Iterator<FieldMapping> it = objectMapping.fieldMappingsIterator();
					int idx = 0;
					while (it.hasNext()) {
						FieldMapping fMap = it.next();
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
								terminatesClause = false;
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
				}
				context.path.remove(context.path.size() - 1); // remove the last one
				if (!resolveDeep || terminatesClause) { // clause ends here
					String nm = this.buildNodeOrRelationName(context.path,
							DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber);
					context.queryEndNodes.add(nm);
					if (!resolveDeep) // exits a recursion
						context.recursionExitNodes.add(nm);
				}
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
						DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber));
				JcRelation r = new JcRelation(this.buildNodeOrRelationName(context.path,
						DomainAccessHandler.RelationPrefix, context.clauseRepetitionNumber));
				context.currentMatchClause.relation(r).out().type(fm.getPropertyOrRelationName())
				.node(n);
			}
			
			private String buildNodeOrRelationName(List<PathElement> path, String prefix,
					int clauseNumber) {
				// format of node name: n_clauseNumber_idx1_idx2_idx3...
				StringBuilder sb = new StringBuilder();
				sb.append(prefix);
				if (path.size() > 0) {
					sb.append(clauseNumber);
					sb.append('_');
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
		
		/**********************************************/
		private class MapTerminator {
			
		}
	}
	
	/***********************************/
	private class FillModelContext<T> {
		private JcQueryResult qResult;
		private Class<T> domainObjectClass;
		private T domainObject;
		private Object currentObject;
		private List<PathElement> path;
		private List<String> queryEndNodes;
		private List<String> recursionExitNodes;
		private int clauseRepetitionNumber;
		private int maxClauseRepetitionNumber;
		private boolean terminatesClause;
		private List<String> alreadyTested;
		
		FillModelContext(Class<T> domainObjectClass, JcQueryResult qResult,
				List<String> queryEndNds, List<String> recursionExitNds) {
			super();
			this.domainObjectClass = domainObjectClass;
			this.qResult = qResult;
			this.path = new ArrayList<PathElement>();
			this.queryEndNodes = queryEndNds;
			this.recursionExitNodes = recursionExitNds;
			this.clauseRepetitionNumber = 0;
			this.maxClauseRepetitionNumber = 0;
			this.terminatesClause = false;
			this.alreadyTested = new ArrayList<String>();
		}
		
		private PathElement getLastPathElement() {
			if (this.path.size() > 0)
				return this.path.get(this.path.size() - 1);
			return null;
		}
		
		private void setTerminatesClause(String nodeName) {
			this.terminatesClause = this.queryEndNodes.contains(nodeName);
		}
		
		private void updateMaxClauseRepetitionNumber() {
			if (this.terminatesClause) {
				this.maxClauseRepetitionNumber++;
				this.terminatesClause = false;
			}
		}
	}
	
	/***********************************/
	private class ClosureQueryContext {
		private Class<?> domainObjectClass;
		private List<IClause> matchClauses;
		private Node currentMatchClause;
		private List<PathElement> path;
		private List<String> queryEndNodes;
		private List<String> recursionExitNodes;
		private int clauseRepetitionNumber;
		
		private ClosureQueryContext(Class<?> domainObjectClass) {
			super();
			this.domainObjectClass = domainObjectClass;
			this.path = new ArrayList<PathElement>();
			this.queryEndNodes = new ArrayList<String>();
			this.recursionExitNodes = new ArrayList<String>();
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
		private List<IRelation> relations = new ArrayList<IRelation>();
		private List<IRelation> relationsToRemove = new ArrayList<IRelation>();
		private List<Object> domainObjectsToRemove = new ArrayList<Object>();
		private Map<Object, GrNode> domObj2Node;
		private List<DomRelation2ResultRelation> domRelation2Relations;
		private Graph graph;
	}
	
	/***********************************/
	private class DomRelation2ResultRelation {
		private IRelation domRelation;
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
	private class DomainInfo {
		
		private boolean changed;
		private long nodeId;
		private Map<String, Class<?>> label2ClassMap;
		private Map<Class<?>, String> class2labelMap;
		private Map<String, CompoundObjectType> fieldComponentTypeMap;
		private Map<String, CompoundObjectType> concreteFieldTypeMap;
		private DomainInfo(long nid) {
			super();
			this.changed = false;
			this.nodeId = nid;
			this.label2ClassMap = new HashMap<String, Class<?>>();
			this.class2labelMap = new HashMap<Class<?>, String>();
			this.fieldComponentTypeMap = new HashMap<String, CompoundObjectType>();
			this.concreteFieldTypeMap = new HashMap<String, CompoundObjectType>();
		}

		@SuppressWarnings("unchecked")
		private void initFrom(GrNode rInfo) {
			GrProperty prop = rInfo.getProperty(DomainAccessHandler.DomainInfoLabel2ClassProperty);
			if (prop != null) {
				List<String> val = (List<String>) prop.getValue();
				for (String str : val) {
					String[] c2l = str.split("=");
					try {
						Class<?> clazz = Class.forName(c2l[1]);
						this.addClassLabel(clazz, c2l[0]);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			}
			
			prop = rInfo.getProperty(DomainAccessHandler.DomainInfoFieldComponentTypeProperty);
			if (prop != null) {
				List<String> val = (List<String>) prop.getValue();
				for (String str : val) {
					String[] c2l = str.split("=");
					String[] classes = c2l[1].split(CompoundObjectType.SEPARATOR);
					for (String cls : classes) {
						try {
							Class<?> clazz = Class.forName(cls);
							this.addFieldComponentType(c2l[0], clazz);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			
			prop = rInfo.getProperty(DomainAccessHandler.DomainInfoConcreteFieldTypeProperty);
			if (prop != null) {
				List<String> val = (List<String>) prop.getValue();
				for (String str : val) {
					String[] c2l = str.split("=");
					String[] classes = c2l[1].split(CompoundObjectType.SEPARATOR);
					for (String cls : classes) {
						try {
							Class<?> clazz = Class.forName(cls);
							this.addConcreteFieldType(c2l[0], clazz);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			this.changed = false;
		}

		private void updateFrom(DomainInfo dInfo) {
			Iterator<Entry<Class<?>, String>> it = dInfo.class2labelMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Class<?>, String> entry = it.next();
				this.addClassLabel(entry.getKey(), entry.getValue());
			}
			
			Iterator<Entry<String, CompoundObjectType>> it2 = dInfo.fieldComponentTypeMap.entrySet().iterator();
			while(it2.hasNext()) {
				Entry<String, CompoundObjectType> entry = it2.next();
				Iterator<CompoundObjectType> it3 = entry.getValue().typeIterator();
				while(it3.hasNext()) {
					CompoundObjectType cType = it3.next();
					this.addFieldComponentType(entry.getKey(), cType.getType());
				}
			}
			
			it2 = dInfo.concreteFieldTypeMap.entrySet().iterator();
			while(it2.hasNext()) {
				Entry<String, CompoundObjectType> entry = it2.next();
				Iterator<CompoundObjectType> it3 = entry.getValue().typeIterator();
				while(it3.hasNext()) {
					CompoundObjectType cType = it3.next();
					this.addConcreteFieldType(entry.getKey(), cType.getType());
				}
			}
		}
		
		private boolean isChanged() {
			return changed;
		}
		
		private void graphUdated() {
			this.changed = false;
		}
		
		private void addClassLabel(Class<?> clazz, String label) {
			if (!this.class2labelMap.containsKey(clazz)) {
				this.class2labelMap.put(clazz, label);
				this.label2ClassMap.put(label, clazz);
				this.changed = true;
			}
		}
		
		private void addFieldComponentType(String classField, Class<?> clazz) {
			CompoundObjectType cType = this.fieldComponentTypeMap.get(classField);
			if (cType == null) {
				cType = new CompoundObjectType(clazz);
				this.fieldComponentTypeMap.put(classField, cType);
				this.changed = true;
			} else {
				boolean added = cType.addType(clazz);
				this.changed = this.changed || added;
			}
		}
		
		private CompoundObjectType getFieldComponentType(String classField) {
			return this.fieldComponentTypeMap.get(classField);
		}
		
		private void addConcreteFieldType(String classField, Class<?> clazz) {
			CompoundObjectType cType = this.concreteFieldTypeMap.get(classField);
			if (cType == null) {
				cType = new CompoundObjectType(clazz);
				this.concreteFieldTypeMap.put(classField, cType);
				this.changed = true;
			} else {
				boolean added = cType.addType(clazz);
				this.changed = this.changed || added;
			}
		}
		
		private CompoundObjectType getConcreteFieldType(String classField) {
			return this.concreteFieldTypeMap.get(classField);
		}
		
		private Class<?> getClassForLabel(String label) {
			return this.label2ClassMap.get(label);
		}
		
		private Set<Class<?>> getAllStoredDomainClasses() {
			return this.class2labelMap.keySet();
		}
		
		private List<String> getLabel2ClassNameStringList() {
			List<String> ret = new ArrayList<String>(this.class2labelMap.size());
			Iterator<Entry<Class<?>, String>> it = this.class2labelMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<Class<?>, String> entry = it.next();
				StringBuilder sb = new StringBuilder();
				sb.append(entry.getValue());
				sb.append('=');
				sb.append(entry.getKey().getName());
				ret.add(sb.toString());
			}
			Collections.sort(ret);
			return ret;
		}
		
		private List<String> getFieldComponentTypeStringList() {
			List<String> ret = new ArrayList<String>(this.fieldComponentTypeMap.size());
			Iterator<Entry<String, CompoundObjectType>> it = this.fieldComponentTypeMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, CompoundObjectType> entry = it.next();
				StringBuilder sb = new StringBuilder();
				sb.append(entry.getKey());
				sb.append('=');
				sb.append(entry.getValue().getTypeListString());
				ret.add(sb.toString());
			}
			Collections.sort(ret);
			return ret;
		}
		
		private List<String> getConcreteFieldTypeStringList() {
			List<String> ret = new ArrayList<String>(this.concreteFieldTypeMap.size());
			Iterator<Entry<String, CompoundObjectType>> it = this.concreteFieldTypeMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, CompoundObjectType> entry = it.next();
				StringBuilder sb = new StringBuilder();
				sb.append(entry.getKey());
				sb.append('=');
				sb.append(entry.getValue().getTypeListString());
				ret.add(sb.toString());
			}
			Collections.sort(ret);
			return ret;
		}
	}
	
	/***********************************/
	public class InternalDomainAccess {

		public CompoundObjectType getFieldComponentType(String classField) {
			DomainInfo di = domainAccessHandler.loadDomainInfoIfNeeded();
			return di.getFieldComponentType(classField);
		}
		
		public CompoundObjectType getConcreteFieldType(String classField) {
			DomainInfo di = domainAccessHandler.loadDomainInfoIfNeeded();
			return di.getConcreteFieldType(classField);
		}

		public void addFieldComponentType(String classField, Class<?> type) {
			DomainInfo di = domainAccessHandler.getAvailableDomainInfo();
			di.addFieldComponentType(classField, type);
		}
		
		public void addConcreteFieldType(String classField, Class<?> type) {
			DomainInfo di = domainAccessHandler.getAvailableDomainInfo();
			di.addConcreteFieldType(classField, type);
		}
	}
}
