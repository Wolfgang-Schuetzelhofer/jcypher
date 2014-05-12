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

package iot.jcypher;

import iot.jcypher.api.IClause;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER QUERY</i></b></div>
 */
public class JcQuery {

	private IClause clauses[];

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>answer the set of clauses which compose the query</i></div>
	 * <br/>
	 */
	public IClause[] getClauses() {
		return clauses;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set an array of clauses which compose the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. JcQuery query = new JcQuery();</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>query.setClauses(new IClause[] {
	  * <br/>CREATE.node(juliusCaesar).property("title").value("Julius Caesar"),
	  * <br/>CREATE...
	  * <br/>});</i></div></i></div>
	 * <br/>
	 */
	public void setClauses(IClause[] clauses) {
		this.clauses = clauses;
	}
}
