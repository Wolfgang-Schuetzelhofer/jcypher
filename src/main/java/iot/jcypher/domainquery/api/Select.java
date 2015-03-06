/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.domainquery.api;

import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.ast.SelectExpression;

public class Select<T> extends APIObject {

	Select(SelectExpression<T> se) {
		this.astObject = se;
	}
	
	/**
	 * Specify one or more predicate expressions to constrain the set of domain objects
	 * @param where one or more predicate expressions
	 */
	public DomainObjectMatch<T> ELEMENTS(TerminalResult...  where) {
		// all where expressions have already been added to the astObjects list
		// of the SelectExpression
		SelectExpression<T> se = this.getSelectExpression();
		DomainObjectMatch<T> selDom =APIAccess.createDomainObjectMatch(
					se.getStart().getDomainObjectType(),
				se.getQueryExecutor().getDomainObjectMatches().size(),
				se.getQueryExecutor().getMappingInfo());
		se.getQueryExecutor().getDomainObjectMatches().add(selDom);
		se.resetAstObjectsContainer();
		se.setEnd(selDom);
		if (se.isReject()) {
			// remove it from the return statement
			// it is only temporary
			APIAccess.setPartOfReturn(selDom, false);
			DomainQuery q = se.getDomainQuery();
			DomainObjectMatch<T> rejectDom = q.createMatch(se.getStart().getDomainObjectType());
			// build complementary set
			q.WHERE(rejectDom).IN(se.getStart());
			q.WHERE(rejectDom).NOT().IN(selDom);
			return rejectDom;
		}
		return selDom;
	}
	
	@SuppressWarnings("unchecked")
	private SelectExpression<T> getSelectExpression() {
		return (SelectExpression<T>) this.astObject;
	}
}
