/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.query.values.functions.FUNCTION;
import iot.jcypher.query.values.operators.OPERATOR;

public class JcString extends JcPrimitive {

	JcString() {
		super();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a string which is identified by a name</i></div>
	 * <br/>
	 */
	public JcString(String name) {
		this(name, null, null, null);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a string which is identified by a name and initialized with a value</i></div>
	 * <br/>
	 */
	public JcString(String name, String value) {
		this(name, value, null, null);
	}
	
	JcString(Object val, ValueElement predecessor, IOperatorOrFunction opf) {
		this(null, val, predecessor, opf);
	}
	
	JcString(String name, Object val, ValueElement predecessor, IOperatorOrFunction opf) {
		super(name, val, predecessor, opf);
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of concatenating two strings, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString concat(String concat) {
		JcString ret = new JcString(concat, this, OPERATOR.String.CONCAT);
		QueryRecorder.recordInvocationConditional(this, "concat", ret, QueryRecorder.literal(concat));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of concatenating two strings, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString concat(JcString concat) {
		JcString ret = new JcString(concat, this, OPERATOR.String.CONCAT);
		QueryRecorder.recordInvocationConditional(this, "concat", ret, QueryRecorder.placeHolder(concat));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of removing leading and trailing white spaces form a string, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString trim() {
		JcString ret = new JcString(null, this,
				new FunctionInstance(FUNCTION.String.TRIM, 1));
		QueryRecorder.recordInvocationConditional(this, "trim", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of removing leading white spaces form a string, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString trimLeft() {
		JcString ret = new JcString(null, this,
				new FunctionInstance(FUNCTION.String.LTRIM, 1));
		QueryRecorder.recordInvocationConditional(this, "trimLeft", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of removing trailing white spaces form a string, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString trimRight() {
		JcString ret = new JcString(null, this,
				new FunctionInstance(FUNCTION.String.RTRIM, 1));
		QueryRecorder.recordInvocationConditional(this, "trimRight", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the length of a string, return a <b>JcNumber</b></i></div>
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
	 * <div color='red' style="font-size:18px;color:red"><i>specify the part of a string which should be replaced</i></div>
	 * <br/>
	 */
	public ReplaceWith replace(String what) {
		ReplaceWith ret = new ReplaceWith(what, this, OPERATOR.Common.COMMA_SEPARATOR);
		QueryRecorder.recordInvocationConditional(this, "replace", ret, QueryRecorder.literal(what));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the part of a string which should be replaced</i></div>
	 * <br/>
	 */
	public ReplaceWith replace(JcString what) {
		ReplaceWith ret = new ReplaceWith(what, this, OPERATOR.Common.COMMA_SEPARATOR);
		QueryRecorder.recordInvocationConditional(this, "replace", ret, QueryRecorder.placeHolder(what));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the start offset of a substring to be extracted</i></div>
	 * <br/>
	 */
	public SubString substring(int start) {
		JcNumber sub = new JcNumber(start, this, OPERATOR.Common.COMMA_SEPARATOR);
		SubString ret = new SubString(null, sub,
				new FunctionInstance(FUNCTION.String.SUBSTRING, 2));
		QueryRecorder.recordInvocationConditional(this, "substring", ret, QueryRecorder.literal(start));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a string containing the left n characters of the original string</i></div>
	 * <br/>
	 */
	public JcString left(int n) {
		JcNumber left = new JcNumber(n, this, OPERATOR.Common.COMMA_SEPARATOR);
		SubString ret = new SubString(null, left,
				new FunctionInstance(FUNCTION.String.LEFT, 2));
		QueryRecorder.recordInvocationConditional(this, "left", ret, QueryRecorder.literal(n));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a string containing the right n characters of the original string</i></div>
	 * <br/>
	 */
	public JcString right(int n) {
		JcNumber right = new JcNumber(n, this, OPERATOR.Common.COMMA_SEPARATOR);
		SubString ret = new SubString(null, right,
				new FunctionInstance(FUNCTION.String.RIGHT, 2));
		QueryRecorder.recordInvocationConditional(this, "right", ret, QueryRecorder.literal(n));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the original string in lowercase</i></div>
	 * <br/>
	 */
	public JcString lower() {
		JcString ret = new JcString(null, this,
				new FunctionInstance(FUNCTION.String.LOWER, 1));
		QueryRecorder.recordInvocationConditional(this, "lower", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the original string in uppercase</i></div>
	 * <br/>
	 */
	public JcString upper() {
		JcString ret = new JcString(null, this,
				new FunctionInstance(FUNCTION.String.UPPER, 1));
		QueryRecorder.recordInvocationConditional(this, "upper", ret);
		return ret;
	}
	
}
