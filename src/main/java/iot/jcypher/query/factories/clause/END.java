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

import iot.jcypher.query.api.cases.CaseFactory;
import iot.jcypher.query.api.cases.CaseTerminal;
import iot.jcypher.query.api.cases.EndTerminal;
import iot.jcypher.query.ast.ClauseType;
import iot.jcypher.query.ast.cases.CaseExpression;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE - END of a CASE expression</i></b></div>
 */
public class END {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>end a CASE expression</i></div>
	 * <br/>WHEN...
	 * <br/>ELSE.perform() ...</b></i></div>
	 * <br/>
	 */
	public static EndTerminal caseXpr() {
		CaseExpression cx = new CaseExpression();
		EndTerminal ret = CaseFactory.createEndTerminal(cx);
		cx.setClauseType(ClauseType.END);
		return ret;
	}
}
