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

import iot.jcypher.domainquery.ast.CollectExpression;
import iot.jcypher.domainquery.internal.QueryRecorder;

public class Collect extends APIObject {

	public Collect(CollectExpression ce) {
		this.astObject = ce;
	}
	
	/**
	 * Return the collect result as a DomainObjectMatch of specified typ
	 * (it represents a set of objects of that type).
	 * @param domainObjectType
	 * @return a DomainObjectMatch
	 */
	public <T> DomainObjectMatch<T> AS(Class<T> domainObjectType) {
		CollectExpression ce = (CollectExpression)this.astObject;
		DomainObjectMatch<T> ret =APIAccess.createDomainObjectMatch(domainObjectType,
				ce.getQueryExecutor().getDomainObjectMatches().size(),
				ce.getQueryExecutor().getMappingInfo());
		ce.setEnd(ret);
		ce.getQueryExecutor().getDomainObjectMatches().add(ret);
		QueryRecorder.recordAssignment(this, "AS", ret, QueryRecorder.literal(domainObjectType.getName()));
		return ret;
	}
}
