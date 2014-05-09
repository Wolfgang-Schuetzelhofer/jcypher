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

package iot.jcypher.api.start;

import iot.jcypher.ast.start.StartExpression;
import iot.jcypher.values.JcNode;
import iot.jcypher.values.JcRelation;

public class SFactory {

	public static SNodeOrRelation node(JcNode node) {
		StartExpression sx = new StartExpression(node);
		SNodeOrRelation strt = new SNodeOrRelation(sx);
		return strt;
	}
	
	public static SNodeOrRelation relation(JcRelation relation) {
		StartExpression sx = new StartExpression(relation);
		SNodeOrRelation strt = new SNodeOrRelation(sx);
		return strt;
	}
}
