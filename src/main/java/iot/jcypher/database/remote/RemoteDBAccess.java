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
import iot.jcypher.JcQueryResult;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBUtil;
import iot.jcypher.database.internal.IDBAccessInit;
import iot.jcypher.query.writer.ContextAccess;
import iot.jcypher.query.writer.WriterContext;
import iot.jcypher.result.JcError;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

public class RemoteDBAccess implements IDBAccessInit {

	private static final String transactionalURLPostfix = "db/data/transaction/commit";
	
	private Properties properties;
	private Client restClient;
	private WebTarget transactionalTarget;
	private Invocation.Builder invocationBuilder;
	private Thread shutdownHook;
	
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
		List<JcQuery> qList = new ArrayList<JcQuery>();
		qList.add(query);
		List<JcQueryResult> qrList = execute(qList);
		return qrList.get(0);
	}

	@Override
	public List<JcQueryResult> execute(List<JcQuery> queries) {
		WriterContext context = new WriterContext();
		ContextAccess.getResultDataContents(context).add("rest");
		ContextAccess.getResultDataContents(context).add("graph");
		JSONWriter.toJSON(queries, context);
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
		
		List<JcQueryResult> ret = new ArrayList<JcQueryResult>(queries.size());
		for (int i = 0; i < queries.size(); i++) {
			JcQueryResult qr = new JcQueryResult(jsonResult, i, this);
			ret.add(qr);
			if (exception != null) {
				String typ = exception.getClass().getSimpleName();
				String msg = exception.getLocalizedMessage();
				qr.addGeneralError(new JcError(typ, msg, DBUtil.getStacktrace(exception)));
			} else if (status != null && status.getStatusCode() >= 400) {
				String code = String.valueOf(status.getStatusCode());
				String msg = status.getReasonPhrase();
				qr.addGeneralError(new JcError(code, msg, null));
			}
		}
		return ret;
	}

	@Override
	public synchronized void close() {
		if (this.shutdownHook != null) {
			Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
			this.shutdownHook = null;
		}
		
		if (this.restClient != null) {
			this.restClient.close();
			this.restClient = null;
		}
		this.transactionalTarget = null;
		this.invocationBuilder = null;
	}

	private synchronized Client getRestClient() {
		if (this.restClient == null) {
			this.restClient = ClientBuilder.newClient();
			this.shutdownHook = registerShutdownHook(this.restClient);
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
	
	private static Thread registerShutdownHook(final Client client) {
		// Registers a shutdown hook
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Thread hook = new Thread() {
			@Override
			public void run() {
				try {
					client.close();
				} catch (Throwable e) {
					// do nothing
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(hook);
		return hook;
	}
}
