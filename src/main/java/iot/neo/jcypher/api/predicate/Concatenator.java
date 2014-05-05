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

package iot.neo.jcypher.api.predicate;

import iot.neo.jcypher.api.APIObject;
import iot.neo.jcypher.api.collection.ICollectExpression;
import iot.neo.jcypher.api.collection.IPredicateFunction;
import iot.neo.jcypher.ast.predicate.Predicate;
import iot.neo.jcypher.ast.predicate.PredicateConcatenator;
import iot.neo.jcypher.ast.predicate.PredicateConcatenator.ConcatOperator;
import iot.neo.jcypher.ast.predicate.PredicateExpression;
import iot.neo.jcypher.clause.IClause;

public class Concatenator extends APIObject implements ICollectExpression, IPredicateFunction, IClause {

	Concatenator(PredicateExpression pe) {
		super();
		this.astNode = pe;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>'and'</b> two boolean expressions or values</i></div>
	 * <br/>
	 */
	public Concat AND() {
		return concatenate(ConcatOperator.AND);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>'or'</b> two boolean expressions or values</i></div>
	 * <br/>
	 */
	public Concat OR() {
		return concatenate(ConcatOperator.OR);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>'xor' (exclusive or)</b> two boolean expressions or values</i></div>
	 * <br/>
	 */
	public Concat XOR() {
		return concatenate(ConcatOperator.XOR);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>close a bracket; allows to nest expressions, must have a matching BR_OPEN()</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. WHERE... ...<b>BR_OPEN()</b>
	 * <br/>.valueOf(charlie.property("lastName")).EQUALS("Sheen")<br/>.<b>BR_CLOSE()</b></i></div>
	 * <br/>
	 */
	public Concatenator BR_CLOSE() {
		if (((PredicateExpression)this.astNode).getParent() != null) {
			Concatenator ret = new Concatenator(((PredicateExpression)this.astNode).getParent());
			return ret;
		} else
			throw new RuntimeException("No matching open bracket");
	}
	
	private Concat concatenate(ConcatOperator op) {
		Predicate predicate = ((PredicateExpression)this.astNode).getLastPredicate();
		PredicateConcatenator cp = new PredicateConcatenator();
		predicate.setNext(cp);
		cp.setConcatOperator(op);
		Concat concat = new Concat((PredicateExpression)this.astNode);
		return concat;
	}
}
