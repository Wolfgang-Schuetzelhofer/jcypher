/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
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
import iot.jcypher.domainquery.api.BooleanOperation;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.ast.IASTObject;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.ast.PredicateExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainQuery {

	private IDomainAccess domainAccess;
	private List<IASTObject> astObjects;
	private List<DomainObjectMatch<?>> domainObjectMatches;
	private Map<String, Parameter> parameters;

	public DomainQuery(IDomainAccess domainAccess) {
		super();
		this.domainAccess = domainAccess;
		this.astObjects = new ArrayList<IASTObject>();
		this.domainObjectMatches = new ArrayList<DomainObjectMatch<?>>();
		this.parameters = new HashMap<String, Parameter>();
	}
	
	/**
	 * Create a match for a specific type of domain objects
	 * @param domainObjectType
	 * @return a DomainObjectMatch for a specific type of domain objects
	 */
	public <T> DomainObjectMatch<T> createMatch(Class<T> domainObjectType) {
		DomainObjectMatch<T> ret = new DomainObjectMatch<>(domainObjectType);
		this.domainObjectMatches.add(ret);
		return ret;
	}
	
	/**
	 * Get or create, if not exists, a query parameter.
	 * @param name of the parameter
	 * @return a query parameter
	 */
	public Parameter parameter(String name) {
		Parameter param = this.parameters.get(name);
		if (param == null) {
			param = new Parameter(name);
			this.parameters.put(name, param);
		}
		return param;
	}
	
	/**
	 * Start formulating a predicate expression.
	 * A predicate expression yields a boolean value.
	 * @param value the value to formulate the predicate expression upon.
	 * @return
	 */
	public BooleanOperation WHERE(Object value) {
		PredicateExpression pe = new PredicateExpression(value);
		this.astObjects.add(pe);
		BooleanOperation ret = new BooleanOperation(pe);
		return ret;
	}
}
