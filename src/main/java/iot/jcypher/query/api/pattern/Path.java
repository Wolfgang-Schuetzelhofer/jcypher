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

package iot.jcypher.query.api.pattern;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.ast.pattern.PatternExpression;
import iot.jcypher.query.ast.pattern.PatternNode;
import iot.jcypher.query.values.JcNode;


public class Path extends APIObject {
	
	Path(PatternExpression px) {
		super();
		this.astNode = px;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match or create (depends on the clause) nodes as starting points of the path</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.path(p).<b>node()</b>...</i></div>
	 * <br/>
	 */
	public Node node() {
		return node(null);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match or create (depends on the clause) nodes as starting points of the path,
	 * <br/>make them accessible by a JcNode element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.path(p).<b>node(n)</b>...</i></div>
	 * <br/>
	 */
	public Node node(JcNode jcNode) {
		PatternExpression px = (PatternExpression)this.astNode;
		PatternNode pn = new PatternNode(jcNode);
		px.addElement(pn);
		Node ret = new Node(px);
		return ret;
	}

}
