/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.database.remote;

import iot.jcypher.database.internal.DBUtil;
import iot.jcypher.query.result.JcError;
import iot.jcypher.transaction.internal.AbstractTransaction;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RTransactionImpl extends AbstractTransaction {

	private static final String transactionalURLPostfix = "db/data/transaction";
	private static final String txCommit = "/commit";
	private static final String emptyJSON = "{\"statements\" : [ ]}";
	private Invocation.Builder invocationBuilder_open;
	private Invocation.Builder invocationBuilder_next;
	private String txLocation;
	
	/**
	 * @param dbAccess
	 */
	public RTransactionImpl(RemoteDBAccess dbAccess) {
		super(dbAccess);
	}
	
	@Override
	public List<JcError> close() {
		if (isClosed())
			throw new RuntimeException("transaction has already been closed");
		if (!isMyThread())
			throw new RuntimeException("close() must be called from within the same thread which created this transaction");
		
		RemoteDBAccess rdba = getRDBAccess();
		rdba.removeTx();
		Builder iBuilder;
		if (failed) {
			iBuilder = createNextInvocationBuilder();
		} else {
			WebTarget serverRootTarget = rdba.getRestClient().target(rdba.getServerRootUri());
			WebTarget transactionalTarget = serverRootTarget.path(this.txLocation.concat(txCommit));
			iBuilder = transactionalTarget.request(MediaType.APPLICATION_JSON_TYPE);
			if (rdba.getAuth() != null)
				iBuilder = iBuilder.header(RemoteDBAccess.authHeader, rdba.getAuth());
		}
		
		Response response = null;
		Throwable exception = null;
		try {
			if (failed)
				response = iBuilder.delete();
			else
				response = iBuilder.post(Entity.entity(emptyJSON, MediaType.APPLICATION_JSON_TYPE));
		} catch(Throwable e) {
			exception = e;
		}
		
		return DBUtil.buildErrorList(response, exception);
	}
	
	private RemoteDBAccess getRDBAccess() {
		return (RemoteDBAccess)getDBAccess();
	}
	
	public synchronized Invocation.Builder getInvocationBuilder() {
		Invocation.Builder ret;
		RemoteDBAccess rdba = getRDBAccess();
		if (this.invocationBuilder_open == null) {
			WebTarget serverRootTarget = rdba.getRestClient().target(rdba.getServerRootUri());
			WebTarget transactionalTarget = serverRootTarget.path(transactionalURLPostfix);
			this.invocationBuilder_open = transactionalTarget.request(MediaType.APPLICATION_JSON_TYPE);
			if (rdba.getAuth() != null)
				this.invocationBuilder_open = this.invocationBuilder_open.header(RemoteDBAccess.authHeader, rdba.getAuth());
			ret = this.invocationBuilder_open;
		} else if (this.invocationBuilder_next == null) {
			this.invocationBuilder_next = createNextInvocationBuilder();
			ret = this.invocationBuilder_next;
		} else {
			ret = this.invocationBuilder_next;
		}
		return ret;
	}

	public void setTxLocation(String txLoc) {
		if (this.txLocation == null) {
			int idx = txLoc.indexOf(transactionalURLPostfix);
			this.txLocation = txLoc.substring(idx);
		}
	}
	
	private Invocation.Builder createNextInvocationBuilder() {
		Invocation.Builder ret;
		RemoteDBAccess rdba = getRDBAccess();
		WebTarget serverRootTarget = rdba.getRestClient().target(rdba.getServerRootUri());
		WebTarget transactionalTarget = serverRootTarget.path(this.txLocation);
		ret = transactionalTarget.request(MediaType.APPLICATION_JSON_TYPE);
		if (rdba.getAuth() != null)
			ret = ret.header(RemoteDBAccess.authHeader, rdba.getAuth());
		return ret;
	}
	
}
