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

public class JcPath extends JcValue {

	JcPath() {
		super();
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a path which is identified by a name</i></div>
	 * <br/>
	 */
	public JcPath(String name) {
		super(name);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all nodes of a path, return a <b>JcCollection</b></i></div>
	 * <br/>
	 */
	public JcCollection nodes() {
		return new JcCollection(null, this,
				new FunctionInstance(FUNCTION.Path.NODES, 1));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return all relations of a path, return a <b>JcCollection</b></i></div>
	 * <br/>
	 */
	public JcCollection relations() {
		return new JcCollection(null, this,
				new FunctionInstance(FUNCTION.Path.RELATIONS, 1));
	}

}
