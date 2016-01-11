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

package iot.jcypher.domainquery.api;

import iot.jcypher.domainquery.ast.PredicateExpression;
import iot.jcypher.domainquery.ast.PredicateExpression.Operator;
import iot.jcypher.domainquery.internal.QueryRecorder;

public class BooleanOperation extends APIObject {

	BooleanOperation(PredicateExpression pe) {
		this.astObject = pe;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test for equality</i></div>
	 * <br/>
	 */
	public <E> TerminalResult EQUALS(E value) {
		getPredicateExpression().setOperator(Operator.EQUALS);
		getPredicateExpression().setValue_2(value);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "EQUALS", ret, QueryRecorder.placeHolder(value));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>negate</i></div>
	 * <br/>
	 */
	public BooleanOperation NOT() {
		getPredicateExpression().addNegation();
		QueryRecorder.recordInvocation(this, "NOT", this);
		return this;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>less than</i></div>
	 * <br/>
	 */
	public <E> TerminalResult LT(E value) {
		getPredicateExpression().setOperator(Operator.LT);
		getPredicateExpression().setValue_2(value);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "LT", ret, QueryRecorder.placeHolder(value));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>less than or equal</i></div>
	 * <br/>
	 */
	public <E> TerminalResult LTE(E value) {
		getPredicateExpression().setOperator(Operator.LTE);
		getPredicateExpression().setValue_2(value);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "LTE", ret, QueryRecorder.placeHolder(value));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>greater than</i></div>
	 * <br/>
	 */
	public <E> TerminalResult GT(E value) {
		getPredicateExpression().setOperator(Operator.GT);
		getPredicateExpression().setValue_2(value);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "GT", ret, QueryRecorder.placeHolder(value));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>greater than or equal</i></div>
	 * <br/>
	 */
	public <E> TerminalResult GTE(E value) {
		getPredicateExpression().setOperator(Operator.GTE);
		getPredicateExpression().setValue_2(value);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "GTE", ret, QueryRecorder.placeHolder(value));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>case-sensitive matching on the start of strings</i></div>
	 * <br/>
	 */
	public TerminalResult STARTS_WITH(String string) {
		getPredicateExpression().setOperator(Operator.STARTS_WITH);
		getPredicateExpression().setValue_2(string);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "STARTS_WITH", ret, QueryRecorder.literal(string));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>case-sensitive matching on the end of strings</i></div>
	 * <br/>
	 */
	public TerminalResult ENDS_WITH(String string) {
		getPredicateExpression().setOperator(Operator.ENDS_WITH);
		getPredicateExpression().setValue_2(string);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "ENDS_WITH", ret, QueryRecorder.literal(string));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>case-sensitive matching regardless of location in strings</i></div>
	 * <br/>
	 */
	public TerminalResult CONTAINS_string(String string) {
		getPredicateExpression().setOperator(Operator.CONTAINS_STRING);
		getPredicateExpression().setValue_2(string);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "CONTAINS_string", ret, QueryRecorder.literal(string));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test against a regular expression</i></div>
	 * <br/>
	 */
	public TerminalResult LIKE(String regex) {
		getPredicateExpression().setOperator(Operator.LIKE);
		getPredicateExpression().setValue_2(regex);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "LIKE", ret, QueryRecorder.literal(regex));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test against NULL (test if a property exists)</i></div>
	 * <br/>
	 */
	public TerminalResult IS_NULL() {
		getPredicateExpression().setOperator(Operator.IS_NULL);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "IS_NULL", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test for containment in a list of elements</i></div>
	 * <br/>
	 */
	@SuppressWarnings("unchecked")
	public <E> TerminalResult IN_list(E... value) {
		getPredicateExpression().setOperator(Operator.IN);
		getPredicateExpression().setValue_2(value);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "IN_list", ret, QueryRecorder.placeHolder(value));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test for containment in a list of matched domain objects</i></div>
	 * <br/>
	 */
	public <E> TerminalResult IN(DomainObjectMatch<E> domainObjects) {
		DomainObjectMatch<?> delegate = APIAccess.getDelegate(domainObjects);
		DomainObjectMatch<?> match = delegate != null ? delegate : domainObjects;
		getPredicateExpression().setOperator(Operator.IN);
		getPredicateExpression().setValue_2(match);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "IN", ret, QueryRecorder.placeHolder(match));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test if this list contains all elements of the target list</i></div>
	 * <br/>
	 */
	@SuppressWarnings("unchecked")
	public <E> TerminalResult CONTAINS_elements(E... value) {
		getPredicateExpression().setOperator(Operator.CONTAINS);
		getPredicateExpression().setValue_2(value);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "CONTAINS_elements", ret, QueryRecorder.placeHolder(value));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test if this list of domain objects contains all elements
	 * of the target list of domain objects</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>Note:</b> this expression is only valid
	 * within a collection expression.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>It constrains a set by applying the CONTAINS expression
	 *  on another set which must be directly derived via a traversal expression.
	 *  <br/> 'addressAreas' below has been directly derived from 'addresses' via traversal.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. q.SELECT_FROM(addresses).ELEMENTS(
	 * q.WHERE(addressAreas).CONTAINS(europe));</i></div>
	 * <br/>
	 */
	public <E> TerminalResult CONTAINS(DomainObjectMatch<E> domainObjects) {
		DomainObjectMatch<?> delegate = APIAccess.getDelegate(domainObjects);
		DomainObjectMatch<?> match = delegate != null ? delegate : domainObjects;
		getPredicateExpression().setOperator(Operator.CONTAINS);
		getPredicateExpression().setValue_2(match);
		TerminalResult ret = APIAccess.createTerminalResult(this.getPredicateExpression());
		QueryRecorder.recordInvocation(this, "CONTAINS", ret, QueryRecorder.placeHolder(match));
		return ret;
	}
	
	private PredicateExpression getPredicateExpression() {
		return (PredicateExpression) this.astObject;
	}
}
