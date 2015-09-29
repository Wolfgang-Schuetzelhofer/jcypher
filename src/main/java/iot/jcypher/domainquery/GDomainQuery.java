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
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domainquery.api.DomainObjectMatch;

import java.util.List;

/**
 * Allows to formulate a query against a generic domain model
 * @author wolfgang
 *
 */
public class GDomainQuery extends AbstractDomainQuery {

	public GDomainQuery(IDomainAccess domainAccess) {
		super(domainAccess);
	}
	
	/**
	 * Create a match for a specific type of domain objects.
	 * <br/>The match is part of a query performed on a generic domain model.
	 * @param domainObjectTypeName
	 * @return a DomainObjectMatch for a specific type of domain objects
	 */
	public DomainObjectMatch<DomainObject> createMatch(String domainObjectTypeName) {
		//return super.createMatch(domainObjectType);
		return null;
	}
	
	/**
	 * Create a match for a list of domain objects which were retrieved by another query.
	 * <br/>The match will be part of a query performed on a generic domain model.
	 * @param domainObjects a list of domain objects which were retrieved by another query
	 * @param domainObjectTypeName the type name of those domain objects
	 * @return a DomainObjectMatch
	 */
	public DomainObjectMatch<DomainObject> createMatchFor(List<DomainObject> domainObjects,
			String domainObjectTypeName) {
		//return super.createMatchFor(domainObjects, domainObjectType);
		return null;
	}

}