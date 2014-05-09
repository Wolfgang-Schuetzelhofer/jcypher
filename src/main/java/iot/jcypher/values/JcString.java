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

package iot.jcypher.values;

import iot.jcypher.values.functions.FUNCTION;
import iot.jcypher.values.operators.OPERATOR;

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
		return new JcString(concat, this, OPERATOR.String.CONCAT);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of concatenating two strings, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString concat(JcString concat) {
		return new JcString(concat, this, OPERATOR.String.CONCAT);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of removing leading and trailing white spaces form a string, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString trim() {
		return new JcString(null, this,
				new FunctionInstance(FUNCTION.String.TRIM, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of removing leading white spaces form a string, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString trimLeft() {
		return new JcString(null, this,
				new FunctionInstance(FUNCTION.String.LTRIM, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of removing trailing white spaces form a string, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString trimRight() {
		return new JcString(null, this,
				new FunctionInstance(FUNCTION.String.RTRIM, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the length of a string, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber length() {
		return new JcNumber(null, this,
				new FunctionInstance(FUNCTION.Collection.LENGTH, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the part of a string which should be replaced</i></div>
	 * <br/>
	 */
	public ReplaceWith replace(String what) {
		return new ReplaceWith(what, this, OPERATOR.Common.COMMA_SEPARATOR);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the part of a string which should be replaced</i></div>
	 * <br/>
	 */
	public ReplaceWith replace(JcString what) {
		return new ReplaceWith(what, this, OPERATOR.Common.COMMA_SEPARATOR);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the start offset of a substring to be extracted</i></div>
	 * <br/>
	 */
	public SubString substring(int start) {
		JcNumber sub = new JcNumber(start, this, OPERATOR.Common.COMMA_SEPARATOR);
		return new SubString(null, sub,
				new FunctionInstance(FUNCTION.String.SUBSTRING, 2));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a string containing the left n characters of the original string</i></div>
	 * <br/>
	 */
	public JcString left(int n) {
		JcNumber left = new JcNumber(n, this, OPERATOR.Common.COMMA_SEPARATOR);
		return new SubString(null, left,
				new FunctionInstance(FUNCTION.String.LEFT, 2));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a string containing the right n characters of the original string</i></div>
	 * <br/>
	 */
	public JcString right(int n) {
		JcNumber right = new JcNumber(n, this, OPERATOR.Common.COMMA_SEPARATOR);
		return new SubString(null, right,
				new FunctionInstance(FUNCTION.String.RIGHT, 2));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the original string in lowercase</i></div>
	 * <br/>
	 */
	public JcString lower() {
		return new JcString(null, this,
				new FunctionInstance(FUNCTION.String.LOWER, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the original string in uppercase</i></div>
	 * <br/>
	 */
	public JcString upper() {
		return new JcString(null, this,
				new FunctionInstance(FUNCTION.String.UPPER, 1));
	}
	
}
