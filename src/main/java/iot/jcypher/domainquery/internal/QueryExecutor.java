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

package iot.jcypher.domainquery.internal;

import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.ast.IASTObject;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.values.JcNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryExecutor {

	private IDomainAccess domainAccess;
	private List<IASTObject> astObjects;
	private List<DomainObjectMatch<?>> domainObjectMatches;
	private Map<String, Parameter> parameters;
	
	public QueryExecutor(IDomainAccess domainAccess) {
		super();
		this.domainAccess = domainAccess;
		this.astObjects = new ArrayList<IASTObject>();
		this.domainObjectMatches = new ArrayList<DomainObjectMatch<?>>();
		this.parameters = new HashMap<String, Parameter>();
	}

	public List<IASTObject> getAstObjects() {
		return astObjects;
	}

	public List<DomainObjectMatch<?>> getDomainObjectMatches() {
		return domainObjectMatches;
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
	 * Execute the domain query
	 * @return a DomainQueryResult
	 */
	public DomainQueryResult execute() {
		JcQuery query = new QueryBuilder().buildQuery();
		return null;
	}
	
	public Attribute2PropertyNameConverter getConverterFor(Class<?> domainObjectType) {
		return new Attribute2PropertyNameConverter(domainObjectType);
	}
	
	/************************************/
	private class QueryBuilder {
		
		private JcQuery buildQuery() {
			List<IClause> clauses = new ArrayList<IClause>();
			for (DomainObjectMatch<?> dom : domainObjectMatches) {
				JcNode node = APIAccess.getNode(dom);
			}
			return null;
		}
	}
	
	/************************************/
	public class Attribute2PropertyNameConverter {

		private Class<?> domainObjectType;
		
		private Attribute2PropertyNameConverter(Class<?> domainObjectType) {
			super();
			this.domainObjectType = domainObjectType;
		}

		public String convert(String attribName) {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess()
				.getPropertyName(this.domainObjectType, attribName);
		}
		
	}
}
