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

package iot.jcypher.api.modify;

import iot.jcypher.api.APIObject;
import iot.jcypher.ast.modify.ModifyExpression;
import iot.jcypher.values.ValueElement;

public class Set<T extends APIObject> extends APIObject {

	private T connector;
	
	Set(ModifyExpression mx, T connector) {
		super();
		this.astNode = mx;
		this.connector = connector;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER LANGUAGE ELEMENT</i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set to a primitive value like a String or a Number</i></div>
	 * <br/>
	 */
	public <E> T to(E value) {
		ModifyExpression mx = (ModifyExpression)this.astNode;
		mx.setValue(value);
		return this.connector;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER LANGUAGE ELEMENT</i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set by an Expression as e.g.: <b>a.stringProperty("name").concat("<->").concat(b.stringProperty("name"))</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or: <b>a.numberProperty("amount").plus(b.numberProperty("amount"))</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i> or simply a reference to a named element e.g. <b>val</b></i></div>
	 * <br/>
	 */
	public T byExpression(ValueElement expression) {
		ModifyExpression mx = (ModifyExpression)this.astNode;
		mx.setValueExpression(expression);
		return this.connector;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER LANGUAGE ELEMENT</i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>remove the property (set to <b>NULL</b>)</i></div>
	 * <br/>
	 */
	public ModifyTerminal toNull() {
		ModifyExpression mx = (ModifyExpression)this.astNode;
		mx.setToNull();
		ModifyTerminal ret = new ModifyTerminal(mx);
		return ret;
	}
}
