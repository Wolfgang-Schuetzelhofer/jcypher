/************************************************************************
 * Copyright (c) 2014-2016 IoT-Solutions e.U.
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

import org.neo4j.driver.v1.AuthToken;

import iot.jcypher.database.internal.PlannerStrategy;
import iot.jcypher.database.remote.BoltDBAccess;
import iot.jcypher.domainquery.internal.Settings;

/**
 * A Factory for creating accessors to Neo4j databases.
 *
 */
public class DBAccessFactory {

	/**
	 * Set the globally used planner strategy. This can be overridden on a per query basis.
	 * <br/>Setting <b>PlannerStragey.DEFAULT</b> leaves the decision to the NEO4J query engine.
	 * @param plannerStrategy
	 */
	public static void seGlobaltPlannerStrategy(PlannerStrategy plannerStrategy) {
		if (plannerStrategy == null)
			throw new RuntimeException("plannerStrategy must not be null.");
		Settings.plannerStrategy = plannerStrategy;
	}
	
	/**
	 * @return the globally used planner strategy.
	 */
	public static PlannerStrategy getGlobalPlannerStrategy() {
		return Settings.plannerStrategy;
	}
	
	/**
	 * create an IDBAccess (an accessor) for a specific database.
	 * @param dbType the type of database to access. Can be
	 * <br/>DBType.REMOTE or DBType.EMBEDDED or DBType.IN_MEMORY
	 * @param properties to configure the database connection.
	 * <br/>The appropriate database access class will pick the properties it needs.
	 * <br/>See also: DBProperties interface for required and optional properties.
	 * @return an instance of IDBAccess
	 */
	public static IDBAccess createDBAccess(DBType dbType, Properties properties) {
		return createDBAccess(dbType, properties, null, null, null);
	}
	
	/**
	 * create an IDBAccess (an accessor) for a specific database,
	 * supports authentication. 
	 * @param dbType the type of database to access. Can be
	 * <br/>DBType.REMOTE or DBType.EMBEDDED or DBType.IN_MEMORY
	 * @param properties to configure the database connection.
	 * <br/>The appropriate database access class will pick the properties it needs.
	 * <br/>See also: DBProperties interface for required and optional properties.
	 * @param userId
	 * @param password
	 * @return an instance of IDBAccess
	 */
	public static IDBAccess createDBAccess(DBType dbType, Properties properties,
			String userId, String password) {
		return createDBAccess(dbType, properties, userId, password, null);
	}
	
	/**
	 * create an IDBAccess (an accessor) for a specific database,
	 * supports authentication. 
	 * @param dbType the type of database to access. Can be
	 * <br/>DBType.REMOTE or DBType.EMBEDDED or DBType.IN_MEMORY
	 * @param properties to configure the database connection.
	 * <br/>The appropriate database access class will pick the properties it needs.
	 * <br/>See also: DBProperties interface for required and optional properties.
	 * @param authToken
	 * @return an instance of IDBAccess
	 */
	public static IDBAccess createDBAccess(DBType dbType, Properties properties,
			AuthToken authToken) {
		return createDBAccess(dbType, properties, null, null, authToken);
	}
	
	@SuppressWarnings("unchecked")
	private static IDBAccess createDBAccess(DBType dbType, Properties properties,
			String userId, String password, AuthToken authToken) {
		Class<? extends IDBAccess> dbAccessClass = null;
		IDBAccess dbAccess = null;
		try {
			switch(dbType) {
				case REMOTE:
					if (properties == null)
						throw new RuntimeException("missing properties in database configuration");
					boolean bolt = BoltDBAccess.isBoltProtocol(properties.getProperty(DBProperties.SERVER_ROOT_URI));
					if (!DBVersion.Neo4j_Version.startsWith("2") && bolt)
						dbAccessClass =
							(Class<? extends IDBAccess>) Class.forName("iot.jcypher.database.remote.BoltDBAccess");
					else
						dbAccessClass =
							(Class<? extends IDBAccess>) Class.forName("iot.jcypher.database.remote.RemoteDBAccess");
					break;
				case EMBEDDED:
					if (properties == null)
						throw new RuntimeException("missing properties in database configuration");
					dbAccessClass =
							(Class<? extends IDBAccess>) Class.forName("iot.jcypher.database.embedded.EmbeddedDBAccess");
					break;
				case IN_MEMORY:
					dbAccessClass =
							(Class<? extends IDBAccess>) Class.forName("iot.jcypher.database.embedded.InMemoryDBAccess");
					break;
				default:
					throw new UnsupportedOperationException("Unexpected DBType: " + dbType);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
		if (dbAccessClass != null) {
			try {
				Method init = dbAccessClass.getMethod("initialize", new Class[] {Properties.class});
				dbAccess = dbAccessClass.newInstance();
				init.invoke(dbAccess, new Object[] {properties});
				if (dbType == DBType.REMOTE) { // authentication only applies to remote db access
					if (userId != null && password != null) {
						Method setAuth = dbAccessClass.getDeclaredMethod("setAuth", new Class[] {String.class, String.class});
						setAuth.invoke(dbAccess, new Object[] {userId, password});
					} else if (authToken != null) {
						Method setAuth = dbAccessClass.getDeclaredMethod("setAuth", new Class[] {AuthToken.class});
						setAuth.invoke(dbAccess, new Object[] {authToken});
					}
				}
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return dbAccess;
	}
}
