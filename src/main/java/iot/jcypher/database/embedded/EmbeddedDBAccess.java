package iot.jcypher.database.embedded;

import iot.jcypher.database.DBProperties;

import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class EmbeddedDBAccess extends AbstractEmbeddedDBAccess {

	@Override
	public void initialize(Properties properties) {
		this.properties = properties;
		if (this.properties == null)
			throw new RuntimeException(
					"missing properties in database configuration");
		if (this.properties.getProperty(DBProperties.DATABASE_DIR) == null)
			throw new RuntimeException("missing property: '"
					+ DBProperties.DATABASE_DIR + "' in database configuration");
	}

	@Override
	protected GraphDatabaseService createGraphDB() {
		GraphDatabaseBuilder builder = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(this.properties
						.getProperty(DBProperties.DATABASE_DIR));
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
		return builder.newGraphDatabase();
	}
}
