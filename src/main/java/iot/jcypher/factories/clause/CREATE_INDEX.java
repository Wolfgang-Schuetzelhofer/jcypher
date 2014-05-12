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
import iot.jcypher.api.index.IFactory;
import iot.jcypher.api.index.IndexFor;
import iot.jcypher.ast.ASTNode;
import iot.jcypher.ast.ClauseType;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 */
public class CREATE_INDEX {
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select a label (of nodes) on which to create an index</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>CREATE_INDEX.onLabel("Person")</b>...</i></div>
	 * <br/>
	 */
	public static IndexFor onLabel(String label) {
		IndexFor ret = IFactory.createOnLabel(label);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.CREATE_INDEX);
		return ret;
	}
}
