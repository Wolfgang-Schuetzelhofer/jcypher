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

import java.util.HashMap;
import java.util.Map;

import iot.jcypher.domainquery.api.DomainObjectMatch;

public class ReplayedQueryContext {
	
	private RecordedQuery recordedQuery;
	private Map<String, DomainObjectMatch<?>> id2DomainObjectMatch;

	ReplayedQueryContext(RecordedQuery rq) {
		super();
		this.recordedQuery = rq;
		this.id2DomainObjectMatch = new HashMap<String, DomainObjectMatch<?>>();
	}

	void addDomainObjectMatch(String id, DomainObjectMatch<?> dom) {
		this.id2DomainObjectMatch.put(id, dom);
	}
	
	public DomainObjectMatch<?> getById(String id) {
		return this.id2DomainObjectMatch.get(id);
	}

	public RecordedQuery getRecordedQuery() {
		return recordedQuery;
	}

	public Map<String, DomainObjectMatch<?>> getId2DomainObjectMatch() {
		return id2DomainObjectMatch;
	}
}
