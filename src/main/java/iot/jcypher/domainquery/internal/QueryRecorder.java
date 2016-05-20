/************************************************************************
 * Copyright (c) 2015-2016 IoT-Solutions e.U.
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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import iot.jcypher.domainquery.AbstractDomainQuery;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.InternalAccess;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.RecordedQuery.Invocation;
import iot.jcypher.domainquery.internal.RecordedQuery.Statement;
import iot.jcypher.query.values.MathFunctions;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueElement;

public class QueryRecorder {
	
	public static final String QUERY_ID = "q";
	
	private static ThreadLocal<QueriesPerThread> queriesPerThread =
			new ThreadLocal<QueriesPerThread>();
	public static ThreadLocal<Boolean> blockRecording =
			new ThreadLocal<Boolean>() {
				@Override
				protected Boolean initialValue() {
					return Boolean.FALSE;
				}
		
	};

	/**
	 * invocations on domainQuery (q)
	 * but stacked within e.g. a SELECT_FROM(...).ELEMENTS(...) statement.
	 * Must not be directly added to the root RecordedQuery
	 * but must be encapsulated in a sub statement
	 * @param on
	 * @param method
	 * @param result
	 * @param params
	 */
	public static void recordStackedInvocation(Object on, String method, Object result, Object... params) {
		if (blockRecording.get())
			return;
		recordInvocation(false, true, false, on, method, result, params);
	}
	
	/**
	 * invocations on domainQuery (q)
	 * but stacked within e.g. a SELECT_FROM(...).ELEMENTS(...) statement.
	 * Must not be directly added to the root RecordedQuery
	 * but must be encapsulated in a sub statement
	 * @param on
	 * @param method
	 * @param result
	 * @param params
	 */
	public static void recordStackedAssignment(Object on, String method, Object result, Object... params) {
		if (blockRecording.get())
			return;
		recordInvocation(true, true, false, on, method, result, params);
	}
	
	public static void recordInvocation(Object on, String method, Object result, Object... params) {
		if (blockRecording.get())
			return;
		recordInvocation(false, false, false, on, method, result, params);
	}
	
	public static void recordInvocationNoConcat(Object on, String method, Object result, Object... params) {
		if (blockRecording.get())
			return;
		recordInvocation(false, false, true, on, method, result, params);
	}
	
	public static void recordAssignment(Object on, String method, Object result, Object... params) {
		if (blockRecording.get())
			return;
		recordInvocation(true, false, false, on, method, result, params);
	}
	
	private static void recordInvocation(boolean assign, boolean subRoot, boolean noConcat, Object on, String method,
			Object result, Object... params) {
		// subRoot true means invocations on domainQuery (q)
		// but stacked within e.g. a SELECT_FROM(...).ELEMENTS(...) statement.
		// Must not be directly added to the root RecordedQuery
		// but must be encapsulated in a sub statement
		QueriesPerThread qpt = getCreateQueriesPerThread();
		RecQueryHolder rqh = null;
		if (noConcat) {
			rqh = createRecQueryHolder();
		} else {
			if (on instanceof AbstractDomainQuery)
				rqh = getRecQueryHolder((AbstractDomainQuery)on);
			else {
				rqh = qpt.getHolderRef(on);
				if (rqh == null)
					rqh = createRecQueryHolder();
			}
		}
		
		if (subRoot && rqh.root) {
			// the parameters have already been adopted to the root
			rqh.stackLastNStatements(assign, params == null ? 0 : params.length, on, method, result);
		} else {
			List<Statement> parameters = new ArrayList<Statement>(params.length);
			for (int i = 0; i < params.length; i++) {
				Object param = params[i];
				if (param instanceof Literal)
					parameters.add(rqh.recordedQuery.literal(((Literal)param).value));
				else if (param instanceof PlaceHolder) {
					Object val = ((PlaceHolder)param).value;
					if (val instanceof DomainObjectMatch<?>) {
						String oid = rqh.object2IdMap.get(val);
						parameters.add(rqh.recordedQuery.doMatchRef(oid));
					} else {
						RecQueryHolder trqh = qpt.getHolderRef(val);
						if (trqh != null) {
							List<Statement> adopted = adoptStatements(trqh, rqh);
							parameters.addAll(adopted);
							qpt.removeHolderRef(val);
						} else { // assume it is a literal (or a parameter)
							parameters.add(rqh.recordedQuery.literal(val));
						}
					}
				} else if (param instanceof Reference) {
					Object val = ((Reference)param).value;
					parameters.add(rqh.recordedQuery.reference(val, rqh.getNextRefId()));
				}
			}
			rqh.recordInvocation(assign, on, method, result, parameters);
			qpt.putHolderRef(result, rqh);
			if (rqh.root) {
				qpt.removeFromQuery2HolderMap(rqh, null, false, null); // don't remove root
			}
		}
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
			String or = ((RecordedQuery.Invocation)s).getOnObjectRef();
			Object o = from.id2ObjectMap.get(or);
			String id = to.object2IdMap.get(o);
			if (id == null) {
				id = to.getNextId();
				to.object2IdMap.put(o, id);
				to.id2ObjectMap.put(id, o);
			}
			((RecordedQuery.Invocation) s).setOnObjectRef(id);
			
			or = ((RecordedQuery.Invocation)s).getReturnObjectRef();
			o = from.id2ObjectMap.get(or);
			id = to.object2IdMap.get(o);
			if (id == null) {
				id = to.getNextId();
				to.object2IdMap.put(o, id);
				to.id2ObjectMap.put(id, o);
			}
			((RecordedQuery.Invocation) s).setReturnObjectRef(id);
			
			QueriesPerThread qpt = getCreateQueriesPerThread();
			List<Statement> params = ((RecordedQuery.Invocation) s).getParams();
			for (Statement stmt : params) {
				RecQueryHolder nextFrom = qpt.getHolderForQuery(stmt.getRecordedQuery());
				if (nextFrom != null)
					adoptStatement(nextFrom, to, stmt);
			}
		}
		to.addAdopted(from);
	}

	public static void recordInvocationConditional(ValueElement on, String method, Object result, Object... params) {
		if (blockRecording.get())
			return;
		QueriesPerThread qpt = queriesPerThread.get();
		if (qpt != null && !qpt.isEmpty()) {
			Object dom = ValueAccess.getAnyHint(on, APIAccess.hintKey_dom);
			if (dom != null)
				recordInvocation(on, method, result, params);
		}
	}
	
	public static void recordInvocationConditional(MathFunctions on, String method, Object result, Object... params) {
		if (blockRecording.get())
			return;
		QueriesPerThread qpt = queriesPerThread.get();
		if (qpt != null && !qpt.isEmpty()) {
			Object dom = ValueAccess.getAnyHint(ValueAccess.getArgument(on), APIAccess.hintKey_dom);
			if (dom != null)
				recordInvocation(on, method, result, params);
		}
	}
	
	public static void recordInvocationReplace(DomainObjectMatch<?> on, Object toReplace,
			String methodName) {
		if (blockRecording.get())
			return;
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
					((RecordedQuery.Invocation)stmt).setOnObjectRef(onId);
					((RecordedQuery.Invocation)stmt).setMethod(methodName);
				}
			}
			//qpt.removeHolderRef(toReplace);
			if (rqh == null)
				qpt.putHolderRef(on, trqh);
			else
				rqh.addReplaced(trqh);
		}
	}
	
	public static void recordCreateQuery(AbstractDomainQuery query) {
		if (blockRecording.get())
			return;
		RecordedQuery rq = new RecordedQuery(query instanceof GDomainQuery);
		RecQueryHolder rqh = new RecQueryHolder(rq);
		rqh.root = true;
		getCreateQueriesPerThread().put(query, rqh);
	}
	
	public static void queryCompleted(AbstractDomainQuery query) {
		if (blockRecording.get())
			return;
		QueryExecutor qe = InternalAccess.getQueryExecutor((AbstractDomainQuery) query);
		boolean done = qe.queryCreationCompleted(false);
		if (!done) {
			QueriesPerThread qpt = queriesPerThread.get();
			if (qpt != null) {
				qpt.queryCompleted(query);
			}
		}
	}
	
	public static RecordedQuery getRecordedQuery(AbstractDomainQuery query) {
		if (blockRecording.get())
			return null;
		RecQueryHolder rqh = getRecQueryHolder(query);
		if (rqh != null)
			return rqh.recordedQuery;
		return null;
	}
	
	public static Literal literal(Object value) {
		return new Literal(value);
	}
	
	public static PlaceHolder placeHolder(Object value) {
		return new PlaceHolder(value);
	}
	
	public static Reference reference(Object value) {
		return new Reference(value);
	}
	
	private static RecQueryHolder getRecQueryHolder(AbstractDomainQuery on) {
		QueriesPerThread qpt = getCreateQueriesPerThread();
		return qpt.get(on);
	}
	
	private static RecQueryHolder createRecQueryHolder() {
		RecQueryHolder rqh = new RecQueryHolder(new RecordedQuery(false));
		return rqh;
	}

	public static QueriesPerThread getCreateQueriesPerThread() {
		QueriesPerThread qpt = queriesPerThread.get();
		if (qpt == null)  {
			qpt = new QueriesPerThread();
			queriesPerThread.set(qpt);
		}
		return qpt;
	}
	
	public static QueriesPerThread getQueriesPerThread() {
		return queriesPerThread.get();
	}
	
	/*******************************/
	public static class QueriesPerThread {
		private Map<AbstractDomainQuery, RecQueryHolder> queries;
		private Map<Object, RecQueryHolder> recHolderRefs;
		private Map<RecordedQuery, RecQueryHolder> query2HolderMap;
		
		private QueriesPerThread() {
			super();
			this.queries = new HashMap<AbstractDomainQuery, RecQueryHolder>();
			this.recHolderRefs = new IdentityHashMap<Object, RecQueryHolder>();
			this.query2HolderMap = new HashMap<RecordedQuery, RecQueryHolder>();
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
		
		private RecQueryHolder getHolderForQuery(RecordedQuery key) {
			RecQueryHolder ret = this.query2HolderMap.get(key);
			return ret;
		}
		
		private RecQueryHolder removeHolderRef(Object key) {
			RecQueryHolder ret = this.recHolderRefs.remove(key);
			//this.query2HolderMap.remove(ret.recordedQuery);
			return ret;
		}
		
		private void removeFromQuery2HolderMap(RecQueryHolder rqh, RecQueryHolder par,
				boolean removeRoot, Set<RecQueryHolder> recursionSet) {
			if (recursionSet == null)
				recursionSet = new HashSet<RecQueryHolder>();
			recursionSet.add(rqh);
			if (!rqh.root || removeRoot) {
				this.query2HolderMap.remove(rqh.recordedQuery);
				if (par != null)
					par.adopted.remove(rqh);
			}
			ArrayList<RecQueryHolder> adopted = new ArrayList<RecQueryHolder>();
			adopted.addAll(rqh.adopted);
			for (RecQueryHolder qh : adopted) {
				if (!recursionSet.contains(qh))
					this.removeFromQuery2HolderMap(qh, rqh, removeRoot, recursionSet);
			}
		}
		
		private boolean isEmpty() {
			return this.queries.isEmpty();
		}
		
		/**
		 * For testing purposes
		 * @return
		 */
		public boolean isCleared() {
			if (Settings.TEST_MODE) {
				System.gc();
				System.runFinalization();
			}
			return this.queries.isEmpty() &&
					this.recHolderRefs.isEmpty() &&
					this.query2HolderMap.isEmpty();
		}
		
		public void queryCompleted(AbstractDomainQuery query) {
			RecQueryHolder rqh = this.queries.remove(query);
			if (rqh != null) {
				this.removeFromQuery2HolderMap(rqh, null, true, null); // also remove root
				List<Object> toRemove = new ArrayList<Object>();
				Iterator<Entry<Object, RecQueryHolder>> it = this.recHolderRefs.entrySet().iterator();
				while(it.hasNext()) {
					Entry<Object, RecQueryHolder> e = it.next();
					if (e.getValue() == rqh)
						toRemove.add(e.getKey());
					else {
						if (rqh.inReplaced(e.getValue()))
							toRemove.add(e.getKey());
					}
				}
				for (Object o : toRemove) 
					this.recHolderRefs.remove(o);
			}
		}
		
		/**
		 * answer a map of DomainObjectMatch(es) to recorded query ids.
		 * <br/>Note: this destroys the original object2IdMap and may be called
		 * <br/>only after query recording has been completed.
		 * @param q
		 * @return
		 */
		public Map<Object, String> getDOM2IdMap(AbstractDomainQuery q) {
			RecQueryHolder rqh = this.get(q);
			if (rqh != null) {
				List<Object> remove = new ArrayList<Object>();
				Iterator<Object> it = rqh.object2IdMap.keySet().iterator();
				while(it.hasNext()) {
					Object o = it.next();
					if (!(o instanceof DomainObjectMatch<?>))
						remove.add(o);
				}
				for(Object o : remove)
					rqh.object2IdMap.remove(o);
				return rqh.object2IdMap;
			}
			return null;
		}
	}
	
	/*******************************/
	private static class RecQueryHolder {
		
		private static final String idPrefix = "obj";
		private static final String refIdPrefix = "ref_";
		
		private boolean root;
		private RecordedQuery recordedQuery;
		private Map<Object, String> object2IdMap;
		private Map<String, Object> id2ObjectMap;
		private List<RecQueryHolder> adopted;
		private List<RecQueryHolder> replaced;
		private int lastId;
		private int lastRefId;

		private RecQueryHolder(RecordedQuery recordedQuery) {
			super();
			this.recordedQuery = recordedQuery;
			this.object2IdMap = new HashMap<Object, String>();
			this.id2ObjectMap = new HashMap<String, Object>();
			this.adopted = new ArrayList<RecQueryHolder>();
			this.replaced = new ArrayList<RecQueryHolder>();
			this.lastId = -1;
			this.lastRefId = -1;
			this.root = false;
		}
		
		private void recordInvocation(boolean assign, Object on, String method, Object result, List<Statement> parameters) {
			String[] ids = getIds(on, result);
			if (assign)
				this.recordedQuery.addAssignment(ids[0], method, ids[1], parameters);
			else
				this.recordedQuery.addInvocation(ids[0], method, ids[1], parameters);
		}
		
		private String getRefId(Object ref) {
			String refId = this.object2IdMap.get(ref);
			if (refId == null) {
				refId = getNextRefId();
				this.object2IdMap.put(ref, refId);
				this.id2ObjectMap.put(refId, ref);
			}
			return refId;
		}
		
		/**
		 * @param on
		 * @param result
		 * @return [onId, resId]
		 */
		private String[] getIds(Object on, Object result) {
			String onId = this.object2IdMap.get(on);
			if (onId == null) {
				if (on instanceof  AbstractDomainQuery)
					onId = QUERY_ID;
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
			return new String[]{onId, resId};
		}
		
		private String getNextId() {
			this.lastId++;
			return idPrefix.concat(String.valueOf(this.lastId));
		}
		
		private String getNextRefId() {
			this.lastRefId++;
			return refIdPrefix.concat(String.valueOf(this.lastRefId));
		}
		
		private void addAdopted(RecQueryHolder qh) {
			if (!this.adopted.contains(qh))
				this.adopted.add(qh);
		}
		
		private void addReplaced(RecQueryHolder qh) {
			if (!this.replaced.contains(qh))
				this.replaced.add(qh);
		}
		
		private boolean inReplaced(RecQueryHolder rqh) {
			int idx = this.replaced.indexOf(rqh);
			return idx >= 0;
		}
		
		private void stackLastNStatements(boolean assign, int n, Object on, String method, Object result) {
			List<Statement> stmts = new ArrayList<Statement>(n);
			if (n > 0) {
				int addOffs = 0;
				Statement stmt = null;
				Statement following = null;
				List<Statement> qstmts = this.recordedQuery.getStatements();
				int offs = qstmts.size() - 1;
				for (int i = 0; i < n +addOffs; i++) {
					stmt = qstmts.remove(offs - i);
					stmts.add(0, stmt);
					// adjustment for concatenated statements
					if (following instanceof Invocation && stmt instanceof Invocation) {
						if (((Invocation)following).getOnObjectRef().equals(
								((Invocation)stmt).getReturnObjectRef()))
							addOffs++;
					}
					following = stmt;
				}
				// now follow the concatenations of the first stacked statement
				offs = qstmts.size() - 1;
				while (offs >= 0) {
					Statement prev = qstmts.get(offs);
					boolean goOn = false;
					if (prev instanceof Invocation && stmt instanceof Invocation) {
						if (((Invocation)stmt).getOnObjectRef().equals(
								((Invocation)prev).getReturnObjectRef())) {
							qstmts.remove(offs);
							stmts.add(0, prev);
							stmt = prev;
							offs--;
							goOn = true;
						}
					}
					if (!goOn)
						break;
				}
			}
			String[] ids = getIds(on, result);
			if (assign)
				this.recordedQuery.addAssignment(ids[0], method, ids[1], stmts);
			else
				this.recordedQuery.addInvocation(ids[0], method, ids[1], stmts);
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
	
	/*********************************/
	public static class Reference {
		private Object value;

		public Reference(Object value) {
			super();
			this.value = value;
		}
	}
}
