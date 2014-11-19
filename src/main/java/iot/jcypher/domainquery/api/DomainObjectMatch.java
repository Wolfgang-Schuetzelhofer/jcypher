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

import iot.jcypher.domainquery.ast.DqObject;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.operators.OPERATOR;


public class DomainObjectMatch<T> {

	private Class<T> domainObjectType;
	private DqObject dqObject;
	
	public DomainObjectMatch(Class<T> targetType) {
		super();
		this.domainObjectType = targetType;
	}
	
	/**
	 * Access a string attribute
	 * @param name the attribute name
	 * @return a JcString
	 */
	public JcString stringAtttribute(String name) {
		JcString ret = ValueAccess.createJcString(name, null, getDqObject(),
				OPERATOR.PropertyContainer.PROPERTY_ACCESS);
		return ret;
	}
	
	private DqObject getDqObject() {
		if (this.dqObject == null)
			this.dqObject = new DqObject();
		return this.dqObject;
	}
}
