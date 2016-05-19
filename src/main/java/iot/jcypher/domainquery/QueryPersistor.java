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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.JSONConverter;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.MERGE;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.Util;

public class QueryPersistor {
	
	static final String Q_LABEL_POSTFIX = "_query";
	static final String PROP_NAME = "name";
	static final String PROP_Q_JSON = "queryJSON";
	static final String PROP_Q_JAVA = "queryJava";

	private AbstractDomainQuery query;
	private Object domainAccess; // can be IDomainAccess or IGenericDomainAccess
	private Map<DomainObjectMatch<?>, String> augmentations;
	
	private Format prettyFormat;
	
	QueryPersistor(AbstractDomainQuery query, Object domAccess) {
		this.query = query;
		this.domainAccess = domAccess;
		this.prettyFormat = Format.NONE;
	}
	
	/**
	 * The memento contains a JSON representation of the query as well as a Java-DSL like string representation.
	 * @return
	 */
	public QueryMemento createMemento() {
		RecordedQuery rq = this.query.getRecordedQuery();
		if (this.augmentations != null) {
			Map<Object, String> domMap = InternalAccess.getRecordedQueryObjects(this.query);
			Map<String, String> augments = new HashMap<String, String>();
			Iterator<Entry<DomainObjectMatch<?>, String>> it = this.augmentations.entrySet().iterator();
			while(it.hasNext()) {
				Entry<DomainObjectMatch<?>, String> entry = it.next();
				DomainObjectMatch<?> dom = entry.getKey();
				DomainObjectMatch<?> delegate = APIAccess.getDelegate(dom);
				if (delegate != null)
					dom = delegate;
				augments.put(domMap.get(dom), entry.getValue());
			}
			rq.setAugmentations(augments);
		}
		QueryMemento ret = new QueryMemento(rq.toString(), new JSONConverter().setPrettyFormat(this.prettyFormat).toJSON(rq));
		
		return ret;
	}

	/**
	 * Store the query with the domain model under the given name.
	 * @param queryName
	 */
	public void storeAs(String queryName) {
		QueryMemento qm = this.createMemento();
		IDBAccess dbAccess = ((IIntDomainAccess)this.domainAccess).getInternalDomainAccess().getDBAccess();
		String qLabel = ((IIntDomainAccess)this.domainAccess).getInternalDomainAccess().getDomainLabel()
				.concat(Q_LABEL_POSTFIX);
		
		JcNode n = new JcNode("n");
		IClause[] clauses = new IClause[] {
				MERGE.node(n).label(qLabel).property(PROP_NAME).value(queryName),
				DO.SET(n.property(PROP_Q_JAVA)).to(qm.getQueryJava()),
				DO.SET(n.property(PROP_Q_JSON)).to(qm.getQueryJSON())
		};
		JcQuery q = new JcQuery();
		q.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(q);
		if (result.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			Util.appendErrorList(Util.collectErrors(result), sb);
			throw new RuntimeException(sb.toString());
		}
		return;
	}
	
	/**
	 * Give a name to a DomainObjectMatch for better readability in a Java-DSL like string representation
	 * @param domainObjectMatch
	 * @param as
	 * @return
	 */
	public QueryPersistor augment(DomainObjectMatch<?> domainObjectMatch, String as) {
		if (this.augmentations == null)
			this.augmentations = new HashMap<DomainObjectMatch<?>, String>();
		this.augmentations.put(domainObjectMatch, as);
		return this;
	}

	/**
	 * Support for pretty printing the JSON representation
	 * @param prettyFormat
	 * @return
	 */
	public QueryPersistor setPrettyFormat(Format prettyFormat) {
		this.prettyFormat = prettyFormat;
		return this;
	}
}
