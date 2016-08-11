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

package iot.jcypher.database;

/**
 * Defines the keys of properties needed to configure database access.
 */
public interface DBProperties {
	
	/******************** properties for remote database access *********************/
	/** REQUIRED - Address (uri) of a remote database server.	 */
	public static final String SERVER_ROOT_URI = "server_root_uri";
	/** OPTIONAL - From Neo4J 3.0 onwards an optimized binary protocol,
	 * the BOLT protocol is available for remote db communications.
	 * By default JCypher uses the BOLT protocol. By specifying:
	 * 	USE_BOLT_PROTOCOL=false; a fallback to the JSON-HTTP protocol is possible. */
	public static final String USE_BOLT_PROTOCOL = "use_bolt_protocol";
	
	/******************** properties for embedded database access *********************/
	/** REQUIRED - Directory of the embedded database. */
	public static final String DATABASE_DIR = "database_dir";
	
	/******************** properties for embedded and in memory database access *********************/
	/** OPTIONAL  e.g. "512M" */
	public static final String PAGECACHE_MEMORY = "pagecache_memory";
	/** OPTIONAL  e.g. "60" */
	public static final String STRING_BLOCK_SIZE = "string_block_size";
	/** OPTIONAL  e.g. "300" */
	public static final String ARRAY_BLOCK_SIZE = "array_block_size";
}
