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

package iot.jcypher.query.values;

import java.util.Collection;

import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.query.values.functions.FUNCTION;
import iot.jcypher.query.values.operators.OPERATOR;

public class JcCollection extends JcValue {
	
	private Object value;
	
	JcCollection() {
		super();
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a collection which is identified by a name</i></div>
	 * <br/>
	 */
	public JcCollection(String name) {
		super(name);
	}
	
	JcCollection(String name, ValueElement predecessor, IOperatorOrFunction opf) {
		super(name, predecessor, opf);
	}
	
	JcCollection(String name, Object val, ValueElement predecessor, IOperatorOrFunction opf) {
		super(name, predecessor, opf);
		this.value = val;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the size of a collection, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber length() {
		JcNumber ret = new JcNumber(null, this,
				new FunctionInstance(FUNCTION.Collection.LENGTH, 1));
		QueryRecorder.recordInvocationConditional(this, "length", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>add a value to a collection, return a <b>JcCollection</b></i></div>
	 * <br/>
	 */
	public JcCollection add(Object value) {
		JcCollection ret = new JcCollection(null, value, this,
				OPERATOR.Collection.ADD);
		QueryRecorder.recordInvocationConditional(this, "add", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>add all elements of a collection to this collection, return a <b>JcCollection</b></i></div>
	 * <br/>
	 */
	public JcCollection addAll(Collection<?> coll) {
		JcCollection ret = new JcCollection(null, coll, this,
				OPERATOR.Collection.ADD_ALL);
		QueryRecorder.recordInvocationConditional(this, "addAll", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the first element of a collection</i></div>
	 * <br/>
	 */
	public ValueElement head() {
		ValueElement ret = new ValueElement(this, 
				new FunctionInstance(FUNCTION.Collection.HEAD, 1));
		QueryRecorder.recordInvocationConditional(this, "head", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the last element of a collection</i></div>
	 * <br/>
	 */
	public ValueElement last() {
		ValueElement ret = new ValueElement(this, 
				new FunctionInstance(FUNCTION.Collection.LAST, 1));
		QueryRecorder.recordInvocationConditional(this, "last", ret);
		return ret;
	}
	
	Object getValue() {
		return value;
	}
}
