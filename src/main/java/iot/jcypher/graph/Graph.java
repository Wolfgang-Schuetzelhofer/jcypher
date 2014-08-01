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

public class Graph {

	private ResultHandler resultHandler;
	private SyncState syncState;

	Graph(ResultHandler resultHandler) {
		super();
		this.resultHandler = resultHandler;
	}
	
	/**
	 * create an empty graph
	 * @return
	 */
	public static Graph create() {
		ResultHandler rh = new ResultHandler(null, -1);
		Graph ret = rh.getGraph();
		ret.setSyncState(SyncState.NEW);
		return ret;
	}
	
	/**
	 * create a node in the graph
	 * @return a GrNode
	 */
	public GrNode createNode() {
		return this.resultHandler.getLocalElements().createNode();
	}
	
	/**
	 * create a relation in the graph
	 * @param type
	 * @param startNode
	 * @param endNode
	 * @return a GrRelation
	 */
	public GrRelation createRelation(String type, GrNode startNode, GrNode endNode) {
		return this.resultHandler.getLocalElements().createRelation(type, startNode, endNode);
	}
	
	/**
	 * @return true, if the graph contains new or modified elements,
	 * or if elements of the graph were removed.
	 */
	public boolean isModified() {
		if (this.syncState == SyncState.NEW)
			return !this.resultHandler.getLocalElements().isEmpty();
		return this.syncState != SyncState.SYNC;
	}
	
	public void store() {
		if (isModified())
			this.resultHandler.store();
	}

	SyncState getSyncState() {
		return syncState;
	}

	void setSyncState(SyncState syncState) {
		this.syncState = syncState;
	}
}
