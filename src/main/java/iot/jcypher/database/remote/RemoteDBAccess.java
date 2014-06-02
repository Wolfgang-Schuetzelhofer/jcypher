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
import iot.jcypher.result.JcQueryResult;
import iot.jcypher.writer.ContextAccess;
import iot.jcypher.writer.WriterContext;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

public class RemoteDBAccess implements IDBAccess {

	private static final String transactionalURLPostfix = "db/data/transaction/commit";
	
	private Properties properties;
	private Client restClient;
	private WebTarget transactionalTarget;
	private Invocation.Builder invocationBuilder;
	
	@Override
	public void initialize(Properties properties) {
		this.properties = properties;
	}

	@Override
	public JcQueryResult execute(JcQuery query) {
		WriterContext context = new WriterContext();
		ContextAccess.setUseTransactionalEndpoint(true, context);
		JSONWriter.toJSON(query, context);
		String json = context.buffer.toString();
		Builder iBuilder = getInvocationBuilder();
		Response response = iBuilder.post(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE));
		StatusType status = response.getStatusInfo();
		String result = response.readEntity(String.class);
		return null;
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
