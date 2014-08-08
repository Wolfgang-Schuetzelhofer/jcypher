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
	
	public static WriterContext cloneContext(WriterContext ctxt) {
		WriterContext ret = new WriterContext();
		ret.cypherFormat = ctxt.cypherFormat;
		ret.extractParams = ctxt.extractParams;
		ret.useTransactionalEndpoint = ctxt.useTransactionalEndpoint;
		for (String contentType : ctxt.getResultDataContents()) {
			ret.getResultDataContents().add(contentType);
		}
		return ret;
	}
}
