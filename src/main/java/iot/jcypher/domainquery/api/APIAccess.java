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

package iot.jcypher.domainquery.api;

import iot.jcypher.domainquery.ast.PredicateExpression;
import iot.jcypher.domainquery.internal.QueryExecutor.Attribute2PropertyNameConverter;
import iot.jcypher.query.values.JcNode;

public class APIAccess {

	public static BooleanOperation createBooleanOperation(PredicateExpression pe) {
		return new BooleanOperation(pe);
	}
	
	public static <T> DomainObjectMatch<T> createDomainObjectMatch(Class<T> domainObjectType,
			int num, Attribute2PropertyNameConverter propNameConverter) {
		return new DomainObjectMatch<T>(domainObjectType, num, propNameConverter);
	}
	
	public static JcNode getNode(DomainObjectMatch<?> dom) {
		return dom.getNode();
	}
}
