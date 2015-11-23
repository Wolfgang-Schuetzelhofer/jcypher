/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.internal.QueryRecorder;
import iot.jcypher.domainquery.api.DomainObjectMatch;

import java.util.List;

public class DomainQuery extends AbstractDomainQuery {

	public DomainQuery(IDomainAccess domainAccess) {
		super(domainAccess);
	}
	
	/**
	 * Create a match for a specific type of domain objects
	 * @param domainObjectType
	 * @return a DomainObjectMatch for a specific type of domain objects
	 */
	public <T> DomainObjectMatch<T> createMatch(Class<T> domainObjectType) {
		DomainObjectMatch<T> ret = createMatchInternal(domainObjectType);
		QueryRecorder.recordInvocation(this, "createMatch", ret,
				QueryRecorder.literal(domainObjectType.getName()));
		return ret;
	}
	
	/**
	 * Create a match for a list of domain objects which were retrieved by another query
	 * @param domainObjects a list of domain objects which were retrieved by another query
	 * @param domainObjectType the type of those domain objects
	 * @return a DomainObjectMatch
	 */
	public <T> DomainObjectMatch<T> createMatchFor(List<T> domainObjects,
			Class<T> domainObjectType) {
		DomainObjectMatch<T> ret = createMatchForInternal(domainObjects, domainObjectType);
		QueryRecorder.recordInvocation(this, "createMatchFor", ret);
		return ret;
	}

}
