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

package iot.jcypher.factories.clause;

import iot.jcypher.api.APIObjectAccess;
import iot.jcypher.api.union.UnionFactory;
import iot.jcypher.api.union.UnionTerminal;
import iot.jcypher.ast.ASTNode;
import iot.jcypher.clause.ClauseType;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 * <div color='red' style="font-size:18px;color:red"><i>combine results of multiple queries</i></div>
 * <br/>
 */
public class UNION {
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>combine the results of two queries</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>UNION.all()</b></i></div>
	 * <br/>
	 */
	public static UnionTerminal all() {
		UnionTerminal ret = UnionFactory.all();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.UNION_ALL);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>combine the results of two queries, remove duplicates</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>UNION.distinct()</b></i></div>
	 * <br/>
	 */
	public static UnionTerminal distinct() {
		UnionTerminal ret = UnionFactory.distinct();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.UNION);
		return ret;
	}
}
