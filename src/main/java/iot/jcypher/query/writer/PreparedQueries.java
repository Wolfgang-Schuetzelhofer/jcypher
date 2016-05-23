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

package iot.jcypher.query.writer;

import java.util.ArrayList;
import java.util.List;

public class PreparedQueries {

	private List<PreparedQuery> preparedQueries;
	private String json;
	private boolean dSLParams;
	
	PreparedQueries() {
		this.preparedQueries = new ArrayList<PreparedQuery>();
	}

	public void add(PreparedQuery preparedQuery) {
		this.preparedQueries.add(preparedQuery);
	}
	
	public List<PreparedQuery> getPreparedQueries() {
		return preparedQueries;
	}

	public String getJson() {
		return json;
	}

	void setJson(String json) {
		this.json = json;
	}
	
	boolean hasdSLParams() {
		return dSLParams;
	}

	void setdSLParams() {
		this.dSLParams = true;
	}
}
