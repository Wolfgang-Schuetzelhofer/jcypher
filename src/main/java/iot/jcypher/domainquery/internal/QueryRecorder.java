/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.domainquery.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import iot.jcypher.domainquery.AbstractDomainQuery;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.RecordedQuery.Invocation;
import iot.jcypher.domainquery.internal.RecordedQuery.Statement;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueElement;

public class QueryRecorder {
	
	private static ThreadLocal<QueriesPerThread> queriesPerThread =
			new ThreadLocal<QueriesPerThread>();

	public static void recordInvocation(Object on, String method, Object result, Object... params) {
		recordInvocation(false, on, method, result, params);
	}
	
	public static void recordAssignment(Object on, String method, Object result, Object... params) {
		recordInvocation(true, on, method, result, params);
	}
	
	private static void recordInvocation(boolean assign, Object on, String method, Object result, Object... params) {
		QueriesPerThread qpt = getCreateQueriesPerThread();
		RecQueryHolder rqh = null;
		if (on instanceof AbstractDomainQuery)
			rqh = getRecQueryHolder((AbstractDomainQuery)on);
		else {
			rqh = qpt.getHolderRef(on);
			if (rqh == null)
				rqh = createRecQueryHolder();
		}
		List<Statement> parameters = new ArrayList<Statement>(params.length);
		for (int i = 0; i < params.length; i++) {
			Object param = params[i];
			if (param instanceof Literal)
				parameters.add(rqh.recordedQuery.literal(((Literal)param).value));
			else if (param instanceof PlaceHolder) {
				RecQueryHolder trqh = qpt.getHolderRef(((PlaceHolder)param).value);
				if (trqh != null) {
					List<Statement> adopted = adoptStatements(trqh, rqh);
					parameters.addAll(adopted);
					qpt.removeHolderRef(((PlaceHolder)param).value);
				} else { // assume it is a literal (or a parameter)
					parameters.add(rqh.recordedQuery.literal(((PlaceHolder)param).value));
				}
			}
		}
		rqh.recordInvocation(assign, on, method, result, parameters);
		qpt.putHolderRef(result, rqh);
		return;
	}
	
	private static List<Statement> adoptStatements(RecQueryHolder from,
			RecQueryHolder to) {
		List<Statement> params = from.recordedQuery.getStatements();
		for (Statement s : params) {
			adoptStatement(from, to, s);
		}
		return params;
	}
	
	private static void adoptStatement(RecQueryHolder from,
			RecQueryHolder to, Statement s) {
		if (s instanceof Invocation) {
			String or = ((RecordedQuery<?>.Invocation)s).getOnObjectRef();
			Object o = from.id2ObjectMap.get(or);
			String id = to.object2IdMap.get(o);
			if (id == null) {
				id = to.getNextId();
				to.object2IdMap.put(o, id);
				to.id2ObjectMap.put(id, o);
			}
			((RecordedQuery<?>.Invocation) s).setOnObjectRef(id);
			
			or = ((RecordedQuery<?>.Invocation)s).getReturnObjectRef();
			o = from.id2ObjectMap.get(or);
			id = to.object2IdMap.get(o);
			if (id == null) {
				id = to.getNextId();
				to.object2IdMap.put(o, id);
				to.id2ObjectMap.put(id, o);
			}
			((RecordedQuery<?>.Invocation) s).setReturnObjectRef(id);
			
			QueriesPerThread qpt = getCreateQueriesPerThread();
			List<Statement> params = ((RecordedQuery<?>.Invocation) s).getParams();
			for (Statement stmt : params) {
				RecQueryHolder nextFrom = qpt.getHolderForQuery(stmt.getRecordedQuery());
				if (nextFrom != null)
					adoptStatement(nextFrom, to, stmt);
			}
		}
	}

	public static void recordInvocationConditional(ValueElement on, String method, Object result, Object... params) {
		QueriesPerThread qpt = queriesPerThread.get();
		if (qpt != null && !qpt.isEmpty()) {
			Object dom = ValueAccess.getAnyHint(on, APIAccess.hintKey_dom);
			if (dom != null)
				recordInvocation(on, method, result, params);
		}
	}
	
	public static void recordInvocationReplace(DomainObjectMatch<?> on, Object toReplace) {
		QueriesPerThread qpt = getCreateQueriesPerThread();
		RecQueryHolder trqh = qpt.getHolderRef(toReplace);
		RecQueryHolder rqh = qpt.getHolderRef(on);
		if (trqh != null) {
			List<Statement> stmts = trqh.recordedQuery.getStatements();
			for (Statement stmt : stmts) {
				if (stmt instanceof Invocation) {
					String onId = trqh.object2IdMap.get(on);
					if (onId == null) {
						onId = trqh.getNextId();
						trqh.object2IdMap.put(on, onId);
						trqh.id2ObjectMap.put(onId, on);
					}
					((RecordedQuery<?>.Invocation)stmt).setOnObjectRef(onId);
				}
			}
			//qpt.removeHolderRef(toReplace);
			if (rqh == null)
				qpt.putHolderRef(on, trqh);
		}
	}
	
