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

package iot.jcypher.query.api.cases;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.ast.cases.CaseExpression;
import iot.jcypher.query.values.JcValue;

public class EndTerminal extends APIObject implements IClause {

	EndTerminal(CaseExpression cx) {
		super();
		this.astNode = cx;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify an alias for the result</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>you need to do this if you want to return a result from the CASE statement</i></div>
	 * <br/>
	 */
	public CaseTerminal AS(JcValue alias) {
		CaseExpression cx = (CaseExpression) this.astNode;
		cx.setEndAlias(alias);
		CaseTerminal ret = CaseFactory.createCaseTerminal(cx);
		return ret;
	}
}
