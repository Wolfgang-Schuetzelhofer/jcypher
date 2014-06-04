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

/**
 * Defines the keys of properties needed to configure database access.
 */
public interface DBProperties {
	
	/******************** properties for remote database access *********************/
	/** REQUIRED - Address (uri) of a remote database server.	 */
	public static final String SERVER_ROOT_URI = "server_root_uri";
	
	/******************** properties for embedded database access *********************/
	/** REQUIRED - Directory of the embedded database. */
	public static final String DATABASE_DIR = "database_dir";
	/** OPTIONAL  e.g. "10M" */
	public static final String NODESTORE_MAPPED_MAMORY_SIZE = "nodestore_mapped_memory_size";
	/** OPTIONAL  e.g. "60" */
	public static final String STRING_BLOCK_SIZE = "string_block_size";
	/** OPTIONAL  e.g. "300" */
	public static final String ARRAY_BLOCK_SIZE = "array_block_size";
}
