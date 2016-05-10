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

package iot.jcypher.domainquery;

import java.util.Map;

import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.QueryExecutor;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.domainquery.internal.ReplayedQueryContext;

/**
 * For internal use only
 * @author wolfgang
 *
 */
public class InternalAccess {

	public static <T> DomainObjectMatch<T> createMatch(AbstractDomainQuery query, Class<T> domainObjectType) {
		return query.createMatchInternal(domainObjectType);
	}
	
	public static void recordQuery(AbstractDomainQuery query, RecordedQuery rq) {
		query.recordQuery(rq);
	}
	
	public static void replayQuery(AbstractDomainQuery query, ReplayedQueryContext rqc) {
		query.replayQuery(rqc);
	}
	
	public static AbstractDomainQuery getDomainQuery(DomainQueryResult qr) {
		return qr.getDomainQuery();
	}
	
	public static AbstractDomainQuery getDomainQuery(CountQueryResult qr) {
		return qr.getDomainQuery();
	}
	
	public static QueryExecutor getQueryExecutor(AbstractDomainQuery q) {
		return q.getQueryExecutor();
	}
	
	public static Map<Object, String> getRecordedQueryObjects(AbstractDomainQuery q) {
		return q.getRecordedQueryObjects();
	}
	
	public static QueryPersistor createQueryPersistor(AbstractDomainQuery query, Object domAccess) {
		return new QueryPersistor(query, domAccess);
	}
	
	public static <T> QueryLoader<T> createQueryLoader(String queryName, Object domAccess) {
		return new QueryLoader<T>(queryName, domAccess);
	}
}
