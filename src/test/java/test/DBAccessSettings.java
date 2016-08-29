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

package test;

import java.util.Properties;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;

public class DBAccessSettings {

	private static Properties props = new Properties();
	private static DBType dbType = DBType.IN_MEMORY;
	
	static {
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "bolt://localhost:7687");
//		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/02");
		
		// dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
		// dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "jcypher");
	}
	
	public static IDBAccess createDBAccess() {
		return DBAccessFactory.createDBAccess(dbType, props);
	}

}
