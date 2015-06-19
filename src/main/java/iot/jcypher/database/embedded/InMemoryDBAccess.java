/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

package iot.jcypher.database.embedded;

import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;

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
	public DBType getDBType() {
		return DBType.IN_MEMORY;
	}
	
	@Override
	protected GraphDatabaseService createGraphDB() {
		GraphDatabaseBuilder builder = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder();
		if (this.properties != null) {
			if (this.properties
					.getProperty(DBProperties.PAGECACHE_MEMORY) != null)
				builder.setConfig(
						GraphDatabaseSettings.pagecache_memory,
						DBProperties.PAGECACHE_MEMORY);
			if (this.properties.getProperty(DBProperties.STRING_BLOCK_SIZE) != null)
				builder.setConfig(GraphDatabaseSettings.string_block_size,
						DBProperties.ARRAY_BLOCK_SIZE);
			if (this.properties.getProperty(DBProperties.STRING_BLOCK_SIZE) != null)
				builder.setConfig(GraphDatabaseSettings.array_block_size,
						DBProperties.ARRAY_BLOCK_SIZE);
		}
		
//		builder.setConfig(GraphDatabaseSettings.cypher_planner, "RULE");
		
		return builder.newGraphDatabase();
	}
}
