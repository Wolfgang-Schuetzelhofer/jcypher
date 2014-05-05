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

public class PredicateConcatenator implements IPredicateHolder {

	private ConcatOperator concatOperator;
	private Predicate predicate;
	
	public ConcatOperator getConcatOperator() {
		return concatOperator;
	}

	public void setConcatOperator(ConcatOperator concatOperator) {
		this.concatOperator = concatOperator;
	}

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

	/*****************************************************************/
	public enum ConcatOperator {
		AND, OR, XOR
	}
}
