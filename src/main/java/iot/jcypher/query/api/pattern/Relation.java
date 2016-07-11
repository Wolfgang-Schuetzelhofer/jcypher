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

import iot.jcypher.query.ast.pattern.PatternExpression;
import iot.jcypher.query.ast.pattern.PatternNode;
import iot.jcypher.query.ast.pattern.PatternRelation;
import iot.jcypher.query.values.JcNode;

public class Relation extends Element<Relation> {

	Relation(PatternExpression expression) {
		super(expression);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a type to match or create relations with that type</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation(r).<b>type("Works_At")</b></i></div>
	 * <br/>
	 */
	public Relation type(String type) {
		getPatternRelation().getTypes().add(type);
		return this;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a type to match or create relations with that type</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>using enum type</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation(r).<b>type(MyRelations.WORKS_AT)</b></i></div>
	 * <br/>
	 */
	public Relation type(Enum<?> type) {
		getPatternRelation().getTypes().add(type.name());
		return this;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match or create (depends on the clause) nodes connected to the relation</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation().<b>node()</b></i></div>
	 * <br/>
	 */
	public Node node() {
		return node(null);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match or create (depends on the clause) nodes connected to the relation, 
	 * <br/>make them accessible by a JcNode element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation().<b>node(m)</b></i></div>
	 * <br/>
	 */
	public Node node(JcNode jcNode) {
		PatternExpression px = (PatternExpression)this.astNode;
		PatternNode pn = new PatternNode(jcNode);
		px.addElement(pn);
		Node ret = new Node(px);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>define the relation direction as incoming</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation(r).<b>in()</b></i></div>
	 * <br/>
	 */
	public Relation in() {
		getPatternRelation().in();
		return this;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>define the relation direction as outgoing</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation(r).<b>out()</b></i></div>
	 * <br/>
	 */
	public Relation out() {
		getPatternRelation().out();
		return this;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>define the minimum number of hops 
	 * (hops are the number of relations between two nodes in a pattern);<br/>if hops are not defined explicitly, it defaults to 1</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation().<b>minHops(2)</b>.node(m)</i></div>
	 * <br/>
	 */
	public Relation minHops(int minHops) {
		getPatternRelation().minHops(minHops);
		return this;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>define the maximum number of hops 
	 * (hops are the number of relations between two nodes in a pattern);<br/>if hops are not defined explicitly, it defaults to 1</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation().<b>maxHops(3)</b>.node(m)</i></div>
	 * <br/>
	 */
	public Relation maxHops(int maxHops) {
		getPatternRelation().maxHops(maxHops);
		return this;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>define the maximum number of hops as infinite
	 * (hops are the number of relations between two nodes in a pattern)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation().<b>maxHopsUnbound()</b>.node(m)</i></div>
	 * <br/>
	 */
	public Relation maxHopsUnbound() {
		getPatternRelation().maxHopsUnbound();
		return this;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>define the minimum number of hops as 0 and the maximum number of hops as infinite
	 * <br/>(hops are the number of relations between two nodes in a pattern);
	 * <br/>if hops are not defined explicitly, the number of hops defaults to 1</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation().<b>hopsUnbound()</b>.node(m)</i></div>
	 * <br/>
	 */
	public Relation hopsUnbound() {
		getPatternRelation().hopsUnbound();
		return this;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>define the exact number of hops 
	 * (hops are the number of relations between two nodes in a pattern);<br/>if hops are not defined explicitly, it defaults to 1</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).relation().<b>hops(3)</b>.node(m)</i></div>
	 * <br/>
	 */
	public Relation hops(int hops) {
		getPatternRelation().hops(hops);
		return this;
	}
	
	private PatternRelation getPatternRelation() {
		PatternExpression px = (PatternExpression)this.astNode;
		return (PatternRelation) px.getLastElement();
	}
}
