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
import iot.jcypher.ast.collection.CollectionSpec;
import iot.jcypher.values.JcCollection;

public class CollectFrom extends APIObject {

	CollectFrom(CollectExpression cx) {
		super();
		this.astNode = cx;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a collection of property containers (nodes or relations)
	 * <br/>from which to collect properties</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...COLLECT().property("name").<b>from(p.nodes())</b></i></div>
	 * <br/>
	 */
	public CTerminal from(JcCollection collection) {
		CollectExpression cx = (CollectExpression)this.astNode;
		CollectionSpec cs = new CollectionSpec(collection);
		cx.setCollectionToOperateOn(cs);
		CTerminal ret = new CTerminal(cx);
		return ret;
	}
}
