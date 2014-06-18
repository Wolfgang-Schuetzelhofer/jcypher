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

package iot.jcypher;

import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.result.JcError;
import iot.jcypher.result.model.JcrNode;
import iot.jcypher.result.model.JcrRelation;
import iot.jcypher.result.util.ResultHandler;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class JcQueryResult {

	private JsonObject jsonResult;
	private List<JcError> generalErrors;
	private List<JcError> dbErrors;
	private ResultHandler resultHandler;

	public JcQueryResult(JsonObject jsonResult) {
		super();
		this.jsonResult = jsonResult;
		this.resultHandler = new ResultHandler(this);
	}

	/**
	 * @return the JsonObject representing the query result.
	 */
	public JsonObject getJsonResult() {
		return jsonResult;
	}
	
	public JcrNode resultOf(JcNode node) {
		return this.resultHandler.getNode(node);
	}
	
	public JcrRelation resultOf(JcRelation relation) {
		return this.resultHandler.getRelation(relation);
	}

	/**
	 * @return a list of general errors (e.g. connection errors).
	 */
	public List<JcError> getGeneralErrors() {
		if (this.generalErrors == null)
			this.generalErrors = new ArrayList<JcError>();
		return this.generalErrors;
	}
	
	/**
	 * @return a list of database errors
	 * <br/>(the database was successfully accessed, but the query produced error(s)).
	 */
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
	
	/**
	 * Add a general (system) error
	 * @param generalError
	 */
	public void addGeneralError(JcError generalError) {
		getGeneralErrors().add(generalError);
	}

	/**
	 * @return true, if the query result contains any error(s).
	 */
	public boolean hasErrors() {
		return !this.getGeneralErrors().isEmpty() || !this.getDBErrors().isEmpty();
	}
}
