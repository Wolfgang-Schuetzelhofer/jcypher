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

package iot.neo.jcypher.api.returns;

import iot.neo.jcypher.api.APIObject;
import iot.neo.jcypher.api.APIObjectAccess;
import iot.neo.jcypher.api.collection.CFactory;
import iot.neo.jcypher.api.collection.ICollectExpression;
import iot.neo.jcypher.api.pattern.IElement;
import iot.neo.jcypher.api.pattern.XFactory;
import iot.neo.jcypher.api.predicate.Concatenator;
import iot.neo.jcypher.ast.collection.CollectExpression;
import iot.neo.jcypher.ast.collection.CollectionSpec;
import iot.neo.jcypher.ast.predicate.PredicateExpression;
import iot.neo.jcypher.ast.returns.ReturnBoolean;
import iot.neo.jcypher.ast.returns.ReturnCollection;
import iot.neo.jcypher.ast.returns.ReturnElement;
import iot.neo.jcypher.ast.returns.ReturnExpression;
import iot.neo.jcypher.ast.returns.ReturnPattern;
import iot.neo.jcypher.values.JcCollection;
import iot.neo.jcypher.values.JcValue;

public class RDistinct extends APIObject {

	RDistinct(ReturnExpression rx) {
		super();
		this.astNode = rx;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all nodes, relationships and paths found in a query,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>ALL()</b></i></div>
	 * <br/>
	 */
	public RTerminal ALL() {
		ReturnExpression rx = getReturnExpression();
		ReturnElement elem = new ReturnElement();
		elem.setAll();
		rx.setReturnValue(elem);
		RTerminal ret = new RTerminal(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select a named (identified) element</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>element(n)</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or an expression like <b>element(n.property("age"))</b> to be returned</i></div>
	 * <br/>
	 */
	public RSortable element(JcValue element) {
		ReturnExpression rx = getReturnExpression();
		ReturnElement elem = new ReturnElement();
		elem.setElement(element);
		rx.setReturnValue(elem);
		RSortable ret = new RSortable(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of a boolean expression (<b>true</b> or <b>false</b>)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>P</b> to create a boolean Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>resultOf(P.valueOf(a.property("age")).GT(30))</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> resultOf(Concatenator P) {
		CollectExpression cx = CFactory.getRootCollectExpression(P);
		if (cx != null) {
			return resultOfCollection(cx);
		}
		
		ReturnExpression rx = getReturnExpression();
		ReturnBoolean bool = new ReturnBoolean();
		PredicateExpression px = (PredicateExpression) APIObjectAccess.getAstNode(P);
		bool.setPredicateExpression(px);
		rx.setReturnValue(bool);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>try to match a pattern against the graph. If a match is found return <b>true</b> else return <b>false</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>X</b> to create a Pattern Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>resultOf(X.node(n)...)</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> resultOf(IElement X) {
		ReturnExpression rx = getReturnExpression();
		ReturnPattern pat = new ReturnPattern();
		pat.setPatternExpression(
				XFactory.getExpression(X));
		rx.setReturnValue(pat);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of a collection expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>C</b> to create a Collection Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>resultOf(C.COLLECT().property("name").from(nds))<b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> resultOf(ICollectExpression C) {
		return resultOfCollection(CFactory.getRootCollectExpression((APIObject)C));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>resultOf(p.nodes())</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or <b>resultOf(n.labels())</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> resultOf(JcCollection collection) {
		ReturnExpression rx = getReturnExpression();
		ReturnCollection coll = new ReturnCollection();
		CollectionSpec cs = new CollectionSpec(collection);
		CollectExpression cx = new CollectExpression();
		cx.setCollectionToOperateOn(cs);
		coll.setCollectExpression(cx);
		rx.setReturnValue(coll);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	private RElement<RElement<?>> resultOfCollection(CollectExpression collXpr) {
		ReturnExpression rx = getReturnExpression();
		ReturnCollection coll = new ReturnCollection();
		coll.setCollectExpression(collXpr);
		rx.setReturnValue(coll);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	protected ReturnExpression getReturnExpression() {
		return (ReturnExpression)this.astNode;
	}
}