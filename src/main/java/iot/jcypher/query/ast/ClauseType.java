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

package iot.jcypher.query.ast;

public enum ClauseType {
	START, MATCH, OPTIONAL_MATCH, CREATE, CREATE_UNIQUE, MERGE, USING_INDEX,
	USING_SCAN, RETURN, WITH, WHERE, UNION, UNION_ALL,
	CYPHER_NATIVE,
	SET, DELETE, DETACH_DELETE, REMOVE, FOREACH,
	CREATE_INDEX, DROP_INDEX,
	SEPARATE,
	CASE, WHEN, ELSE, END,
	ON_CREATE_SET("ON CREATE SET"), ON_MATCH_SET("ON MATCH SET"),
	ON_CREATE_DELETE("ON CREATE DELETE"), ON_MATCH_DELETE("ON MATCH DELETE"),
	ON_CREATE_DETACH_DELETE("ON CREATE DETACH DELETE"), ON_MATCH_DETACH_DELETE("ON MATCH DETACH DELETE"),
	ON_CREATE_REMOVE("ON CREATE REMOVE"), ON_MATCH_REMOVE("ON MATCH REMOVE");
	
	private String display;

	private ClauseType(final String display) {
		this.display = display;
	}
	
	private ClauseType() {
	}

	public String getDisplay() {
		return display;
	}
	
}
