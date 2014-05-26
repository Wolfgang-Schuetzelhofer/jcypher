package iot.jcypher;

import iot.jcypher.writer.Format;
import iot.jcypher.writer.IQueryParam;
import iot.jcypher.writer.QueryParam;
import iot.jcypher.writer.QueryParamSet;
import iot.jcypher.writer.WriterContext;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.junit.internal.matchers.ThrowableCauseMatcher;

public class JSONWriter {

	private static JsonGeneratorFactory prettyGeneratorFactory;
	
	public static void toJSON(JcQuery query, WriterContext context) {
		Format cf = context.cypherFormat;
		context.cypherFormat = Format.NONE;
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
		generator.write("query", cypher);
		writeQueryParams(context, generator);
		generator.writeEnd();

		generator.flush();
		context.buffer.append(sw.getBuffer());
	}
	
	private static void writeQueryParams(WriterContext context,
			JsonGenerator generator) {
		if (context.extractParams) {
			List<IQueryParam> params = QueryParamSet.getQueryParams(context);
			if (params != null) {
				generator.writeStartObject("params");
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
}
