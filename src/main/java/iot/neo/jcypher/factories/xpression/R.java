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

package iot.neo.jcypher.factories.xpression;

import iot.neo.jcypher.api.collection.ICollectExpression;
import iot.neo.jcypher.api.pattern.IElement;
import iot.neo.jcypher.api.predicate.Concatenator;
import iot.neo.jcypher.api.returns.RCount;
import iot.neo.jcypher.api.returns.RDistinct;
import iot.neo.jcypher.api.returns.RElement;
import iot.neo.jcypher.api.returns.RFactory;
import iot.neo.jcypher.api.returns.RSortable;
import iot.neo.jcypher.api.returns.RTerminal;
import iot.neo.jcypher.values.JcCollection;
import iot.neo.jcypher.values.JcValue;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER FACTORY</i></b></div>
 */
public class R {
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all nodes, relationships and paths found in a query,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>R.ALL()</b></i></div>
	 * <br/>
	 */
	public static RTerminal ALL() {
		return RFactory.ALL();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select a named (identified) value</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>R.value(n)</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or an expression like <b>R.value(n.property("age"))</b> to be returned</i></div>
	 * <br/>
	 */
	public static RSortable value(JcValue element) {
		return RFactory.value(element);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of a boolean expression (<b>true</b> or <b>false</b>)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>P</b> to create a boolean Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>R.resultOf(P.valueOf(a.property("age")).GT(30))</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> resultOf(Concatenator P) {
		return RFactory.resultOf(P);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>try to match a pattern against the graph. If a match is found return <b>true</b> else return <b>false</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>X</b> to create a Pattern Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>R.existsPattern(X.node(n)...)</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> existsPattern(IElement X) {
		return RFactory.existsPattern(X);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of a collection expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>C</b> to create a Collection Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>R.resultOf(C.COLLECT().property("name").from(nds))<b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> resultOf(ICollectExpression C) {
		return RFactory.resultOf(C);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>R.resultOf(p.nodes())</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or <b>R.resultOf(n.labels())</b></i></div>
	 * <br/>
	 */
	public static RElement<RElement<?>> resultOf(JcCollection collection) {
		return RFactory.resultOf(collection);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the size of a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>R.count().resultOf(p.nodes())</b></i></div>
	 * <br/>
	 */
	public static RCount count() {
		return RFactory.count();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return unique results</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>R.DISTINCT().resultOf(p.nodes())</b></i></div>
	 * <br/>
	 */
	public static RDistinct DISTINCT() {
		return RFactory.DISTINCT();
	}
}
