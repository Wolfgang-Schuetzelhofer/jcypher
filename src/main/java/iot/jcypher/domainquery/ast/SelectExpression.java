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

package iot.jcypher.domainquery.ast;

import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQuery.IntAccess;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.IASTObjectsContainer;
import iot.jcypher.domainquery.internal.QueryExecutor;

import java.util.ArrayList;
import java.util.List;

public class SelectExpression<T> implements IASTObject, IASTObjectsContainer {
	
	private IntAccess domainQueryIntAccess;
	private DomainObjectMatch<T> start;
	private DomainObjectMatch<T> end;
	private List<IASTObject> astObjects;
	private List<DomainObjectMatch<?>> traversalResults;
	private boolean reject;

	public SelectExpression(DomainObjectMatch<T> start, IntAccess domainQueryIntAccess) {
		this(start, domainQueryIntAccess, false);
	}
	
	public SelectExpression(DomainObjectMatch<T> start, IntAccess domainQueryIntAccess,
			boolean reject) {
		super();
		this.start = start;
		this.astObjects = new ArrayList<IASTObject>();
		this.domainQueryIntAccess = domainQueryIntAccess;
		this.reject = reject;
	}
	
	public List<IASTObject> getAstObjects() {
		return astObjects;
	}
	
	@Override
	public void addAstObject(IASTObject astObj) {
		if (astObj instanceof PredicateExpression) {
			// this must represent either the source set or a set derived from the source set by traversal
			DomainObjectMatch<?> dom = ((PredicateExpression)astObj).getStartDOM();
			// must be a derived set
			if (dom != this.start) {
				DomainObjectMatch<?> src = APIAccess.getTraversalSource(dom);
				if (src != this.start)
					throw new RuntimeException(
							"Predicate expressions within a collection expression must express constraints on " +
							"either the source set or on a set directly derived from the source set by traversal");
				else {
					if (this.traversalResults == null)
						this.traversalResults = new ArrayList<DomainObjectMatch<?>>();
					if (!this.traversalResults.contains(dom))
						this.traversalResults.add(dom);
				}
			}
		}
		this.astObjects.add(astObj);
	}
	
	public void setEnd(DomainObjectMatch<T> end) {
		this.end = end;
		if (this.traversalResults != null) {
			for (DomainObjectMatch<?> dom : this.traversalResults) {
				// add the collection owner to a traversal owner
				APIAccess.addCollectExpressionOwner(dom, this.end);
			}
		}
	}
	
	public DomainObjectMatch<T> getEnd() {
		return end;
	}

	public DomainObjectMatch<T> getStart() {
		return start;
	}
	
	public QueryExecutor getQueryExecutor() {
		return this.domainQueryIntAccess.getQueryExecutor();
	}
	
	public DomainQuery getDomainQuery() {
		return this.domainQueryIntAccess.getDomainQuery();
	}
	
	public List<DomainObjectMatch<?>> getTraversalResults() {
		return traversalResults;
	}

	public boolean isReject() {
		return reject;
	}

	public void resetAstObjectsContainer() {
		this.domainQueryIntAccess.resetAstObjectsContainer();
	}
}
