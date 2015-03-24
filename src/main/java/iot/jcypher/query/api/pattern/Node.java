/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

import java.util.List;

import iot.jcypher.domain.internal.CurrentDomain;
import iot.jcypher.query.ast.pattern.PatternExpression;
import iot.jcypher.query.ast.pattern.PatternNode;
import iot.jcypher.query.ast.pattern.PatternRelation;
import iot.jcypher.query.values.JcRelation;

public class Node extends Element<Node> {

	Node(PatternExpression expression) {
		super(expression);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match or create (depends on the clause) relations connected to the node</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).<b>relation()</b></i></div>
	 * <br/>
	 */
	public Relation relation() {
		return relation(null);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match or create (depends on the clause) relations connected to the node, 
	 * <br/>make them accessible by a JcRelation element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).<b>relation(r)</b></i></div>
	 * <br/>
	 */
	public Relation relation(JcRelation relation) {
		PatternExpression px = (PatternExpression)this.astNode;
		PatternRelation pr = new PatternRelation(relation);
		px.addElement(pr);
		Relation ret = new Relation(px);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a label to match or create nodes with that label</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).<b>label("Movie")</b></i></div>
	 * <br/>
	 */
	public Node label(String label) {
		PatternExpression px = (PatternExpression)this.astNode;
		List<String> labels = ((PatternNode)px.getLastElement()).getLabels();
		labels.add(label);
		String dLab = CurrentDomain.label.get();
		if (dLab != null && !labels.contains(dLab))
			labels.add(dLab);
		return this;
	}

}
