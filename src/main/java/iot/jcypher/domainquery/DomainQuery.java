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
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.BooleanOperation;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.api.IPredicateOperand1;
import iot.jcypher.domainquery.ast.ConcatenateExpression;
import iot.jcypher.domainquery.ast.ConcatenateExpression.Concatenator;
import iot.jcypher.domainquery.ast.IASTObject;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.ast.PredicateExpression;
import iot.jcypher.domainquery.internal.QueryExecutor;
import iot.jcypher.query.values.ValueElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainQuery {

	private QueryExecutor queryExecutor;
	
	public DomainQuery(IDomainAccess domainAccess) {
		super();
		this.queryExecutor = new QueryExecutor(domainAccess);
	}
	
	/**
	 * Create a match for a specific type of domain objects
	 * @param domainObjectType
	 * @return a DomainObjectMatch for a specific type of domain objects
	 */
	public <T> DomainObjectMatch<T> createMatch(Class<T> domainObjectType) {
		DomainObjectMatch<T> ret =APIAccess.createDomainObjectMatch(domainObjectType,
				this.queryExecutor.getDomainObjectMatches().size(),
				this.queryExecutor.getMappingInfo());
		this.queryExecutor.getDomainObjectMatches().add(ret);
		return ret;
	}
	
	/**
	 * Get or create, if not exists, a query parameter.
	 * @param name of the parameter
	 * @return a query parameter
	 */
	public Parameter parameter(String name) {
		return this.queryExecutor.parameter(name);
	}
	
	/**
	 * Start formulating a predicate expression.
	 * A predicate expression yields a boolean value.
	 * <br/>Takes an expression like 'person.stringAttribute("name")', yielding an attribute,
	 *	<br/>e.g. WHERE(person.stringAttribute("name")).EQUALS(...)
	 * @param value the value(expression) to formulate the predicate expression upon.
	 * @return
	 */
	public BooleanOperation WHERE(IPredicateOperand1 value) {
		PredicateExpression pe = new PredicateExpression(value);
		this.queryExecutor.getAstObjects().add(pe);
		BooleanOperation ret = APIAccess.createBooleanOperation(pe);
		return ret;
	}
	
	/**
	 * Or two predicate expressions
	 */
	public void OR() {
		this.queryExecutor.getAstObjects().add(new ConcatenateExpression(Concatenator.OR));
	}
	
	/**
	 * Open a block, encapsulating predicate expressions
	 */
	public void BR_OPEN() {
		this.queryExecutor.getAstObjects().add(new ConcatenateExpression(Concatenator.BR_OPEN));
	}
	
	/**
	 * Close a block, encapsulating predicate expressions
	 */
	public void BR_CLOSE() {
		this.queryExecutor.getAstObjects().add(new ConcatenateExpression(Concatenator.BR_CLOSE));
	}
	
	/**
	 * Execute the domain query
	 * @return a DomainQueryResult
	 */
	public DomainQueryResult execute() {
		DomainQueryResult ret = new DomainQueryResult(this);
		this.queryExecutor.execute();
		return ret;
	}
	
	QueryExecutor getQueryExecutor() {
		return this.queryExecutor;
	}
}
