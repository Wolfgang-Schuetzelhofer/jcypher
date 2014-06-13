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

package iot.jcypher.query.api.collection;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.ast.collection.CollectExpression;


public class DoConcat extends APIObject implements IClause {

	private Do connector;

	DoConcat(CollectExpression cx, Do connector) {
		super();
		this.astNode = cx;
		this.connector = connector;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>concatenate multiple DO parts of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...DO()<br/>
				.SET(n.property("marked")).to(true).<b>AND_DO()</b><br/>
				.SET(n.property("name")).to("John")...</i></div>
	 * <br/>
	 */
	public Do AND_DO() {
		return this.connector;
	}
}