	public static void recordCreateQuery(AbstractDomainQuery query) {
		RecordedQuery<?> rq = new RecordedQuery<AbstractDomainQuery>(query instanceof GDomainQuery);
		RecQueryHolder rqh = new RecQueryHolder(rq);
		getCreateQueriesPerThread().put(query, rqh);
	}
	
	@SuppressWarnings("unchecked")
	public static RecordedQuery<DomainQuery> getRecordedQuery(DomainQuery query) {
		return (RecordedQuery<DomainQuery>) getRecQueryHolder(query).recordedQuery;
	}
	
	@SuppressWarnings("unchecked")
	public static RecordedQuery<GDomainQuery> getRecordedQuery(GDomainQuery query) {
		return (RecordedQuery<GDomainQuery>) getRecQueryHolder(query).recordedQuery;
	}
	
	public static Literal literal(Object value) {
		return new Literal(value);
	}
	
	public static PlaceHolder placeHolder(Object value) {
		return new PlaceHolder(value);
	}
	
	private static RecQueryHolder getRecQueryHolder(AbstractDomainQuery on) {
		QueriesPerThread qpt = getCreateQueriesPerThread();
		return qpt.get(on);
	}
	
	private static RecQueryHolder createRecQueryHolder() {
		RecQueryHolder rqh = new RecQueryHolder(new RecordedQuery<AbstractDomainQuery>(false));
		return rqh;
	}

	private static QueriesPerThread getCreateQueriesPerThread() {
		QueriesPerThread qpt = queriesPerThread.get();
		if (qpt == null)  {
			qpt = new QueriesPerThread();
			queriesPerThread.set(qpt);
		}
		return qpt;
	}
	
	/*******************************/
	private static class QueriesPerThread {
		private Map<AbstractDomainQuery, RecQueryHolder> queries;
		private Map<Object, RecQueryHolder> recHolderRefs;
		private Map<RecordedQuery<?>, RecQueryHolder> query2HolderMap;
		
		private QueriesPerThread() {
			super();
			this.queries = new HashMap<AbstractDomainQuery, RecQueryHolder>();
			this.recHolderRefs = new IdentityHashMap<Object, RecQueryHolder>();
			this.query2HolderMap = new HashMap<RecordedQuery<?>, RecQueryHolder>();
		}
		
		private void put(AbstractDomainQuery key, RecQueryHolder value) {
			this.queries.put(key, value);
		}
		
		private RecQueryHolder get(AbstractDomainQuery key) {
			RecQueryHolder ret = this.queries.get(key);
			return ret;
		}
		
		private void putHolderRef(Object key, RecQueryHolder value) {
			this.recHolderRefs.put(key, value);
			this.query2HolderMap.put(value.recordedQuery, value);
		}
		
		private RecQueryHolder getHolderRef(Object key) {
			RecQueryHolder ret = this.recHolderRefs.get(key);
			return ret;
		}
		
		private RecQueryHolder getHolderForQuery(RecordedQuery<?> key) {
			RecQueryHolder ret = this.query2HolderMap.get(key);
			return ret;
		}
		
		private RecQueryHolder removeHolderRef(Object key) {
			RecQueryHolder ret = this.recHolderRefs.remove(key);
			//this.query2HolderMap.remove(ret.recordedQuery);
			return ret;
		}
		
		private boolean isEmpty() {
			return this.queries.isEmpty();
		}
	}
	
	/*******************************/
	private static class RecQueryHolder {
		
		private static final String idPrefix = "obj";
		
		private RecordedQuery<?> recordedQuery;
		private Map<Object, String> object2IdMap;
		private Map<String, Object> id2ObjectMap;
		private int lastId;

		private RecQueryHolder(RecordedQuery<?> recordedQuery) {
			super();
			this.recordedQuery = recordedQuery;
			this.object2IdMap = new HashMap<Object, String>();
			this.id2ObjectMap = new HashMap<String, Object>();
			this.lastId = -1;
		}
		
		private void recordInvocation(boolean assign, Object on, String method, Object result, List<Statement> parameters) {
			String onId = this.object2IdMap.get(on);
			if (onId == null) {
				if (on instanceof  AbstractDomainQuery)
					onId = "q";
				else
					onId = getNextId();
				this.object2IdMap.put(on, onId);
				this.id2ObjectMap.put(onId, on);
			}
			String resId = this.object2IdMap.get(result);
			if (resId == null) {
				resId = getNextId();
				this.object2IdMap.put(result, resId);
				this.id2ObjectMap.put(resId, result);
			}
			if (assign)
				this.recordedQuery.addAssignment(onId, method, resId, parameters);
			else
				this.recordedQuery.addInvocation(onId, method, resId, parameters);
		}
		
		private String getNextId() {
			this.lastId++;
			return idPrefix.concat(String.valueOf(this.lastId));
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.recordedQuery.toString());
			return sb.toString();
		}
	}
	
	/*********************************/
	public static class Literal {
		private Object value;

		public Literal(Object value) {
			super();
			this.value = value;
		}
	}
	
	/*********************************/
	public static class PlaceHolder {
		private Object value;

		public PlaceHolder(Object value) {
			super();
			this.value = value;
		}
	}
}
