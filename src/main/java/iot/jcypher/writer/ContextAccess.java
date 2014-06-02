package iot.jcypher.writer;

public class ContextAccess {

	public static boolean useTransationalEndpoint(WriterContext context) {
		return context.useTransactionalEndpoint;
	}
	
	public static void setUseTransactionalEndpoint(boolean b, WriterContext context) {
		context.useTransactionalEndpoint = b;
	}
}
