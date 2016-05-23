/************************************************************************
 * Copyright (c) 2014-2016 IoT-Solutions e.U.
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

package iot.jcypher.util;

import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.writer.CypherWriter;
import iot.jcypher.query.writer.Format;
import iot.jcypher.query.writer.JSONWriter;
import iot.jcypher.query.writer.PreparedQueries;
import iot.jcypher.query.writer.PreparedQuery;
import iot.jcypher.query.writer.WriterContext;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

public class Util {

	/**
	 * write a JsonObject formatted in a pretty way into a String
	 * @param jsonObject
	 * @return a String containing the JSON
	 */
	public static String writePretty(JsonObject jsonObject) {
		JsonWriterFactory factory = createPrettyWriterFactory();
		StringWriter sw = new StringWriter();
		JsonWriter writer = factory.createWriter(sw);
		writer.writeObject(jsonObject);
		String ret = sw.toString();
		writer.close();
		return ret;
	}
	
	private static JsonWriterFactory createPrettyWriterFactory() {
		HashMap<String, Object> prettyConfigMap = new HashMap<String, Object>();
		prettyConfigMap.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
		JsonWriterFactory prettyWriterFactory = Json.createWriterFactory(prettyConfigMap);
		return prettyWriterFactory;
	}
	
	/**
	 * map to CYPHER statements and map to JSON, print the mapping results to System.out
	 * @param queries
	 * @param toObserve
	 * @param format
	 */
	public static void printQuery(JcQuery query, QueryToObserve toObserve, Format format) {
		boolean titlePrinted = false;
		ContentToObserve tob = QueriesPrintObserver.contentToObserve(toObserve);
		if (tob == ContentToObserve.CYPHER || tob == ContentToObserve.CYPHER_JSON) {
			titlePrinted = true;
			QueriesPrintObserver.printStream.println("#QUERY: " + toObserve.getTitle() + " --------------------");
			// map to Cypher
			QueriesPrintObserver.printStream.println("#CYPHER --------------------");
			String cypher = iot.jcypher.util.Util.toCypher(query, format);
			QueriesPrintObserver.printStream.println("#--------------------");
			QueriesPrintObserver.printStream.println(cypher);
			QueriesPrintObserver.printStream.println("");
		}
		if (tob == ContentToObserve.JSON || tob == ContentToObserve.CYPHER_JSON) {
			// map to JSON
			if (!titlePrinted)
				QueriesPrintObserver.printStream.println("#QUERY: " + toObserve.getTitle() + " --------------------");
			String json = iot.jcypher.util.Util.toJSON(query, format);
			QueriesPrintObserver.printStream.println("#JSON   --------------------");
			QueriesPrintObserver.printStream.println(json);
			
			QueriesPrintObserver.printStream.println("");
		}
	}
	
	/**
	 * map to CYPHER statements and map to JSON, print the mapping results
	 * to the output streams configured in QueriesPrintObserver
	 * @param queries
	 * @param toObserve
	 * @param format
	 */
	public static void printQueries(List<JcQuery> queries, QueryToObserve toObserve, Format format) {
		boolean titlePrinted = false;
		ContentToObserve tob = QueriesPrintObserver.contentToObserve(toObserve);
		if (tob == ContentToObserve.CYPHER || tob == ContentToObserve.CYPHER_JSON) {
			titlePrinted = true;
			QueriesPrintObserver.printStream.println("#QUERIES: " + toObserve.getTitle() + " --------------------");
			// map to Cypher
			QueriesPrintObserver.printStream.println("#CYPHER --------------------");
			for(int i = 0; i < queries.size(); i++) {
				String cypher = iot.jcypher.util.Util.toCypher(queries.get(i), format);
				QueriesPrintObserver.printStream.println("#--------------------");
				QueriesPrintObserver.printStream.println(cypher);
			}
			QueriesPrintObserver.printStream.println("");
		}
		if (tob == ContentToObserve.JSON || tob == ContentToObserve.CYPHER_JSON) {
			// map to JSON
			if (!titlePrinted)
				QueriesPrintObserver.printStream.println("#QUERIES: " + toObserve.getTitle() + " --------------------");
			String json = iot.jcypher.util.Util.toJSON(queries, format);
			QueriesPrintObserver.printStream.println("#JSON   --------------------");
			QueriesPrintObserver.printStream.println(json);
			
			QueriesPrintObserver.printStream.println("");
		}
	}
	
	public static void printResult(JcQueryResult result, String title, Format format) {
		System.out.println("RESULT OF: " + title + " --------------------");
		List<JcError> errors = collectErrors(result);
		if (errors.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("---------------Errors:");
			appendErrorList(errors, sb);
			String str = sb.toString();
			System.out.println("");
			System.out.println(str);
		}
		
		System.out.println("JSON RESULT --------------------");
		String resultString = Util.writePretty(result.getJsonResult());
		System.out.println(resultString);
	}
	
	public static void printResults(List<JcQueryResult> results, String title, Format format) {
		System.out.println("RESULTS OF: " + title + " --------------------");
		List<JcError> errors = collectErrors(results);
		if (errors.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("---------------Errors:");
			appendErrorList(errors, sb);
			String str = sb.toString();
			System.out.println("");
			System.out.println(str);
		}
		
		System.out.println("JSON RESULT --------------------");
		String resultString = Util.writePretty(results.get(0).getJsonResult());
		System.out.println(resultString);
	}
	
	public static List<JcError> collectErrors(List<JcQueryResult> results) {
		List<JcError> errors = new ArrayList<JcError>();
		boolean first = true;
		for (JcQueryResult result : results) {
			if (first) {
				errors.addAll(result.getGeneralErrors());
				first = false;
			}
			errors.addAll(result.getDBErrors());
		}
		return errors;
	}
	
	public static List<JcError> collectErrors(JcQueryResult result) {
		List<JcError> errors = new ArrayList<JcError>();
		errors.addAll(result.getGeneralErrors());
		errors.addAll(result.getDBErrors());
		return errors;
	}
	
	public static void appendErrorList(List<JcError> errors, StringBuilder sb) {
		int num = errors.size();
		for (int i = 0; i < num; i++) {
			JcError err = errors.get(i);
			sb.append('\n');
			if (i > 0) {
				sb.append("-------------------\n");
			}
			sb.append("codeOrType: ");
			sb.append(err.getCodeOrType());
			sb.append("\nmessage: ");
			sb.append(err.getMessage());
			if (err.getAdditionalInfo() != null) {
				sb.append("\nadditional info: ");
				sb.append(err.getAdditionalInfo());
			}
		}
	}

	public static String toCypher(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		CypherWriter.toCypherExpression(query, context);
		return context.buffer.toString();
	}

	public static String toJSON(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		//ContextAccess.setUseTransactionalEndpoint(true, context);
		context.cypherFormat = pretty;
		JSONWriter.toJSON(query, context);
		return context.buffer.toString();
	}
	
	public static String toJSON(List<JcQuery> queries, Format pretty) {
		WriterContext context = new WriterContext();
		//ContextAccess.setUseTransactionalEndpoint(true, context);
		context.cypherFormat = pretty;
		JSONWriter.toJSON(queries, context);
		return context.buffer.toString();
	}
	
	public static PreparedQuery toPreparedQuery(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		//ContextAccess.setUseTransactionalEndpoint(true, context);
		context.cypherFormat = pretty;
		return JSONWriter.toPreparedQuery(query, context);
	}
	
	public static PreparedQueries toPreparedQueries(List<JcQuery> queries, Format pretty) {
		WriterContext context = new WriterContext();
		//ContextAccess.setUseTransactionalEndpoint(true, context);
		context.cypherFormat = pretty;
		return JSONWriter.toPreparedQueries(queries, context);
	}
	
	public static String toJSON(PreparedQuery preparedQuery) {
		return JSONWriter.toJSON(preparedQuery);
	}
	
	public static String toJSON(PreparedQueries preparedQueries) {
		return JSONWriter.toJSON(preparedQueries);
	}
}
