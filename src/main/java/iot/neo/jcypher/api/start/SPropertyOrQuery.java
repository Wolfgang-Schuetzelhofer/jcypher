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

package iot.neo.jcypher.api.start;

import iot.neo.jcypher.api.APIObject;
import iot.neo.jcypher.ast.start.PropertyOrQuery;
import iot.neo.jcypher.ast.start.StartExpression;


public class SPropertyOrQuery extends APIObject {

	SPropertyOrQuery(StartExpression sx) {
		super();
		this.astNode = sx;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select the property to be matched by the lookup</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. START.node(n).byIndex("Person").<b>property("name")</b></i></div>
	 * <br/>
	 */
	public SPropertyValue property(String name) {
		StartExpression sx = (StartExpression)this.astNode;
		sx.setPropertyOrQuery(new PropertyOrQuery(name, null));
		SPropertyValue ret = new SPropertyValue(sx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a lucene query to be used by the lookup</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. START.node(n).byIndex("Person").<b>query("name:A")</b></i></div>
	 * <br/>
	 */
	public StartPoint query(String luceneQuery) {
		StartExpression sx = (StartExpression)this.astNode;
		sx.setPropertyOrQuery(new PropertyOrQuery(null, luceneQuery));
		StartPoint ret = new StartPoint(sx);
		return ret;
	}
}
