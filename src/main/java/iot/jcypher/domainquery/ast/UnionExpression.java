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

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;

public class UnionExpression implements IASTObject {

	private DomainObjectMatch<?> result;
	private List<DomainObjectMatch<?>> sources;
	private boolean union;
	// the last expression which defines the basic union
	private IASTObject lastOfUnionBase;
	
	public UnionExpression(boolean union) {
		super();
		this.union = union;
		this.sources = new ArrayList<DomainObjectMatch<?>>();
	}
	public DomainObjectMatch<?> getResult() {
		return result;
	}
	public void setResult(DomainObjectMatch<?> result) {
		this.result = result;
	}
	public List<DomainObjectMatch<?>> getSources() {
		return sources;
	}
	public DomainObjectMatch<?> getCommonTraversalSource() {
		DomainObjectMatch<?> travSource = null;
		int idx = 0;
		for (DomainObjectMatch<?> dom : this.sources) {
			DomainObjectMatch<?> ts = APIAccess.getTraversalSource(dom);
			if (idx == 0) {
				travSource = ts;
			} else {
				if (ts != travSource)
					travSource = null;
			}
		}
		return travSource;
	}
	public boolean isUnion() {
		return union;
	}
	public IASTObject getLastOfUnionBase() {
		return lastOfUnionBase;
	}
	public void setLastOfUnionBase(IASTObject lastOfUnionBase) {
		this.lastOfUnionBase = lastOfUnionBase;
	}
	
	public boolean isLastOfSources(DomainObjectMatch<?> dom) {
		int sz = this.sources.size();
		return sz > 0 && dom.equals(this.sources.get(sz - 1));
	}
}
