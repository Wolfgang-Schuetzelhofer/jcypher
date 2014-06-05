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

package iot.jcypher.database.remote;

import iot.jcypher.JSONWriter;
import iot.jcypher.JcQuery;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.result.JcError;
import iot.jcypher.result.JcQueryResult;
import iot.jcypher.writer.ContextAccess;
import iot.jcypher.writer.WriterContext;

import java.io.StringReader;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;

public class RemoteDBAccess implements IDBAccess {

	private static final String transactionalURLPostfix = "db/data/transaction/commit";
	
	private Properties properties;
	private Client restClient;
	private WebTarget transactionalTarget;
	private Invocation.Builder invocationBuilder;
	
	@Override
	public void initialize(Properties properties) {
		this.properties = properties;
		if (this.properties == null)
			throw new RuntimeException("missing properties in database configuration");
		if (this.properties.getProperty(DBProperties.SERVER_ROOT_URI) == null)
			throw new RuntimeException("missing property: '" +
					DBProperties.SERVER_ROOT_URI + "' in database configuration");
	}

	@Override
	public JcQueryResult execute(JcQuery query) {
		WriterContext context = new WriterContext();
		// to create correct json for using transactional endpoint vs. using cypher endpoint
		ContextAccess.setUseTransactionalEndpoint(true, context);
		ContextAccess.getResultDataContents(context).add("rest");
		ContextAccess.getResultDataContents(context).add("graph");
		JSONWriter.toJSON(query, context);
		String json = context.buffer.toString();
		Response response = null;
		Throwable exception = null;
		try {
			Builder iBuilder = getInvocationBuilder();
			response = iBuilder.post(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE));
		} catch(Throwable e) {
			exception = e;
		}
		
		JsonObject jsonResult = null;
		StatusType status = null;
		if (exception == null) {
			status = response.getStatusInfo();
			String result = response.readEntity(String.class);
			if (result != null && result.length() > 0) {
				StringReader sr = new StringReader(result);
				JsonReader reader = Json.createReader(sr);
				jsonResult = reader.readObject();
			}
		}
		JcQueryResult ret = new JcQueryResult(jsonResult);
		if (exception != null) {
			String typ = exception.getClass().getSimpleName();
			String msg = exception.getLocalizedMessage();
			ret.setGeneralError(new JcError(typ, msg));
		} else if (status != null && status.getStatusCode() >= 400) {
			String code = String.valueOf(status.getStatusCode());
			String msg = status.getReasonPhrase();
			ret.setGeneralError(new JcError(code, msg));
		}
		return ret;
	}

	private synchronized Client getRestClient() {
		if (this.restClient == null) {
			this.restClient = ClientBuilder.newClient();
		}
		return this.restClient;
	}
	
	private synchronized WebTarget getTransactionalTarget() {
		if (this.transactionalTarget == null) {
			WebTarget serverRootTarget = getRestClient().target(
					this.properties.getProperty(DBProperties.SERVER_ROOT_URI));
			this.transactionalTarget = serverRootTarget.path(transactionalURLPostfix);
		}
		return this.transactionalTarget;
	}
	
	private synchronized Invocation.Builder getInvocationBuilder() {
		if (this.invocationBuilder == null) {
			this.invocationBuilder = getTransactionalTarget().request(MediaType.APPLICATION_JSON_TYPE);
		}
		return this.invocationBuilder;
	}
}
