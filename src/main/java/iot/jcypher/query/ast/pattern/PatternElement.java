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

package iot.jcypher.query.ast.pattern;

import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.values.JcElement;

import java.util.ArrayList;
import java.util.List;

public abstract class PatternElement extends ASTNode {

	private JcElement jcElement;
	private List<PatternProperty> properties;
	
	public PatternElement(JcElement jcElement) {
		super();
		this.jcElement = jcElement;
	}
	
	public JcElement getJcElement() {
		return jcElement;
	}

	public List<PatternProperty> getProperties() {
		if (this.properties == null)
			this.properties = new ArrayList<PatternProperty>();
		return this.properties;
	}
	
	public PatternProperty getLastProperty() {
		int sz = getProperties().size();
		if (sz > 0)
			return getProperties().get(sz - 1);
		return null;
	}
	
	public boolean hasProperties() {
		return this.properties != null && this.properties.size() > 0;
	}
}
