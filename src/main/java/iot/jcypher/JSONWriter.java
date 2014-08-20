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

import iot.jcypher.query.writer.ContextAccess;
import iot.jcypher.query.writer.Format;
import iot.jcypher.query.writer.IQueryParam;
import iot.jcypher.query.writer.QueryParam;
import iot.jcypher.query.writer.QueryParamSet;
import iot.jcypher.query.writer.WriterContext;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

public class JSONWriter {

	private static JsonGeneratorFactory prettyGeneratorFactory;
	
	public static void toJSON(List<JcQuery> queries, WriterContext context) {
		List<Statement> statements = new ArrayList<Statement>(queries.size());
		Format cf = context.cypherFormat;
		context.cypherFormat = Format.NONE;
		boolean useTxEndpoint = ContextAccess.useTransationalEndpoint(context);
		// needed for multiple statements
		ContextAccess.setUseTransactionalEndpoint(true, context);
		boolean extract = QueryParam.isExtractParams(context);
		for (JcQuery query : queries) {
			WriterContext ctxt = ContextAccess.cloneContext(context);
			QueryParam.setExtractParams(query.isExtractParams(), ctxt);
			CypherWriter.toCypherExpression(query, ctxt);
			String cypher = ctxt.buffer.toString();
			reInitContext(ctxt);
			statements.add(new Statement(ctxt, cypher));
		}
		context.cypherFormat = cf;
		
		StringWriter sw = new StringWriter();
		JsonGenerator generator;
		if (context.cypherFormat != Format.NONE) {
			JsonGeneratorFactory gf = getPrettyGeneratorFactory();
			generator = gf.createGenerator(sw);
		} else
			generator = Json.createGenerator(sw);
		
		generator.writeStartObject();
		Statement[] statementsArray = statements.toArray(new Statement[statements.size()]);
		writeStatements(statementsArray, generator);
		generator.writeEnd();

		generator.flush();
		context.buffer.append(sw.getBuffer());
		
		// reset to original
		QueryParam.setExtractParams(extract, context);
		ContextAccess.setUseTransactionalEndpoint(useTxEndpoint, context);
	}
	
	public static void toJSON(JcQuery query, WriterContext context) {
		Format cf = context.cypherFormat;
		context.cypherFormat = Format.NONE;
		boolean extract = QueryParam.isExtractParams(context);
		QueryParam.setExtractParams(query.isExtractParams(), context);
		CypherWriter.toCypherExpression(query, context);
		context.cypherFormat = cf;
		String cypher = context.buffer.toString();
		reInitContext(context);
		
		StringWriter sw = new StringWriter();
		JsonGenerator generator;
		if (context.cypherFormat != Format.NONE) {
			JsonGeneratorFactory gf = getPrettyGeneratorFactory();
			generator = gf.createGenerator(sw);
		} else
			generator = Json.createGenerator(sw);
		
		generator.writeStartObject();
		if (ContextAccess.useTransationalEndpoint(context))
			writeStatements(new Statement[] {new Statement(context, cypher)}, generator);
		else
			writeQuery("query", cypher, context, generator);
		generator.writeEnd();

		generator.flush();
		context.buffer.append(sw.getBuffer());
		
		// reset to original
		QueryParam.setExtractParams(extract, context);
	}
	
	private static void writeStatements(Statement[] statements, JsonGenerator generator) {
		generator.writeStartArray("statements");
		for (Statement statement : statements) {
			generator.writeStartObject();
			writeQuery("statement", statement.cypher, statement.context, generator);
			if (ContextAccess.getResultDataContents( statement.context).size() > 0) {
				generator.writeStartArray("resultDataContents");
				for (String contentDescription : ContextAccess.getResultDataContents( statement.context)) {
					generator.write(contentDescription);
				}
				generator.writeEnd();
			}
			generator.writeEnd();
		}
		generator.writeEnd();
	}

	private static void writeQuery(String queryKey, String cypher,
			WriterContext context, JsonGenerator generator) {
		generator.write(queryKey, cypher);
		writeQueryParams(context, generator);
	}

