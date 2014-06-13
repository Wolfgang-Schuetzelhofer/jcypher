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

package iot.jcypher.query.ast.predicate;

import iot.jcypher.query.ast.collection.CollectExpression;
import iot.jcypher.query.ast.collection.PredicateFunctionEvalExpression;


public class PredicateFunction extends Predicate {

	private CollectExpression collectExpression;
	
	public PredicateFunctionType getType() {
		return ((PredicateFunctionEvalExpression)this.collectExpression.getEvalExpression()).getType();
	}

	public CollectExpression getCollectExpression() {
		return collectExpression;
	}

	public void setCollectExpression(CollectExpression collectExpression) {
		this.collectExpression = collectExpression;
	}

	/******************************************/
	public enum PredicateFunctionType {
		ALL, ANY, NONE, SINGLE
	}
}
