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
import iot.jcypher.query.ast.collection.CollectExpression;
import iot.jcypher.query.ast.collection.ReduceEvalExpression;
import iot.jcypher.query.values.ValueElement;

public class ReduceBy extends APIObject {

	ReduceBy(CollectExpression cx) {
		super();
		this.astNode = cx;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify an expression to be evaluated against individual elements of a collection in the context of a REDUCE expression.
	 * </br>The result of each evaluation is accumulated in a variable</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...REDUCE()<br/>.fromAll(n).IN_nodes(p)<br/>.to(totalAge)<br/>.<b>by(totalAge.plus(n.numberProperty("age")))</b><br/>.startWith(0)</i></div>
	 * <br/>
	 */
	public ReduceInit by(ValueElement valueExpression) {
		CollectExpression collXpr = (CollectExpression)this.astNode;
		ReduceEvalExpression reduceEval = (ReduceEvalExpression)(collXpr).getEvalExpression();
		reduceEval.setReduceExpression(valueExpression);
		ReduceInit ret = new ReduceInit(collXpr);
		return ret;
	}
}
