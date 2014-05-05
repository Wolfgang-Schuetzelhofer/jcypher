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

package iot.neo.jcypher.ast.pattern;

import iot.neo.jcypher.values.JcPath;

public class PatternPath {

	private JcPath jcPath;
	private PathFunction pathFunction;
	
	public PatternPath(JcPath jcPath, PathFunction pathFunction) {
		super();
		this.jcPath = jcPath;
		this.pathFunction = pathFunction;
	}

	public JcPath getJcPath() {
		return jcPath;
	}

	public PathFunction getPathFunction() {
		return pathFunction;
	}

	/******************************************/
	public enum PathFunction {
		PATH, SHORTEST_PATH, ALL_SHORTEST_PATHS
	}
}
