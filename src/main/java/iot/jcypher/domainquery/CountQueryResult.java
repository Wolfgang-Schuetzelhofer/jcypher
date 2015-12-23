/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.QueryRecorder;

public class CountQueryResult {
	
	private AbstractDomainQuery domainQuery;

	CountQueryResult(AbstractDomainQuery domainQuery) {
		super();
		this.domainQuery = domainQuery;
	}

	/**
	 * Answer the number of domain objects
	 * @param match
	 * @return the number of domain objects
	 */
	public long countOf(DomainObjectMatch<?> match) {
		long ret;
		if (this.domainQuery.getQueryExecutor().hasBeenReplayed()) {
			ret = this.domainQuery.getQueryExecutor().getReplayedCountResult(match);
		} else {
			Boolean br_old = QueryRecorder.blockRecording.get();
			try {
				QueryRecorder.blockRecording.set(Boolean.TRUE);
				DomainObjectMatch<?> delegate = APIAccess.getDelegate(match);
				if (delegate != null) // this is a generic domain query
					ret = this.domainQuery.getQueryExecutor().getCountResult(delegate);
				else
					ret = this.domainQuery.getQueryExecutor().getCountResult(match);
			} finally {
				QueryRecorder.blockRecording.set(br_old);
			}
		}
		return ret;
	}
	
	AbstractDomainQuery getDomainQuery() {
		return this.domainQuery;
	}
}
