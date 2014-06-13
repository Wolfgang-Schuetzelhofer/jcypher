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

package iot.jcypher.query.ast.collection;

import iot.jcypher.query.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class DoEvalExpression extends EvalExpression {
	private List<ASTNode> clauses = new ArrayList<ASTNode>();

	public List<ASTNode> getClauses() {
		return clauses;
	}

	public void setClauses(List<ASTNode> clauses) {
		this.clauses = clauses;
	}
}
