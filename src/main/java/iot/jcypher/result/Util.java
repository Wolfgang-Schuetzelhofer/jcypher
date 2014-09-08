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

import iot.jcypher.JcQuery;
import iot.jcypher.JcQueryResult;
import iot.jcypher.query.writer.Format;

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
	 * @param title
	 * @param format
	 */
	public static void printQuery(JcQuery query, String title, Format format) {
		System.out.println("QUERY: " + title + " --------------------");
		// map to Cypher
		System.out.println("CYPHER --------------------");
		String cypher = iot.jcypher.samples.Util.toCypher(query, format);
		System.out.println("--------------------");
		System.out.println(cypher);
		
		// map to JSON
		String json = iot.jcypher.samples.Util.toJSON(query, format);
		System.out.println("");
		System.out.println("JSON   --------------------");
		System.out.println(json);
		
		System.out.println("");
	}
	
	/**
	 * map to CYPHER statements and map to JSON, print the mapping results to System.out
	 * @param queries
	 * @param title
	 * @param format
	 */
	public static void printQueries(List<JcQuery> queries, String title, Format format) {
		System.out.println("QUERIES: " + title + " --------------------");
		// map to Cypher
		System.out.println("CYPHER --------------------");
		for(int i = 0; i < queries.size(); i++) {
			String cypher = iot.jcypher.samples.Util.toCypher(queries.get(i), format);
			System.out.println("--------------------");
			System.out.println(cypher);
		}
		
		// map to JSON
		String json = iot.jcypher.samples.Util.toJSON(queries, format);
		System.out.println("");
		System.out.println("JSON   --------------------");
		System.out.println(json);
		
		System.out.println("");
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
	
	private static void appendErrorList(List<JcError> errors, StringBuilder sb) {
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
}
