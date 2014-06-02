package iot.jcypher.samples;

import iot.jcypher.CypherWriter;
import iot.jcypher.JSONWriter;
import iot.jcypher.JcQuery;
import iot.jcypher.writer.Format;
import iot.jcypher.writer.WriterContext;

public class Util {

	public static String toCypher(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		CypherWriter.toCypherExpression(query, context);
		return context.buffer.toString();
	}
	
	public static String toJSON(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		JSONWriter.toJSON(query, context);
		return context.buffer.toString();
	}
}
