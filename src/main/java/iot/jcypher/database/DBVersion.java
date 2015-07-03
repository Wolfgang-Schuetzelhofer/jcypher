/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

public class DBVersion {

	/**
	 * possible values:
	 * <br/>'2.1.x' .. to work with remote neo4j databases of versions: 2.1.6 or 2.1.7.
	 * <br/>'2.2.x' .. to work with remote neo4j databases of version: 2.2.2.
	 */
	public static String Neo4j_Version = "2.2.x";
}
