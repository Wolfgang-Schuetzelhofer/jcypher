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

package iot.jcypher.factories.clause;

import iot.jcypher.api.APIObjectAccess;
import iot.jcypher.api.collection.ICollectExpression;
import iot.jcypher.api.pattern.IElement;
import iot.jcypher.api.predicate.Concatenator;
import iot.jcypher.api.returns.AggregateDistinct;
import iot.jcypher.api.returns.RCount;
import iot.jcypher.api.returns.RDistinct;
import iot.jcypher.api.returns.RElement;
import iot.jcypher.api.returns.RFactory;
import iot.jcypher.api.returns.RSortable;
import iot.jcypher.api.returns.RTerminal;
import iot.jcypher.ast.ASTNode;
import iot.jcypher.clause.ClauseType;
import iot.jcypher.values.JcCollection;
import iot.jcypher.values.JcValue;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 */
public class RETURN {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all nodes, relationships and paths found in a query,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.ALL()</b></i></div>
	 * <br/>
	 */
	public static RTerminal ALL() {
		RTerminal ret = RFactory.ALL();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select a named (identified) value</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.value(n)</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or an expression like <b>RETURN.value(n.property("age"))</b> to be returned</i></div>
	 * <br/>
	 */
	public static RSortable value(JcValue element) {
		RSortable ret = RFactory.value(element);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of evaluating a Predicate Expression (<b>true</b> or <b>false</b>)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>P</b> to create a Predicate Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.evalPredicate(P.valueOf(a.property("age")).GT(30))</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> evalPredicate(Concatenator P) {
		RElement<RElement<?>> ret = RFactory.resultOf(P);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>try to match a pattern against the graph. If a match is found return <b>true</b> else return <b>false</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>X</b> to create a Pattern Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.existsPattern(X.node(n)...)</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> existsPattern(IElement X) {
		RElement<RElement<?>> ret = RFactory.existsPattern(X);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of a collection expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>C</b> to create a Collection Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.collection(C.COLLECT().property("name").from(nds))<b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> collection(ICollectExpression C) {
		RElement<RElement<?>> ret = RFactory.resultOf(C);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.collection(p.nodes())</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or <b>RETURN.collection(n.labels())</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> collection(JcCollection collection) {
		RElement<RElement<?>> ret = RFactory.resultOf(collection);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of performing an aggregate function like sum(...), avg(...), ...</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.aggregate().sum(n.property(amount))<b></i></div>
	 * <br/>
	 */
	public static AggregateDistinct aggregate() {
		AggregateDistinct ret = RFactory.aggregate();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the size of a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.count().resultOf(p.nodes())</b></i></div>
	 * <br/>
	 */
	public static RCount count() {
		RCount ret = RFactory.count();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return unique results</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>RETURN.DISTINCT().collection(p.nodes())</b></i></div>
	 * <br/>
	 */
	public static RDistinct DISTINCT() {
		RDistinct ret = RFactory.DISTINCT();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.RETURN);
		return ret;
	}
}
