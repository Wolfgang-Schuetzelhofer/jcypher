package iot.jcypher.database.embedded;

import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;

public class InMemoryDBAccess extends AbstractEmbeddedDBAccess {

	@Override
	public void initialize(Properties properties) {
		this.properties = properties;
	}

	@Override
	protected GraphDatabaseService createGraphDB() {
		// TODO Auto-generated method stub
		return null;
	}
}
