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
import iot.jcypher.api.using.UFactory;
import iot.jcypher.api.using.UsingIndex;
import iot.jcypher.api.using.UsingScan;
import iot.jcypher.ast.ASTNode;
import iot.jcypher.ast.ClauseType;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 */
public class USING {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set the name of the index to use</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>USING.INDEX("Person")</b>...</i></div>
	 * <br/>
	 */
	public static UsingIndex INDEX(String indexName) {
		UsingIndex ret = UFactory.index(indexName);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.USING_INDEX);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set the label to be used in the label scan</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>USING.LABEL_SCAN("German")</b>...</i></div>
	 * <br/>
	 */
	public static UsingScan LABEL_SCAN(String labelName) {
		UsingScan ret = UFactory.onLabel(labelName);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.USING_SCAN);
		return ret;
	}
}
