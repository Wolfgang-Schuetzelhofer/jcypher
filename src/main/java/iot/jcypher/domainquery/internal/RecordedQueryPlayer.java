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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.InternalAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.internal.RecordedQuery.DOMatchRef;
import iot.jcypher.domainquery.internal.RecordedQuery.Invocation;
import iot.jcypher.domainquery.internal.RecordedQuery.Literal;
import iot.jcypher.domainquery.internal.RecordedQuery.Reference;
import iot.jcypher.domainquery.internal.RecordedQuery.Statement;

public class RecordedQueryPlayer {

	private Map<String, Object> id2ObjectMap;
	private ReplayedQueryContext replayedQueryContext;
	private boolean generic;
	private boolean createNew;
	
	public RecordedQueryPlayer() {
		this(false);
	}
	
	public RecordedQueryPlayer(boolean createNew) {
		super();
		this.createNew = createNew;
		this.id2ObjectMap = new HashMap<String, Object>();
	}

	/**
	 * Create a domain query from a recorded query.
	 * @param recordedQuery
	 * @param domainAccess
	 * @return
	 */
	public DomainQuery replayQuery(RecordedQuery recordedQuery, IDomainAccess domainAccess) {
		Boolean br_old = null;
		DomainQuery query;
		try {
			if (!Settings.TEST_MODE) {
				br_old = QueryRecorder.blockRecording.get();
				if (this.createNew)
					QueryRecorder.blockRecording.set(Boolean.FALSE);
				else
					QueryRecorder.blockRecording.set(Boolean.TRUE);
			}
			this.generic = false;
			this.replayedQueryContext = new ReplayedQueryContext(recordedQuery);
			query = ((IIntDomainAccess)domainAccess).getInternalDomainAccess().createRecordedQuery(this.replayedQueryContext,
					this.createNew); // do record
			this.id2ObjectMap.put(QueryRecorder.QUERY_ID, query);
			
			for (Statement stmt : recordedQuery.getStatements()) {
				replayStatement(stmt);
			}
			Map<String, Parameter> params = recordedQuery.getParameters();
			if (params != null) {
				QueryExecutor qe = InternalAccess.getQueryExecutor(query);
				Iterator<Parameter> pit = params.values().iterator();
				while(pit.hasNext()) {
					Parameter param = pit.next();
					qe.addParameter(param);
				}
			}
		} finally {
			if (!Settings.TEST_MODE)
				QueryRecorder.blockRecording.set(br_old);
		}
		
		return query;
	}
	
	/**
	 * Create a generic domain query from a recorded query.
	 * @param recordedQuery
	 * @param domainAccess
	 * @return
	 */
	public GDomainQuery replayGenericQuery(RecordedQuery recordedQuery, IGenericDomainAccess domainAccess) {
		Boolean br_old = null;
		GDomainQuery query;
		try {
			if (!Settings.TEST_MODE) {
				br_old = QueryRecorder.blockRecording.get();
				if (this.createNew)
					QueryRecorder.blockRecording.set(Boolean.FALSE);
				else
					QueryRecorder.blockRecording.set(Boolean.TRUE);
			}
			this.generic = true;
			this.replayedQueryContext = new ReplayedQueryContext(recordedQuery);
			query = ((IIntDomainAccess)domainAccess).getInternalDomainAccess().createRecordedGenQuery(this.replayedQueryContext,
					this.createNew); // do record
			this.id2ObjectMap.put(QueryRecorder.QUERY_ID, query);
			
			for (Statement stmt : recordedQuery.getStatements()) {
				replayStatement(stmt);
			}
		} finally {
			if (!Settings.TEST_MODE)
				QueryRecorder.blockRecording.set(br_old);
		}
		
		return query;
	}
	
	private Object replayStatement(Statement stmt) {
		Object ret = null;
		if (stmt instanceof Literal)
			ret = ((Literal)stmt).getValue();
		else if (stmt instanceof Invocation) {
			ret = replayInvocation((Invocation) stmt);
		} else if (stmt instanceof DOMatchRef) {
			String oid = ((DOMatchRef)stmt).getRef();
			ret = this.id2ObjectMap.get(oid);
		} else if (stmt instanceof Reference) {
			ret = ((Reference)stmt).getValue();
			addToId2ObjectMap(((Reference)stmt).getRefId(), ret);
		}
		return ret;
	}
	
	private Object replayInvocation(Invocation invocation) {
		try {
			List<Object> params = new ArrayList<Object>(invocation.getParams().size());
			Class<?>[] args = new Class[invocation.getParams().size()];
			int idx = 0;
			for (Statement stmt : invocation.getParams()) {
				Object param = replayStatement(stmt);
				params.add(param);
				args[idx] = param.getClass();
				idx++;
			}
			args = concatParams(invocation.getParams(), params, args);
			Object on = this.id2ObjectMap.get(invocation.getOnObjectRef());
			Method mthd = findMethod(invocation.getMethod(), on, args, params);
			Object ret = mthd.invoke(on, params.toArray());
			addToId2ObjectMap(invocation.getReturnObjectRef(), ret);
			return ret;
		} catch(Throwable e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			else
				throw new RuntimeException(e);
		}
	}
	
