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
import iot.jcypher.query.api.nativ.NatCypher;
import iot.jcypher.query.api.nativ.NatFactory;
import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.ast.ClauseType;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 * <div color='red' style="font-size:18px;color:red"><i>provides the ability to write <b>CYPHER</b> expressions which are not implemented in <b>JCypher</b> yet</i></div>
 * <br/>
 */
public class NATIVE {
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>write one or more line(s) of <b>CYPHER</b> text (e.g. for expressions which are not implemented in <b>JCypher</b> yet)</i></div>
	 * <br/>
	 */
	public static NatCypher cypher(String...line) {
		NatCypher ret = NatFactory.cypher(line);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.CYPHER_NATIVE);
		return ret;
	}
}
