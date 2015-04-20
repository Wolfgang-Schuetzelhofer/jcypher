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

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQuery.IntAccess;
import iot.jcypher.domainquery.ast.ConcatenateExpression;
import iot.jcypher.domainquery.ast.IASTObject;
import iot.jcypher.domainquery.ast.PredicateExpression;
import iot.jcypher.domainquery.ast.SelectExpression;
import iot.jcypher.domainquery.ast.UnionExpression;
import iot.jcypher.domainquery.ast.ConcatenateExpression.Concatenator;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueElement;

public class Select<T> extends APIObject {

	private IntAccess intAccess;
	
	Select(SelectExpression<T> se, IntAccess ia) {
		this.astObject = se;
		this.intAccess = ia;
	}
	
	/**
	 * Specify one or more predicate expressions to constrain the set of domain objects
	 * @param where one or more predicate expressions
	 */
	public DomainObjectMatch<T> ELEMENTS(TerminalResult...  where) {
		// all where expressions have already been added to the astObjects list
		// of the SelectExpression
		SelectExpression<T> se = this.getSelectExpression();
		handleUnionExpressions(se);
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
	
	private void handleUnionExpressions(SelectExpression<T> se) {
		for (int i = se.getAstObjects().size() - 1; i >= 0; i--) {
			IASTObject astObj = se.getAstObjects().get(i);
			if (astObj instanceof PredicateExpression) {
				PredicateExpression pe = (PredicateExpression) astObj;
				IPredicateOperand1 v1 = pe.getValue_1();
				if (v1 instanceof DomainObjectMatch<?>) {
					DomainObjectMatch<?> dom = (DomainObjectMatch<?>) v1;
					if (APIAccess.getUnionExpression(dom) != null) {
						// is a union expression
						UnionExpression ue = APIAccess.getUnionExpression(dom);
						List<IASTObject> replacements = new ArrayList<IASTObject>();
						ConcatenateExpression ce;
						int idx = 0;
//						if (ue.getSources().size() > 1) {
//							ce = new ConcatenateExpression(Concatenator.BR_OPEN);
//							replacements.add(ce);
//						}
						List<IASTObject> astObjs = new ArrayList<IASTObject>();
						List<IASTObject> complete = this.intAccess.getQueryExecutor().getAstObjects();
						boolean add = false;
						for (IASTObject ao : complete) {
							if (ao == se)
								break;
							if (add)
								astObjs.add(ao);
							if (ao == ue.getLastOfUnionBase())
								add = true;
						}
						for (DomainObjectMatch<?> src : ue.getSources()) {
							List<IASTObject> xprs = this.intAccess.getQueryExecutor().getExpressionsFor(dom, astObjs);
							PredicateExpression cpe = pe.createCopy();
							cpe.setValue_1(src);
							if (idx > 0 && ue.isUnion()) {
								ce = new ConcatenateExpression(Concatenator.OR);
								replacements.add(ce);
							}
							addAdditionalXprs(xprs, replacements, src, dom);
							replacements.add(cpe);
							idx++;
						}
//						if (ue.getSources().size() > 1) {
//							ce = new ConcatenateExpression(Concatenator.BR_CLOSE);
//							replacements.add(ce);
//						}
						se.replaceAstObject(i, replacements);
					}
				}
			}
		}
	}
	
	private void addAdditionalXprs(List<IASTObject> xprs, List<IASTObject> addTo,
			DomainObjectMatch<?> src, DomainObjectMatch<?> old) {
		for (IASTObject astObj : xprs) {
			IASTObject toAdd = astObj;
			if (astObj instanceof PredicateExpression) {
				PredicateExpression pe = (PredicateExpression) astObj;
				PredicateExpression cpe = pe.createCopy();
				IPredicateOperand1 v1 = cpe.getValue_1();
				if (v1 instanceof JcValue) {
					JcValue v = APIAccess.getCloneOf(src, (JcValue) v1);
					cpe.setValue_1(v);
					cpe.setInCollectionExpression(true);
				}
				toAdd = cpe;
			}
			addTo.add(toAdd);
		}
	}
}