	private static void writeQueryParams(WriterContext context,
			JsonGenerator generator) {
		if (QueryParam.isExtractParams(context)) {
			List<IQueryParam> params = QueryParamSet.getQueryParams(context);
			if (params != null) {
				String paramsKey = "params";
				if (ContextAccess.useTransationalEndpoint(context))
					paramsKey = "parameters";
				generator.writeStartObject(paramsKey);
				for (IQueryParam iparam : params) {
					if (iparam instanceof QueryParamSet) {
						QueryParamSet paramSet = (QueryParamSet)iparam;
						if (paramSet.canUseSet() && paramSet.getQueryParams().size() > 1)
							writeAsSet(paramSet, generator);
						else
							writeAsParams(paramSet, generator);
					} else if (iparam instanceof QueryParam) {
						writeParam((QueryParam)iparam, generator);
					}
				}
				generator.writeEnd();
			}
		}
	}

	private static void writeAsParams(QueryParamSet paramSet,
			JsonGenerator generator) {
		for (QueryParam param : paramSet.getQueryParams()) {
			String key = param.getKey();
			Object val = param.getValue();
			writeLiteral(key, val, generator);
		}
	}

	private static void writeAsSet(QueryParamSet paramSet,
			JsonGenerator generator) {
		generator.writeStartObject(paramSet.getKey());
		for (QueryParam param : paramSet.getQueryParams()) {
			String key = param.getOrgName();
			Object val = param.getValue();
			writeLiteral(key, val, generator);
		}
		generator.writeEnd();
	}
	
	private static void writeParam(QueryParam param, JsonGenerator generator) {
		String key = param.getKey();
		Object val = param.getValue();
		writeLiteral(key, val, generator);
	}

	private static void writeLiteral(String key, Object val,
			JsonGenerator generator) {
		if (val instanceof String)
			generator.write(key, val.toString());
		else if (val instanceof Number) {
			if (val instanceof Long)
				generator.write(key, (Long)val);
			else if (val instanceof Integer)
				generator.write(key, (Integer)val);
			else if (val instanceof Double)
				generator.write(key, (Double)val);
			else if (val instanceof Float)
				generator.write(key, (Float)val);
		} else if (val instanceof Boolean)
			generator.write(key, (Boolean)val);
		else if (val instanceof List<?>) {
			generator.writeStartArray(key);
			for (Object v : (List<?>)val) {
				writeLiteral(v, generator);
			}
			generator.writeEnd();
		} else if (val != null) // handle everything else as a string
			generator.write(key, val.toString());
	}
	
	private static void writeLiteral(Object val, JsonGenerator generator) {
		if (val instanceof String)
			generator.write(val.toString());
		else if (val instanceof Number) {
			if (val instanceof Long)
				generator.write((Long)val);
			else if (val instanceof Integer)
				generator.write((Integer)val);
			else if (val instanceof Double)
				generator.write((Double)val);
			else if (val instanceof Float)
				generator.write((Float)val);
		} else if (val instanceof Boolean)
			generator.write((Boolean)val);
		else if (val != null) // handle everything else as a string
			generator.write(val.toString());
	}

	private static void reInitContext(WriterContext context) {
		context.buffer = new StringBuilder();
		context.inFunction = false;
		context.currentClause = null;
		context.previousClause = null;
		context.resetLevel();
	}
	
	private static JsonGeneratorFactory getPrettyGeneratorFactory() {
		if (prettyGeneratorFactory == null) {
			HashMap<String, Object> prettyConfigMap = new HashMap<String, Object>();
			prettyConfigMap.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
			prettyGeneratorFactory = Json.createGeneratorFactory(prettyConfigMap);
		}
		return prettyGeneratorFactory;
	}
	
	/*******************************************/
	private static class Statement {
		private WriterContext context;
		private String cypher;
		
		private Statement(WriterContext context, String cypher) {
			super();
			this.context = context;
			this.cypher = cypher;
		}
		
	}
}
