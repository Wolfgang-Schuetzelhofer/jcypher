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

package iot.jcypher.transaction;

import iot.jcypher.query.result.JcError;

import java.util.List;

public interface ITransaction {

	/**
     * Marks this transaction as failed, which means that it will
     * unconditionally be rolled back when close() is called.
     */
	public void failure();
	
	/**
	 * Close the transaction. If it was marked as failed, a rollback is performed,
	 * else the transaction is committed.
	 * @return a possibly empty list of errors.
	 */
	public List<JcError> close();
	
	/**
	 * answer true if this transaction has been closed
	 * @return
	 */
	public boolean isClosed();
}
