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
	
	public UnionExpression(boolean union) {
		super();
		this.union = union;
	}
	public DomainObjectMatch<?> getResult() {
		return result;
	}
	public void setResult(DomainObjectMatch<?> result) {
		this.result = result;
	}
	public List<DomainObjectMatch<?>> getSources() {
		if (sources == null)
			sources = new ArrayList<DomainObjectMatch<?>>();
		return sources;
	}
	public DomainObjectMatch<?> getCommonTraversalSource() {
		return APIAccess.getTraversalSource(result);
	}
	
}
