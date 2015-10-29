/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.query.factories.clause;

import iot.jcypher.query.api.APIObjectAccess;
import iot.jcypher.query.api.predicate.BooleanOperation;
import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.ast.ClauseType;
import iot.jcypher.query.ast.cases.CaseExpression.WhenJcValue;
import iot.jcypher.query.factories.xpression.P;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.values.ValueElement;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE - WHEN part of a CASE expression</i></b></div>
 */
public class WHEN {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>WHEN part of a CASE expression pairs with <b>CASE.result()</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>takes an expression like '<b>n.property("age")</b>', yielding a property</i></div>
	 * <br/>
	 */
	public static BooleanOperation valueOf(ValueElement val) {
		BooleanOperation ret = P.valueOf(val);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WHEN);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>WHEN part of a CASE expression pairs with <b>CASE.resultOf(...)</b></i></div>
	 * <br/>
	 */
	public static BooleanOperation value() {
		JcValue val = new WhenJcValue();
		BooleanOperation ret = P.valueOf(val);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.WHEN);
		return ret;
	}
}
