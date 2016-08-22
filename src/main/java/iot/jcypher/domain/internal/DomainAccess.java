/************************************************************************
 * Copyright (c) 2014-2016 IoT-Solutions e.U.
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

package iot.jcypher.domain.internal;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import iot.jcypher.concurrency.Locking;
import iot.jcypher.concurrency.QExecution;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.ResolutionDepth;
import iot.jcypher.domain.SyncInfo;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DOTypeBuilderFactory;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.InternalAccess;
import iot.jcypher.domain.genericmodel.internal.DomainModel;
import iot.jcypher.domain.internal.SkipLimitCalc.SkipsLimits;
import iot.jcypher.domain.mapping.CompoundObjectMapping;
import iot.jcypher.domain.mapping.CompoundObjectType;
import iot.jcypher.domain.mapping.DefaultObjectMappingCreator;
import iot.jcypher.domain.mapping.DomainState;
import iot.jcypher.domain.mapping.DomainState.IRelation;
import iot.jcypher.domain.mapping.DomainState.KeyedRelation;
import iot.jcypher.domain.mapping.DomainState.KeyedRelationToChange;
import iot.jcypher.domain.mapping.DomainState.LoadInfo;
import iot.jcypher.domain.mapping.DomainState.Relation;
import iot.jcypher.domain.mapping.DomainState.RelationLoadInfo;
import iot.jcypher.domain.mapping.DomainState.SourceField2TargetKey;
import iot.jcypher.domain.mapping.DomainState.SourceFieldKey;
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.FieldMapping.FieldKind;
import iot.jcypher.domain.mapping.IMapEntry;
import iot.jcypher.domain.mapping.MapTerminator;
import iot.jcypher.domain.mapping.MappingUtil;
import iot.jcypher.domain.mapping.ObjectMapping;
import iot.jcypher.domain.mapping.surrogate.AbstractSurrogate;
import iot.jcypher.domain.mapping.surrogate.Array;
import iot.jcypher.domain.mapping.surrogate.Deferred2DO;
import iot.jcypher.domain.mapping.surrogate.IDeferred;
import iot.jcypher.domain.mapping.surrogate.IEntryUpdater;
import iot.jcypher.domain.mapping.surrogate.ISurrogate2Entry;
import iot.jcypher.domain.mapping.surrogate.InnerClassSurrogate;
import iot.jcypher.domain.mapping.surrogate.ListEntriesUpdater;
import iot.jcypher.domain.mapping.surrogate.MapEntry;
import iot.jcypher.domain.mapping.surrogate.MapEntryUpdater;
import iot.jcypher.domain.mapping.surrogate.ObservableList;
import iot.jcypher.domain.mapping.surrogate.Surrogate2ListEntry;
import iot.jcypher.domain.mapping.surrogate.Surrogate2MapEntry;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.QueryLoader;
import iot.jcypher.domainquery.QueryPersistor;
import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.domainquery.internal.ReplayedQueryContext;
import iot.jcypher.graph.GrAccess;
import iot.jcypher.graph.GrLabel;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.graph.internal.LockUtil;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.pattern.Node;
import iot.jcypher.query.factories.clause.CASE;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.ELSE;
import iot.jcypher.query.factories.clause.END;
import iot.jcypher.query.factories.clause.FOR_EACH;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.MERGE;
import iot.jcypher.query.factories.clause.NATIVE;
import iot.jcypher.query.factories.clause.ON_CREATE;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.factories.clause.WHEN;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.factories.clause.WITH;
import iot.jcypher.query.factories.xpression.C;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.result.util.ResultHandler;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.writer.Format;
import iot.jcypher.transaction.ITransaction;
import iot.jcypher.transaction.internal.AbstractTransaction;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;
import iot.jcypher.util.Util;

public class DomainAccess implements IDomainAccess, IIntDomainAccess {
	
	private DomainAccessHandler domainAccessHandler;
	private InternalDomainAccess internalDomainAccess;
	private GenericDomainAccess genericDomainAccess;
	private static ThreadLocal<QExecution> qExecution = new ThreadLocal<QExecution>();

	/**
	 * @param dbAccess the graph database connection
	 * @param domainName
	 * @param domainLabelUse
	 */
	public DomainAccess(IDBAccess dbAccess, String domainName, DomainLabelUse domainLabelUse) {
		super();
		this.domainAccessHandler = new DomainAccessHandler(dbAccess, domainName, domainLabelUse);
	}

	@Override
	public List<JcError> store(Object domainObject) {
		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(domainObject);
		return this.store(domainObjects);
	}
	
	@Override
	public List<JcError> store(List<?> domainObjects) {
		List<JcError> ret = null;
		String pLab = this.domainAccessHandler.setDomainLabel();
		ITransaction txToClose = null;
		try {
			if (this.domainAccessHandler.lockingStrategy == Locking.OPTIMISTIC) {
				if (this.domainAccessHandler.dbAccess.getTX() == null)
					txToClose = this.beginTX();
			}
			ret = this.domainAccessHandler.store(domainObjects);
		} finally {
			CurrentDomain.setDomainLabel(pLab);
			if (txToClose != null) { // we have created the transaction
				if (ret == null)
					ret = new ArrayList<JcError>();
				ret.addAll(txToClose.close());
			}
		}
		return ret;
	}
	
	@Override
	public <T> T loadById(Class<T> domainObjectClass, int resolutionDepth, long id) {
		long[] ids = new long[] {id};
		List<T> ret = this.loadByIds(domainObjectClass, resolutionDepth, ids);
		return ret.get(0);
	}
	
	@Override
	public <T> List<T> loadByIds(Class<T> domainObjectClass, int resolutionDepth, long... ids) {
		List<T> ret;
		String pLab = this.domainAccessHandler.setDomainLabel();
		try {
			ret = this.domainAccessHandler.loadByIds(domainObjectClass, null,
					resolutionDepth, ids);
		} finally {
			CurrentDomain.setDomainLabel(pLab);
		}
		return ret;
	}
	
	@Override
	public <T> List<T> loadByType(Class<T> domainObjectClass, int resolutionDepth,
			int offset, int count) {
		List<T> ret;
		String pLab = this.domainAccessHandler.setDomainLabel();
		try {
			ret = this.domainAccessHandler.loadByType(domainObjectClass, resolutionDepth, offset, count);
		} finally {
			CurrentDomain.setDomainLabel(pLab);
		}
		return ret;
	}

	@Override
	public SyncInfo getSyncInfo(Object domainObject) {
		List<Object> domainObjects = new ArrayList<Object>(1);
		domainObjects.add(domainObject);
		List<SyncInfo> ret = this.domainAccessHandler.getSyncInfos(domainObjects);
		return ret.get(0);
	}
	
	@Override
	public List<SyncInfo> getSyncInfos(List<Object> domainObjects) {
		return this.domainAccessHandler.getSyncInfos(domainObjects);
	}
	
	@Override
	public long numberOfInstancesOf(Class<?> type) {
		List<Class<?>> types = new ArrayList<Class<?>>(1);
		types.add(type);
		List<Long> ret = this.numberOfInstancesOf(types);
		return ret.get(0);
	}

	@Override
	public List<Long> numberOfInstancesOf(List<Class<?>> types) {
		List<Long> ret;
		String pLab = this.domainAccessHandler.setDomainLabel();
		try {
			ret = this.domainAccessHandler.numberOfInstancesOf(types);
		} finally {
			CurrentDomain.setDomainLabel(pLab);
		}
		return ret;
	}

	@Override
	public DomainQuery createQuery() {
		DomainQuery ret = new DomainQuery(this);
		QueryRecorder.recordCreateQuery(ret);
		iot.jcypher.domainquery.InternalAccess.recordQuery(ret, QueryRecorder.getRecordedQuery(ret));
		return ret;
	}
	
	@Override
	public List<String> getStoredQueryNames() {
		return this.domainAccessHandler.getStoredQueryNames();
	}

	@Override
	public QueryPersistor createQueryPersistor(DomainQuery query) {
		return iot.jcypher.domainquery.InternalAccess.createQueryPersistor(query, this);
	}

	@Override
	public QueryLoader<DomainQuery> createQueryLoader(String queryName) {
		return iot.jcypher.domainquery.InternalAccess.createQueryLoader(queryName, this);
	}

	private DomainQuery createRecordedQuery(ReplayedQueryContext rqc, boolean doRecord) {
		DomainQuery ret = new DomainQuery(this);
		QueryRecorder.recordCreateQuery(ret);
		if (doRecord) {
			iot.jcypher.domainquery.InternalAccess.recordQuery(ret, QueryRecorder.getRecordedQuery(ret));
		}
		iot.jcypher.domainquery.InternalAccess.replayQuery(ret, rqc);
		return ret;
	}

	@Override
	public InternalDomainAccess getInternalDomainAccess() {
		if (this.internalDomainAccess == null)
			this.internalDomainAccess = new InternalDomainAccess();
		return this.internalDomainAccess;
	}

	@Override
	public ITransaction beginTX() {
		ITransaction ret = this.domainAccessHandler.dbAccess.beginTX();
		((AbstractTransaction)ret).setIntDomainAccess(this);
		synchronized (this.domainAccessHandler) {
			DomainState ds = this.domainAccessHandler.domainState.createCopy();
			this.domainAccessHandler.transactionState.set(ds);
			this.domainAccessHandler.domainModel.beginTx();
		}
		return ret;
	}
	
	@Override
	public IDomainAccess setLockingStrategy(Locking locking) {
		this.domainAccessHandler.lockingStrategy = locking;
		return this;
	}

	@Override
	public IGenericDomainAccess getGenericDomainAccess() {
		if (this.genericDomainAccess == null)
			this.genericDomainAccess = new GenericDomainAccess();
		return this.genericDomainAccess;
	}

	/**********************************************************************/
	public class GenericDomainAccess implements IGenericDomainAccess, IIntDomainAccess {

		@Override
		public List<JcError> store(DomainObject domainObject) {
			List<JcError> ret = DomainAccess.this.store(InternalAccess.getRawObject(domainObject));
			DomainState ds = domainAccessHandler.getDomainState();
			LoadInfo info = ds.getLoadInfoFrom_Object2IdMap(InternalAccess.getRawObject(domainObject));
			info.setDomainObject(domainObject);
			domainAccessHandler.domainModel.removeNurseryObject(InternalAccess.getRawObject(domainObject));
			return ret;
		}
		
		@Override
		public List<JcError> store(List<DomainObject> domainObjects) {
			List<Object> domObjs = new ArrayList<Object>(domainObjects.size());
			for(DomainObject dobj : domainObjects) {
				domObjs.add(InternalAccess.getRawObject(dobj));
			}
			List<JcError> ret = DomainAccess.this.store(domObjs);
			DomainState ds = domainAccessHandler.getDomainState();
			for(DomainObject dobj : domainObjects) {
				LoadInfo info = ds.getLoadInfoFrom_Object2IdMap(InternalAccess.getRawObject(dobj));
				info.setDomainObject(dobj);
				domainAccessHandler.domainModel.removeNurseryObject(InternalAccess.getRawObject(dobj));
			}
			return ret;
		}
		
		@Override
		public List<DomainObject> loadByIds(String domainObjectClassName,
				int resolutionDepth, long... ids) {
			List<Object> objs = domainAccessHandler.loadByIds(null, null, resolutionDepth, ids);
			List<DomainObject> ret = this.getDomainObjects(objs);
			return ret;
		}

		@Override
		public DomainObject loadById(String domainObjectClassName,
				int resolutionDepth, long id) {
			long[] ids = new long[] {id};
			List<DomainObject> ret = this.loadByIds(domainObjectClassName, resolutionDepth, ids);
			return ret.get(0);
		}

		@Override
		public List<DomainObject> loadByType(String domainObjectClassName,
				int resolutionDepth, int offset, int count) {
			List<DomainObject> ret;
			try {
				domainAccessHandler.updateMappingsIfNeeded();
				Class<?> clazz = domainAccessHandler.domainModel.getClassForName(domainObjectClassName);
				List<?> objs = this.getDomainAccess().loadByType(clazz, resolutionDepth, offset, count);
				ret = this.getDomainObjects(objs);
			} catch(Throwable e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException)e;
				else
					throw new RuntimeException(e);
				
			}
			return ret;
		}

		@Override
		public IDomainAccess getDomainAccess() {
			return DomainAccess.this;
		}
		
		private List<DomainObject> getDomainObjects(List<?> objects) {
			List<DomainObject> ret = new ArrayList<>(objects.size());
			DomainState ds = domainAccessHandler.getDomainState();
			for (Object obj : objects) {
				LoadInfo lInfo = ds.getLoadInfoFrom_Object2IdMap(obj);
				DomainObject dObj = lInfo.getDomainObject();
				if (dObj == null) {
					dObj = domainAccessHandler.domainModel.getCreateDomainObjectFor(obj);
					lInfo.setDomainObject(dObj);
				}
				ret.add(dObj);
			}
			return ret;
		}
		
		private DomainObject getDomainObject(Object obj) {
			DomainState ds = domainAccessHandler.getDomainState();
			LoadInfo lInfo = ds.getLoadInfoFrom_Object2IdMap(obj);
			if (lInfo != null) {
				DomainObject dObj = lInfo.getDomainObject();
				if (dObj == null) {
					dObj = domainAccessHandler.domainModel.getCreateDomainObjectFor(obj);
					lInfo.setDomainObject(dObj);
				}
				return dObj;
			}
			return null;
		}

		@Override
		public DOTypeBuilderFactory getTypeBuilderFactory() {
			return domainAccessHandler.domainModel.getTypeBuilderFactory();
		}

		@Override
		public DOType getDomainObjectType(String typeName) {
			domainAccessHandler.updateMappingsIfNeeded();
			return domainAccessHandler.domainModel.getDOType(typeName);
		}

		@Override
		public List<SyncInfo> getSyncInfos(List<DomainObject> domainObjects) {
			List<Object> dobjs = new ArrayList<Object>(domainObjects.size());
			for (DomainObject dobj : domainObjects) {
				dobjs.add(InternalAccess.getRawObject(dobj));
			}
			return DomainAccess.this.getSyncInfos(dobjs);
		}

		@Override
		public SyncInfo getSyncInfo(DomainObject domainObject) {
			return DomainAccess.this.getSyncInfo(InternalAccess.getRawObject(domainObject));
		}

		@Override
		public long numberOfInstancesOf(String typeName) {
			try {
				domainAccessHandler.updateMappingsIfNeeded();
				Class<?> typ = domainAccessHandler.domainModel.getClassForName(typeName);
				return DomainAccess.this.numberOfInstancesOf(typ);
			} catch(Throwable e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException)e;
				else
					throw new RuntimeException(e);
				
			}
		}

		@Override
		public List<Long> numberOfInstancesOf(List<String> typeNames) {
			try {
				domainAccessHandler.updateMappingsIfNeeded();
				List<Class<?>> typs = new ArrayList<Class<?>>(typeNames.size());
				for (String tn : typeNames) {
					Class<?> typ = domainAccessHandler.domainModel.getClassForName(tn);
					typs.add(typ);
				}
				return DomainAccess.this.numberOfInstancesOf(typs);
			} catch(Throwable e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException)e;
				else
					throw new RuntimeException(e);
				
			}
		}

		@Override
		public GDomainQuery createQuery() {
			GDomainQuery ret = new GDomainQuery(DomainAccess.this);
			QueryRecorder.recordCreateQuery(ret);
			iot.jcypher.domainquery.InternalAccess.recordQuery(ret, QueryRecorder.getRecordedQuery(ret));
			return ret;
		}
		
		@Override
		public List<String> getStoredQueryNames() {
			return domainAccessHandler.getStoredQueryNames();
		}

		@Override
		public QueryPersistor createQueryPersistor(GDomainQuery query) {
			return iot.jcypher.domainquery.InternalAccess.createQueryPersistor(query, this);
		}

		@Override
		public QueryLoader<GDomainQuery> createQueryLoader(String queryName) {
			return iot.jcypher.domainquery.InternalAccess.createQueryLoader(queryName, this);
		}

		private GDomainQuery createRecordedQuery(ReplayedQueryContext rqc, boolean doRecord) {
			GDomainQuery ret = new GDomainQuery(DomainAccess.this);
			QueryRecorder.recordCreateQuery(ret);
			if (doRecord) {
				iot.jcypher.domainquery.InternalAccess.recordQuery(ret, QueryRecorder.getRecordedQuery(ret));
			}
			iot.jcypher.domainquery.InternalAccess.replayQuery(ret, rqc);
			return ret;
		}

		@Override
		public ITransaction beginTX() {
			return DomainAccess.this.beginTX();
		}

		@Override
		public IGenericDomainAccess setLockingStrategy(Locking locking) {
			domainAccessHandler.lockingStrategy = locking;
			return this;
		}

		@Override
		public InternalDomainAccess getInternalDomainAccess() {
			return DomainAccess.this.getInternalDomainAccess();
		}
		
	}

	/**********************************************************************/
	public class DomainAccessHandler {
		private String regexClassfieldSep = "\\".concat(FieldMapping.ClassFieldSeparator);
		private static final String NodePrefix = "n_";
		private static final String RelationPrefix = "r_";
		private static final String DomainInfoNodeLabel = "DomainInfo";
		private static final String DomainInfoNameProperty = "name";
		private static final String DomainInfoLabel2ClassProperty = "label2ClassMap";
		private static final String DomainInfoFieldComponentTypeProperty = "componentTypeMap";
		private static final String DomainInfoConcreteFieldTypeProperty = "fieldTypeMap";
		private static final String DomainInfoUseDomainLabelProperty = "useDomainLabels";
		private static final String DomainInfoVersionProperty = "_i_version";
		private static final String DomainInfoModelVersionProperty = "_m_version";
		private static final String KeyProperty = "key";
		private static final String ValueProperty = "value";
		private static final String KeyTypeProperty = "keyType";
		private static final String ValueTypeProperty = "valueType";
		
		private String domainName;
		private String domainLabel;
		/**
		 * defines at which recursion occurrence building a query is stopped
		 */
		private int maxRecursionCount = 1;
		private int maxPathSize = 2;
		private IDBAccess dbAccess;
		private DomainState domainState;
		private ThreadLocal<DomainState> transactionState;
		private ThreadLocal<ReResolve> reResolve;
		private Locking lockingStrategy;
		private Map<Class<?>, ObjectMapping> mappings;
		
		// for a root level type in a query, all possible variants (subclasses) must be considered
		// in order to build a query completely resolving all paths of all possible variants.
		// That is important, if the root level type is an interface, an abstract class or simple a super class
		// of the object that has actually been stored in the graph.
		private Map<Class<?>, CompoundObjectType> type2CompoundTypeMap;
		private DomainInfo domainInfo;
		private DomainModel domainModel;
		private DomainLabelUse domainLabelUse;

		private DomainAccessHandler(IDBAccess dbAccess, String domainName, DomainLabelUse du) {
			super();
			this.domainLabelUse = du;
			this.domainName = domainName;
			this.dbAccess = new DBAccessWrapper(dbAccess);
			this.domainState = new DomainState();
			this.mappings = new HashMap<Class<?>, ObjectMapping>();
			this.type2CompoundTypeMap = new HashMap<Class<?>, CompoundObjectType>();
			this.transactionState = new ThreadLocal<DomainState>();
			this.reResolve = new ThreadLocal<ReResolve>();
			this.lockingStrategy = Locking.NONE;
			this.domainModel = iot.jcypher.domain.genericmodel.internal.InternalAccess
					.createDomainModel(this.domainName, getDomainLabel(), DomainAccess.this);
		}
		
		@SuppressWarnings("unchecked")
		<T> List<T> loadByIds(Class<T> domainObjectClass,
				Map<Class<?>, List<Long>> type2IdsMap, int resolutionDepth, long... ids) {
			List<T> resultList = new ArrayList<T>(ids.length);
			
			if (ids.length == 0)
				return resultList;
			
			InternalDomainAccess internalAccess = null;
			try {
				internalAccess = MappingUtil.internalDomainAccess.get();
				MappingUtil.internalDomainAccess.set(getInternalDomainAccess());
				updateMappingsIfNeeded();
				Map<Class<?>, List<Long>> typeMap = type2IdsMap;
				if (typeMap == null)
					typeMap = queryConcreteTypes(ids);
				Iterator<Entry<Class<?>, List<Long>>> it = typeMap.entrySet().iterator();
				while(it.hasNext()) {
					Entry<Class<?>, List<Long>> entry = it.next();
					if (domainObjectClass != null) { // null when using generic domain model
						if (!domainObjectClass.isAssignableFrom(entry.getKey()))
							throw new RuntimeException("concrete type must be the same or a subtype of: "
										+ domainObjectClass.getName());
					}
					ClosureQueryContext context = new ClosureQueryContext(entry.getKey());
					new ClosureCalculator().calculateClosureQuery(context);
					boolean repeat = context.matchClauses != null && context.matchClauses.size() > 0;
					
					List<IdAndDepth> idList = new ArrayList<IdAndDepth>(entry.getValue().size());
					for (Long id : entry.getValue()) {
						idList.add(new IdAndDepth(id, 0));
					}
					if (repeat) { // has one or more match clauses
						loadByIdsWithMatches(entry.getKey(), context, null, null, idList,
								resolutionDepth);
					} else { // only simple start by id clauses are needed
						loadByIdsSimple(entry.getKey(), idList);
					}
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

			for (long id : ids) {
				resultList.add((T)getDomainState().getFrom_Id2ObjectMap(id));
			}
			return resultList;
		}
		
		@SuppressWarnings("rawtypes")
		void loadDeep(List<FillModelContext.ResolvedDepth> objectsNotResolvedDeep, Set<IDeferred> deferredSet,
				SurrogateChangeLog surrogateChangeLog, int resolutionDepth) {
			Map<Class<?>, List<FillModelContext.ResolvedDepth>> byType =
					new HashMap<Class<?>, List<FillModelContext.ResolvedDepth>>();
			for (FillModelContext.ResolvedDepth notResolvedDeep : objectsNotResolvedDeep) {
				List<FillModelContext.ResolvedDepth> list = byType.get(notResolvedDeep.domainObject.getClass());
				if (list == null) {
					list = new ArrayList<FillModelContext.ResolvedDepth>();
					byType.put(notResolvedDeep.domainObject.getClass(), list);
				}
				list.add(notResolvedDeep);
			}
			Iterator<Entry<Class<?>, List<FillModelContext.ResolvedDepth>>> it = byType.entrySet().iterator();
			while(it.hasNext()) {
				Entry<Class<?>, List<FillModelContext.ResolvedDepth>> entry = it.next();
				loadDeepByType(entry.getKey(), entry.getValue(), deferredSet, surrogateChangeLog,
						resolutionDepth);
			}
		}
		
		<T> List<T> loadByType(Class<T> domainObjectClass, int resolutionDepth,
				int offset, int count) {
			if (offset < 0)
				throw new RuntimeException("offset must be >= 0");
			List<Class<?>> typeList = this.getCompoundTypesFor(domainObjectClass);
			int numTypes = typeList.size();
			JcNode n = new JcNode("n");
			JcNumber num = new JcNumber("num");
			List<Integer> offsets;
			List<Integer> lens;
			if (numTypes > 1 && (offset > 0 || count >= 0)) {
				List<JcQuery> queries = new ArrayList<JcQuery>(numTypes);
				for (Class<?> rawType : typeList) {
					String nodeLabel = domainInfo.getLabelForClass(rawType);
					if (nodeLabel != null) {
						JcQuery query = new JcQuery();
						query.setClauses(new IClause[]{
								MATCH.node(n).label(nodeLabel),
								RETURN.count().value(n).AS(num)
						});
						queries.add(query);
					}
				}
//				Util.printQueries(queries, "LOAD-BY-TYPE-COUNT", Format.PRETTY_1);
				List<JcQueryResult> results = this.dbAccess.execute(queries);
				List<JcError> errors = Util.collectErrors(results);
				if (errors.size() > 0) {
					throw new JcResultException(errors);
				}
//				Util.printResults(results, "LOAD-BY-TYPE-COUNT", Format.PRETTY_1);
				List<Integer> counts = new ArrayList<Integer>(results.size());
				for (JcQueryResult result : results) {
					BigDecimal res = result.resultOf(num).get(0);
					counts.add(res.intValue());
				}
				
				SkipsLimits slc = SkipLimitCalc.calcSkipsLimits(counts, offset, count);
				offsets = slc.getOffsets();
				lens = slc.getLengths();
			} else {
				offsets = new ArrayList<Integer>(numTypes);
				lens = new ArrayList<Integer>(numTypes);
				if (numTypes == 1) {
					offsets.add(offset);
					lens.add(count);
				} else {
					for (int i = 0; i < numTypes; i++) {
						offsets.add(0);
						lens.add(-1);
					}
				}
			}
			
			List<JcQuery> queries = new ArrayList<JcQuery>(numTypes);
			int idx = 0;
			for (Class<?> rawType : typeList) {
				String nodeLabel = domainInfo.getLabelForClass(rawType);
				if (nodeLabel != null) {
					if (lens.get(idx) != 0) { // otherwise there is a distinct number of elements or -1
						JcQuery query = new JcQuery();
						query.setClauses(new IClause[]{
								MATCH.node(n).label(nodeLabel),
								RETURN.value(n.id()).AS(num)
						});
						queries.add(query);
					}
					idx++;
				}
			}
			List<Long> ids = new ArrayList<Long>();
			if (queries.size() > 0) {
				Util.printQueries(queries, QueryToObserve.LOAD_BY_TYPE_QUERY, Format.PRETTY_1);
				List<JcQueryResult> results = this.dbAccess.execute(queries);
				List<JcError> errors = Util.collectErrors(results);
				if (errors.size() > 0) {
					throw new JcResultException(errors);
				}
	//			Util.printResults(results, "LOAD-BY-TYPE", Format.PRETTY_1);
				idx = 0;
				int resIdx = 0;
				for (Class<?> rawType : typeList) {
					String nodeLabel = domainInfo.getLabelForClass(rawType);
					if (nodeLabel != null) {
						if (lens.get(idx) != 0) { // no query in that case
							JcQueryResult result = results.get(resIdx);
							List<BigDecimal> rList = result.resultOf(num);
							int sz = lens.get(idx) + offsets.get(idx);
							sz = sz == -1 ? rList.size() : sz > rList.size() ? rList.size() : sz;
							for (int i = offsets.get(idx); i < sz; i++) {
								ids.add(rList.get(i).longValue());
							}
							resIdx++;
						}
						idx++;
					}
				}
			}
			long[] idsArray = new long[ids.size()];
			for (int i = 0; i < ids.size(); i++) {
				idsArray[i] = ids.get(i).longValue();
			}
			
			return loadByIds(domainObjectClass, null, resolutionDepth, idsArray);
		}
		
		@SuppressWarnings("rawtypes")
		<T> void loadDeepByType(Class<T> domainObjectClass,
				List<FillModelContext.ResolvedDepth> objectsNotResolvedDeep,
				Set<IDeferred> deferredSet, SurrogateChangeLog surrogateChangeLog,
				int resolutionDepth) {
			InternalDomainAccess internalAccess = null;
			ClosureQueryContext context = new ClosureQueryContext(domainObjectClass);
			List<IdAndDepth> idList = new ArrayList<IdAndDepth>(objectsNotResolvedDeep.size());
			for (int i = 0; i < objectsNotResolvedDeep.size(); i++) {
				FillModelContext.ResolvedDepth resDepth = objectsNotResolvedDeep.get(i);
				idList.add(new IdAndDepth(this.getDomainState().getLoadInfoFrom_Object2IdMap(
						resDepth.domainObject).getId(),
						resDepth.resolvedDepth));
			}
			try {
				internalAccess = MappingUtil.internalDomainAccess.get();
				MappingUtil.internalDomainAccess.set(getInternalDomainAccess());
				updateMappingsIfNeeded();
				new ClosureCalculator().calculateClosureQuery(context);
				boolean repeat = context.matchClauses != null && context.matchClauses.size() > 0;
				
				if (repeat) { // has one or more match clauses
					loadByIdsWithMatches(domainObjectClass, context, deferredSet, surrogateChangeLog, idList,
							resolutionDepth);
				} else { // only simple start by id clauses are needed
					loadByIdsSimple(domainObjectClass, idList);
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
		}
		
		List<JcError> store(List<?> domainObjects) {
			UpdateContext context;
			InternalDomainAccess internalAccess = null;
			try {
				internalAccess = MappingUtil.internalDomainAccess.get();
				MappingUtil.internalDomainAccess.set(getInternalDomainAccess());
				context = this.updateLocalGraph(domainObjects);
			} finally {
				if (internalAccess != null)
					MappingUtil.internalDomainAccess.set(internalAccess);
				else
					MappingUtil.internalDomainAccess.remove();
			}
			
			List<JcError> errors;
			if (context.lockingErrors) { // only set if Locking.OPTIMISTIC and there are errors
				errors = new ArrayList<JcError>();
				JcError error = new JcError("JCypher.Locking", "Optimistic locking failed (an element was deleted by another client)",
						null);
				errors.add(error);
				ITransaction tx = this.dbAccess.getTX();
				if (tx != null)
					tx.failure();
			} else {
				Map<Long, Integer> elementVersionsMap = null;
				if (this.lockingStrategy == Locking.OPTIMISTIC) {
					if (context.nodeIndexMap != null || context.relationIndexMap != null)
						elementVersionsMap = new HashMap<Long, Integer>();
					if (context.nodeIndexMap != null) {
						Iterator<QueryNode2ResultNode> it = context.nodeIndexMap.values().iterator();
						while (it.hasNext()) {
							QueryNode2ResultNode n2n = it.next();
							elementVersionsMap.put(n2n.resultNode.getId(), n2n.version);
						}
					}
					if (context.relationIndexMap != null) {
						Iterator<QueryRelation2ResultRelation> it = context.relationIndexMap.values().iterator();
						while (it.hasNext()) {
							QueryRelation2ResultRelation r2r = it.next();
							elementVersionsMap.put(r2r.resultRelation.getId(), r2r.version);
						}
					}
				}
				errors = GrAccess.store(context.graph, elementVersionsMap);
				DomainState ds = getDomainState();
				if (errors.isEmpty()) {
					for (IRelation relat : context.relationsToRemove) {
						ds.removeRelation(relat);
					}
					
					Iterator<Entry<Object, GrNode>> it = context.domObj2Node.entrySet().iterator();
					while(it.hasNext()) {
						Entry<Object, GrNode> entry = it.next();
						GrNode nd = entry.getValue();
						GrProperty prop = nd.getProperty(ResultHandler.lockVersionProperty);
						int v = -1;
						if (prop != null)
							v = ((Number)prop.getValue()).intValue();
						ds.add_Id2Object(entry.getKey(), nd.getId(), v, ResolutionDepth.DEEP);
					}
					
					for (DomRelation2ResultRelation d2r : context.domRelation2Relations) {
						GrRelation rel = d2r.resultRelation;
						GrProperty prop = rel.getProperty(ResultHandler.lockVersionProperty);
						int v = -1;
						if (prop != null)
							v = ((Number)prop.getValue()).intValue();
						ds.add_Id2Relation(d2r.domRelation, rel.getId(), v);
					}
				}
			}
			return errors;
		}
		
		List<SyncInfo> getSyncInfos(List<Object> domainObjects) {
			List<SyncInfo> ret = new ArrayList<SyncInfo>(domainObjects.size());
			for (Object obj : domainObjects) {
				LoadInfo li = this.getDomainState().getLoadInfoFrom_Object2IdMap(obj);
				if (li != null)
					ret.add(new SyncInfo(li.getId(), li.getResolutionDepth()));
				else
					ret.add(new SyncInfo(-1, null));
			}
			return ret;
		}
		
		List<Long> numberOfInstancesOf(List<Class<?>> types) {
			updateMappingsIfNeeded();
			List<Long> resultList = new ArrayList<Long>();
			List<Integer> cumulationList = new ArrayList<Integer>(types.size());
			List<JcQuery> queries = new ArrayList<JcQuery>();
			JcNode n = new JcNode("n");
			JcNumber num = new JcNumber("num");
			for (Class<?> type : types) {
				Iterator<CompoundObjectType> it = getCompoundTypeFor(type).typeIterator();
				int cumulation = 0;
				while(it.hasNext()) {
					cumulation++;
					Class<?> rawType = it.next().getType();
					String nodeLabel = domainInfo.getLabelForClass(rawType);
					JcQuery query = new JcQuery();
					query.setClauses(new IClause[]{
							MATCH.node(n).label(nodeLabel),
							RETURN.count().value(n).AS(num)
					});
					queries.add(query);
				}
				cumulationList.add(cumulation);
			}
//			Util.printQueries(queries, "INSTANCE-COUNT", Format.PRETTY_1);
			List<JcQueryResult> results = this.dbAccess.execute(queries);
			List<JcError> errors = Util.collectErrors(results);
			if (errors.size() > 0) {
				throw new JcResultException(errors);
			}
//			Util.printResults(results, "INSTANCE-COUNT", Format.PRETTY_1);
			
			int idx = 0;
			for (int i = 0; i< cumulationList.size(); i++) {
				int cumulation = cumulationList.get(i);
				long count = 0;
				while (cumulation > 0) {
					JcQueryResult result = results.get(idx);
					BigDecimal res = result.resultOf(num).get(0);
					count = count + res.longValue();
					idx++;
					cumulation--;
				}
				resultList.add(count);
			}
			
			return resultList;
		}
		
		List<String> getStoredQueryNames() {
			List<String> ret = new ArrayList<String>();
			IDBAccess dba = ((DBAccessWrapper)this.dbAccess).delegate;
			String qLabel = this.getDomainLabel()	.concat(QueryPersistor.Q_LABEL_POSTFIX);
			
			JcNode n = new JcNode("n");
			IClause[] clauses = new IClause[] {
					MATCH.node(n).label(qLabel),
					RETURN.value(n)
			};
			JcQuery q = new JcQuery();
			q.setClauses(clauses);
			JcQueryResult result = dba.execute(q);
			if (result.hasErrors()) {
				StringBuilder sb = new StringBuilder();
				Util.appendErrorList(Util.collectErrors(result), sb);
				throw new RuntimeException(sb.toString());
			}
			List<GrNode> lgn = result.resultOf(n);
			for (GrNode rn : lgn) {
				ret.add(rn.getProperty(QueryPersistor.PROP_NAME).getValue().toString());
			}
			return ret;
		}
		
		private synchronized DomainState getDomainState() {
			DomainState ds = this.transactionState.get();
			if (ds == null)
				ds = this.domainState;
			return ds;
		}
		
		private DomainModel getDomainModel() {
			this.loadDomainInfoIfNeeded();
			return domainModel;
		}

		private Map<Class<?>, List<Long>> queryConcreteTypes(long[] ids) {
			JcQuery query = new JcQuery();
			JcNode n = new JcNode("n");
			IClause[] clauses = new IClause[] {
				START.node(n).byId(ids),
				RETURN.value(n)
			};
			query.setClauses(clauses);
			// TODO check in later versions of neo4j if params work
			// with embedded and in_memory databases
			if (this.dbAccess.getDBType() != DBType.REMOTE)
				query.setExtractParams(false);
			Util.printQuery(query, QueryToObserve.QUERY_CONCRETE_TYPE, Format.PRETTY_1);
			JcQueryResult result = dbAccess.execute(query);
			List<JcError> errors = Util.collectErrors(result);
			if (errors.size() > 0) {
				throw new JcResultException(errors);
			}
			Map<Class<?>, List<Long>> byType = new HashMap<Class<?>, List<Long>>();
			DomainInfo di = loadDomainInfoIfNeeded();
			List<GrNode> nodes = result.resultOf(n);
			for (GrNode node : nodes) {
				List<GrLabel> labels = node.getLabels();
				for (GrLabel label : labels) {
					String lab = label.getName();
					Class<?> typ = di.getClassForLabel(lab);
					if (typ != null) {
						List<Long> list = byType.get(typ);
						if (list == null) {
							list = new ArrayList<Long>();
							byType.put(typ, list);
						}
						list.add(node.getId());
						break;
					}
				}
			}
			return byType;
		}
		
		private void updateMappingsIfNeeded() {
			if (this.domainInfo == null) {
				loadDomainInfoIfNeeded();
			}
		}

		private UpdateContext updateLocalGraph(List<?> domainObjects) {
			UpdateContext context = new UpdateContext();
			context.lockingErrors = false;
			new ClosureCalculator().calculateClosure(domainObjects,
					context);
			//int sz = domainState.getSurrogateState().size();
			context.surrogateChangeLog.applyChanges();
			//int sz1 = domainState.getSurrogateState().size();
			Graph graph = null;
			Object domainObject;
			List<IClause> clauses = null;
			List<IClause> removeStartClauses = null;
			List<IClause> removeClauses = null;
			DomainState ds = this.getDomainState();
			for (int i = 0; i < context.domainObjects.size(); i++) {
				domainObject = context.domainObjects.get(i);
				LoadInfo li = ds.getLoadInfoFrom_Object2IdMap(domainObject);
				Long id = li != null ? li.getId() : null;
				if (id != null) { // object exists in graphdb
					JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
					QueryNode2ResultNode n2n = new QueryNode2ResultNode();
					n2n.queryNode = n;
					n2n.version = li.getVersion();
					if (context.nodeIndexMap == null)
						context.nodeIndexMap = new HashMap<Integer, QueryNode2ResultNode>();
					context.nodeIndexMap.put(new Integer(i), n2n);
					if (clauses == null)
						clauses = new ArrayList<IClause>();
					//clauses.add(START.node(n).byId(id.longValue()));
					
					// use OPTIONAL_MATCH to be tolerant for removed elements
					clauses.add(OPTIONAL_MATCH.node(n));
					clauses.add(WHERE.valueOf(n.id()).EQUALS(id.longValue()));
				}
			}
			
			for (int i = 0; i < context.relations.size(); i++) {
				IRelation relat = context.relations.get(i);
				RelationLoadInfo rli = ds.getFrom_Relation2IdMap(relat);
				Long id = rli != null ? rli.getId() : null;
				if (id != null) { // relation exists in graphdb
					JcRelation r = new JcRelation(RelationPrefix.concat(String.valueOf(i)));
					QueryRelation2ResultRelation r2r = new QueryRelation2ResultRelation();
					r2r.queryRelation = r;
					r2r.version = rli.getVersion();
					if (context.relationIndexMap == null)
						context.relationIndexMap = new HashMap<Integer, QueryRelation2ResultRelation>();
					context.relationIndexMap.put(new Integer(i), r2r);
					//clauses.add(START.relation(r).byId(id.longValue()));
					
					// use OPTIONAL_MATCH to be tolerant for removed elements
					clauses.add(OPTIONAL_MATCH.node().relation(r).out().node());
					clauses.add(WHERE.valueOf(r.id()).EQUALS(id.longValue()));
				}
			}
			
			// relations to remove
			LockUtil.Removes removes = new LockUtil.Removes();
			if (context.relationsToRemove.size() > 0) {
				removeStartClauses = new ArrayList<IClause>();
				removeClauses = new ArrayList<IClause>();
				for (int i = 0; i < context.relationsToRemove.size(); i++) {
					IRelation relat = context.relationsToRemove.get(i);
					// relation must exist in db
					RelationLoadInfo rli = ds.getFrom_Relation2IdMap(relat);
					Long id = rli.getId();
					JcRelation r = new JcRelation(RelationPrefix.concat(String.valueOf(i)));
					//removeStartClauses.add(START.relation(r).byId(id.longValue()));
					
					// use OPTIONAL_MATCH to be tolerant for removed elements
					removeStartClauses.add(OPTIONAL_MATCH.node().relation(r).out().node());
					removeStartClauses.add(WHERE.valueOf(r.id()).EQUALS(id.longValue()));
					if (this.lockingStrategy == Locking.OPTIMISTIC) {
						LockUtil.calcRemoves(removes, r, rli.getVersion());
					}
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
					// node must exist in db
					LoadInfo li = ds.getLoadInfoFrom_Object2IdMap(dobj);
					Long id = li.getId();
					JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
					//removeStartClauses.add(START.node(n).byId(id.longValue()));
					
					// use OPTIONAL_MATCH to be tolerant for removed elements
					removeStartClauses.add(OPTIONAL_MATCH.node(n));
					removeStartClauses.add(WHERE.valueOf(n.id()).EQUALS(id.longValue()));
					if (this.lockingStrategy == Locking.OPTIMISTIC) {
						LockUtil.calcRemoves(removes, n, li.getVersion());
					}
					removeClauses.add(DO.DELETE(n));
				}
			}
			
			if (removes.getWithClauses() != null) {
				JcNumber nSum = new JcNumber("sum");
				removes.getWithClauses().add(WITH.value(removes.getSum()).AS(nSum));
			}
			
			JcNumber nSum = null;
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
					if (removes.getWithClauses() != null) {
						removeStartClauses.addAll(removes.getWithClauses());
						JcValue x = new JcValue("x");
						nSum = new JcNumber("sum");
						// conditional remove in case of Locking.OPTIONAL
						IClause clause = FOR_EACH.element(x).IN(C.CREATE(new IClause[]{
								CASE.result(),
								WHEN.valueOf(nSum).EQUALS(removes.getVersionSum()),
									NATIVE.cypher("[1]"),
								ELSE.perform(),
									NATIVE.cypher("[]"),
								END.caseXpr()
						})).DO(removeClauses.toArray(new IClause[removeClauses.size()]));
						removeStartClauses.add(clause);
						removeStartClauses.add(RETURN.value(nSum));
					} else
						removeStartClauses.addAll(removeClauses);
					query = new JcQuery();
					query.setClauses(removeStartClauses.toArray(new IClause[removeStartClauses.size()]));
					queries.add(query);
				}
				Util.printQueries(queries, QueryToObserve.CLOSURE_QUERY, Format.PRETTY_1);
				List<JcQueryResult> results = this.dbAccess.execute(queries);
				List<JcError> errors = Util.collectErrors(results);
				if (errors.size() > 0) {
					throw new JcResultException(errors);
				}
				
				// check for locking error
				if (removeStartClauses != null && this.lockingStrategy == Locking.OPTIMISTIC) {
					JcQueryResult result = clauses != null ? results.get(1) : results.get(0);
					List<BigDecimal> versionSums = result.resultOf(nSum);
					if (versionSums.size() == 0 || versionSums.get(0) == null)
						context.lockingErrors = true;
					else if (versionSums.get(0).intValue() != removes.getVersionSum())
						context.lockingErrors = true;
				}
				
				if (clauses != null) {
					JcQueryResult result = results.get(0);
					graph = result.getGraph();
					GrAccess.setDBAccess(this.dbAccess, graph);
					if (context.nodeIndexMap != null) {
						Iterator<Entry<Integer, QueryNode2ResultNode>> nit = context.nodeIndexMap.entrySet().iterator();
						while (nit.hasNext()) {
							Entry<Integer, QueryNode2ResultNode> entry = nit.next();
							List<GrNode> nds = result.resultOf(entry.getValue().queryNode);
							GrNode res = null;
							if (nds.size() > 0)
								res = nds.get(0);
							if (this.lockingStrategy == Locking.OPTIMISTIC && res == null) // element has been deleted
								context.lockingErrors = true;
							entry.getValue().resultNode = res;
						}
					}
					if (context.relationIndexMap != null) {
						Iterator<Entry<Integer, QueryRelation2ResultRelation>> rit = context.relationIndexMap.entrySet().iterator();
						while (rit.hasNext()) {
							Entry<Integer, QueryRelation2ResultRelation> entry = rit.next();
							List<GrRelation> rels = result.resultOf(entry.getValue().queryRelation);
							GrRelation res = null;
							if (rels.size() > 0)
								res = rels.get(0);
							if (this.lockingStrategy == Locking.OPTIMISTIC && res == null) // element has been deleted
								context.lockingErrors = true;
							entry.getValue().resultRelation = res;
						}
					}
				}
			}
			// up to here, objects existing as nodes in the graphdb as well as relations have been loaded
			// and relations that should be removed have been removed from the db
			
			if (graph == null) // no nodes loaded from db
				graph = Graph.create(this.dbAccess);
			graph.setLockingStrategy(this.lockingStrategy);
			
			if (!context.lockingErrors) {
				context.domObj2Node = new HashMap<Object, GrNode>(
						context.domainObjects.size());
				context.domRelation2Relations = new ArrayList<DomRelation2ResultRelation>();
				for (int i = 0; i < context.domainObjects.size(); i++) {
					GrNode rNode = null;
					if (context.nodeIndexMap != null && context.nodeIndexMap.get(i) != null) {
						rNode = context.nodeIndexMap.get(i).resultNode;
					}
					if (rNode == null)
						rNode = graph.createNode();
					
					context.domObj2Node.put(context.domainObjects.get(i), rNode);
					updateGraphFromObject(context.domainObjects.get(i), rNode);
				}
				
				for (int i = 0; i < context.relations.size(); i++) {
					GrRelation rRelation = null;
					if (context.relationIndexMap != null && context.relationIndexMap.get(i) != null) {
						rRelation = context.relationIndexMap.get(i).resultRelation;
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
		private <T> List<T> loadByIdsWithMatches(Class<T> domainObjectClass,
				ClosureQueryContext context, Set<IDeferred> deferredSet,
				SurrogateChangeLog surrogateChangeLog, List<IdAndDepth> idList,
				int resolutionDepth) {
			List<T> resultList = new ArrayList<T>();
			Set<IDeferred> deferreds = deferredSet;
			JcQuery query;
			String nm = NodePrefix.concat(String.valueOf(0));
			List<JcQuery> queries = new ArrayList<JcQuery>();
			Map<Long, JcQueryResult> id2QueryResult = new HashMap<Long, JcQueryResult>();
			List<Long> queryIds = new ArrayList<Long>();
			for (int i = 0; i < idList.size(); i++) {
				// if the object has already been loaded it will be reloaded (updated)
				long id = idList.get(i).id;
				query = new JcQuery();
				JcNode n = new JcNode(nm);
				List<IClause> clauses = new ArrayList<IClause>();
				clauses.add(START.node(n).byId(id));
				clauses.addAll(context.matchClauses);
				clauses.add(RETURN.ALL());
				IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
				query.setClauses(clausesArray);
				queries.add(query);
				queryIds.add(id);
			}
			
			if (queries.size() > 0) { // at least one node has to be (re)loaded
				Util.printQueries(queries, QueryToObserve.CLOSURE_QUERY, Format.PRETTY_1);
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
			
			boolean isRoot;
			if (deferreds == null) {
				isRoot = true;
				deferreds = new HashSet<IDeferred>();
				surrogateChangeLog = new SurrogateChangeLog();
			} else
				isRoot = false;
			@SuppressWarnings("rawtypes")
			List<FillModelContext.ResolvedDepth> objectsNotResolvedDeep =
					new ArrayList<FillModelContext.ResolvedDepth>();
			for (int i = 0; i < idList.size(); i++) {
				FillModelContext<T> fContext = new FillModelContext<T>(domainObjectClass,
						id2QueryResult.get(idList.get(i).id), context.queryEndNodes, context.recursionExitNodes,
						surrogateChangeLog, resolutionDepth, idList.get(i).depth);
				new ClosureCalculator().fillModel(fContext);
				objectsNotResolvedDeep.addAll(fContext.recursionExitObjects);
				resultList.add(fContext.domainObject);
				deferreds.addAll(fContext.deferredList);
			}
			
			if (!objectsNotResolvedDeep.isEmpty()) {
				loadDeep(objectsNotResolvedDeep, deferreds, surrogateChangeLog,
						resolutionDepth);
			}
			
			if (isRoot) {
				buildDeferredResolutionTree(deferreds);
				handleLoops(deferreds);
				// find leafs
				List<IDeferred> leafs = new ArrayList<IDeferred>();
				for (IDeferred deferred : deferreds) {
					if (deferred.isLeaf())
						leafs.add(deferred);
				}
				// handle deferred updates
				resolveDeferreds(leafs.iterator());
				surrogateChangeLog.applyChanges();
			}
			
			return resultList;
		}
		
		private void handleLoops(Set<IDeferred> deferreds) {
			for (IDeferred deferred : deferreds) {
				if (deferred.isRoot()) {
					deferred.breakLoops();
				}
			}
		}

		private void resolveDeferreds(Iterator<IDeferred> it) {
			while(it.hasNext()) {
				IDeferred deferred = it.next();
				if (deferred.isLeaf()) {
					deferred.performUpdate();
					resolveDeferreds(deferred.nextUp());
				}
			}
		}
		
		private void buildDeferredResolutionTree(Set<IDeferred> deferreds) {
			for (IDeferred deferred : deferreds) {
				if (deferred instanceof ISurrogate2Entry) {
					for (IDeferred def : deferreds) {
						if (def instanceof IEntryUpdater) {
							if (((ISurrogate2Entry) deferred).entry2Update().equals(((IEntryUpdater) def).entry2Update())) {
								deferred.addNextUpInTree(def);
							}
						}
					}
				} else if (deferred instanceof IEntryUpdater) {
					for (IDeferred def : deferreds) {
						if (def instanceof ISurrogate2Entry) {
							if (((IEntryUpdater) deferred).objectToUpdate() == ((ISurrogate2Entry) def).getSurrogate().objectToUpdate()) {
								deferred.addNextUpInTree(def);
							}
						} else if (def instanceof Deferred2DO) {
							if (((IEntryUpdater) deferred).objectToUpdate() == ((Deferred2DO) def).getDeferred().objectToUpdate()) {
								deferred.addNextUpInTree(def);
							}
						}
					}
				}
			}
		}

		/**
		 * has start by id clauses only
		 * @param domainObjectClass
		 * @param ids
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private <T> List<T> loadByIdsSimple(Class<T> domainObjectClass, List<IdAndDepth> idList) {
			List<T> resultList = new ArrayList<T>();
			List<IClause> clauses = new ArrayList<IClause>();
			Map<Long, JcNode> id2QueryNode = new HashMap<Long, JcNode>();
			Map<Long, T> id2Object = new HashMap<Long, T>();
			DomainState ds = this.getDomainState();
			for (int i = 0; i < idList.size(); i++) {
				long id = idList.get(i).id;
				// check if domain objects have already been loaded
				T obj = (T) ds.getFrom_Id2ObjectMap(id);
				if (obj != null)
					id2Object.put(id, obj);

				// if the object has already been loaded it will be reloaded (updated)
				JcNode n = new JcNode(NodePrefix.concat(String.valueOf(i)));
				id2QueryNode.put(id, n);
				clauses.add(START.node(n).byId(id));
			}
			
			JcQueryResult result = null;
			if (clauses.size() > 0) { // one or more nodes are to be (re)loaded
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
			
			for (int i = 0; i < idList.size(); i++) {
				long id = idList.get(i).id;
				T obj = id2Object.get(id);
				GrNode rNode = result.resultOf(id2QueryNode.get(id)).get(0);
				T resObj = createIfNeeded_MapProperties(domainObjectClass, rNode, obj);
				GrProperty prop = rNode.getProperty(ResultHandler.lockVersionProperty);
				int v = -1;
				if (prop != null)
					v = ((Number)prop.getValue()).intValue();
				if (obj == null)
					ds.add_Id2Object(resObj, id, v, ResolutionDepth.DEEP);
				else
					ds.getLoadInfoFrom_Object2IdMap(obj)
						.setResolutionDepth(ResolutionDepth.DEEP)
						.setVersion(v);
				resultList.add(resObj);
			}
			return resultList;
		}

		@SuppressWarnings("unchecked")
		private <T> T createIfNeeded_MapProperties(Class<T> domainObjectClass, GrNode rNode, T domObj) {
			T domainObject = domObj;
			if (domainObject == null) {
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
				domainObject = (T) createInstance(concreteClass);
			}
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
			Object value = null;
			if (relat instanceof KeyedRelation) {
				key = ((KeyedRelation)relat).getKey();
				value = ((KeyedRelation)relat).getValue();
			} else if (relat instanceof KeyedRelationToChange) {
				key = ((KeyedRelationToChange)relat).getNewOne().getKey();
				value = ((KeyedRelationToChange)relat).getNewOne().getValue();
			}
			
			if (key != null) {
				GrProperty prop = rRelation.getProperty(KeyProperty);
				if (prop != null) {
					Object propValue = MappingUtil.convertFromProperty(prop.getValue(), key.getClass());
					if (!key.equals(propValue))
						prop.setValue(key);
				} else
					rRelation.addProperty(KeyProperty, key);
				
				prop = rRelation.getProperty(KeyTypeProperty);
				if (prop != null) {
					Object propValue = key.getClass().getName();
					if (!prop.getValue().equals(propValue))
						prop.setValue(propValue);
				} else
					rRelation.addProperty(KeyTypeProperty, key.getClass().getName());
			}
			
			GrProperty prop = rRelation.getProperty(ValueProperty);
			if (value != null) {
				if (prop != null) {
					Object propValue = MappingUtil.convertFromProperty(prop.getValue(), value.getClass());
					if (!value.equals(propValue))
						prop.setValue(value);
				} else
					rRelation.addProperty(ValueProperty, value);
				
				prop = rRelation.getProperty(ValueTypeProperty);
				if (prop != null) {
					Object propValue = value.getClass().getName();
					if (!prop.getValue().equals(propValue))
						prop.setValue(propValue);
				} else
					rRelation.addProperty(ValueTypeProperty, value.getClass().getName());
			} else {
				if (prop != null)
					prop.setValue(null);
				prop = rRelation.getProperty(ValueTypeProperty);
				if (prop != null)
					prop.setValue(null);
			}
		}
		
		private ObjectMapping getCompoundObjectMappingFor(CompoundObjectType cType, Object filter) {
			return new CompoundObjectMapping(cType, this.mappings, filter);
		}
		
		private ObjectMapping getObjectMappingFor(Object domainObject) {
			Class<?> clazz = domainObject.getClass();
			return getObjectMappingFor(clazz);
		}
		
		private ObjectMapping getObjectMappingFor(Class<?> clazz) {
			ObjectMapping objectMapping = this.mappings.get(clazz);
			if (objectMapping == null) {
				objectMapping = createObjectMappingFor(clazz);
				addObjectMappingForClass(clazz, objectMapping);
			}
			return objectMapping;
		}
		
		private ObjectMapping createObjectMappingFor(Class<?> clazz) {
			ObjectMapping ret;
			InternalDomainAccess internalAccess = null;
			try {
				internalAccess = MappingUtil.internalDomainAccess.get();
				MappingUtil.internalDomainAccess.set(getInternalDomainAccess());
				ret = DefaultObjectMappingCreator.createObjectMapping(clazz, this.domainModel);
			} finally {
				if (internalAccess != null)
					MappingUtil.internalDomainAccess.set(internalAccess);
				else
					MappingUtil.internalDomainAccess.remove();
			}
			return ret;
		}
		
		private FieldMapping modifyFieldMapping(FieldMapping fm, FieldMapping parentField) {
//			Class<?> clazz = fm.getField().getDeclaringClass();
//			if (clazz.equals(MapEntry.class) || clazz.equals(iot.jcypher.domain.mapping.Map.class)) {
//				FieldMappingWithParent mfm = modifiedFieldMappings.get(fm);
//				if (mfm == null) {
//					mfm = new FieldMappingWithParent(fm, parentField);
//					modifiedFieldMappings.put(fm, mfm);
//				} else
//					mfm.addParentField(parentField);
//				return mfm;
//			}
			return fm;
		}
		
		private void addObjectMappingForClass(Class<?> domainObjectClass, ObjectMapping objectMapping) {
			this.mappings.put(domainObjectClass, objectMapping);
			this.updateCompoundTypeMapWith(domainObjectClass);
			getAvailableDomainInfo().addClassLabel(domainObjectClass, objectMapping.getNodeLabelMapping().getLabel());
		}
		
		private DomainInfo loadDomainInfoIfNeeded() {
			DomainInfo ret;
			Object so = getInternalDomainAccess().getSyncObject();
			if (so != null) {
				synchronized (so) {
					ret = intLoadDomainInfoIfNeeded();
				}
			} else
				ret = intLoadDomainInfoIfNeeded();
			return ret;
		}
		
		private DomainInfo intLoadDomainInfoIfNeeded() {
			if (this.domainInfo == null) {
				ExecContext ctxt = new ExecContext();
				JcQuery query = ((DBAccessWrapper)this.dbAccess).createDomainInfoSyncQuery(ctxt);
				JcQueryResult result = ((DBAccessWrapper)this.dbAccess)
						.delegate.execute(query);
				List<JcError> errors = Util.collectErrors(result);
				if (errors.isEmpty()) {
//					Util.printResult(result, "DOMAIN INFO", Format.PRETTY_1);
					((DBAccessWrapper)this.dbAccess)
						.updateDomainInfo(result, ctxt);
				} else
					throw new JcResultException(errors);
				
				updateClassMapping(this.domainInfo);
			}
			return this.domainInfo;
		}
		
		private void updateClassMapping(DomainInfo di) {
			// update the type2CompoundTypeMap
			Set<Class<?>> classes = di.getAllStoredDomainClasses();
			Iterator<Class<?>> it = classes.iterator();
			while(it.hasNext()) {
				Class<?> clazz = it.next();
				ObjectMapping objectMapping = this.mappings.get(clazz);
				if (objectMapping == null) {
					objectMapping = createObjectMappingFor(clazz);
					this.mappings.put(clazz, objectMapping);
					this.updateCompoundTypeMapWith(clazz);
				}
			}
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
		
		private String setDomainLabel() {
			boolean useLab;
			if (this.domainInfo == null) {
				if (this.domainLabelUse == DomainLabelUse.AUTO) {
					// useDomainLabels is set correctly from db
					useLab = this.loadDomainInfoIfNeeded().useDomainLabels;
				} else {
					useLab = this.domainLabelUse == DomainLabelUse.ALWAYS;
				}
			} else
				useLab = this.domainInfo.useDomainLabels;
			String ret = CurrentDomain.label.get();
			if (useLab)
				CurrentDomain.setDomainLabel(this.getDomainLabel());
			return ret;
		}
		
		private String getDomainLabel() {
			if (this.domainLabel == null) {
				this.domainLabel = getInternalDomainAccess().buildDomainLabel(this.domainName);
			}
			return this.domainLabel;
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
			Object ret = null;
			
			try {
				if (clazz.isMemberClass()) {
					Class<?> eClass = clazz.getEnclosingClass();
					Constructor<?> constr = null;
					Constructor<?>[] constrs = clazz.getDeclaredConstructors();
					for (Constructor<?> c : constrs) {
						Class<?>[] pTypes = c.getParameterTypes();
						if (pTypes.length == 1 && pTypes[0].equals(eClass)) {
							constr = c;
							break;
						}
					}
					if (constr != null) // inner class, non static
						ret = new InnerClassSurrogate(constr);
				}
			
				if (ret == null)
					ret = clazz.newInstance();
			} catch(Throwable e) {
				throw new RuntimeException(e);
			}
			return ret;
		}
		
		private List<Class<?>> getCompoundTypesFor(Class<?> domainObjectType) {
			updateMappingsIfNeeded();
			CompoundObjectType cType = getCompoundTypeFor(domainObjectType);
			List<Class<?>> typeList = cType.getTypes(true); // no abstract types and interfaces
			// make sure we have always the same order
			Collections.sort(typeList, new Comparator<Class<?>>() {
				@Override
				public int compare(Class<?> o1, Class<?> o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			return typeList;
		}
		
		/****************************************/
		private class IdAndDepth {
			private long id;
			private int depth;
			
			private IdAndDepth(long id, int depth) {
				super();
				this.id = id;
				this.depth = depth;
			}
		}

		/****************************************/
		public class DBAccessWrapper implements IDBAccess {

			private IDBAccess delegate;
			private DomainInfo temporaryDomainInfo;
			
			private DBAccessWrapper(IDBAccess delegate) {
				super();
				this.delegate = delegate;
			}

			@Override
			public JcQueryResult execute(JcQuery query) {
				ExecContext ctxt = new ExecContext();
				QExecution qExec = DomainAccess.qExecution.get();
				JcQuery infoQuery = createDomainInfoSyncQuery(ctxt);
				if (infoQuery != null) {
					List<JcQuery> queries = new ArrayList<JcQuery>(2);
					queries.add(query);
					queries.add(infoQuery);
					Util.printQueries(queries, QueryToObserve.DOMAINACCESS_EXECUTE_INTERNAL, Format.PRETTY_1);
					List<JcQueryResult> results = this.delegate.execute(queries);
					List<JcError> errors = Util.collectErrors(results);
					if (errors.isEmpty()) {
						updateDomainInfo(results.get(1), ctxt);
					}
					if (qExec != null)
						qExec.setCheckedReloadModel(true);
					return results.get(0);
				} else {
					List<JcQuery> queries = new ArrayList<JcQuery>();
					queries.add(query);
					boolean doCheck = false;
					if (qExec != null && qExec.geExecType().shouldCheckForReload() && !qExec.isCheckedReloadModel()) {
						int[] versions = new int[]{domainAccessHandler.domainInfo.version,
								domainAccessHandler.domainModel.getVersion()};
						queries.add(createCheckInfoVersionQuery(versions));
						doCheck = true;
					}
					Util.printQueries(queries, QueryToObserve.DOMAINACCESS_EXECUTE_INTERNAL, Format.PRETTY_1);
					List<JcQueryResult> results = this.delegate.execute(queries);
					if (doCheck) {
						// check for need of reload
						if (Util.collectErrors(results).isEmpty()) {
							boolean reloaded = reloadInfo_ModelIfNeeded(results.get(1));
							if (reloaded) { // need to replay query
								JcQueryResult r = results.get(0);
								JcError err = new JcError(QExecution.REPLAY_QUERY, null, null);
								r.addGeneralError(err);
							}
						}
					}
					return results.get(0);
				}
			}

			@Override
			public List<JcQueryResult> execute(List<JcQuery> queries) {
				ExecContext ctxt = new ExecContext();
				QExecution qExec = DomainAccess.qExecution.get();
				JcQuery infoQuery = createDomainInfoSyncQuery(ctxt);
				if (infoQuery != null) {
					List<JcQuery> extQueries = new ArrayList<JcQuery>(queries.size() + 1);
					extQueries.addAll(queries);
					extQueries.add(infoQuery);
					Util.printQueries(extQueries, QueryToObserve.DOMAINACCESS_EXECUTE_INTERNAL, Format.PRETTY_1);
					List<JcQueryResult> results = this.delegate.execute(extQueries);
//					Util.printResults(results, "DOMAIN INFO", Format.PRETTY_1);
					List<JcError> errors = Util.collectErrors(results);
					if (errors.isEmpty()) {
						updateDomainInfo(results.get(queries.size()), ctxt);
					}
					if (qExec != null)
						qExec.setCheckedReloadModel(true);
					return results.subList(0, queries.size());
				} else {
					boolean doCheck = false;
					List<JcQuery> extQueries;
					if (qExec != null && qExec.geExecType().shouldCheckForReload() && !qExec.isCheckedReloadModel()) {
						int[] versions = new int[]{domainAccessHandler.domainInfo.version,
								domainAccessHandler.domainModel.getVersion()};
						extQueries = new ArrayList<JcQuery>(queries.size() + 1);
						extQueries.addAll(queries);
						extQueries.add(createCheckInfoVersionQuery(versions));
						doCheck = true;
					} else
						extQueries = queries;
					Util.printQueries(extQueries, QueryToObserve.DOMAINACCESS_EXECUTE_INTERNAL, Format.PRETTY_1);
					List<JcQueryResult> results = this.delegate.execute(extQueries);
					if (doCheck) {
						// check for need of reload
						if (Util.collectErrors(results).isEmpty()) {
							boolean reloaded = reloadInfo_ModelIfNeeded(results.get(results.size() - 1));
							if (reloaded) { // need to replay query
								JcQueryResult r = results.get(0);
								JcError err = new JcError(QExecution.REPLAY_QUERY, null, null);
								r.addGeneralError(err);
							}
						}
					}
					return results.subList(0, queries.size());
				}
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
			public ITransaction beginTX() {
				return this.delegate.beginTX();
			}

			@Override
			public ITransaction getTX() {
				return this.delegate.getTX();
			}

			@Override
			public DBType getDBType() {
				return this.delegate.getDBType();
			}

			@Override
			public void close() {
				this.delegate.close();
			}

			public IDBAccess getDelegate() {
				return delegate;
			}

			private void updateDomainInfo(JcQueryResult result, ExecContext context) {
				if (context.dInfo == Exec.INIT_LOADED) { // initial load
					JcNode mdl = new JcNode("mdl");
					List<GrNode> mdlInfos = result.resultOf(mdl);
					domainModel.loadFrom(mdlInfos);
					
					JcNode info = new JcNode("info");
					List<GrNode> rInfos = result.resultOf(info);
					DomainInfo dInfo;
					GrNode rInfo;
					rInfo = rInfos.get(0);
					dInfo = new DomainInfo(rInfo.getId());
					dInfo.initFrom(rInfo);
					DomainAccessHandler.this.domainInfo = dInfo;
					GrProperty prop = rInfo.getProperty(DomainInfoUseDomainLabelProperty);
					if (prop == null) { // domain info was newly created in graph
						// AUTO is the default setting
						if (DomainAccessHandler.this.domainLabelUse == DomainLabelUse.AUTO) {
							JcNumber infos = new JcNumber("infos");
							List<BigDecimal> cInfos = result.resultOf(infos);
							// set to true if there exist other domain info nodes
							DomainAccessHandler.this.domainInfo.useDomainLabels =
									cInfos.get(0).intValue() > 1;
						} else
							DomainAccessHandler.this.domainInfo.useDomainLabels =
									DomainAccessHandler.this.domainLabelUse == DomainLabelUse.ALWAYS;
						
						// the default is false, set changed only if default was changed
						if (DomainAccessHandler.this.domainInfo.useDomainLabels)
							DomainAccessHandler.this.domainInfo.setChanged(true);
					}
				} else if (context.dInfo == Exec.TRIED_STORE || context.dModel == Exec.TRIED_STORE) { // update info to graph
					JcNumber rDi = new JcNumber("retDi");
					JcNumber rDm = new JcNumber("retDm");
					int retDi = ((Number)result.resultOf(rDi).get(0)).intValue();
					int retDm = ((Number)result.resultOf(rDm).get(0)).intValue();
					if (retDi + retDm == 0) { // successfully stored
						if (context.dInfo == Exec.TRIED_STORE)
							DomainAccessHandler.this.domainInfo.graphUdated();
						if (context.dModel == Exec.TRIED_STORE) {
							int idx = 0;
							for (DOType t : DomainAccessHandler.this.domainModel.getUnsaved()) {
								JcNumber nid = new JcNumber("nid_".concat(String.valueOf(idx)));
								BigDecimal rNid = result.resultOf(nid).get(0);
								InternalAccess.setNodeId(t, rNid.longValue());
								idx++;
							}
							domainModel.updatedToGraph();
						}
					} else {
						domainModel.updatedToGraph(); // model was stored in any case
						ExecContext ctxt = new ExecContext();
						if (retDi != 0) // domain info was concurrently changed
							ctxt.dInfo = Exec.RELOAD_STORE;
						if (retDm != 0) // domain model was concurrently changed
							ctxt.dModel = Exec.RELOAD_STORE;
						this.handleDomainInfo_Model(ctxt); // reload and merge
					}
				} else if (context.dInfo == Exec.RELOADED_STORE || context.dModel == Exec.RELOADED_STORE) { // merge
					ExecContext ctxt = new ExecContext();
					// need  to load the model first (important for generic model)
					int v = 0;
					if (context.dModel == Exec.RELOADED_STORE) { // model reloaded after concurrent change
						JcNode mdl = new JcNode("mdl");
						List<GrNode> mdlInfos = result.resultOf(mdl);
						domainModel.mergeFrom(mdlInfos);
						// get stored model version
						if (context.dInfo != Exec.RELOADED_STORE) {
							JcNumber m_version = new JcNumber("mv");
							v = result.resultOf(m_version).get(0).intValue();
						} else {
							JcNode info = new JcNode("info");
							v = ((Number)result.resultOf(info).get(0).getProperty(DomainInfoModelVersionProperty).getValue()).intValue();
						}
						// we have stored additional model elements
						// and need to increment the stored model version by 1
						v++;
					}
					
					if (context.dInfo == Exec.RELOADED_STORE) { // domain info reloaded after concurrent change
						JcNode info = new JcNode("info");
						GrNode rInfo = result.resultOf(info).get(0);
						DomainInfo di = new DomainInfo(domainInfo.nodeId);
						di.initFrom(rInfo);
						domainInfo.updateFrom(di);
						domainInfo.version = di.version + 1;
						domainInfo.changed = true;
						updateClassMapping(domainInfo);
					} else
						ctxt.dInfo = Exec.STORE_VERSIONS;
					
					if (context.dModel == Exec.RELOADED_STORE)
						domainModel.setVersion(v);
						
					this.handleDomainInfo_Model(ctxt); // we still have to store our own changes (if there are any)
				} else if (context.dInfo == Exec.RELOADED || context.dModel == Exec.RELOADED) { // merge
					// need  to load the model first (important for generic model)
					if (context.dModel == Exec.RELOADED) { // model reloaded after concurrent change
						JcNode mdl = new JcNode("mdl");
						List<GrNode> mdlInfos = result.resultOf(mdl);
						domainModel.mergeFrom(mdlInfos);
					}
					
					if (context.dInfo == Exec.RELOADED) { // domain info reloaded after concurrent change
						JcNode info = new JcNode("info");
						GrNode rInfo = result.resultOf(info).get(0);
						DomainInfo di = new DomainInfo(domainInfo.nodeId);
						di.initFrom(rInfo);
						domainInfo.updateFrom(di);
						domainInfo.version = di.version;
						domainInfo.changed = false;
						updateClassMapping(domainInfo);
					}
				}
				
				if (this.temporaryDomainInfo != null) {
					DomainAccessHandler.this.domainInfo.updateFrom(this.temporaryDomainInfo);
					this.temporaryDomainInfo = null;
				}
				
				this.handleDomainInfo_Model(null); // make sure that still pending changes are committed to the database
			}

			private JcQuery createDomainInfoSyncQuery(ExecContext context) {
				JcQuery query = null;
				String pLab = CurrentDomain.label.get();
				CurrentDomain.setDomainLabel(null);
				try {
					if (DomainAccessHandler.this.domainInfo == null) { // initial load
						JcNode info = new JcNode("info");
						JcNode infos = new JcNode("infs");
						JcNode mdl = new JcNode("mdl");
						query = new JcQuery();
						query.setClauses(new IClause[] {
								MERGE.node(info).label(DomainInfoNodeLabel)
									.property(DomainInfoNameProperty).value(DomainAccessHandler.this.domainName),
								ON_CREATE.SET(info.property(DomainInfoVersionProperty)).to(0),
								ON_CREATE.SET(info.property(DomainInfoModelVersionProperty)).to(0),
								WITH.value(info),
								OPTIONAL_MATCH.node(infos).label(DomainInfoNodeLabel),
								SEPARATE.nextClause(),
								OPTIONAL_MATCH.node(mdl).label(domainModel.getTypeNodeName()),
								RETURN.value(info),
								RETURN.value(mdl),
								RETURN.count().value(infos).AS(new JcNumber("infos"))
						});
						context.dInfo = Exec.INIT_LOADED;
						context.dModel = Exec.INIT_LOADED;
					} else if (context.dInfo == Exec.RELOAD || context.dModel == Exec.RELOAD) {
						// reload
						List<IClause> clauses = new ArrayList<IClause>();
						List<IClause> returnClauses = new ArrayList<IClause>();
						JcNode info = new JcNode("info");
						if (context.dInfo == Exec.RELOAD) {
							clauses.add(MERGE.node(info).label(DomainInfoNodeLabel)
									.property(DomainInfoNameProperty).value(DomainAccessHandler.this.domainName));
							clauses.add(ON_CREATE.SET(info.property(DomainInfoVersionProperty)).to(0));
							clauses.add(ON_CREATE.SET(info.property(DomainInfoModelVersionProperty)).to(0));
							returnClauses.add(RETURN.value(info));
							context.dInfo = Exec.RELOADED;
						}
						if (context.dModel == Exec.RELOAD) {
							JcNode mdl = new JcNode("mdl");
							if (context.dInfo == Exec.RELOADED)
								clauses.add(WITH.value(info));
							clauses.add(OPTIONAL_MATCH.node(mdl).label(domainModel.getTypeNodeName()));
							returnClauses.add(RETURN.value(mdl));
							context.dModel = Exec.RELOADED;
						}
						clauses.addAll(returnClauses);
						query = new JcQuery();
						query.setClauses(clauses.toArray(new IClause[clauses.size()]));
					} else if (context.dInfo == Exec.RELOAD_STORE || context.dModel == Exec.RELOAD_STORE) {
						// reload and merge
						List<IClause> clauses = new ArrayList<IClause>();
						List<IClause> returnClauses = new ArrayList<IClause>();
						if (context.dInfo == Exec.RELOAD_STORE) {
							JcNode info = new JcNode("info");
							clauses.addAll(createDomainInfoStartClause(info));
							clauses.add(WITH.value(info)); // only needed with MERGE as start clause
							returnClauses.add(RETURN.value(info));
							context.dInfo = Exec.RELOADED_STORE;
						}
						if (context.dModel == Exec.RELOAD_STORE) {
							JcNode mdl = new JcNode("mdl");
							clauses.add(OPTIONAL_MATCH.node(mdl).label(domainModel.getTypeNodeName()));
							returnClauses.add(RETURN.value(mdl));
							if (context.dInfo != Exec.RELOADED_STORE) { // need to know the stored model version
								JcNode info = new JcNode("info");
								JcNumber m_version = new JcNumber("mv");
								clauses.add(0, WITH.value(info)); // only needed with MERGE as start clause
								clauses.addAll(0, createDomainInfoStartClause(info));
								returnClauses.add(RETURN.value(info.property(DomainInfoModelVersionProperty)).AS(m_version));
							}
							context.dModel = Exec.RELOADED_STORE;
						}
						clauses.addAll(returnClauses);
						query = new JcQuery();
						query.setClauses(clauses.toArray(new IClause[clauses.size()]));
					} else if (context.dInfo == Exec.STORE_VERSIONS) {
						// only store version properties in domain info
						List<IClause> diClauses = new ArrayList<IClause>();
						List<IClause> clauses = new ArrayList<IClause>();
						List<IClause> diReturn = new ArrayList<IClause>();
						int[] versions = createDomainInfoStoreClauses(diClauses);
						// build return info: 0 ... OK, 1 ... ERROR (info was concurrently changed)
						createReturnCodeClauses(clauses, diReturn, versions);
						
						// add domain info clauses as second because they change
						// the stored version properties when they write to the db
						clauses.addAll(diClauses);

						// add return clauses
						clauses.addAll(diReturn);
						query = new JcQuery();
						query.setClauses(clauses.toArray(new IClause[clauses.size()]));
						
						context.dInfo = Exec.TRIED_STORE;
					} else if (DomainAccessHandler.this.domainInfo.isChanged() ||
							DomainAccessHandler.this.domainModel.hasChanged()) { // update info to graph
						List<IClause> diClauses = new ArrayList<IClause>();
						List<IClause> diReturn = new ArrayList<IClause>();
						List<IClause>[] dmClauses = null;

						int[] versions = createDomainInfoStoreClauses(diClauses);
						
						if (DomainAccessHandler.this.domainModel.hasChanged()) {
							dmClauses = domainModel.getChangeClauses();
						}
						
						// now collect all clauses
						List<IClause> clauses = new ArrayList<IClause>();
						// build return info: 0 ... OK, 1 ... ERROR (info was concurrently changed)
						createReturnCodeClauses(clauses, diReturn, versions);
						
						if (dmClauses != null) {
							clauses.addAll(dmClauses[0]);
							// add with clauses
							
							// the following WITH clauses are only needed when using START instead of MERGE as start clause
//							JcNumber retDi = new JcNumber("retDi");
//							JcNumber retDm = new JcNumber("retDm");
//							clauses.add(WITH.value(retDi));
//							clauses.add(WITH.value(retDm));
//							clauses.addAll(dmClauses[2]);
						}
						
						// add domain info clauses as second because they change
						// the stored version properties when they write to the db
						clauses.addAll(diClauses);

						// add return clauses
						clauses.addAll(diReturn);
						if (dmClauses != null)
							clauses.addAll(dmClauses[1]);
						query = new JcQuery();
						query.setClauses(clauses.toArray(new IClause[clauses.size()]));
						
						context.dInfo = Exec.TRIED_STORE;
						if (dmClauses != null)
							context.dModel = Exec.TRIED_STORE;
					}
				} finally {
					CurrentDomain.setDomainLabel(pLab);
				}
				if (query != null)
					Util.printQuery(query, QueryToObserve.DOMAIN_INFO, Format.PRETTY_1);

				return query;
			}
			
			private void createReturnCodeClauses(List<IClause> clauses, List<IClause> diReturn, int[] versions) {
				// build return info: 0 ... OK, 1 ... ERROR (info was concurrently changed)
				JcNode info1 = new JcNode("info1");
				clauses.addAll(createDomainInfoStartClause(info1));
				JcNumber retDi = new JcNumber("retDi");
				JcNumber retDm = new JcNumber("retDm");
				clauses.add(WITH.collection(C.CREATE(
						new IClause[]{
								CASE.result(),
								WHEN.valueOf(info1.property(DomainInfoVersionProperty)).EQUALS(versions[0]),
								NATIVE.cypher("0"),
								ELSE.perform(),
								NATIVE.cypher("1"),
								END.caseXpr().AS(retDi)
						})));
				clauses.add(WITH.collection(C.CREATE(
						new IClause[]{
								CASE.result(),
								WHEN.valueOf(info1.property(DomainInfoModelVersionProperty)).EQUALS(versions[1]),
								NATIVE.cypher("0"),
								ELSE.perform(),
								NATIVE.cypher("1"),
								END.caseXpr().AS(retDm)
						})));
				diReturn.add(RETURN.value(retDi));
				diReturn.add(RETURN.value(retDm));
			}
			
			/**
			 * @param diClauses
			 * @return [domainInfoVersion, domainModelVersion]
			 */
			private int[] createDomainInfoStoreClauses(List<IClause> diClauses) {
				JcNode info = new JcNode("info");
				if (DomainAccessHandler.this.domainInfo.isChanged()) {
					List<String> class2LabelList = DomainAccessHandler.this.domainInfo.getLabel2ClassNameStringList();
					List<String> fieldComponentTypeList =
							DomainAccessHandler.this.domainInfo.getFieldComponentTypeStringList();
					List<String> concreteFieldTypeList =
							DomainAccessHandler.this.domainInfo.getConcreteFieldTypeStringList();
					diClauses.add(DO.SET(info.property(DomainInfoLabel2ClassProperty)).to(class2LabelList));
					diClauses.add(DO.SET(info.property(DomainInfoFieldComponentTypeProperty)).to(fieldComponentTypeList));
					diClauses.add(DO.SET(info.property(DomainInfoConcreteFieldTypeProperty)).to(concreteFieldTypeList));
					diClauses.add(DO.SET(info.property(DomainInfoUseDomainLabelProperty))
						.to(DomainAccessHandler.this.domainInfo.useDomainLabels));
				}
				// always write version properties to domainInfo
				diClauses.add(DO.SET(info.property(DomainInfoVersionProperty)).to(
						DomainAccessHandler.this.domainInfo.getVersion()));
				diClauses.add(DO.SET(info.property(DomainInfoModelVersionProperty)).to(
						DomainAccessHandler.this.domainModel.getVersion()));
				
				// calc versions that should be found in the db if no concurrent changes were done
				int i_version = DomainAccessHandler.this.domainInfo.getVersion();
				i_version = i_version > 0 ? i_version - 1 : i_version;
				int m_version = DomainAccessHandler.this.domainModel.getVersion();
				m_version = m_version > 0 ? m_version - 1 : m_version;
				
				// conditionally perform modifications
				IClause[] clausesArray = diClauses.toArray(new IClause[diClauses.size()]);
				diClauses.clear();
				diClauses.addAll(createDomainInfoStartClause(info));
				JcValue x = new JcValue("x");
				IClause clause = FOR_EACH.element(x).IN(C.CREATE(new IClause[]{
						CASE.result(),
						WHEN.valueOf(info.property(DomainInfoVersionProperty)).EQUALS(i_version)
								.AND().valueOf(info.property(DomainInfoModelVersionProperty)).EQUALS(m_version),
							NATIVE.cypher("[1]"),
						ELSE.perform(),
							NATIVE.cypher("[]"),
						END.caseXpr()
				})).DO(clausesArray);
				diClauses.add(clause);
				
				return new int[] {i_version, m_version};
			}
			
			/**
			 * store if needed
			 */
			private void handleDomainInfo_Model(ExecContext context) {
				// make sure that still pending changes are committed to the database
				// can happen in certain scenarios with the first domain query executed
				ExecContext ctxt = context == null ? new ExecContext() : context;
				JcQuery query = this.createDomainInfoSyncQuery(ctxt);
				if (query != null) {
					JcQueryResult uResult = this.delegate.execute(query);
					List<JcError> errors = Util.collectErrors(uResult);
					if (errors.isEmpty()) {
						updateDomainInfo(uResult, ctxt);
					} else {
						throw new JcResultException(errors, "Error on update of Domain Info!");
					}
				}
			}
			
			private List<IClause> createDomainInfoStartClause(JcNode info) {
				String pLab = CurrentDomain.label.get();
				CurrentDomain.setDomainLabel(null);
				List<IClause> ret = new ArrayList<IClause>();
				try {
	//				ret.add(START.node(info).byId(DomainAccessHandler.this.domainInfo.nodeId));
					ret.add(MERGE.node(info).label(DomainInfoNodeLabel)
							.property(DomainInfoNameProperty).value(DomainAccessHandler.this.domainName));
					ret.add(ON_CREATE.SET(info.property(DomainInfoVersionProperty)).to(0));
					ret.add(ON_CREATE.SET(info.property(DomainInfoModelVersionProperty)).to(0));
				} finally {
					CurrentDomain.setDomainLabel(pLab);
				}
				return ret;
			}
			
			private JcQuery createCheckInfoVersionQuery(int[] versions) {
				List<IClause> clauses = new ArrayList<IClause>();
				JcNode info1 = new JcNode("info1");
				clauses.addAll(createDomainInfoStartClause(info1));
				JcNumber retDi = new JcNumber("retDi");
				JcNumber retDm = new JcNumber("retDm");
				clauses.add(WITH.collection(C.CREATE(
						new IClause[]{
								CASE.result(),
								WHEN.valueOf(info1.property(DomainInfoVersionProperty)).EQUALS(versions[0]),
								NATIVE.cypher("0"),
								ELSE.perform(),
								NATIVE.cypher("1"),
								END.caseXpr().AS(retDi)
						})));
				clauses.add(WITH.collection(C.CREATE(
						new IClause[]{
								CASE.result(),
								WHEN.valueOf(info1.property(DomainInfoModelVersionProperty)).EQUALS(versions[1]),
								NATIVE.cypher("0"),
								ELSE.perform(),
								NATIVE.cypher("1"),
								END.caseXpr().AS(retDm)
						})));
				clauses.add(RETURN.value(retDi));
				clauses.add(RETURN.value(retDm));
				JcQuery query = new JcQuery();
				query.setClauses(clauses.toArray(new IClause[clauses.size()]));
				return query;
			}
			
			/**
			 * answer true if model and / or info was reloaded
			 * @param result
			 * @return
			 */
			private boolean reloadInfo_ModelIfNeeded(JcQueryResult result) {
				JcNumber rDi = new JcNumber("retDi");
				JcNumber rDm = new JcNumber("retDm");
				int retDi = ((Number)result.resultOf(rDi).get(0)).intValue();
				int retDm = ((Number)result.resultOf(rDm).get(0)).intValue();
				if (retDi + retDm == 0) // don't need to reload
					return false;
				else { // need to reload
					ExecContext ctxt = new ExecContext();
					if (retDi > 0)
						ctxt.dInfo = Exec.RELOAD;
					if (retDm > 0)
						ctxt.dModel = Exec.RELOAD;
					handleDomainInfo_Model(ctxt);
					return true;
				}
			}
		}
	}
	
	/**********************************************/
	private class ClosureCalculator {
		
		private <T> void fillModel(FillModelContext<T> context) {
			Step step = new Step();
			step.fillModel(context, null, null, null, null);
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

		private void calculateClosure(List<?> domainObjects, UpdateContext context) {
			for (Object domainObject : domainObjects) {
				recursiveCalculateClosure(domainObject, null, context, false); // don't delete
			}
		}
		
		/**
		 * @param domainObject
		 * @param context
		 * @param prepareToDelete if true, delete outgoing relations from domainObject that later on itself will be deleted
		 */
		@SuppressWarnings("unchecked")
		private void recursiveCalculateClosure(Object domainObject, FieldMapping parentField, UpdateContext context,
				boolean prepareToDelete) {
			if (!context.domainObjects.contains(domainObject)) { // avoid infinite loops
				context.domainObjects.add(domainObject);
				if (domainObject instanceof iot.jcypher.domain.mapping.surrogate.Collection) {
					iot.jcypher.domain.mapping.surrogate.Collection surrColl = (iot.jcypher.domain.mapping.surrogate.Collection)domainObject;
					if (surrColl.getContent() != null)
						surrColl.setCollType(surrColl.getContent().getClass().getName());
				}
				ObjectMapping objectMapping = domainAccessHandler.getObjectMappingFor(domainObject);
				Iterator<FieldMapping> it = objectMapping.fieldMappingsIterator();
				while (it.hasNext()) {
					FieldMapping fm = domainAccessHandler.modifyFieldMapping(it.next(), parentField);
					Object obj = fm.getObjectNeedingRelation(domainObject);
					if (obj != null && !prepareToDelete) { // definitly need relation
						if (obj instanceof Collection<?>) { // collection with non-simple elements,
																				   // we won't reach this spot with empty collections
							Collection<?> coll = (Collection<?>)obj;
							handleListArrayInClosureCalc(coll, null, domainObject, context, fm);
						} else if (obj.getClass().isArray()) { // array with non-simple elements,
																				 // we won't reach this spot with empty arrays
							Object[] array = (Object[]) obj;
							handleListArrayInClosureCalc(null, array, domainObject, context, fm);
						} else if (obj instanceof Map<?, ?>) {
							Map<Object, Object> map = (Map<Object, Object>)obj;
							handleMapInClosureCalc(map, domainObject, context, fm);
						} else {
							handleObjectInClosureCalc(obj, domainObject, context, fm);
						}
					} else {
						obj = fm.getFieldValue(domainObject);
						if (obj != null) // store class field info in DomainInfo
							MappingUtil.internalDomainAccess.get()
							.addConcreteFieldType(fm.getClassFieldName(), obj.getClass());
						if (fm.needsRelation()) { // in case obj == null because it was not set
							// no relation --> check if an old relation needs to be removed
							if (fm.getFieldKind() == FieldKind.COLLECTION ||
									fm.getFieldKind() == FieldKind.MAP ||
									fm.getFieldKind() == FieldKind.ARRAY) {
								// remove multiple relations if they exist
								List<IMapEntry> mapEntriesToRemove = handleKeyedRelationsModification(null, context,
										new SourceFieldKey(domainObject, fm.getFieldName()), false);
								removeObjectsIfNeeded(fm, context, mapEntriesToRemove);
							} else {
								// remove just a single relation if it exists
								IRelation relat = domainAccessHandler.getDomainState().findRelation(domainObject,
										fm.getPropertyOrRelationName());
								if (relat != null) {
									context.relationsToRemove.add(relat);
									if (relat.getEnd() instanceof AbstractSurrogate)
										context.surrogateChangeLog.removed.add(relat);
								}
							}
						}
					}
				}
			}
		}
		
		private void handleMapInClosureCalc(Map<Object, Object> map, Object domainObject,
				UpdateContext context, FieldMapping fm) {
			MapTerminator mapTerminator = null;
			String typ = fm.getPropertyOrRelationName();
			Map<SourceField2TargetKey, List<KeyedRelation>> keyedRelations =
					new HashMap<SourceField2TargetKey, List<KeyedRelation>>();
			List<IMapEntry> mapEntries = new ArrayList<IMapEntry>();
			List<Object> targetObjects = new ArrayList<Object>();
			// store concrete type in DomainInfo
			String classField = fm.getClassFieldName();
			MappingUtil.internalDomainAccess.get()
				.addConcreteFieldType(classField, map.getClass());
			boolean containsMapTerm = false;
			DomainState ds = domainAccessHandler.getDomainState();
			Iterator<Entry<Object, Object>> it = map.entrySet().iterator();
			while(it.hasNext()) {
				Entry<Object, Object> entry = it.next();
				Object val = entry.getValue();
				if (val instanceof Map<?, ?>)
					val = ds.getSurrogateState().getCreateSurrogateFor(val, iot.jcypher.domain.mapping.surrogate.Map.class);
				else if (val instanceof Collection<?>)
					val = ds.getSurrogateState().getCreateSurrogateFor(val, iot.jcypher.domain.mapping.surrogate.Collection.class);
				else if (val.getClass().isArray())
					val = ds.getSurrogateState().getCreateSurrogateFor(val, iot.jcypher.domain.mapping.surrogate.Array.class);
				Object key = entry.getKey();
				if (key instanceof Collection<?>)
					key = ds.getSurrogateState().getCreateSurrogateFor(key, iot.jcypher.domain.mapping.surrogate.Collection.class);
				else if (key.getClass().isArray())
					key = ds.getSurrogateState().getCreateSurrogateFor(key, iot.jcypher.domain.mapping.surrogate.Array.class);
				else if (key instanceof Map<?, ?>)
					key = ds.getSurrogateState().getCreateSurrogateFor(key, iot.jcypher.domain.mapping.surrogate.Map.class);
				boolean keyMapsToProperty = MappingUtil.mapsToProperty(key.getClass());
				boolean valMapsToProperty = MappingUtil.mapsToProperty(val.getClass());
				Object target;
				Object relationKey;
				if (keyMapsToProperty) {
					if (valMapsToProperty) {
						if (mapTerminator == null) {
							mapTerminator = new MapTerminator(domainObject, fm.getFieldName());
							mapEntries.add(mapTerminator);
							containsMapTerm = true;
						}
						target = mapTerminator;
					} else {
						target = val;
						targetObjects.add(target);
					}
					relationKey = entry.getKey();
				} else { // complex key always needs a MapEntry
					// handle it like a list for correct removal of removed entries
					MapEntry mapEntry =  new MapEntry(key, val);
					mapEntries.add(mapEntry);
					target = mapEntry;
					relationKey = (int)0;
				}
				SourceField2TargetKey s2tKey =
						new SourceField2TargetKey(domainObject, fm.getFieldName(), target);
				List<KeyedRelation> relats = keyedRelations.get(s2tKey);
				if (relats == null) {
					relats = new ArrayList<KeyedRelation>();
					keyedRelations.put(s2tKey, relats);
				}
				KeyedRelation keyedRelation = new KeyedRelation(typ, relationKey, domainObject, target);
				if (valMapsToProperty && keyMapsToProperty)
					keyedRelation.setValue(val);
				relats.add(keyedRelation);
				if (target instanceof iot.jcypher.domain.mapping.surrogate.Map)
					context.surrogateChangeLog.added.add(keyedRelation);
				
				// store component types in DomainInfo
				MappingUtil.internalDomainAccess.get()
					.addFieldComponentType(fm.getClassFieldName(),
							target.getClass());
			}
			
			List<IMapEntry> mapEntriesToRemove =
					handleKeyedRelationsModification(keyedRelations, context,
							new SourceFieldKey(domainObject, fm.getFieldName()),
							containsMapTerm);
			for (IMapEntry mapEntry : mapEntries) {
				recursiveCalculateClosure(mapEntry, fm, context, false); // don't delete
			}
			for (Object obj : targetObjects) {
				recursiveCalculateClosure(obj, fm, context, false); // don't delete
			}
			removeObjectsIfNeeded(fm, context, mapEntriesToRemove);
		}
		
		private void handleListArrayInClosureCalc(Collection<?> coll, Object[] array, Object domainObject,
				UpdateContext context, FieldMapping fm) {
			Collection<?> toIterate;
			String typ = fm.getPropertyOrRelationName();
			Map<SourceField2TargetKey, List<KeyedRelation>> keyedRelations =
					new HashMap<SourceField2TargetKey, List<KeyedRelation>>();
			List<Object> targetObjects = new ArrayList<Object>();
			// store concrete type in DomainInfo
			String classField = fm.getClassFieldName();
			if (coll != null) {
				MappingUtil.internalDomainAccess.get()
					.addConcreteFieldType(classField, coll.getClass());
				toIterate = coll;
			} else {
				MappingUtil.internalDomainAccess.get()
				.addConcreteFieldType(classField, array.getClass());
				toIterate = Arrays.asList(array);
			}
			int idx = 0;
			Iterator<?> it = toIterate.iterator();
			DomainState ds = domainAccessHandler.getDomainState();
			while(it.hasNext()) {
				Object elem = it.next();
				if (elem instanceof Collection<?>)
					elem = ds.getSurrogateState().getCreateSurrogateFor(elem,
									iot.jcypher.domain.mapping.surrogate.Collection.class);
				else if (elem.getClass().isArray())
					elem = ds.getSurrogateState().getCreateSurrogateFor(elem,
									iot.jcypher.domain.mapping.surrogate.Array.class);
				else if (elem instanceof Map<?, ?>)
					elem = ds.getSurrogateState().getCreateSurrogateFor(elem,
									iot.jcypher.domain.mapping.surrogate.Map.class);
				SourceField2TargetKey key =
						new SourceField2TargetKey(domainObject, fm.getFieldName(), elem);
				targetObjects.add(elem);
				List<KeyedRelation> relats = keyedRelations.get(key);
				if (relats == null) {
					relats = new ArrayList<KeyedRelation>();
					keyedRelations.put(key, relats);
				}
				KeyedRelation keyedRelation = new KeyedRelation(typ, idx, domainObject, elem);
				relats.add(keyedRelation);
				if (elem instanceof iot.jcypher.domain.mapping.surrogate.Collection)
					context.surrogateChangeLog.added.add(keyedRelation);
				// store component type in DomainInfo
				MappingUtil.internalDomainAccess.get()
					.addFieldComponentType(classField, elem.getClass());
				idx++;
			}
			
			handleKeyedRelationsModification(keyedRelations, context,
					new SourceFieldKey(domainObject, fm.getFieldName()), false);
			for (Object elem : targetObjects) {
				recursiveCalculateClosure(elem, fm, context, false); // don't delete
			}
		}
		
		private void handleObjectInClosureCalc(Object relatedObject, Object domainObject,
				UpdateContext context, FieldMapping fm) {
			IRelation relat = new Relation(fm.getPropertyOrRelationName(), domainObject, relatedObject);
			DomainState ds = domainAccessHandler.getDomainState();
			if (relatedObject instanceof AbstractSurrogate)
				context.surrogateChangeLog.added.add(relat);
			if (!ds.existsRelation(relat)) {
				context.relations.add(relat); // relation not in db
				
				// check if an old relation (for the same field but to another object, which is now replaced
				// by the new relation) needs to be removed.
				relat = ds.findRelation(domainObject,
						fm.getPropertyOrRelationName());
				if (relat != null) {
					context.relationsToRemove.add(relat);
				}
			}
			// store concrete type in DomainInfo
			String classField = fm.getClassFieldName();
			MappingUtil.internalDomainAccess.get()
				.addConcreteFieldType(classField, relatedObject.getClass());
			recursiveCalculateClosure(relatedObject, fm, context, false); // don't delete
		}
		
		private List<IMapEntry> handleKeyedRelationsModification(Map<SourceField2TargetKey, List<KeyedRelation>> keyedRelations,
				UpdateContext context, SourceFieldKey fieldKey, boolean containsMapTerm) {
			List<KeyedRelation> allExistingRels = new ArrayList<KeyedRelation>();
			DomainState ds = domainAccessHandler.getDomainState();
			List<KeyedRelation> allExist = ds.getKeyedRelations(fieldKey);
			if (allExist != null)
				allExistingRels.addAll(allExist);
			
			if (keyedRelations != null) {
				Iterator<Entry<SourceField2TargetKey, List<KeyedRelation>>> it = keyedRelations.entrySet().iterator();
				while(it.hasNext()) {
					Entry<SourceField2TargetKey, List<KeyedRelation>> entry = it.next();
					List<KeyedRelation> existingRels =
							ds.getKeyedRelations(entry.getKey());
					RelationsToModify toModify = calculateKeyedRelationsToModify(entry.getValue(), existingRels, allExistingRels);
					context.relations.addAll(toModify.toChange);
					context.relations.addAll(toModify.toCreate);
					context.relationsToRemove.addAll(toModify.toRemove);
				}
			}
			// in allExistingRels we have those which previously existed but don't exist in the collection or map any more
			context.relationsToRemove.addAll(allExistingRels);
			List<IMapEntry> mapEntriesToRemove = new ArrayList<IMapEntry>();
			boolean mapTermAdded = false;
			for(KeyedRelation kRel : allExistingRels) { // they are to be removed
				Object end = kRel.getEnd();
				if (end instanceof MapEntry) { // remove MapEntry
					// TODO is the check really needed
					if (!mapEntriesToRemove.contains(end))
						mapEntriesToRemove.add((MapEntry) end);
				} else if (end instanceof MapTerminator) {
					if (!containsMapTerm && !mapTermAdded) {
						mapTermAdded = true;
						mapEntriesToRemove.add((MapTerminator)end);
					}
				} else if (end instanceof AbstractSurrogate) {
					context.surrogateChangeLog.removed.add(kRel);
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
					toChange.add(new KeyedRelationToChange(existingOnes.get(idx), iRel));
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
		
		private void removeObjectsIfNeeded(FieldMapping parentField, UpdateContext context, List<IMapEntry> mapEntriesToRemove) {
			if (mapEntriesToRemove.size() > 0) {
				for(Object obj : mapEntriesToRemove) { // remove MapEntry objects
					recursiveCalculateClosure(obj, parentField, context, true); // prepare for remove
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
			 */
			@SuppressWarnings("unchecked")
			private <T> void fillModel(FillModelContext<T> context, FieldMapping fm,
					String nodeName, String relationName, GrNode parentNode) {
				DomainState ds = domainAccessHandler.getDomainState();
				boolean resetInnerClassResolution = false;
				int prevClauseRepetitionNumber = context.clauseRepetitionNumber;
				boolean isRoot = fm == null;
				String nnm;
				if (nodeName != null)
					nnm = nodeName;
				else
					nnm = this.buildNodeOrRelationName(context.path,
							DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber);
				context.setTerminatesClause(nnm);
				
				CompoundObjectType compoundType;
				if (isRoot) // root type
					compoundType = domainAccessHandler.getCompoundTypeFor(context.domainObjectClass);
				else {
					String classFieldName = fm.getClassFieldName();
					compoundType = MappingUtil.internalDomainAccess.get()
							.getConcreteFieldType(classFieldName);
				}
				Class<? extends Object> pureType = fm != null ? fm.getFieldType() : context.domainObjectClass;
				
				if (compoundType != null) { // else this field cannot have been stored in the graph earlier
					// prepare for navigation to next node
					context.path.add(new PathElement(pureType));
					
					boolean resolveDeep = true;
					boolean maxDepthReached = context.maxResolutionDepth >= 0 &&
							context.maxResolutionDepth == context.currentDepth;
					if (maxDepthReached)
						resolveDeep = false;
					else if (context.recursionExitNodes.contains(nnm)) // exit recursion
						resolveDeep = false;
					
					FieldKind fieldKind = fm != null ? fm.getFieldKind() : FieldKind.SINGLE;
					SurrogateContent surrogateContent = checkForSurrogates(context, fm, fieldKind, compoundType);
					Collection<Object> collection = surrogateContent.collection;
					Map<Object, Object> map = surrogateContent.map;
					List<Object> array = surrogateContent.array;
					if (surrogateContent.compoundType != null)
						compoundType = surrogateContent.compoundType;
					
					// initialize for loop iteration
					Set<GrRelation> relationList; // make list of relations distinct
					Iterator<GrRelation> relationsIterator = null;
					List<GrRelation> usedRelations = null;
					GrNode actNode = null;
					GrRelation actRelation = null;
					boolean loopDone = true;
					if (isRoot) {
						JcNode n = new JcNode(nnm);
						List<GrNode> nodeList = context.qResult.resultOf(n);
						if (nodeList.size() > 0) {
							actNode = nodeList.get(0); // there can only be one
							loopDone = false;
						}
					} else {
						JcRelation r = new JcRelation(relationName);
						relationList = new LinkedHashSet<GrRelation>(context.qResult.resultOf(r));
						relationsIterator = relationList.iterator();
						if (relationsIterator.hasNext()) {
							actRelation = relationsIterator.next();
							loopDone = false;
						}
						usedRelations = new ArrayList<GrRelation>();
					}
					
					Object domainObject = null;
					if (!loopDone) { // at least one result node exists for this pattern
						int initialMaxClauseRepetitionNumber = context.maxClauseRepetitionNumber;
						while (!loopDone) {
							if (!isRoot) {
								actNode = null;
								if (actRelation != null) {
									if (actRelation.getStartNode().getId() != parentNode.getId()) {
										if (relationsIterator.hasNext())
											actRelation = relationsIterator.next();
										else
											loopDone = true;
										continue;
									}
									actNode = actRelation.getEndNode();
									usedRelations.add(actRelation);
								}
							} // else is root
							
							if (actNode != null) { // null values are supported
								boolean performMapping = false;
								boolean mapProperties = true;
								// check if a domain object has already been mapped to this node
								domainObject = ds.getFrom_Id2ObjectMap(actNode.getId());
								
								if (domainObject == null) {
									Class<?> clazz = domainAccessHandler.findClassToInstantiateFor(actNode);
									if (clazz.equals(MapTerminator.class))
										domainObject = new MapTerminator(context.parentObject, fm.getFieldName());
									else
										domainObject = domainAccessHandler.createInstance(clazz);
									if (domainObject instanceof Array)
										((Array)domainObject).setSurrogateState(ds.getSurrogateState());
									GrProperty prop = actNode.getProperty(ResultHandler.lockVersionProperty);
									int v = -1;
									if (prop != null)
										v = ((Number)prop.getValue()).intValue();
									ds.add_Id2Object(domainObject, actNode.getId(), v,
											resolveDeep ? ResolutionDepth.DEEP : ResolutionDepth.SHALLOW);
									if (domainObject instanceof InnerClassSurrogate) {
										((InnerClassSurrogate)domainObject).setId2ObjectMapper(getInternalDomainAccess());
										((InnerClassSurrogate)domainObject).setNodeId(actNode.getId());
									}
									performMapping = true;
									// recursion exit
									if (!resolveDeep && !maxDepthReached) {
										if (!(domainObject instanceof InnerClassSurrogate))
											context.addRecursionExitObject(domainObject, context.currentDepth);
										else {
											if (!context.resolveInnerClasses) {
												((InnerClassSurrogate)domainObject).setRecursionExit(context);
												((InnerClassSurrogate)domainObject).setActResolutionDepth(context.currentDepth);
												context.resolveInnerClasses = true;
												resetInnerClassResolution = true;
											}
										}
									}
								} else {
									// domainObject has at least been shallowly mapped
									boolean forceMapProperties = this.handleReResolve(domainObject, ds);
									if (ds.getResolutionDepth(domainObject) !=
												ResolutionDepth.DEEP) {
										boolean removed = false;
										if (maxDepthReached) {
											context.removeRecursionExitObject(domainObject);
											removed = true;
										}
										if (resolveDeep) {
											performMapping = true;
											mapProperties = forceMapProperties; // properties have already been mapped
													// but may be forced to be re-mapped
											ds.getLoadInfoFrom_Object2IdMap(domainObject)
												.setResolutionDepth(ResolutionDepth.DEEP);
											if (!removed)
												context.removeRecursionExitObject(domainObject);
										} else { // recursion exit
											if (!maxDepthReached) {
												if (!(domainObject instanceof InnerClassSurrogate))
													context.addRecursionExitObject(domainObject, context.currentDepth);
												else {
													if (!context.resolveInnerClasses) {
														((InnerClassSurrogate)domainObject).setRecursionExit(context);
														((InnerClassSurrogate)domainObject).setActResolutionDepth(context.currentDepth);
														context.resolveInnerClasses = true;
														resetInnerClassResolution = true;
													}
												}
											}
										}
									}
								}
								
								if (fm == null) { // we are at the root level
									context.domainObject = (T) domainObject;
								}
								
								if (performMapping) {
									// need to reset if we iterate through a list
									context.maxClauseRepetitionNumber = initialMaxClauseRepetitionNumber;
									ObjectMapping objectMapping = domainAccessHandler
											.getCompoundObjectMappingFor(compoundType, domainObject);
									Iterator<FieldMapping> it = objectMapping.fieldMappingsIterator();
									boolean hasComplexFields = false;
									int idx = 0;
									while (it.hasNext()) {
										FieldMapping fMap = domainAccessHandler.modifyFieldMapping(it.next(), fm);
										idx++; // index starts with 1 so as not to mix with the root node (n_0)
										if (!objectMapping.shouldPerformFieldMapping(fMap)) {
											if (fMap.needsRelation() && resolveDeep) {
												calculateMaxClauseRepetitionNumber(context, fMap, idx);
											}
											continue;
										}
										boolean mapped = false;
										if (fMap.needsRelationOrProperty()) {
											mapped = fMap.mapPropertyToField(domainObject, actNode);
										}
										if (fMap.needsRelation()) {
											hasComplexFields = true;
											if (resolveDeep || fMap.isInnerClassRefField()) {
												if (!mapped) {
													PathElement pe = context.getLastPathElement();
													pe.fieldIndex = idx;
													context.clauseRepetitionNumber = context.maxClauseRepetitionNumber;
													pe.fieldName = fMap.getFieldName();
													pe.propOrRelName = fMap.getPropertyOrRelationName();
													pe.sourceType = fMap.getField().getDeclaringClass();
													String ndName = this.buildNodeOrRelationName(context.path,
															DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber);
													boolean needToRepeat = false;
													if (isValidNodeName(ndName, context)) {
														Object prevParent = context.parentObject;
														context.parentObject = domainObject;
														String rnm = this.buildNodeOrRelationName(context.path,
																DomainAccessHandler.RelationPrefix, context.clauseRepetitionNumber);
														context.currentDepth++;
														this.fillModel(context, fMap, ndName, rnm, actNode);
														context.currentDepth--;
														context.parentObject = prevParent;
													} else
														needToRepeat = true; // need to repeat
													context.alreadyTested.clear();
													while(needToRepeat && morePathsToTest(context, fMap, idx)) {
														pe = context.getLastPathElement();
														pe.fieldIndex = idx;
														context.clauseRepetitionNumber = context.maxClauseRepetitionNumber;
														pe.fieldName = fMap.getFieldName();
														pe.propOrRelName = fMap.getPropertyOrRelationName();
														pe.sourceType = fMap.getField().getDeclaringClass();
														ndName = this.buildNodeOrRelationName(context.path,
																DomainAccessHandler.NodePrefix, context.clauseRepetitionNumber);
														needToRepeat = false;
														if (isValidNodeName(ndName, context)) {
															Object prevParent = context.parentObject;
															context.parentObject = domainObject;
															String rnm = this.buildNodeOrRelationName(context.path,
																	DomainAccessHandler.RelationPrefix, context.clauseRepetitionNumber);
															context.currentDepth++;
															this.fillModel(context, fMap, ndName, rnm, actNode);
															context.currentDepth--;
															context.parentObject = prevParent;
														} else {
															if (moreClausesAvailable(ndName, context))
																needToRepeat = true; // need to repeat
														}
													}
													context.updateMaxClauseRepetitionNumber();
												}
											}
										} else {
											if (mapProperties && !mapped)
												fMap.mapPropertyToField(domainObject, actNode);
										}
									}
									boolean removed = false;
									if (!hasComplexFields && !resolveDeep) {
										context.removeRecursionExitObject(domainObject);
										removed = true;
										// domainObject needs no further resolution
										ds.getLoadInfoFrom_Object2IdMap(domainObject)
											.setResolutionDepth(ResolutionDepth.DEEP);
									}
									if (maxDepthReached && !removed)
										context.removeRecursionExitObject(domainObject);
								}
							}
							
							if (relationsIterator == null)
								loopDone = true;
							else {
								loopDone = !relationsIterator.hasNext();
								if (!loopDone)
									actRelation = relationsIterator.next();
							}
						} // end of loop
						
						if (!isRoot) {
							if (fieldKind == FieldKind.COLLECTION) {
								addCollectionRelations(context, usedRelations, collection,
										null, fm.getPropertyOrRelationName());
								if (collection.isEmpty()) {
									// test for empty collection, which eventually was mapped to a property
									fm.mapPropertyToField(context.parentObject, parentNode);
									domainObject = null;
								} else
									domainObject = collection;
							} else if (fieldKind == FieldKind.ARRAY) {
								if (context.parentObject instanceof Array)
									((Array)context.parentObject).setSize(usedRelations.size());
								addCollectionRelations(context, usedRelations, null,
										array, fm.getPropertyOrRelationName());
								if (array.isEmpty()) {
									// test for empty array, which eventually was mapped to a property
									fm.mapPropertyToField(context.parentObject, parentNode);
									domainObject = null;
								} else
									domainObject = array;
							} else if (fieldKind == FieldKind.MAP) {
								addMapRelations_FillMap(context, usedRelations,
										fm.getPropertyOrRelationName(), map);
								if (map.isEmpty()) {
									// test for empty map, which evantually was mapped to a property
									fm.mapPropertyToField(context.parentObject, parentNode);
								}
								domainObject = null;
							} else if (usedRelations.size() > 0) {
								GrRelation rel = usedRelations.get(0);
								Relation relat = new Relation(fm.getPropertyOrRelationName(),
										context.parentObject,
										domainObject);
								GrProperty prop = rel.getProperty(ResultHandler.lockVersionProperty);
								int v = -1;
								if (prop != null)
									v = ((Number)prop.getValue()).intValue();
								ds.add_Id2Relation(
										relat, rel.getId(), v);
								if (domainObject instanceof AbstractSurrogate)
									context.surrogateChangeLog.added.add(relat);
							}
							if (domainObject instanceof AbstractSurrogate) {
								IDeferred deferred;
								if (context.parentObject instanceof MapEntry) {
									deferred = new Surrogate2MapEntry(
										fm.getFieldName(), (MapEntry) context.parentObject, (AbstractSurrogate)domainObject);
								} else {
									// TODO are there more possibilities ? maps in lists and vice versa ?
									// no, because there is no equivalent to MapEntry stored in the graph for lists
									deferred = new Deferred2DO(fm,
											(AbstractSurrogate)domainObject, context.parentObject);
								}
								context.deferredList.add(deferred);
								domainObject = null;
							}
							if (domainObject != null) {
								fm.setFieldValue(context.parentObject, domainObject);
							}
						}
					}
					context.path.remove(context.path.size() - 1); // remove the last one
				}
				context.clauseRepetitionNumber = prevClauseRepetitionNumber;
				if (resetInnerClassResolution)
					context.resolveInnerClasses = false;
			}
			
			private boolean handleReResolve(Object domainObject, DomainState ds) {
				boolean ret = false;
				ReResolve rer = DomainAccess.this.domainAccessHandler.reResolve.get();
				if (rer != null) {
					if (!rer.reResolved.contains(domainObject)) {
						rer.reResolved.add(domainObject);
						LoadInfo li = ds.getLoadInfoFrom_Object2IdMap(domainObject);
						li.setResolutionDepth(ResolutionDepth.SHALLOW);
						ret = true;
					}
				}
				return ret;
			}

			@SuppressWarnings("unchecked")
			private <T> SurrogateContent checkForSurrogates(FillModelContext<T> context, FieldMapping fm,
					FieldKind fieldKind, CompoundObjectType compoundType) {
				SurrogateContent ret = new SurrogateContent();
				DomainState ds = domainAccessHandler.getDomainState();
				if (fieldKind == FieldKind.COLLECTION) {
					if (context.parentObject instanceof iot.jcypher.domain.mapping.surrogate.Collection) {
						ret.collection = ((iot.jcypher.domain.mapping.surrogate.Collection)context.parentObject).getContent();
					}
					String classFieldName = fm.getClassFieldName();
					ret.compoundType = MappingUtil.internalDomainAccess.get()
							.getFieldComponentType(classFieldName);
					if (ret.collection == null) {
						// select the first concrete type in the CompoundType to instantiate.
						// Most certainly there will only be one type in the CompoundType,
						// anyway it must be instantiable as it has earlier been stored to the graph
						ret.collection = (Collection<Object>) domainAccessHandler.createInstance(MappingUtil.internalDomainAccess.get()
								.getConcreteFieldType(classFieldName).getType());
						if (context.parentObject instanceof iot.jcypher.domain.mapping.surrogate.Collection) {
							((iot.jcypher.domain.mapping.surrogate.Collection)context.parentObject)
								.setContent(ret.collection);
							ds.getSurrogateState()
								.addOriginal2Surrogate(ret.collection,
										(iot.jcypher.domain.mapping.surrogate.Collection)context.parentObject);
						}
					}
				} else if (fieldKind == FieldKind.ARRAY) {
					if (context.parentObject instanceof iot.jcypher.domain.mapping.surrogate.Array) {
						ret.array = ((iot.jcypher.domain.mapping.surrogate.Array)context.parentObject).getListContent();
					}
					String classFieldName = fm.getClassFieldName();
					ret.compoundType = MappingUtil.internalDomainAccess.get()
							.getFieldComponentType(classFieldName);
					if (ret.array == null) {
						ret.array = new ObservableList<Object>();
						if (context.parentObject instanceof iot.jcypher.domain.mapping.surrogate.Array) {
							((iot.jcypher.domain.mapping.surrogate.Array)context.parentObject)
								.setListContent(ret.array);
						}
					}
				} else if (fieldKind == FieldKind.MAP) {
					if (context.parentObject instanceof iot.jcypher.domain.mapping.surrogate.Map) {
						ret.map = ((iot.jcypher.domain.mapping.surrogate.Map)context.parentObject).getContent();
					}
					String classFieldName = fm.getClassFieldName();
					ret.compoundType = MappingUtil.internalDomainAccess.get()
							.getFieldComponentType(classFieldName);
					if (ret.map == null) {
						// select the first concrete type in the CompoundType to instantiate.
						// Most certainly there will only be one type in the CompoundType,
						// anyway it must be instantiable as it has earlier been stored to the graph
						ret.map = (Map<Object, Object>) domainAccessHandler.createInstance(MappingUtil.internalDomainAccess.get()
								.getConcreteFieldType(classFieldName).getType());
						if (context.parentObject instanceof iot.jcypher.domain.mapping.surrogate.Map) {
							((iot.jcypher.domain.mapping.surrogate.Map)context.parentObject)
								.setContent(ret.map);
							ds.getSurrogateState()
								.addOriginal2Surrogate(ret.map, (iot.jcypher.domain.mapping.surrogate.Map)context.parentObject);
						}
					}
				}
				return ret;
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			private <T> void addCollectionRelations(FillModelContext<T> context, List<GrRelation> relList, Collection coll,
					List<Object> array, String relType) {
				Iterator<GrRelation> rit = relList.iterator();
				List<KeyedRelation> toResort = new ArrayList<KeyedRelation>();
				ListEntriesUpdater listUpdater = null;
				long prevIndex = -1;
				boolean needResort = false;
				DomainState ds = null;
				while(rit.hasNext()) {
					if (ds == null)
						ds = domainAccessHandler.getDomainState();
					GrRelation rel = rit.next();
					Object domainObject = ds.getFrom_Id2ObjectMap(
							rel.getEndNode().getId());
					GrProperty prop = rel.getProperty(DomainAccessHandler.KeyProperty);
					int idx = (Integer)MappingUtil.convertFromProperty(prop.getValue(), Integer.class);
					if (idx <= prevIndex)
						needResort = true;
					prevIndex = idx;
					KeyedRelation irel = new KeyedRelation(relType, idx, context.parentObject, domainObject);
					prop = rel.getProperty(ResultHandler.lockVersionProperty);
					int v = -1;
					if (prop != null)
						v = ((Number)prop.getValue()).intValue();
					ds.add_Id2Relation(irel, rel.getId(), v);
					boolean fillList = true;
					if (domainObject instanceof iot.jcypher.domain.mapping.surrogate.AbstractSurrogate) {
						if (listUpdater == null) {
							listUpdater = new ListEntriesUpdater(coll != null ? coll : array);
							context.deferredList.add(listUpdater);
						}
						IDeferred deferred = new Surrogate2ListEntry(idx, listUpdater,
								(iot.jcypher.domain.mapping.surrogate.AbstractSurrogate)domainObject);
						context.deferredList.add(deferred);
						fillList = false;
					}
					if (fillList) {
						toResort.add(irel);
					}
				}
				
				if (needResort) {
					Collections.sort(toResort, new Comparator<KeyedRelation>() {
						@Override
						public int compare(KeyedRelation o1,
								KeyedRelation o2) {
							return Integer.compare(((Integer)o1.getKey()).intValue(),
									((Integer)o2.getKey()).intValue());
						}
					});
				}
				for (KeyedRelation irel : toResort) {
					if (coll != null) {
						if (!coll.contains(irel.getEnd()))
							coll.add(irel.getEnd());
					} else {
						if (!array.contains(irel.getEnd()))
							array.add(irel.getEnd());
					}
				}
			}
			
			private <T> void addMapRelations_FillMap(FillModelContext<T> context, List<GrRelation> relList,
					String relType, Map<Object, Object> map) {
				try {
					Object start = context.parentObject;
					Map<Long, Boolean> handledRelations = new HashMap<Long, Boolean>();
					Iterator<GrRelation> rit = relList.iterator();
					DomainState ds = null;
					while(rit.hasNext()) {
						if (ds == null)
							ds = domainAccessHandler.getDomainState();
						GrRelation rel = rit.next();
						long relId = rel.getId();
						if (handledRelations.get(relId) == null) {
							handledRelations.put(relId, Boolean.TRUE);
							Object end = ds.getFrom_Id2ObjectMap(
									rel.getEndNode().getId());
							GrProperty prop = rel.getProperty(DomainAccessHandler.KeyProperty);
							GrProperty typeProp = rel.getProperty(DomainAccessHandler.KeyTypeProperty);
							Object key = MappingUtil.convertFromProperty(prop.getValue(),
									domainAccessHandler.domainModel.getClassForName(typeProp.getValue().toString()));
							KeyedRelation irel = new KeyedRelation(relType, key, start, end);
							Object val = null;
							prop = rel.getProperty(DomainAccessHandler.ValueProperty);
							if (prop != null) {
								typeProp = rel.getProperty(DomainAccessHandler.ValueTypeProperty);
								val = MappingUtil.convertFromProperty(prop.getValue(),
										domainAccessHandler.domainModel.getClassForName(typeProp.getValue().toString()));
								irel.setValue(val);
							}
							prop = rel.getProperty(ResultHandler.lockVersionProperty);
							int v = -1;
							if (prop != null)
								v = ((Number)prop.getValue()).intValue();
							ds.add_Id2Relation(irel, relId, v);
							
							boolean fillMap = true;
							if (end instanceof MapEntry) {
								// store for later update
								MapEntryUpdater deferred = new MapEntryUpdater((MapEntry)end, map);
								context.deferredList.add(deferred);
								fillMap = false;
							} else if (!(end instanceof MapTerminator)) {
								val = end;
								if (val instanceof iot.jcypher.domain.mapping.surrogate.AbstractSurrogate) {
										// key instanceof iot.jcypher.domain.mapping.surrogate.Map can not happen
									MapEntry me = new MapEntry(key, null);
									IDeferred deferred = new MapEntryUpdater(me, map);
									context.deferredList.add(deferred);
									deferred = new Surrogate2MapEntry(
											Surrogate2MapEntry.valueField, me, (iot.jcypher.domain.mapping.surrogate.AbstractSurrogate)val);
									context.deferredList.add(deferred);
									fillMap = false;
								}
							}
							
							if (fillMap)
								map.put(key, val);
						}
					}
				} catch(Throwable e) {
					throw new RuntimeException(e);
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
				pe.propOrRelName = fMap.getPropertyOrRelationName();
				pe.sourceType = fMap.getField().getDeclaringClass();
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
				if (fm == null) { // root type
					Class<?> domClass = context.domainObjectClass;
					compoundType = domainAccessHandler.getCompoundTypeFor(domClass);
				} else {
					String classFieldName = fm.getClassFieldName();
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
					if (context.getPathSize() >= domainAccessHandler.maxPathSize)
						resolveDeep = false;
					// to make sure, that the node itself is added as an end  node to the query
					if (fm != null)
						subPathWalked = true;
				}
				
				// prepare for navigation to next node
				context.path.add(new PathElement(pureType));
				
				boolean isCollection = Collection.class.isAssignableFrom(pureType);
				boolean isMap = Map.class.isAssignableFrom(pureType);
				if (isCollection || isMap) {
					compoundType = MappingUtil.internalDomainAccess.get()
							.getFieldComponentType(fm.getClassFieldName());
				}
				
				boolean terminatesClause = true;
				if (compoundType != null) { // else no instance of that class has yet been stored in the database
					ObjectMapping objectMapping = domainAccessHandler.getCompoundObjectMappingFor(compoundType, null);
					Iterator<FieldMapping> it = objectMapping.fieldMappingsIterator();
					int idx = 0;
					while (it.hasNext()) {
						FieldMapping fMap = domainAccessHandler.modifyFieldMapping(it.next(), fm);
						idx++; // index starts with 1 so as not to mix with the root node (n_0)
						if (!walkedToIndex) {
							if (idx != this.subPathIndex) // until subPathIndex is reached
								continue;
							else
								walkedToIndex = true;
						}
						
						if (fMap.needsRelation() && (resolveDeep || fMap.isInnerClassRefField())) {
							boolean needToComeBack = false;
							if (!subPathWalked) {
								terminatesClause = false;
								if (this.next == null)
									this.next = new Step();
								PathElement pe = context.getLastPathElement();
								pe.fieldIndex = idx;
								pe.fieldName = fMap.getFieldName();
								pe.propOrRelName = fMap.getPropertyOrRelationName();
								pe.sourceType = fMap.getField().getDeclaringClass();
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
			
			/***********************************/
			private class SurrogateContent {
				private Collection<Object> collection;
				private Map<Object, Object> map;
				private List<Object> array;
				private CompoundObjectType compoundType;
			}
		}
	}
	
	/***********************************/
	private class FillModelContext<T> implements IRecursionExit {
		private JcQueryResult qResult;
		private Class<T> domainObjectClass;
		private T domainObject;
		private Object parentObject;
		private List<PathElement> path;
		private List<String> queryEndNodes;
		private List<String> recursionExitNodes;
		private int clauseRepetitionNumber;
		private int maxClauseRepetitionNumber;
		private boolean terminatesClause;
		private List<String> alreadyTested;
		private List<ResolvedDepth> recursionExitObjects;
		private List<IDeferred> deferredList;
		private SurrogateChangeLog surrogateChangeLog;
		private int maxResolutionDepth;
		private int currentDepth;
		private boolean resolveInnerClasses;
		
		private FillModelContext(Class<T> domainObjectClass, JcQueryResult qResult,
				List<String> queryEndNds, List<String> recursionExitNds,
				SurrogateChangeLog surrogateChangeLog,
				int maxResolutionDepth, int startDepth) {
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
			this.recursionExitObjects = new ArrayList<ResolvedDepth>();
			this.deferredList = new ArrayList<IDeferred>();
			this.surrogateChangeLog = surrogateChangeLog;
			this.maxResolutionDepth = maxResolutionDepth;
			this.currentDepth = startDepth;
			this.resolveInnerClasses = false;
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
		
		@Override
		public void addRecursionExitObject(Object obj, int resolvedDepth) {
			if (!this.contains(this.recursionExitObjects, obj))
				this.recursionExitObjects.add(new ResolvedDepth(obj, resolvedDepth));
		}
		
		private void removeRecursionExitObject(Object obj) {
			int idx = -1;
			for (int i = 0; i < this.recursionExitObjects.size(); i++) {
				if (this.recursionExitObjects.get(i).domainObject.equals(obj)) {
					idx = i;
					break;
				}
			}
			if (idx != -1)
				this.recursionExitObjects.remove(idx);
		}
		
		private boolean contains(List<ResolvedDepth> list, Object obj) {
			for (ResolvedDepth rd : list) {
				if (rd.domainObject.equals(obj))
					return true;
			}
			return false;
		}
		
		/*********************************/
		private class ResolvedDepth {
			private Object domainObject;
			private int resolvedDepth;
			
			private ResolvedDepth(Object domainObject, int resolvedDepth) {
				super();
				this.domainObject = domainObject;
				this.resolvedDepth = resolvedDepth;
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
					if (pe.sourceType.equals(peComp.sourceType) && pe.propOrRelName.equals(peComp.propOrRelName))
						count++;
				}
			}
			return count;
		}
		
		private int getPathSize() {
			return this.path.size();
		}
	}
	
	/*********************************/
	private static class PathElement {
		private Class<?> sourceType;
		private String fieldName;
		private int fieldIndex;
		private String propOrRelName;
		
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
		private Map<Integer, QueryNode2ResultNode> nodeIndexMap;
		private Map<Integer, QueryRelation2ResultRelation> relationIndexMap;
		private boolean lockingErrors;
		private Graph graph;
		private SurrogateChangeLog surrogateChangeLog = new SurrogateChangeLog();
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
		private int version;
	}
	
	/***********************************/
	private class QueryRelation2ResultRelation {
		private JcRelation queryRelation;
		private GrRelation resultRelation;
		private int version;
	}
	
	/***********************************/
	private class DomainInfo {
		
		private boolean changed;
		private long nodeId;
		private Map<String, Class<?>> label2ClassMap;
		private Map<Class<?>, String> class2labelMap;
		private Map<String, CompoundObjectType> fieldComponentTypeMap;
		private Map<String, CompoundObjectType> concreteFieldTypeMap;
		private Map<Class<?>, List<BackwardField>> componentTypeBackward;
		private Map<Class<?>, List<BackwardField>> fieldTypeBackward;
		private boolean useDomainLabels;
		private int version;
		
		private DomainInfo(long nid) {
			super();
			this.changed = false;
			this.nodeId = nid;
			this.label2ClassMap = new HashMap<String, Class<?>>();
			this.class2labelMap = new HashMap<Class<?>, String>();
			this.fieldComponentTypeMap = new HashMap<String, CompoundObjectType>();
			this.concreteFieldTypeMap = new HashMap<String, CompoundObjectType>();
			this.componentTypeBackward = new HashMap<Class<?>, List<BackwardField>>();
			this.fieldTypeBackward = new HashMap<Class<?>, List<BackwardField>>();
			this.useDomainLabels = false;
			this.version = -1;
		}

		private int getVersion() {
			return version;
		}

		@SuppressWarnings("unchecked")
		private void initFrom(GrNode rInfo) {
			GrProperty prop = rInfo.getProperty(DomainAccessHandler.DomainInfoUseDomainLabelProperty);
			if (prop != null) {
				this.useDomainLabels = (boolean) prop.getValue();
			}
			
			prop = rInfo.getProperty(DomainAccessHandler.DomainInfoLabel2ClassProperty);
			List<String> c2l_list = null;
			if (prop != null)
				c2l_list = (List<String>) prop.getValue();
			prop = rInfo.getProperty(DomainAccessHandler.DomainInfoFieldComponentTypeProperty);
			List<String> compType_list = null;
			if (prop != null)
				compType_list = (List<String>) prop.getValue();
			prop = rInfo.getProperty(DomainAccessHandler.DomainInfoConcreteFieldTypeProperty);
			List<String> concType_list = null;
			if (prop != null)
				concType_list = (List<String>) prop.getValue();
			
			if (c2l_list != null) {
				for (String str : c2l_list) {
					String[] c2l = str.split("=");
					try {
						Class<?> clazz = domainAccessHandler.domainModel.getClassForName(c2l[1]);
						this.addClassLabel(clazz, c2l[0]);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			}
			
			if (compType_list != null) {
				for (String str : compType_list) {
					String[] c2l = str.split("=");
					String[] classes = c2l[1].split(CompoundObjectType.SEPARATOR);
					for (String cls : classes) {
						try {
							Class<?> clazz = domainAccessHandler.domainModel.getClassForName(cls);
							this.addFieldComponentType(c2l[0], clazz);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			
			if (concType_list != null) {
				for (String str : concType_list) {
					String[] c2l = str.split("=");
					String[] classes = c2l[1].split(CompoundObjectType.SEPARATOR);
					for (String cls : classes) {
						try {
							Class<?> clazz = domainAccessHandler.domainModel.getClassForName(cls);
							this.addConcreteFieldType(c2l[0], clazz);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			
			prop = rInfo.getProperty(DomainAccessHandler.DomainInfoVersionProperty);
			if (prop != null)
				this.version = ((Number)prop.getValue()).intValue();
			else
				this.version = 0;
			prop = rInfo.getProperty(DomainAccessHandler.DomainInfoModelVersionProperty);
			if (prop != null)
				DomainAccess.this.domainAccessHandler.domainModel.setVersion(((Number)prop.getValue()).intValue());
			else
				DomainAccess.this.domainAccessHandler.domainModel.setVersion(0);
			
			this.setChanged(false);
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
		
		private void setChanged(boolean changed) {
			if (!this.changed && changed)
				this.version++;
			this.changed = changed;
		}

		private void graphUdated() {
			if (this.changed) {
				ITransaction tx = domainAccessHandler.dbAccess.getTX();
				if (tx != null)
					((AbstractTransaction)tx).setDomainInfoChanged();
			}
			this.setChanged(false);
		}
		
		private void addClassLabel(Class<?> clazz, String label) {
			if (!this.class2labelMap.containsKey(clazz)) {
				this.class2labelMap.put(clazz, label);
				this.label2ClassMap.put(label, clazz);
				this.setChanged(true);
			}
		}
		
		private void addFieldComponentType(String classField, Class<?> clazz) {
			CompoundObjectType cType = this.fieldComponentTypeMap.get(classField);
			if (cType == null) {
				cType = new CompoundObjectType(clazz);
				this.fieldComponentTypeMap.put(classField, cType);
				this.setChanged(true);
			} else {
				boolean added = cType.addType(clazz);
				this.setChanged(this.changed || added);
			}
			// add for backward navigation
			BackwardField bwf = new BackwardField(classField);
			List<BackwardField> bwfs = this.componentTypeBackward.get(clazz);
			if (bwfs == null) {
				bwfs = new ArrayList<BackwardField>();
				this.componentTypeBackward.put(clazz, bwfs);
			}
			if (!bwfs.contains(bwf))
				bwfs.add(bwf);
		}
		
		private CompoundObjectType getFieldComponentType(String classField) {
			return this.fieldComponentTypeMap.get(classField);
		}
		
		private List<BackwardField> getBackwardFields(Class<?> clazz) {
			return this.fieldTypeBackward.get(clazz);
		}
		
		private List<BackwardField> getBackwardComponentFields(Class<?> clazz) {
			return this.componentTypeBackward.get(clazz);
		}
		
		private void addConcreteFieldType(String classField, Class<?> clazz) {
			CompoundObjectType cType = this.concreteFieldTypeMap.get(classField);
			if (cType == null) {
				cType = new CompoundObjectType(clazz);
				this.concreteFieldTypeMap.put(classField, cType);
				this.setChanged(true);
			} else {
				boolean added = cType.addType(clazz);
				this.setChanged(this.changed || added);
			}
			// add for backward navigation
			BackwardField bwf = new BackwardField(classField);
			List<BackwardField> bwfs = this.fieldTypeBackward.get(clazz);
			if (bwfs == null) {
				bwfs = new ArrayList<BackwardField>();
				this.fieldTypeBackward.put(clazz, bwfs);
			}
			if (!bwfs.contains(bwf))
				bwfs.add(bwf);
		}
		
		private CompoundObjectType getConcreteFieldType(String classField) {
			return this.concreteFieldTypeMap.get(classField);
		}
		
		private Class<?> getClassForLabel(String label) {
			return this.label2ClassMap.get(label);
		}
		
		private String getLabelForClass(Class<?> clazz) {
			return this.class2labelMap.get(clazz);
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
		
		/********************************/
		private class BackwardField {
			private Class<?> sourceClass;
			private String sourceFieldName;
			
			private BackwardField(String classField) {
				super();
				String[] clField = classField.split(domainAccessHandler.regexClassfieldSep);
				try {
					this.sourceClass = domainAccessHandler.domainModel.getClassForName(clField[0]);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
				this.sourceFieldName = clField[1];
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((sourceClass == null) ? 0 : sourceClass.hashCode());
				result = prime
						* result
						+ ((sourceFieldName == null) ? 0 : sourceFieldName
								.hashCode());
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
				BackwardField other = (BackwardField) obj;
				if (sourceClass == null) {
					if (other.sourceClass != null)
						return false;
				} else if (!sourceClass.equals(other.sourceClass))
					return false;
				if (sourceFieldName == null) {
					if (other.sourceFieldName != null)
						return false;
				} else if (!sourceFieldName.equals(other.sourceFieldName))
					return false;
				return true;
			}
			
		}
	}
	
	/********************************/
	private class SurrogateChangeLog {
		private Set<IRelation> added = new HashSet<IRelation>();
		private Set<IRelation> removed = new HashSet<IRelation>();
		
		private void applyChanges() {
			removed.removeAll(added);
			DomainState ds = domainAccessHandler.getDomainState();
			for (IRelation rel : added) {
				ds.getSurrogateState()
					.addReference(rel);
			}
			for (IRelation rel : removed) {
				ds.getSurrogateState()
					.removeReference(rel);
			}
			ds.getSurrogateState().removeUnreferenced();
		}
	}
	
	/**********************************/
	private enum Exec {
		INIT_LOADED, TRIED_STORE, RELOAD_STORE, RELOADED_STORE,
		RELOAD, RELOADED, STORE_VERSIONS
	}
	
	/**********************************/
	private class ExecContext {
		private Exec dInfo;
		private Exec dModel;
	}
	
	/**********************************/
	private class ReResolve {
		private Set<Object> reResolved = new HashSet<>();
	}

	/************************************/
	public interface IRecursionExit {
		public void addRecursionExitObject(Object obj, int resolvedDepth);
	}
	
	/***********************************/
	public class InternalDomainAccess {

		private Object syncObject;
		
		private InternalDomainAccess() {
			super();
		}

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
		
		public DomainState getDomainState() {
			return domainAccessHandler.getDomainState();
		}
		
		public ObjectMapping getObjectMappingFor(Class<?> domainObjectType) {
			domainAccessHandler.updateMappingsIfNeeded();
			return domainAccessHandler.getObjectMappingFor(domainObjectType);
		}
		
		public List<FieldMapping> getBackwardFieldMappings(String attribName, Class<?> domainObjectType) {
			List<FieldMapping> ret = new ArrayList<FieldMapping>();
			List<DomainInfo.BackwardField> bwfs = domainAccessHandler.domainInfo.getBackwardFields(domainObjectType);
			if (bwfs != null) {
				for (DomainInfo.BackwardField bwf : bwfs) {
					if (bwf.sourceFieldName.equals(attribName)) {
						FieldMapping fm = this.getObjectMappingFor(bwf.sourceClass).getFieldMappingForField(attribName);
						if (fm != null)
							ret.add(fm);
					}
				}
			}
			
			// add if navigated via a surrogate (e.g. Collection)
			bwfs = domainAccessHandler.domainInfo.getBackwardComponentFields(domainObjectType);
			if (bwfs != null) {
				for (DomainInfo.BackwardField bwf : bwfs) {
					if (this.isBackwardViaSurrogate(attribName, bwf)) {
						FieldMapping fm = this.getObjectMappingFor(bwf.sourceClass)
								.getFieldMappingForField(bwf.sourceFieldName);
						if (fm != null)
							ret.add(fm);
					}
				}
			}
			return ret;
		}
		
		private boolean isBackwardViaSurrogate(String attribName,
				DomainInfo.BackwardField backwardField) {
			List<DomainInfo.BackwardField> bwfs = domainAccessHandler.domainInfo.getBackwardFields(backwardField.sourceClass);
			if (bwfs != null) {
				for (DomainInfo.BackwardField bwf : bwfs) {
					if (bwf.sourceFieldName.equals(attribName)) {
						return true;
					}
				}
			}
			return false;
		}
		
		private void addBackwardFieldMappings(String attribName, Class<?> domainObjectType,
				List<FieldMapping> fms) {
			List<DomainInfo.BackwardField> bwfs = domainAccessHandler.domainInfo.getBackwardFields(domainObjectType);
			if (bwfs != null) {
				for (DomainInfo.BackwardField bwf : bwfs) {
					if (bwf.sourceFieldName.equals(attribName)) {
						FieldMapping fm = this.getObjectMappingFor(bwf.sourceClass).getFieldMappingForField(attribName);
						if (fm != null)
							fms.add(fm);
					}
				}
			}
		}
		
		public List<Class<?>> getCompoundTypesFor(Class<?> domainObjectType) {
			return domainAccessHandler.getCompoundTypesFor(domainObjectType);
		}
		
		public String getLabelForClass(Class<?> clazz) {
			domainAccessHandler.updateMappingsIfNeeded();
			return domainAccessHandler.domainInfo.getLabelForClass(clazz);
		}
		
		public Class<?> getClassForLabel(String label) {
			domainAccessHandler.updateMappingsIfNeeded();
			return domainAccessHandler.domainInfo.getClassForLabel(label);
		}
		
		public boolean existsLabel(String label) {
			domainAccessHandler.updateMappingsIfNeeded();
			return domainAccessHandler.domainInfo.getClassForLabel(label) != null;
		}
		
		public List<JcQueryResult> execute(List<JcQuery> queries) {
			return domainAccessHandler.dbAccess.execute(queries);
		}
		
		public <T> List<T> loadByIds(Class<T> domainObjectClass,
				Map<Class<?>, List<Long>> type2IdsMap, int resolutionDepth, long... ids) {
			return domainAccessHandler.loadByIds(domainObjectClass, type2IdsMap, resolutionDepth, ids);
		}
		
		public void replace_Id2Object(InnerClassSurrogate surrogate, Object domainObject,
				long nodeId) {
			domainAccessHandler.getDomainState().replace_Id2Object(surrogate, domainObject, nodeId);
		}
		
		public String setDomainLabel() {
			return domainAccessHandler.setDomainLabel();
		}
		
		public void transactionClosed(boolean failed, boolean domainInfoChanged,
				boolean noInfoNodeId) {
			synchronized (domainAccessHandler) {
				DomainState ds = domainAccessHandler.transactionState.get();
				if (ds != null) {
					domainAccessHandler.transactionState.remove();
					if (!failed) {
						domainAccessHandler.domainState = ds;
					} else { // failed (rollBack)
						if (domainInfoChanged) {
							domainAccessHandler.domainInfo.setChanged(true);
							if (noInfoNodeId)
								domainAccessHandler.domainInfo.nodeId = -1;
						}
					}
				}
				domainAccessHandler.domainModel.closeTx(failed);
			}
		}
		
		public Class<?> getClassForName(String name) throws ClassNotFoundException {
			return domainAccessHandler.domainModel.getClassForName(name);
		}
		
		public List<DomainObject> getGenericDomainObjects(List<?> objects) {
			return genericDomainAccess.getDomainObjects(objects);
		}
		
		public DomainObject getGenericDomainObject(Object object) {
			return genericDomainAccess.getDomainObject(object);
		}
		
		public void setQExecution(QExecution qExecution) {
			if (qExecution == null)
				DomainAccess.qExecution.remove();
			else
				DomainAccess.qExecution.set(qExecution);
		}
		
		public QExecution getQExecution() {
			return DomainAccess.qExecution.get();
		}
		
		public void startReResolve() {
			domainAccessHandler.reResolve.set(new ReResolve());
		}
		
		public void endReResolve() {
			domainAccessHandler.reResolve.remove();
		}
		
		/**
		 * For Testing
		 */
		public void loadDomainInfoIfNeeded() {
			domainAccessHandler.loadDomainInfoIfNeeded();
		}
		
		public DomainQuery createRecordedQuery(ReplayedQueryContext rqc, boolean doRecord) {
			return DomainAccess.this.createRecordedQuery(rqc, doRecord);
		}
		
		public GDomainQuery createRecordedGenQuery(ReplayedQueryContext rqc, boolean doRecord) {
			return ((GenericDomainAccess)DomainAccess.this.getGenericDomainAccess()).createRecordedQuery(rqc, doRecord);
		}
		
		public Object getSyncObject() {
			return syncObject;
		}

		public void setSyncObject(Object syncObject) {
			this.syncObject = syncObject;
		}
		
		public DomainModel getDomainModel() {
			return domainAccessHandler.getDomainModel();
		}
		
		public String buildDomainLabel(String domainName) {
			String ret = domainName.replace('-', '_').replace(' ', '_'); // also replace blanks
			return ret;
		}
		
		public String getDomainLabel() {
			return domainAccessHandler.getDomainLabel();
		}
		
		public IDBAccess getDBAccess() {
			return ((DomainAccessHandler.DBAccessWrapper)domainAccessHandler.dbAccess).delegate;
		}

		/**
		 * For Testing
		 * @return
		 */
		public String domainModelAsString() {
			return domainAccessHandler.getDomainModel().asString();
		}
		
		/**
		 * For Testing
		 * @return
		 */
		public int getDomainInfoVersion() {
			if (domainAccessHandler.domainInfo == null)
				return -1;
			return domainAccessHandler.domainInfo.version;
		}
		
		/**
		 * For Testing
		 * @return
		 */
		public String nurseryAsString() {
			return domainAccessHandler.domainModel.nurseryAsString();
		}
	}
}
