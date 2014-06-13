package iot.jcypher.query.writer;

import java.util.ArrayList;

public class QueryParam implements IQueryParam {

	private String key;
	private Object value;
	private String orgName;
	
	public static QueryParam createParam(String name, Object value, WriterContext context) {
		QueryParam qp = new QueryParam();
		qp.setKey(createNewParamKey(context));
		qp.setValue(value);
		qp.setOrgName(name);
		return qp;
	}
	
	public static QueryParam createAddParam(String name, Object value, WriterContext context) {
		if (context.extractParams) {
			if (context.queryParams == null)
				context.queryParams = new ArrayList<IQueryParam>();
			QueryParam qp = QueryParam.createParam(name, value, context);
			context.queryParams.add(qp);
			return qp;
		}
		return null;
	}
	
	public static void setParamIndex(int idx, WriterContext context) {
		context.setParamIndex(idx);
	}
	
	public static int getParamIndex(WriterContext context) {
		return context.getParamIndex();
	}
	
	public static boolean isExtractParams(WriterContext context) {
		return context.extractParams;
	}

	public static void setExtractParams(boolean extractParams, WriterContext context) {
		context.extractParams = extractParams;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	private static String createNewParamKey(WriterContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("param_");
		sb.append(context.getNextParamIndex());
		return sb.toString();
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
}
