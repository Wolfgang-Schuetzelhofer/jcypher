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

import iot.jcypher.query.values.functions.FUNCTION;

public class ValueElement implements IFragment {

	private ValueElement predecessor;
	private IOperatorOrFunction operatorOrFunction;

	ValueElement() {
		super();
	}
	
	protected ValueElement(ValueElement pred, IOperatorOrFunction opf) {
		super();
		this.predecessor = pred;
		this.operatorOrFunction = opf;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>convert to a string, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString str() {
		return new JcString(null, this,
				new FunctionInstance(FUNCTION.Common.STR, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>convert the argument to an integer, return a <b>JcNumber</b>, if the conversion fails return NULL</i></div>
	 * <br/>
	 */
	public JcNumber toInt() {
		return new JcNumber(null, this,
				new FunctionInstance(FUNCTION.Common.TOINT, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>convert the argument to a float, return a <b>JcNumber</b>, if the conversion fails return NULL</i></div>
	 * <br/>
	 */
	public JcNumber toFloat() {
		return new JcNumber(null, this,
				new FunctionInstance(FUNCTION.Common.TOFLOAT, 1));
	}

	ValueElement getPredecessor() {
		return predecessor;
	}

	void setPredecessor(ValueElement predecessor) {
		this.predecessor = predecessor;
	}

	IOperatorOrFunction getOperatorOrFunction() {
		return operatorOrFunction;
	}

	void setOperatorOrFunction(IOperatorOrFunction operatorOrFunction) {
		this.operatorOrFunction = operatorOrFunction;
	}
}
