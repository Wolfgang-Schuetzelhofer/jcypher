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

package iot.jcypher.api.collection;

import iot.jcypher.api.APIObject;
import iot.jcypher.ast.collection.CollectExpression;
import iot.jcypher.values.JcValue;

public class CFrom<T extends APIObject> extends APIObject {

	private T connector;

	CFrom(CollectExpression cx, T connector) {
		super();
		this.astNode = cx;
		this.connector = connector;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>define a variable (a JcValue or a subclass like JcNode) to iterate over a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>fromAll(n)</b>.IN_nodes(p)</i></div>
	 * <br/>
	 */
	public InCollection<T> fromAll(JcValue jcValue) {
		CollectExpression cx = (CollectExpression)this.astNode;
		cx.setIterationVariable(jcValue);
		return new InCollection<T>(cx, this.connector);
	}
}
