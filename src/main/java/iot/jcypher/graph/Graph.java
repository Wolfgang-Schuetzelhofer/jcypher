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

import java.util.Collections;
import java.util.List;

import iot.jcypher.concurrency.Locking;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.util.ResultHandler;

public class Graph {

	private ResultHandler resultHandler;
	private SyncState syncState;

	Graph(ResultHandler resultHandler) {
		super();
		this.resultHandler = resultHandler;
	}
	
	/**
	 * create an empty graph
	 * @param dbAccess the database on which to perform updates of the graph
	 * @return the empty graph model
	 */
	public static Graph create(IDBAccess dbAccess) {
		ResultHandler rh = new ResultHandler(null, -1, dbAccess);
		Graph ret = rh.getGraph();
		ret.setSyncState(SyncState.NEW);
		return ret;
	}
	
	/**
	 * Set the locking strategy (e.g. Locking.OPTIMISTIC, ...).
	 * @param locking
	 * @return the Graph (self) to allow fluent method concatenation.
	 */
	public Graph setLockingStrategy(Locking locking) {
		this.resultHandler.setLockingStrategy(locking);
		return this;
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
	
	/**
	 * Update the underlying database with changes made on the graph
	 * @return a list of errors, which is empty if no errors occurred
	 */
	public List<JcError> store() {
		if (isModified())
			return this.resultHandler.store();
		return Collections.emptyList();
	}
	
	/**
	 * create a list of queries which would apply the changes of the graph to the
	 * underlying database. You can use it to have a look which queries will be executed
	 * by a store operation.
	 * @return a list of JcQueries
	 */
	public List<JcQuery> createUpdateQueries() {
		if (isModified())
			return this.resultHandler.createUpdateQueries();
		return null;
	}

	SyncState getSyncState() {
		return syncState;
	}

	void setSyncState(SyncState syncState) {
		this.syncState = syncState;
	}
	
	void setDBAccess(IDBAccess dbAccess) {
		this.resultHandler.setDbAccess(dbAccess);
	}
}
