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
import java.util.List;

import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.query.values.functions.FUNCTION;
import iot.jcypher.query.values.operators.OPERATOR;

public class JcCollection<T extends JcValue> extends JcValue {
	
	private Object value;
	private Class<T> type;
	
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
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a JcCollection initialized with a possibly empty list of primitives.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>This serves to create lists of literals as e.g. <b>[1, 2, 3]</b> or <b>['Peter', 'Paul', 'Mary']...</b></i></div>
	 * <br/>
	 */
	public JcCollection(List<?> list) {
		this(fromPrimitiveList(list), null, null);
	}
	
	JcCollection(String name, ValueElement predecessor, IOperatorOrFunction opf) {
		super(name, predecessor, opf);
	}
	
	JcCollection(String name, ValueElement predecessor, IOperatorOrFunction opf, Class<T> typ) {
		super(name, predecessor, opf);
		this.type = typ;
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
	 * <div color='red' style="font-size:18px;color:red"><i>add a value to a collection, return a <b>JcCollection</b>.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The value can be a simple value or being derived via an expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>like ...<b>add(a.property("value"));</b>.</i></div>
	 * <br/>
	 */
	public JcCollection<JcValue> add(Object value) {
		JcCollection<JcValue> ret = new JcCollection<JcValue>(null, value, this,
				OPERATOR.Collection.ADD);
		QueryRecorder.recordInvocationConditional(this, "add", ret, QueryRecorder.placeHolder(value));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>add all elements of a collection to this collection, return a <b>JcCollection</b></i></div>
	 * <br/>
	 */
	public JcCollection<JcValue> addAll(Collection<?> coll) {
		JcCollection<JcValue> ret = new JcCollection<JcValue>(null, coll, this,
				OPERATOR.Collection.ADD_ALL);
		QueryRecorder.recordInvocationConditional(this, "addAll", ret, QueryRecorder.literal(coll));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>access the element of the collection at the given index, return a <b>JcProperty</b></i></div>
	 * <br/>
	 */
	@SuppressWarnings("unchecked")
	public T get(int index) {
		T ret;
		if (JcRelation.class.equals(type)) {
			ret = (T) new JcRelation(null, this, OPERATOR.Collection.GET);
		} else if (JcNode.class.equals(type)) {
			ret = (T) new JcNode(null, this, OPERATOR.Collection.GET);
		} else {
			ret = (T) new JcValue(null, this, OPERATOR.Collection.GET);
		}
		ret.setHint(ValueAccess.hintKey_opValue, index);
		QueryRecorder.recordInvocationConditional(this, "get", ret, QueryRecorder.literal(index));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>access the element of the collection at the index given by the indexExpression, return a <b>JcProperty</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>get(a.numberProperty("index"));</b>.</i></div>
	 * <br/>
	 */
	@SuppressWarnings("unchecked")
	public T get(JcNumber indexExpression) {
		T ret;
		if (JcRelation.class.equals(type)) {
			ret = (T) new JcRelation(null, this, OPERATOR.Collection.GET);
		} else if (JcNode.class.equals(type)) {
			ret = (T) new JcNode(null, this, OPERATOR.Collection.GET);
		} else {
			ret = (T) new JcValue(null, this, OPERATOR.Collection.GET);
		}
		ret.setHint(ValueAccess.hintKey_opValue, indexExpression);
		QueryRecorder.recordInvocationConditional(this, "get", ret, QueryRecorder.placeHolder(indexExpression));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the first element of a collection</i></div>
	 * <br/>
	 */
	@SuppressWarnings("unchecked")
	public T head() {
		T ret;
		if (JcRelation.class.equals(type)) {
			ret = (T) new JcRelation(null, this, 
					new FunctionInstance(FUNCTION.Collection.HEAD, 1));
		} else if (JcNode.class.equals(type)) {
			ret = (T) new JcNode(null, this, 
					new FunctionInstance(FUNCTION.Collection.HEAD, 1));
		} else {
			ret = (T) new JcValue(null, this, 
					new FunctionInstance(FUNCTION.Collection.HEAD, 1));
		}
		QueryRecorder.recordInvocationConditional(this, "head", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the last element of a collection</i></div>
	 * <br/>
	 */
	@SuppressWarnings("unchecked")
	public T last() {
		T ret;
		if (JcRelation.class.equals(type)) {
			ret = (T) new JcRelation(null, this, 
					new FunctionInstance(FUNCTION.Collection.LAST, 1));
		} else if (JcNode.class.equals(type)) {
			ret = (T) new JcNode(null, this, 
					new FunctionInstance(FUNCTION.Collection.LAST, 1));
		} else {
			ret = (T) new JcValue(null, this, 
					new FunctionInstance(FUNCTION.Collection.LAST, 1));
		}
		QueryRecorder.recordInvocationConditional(this, "last", ret);
		return ret;
	}
	
	Object getValue() {
		return value;
	}
	
	private static String fromPrimitiveList(List<?> list) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (list != null) {
			int idx = 0;
			for (Object val : list) {
				if (idx > 0)
					sb.append(", ");
				if (val instanceof String) {
					sb.append('\'');
					sb.append(val.toString());
					sb.append('\'');
				} else
					sb.append(val.toString());
				idx++;
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
