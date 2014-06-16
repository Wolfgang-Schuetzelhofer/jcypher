/************************************************************************
 * accessing a Neo4j database
 ************************************************************************/

package iot.jcypher.snippets;

import iot.jcypher.JcQuery;
import iot.jcypher.JcQueryResult;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;

import java.util.Properties;

import javax.json.JsonObject;


/**
 * accessing a Neo4j database
 *
 */
public class Snippet_04 {

	/**
	 * execute a JCypher query against a Neo4j database
	 * @param query a JcQuery
	 */
	public static void executeQuery(JcQuery query) {
		/** initialize access to a Neo4j database.
		      You access Neo4j databases in a uniform way, no matter if you access
		      a remote database, an embedded database, or an in-memory database,
		      the only difference is in the initialization part**/
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		// have a look at the DBProperties interface
		// the appropriate database access class will pick the properties it needs
		Properties props = new Properties();
		// for remote db access:
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		// for embedded db access:
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		/** Note: An IDBAccess instance needs only to be created once for an application instance.
		     It is reused during the whole lifecycle og an application insance */
		
		// Practically you would use only one of the following three db access creation variants
		/** connect to an in memory database (no properties are required) */
		IDBAccess i_dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
		
		/** connect to remote database via REST (SERVER_ROOT_URI property is needed) */
		IDBAccess r_dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
		
		/** connect to an embedded database (DATABASE_DIR property is needed) */
		IDBAccess e_dbAccess = DBAccessFactory.createDBAccess(DBType.EMBEDDED, props);
		
		/** execute the query against a Neo4j database */
		JcQueryResult result = i_dbAccess.execute(query);
		if (result.hasErrors()) {
			// do something in case of errors
		}
		
		/** You can retrieve a JsonObject containing the query result.
		      Note: With release 1.0.0 you will retrieve a more adequate result model,
		      which will provide for comfortable and easy navigation of the query result.
		 */
		JsonObject jsonResult = result.getJsonResult();
		
		/** Close the Neo4j database connection, releasing all resources.
		     This usually is done once at the lifecycle end of the application instance.
		     If you don't do it, a close is automatically performed when the jvm terminates*/
		// Practically you would only have one IDBAccess instance and hence perform only one call to close()
		i_dbAccess.close();
		r_dbAccess.close();
		e_dbAccess.close();
	}

}
