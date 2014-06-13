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

package iot.jcypher.query.api.pattern;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.ast.pattern.PatternExpression;
import iot.jcypher.query.ast.pattern.PatternProperty;

public abstract class Element<T> extends APIObject implements IElement {

	Element(PatternExpression px) {
		super();
		this.astNode = px;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify a property (of a node or relation) to be matched or created in a pattern</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. MATCH.node(n).<b>property("name")</b></i></div>
	 * <br/>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Property<T> property(String name) {
		PatternExpression px = (PatternExpression)this.astNode;
		PatternProperty pp = new PatternProperty(name);
		px.getLastElement().getProperties().add(pp);
		Property<T> prop = new Property(px, this);
		return prop;
	}
}
