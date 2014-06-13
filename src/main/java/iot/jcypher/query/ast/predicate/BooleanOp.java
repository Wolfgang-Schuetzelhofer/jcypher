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

package iot.jcypher.query.ast.predicate;

import iot.jcypher.query.values.ValueElement;

public class BooleanOp extends Predicate {

	private ValueElement operand1;
	private Operator operator;
	private Object operand2;
	
	public ValueElement getOperand1() {
		return operand1;
	}

	public void setOperand1(ValueElement operand1) {
		this.operand1 = operand1;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Object getOperand2() {
		return operand2;
	}

	public void setOperand2(Object operand2) {
		this.operand2 = operand2;
	}

	/*****************************************************************/
	public enum Operator {
		EQUALS, NOT_EQUALS, LT, GT, LTE, GTE, REGEX, IN, IS_NULL, HAS
	}
}
