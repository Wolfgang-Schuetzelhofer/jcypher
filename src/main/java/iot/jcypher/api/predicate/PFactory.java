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

package iot.jcypher.api.predicate;

import iot.jcypher.api.APIObject;
import iot.jcypher.api.collection.CFactory;
import iot.jcypher.api.collection.IPredicateFunction;
import iot.jcypher.api.pattern.Element;
import iot.jcypher.api.pattern.IElement;
import iot.jcypher.api.pattern.XFactory;
import iot.jcypher.ast.collection.CollectExpression;
import iot.jcypher.ast.predicate.BooleanOp;
import iot.jcypher.ast.predicate.ExistsPattern;
import iot.jcypher.ast.predicate.Negation;
import iot.jcypher.ast.predicate.PredicateExpression;
import iot.jcypher.ast.predicate.PredicateFunction;
import iot.jcypher.ast.predicate.SubExpression;
import iot.jcypher.ast.predicate.BooleanOp.Operator;
import iot.jcypher.values.IHas;
import iot.jcypher.values.ValueElement;


public class PFactory {

	public static BooleanOperation valueOf(ValueElement val) {
		PredicateExpression xpr = new PredicateExpression();
		BooleanOp bo = new BooleanOp();
		bo.setOperand1(val);
		xpr.setPredicate(bo);
		return new BooleanOperation(xpr);
	}
	
	public static Concatenator has(IHas val) {
		PredicateExpression xpr = new PredicateExpression();
		BooleanOp bo = new BooleanOp();
		bo.setOperand1((ValueElement)val);
		bo.setOperator(Operator.HAS);
		xpr.setPredicate(bo);
		return new Concatenator(xpr);
	}
	
	public static IBeforePredicate NOT() {
		PredicateExpression xpr = new PredicateExpression();
		xpr.setPredicate(new Negation());
		return new Concat(xpr);
	}
	
	public static Concat BR_OPEN() {
		PredicateExpression parent = new PredicateExpression();
		SubExpression subxpr = new SubExpression();
		PredicateExpression xpr = new PredicateExpression();
		xpr.setParent(parent);
		subxpr.setPredicateExpression(xpr);
		parent.setPredicate(subxpr);
		return new Concat(xpr);
	}
	
	public static Concatenator existsPattern(IElement expression) {
		if (!(expression instanceof Element<?>))
			throw new RuntimeException("invalid expression");
		ExistsPattern ep = new ExistsPattern();
		ep.setPatternExpression(XFactory.getExpression(expression));
		PredicateExpression xpr = new PredicateExpression();
		xpr.setPredicate(ep);
		return new Concatenator(xpr);
	}
	
	public static Concatenator holdsTrue(IPredicateFunction obj) {
		PredicateExpression xpr = new PredicateExpression();
		CollectExpression collxpr = CFactory.getRootCollectExpression((APIObject)obj);
		PredicateFunction pf = new PredicateFunction();
		pf.setCollectExpression(collxpr);
		xpr.setPredicate(pf);
		return new Concatenator(xpr);
	}
	
	public static Concat createConcat() {
		PredicateExpression xpr = new PredicateExpression();
		Concat concat = new Concat(xpr);
		return concat;
	}
}
