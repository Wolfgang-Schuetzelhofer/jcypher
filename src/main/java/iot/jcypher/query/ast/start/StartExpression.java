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

package iot.jcypher.query.ast.start;

import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.values.JcElement;

public class StartExpression extends ASTNode {
	private JcElement jcElement;
	private boolean all = false;
	private IndexOrId indexOrId;
	private PropertyOrQuery propertyOrQuery;

	public StartExpression(JcElement elem) {
		super();
		this.jcElement = elem;
	}
	
	public void setIndexOrId(IndexOrId iod) {
		this.indexOrId = iod;
	}
	
	public IndexOrId getIndexOrId() {
		return this.indexOrId;
	}
	
	public void setPropertyOrQuery(PropertyOrQuery poq) {
		this.propertyOrQuery = poq;
	}
	
	public PropertyOrQuery getPropertyOrQuery() {
		return this.propertyOrQuery;
	}
	
	public JcElement getJcElement() {
		return jcElement;
	}

	public boolean isAll() {
		return all;
	}

	public void setAll() {
		this.all = true;
	}
}
