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

package iot.neo.jcypher.api.collection;

import iot.neo.jcypher.api.APIObject;
import iot.neo.jcypher.ast.collection.CollectExpression;
import iot.neo.jcypher.ast.collection.ExtractEvalExpression;
import iot.neo.jcypher.values.JcValue;

public class ExtractExpression extends APIObject {

	ExtractExpression(CollectExpression cx) {
		super();
		this.astNode = cx;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the value for an extract expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...EXTRACT().<b>valueOf(n.property("name"))</b>.fromAll(n).IN(nds)</i></div>
	 * <br/>
	 */
	public CFrom<CTerminal> valueOf(JcValue expression) {
		CollectExpression collXpr = (CollectExpression)this.astNode;
		((ExtractEvalExpression)collXpr.getEvalExpression())
				.setExpression(expression);
		
		CFrom<CTerminal> ret = new CFrom<CTerminal>(collXpr, new CTerminal(collXpr));
		return ret;
	}
}
