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

public class MathFunctions {
	
	private JcNumber argument;

	MathFunctions(JcNumber argument) {
		super();
		this.argument = argument;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the absolute value of a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().abs()</b></i></div>
	 * <br/>
	 */
	public JcNumber abs() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.ABS, 1));
		QueryRecorder.recordInvocationConditional(this, "abs", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the arccosine of a number, in radians.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().acos()</b></i></div>
	 * <br/>
	 */
	public JcNumber acos() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.ACOS, 1));
		QueryRecorder.recordInvocationConditional(this, "acos", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the arcsine of a number, in radians.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().asin()</b></i></div>
	 * <br/>
	 */
	public JcNumber asin() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.ASIN, 1));
		QueryRecorder.recordInvocationConditional(this, "asin", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the arctangent of a number, in radians.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().atan()</b></i></div>
	 * <br/>
	 */
	public JcNumber atan() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.ATAN, 1));
		QueryRecorder.recordInvocationConditional(this, "atan", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the cosine of a number, in radians.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().cos()</b></i></div>
	 * <br/>
	 */
	public JcNumber cos() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.COS, 1));
		QueryRecorder.recordInvocationConditional(this, "cos", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the cotangent of a number, in radians.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().cot()</b></i></div>
	 * <br/>
	 */
	public JcNumber cot() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.COT, 1));
		QueryRecorder.recordInvocationConditional(this, "cot", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>convert radians to degrees.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument (containing the radians) is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("radians").math().degrees()</b></i></div>
	 * <br/>
	 */
	public JcNumber degrees() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.DEGREES, 1));
		QueryRecorder.recordInvocationConditional(this, "degrees", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>convert degrees to radians.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument (containing the degrees) is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("degrees").math().radians()</b></i></div>
	 * <br/>
	 */
	public JcNumber radians() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.RADIANS, 1));
		QueryRecorder.recordInvocationConditional(this, "radians", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the value e raised to the power of a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("power").math().exp()</b> ->(e^n.power)</i></div>
	 * <br/>
	 */
	public JcNumber exp() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.EXP, 1));
		QueryRecorder.recordInvocationConditional(this, "exp", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the greatest integer less than or equal to a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().floor()</b></i></div>
	 * <br/>
	 */
	public JcNumber floor() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.FLOOR, 1));
		QueryRecorder.recordInvocationConditional(this, "floor", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the the half versine of a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Please have a look at the Neo4j manual for a description on how to use
	 *  the haversin function to compute the distance between two points on the surface of a sphere.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().haversin()</b></i></div>
	 * <br/>
	 */
	public JcNumber haversin() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.HAVERSIN, 1));
		QueryRecorder.recordInvocationConditional(this, "haversin", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the natural logarithm of a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().log()</b></i></div>
	 * <br/>
	 */
	public JcNumber log() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.LOG, 1));
		QueryRecorder.recordInvocationConditional(this, "log", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the base 10 logarithm of a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().log10()</b></i></div>
	 * <br/>
	 */
	public JcNumber log10() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.LOG10, 1));
		QueryRecorder.recordInvocationConditional(this, "log10", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a number rounded to the nearest integer.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().round()</b></i></div>
	 * <br/>
	 */
	public JcNumber round() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.ROUND, 1));
		QueryRecorder.recordInvocationConditional(this, "round", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the signum of a number — zero if the expression is zero, -1 for any negative number, and 1 for any positive number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().sign()</b></i></div>
	 * <br/>
	 */
	public JcNumber sign() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.SIGN, 1));
		QueryRecorder.recordInvocationConditional(this, "sign", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the sine of a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().sin()</b></i></div>
	 * <br/>
	 */
	public JcNumber sin() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.SIN, 1));
		QueryRecorder.recordInvocationConditional(this, "sin", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the square root of a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().sqrt()</b></i></div>
	 * <br/>
	 */
	public JcNumber sqrt() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.SQRT, 1));
		QueryRecorder.recordInvocationConditional(this, "sqrt", ret);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the the tangent of a number.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>The number argument is the one to which this function is appended,</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>in accordance to a post-fix notation.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>n.numberProperty("number").math().tan()</b></i></div>
	 * <br/>
	 */
	public JcNumber tan() {
		JcNumber ret = new JcNumber(null, this.argument,
				new FunctionInstance(FUNCTION.Math.TAN, 1));
		QueryRecorder.recordInvocationConditional(this, "tan", ret);
		return ret;
	}

	JcNumber getArgument() {
		return argument;
	}
}
