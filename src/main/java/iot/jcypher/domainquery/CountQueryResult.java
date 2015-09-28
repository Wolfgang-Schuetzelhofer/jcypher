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

import iot.jcypher.domainquery.api.DomainObjectMatch;

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
		return this.domainQuery.getQueryExecutor().getCountResult(match);
	}
}
