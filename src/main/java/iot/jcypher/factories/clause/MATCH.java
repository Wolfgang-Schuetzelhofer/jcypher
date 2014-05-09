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
import iot.jcypher.api.pattern.Node;
import iot.jcypher.api.pattern.Path;
import iot.jcypher.api.pattern.Relation;
import iot.jcypher.ast.ASTNode;
import iot.jcypher.clause.ClauseType;
import iot.jcypher.factories.xpression.X;
import iot.jcypher.values.JcNode;
import iot.jcypher.values.JcPath;
import iot.jcypher.values.JcRelation;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 */
public class MATCH {
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match nodes</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.node()</b></i></div>
	 * <br/>
	 */
	public static Node node() {
		Node ret = X.node();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.MATCH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match nodes, make them accessible by a JcNode element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.node(n)</b></i></div>
	 * <br/>
	 */
	public static Node node(JcNode node) {
		Node ret = X.node(node);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.MATCH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match relations</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.relation()</b></i></div>
	 * <br/>
	 */
	public static Relation relation() {
		Relation ret = X.relation();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.MATCH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>match relations, make them accessible by a JcRelation element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.relation(r)</b></i></div>
	 * <br/>
	 */
	public static Relation relation(JcRelation relation) {
		Relation ret = X.relation(relation);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.MATCH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>assign a path to a JcPath element in order to return the path or to filter on that path</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.path(p)</b>...</i></div>
	 * <br/>
	 */
	public static Path path(JcPath path) {
		Path ret = X.path(path);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.MATCH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>find a single shortest path and assign it to a JcPath element</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.shortestPath(p)</b>...</i></div>
	 * <br/>
	 */
	public static Path shortestPath(JcPath path) {
		Path ret = X.shortestPath(path);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.MATCH);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>find all shortest paths and assign them to a JcPath element</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>MATCH.allShortestPaths(p)</b>...</i></div>
	 * <br/>
	 */
	public static Path allShortestPaths(JcPath path) {
		Path ret = X.allShortestPaths(path);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.MATCH);
		return ret;
	}
}
