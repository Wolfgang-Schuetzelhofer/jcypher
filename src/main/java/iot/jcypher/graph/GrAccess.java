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

package iot.jcypher.graph;

import iot.jcypher.result.util.ResultHandler;

public class GrAccess {

	public static GrNode createNode(ResultHandler rh, long id, String name) {
		return new GrNode(rh, id, name);
	}
	
	public static GrRelation createRelation(ResultHandler rh, long id, String name,
			long startNodeId, long endNodeId) {
		return new GrRelation(rh, id, name, startNodeId, endNodeId);
	}
	
	public static Graph createGraph(ResultHandler resultHandler) {
		return new Graph(resultHandler);
	}
}