	private Class<?>[] concatParams(List<Statement> params, List<Object> params2, Class<?>[] args) {
		Statement prev = null;
		boolean concatenated = false;
		int idx = 0;
		for (Statement param : params) {
			if (prev instanceof Invocation) {
				if (param instanceof Invocation) {
					if (((Invocation)param).getOnObjectRef().equals(((Invocation)prev).getReturnObjectRef())) {
						idx--;
						params2.remove(idx);
						concatenated = true;
					}
				}
			}
			prev = param;
			idx++;
		}
		if (concatenated) {
			Class<?>[] nArgs = new Class<?>[params2.size()];
			for (int i = 0; i < params2.size(); i++) {
				nArgs[i] = params2.get(i).getClass();
			}
			return nArgs;
		} else
			return args;
	}

	private Method findMethod(String methodName, Object on, Class<?>[] args, List<Object> params)
			throws Throwable{
		Method ret = null;
		String mthdName = methodName;
		if (methodName.equals("createMatch") && !this.generic) {
			args[0] = Class.class;
			Class<?> cls = Class.forName(params.remove(0).toString());
			params.add(cls);
		} else if (methodName.equals("TO")) {
			if (!this.generic) {
				args[0] = Class.class;
				Class<?> cls = Class.forName(params.remove(0).toString());
				params.add(cls);
			} else {
				mthdName = "TO_GENERIC";
			}
		} else if (methodName.equals("TO_GENERIC")) {
			if (!this.generic) {
				args[0] = Class.class;
				Class<?> cls = Class.forName(params.remove(0).toString());
				params.add(cls);
				mthdName = "TO";
			}
		} else if (methodName.equals("AS")) {
			args[0] = Class.class;
			Class<?> cls = Class.forName(params.remove(0).toString());
			params.add(cls);
		} else if (methodName.equals("createMatchFor") && !this.generic) {
			if (args.length == 2) {
				args[1] = Class.class;
				Class<?> cls = Class.forName(params.remove(1).toString());
				params.add(cls);
			}
		}
		
		try {
			ret = on.getClass().getMethod(mthdName, args);
		} catch(NoSuchMethodException e) {
			Method[] mthds = on.getClass().getMethods();
			for (Method mthd : mthds) {
				if (mthd.getName().equals(mthdName)) {
					boolean matchVarTypes = false;
					Class<?>[] types = mthd.getParameterTypes();
					if (types.length != args.length || (types.length > 0 && types[types.length - 1].isArray())) { // handle variable arguments
						if (types.length > 0 && types[types.length - 1].isArray()) {
							if (args.length == 0 && types.length == 1)
								matchVarTypes = true;
							else if (args.length > 0) {
								if (types[types.length - 1].getComponentType().isAssignableFrom(args[args.length - 1]))
									matchVarTypes = true;
								else if (equalPrimitives(types[types.length - 1].getComponentType(), args[args.length - 1]))
									matchVarTypes = true;
							}
						}
					}
					if (matchVarTypes) {
						// adjust variable parameters
						int vparSize = params.size() - (types.length - 1);
						Object paramsArray = Array.newInstance(types[types.length - 1].getComponentType(), vparSize);
						for (int i = params.size() - 1; i >= types.length - 1; i--) {
							int idx = i - (types.length - 1);
							Array.set(paramsArray, idx, params.remove(i));
						}
						params.add(paramsArray);
						ret = mthd;
						break;
					}
					if (types.length == args.length) {
						boolean same = true;
						for (int i = 0; i < types.length; i++) {
							if (!types[i].isAssignableFrom(args[i])) {
								if (!equalPrimitives(types[i], args[i])) {
									same = false;
									break;
								}
							}
						}
						if (same) {
							ret = mthd;
							break;
						}
					}
				}
			}
			if (ret == null)
				throw e;
		}
		return ret;
	}

	private boolean equalPrimitives(Class<?> prim1, Class<?> prim2) {
		if (prim1.isPrimitive()) {
			if (Integer.class == prim2)
				return Integer.TYPE == prim1;
			else if (Long.class == prim2)
				return Long.TYPE == prim1;
			else if (Short.class == prim2)
				return Short.TYPE == prim1;
			else if (Float.class == prim2)
				return Float.TYPE == prim1;
			else if (Double.class == prim2)
				return Double.TYPE == prim1;
			else if (Boolean.class == prim2)
				return Boolean.TYPE == prim1;
		}
		return false;
	}
	
	private void addToId2ObjectMap(String id, Object value) {
		this.id2ObjectMap.put(id, value);
		if (value instanceof DomainObjectMatch<?>)
			this.replayedQueryContext.addDomainObjectMatch(id, (DomainObjectMatch<?>) value);
	}
}
