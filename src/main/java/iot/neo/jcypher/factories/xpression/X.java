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

package iot.neo.jcypher.factories.xpression;

import iot.neo.jcypher.api.pattern.Node;
import iot.neo.jcypher.api.pattern.Path;
import iot.neo.jcypher.api.pattern.Relation;
import iot.neo.jcypher.api.pattern.XFactory;
import iot.neo.jcypher.values.JcNode;
import iot.neo.jcypher.values.JcPath;
import iot.neo.jcypher.values.JcRelation;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER FACTORY FOR PATTERN EXPRESSIONS</i></b></div>
 */
public class X {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match nodes</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.node()</b></i></div>
	 * <br/>
	 */
	public static Node node() {
		return XFactory.node();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match nodes, make them accessible by a JcNode element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.node(n)</b></i></div>
	 * <br/>
	 */
	public static Node node(JcNode node) {
		return XFactory.node(node);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match relations</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.relation()</b></i></div>
	 * <br/>
	 */
	public static Relation relation() {
		return XFactory.relation();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match relations, make them accessible by a JcRelation element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.relation(r)</b></i></div>
	 * <br/>
	 */
	public static Relation relation(JcRelation relation) {
		return XFactory.relation(relation);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>assign a path to a JcPath element in order to return the path or to filter on that path</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.path(p)</b></i></div>
	 * <br/>
	 */
	public static Path path(JcPath path) {
		return XFactory.path(path);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>find a single shortest path and assign it to a JcPath element</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.shortestPath(p)</b></i></div>
	 * <br/>
	 */
	public static Path shortestPath(JcPath path) {
		Path ret = XFactory.shortestPath(path);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>find all shortest paths and make them accessible by a JcPath element later on</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.allShortestPaths(p)</b></i></div>
	 * <br/>
	 */
	public static Path allShortestPaths(JcPath path) {
		Path ret = XFactory.allShortestPaths(path);
		return ret;
	}
}
