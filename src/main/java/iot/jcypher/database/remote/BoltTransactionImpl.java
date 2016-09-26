/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
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

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Transaction;

import iot.jcypher.database.embedded.AbstractEmbeddedDBAccess;
import iot.jcypher.database.internal.DBUtil;
import iot.jcypher.query.result.JcError;
import iot.jcypher.transaction.internal.AbstractTransaction;

public class BoltTransactionImpl extends AbstractTransaction {

	private Transaction transaction;
	
	public BoltTransactionImpl(BoltDBAccess dbAccess) {
		super(dbAccess);
	}

	@Override
	public List<JcError> close() {
		List<JcError> errors;
		if (isClosed())
			throw new RuntimeException(ERR_CLOSED);
		if (!isMyThread())
			throw new RuntimeException(ERR_THREAD);
		
		BoltDBAccess bdba = getBoltDBAccess();
		bdba.removeTx();
		if (this.transaction != null) {
			Transaction tx = getTransaction();
			if (failed)
				tx.failure();
			else
				tx.success();
			
			Throwable dbException = null;
			try {
				tx.close();
			} catch(Throwable e) {
				dbException = e;
			}
			errors = DBUtil.buildErrorList(null, dbException);
		} else 
			errors = new ArrayList<JcError>();
		
		if (errors.size() > 0)
			failure();
		setClosed();
		
		return errors;
	}

	public Transaction getTransaction() {
		if (this.transaction == null) {
			BoltDBAccess bdba = getBoltDBAccess();
			this.transaction = bdba.getSession().beginTransaction();
		}
		return this.transaction;
	}

	private BoltDBAccess getBoltDBAccess() {
		return (BoltDBAccess) getDBAccess();
	}
}
