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

import iot.jcypher.domainquery.internal.QueryExecutor.Attribute2PropertyNameConverter;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcString;


public class DomainObjectMatch<T> {

	private static final String nodePrefix = "n_";
	
	private Class<T> domainObjectType;
	private JcNode node;
	private Attribute2PropertyNameConverter propNameConverter;
	
	DomainObjectMatch(Class<T> targetType, int num,
			Attribute2PropertyNameConverter propNameConverter) {
		super();
		this.domainObjectType = targetType;
		this.node = new JcNode(nodePrefix.concat(String.valueOf(num)));
		this.propNameConverter = propNameConverter;
	}
	
	/**
	 * Access a string attribute
	 * @param name the attribute name
	 * @return a JcString
	 */
	public JcString stringAtttribute(String name) {
		String propName = this.propNameConverter.convert(name);
		JcString ret = this.node.stringProperty(propName);
		return ret;
	}

	Class<T> getDomainObjectType() {
		return domainObjectType;
	}

	JcNode getNode() {
		return node;
	}
	
}
