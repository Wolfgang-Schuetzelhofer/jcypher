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

public class JcRelation extends JcElement {

	JcRelation() {
		super();
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a relation which is identified by a name</i></div>
	 * <br/>
	 */
	public JcRelation(String name) {
		super(name);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the type of a relation, return a <b>JcString</b></i></div>
	 * <br/>
	 */
	public JcString type() {
		return new JcString(null, this,
				new FunctionInstance(FUNCTION.Relation.TYPE, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the start node of a relation, return a <b>JcNode</b></i></div>
	 * <br/>
	 */
	public JcNode startNode() {
		return new JcNode(null, this,
				new FunctionInstance(FUNCTION.Relation.STARTNODE, 1));
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the end node of a relation, return a <b>JcNode</b></i></div>
	 * <br/>
	 */
	public JcNode endNode() {
		return new JcNode(null, this,
				new FunctionInstance(FUNCTION.Relation.ENDNODE, 1));
	}
}
