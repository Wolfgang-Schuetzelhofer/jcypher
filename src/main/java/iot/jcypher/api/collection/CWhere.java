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
import iot.jcypher.api.APIObjectAccess;
import iot.jcypher.api.predicate.Concat;
import iot.jcypher.api.predicate.PFactory;
import iot.jcypher.ast.collection.CollectExpression;
import iot.jcypher.ast.collection.PredicateEvalExpression;
import iot.jcypher.ast.predicate.PredicateExpression;

public class CWhere extends APIObject {

	CWhere(CollectExpression cx) {
		super();
		this.astNode = cx;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>start a predicate expression which is evaluated against each element of a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...holdsTrue(I.forAll(n)...<b>WHERE()</b>...</i></div>
	 * <br/>
	 */
	public Concat WHERE() {
		Concat concat = PFactory.createConcat();
		PredicateExpression px = (PredicateExpression) APIObjectAccess.getAstNode(concat);
		CollectExpression collXpr = (CollectExpression)this.astNode;
		((PredicateEvalExpression)collXpr.getEvalExpression())
				.setPredicateExpression(px);
		px.setContainingCollectExpression(collXpr);
		return concat;
	}

}
