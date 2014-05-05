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

package iot.neo.jcypher.api.pattern;

import iot.neo.jcypher.api.APIObject;
import iot.neo.jcypher.api.APIObjectAccess;
import iot.neo.jcypher.ast.pattern.PatternExpression;
import iot.neo.jcypher.ast.pattern.PatternNode;
import iot.neo.jcypher.ast.pattern.PatternPath;
import iot.neo.jcypher.ast.pattern.PatternPath.PathFunction;
import iot.neo.jcypher.ast.pattern.PatternRelation;
import iot.neo.jcypher.values.JcNode;
import iot.neo.jcypher.values.JcPath;
import iot.neo.jcypher.values.JcRelation;

public class XFactory {

	public static Node node() {
		return node(null);
	}
	
	public static Node node(JcNode jcNode) {
		PatternExpression px = new PatternExpression();
		PatternNode pn = new PatternNode(jcNode);
		px.addElement(pn);
		return new Node(px);
	}
	
	public static Relation relation() {
		return relation(null);
	}
	
	public static Relation relation(JcRelation jcRelation) {
		PatternExpression px = new PatternExpression();
		PatternRelation pr = new PatternRelation(jcRelation);
		px.addElement(pr);
		return new Relation(px);
	}
	
	public static Path path(JcPath jcPath) {
		PatternExpression px = new PatternExpression();
		PatternPath path = new PatternPath(jcPath, PathFunction.PATH);
		px.setPath(path);
		return new Path(px);
	}
	
	public static Path shortestPath(JcPath jcPath) {
		PatternExpression px = new PatternExpression();
		PatternPath path = new PatternPath(jcPath, PathFunction.SHORTEST_PATH);
		px.setPath(path);
		return new Path(px);
	}
	
	public static Path allShortestPaths(JcPath jcPath) {
		PatternExpression px = new PatternExpression();
		PatternPath path = new PatternPath(jcPath, PathFunction.ALL_SHORTEST_PATHS);
		px.setPath(path);
		return new Path(px);
	}
	
	public static PatternExpression getExpression(IElement element) {
		return (PatternExpression) APIObjectAccess.getAstNode((APIObject)element);
	}
}
