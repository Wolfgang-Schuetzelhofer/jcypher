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

package iot.jcypher.ast.collection;

import iot.jcypher.ast.ASTNode;
import iot.jcypher.values.JcValue;

public class CollectExpression extends ASTNode {

	private CollectExpression parent;
	private CollectXpressionType type;
	private JcValue iterationVariable;
	private CollectionSpec collectionToOperateOn;
	private EvalExpression evalExpression;
	
	public CollectXpressionType getType() {
		return type;
	}

	public void setType(CollectXpressionType type) {
		this.type = type;
	}

	public EvalExpression getEvalExpression() {
		return evalExpression;
	}

	public void setEvalExpression(EvalExpression evalExpression) {
		this.evalExpression = evalExpression;
	}
	
	public CollectionSpec getCollectionToOperateOn() {
		return collectionToOperateOn;
	}

	public void setCollectionToOperateOn(CollectionSpec collectionToOperateOn) {
		this.collectionToOperateOn = collectionToOperateOn;
	}

	public JcValue getIterationVariable() {
		return iterationVariable;
	}

	public void setIterationVariable(JcValue iterationVariable) {
		this.iterationVariable = iterationVariable;
	}

	public CollectExpression getParent() {
		return parent;
	}

	public void setParent(CollectExpression parent) {
		this.parent = parent;
	}

	/**************************************/
	public enum CollectXpressionType {
		EXTRACT, COLLECT, FILTER, REDUCE, TAIL, NODES, RELATIONS, LABELS,
		PATTERN_EXPRESSION, PREDICATE_FUNCTION,
		FOREACH
	}
}
