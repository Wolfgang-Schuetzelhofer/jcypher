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

package iot.neo.jcypher.api.collection;

import iot.neo.jcypher.api.APIObject;
import iot.neo.jcypher.api.APIObjectAccess;
import iot.neo.jcypher.ast.collection.CollectExpression;
import iot.neo.jcypher.ast.collection.CollectionSpec;
import iot.neo.jcypher.values.JcCollection;


public class Collection extends APIObject {

	Collection(CollectExpression cx) {
		super();
		this.astNode = cx;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>collect properties from a collection of property containers (nodes or relations)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>COLLECT()</b>.property("name").from(p.nodes())</i></div>
	 * <br/>
	 */
	public EXProperty<CollectFrom> COLLECT() {
		EXProperty<CollectFrom> ret = CFactory.COLLECT();
		connectSubCollection(ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>go through a collection, run an expression on every element of the collection,
	 * <br/>and return the results as a collection of these values</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>EXTRACT()</b>.valueOf(n.numberProperty("age").div(2)).fromAll(n).IN_nodes(p)</i></div>
	 * <br/>
	 */
	public ExtractExpression EXTRACT() {
		ExtractExpression ret = CFactory.EXTRACT();
		connectSubCollection(ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all the elements in a collection that hold true for a predicate expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>FILTER()</b>.fromAll(x).IN(a.collectionProperty("array")).<b>WHERE()</b>...</i></div>
	 * <br/>
	 */
	public CFrom<CWhere> FILTER() {
		CFrom<CWhere> ret = CFactory.FILTER();
		connectSubCollection(ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>run an expression against individual elements of a collection
	 * </br>and store the result of the expression in an accumulator</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>REDUCE()</b>.fromAll(n).IN_nodes(p).to(totalAge).by(totalAge.plus(n.numberProperty("age"))).startWith(0)</i></div>
	 * <br/>
	 */
	public CFrom<ReduceTo> REDUCE() {
		CFrom<ReduceTo> ret = CFactory.REDUCE();
		connectSubCollection(ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all but the first element in a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>TAIL()</b>.FILTER()...</i></div>
	 * <br/>
	 */
	public Collection TAIL() {
		Collection ret = CFactory.TAIL();
		connectSubCollection(ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all but the first element in a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>TAIL(p.nodes())</b></i></div>
	 * <br/>
	 */
	public CTerminal TAIL(JcCollection collection) {
		CTerminal ret = CFactory.TAIL(collection);
		connectSubCollection(ret);
		return ret;
	}

	private void connectSubCollection(APIObject sub) {
		CollectExpression cx = (CollectExpression)this.astNode;
		CollectExpression ncx = (CollectExpression)APIObjectAccess.getAstNode(sub);
		ncx.setParent(cx);
		CollectionSpec cs = new CollectionSpec(ncx);
		cx.setCollectionToOperateOn(cs);
	}
}
