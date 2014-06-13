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

package iot.jcypher.database;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * A Factory for creating accessors to Neo4j databases.
 *
 */
public class DBAccessFactory {

	/**
	 * create an IDBAccess (an accessor) for a specific database.
	 * @param dbType the type of database to access. Can be
	 * <br/>DBType.REMOTE or DBType.EMBEDDED or DBType.IN_MEMORY
	 * @param properties to configure the database connection.
	 * <br/>The appropriate database access class will pick the properties it needs.
	 * <br/>See also: DBProperties interface for required and optional properties.
	 * @return an instance of IDBAccess
	 */
	@SuppressWarnings("unchecked")
	public static IDBAccess createDBAccess(DBType dbType, Properties properties) {
		Class<? extends IDBAccess> dbAccessClass = null;
		IDBAccess dbAccess = null;
		try {
			if (dbType == DBType.REMOTE) {
				dbAccessClass =
						(Class<? extends IDBAccess>) Class.forName("iot.jcypher.database.remote.RemoteDBAccess");
			} else if (dbType == DBType.EMBEDDED) {
				dbAccessClass =
						(Class<? extends IDBAccess>) Class.forName("iot.jcypher.database.embedded.EmbeddedDBAccess");
			} else if (dbType == DBType.IN_MEMORY) {
				dbAccessClass =
						(Class<? extends IDBAccess>) Class.forName("iot.jcypher.database.embedded.InMemoryDBAccess");
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
		if (dbAccessClass != null) {
			try {
				Method init = dbAccessClass.getDeclaredMethod("initialize", new Class[] {Properties.class});
				dbAccess = dbAccessClass.newInstance();
				init.invoke(dbAccess, new Object[] {properties});
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return dbAccess;
	}
}
