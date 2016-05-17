/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
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

package iot.jcypher.domainquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.JSONConverter;
import iot.jcypher.domainquery.internal.QueryExecutor;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.domainquery.internal.RecordedQueryPlayer;
import iot.jcypher.domainquery.internal.ReplayedQueryContext;
import iot.jcypher.graph.GrNode;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.util.Util;

public class QueryLoader<T> {

	private Object domainAccess; // can be IDomainAccess or IGenericDomainAccess
	private String queryName;
	private ReplayedQueryContext replayedQueryContext;
	
	QueryLoader( String qName, Object domAccess) {
		this.queryName = qName;
		this.domainAccess = domAccess;
	}

	@SuppressWarnings("unchecked")
	public T load() {
		QueryMemento qm = loadMemento();
		if (qm != null) {
			RecordedQuery rq = new JSONConverter().fromJSON(qm.getQueryJSON());
			RecordedQueryPlayer qp = new RecordedQueryPlayer(true); // create new
			T q;
			if (isGeneric())
				q = (T) qp.replayGenericQuery(rq, (IGenericDomainAccess) this.domainAccess);
			else
				q = (T) qp.replayQuery(rq, (IDomainAccess) this.domainAccess);
			this.replayedQueryContext = ((AbstractDomainQuery) q).getReplayedQueryContext();
			QueryExecutor qe = InternalAccess.getQueryExecutor((AbstractDomainQuery) q);
			qe.queryCreationCompleted(true); // delete the replayedQueryContext
			return q;
		}
		return null;
	}
	
	/**
	 * The memento contains a JSON representation of the query as well as a Java-DSL like string representation.
	 * @return
	 */
	public QueryMemento loadMemento() {
		IDBAccess dbAccess = ((IIntDomainAccess)this.domainAccess).getInternalDomainAccess().getDBAccess();
		String qLabel = ((IIntDomainAccess)this.domainAccess).getInternalDomainAccess().getDomainLabel()
				.concat(QueryPersistor.Q_LABEL_POSTFIX);
		
		JcNode n = new JcNode("n");
		IClause[] clauses = new IClause[] {
				MATCH.node(n).label(qLabel).property(QueryPersistor.PROP_NAME).value(queryName),
				RETURN.value(n)
		};
		JcQuery q = new JcQuery();
		q.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(q);
		if (result.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			Util.appendErrorList(Util.collectErrors(result), sb);
			throw new RuntimeException(sb.toString());
		}
		List<GrNode> lgn = result.resultOf(n);
		if (lgn.size() > 0) {
			GrNode gn = lgn.get(0);
			String qJava = gn.getProperty(QueryPersistor.PROP_Q_JAVA).getValue().toString();
			String qJSON = gn.getProperty(QueryPersistor.PROP_Q_JSON).getValue().toString();
			QueryMemento qm = new QueryMemento(qJava, qJSON);
			return qm;
		}
		return null;
	}
	
	public List<String> getAugmentedDOMNames() {
		List<String> dNames;
		if (this.replayedQueryContext.getRecordedQuery().getAugmentations() != null) {
			dNames = new ArrayList<String>(
					this.replayedQueryContext.getRecordedQuery().getAugmentations().values());
			Collections.sort(dNames);
		} else
			dNames = Collections.emptyList();
		return dNames;
	}
	
	public List<String> getInternalDOMNames() {
		List<String> dNames = new ArrayList<String>(
				replayedQueryContext.getId2DomainObjectMatch().keySet());
		Collections.sort(dNames);
		return dNames;
	}
	
	public DomainObjectMatch<?> getDomainObjectMatch(String name) {
		DomainObjectMatch<?> ret = null;
		if (this.replayedQueryContext.getRecordedQuery().getAugmentations() != null) {
			Iterator<Entry<String, String>> it =
				this.replayedQueryContext.getRecordedQuery().getAugmentations().entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (entry.getValue().equals(name)) {
					ret = this.replayedQueryContext.getById(entry.getKey());
					break;
				}
			}
		}
		if (ret == null) {
			ret = this.replayedQueryContext.getById(name);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public <E> DomainObjectMatch<E> getDomainObjectMatch(String name, Class<E> type) {
		DomainObjectMatch<?> dom = this.getDomainObjectMatch(name);
		Class<?> typ = APIAccess.getDomainObjectType(dom);
		if (!type.isAssignableFrom(typ))
			throw new ClassCastException();
		return (DomainObjectMatch<E>) dom;
	}
	
	private boolean isGeneric() {
		return this.domainAccess instanceof IGenericDomainAccess;
	}
}
