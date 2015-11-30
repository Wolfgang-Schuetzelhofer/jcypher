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

package iot.jcypher.query.values;

import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.query.values.functions.FUNCTION;
import iot.jcypher.query.values.operators.OPERATOR;

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
		this(name, null, null, null);
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
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.PLUS);
		QueryRecorder.recordInvocationConditional(this, "plus", ret, QueryRecorder.literal(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of adding two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber plus(JcNumber val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.PLUS);
		QueryRecorder.recordInvocationConditional(this, "plus", ret, QueryRecorder.placeHolder(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of subtracting two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber minus(Number val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.MINUS);
		QueryRecorder.recordInvocationConditional(this, "minus", ret, QueryRecorder.literal(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of subtracting two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber minus(JcNumber val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.MINUS);
		QueryRecorder.recordInvocationConditional(this, "minus", ret, QueryRecorder.placeHolder(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of multiplying two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber mult(Number val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.MULT);
		QueryRecorder.recordInvocationConditional(this, "mult", ret, QueryRecorder.literal(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of multiplying two numbers, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber mult(JcNumber val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.MULT);
		QueryRecorder.recordInvocationConditional(this, "mult", ret, QueryRecorder.placeHolder(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of dividing a number by another number, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber div(Number val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.DIV);
		QueryRecorder.recordInvocationConditional(this, "div", ret, QueryRecorder.literal(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the result of dividing a number by another number, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber div(JcNumber val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.DIV);
		QueryRecorder.recordInvocationConditional(this, "div", ret, QueryRecorder.placeHolder(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the remainder of dividing one number by another, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber mod(Number val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.MOD);
		QueryRecorder.recordInvocationConditional(this, "mod", ret, QueryRecorder.literal(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the remainder of dividing one number by another, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber mod(JcNumber val) {
		JcNumber ret = new JcNumber(val, this, OPERATOR.Number.MOD);
		QueryRecorder.recordInvocationConditional(this, "mod", ret, QueryRecorder.placeHolder(val));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a number raised to the power of another number, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber pow(Number exponent) {
		JcNumber ret = new JcNumber(exponent, this, OPERATOR.Number.POW);
		QueryRecorder.recordInvocationConditional(this, "pow", ret, QueryRecorder.literal(exponent));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a number raised to the power of another number, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber pow(JcNumber exponent) {
		JcNumber ret = new JcNumber(exponent, this, OPERATOR.Number.POW);
		QueryRecorder.recordInvocationConditional(this, "pow", ret, QueryRecorder.placeHolder(exponent));
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>enclose an expression with brackets</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>a.numberProperty("amount").div(b.numberProperty("amount"))<br/>.enclose().mult(2)</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>maps to an expression <b>(a.amount / b.amount) * 2</b></i></div>
	 * <br/>
	 */
	public JcNumber enclose() {
		JcNumber ret = new JcNumber(null, this,
				new FunctionInstance(FUNCTION.Common.ENCLOSE, 1));
		QueryRecorder.recordInvocationConditional(this, "enclose", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>access mathematical functions</i></div>
	 * <br/>
	 */
	public MathFunctions math() {
		MathFunctions ret = new MathFunctions(this);
		QueryRecorder.recordInvocationConditional(this, "math", ret);
		return ret;
	}
}
