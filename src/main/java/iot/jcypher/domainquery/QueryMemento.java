/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
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

package iot.jcypher.domainquery;

public class QueryMemento {

	private String queryJSON;
	private String queryJava;
	private String description;
	
	QueryMemento(String qJava, String qJSON) {
		super();
		this.queryJava = qJava;
		this.queryJSON = qJSON;
	}
	public String getQueryJSON() {
		return queryJSON;
	}
	public String getQueryJava() {
		return queryJava;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
