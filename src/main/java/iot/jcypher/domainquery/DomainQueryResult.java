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

import java.util.List;

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
		return this.domainQuery.getQueryExecutor().loadResult(match);
	}
}
