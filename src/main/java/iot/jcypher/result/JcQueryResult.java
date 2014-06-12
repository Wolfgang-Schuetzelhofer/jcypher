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

package iot.jcypher.result;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class JcQueryResult {

	private JsonObject jsonResult;
	private List<JcError> generalErrors;
	private List<JcError> dbErrors;

	public JcQueryResult(JsonObject jsonResult) {
		super();
		this.jsonResult = jsonResult;
	}

	public JsonObject getJsonResult() {
		return jsonResult;
	}

	public List<JcError> getGeneralErrors() {
		if (this.generalErrors == null)
			this.generalErrors = new ArrayList<JcError>();
		return this.generalErrors;
	}
	
	public List<JcError> getDBErrors() {
		if (this.dbErrors == null) {
			this.dbErrors = new ArrayList<JcError>();
			JsonObject obj = getJsonResult();
			JsonArray errs = obj.getJsonArray("errors");
			int size = errs.size();
			for (int i = 0; i < size; i++) {
				JsonObject err = errs.getJsonObject(i);
				this.dbErrors.add(new JcError(err.getString("code"),
						err.getString("message")));
			}
		}
		return this.dbErrors;
	}
	
	public void addGeneralError(JcError generalError) {
		getGeneralErrors().add(generalError);
	}

	public boolean hasErrors() {
		return !this.getGeneralErrors().isEmpty() || !this.getDBErrors().isEmpty();
	}
}
