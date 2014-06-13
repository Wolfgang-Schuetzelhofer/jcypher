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

import iot.jcypher.query.api.APIObjectAccess;
import iot.jcypher.query.api.pattern.Node;
import iot.jcypher.query.api.pattern.Path;
import iot.jcypher.query.api.pattern.Relation;
import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.ast.ClauseType;
import iot.jcypher.query.factories.xpression.X;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcPath;
import iot.jcypher.query.values.JcRelation;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 * <div color='red' style="font-size:18px;color:red"><i>create unique graph elements</i></div>
 */
public class CREATE_UNIQUE {
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a node</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>CREATE_UNIQUE.node()</b></i></div>
	 * <br/>
	 */
	public static Node node() {
		Node ret = X.node();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.CREATE_UNIQUE);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a node, make it accessible by a JcNode element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>CREATE_UNIQUE.node(n)</b></i></div>
	 * <br/>
	 */
	public static Node node(JcNode jcNode) {
		Node ret = X.node(jcNode);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.CREATE_UNIQUE);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a relation</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>CREATE_UNIQUE.relation()</b></i></div>
	 * <br/>
	 */
	public static Relation relation() {
		Relation ret = X.relation();
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.CREATE_UNIQUE);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a relation, make it accessible by a JcRelation element later on in the query</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>CREATE_UNIQUE.relation(n)</b></i></div>
	 * <br/>
	 */
	public static Relation relation(JcRelation relation) {
		Relation ret = X.relation(relation);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.CREATE_UNIQUE);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>assign a path to a JcPath element in order to return the path or to filter on that path</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. <b>CREATE_UNIQUE.path(p)</b>...</i></div>
	 * <br/>
	 */
	public static Path path(JcPath path) {
		Path ret = X.path(path);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.CREATE_UNIQUE);
		return ret;
	}
}
