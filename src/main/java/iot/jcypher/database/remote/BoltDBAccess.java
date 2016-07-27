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
import java.util.Properties;

import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.internal.IDBAccessInit;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.result.JcError;
import iot.jcypher.transaction.ITransaction;

public class BoltDBAccess implements IDBAccessInit {

	private Properties properties;
	private String userId;
	private String passWord;
	
	@Override
	public JcQueryResult execute(JcQuery query) {
		List<JcQuery> qList = new ArrayList<JcQuery>();
		qList.add(query);
		List<JcQueryResult> qrList = execute(qList);
		return qrList.get(0);
	}

	@Override
	public List<JcQueryResult> execute(List<JcQuery> queries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<JcError> clearDatabase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITransaction beginTX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITransaction getTX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDatabaseEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DBType getDBType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(Properties properties) {
		this.properties = properties;
		if (this.properties == null)
			throw new RuntimeException("missing properties in database configuration");
		if (this.properties.getProperty(DBProperties.SERVER_ROOT_URI) == null)
			throw new RuntimeException("missing property: '" +
					DBProperties.SERVER_ROOT_URI + "' in database configuration");
		
	}

	@Override
	public void setAuth(String userId, String password) {
		this.userId = userId;
		this.passWord = password;
	}

}
