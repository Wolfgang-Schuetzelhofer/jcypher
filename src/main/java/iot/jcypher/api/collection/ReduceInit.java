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
import iot.jcypher.ast.collection.ReduceEvalExpression;
import iot.jcypher.values.ValueElement;

public class ReduceInit extends APIObject {

	ReduceInit(CollectExpression cx) {
		super();
		this.astNode = cx;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify a primitive value like a String or a Number as the initial value of the accumulator variable of a REDUCE expression.
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...REDUCE()<br/>.fromAll(n).IN_nodes(p)<br/>.to(totalAge)<br/>.by(totalAge.plus(n.numberProperty("age")))<br/>.<b>startWith(0)</b></i></div>
	 * <br/>
	 */
	public CTerminal startWith(Object value) {
		CollectExpression collXpr = (CollectExpression)this.astNode;
		ReduceEvalExpression reduceEval = (ReduceEvalExpression)(collXpr).getEvalExpression();
		reduceEval.setInitialValue(value);
		return new CTerminal(collXpr);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify an expression like 'n.property("amount")' or 'n.numberProperty("age").plus(10)' 
	 * yielding a value which is used as the initial value of the accumulator variable of a REDUCE expression.
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...REDUCE()<br/>.fromAll(n).IN_nodes(p)<br/>.to(totalAge)<br/>.by(totalAge.plus(n.numberProperty("age")))<br/>.<b>startWith(0)</b></i></div>
	 * <br/>
	 */
	public CTerminal startWith(ValueElement value) {
		CollectExpression collXpr = (CollectExpression)this.astNode;
		ReduceEvalExpression reduceEval = (ReduceEvalExpression)(collXpr).getEvalExpression();
		reduceEval.setInitialValue(value);
		return new CTerminal(collXpr);
	}

}
