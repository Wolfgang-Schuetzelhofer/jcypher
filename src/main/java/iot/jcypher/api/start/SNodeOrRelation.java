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

package iot.jcypher.api.start;

import iot.jcypher.api.APIObject;
import iot.jcypher.ast.start.IndexOrId;
import iot.jcypher.ast.start.StartExpression;

public class SNodeOrRelation extends APIObject {

	SNodeOrRelation(StartExpression sx) {
		super();
		this.astNode = sx;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>choose the id(s) of the starting point(s)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. START.node(n).<b>byId(0, 1)</b></i></div>
	 * <br/>
	 */
	public StartPoint byId(long... id) {
		getStartExpression().setIndexOrId(new IndexOrId(id));
		StartPoint ret = new StartPoint(getStartExpression());
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>choose the index to lookup</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. START.node(n).<b>byIndex("Person")</b></i></div>
	 * <br/>
	 */
	public SPropertyOrQuery byIndex(String indexName) {
		getStartExpression().setIndexOrId(new IndexOrId(indexName));
		SPropertyOrQuery ret = new SPropertyOrQuery(getStartExpression());
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>choose all elements (nodes or relations) as starting points</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. START.node(n).<b>all()</b></i></div>
	 * <br/>
	 */
	public StartPoint all() {
		getStartExpression().setAll();
		StartPoint ret = new StartPoint(getStartExpression());
		return ret;
	}
	
	private StartExpression getStartExpression() {
		return (StartExpression)this.astNode;
	}
}
