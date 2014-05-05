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

package iot.neo.jcypher.factories.clause;

import iot.neo.jcypher.api.APIObjectAccess;
import iot.neo.jcypher.api.collection.ICollectExpression;
import iot.neo.jcypher.api.pattern.IElement;
import iot.neo.jcypher.api.predicate.Concatenator;
import iot.neo.jcypher.api.returns.RCount;
import iot.neo.jcypher.api.returns.RDistinct;
import iot.neo.jcypher.api.returns.RElement;
import iot.neo.jcypher.api.returns.RSortable;
import iot.neo.jcypher.api.returns.RTerminal;
import iot.neo.jcypher.ast.ASTNode;
import iot.neo.jcypher.clause.ClauseType;
import iot.neo.jcypher.factories.xpression.R;
import iot.neo.jcypher.values.JcCollection;
import iot.neo.jcypher.values.JcValue;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 * <div color='red' style="font-size:18px;color:red"><i>allows the same expressions as the <b>RETURN</b> clause</i></div>
 */
public class WITH {
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all nodes, relationships and paths found in a query,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>WITH.ALL()</b></i></div>
	 * <br/>
	 */
	public static RTerminal ALL() {
		RTerminal ret = R.ALL();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WITH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select a named (identified) value</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>WITH.value(n)</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or an expression like <b>WITH.value(n.property("age"))</b> to be returned</i></div>
	 * <br/>
	 */
	public static RSortable value(JcValue element) {
		RSortable ret = R.value(element);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WITH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of a boolean expression (<b>true</b> or <b>false</b>)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>P</b> to create a boolean Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>WITH.resultOf(P.valueOf(a.property("age")).GT(30))</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> resultOf(Concatenator P) {
		RElement<RElement<?>> ret = R.resultOf(P);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WITH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>try to match a pattern against the graph. If a match is found return <b>true</b> else return <b>false</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>X</b> to create a Pattern Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>WITH.existsPattern(X.node(n)...)</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> existsPattern(IElement X) {
		RElement<RElement<?>> ret = R.existsPattern(X);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WITH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of a collection expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>C</b> to create a Collection Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>WITH.resultOf(C.COLLECT().property("name").from(nds))<b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> resultOf(ICollectExpression C) {
		RElement<RElement<?>> ret = R.resultOf(C);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WITH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>WITH.resultOf(p.nodes())</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or <b>WITH.resultOf(n.labels())</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> resultOf(JcCollection collection) {
		RElement<RElement<?>> ret = R.resultOf(collection);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WITH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the size of a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>WITH.count().resultOf(p.nodes())</b></i></div>
	 * <br/>
	 */
	public static RCount count() {
		RCount ret = R.count();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WITH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return unique results</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>WITH.DISTINCT().resultOf(p.nodes())</b></i></div>
	 * <br/>
	 */
	public static RDistinct DISTINCT() {
		RDistinct ret = R.DISTINCT();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WITH);
		return ret;
	}
}
