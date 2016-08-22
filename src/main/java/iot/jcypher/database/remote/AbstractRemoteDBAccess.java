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

import iot.jcypher.database.DBType;
import iot.jcypher.database.internal.DBUtil;
import iot.jcypher.database.internal.IDBAccessInit;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.result.JcError;

public abstract class AbstractRemoteDBAccess implements IDBAccessInit {

	protected Thread shutdownHook;
	
	@Override
	public JcQueryResult execute(JcQuery query) {
		List<JcQuery> qList = new ArrayList<JcQuery>();
		qList.add(query);
		List<JcQueryResult> qrList = execute(qList);
		return qrList.get(0);
	}
	
	@Override
	public List<JcError> clearDatabase() {
		return DBUtil.clearDatabase(this);
	}
	
	@Override
	public boolean isDatabaseEmpty() {
		return DBUtil.isDatabaseEmpty(this);
	}
	
	@Override
	public DBType getDBType() {
		return DBType.REMOTE;
	}
	
	@Override
	public void close() {
		if (this.shutdownHook != null) {
			Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
			this.shutdownHook = null;
		}
	}
}
