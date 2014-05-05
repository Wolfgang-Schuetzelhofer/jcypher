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

package iot.neo.jcypher.ast.predicate;

import iot.neo.jcypher.ast.ASTNode;
import iot.neo.jcypher.ast.collection.CollectExpression;
import iot.neo.jcypher.clause.ClauseType;


public class PredicateExpression extends ASTNode implements IPredicateHolder {

	private PredicateExpression parent;
	private Predicate predicate;
	private CollectExpression containingCollectExpression;
	
	public Predicate getPredicate() {
		return predicate;
	}
	
	public void setPredicate(Predicate predicate) {
		if (this.predicate != null) {
			predicate.setNotCount(this.predicate.getNotCount() +
					predicate.getNotCount());
		}
		this.predicate = predicate;
	}
	
	public Predicate getLastPredicate() {
		Predicate pred = this.predicate;
		Predicate last = pred;
		while (pred != null) {
			last = pred;
			PredicateConcatenator next = pred.getNext();
			if (next != null)
				pred = next.getPredicate();
			else
				pred = null;
		}
		return last;
	}
	
	public IPredicateHolder getLastPredicateHolder() {
		if (this.predicate == null)
			return this;
		
		Predicate pred;
		IPredicateHolder last = null;
		IPredicateHolder ret = this;
		while(ret != null) {
			last = ret;
			pred = ret.getPredicate();
			if (pred != null)
				ret = pred.getNext();
			else
				ret = null;
		}
		return last;
	}

	public PredicateExpression getParent() {
		return parent;
	}

	public void setParent(PredicateExpression parent) {
		this.parent = parent;
		if (this.parent != null && getClauseType() != null)
			this.parent.setClauseType(getClauseType());
	}

	@Override
	public void setClauseType(ClauseType clauseType) {
		super.setClauseType(clauseType);
		if (this.parent != null)
			this.parent.setClauseType(clauseType);
	}

	public CollectExpression getContainingCollectExpression() {
		return containingCollectExpression;
	}

	public void setContainingCollectExpression(
			CollectExpression containingCollectExpression) {
		this.containingCollectExpression = containingCollectExpression;
	}
	
}
