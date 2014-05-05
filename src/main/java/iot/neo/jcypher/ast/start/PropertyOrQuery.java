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

package iot.neo.jcypher.ast.start;

import iot.neo.jcypher.ast.ASTNode;

public class PropertyOrQuery extends ASTNode {

	private String propertyName;
	private Object propertyValue;
	private String luceneQuery;
	
	public PropertyOrQuery(String propertyName, String query) {
		super();
		this.propertyName = propertyName;
		this.luceneQuery = query;
	}

	public Object getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(Object propertyValue) {
		this.propertyValue = propertyValue;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getLuceneQuery() {
		return luceneQuery;
	}
}
