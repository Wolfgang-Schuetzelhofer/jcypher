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

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.ast.separate.SeparateExpression;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 */
public class SEPARATE extends APIObject implements IClause{

	private SEPARATE() {
		super();
		this.astNode = new SeparateExpression();
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>separates two clauses (e.g. MATCH clauses)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>new IClause[] {
	 * <br/>MATCH.node(n1),
	 * <br/>SEPERATE.nextClause(),
	 * <br/>MATCH.node(n2)}</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>leads to the cypher expression<b>MATCH (n1), MATCH (n2)</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>instead of <b>MATCH (n1), (n2)</b></i></div>
	 * <br/>
	 */
	public static SEPARATE nextClause() {
		return new SEPARATE();
	}
}
