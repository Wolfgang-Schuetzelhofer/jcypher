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

package iot.jcypher.query.api.predicate;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.api.collection.CFactory;
import iot.jcypher.query.api.collection.ICollectExpression;
import iot.jcypher.query.api.collection.ICollection;
import iot.jcypher.query.ast.collection.CollectionSpec;
import iot.jcypher.query.ast.predicate.BooleanOp;
import iot.jcypher.query.ast.predicate.PredicateExpression;
import iot.jcypher.query.ast.predicate.BooleanOp.Operator;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcPath;

import java.util.ArrayList;


public class BooleanOperation extends APIObject implements ICollection<Concatenator> {

	BooleanOperation(PredicateExpression pe) {
		this.astNode = pe;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test for equality</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...valueOf(n.property("age")).<b>EQUALS(25)</b></i></div>
	 * <br/>
	 */
	public <E> Concatenator EQUALS(E value) {
		getBooleanOp().setOperator(Operator.EQUALS);
		return this.operateOn(value);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>negated test for equality</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...valueOf(n.property("age")).<b>NOT_EQUALS(25)</b></i></div>
	 * <br/>
	 */
	public <E> Concatenator NOT_EQUALS(E value) {
		getBooleanOp().setOperator(Operator.NOT_EQUALS);
		return this.operateOn(value);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>less than</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...valueOf(n.property("age")).<b>LT(25)</b></i></div>
	 * <br/>
	 */
	public <E> Concatenator LT(E value) {
		getBooleanOp().setOperator(Operator.LT);
		return this.operateOn(value);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>less than or equal</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...valueOf(n.property("age")).<b>LTE(25)</b></i></div>
	 * <br/>
	 */
	public <E> Concatenator LTE(E value) {
		getBooleanOp().setOperator(Operator.LTE);
		return this.operateOn(value);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>greater than</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...valueOf(n.property("age")).<b>GT(25)</b></i></div>
	 * <br/>
	 */
	public <E> Concatenator GT(E value) {
		getBooleanOp().setOperator(Operator.GT);
		return this.operateOn(value);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>greater than or equal</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...valueOf(n.property("age")).<b>GTE(25)</b></i></div>
	 * <br/>
	 */
	public <E> Concatenator GTE(E value) {
		getBooleanOp().setOperator(Operator.GTE);
		return this.operateOn(value);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test against a regular expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...valueOf(n.property("name")).<b>REGEX("A.*")</b></i></div>
	 * <br/>
	 */
	public Concatenator REGEX(String regex) {
		getBooleanOp().setOperator(Operator.REGEX);
		return this.operateOn(regex);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>test against NULL (test if a property exists)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...valueOf(n.property("age")).<b>IS_NULL()</b></i></div>
	 * <br/>
	 */
	public Concatenator IS_NULL() {
		getBooleanOp().setOperator(Operator.IS_NULL);
		return this.operateOn(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E> Concatenator IN_list(E... value) {
		getBooleanOp().setOperator(Operator.IN);
		ArrayList<E> il = new ArrayList<E>();
		for (int i = 0; i < value.length; i++)
			il.add(value[i]);
		return this.operateOn(new CollectionSpec(il));
	}
	
	@Override
	public Concatenator IN(ICollectExpression C) {
		getBooleanOp().setOperator(Operator.IN);
		return this.operateOn(new CollectionSpec(CFactory.getRootCollectExpression((APIObject)C)));
	}
	
	@Override
	public Concatenator IN(JcCollection collection) {
		getBooleanOp().setOperator(Operator.IN);
		return this.operateOn(new CollectionSpec(collection));
	}
	
	@Override
	public Concatenator IN_nodes(JcPath path) {
		return this.IN(path.nodes());
	}

	@Override
	public Concatenator IN_relations(JcPath path) {
		return this.IN(path.relations());
	}
	
	@Override
	public Concatenator IN_labels(JcNode node) {
		return this.IN(node.labels());
	}

	private <E> Concatenator operateOn(E value) {
		getBooleanOp().setOperand2(value);
		Concatenator ret = new Concatenator((PredicateExpression)this.astNode);
		return ret;
	}
	
	private BooleanOp getBooleanOp() {
		return (BooleanOp) ((PredicateExpression)this.astNode).getLastPredicate();
	}
}
