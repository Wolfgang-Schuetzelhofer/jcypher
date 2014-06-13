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

package iot.jcypher.query.api.start;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.ast.start.StartExpression;


public class SPropertyValue extends APIObject {
	
	SPropertyValue(StartExpression sx) {
		super();
		this.astNode = sx;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set the property value to be matched by the lookup</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. START.node(n).byIndex("Person").property("name").<b>value("Tobias")</b></i></div>
	 * <br/>
	 */
	public StartPoint value(Object value) {
		StartExpression sx = (StartExpression)this.astNode;
		sx.getPropertyOrQuery().setPropertyValue(value);
		StartPoint ret = new StartPoint(sx);
		return ret;
	}
}
