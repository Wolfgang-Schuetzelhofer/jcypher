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

import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.BooleanOperation;
import iot.jcypher.domainquery.api.Collect;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.api.IPredicateOperand1;
import iot.jcypher.domainquery.api.Order;
import iot.jcypher.domainquery.api.Select;
import iot.jcypher.domainquery.api.TerminalResult;
import iot.jcypher.domainquery.api.Traverse;
import iot.jcypher.domainquery.ast.CollectExpression;
import iot.jcypher.domainquery.ast.ConcatenateExpression;
import iot.jcypher.domainquery.ast.ConcatenateExpression.Concatenator;
import iot.jcypher.domainquery.ast.OrderExpression;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.ast.PredicateExpression;
import iot.jcypher.domainquery.ast.SelectExpression;
import iot.jcypher.domainquery.ast.TraversalExpression;
import iot.jcypher.domainquery.internal.IASTObjectsContainer;
import iot.jcypher.domainquery.internal.QueryExecutor;
import iot.jcypher.query.values.JcProperty;

public class DomainQuery {

	private QueryExecutor queryExecutor;
	private IASTObjectsContainer astObjectsContainer;
	private IntAccess intAccess;
	
	public DomainQuery(IDomainAccess domainAccess) {
		super();
		this.queryExecutor = new QueryExecutor(domainAccess);
		this.astObjectsContainer = this.queryExecutor;
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
		PredicateExpression pe = new PredicateExpression(value, this.astObjectsContainer);
		this.astObjectsContainer.addAstObject(pe);
		BooleanOperation ret = APIAccess.createBooleanOperation(pe);
		return ret;
	}
	
	/**
	 * Or two predicate expressions
	 */
	public TerminalResult OR() {
		ConcatenateExpression ce = new ConcatenateExpression(Concatenator.OR);
		this.astObjectsContainer.addAstObject(ce);
		return APIAccess.createTerminalResult(ce);
	}
	
	/**
	 * Open a block, encapsulating predicate expressions
	 */
	public TerminalResult BR_OPEN() {
		ConcatenateExpression ce = new ConcatenateExpression(Concatenator.BR_OPEN);
		this.astObjectsContainer.addAstObject(ce);
		return APIAccess.createTerminalResult(ce);
	}
	
	/**
	 * Close a block, encapsulating predicate expressions
	 */
	public TerminalResult BR_CLOSE() {
		ConcatenateExpression ce = new ConcatenateExpression(Concatenator.BR_CLOSE);
		this.astObjectsContainer.addAstObject(ce);
		return APIAccess.createTerminalResult(ce);
	}
	
	/**
	 * Define an order on a set of domain objects which are specified by
	 * a DomainObjectMatch in the context of the domain query.
	 * @param toOrder the DomainObjectMatch
	 * specifying the set of domain objects which should be ordered
	 * @return
	 */
	public Order ORDER(DomainObjectMatch<?> toOrder) {
		OrderExpression oe = this.queryExecutor.getOrderFor(toOrder);
		Order ret = APIAccess.createOrder(oe);
		return ret;
	}
	
	/**
	 * Start traversing the graph of domain objects.
	 * @param start a DomainObjectMatch form where to start the traversal.
	 * @return
	 */
	public Traverse TRAVERSE_FROM(DomainObjectMatch<?> start) {
		TraversalExpression te = new TraversalExpression(start, this.queryExecutor);
		this.queryExecutor.addAstObject(te);
		Traverse ret = APIAccess.createTraverse(te);
		return ret;
	}
	
	/**
	 * Select domain objects out of a set of other domain objects.
	 * @param start with a DomainObjectMatch representing the initial set.
	 * @return
	 */
	public <T> Select<T> SELECT_FROM(DomainObjectMatch<T> start) {
		SelectExpression<T> se = new SelectExpression<T>(start, this.getIntAccess());
		this.queryExecutor.addAstObject(se);
		this.astObjectsContainer = se;
		Select<T> ret = APIAccess.createSelect(se);
		return ret;
	}
	
	/**
	 * Reject domain objects from a set of domain objects.
	 * Answer a set containing all objects of the source set except the rejected ones.
	 * @param start with a DomainObjectMatch representing the initial set.
	 * @return
	 */
	public <T> Select<T> REJECT_FROM(DomainObjectMatch<T> start) {
		SelectExpression<T> se = new SelectExpression<T>(start, this.getIntAccess(), true);
		this.queryExecutor.addAstObject(se);
		this.astObjectsContainer = se;
		Select<T> ret = APIAccess.createSelect(se);
		return ret;
	}
	
	/**
	 * Collect the specified attribute from all objects in a DomainObjectMatch
	 * @param attribute
	 * @return
	 */
	public Collect COLLECT(JcProperty attribute) {
		CollectExpression ce = new CollectExpression(attribute, this.getIntAccess());
		Collect coll = APIAccess.createCollect(ce);
		this.queryExecutor.addAstObject(ce);
		return coll;
	}
	
	/**
	 * Build the union of the specified sets
	 * @param set
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> DomainObjectMatch<T> UNION(DomainObjectMatch<T>... set) {
		return this.union_Intersection(true, set);
	}
	
	/**
	 * Build the intersection of the specified sets
	 * @param set
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> DomainObjectMatch<T> INTERSECTION(DomainObjectMatch<T>... set) {
		return this.union_Intersection(false, set);
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
	
	/**
	 * Retrieve the count for every DomainObjectMatch of the query
	 * in order to support pagination
	 * @return a CountQueryResult
	 */
	public CountQueryResult executeCount() {
		CountQueryResult ret = new CountQueryResult(this);
		this.queryExecutor.executeCount();
		return ret;
	}
	
	QueryExecutor getQueryExecutor() {
		return this.queryExecutor;
	}
	
	@SuppressWarnings("unchecked")
	private <T> DomainObjectMatch<T> union_Intersection(boolean union, DomainObjectMatch<T>... set) {
		DomainObjectMatch<T> ret = null;
		if (set.length > 0) {
			ret =APIAccess.createDomainObjectMatch(APIAccess.getDomainObjectType(set[0]),
					this.queryExecutor.getDomainObjectMatches().size(),
					this.queryExecutor.getMappingInfo());
			this.queryExecutor.getDomainObjectMatches().add(ret);
			int idx = 0;
			for (DomainObjectMatch<T> dom : set) {
				if (idx > 0 && union)
					this.OR();
				this.WHERE(ret).IN(dom);
				idx++;
			}
		}
		return ret;
	}

	private IntAccess getIntAccess() {
		if (this.intAccess == null)
			this.intAccess = new IntAccess();
		return this.intAccess;
	}
	
	/****************************************************/
	public class IntAccess {
		public QueryExecutor getQueryExecutor() {
			return queryExecutor;
		}
		
		public DomainQuery getDomainQuery() {
			return DomainQuery.this;
		}
		
		public void resetAstObjectsContainer() {
			astObjectsContainer = queryExecutor;
		}
	}
}
