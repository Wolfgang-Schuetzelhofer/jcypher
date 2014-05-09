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

public abstract class JcElement extends JcValue {

	JcElement() {
		super();
	}

	JcElement(String name) {
		super(name);
	}
	
	JcElement(String name, ValueElement predecessor, IOperatorOrFunction opf) {
		super(name, predecessor, opf);
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>access a named property</i></div>
	 * <br/>
	 */
	public JcProperty property(String name) {
		return new JcProperty(name, this, OPERATOR.PropertyContainer.PROPERTY_ACCESS);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>access a named string property, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString stringProperty(String name) {
		return new JcString(name, this, OPERATOR.PropertyContainer.PROPERTY_ACCESS);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>access a named number property, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber numberProperty(String name) {
		return new JcNumber(name, this, OPERATOR.PropertyContainer.PROPERTY_ACCESS);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>access a named collection property, return a <b>JcCollection</b></i></div>
	 * <br/>
	 */
	public JcCollection collectionProperty(String name) {
		return new JcCollection(name, this, OPERATOR.PropertyContainer.PROPERTY_ACCESS);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>access the id of a node or relation, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber id() {
		return new JcNumber(null, this,
				new FunctionInstance(FUNCTION.PropertyContainer.ID, 1));
	}
}
