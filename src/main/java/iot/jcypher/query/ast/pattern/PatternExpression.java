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

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.query.ast.ASTNode;

public class PatternExpression extends ASTNode {

	private PatternPath path;
	private List<PatternElement> patternElements = new ArrayList<PatternElement>();
	
	public PatternElement getLastElement() {
		if (this.patternElements.size() > 0)
			return this.patternElements.get(this.patternElements.size() -1);
		return null;
	}
	
	public void addElement(PatternElement element) {
		this.patternElements.add(element);
	}

	public List<PatternElement> getElements() {
		return patternElements;
	}

	public PatternPath getPath() {
		return path;
	}

	public void setPath(PatternPath path) {
		this.path = path;
	}
}
