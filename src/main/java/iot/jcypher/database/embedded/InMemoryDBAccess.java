package iot.jcypher.database.embedded;

import iot.jcypher.database.DBProperties;

import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

public class InMemoryDBAccess extends AbstractEmbeddedDBAccess {

	@Override
	public void initialize(Properties properties) {
		this.properties = properties;
	}

	@Override
	protected GraphDatabaseService createGraphDB() {
		GraphDatabaseBuilder builder = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder();
		if (this.properties != null) {
			if (this.properties
					.getProperty(DBProperties.NODESTORE_MAPPED_MAMORY_SIZE) != null)
				builder.setConfig(
						GraphDatabaseSettings.nodestore_mapped_memory_size,
						DBProperties.NODESTORE_MAPPED_MAMORY_SIZE);
			if (this.properties.getProperty(DBProperties.STRING_BLOCK_SIZE) != null)
				builder.setConfig(GraphDatabaseSettings.string_block_size,
						DBProperties.ARRAY_BLOCK_SIZE);
			if (this.properties.getProperty(DBProperties.STRING_BLOCK_SIZE) != null)
				builder.setConfig(GraphDatabaseSettings.array_block_size,
						DBProperties.ARRAY_BLOCK_SIZE);
		}
		return builder.newGraphDatabase();
	}
}
