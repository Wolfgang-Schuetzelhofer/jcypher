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

package iot.jcypher.database.embedded;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Transaction;

import iot.jcypher.database.internal.DBUtil;
import iot.jcypher.query.result.JcError;
import iot.jcypher.transaction.internal.AbstractTransaction;

public class ETransactionImpl extends AbstractTransaction {
	
	private Transaction transaction;

	/**
	 * @param dbAccess
	 */
	public ETransactionImpl(AbstractEmbeddedDBAccess dbAccess) {
		super(dbAccess);
	}
	
	@Override
	public List<JcError> close() {
		List<JcError> errors;
		if (isClosed())
			throw new RuntimeException(ERR_CLOSED);
		if (!isMyThread())
			throw new RuntimeException(ERR_THREAD);
		
		setClosed();
		AbstractEmbeddedDBAccess edba = getEDBAccess();
		edba.removeTx();
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
		
		return errors;
	}
	
	public Transaction getTransaction() {
		if (this.transaction == null) {
			AbstractEmbeddedDBAccess edba = getEDBAccess();
			this.transaction = edba.getGraphDB().beginTx();
		}
		return this.transaction;
	}
	
	private AbstractEmbeddedDBAccess getEDBAccess() {
		return (AbstractEmbeddedDBAccess)getDBAccess();
	}
}
