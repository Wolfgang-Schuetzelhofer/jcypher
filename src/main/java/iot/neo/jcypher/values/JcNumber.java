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

package iot.neo.jcypher.values;

import iot.neo.jcypher.values.functions.FUNCTION;
import iot.neo.jcypher.values.operators.OPERATOR;

public class JcNumber extends JcPrimitive {

	JcNumber() {
		super();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a number initialized with a value</i></div>
	 * <br/>
	 */
	public JcNumber(Number number) {
		this(number, null, null);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a number which is identified by a name</i></div>
	 * <br/>
	 */
	public JcNumber(String name) {
		this(name, 0, null, null);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a number which is identified by a name and initialized with a value</i></div>
	 * <br/>
	 */
	public JcNumber(String name, Number number) {
		this(name, number, null, null);
	}
	
	JcNumber(Object val, ValueElement predecessor, IOperatorOrFunction opf) {
		this(null, val, predecessor, opf);
	}
	
	JcNumber(String name, Object val, ValueElement predecessor, IOperatorOrFunction opf) {
		super(name, val, predecessor, opf);
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of adding two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber plus(Number val) {
		return new JcNumber(val, this, OPERATOR.Number.PLUS);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of adding two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber plus(JcNumber val) {
		return new JcNumber(val, this, OPERATOR.Number.PLUS);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of subtracting two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber minus(Number val) {
		return new JcNumber(val, this, OPERATOR.Number.MINUS);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of subtracting two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber minus(JcNumber val) {
		return new JcNumber(val, this, OPERATOR.Number.MINUS);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of multiplying two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber mult(Number val) {
		return new JcNumber(val, this, OPERATOR.Number.MULT);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of multiplying two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber mult(JcNumber val) {
		return new JcNumber(val, this, OPERATOR.Number.MULT);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of dividing a number by another number, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber div(Number val) {
		return new JcNumber(val, this, OPERATOR.Number.DIV);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of dividing a number by another number, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber div(JcNumber val) {
		return new JcNumber(val, this, OPERATOR.Number.DIV);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>enclose an expression with brackets</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>a.numberProperty("amount").div(b.numberProperty("amount"))<br/>.enclose().mult(2)</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>maps to an expression <b>(a.amount / b.amount) * 2</b></i></div>
	 * <br/>
	 */
	public JcNumber enclose() {
		return new JcNumber(null, this,
				new FunctionInstance(FUNCTION.Common.ENCLOSE, 1));
	}
}
