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

package iot.jcypher.transaction.internal;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.transaction.ITransaction;

public abstract class AbstractTransaction implements ITransaction {
	
	protected static final String ERR_CLOSED = "transaction has already been closed"; 
	protected static final String ERR_THREAD = "close() must be called from within the same thread which created this transaction";

	private IDBAccess dbAccess;
	private boolean closed;
	protected boolean failed;
	private IIntDomainAccess intDomainAccess;
	private boolean domainInfoChanged;
	private boolean noInfoNodeId;

	public AbstractTransaction(IDBAccess dbAccess) {
		super();
		this.dbAccess = dbAccess;
		this.failed = false;
		this.closed = false;
		this.domainInfoChanged = false;
		this.noInfoNodeId = false;
	}
	
	@Override
	public void failure() {
		this.failed = true;
	}
	
	protected IDBAccess getDBAccess() {
		return dbAccess;
	}
	
	/**
	 * answer true if the method is called from the same thread
	 * this transaction was created in. 
	 * @return
	 */
	protected boolean isMyThread() {
		return getDBAccess().getTX() == this;
	}
	
	protected void setClosed() {
		this.closed = true;
		if (this.intDomainAccess != null) {
			this.intDomainAccess.getInternalDomainAccess().transactionClosed(this.failed,
					this.domainInfoChanged, this.noInfoNodeId);
		}
		this.intDomainAccess = null;
		this.dbAccess = null;
	}

	@Override
	public boolean isClosed() {
		return this.closed;
	}

	public void setIntDomainAccess(IIntDomainAccess intDomainAccess) {
		this.intDomainAccess = intDomainAccess;
	}
	
	public void setDomainInfoChanged() {
		this.domainInfoChanged = true;
	}
	
	public void setNoInfoNodeId() {
		this.noInfoNodeId = true;
	}
}
