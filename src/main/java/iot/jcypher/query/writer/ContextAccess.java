package iot.jcypher.query.writer;

import java.util.List;

public class ContextAccess {

	public static boolean useTransationalEndpoint(WriterContext context) {
		return context.useTransactionalEndpoint;
	}
	
	public static void setUseTransactionalEndpoint(boolean b, WriterContext context) {
		context.useTransactionalEndpoint = b;
	}
	
	public static List<String> getResultDataContents(WriterContext context) {
		return context.getResultDataContents();
	}
}
