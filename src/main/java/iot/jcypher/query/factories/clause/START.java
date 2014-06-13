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

package iot.jcypher.query.factories.clause;

import iot.jcypher.query.api.APIObjectAccess;
import iot.jcypher.query.api.start.SFactory;
import iot.jcypher.query.api.start.SNodeOrRelation;
import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.ast.ClauseType;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 */
public class START {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select the node(s) to be the starting point(s)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>START.node(n)</b></i></div>
	 * <br/>
	 */
	public static SNodeOrRelation node(JcNode node) {
		SNodeOrRelation ret = SFactory.node(node);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.START);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select the relation(s) to be the starting point(s)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>START.relation(r)</b></i></div>
	 * <br/>
	 */
	public static SNodeOrRelation relation(JcRelation relation) {
		SNodeOrRelation ret = SFactory.relation(relation);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.START);
		return ret;
	}
}
