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

package iot.jcypher.domainquery;

import java.util.List;

import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.QueryRecorder;

public class DomainQueryResult {
	
	private AbstractDomainQuery domainQuery;

	DomainQueryResult(AbstractDomainQuery domainQuery) {
		super();
		this.domainQuery = domainQuery;
	}
	
	/**
	 * Answer the matching domain objects
	 * @param match
	 * @return a list of matching domain objects
	 */
	public <T> List<T> resultOf(DomainObjectMatch<T> match) {
		return resultOf(match, false);
	}

	/**
	 * Answer the matching domain objects
	 * @param match
	 * @param forceResolve force resolving domain objects even if they have been resolved prevoiusly
	 * @return a list of matching domain objects
	 */
	public <T> List<T> resultOf(DomainObjectMatch<T> match, boolean forceResolve) {
		List<T> ret;
		Object so = InternalAccess.getQueryExecutor(this.domainQuery).getMappingInfo()
				.getInternalDomainAccess().getSyncObject();
		if (so != null) {
			synchronized (so) {
				ret = intResultOf(match, forceResolve);
			}
		} else
			ret = intResultOf(match, forceResolve);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> intResultOf(DomainObjectMatch<T> match, boolean forceResolve) {
		List<T> ret;
		try {
			if (forceResolve)
				this.domainQuery.getQueryExecutor().getMappingInfo()
					.getInternalDomainAccess().startReResolve();
			
			if (this.domainQuery.getQueryExecutor().hasBeenReplayed()) {
				ret = this.domainQuery.getQueryExecutor().loadReplayedResult(match);
			} else {
				Boolean br_old = QueryRecorder.blockRecording.get();
				try {
					QueryRecorder.blockRecording.set(Boolean.TRUE);
					DomainObjectMatch<?> delegate = APIAccess.getDelegate(match);
					if (delegate != null) { // this is a generic domain query
						List<?> dobjs = this.domainQuery.getQueryExecutor().loadResult(delegate);
						ret = (List<T>) this.domainQuery.getQueryExecutor().getMappingInfo()
							.getInternalDomainAccess().getGenericDomainObjects(dobjs);
					} else
						ret = this.domainQuery.getQueryExecutor().loadResult(match);
				} finally {
					QueryRecorder.blockRecording.set(br_old);
				}
			}
		} finally {
			if (forceResolve)
				this.domainQuery.getQueryExecutor().getMappingInfo()
					.getInternalDomainAccess().endReResolve();
		}
		return ret;
	}
	
	AbstractDomainQuery getDomainQuery() {
		return this.domainQuery;
	}
}
