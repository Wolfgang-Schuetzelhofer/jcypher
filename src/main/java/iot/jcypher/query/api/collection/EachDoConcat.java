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

package iot.jcypher.query.api.collection;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.ast.collection.CollectExpression;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.DO;

public class EachDoConcat extends APIObject {

	EachDoConcat(CollectExpression cx) {
		super();
		this.astNode = cx;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>start the DO part of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>DO()</b>
				.SET(n.property("marked")).to(true)...</i></div>
	 * <br/>
	 */
	public Do DO() {
		Do ret = new Do((CollectExpression)this.astNode);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the DO part of a FOREACH expression by means of a list of clauses.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...
	 * <pre>FOR_EACH...
	 *   .DO(new IClause[]{
	 *     CREATE.node(n).relation(r).in().node(n1),
	 *     DO.SET(n2.property("name")).to("John")
	 *   })...</pre>
	 * <br/>
	 */
	public DoConcat DO(IClause[] clauses) {
		((CollectExpression)this.astNode).setCreationClauses(clauses);
		Do doConnector = new Do((CollectExpression)this.astNode);
		DoConcat ret = new DoConcat((CollectExpression)this.astNode, doConnector);
		return ret;
	}
}
