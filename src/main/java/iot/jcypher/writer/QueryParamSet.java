package iot.jcypher.writer;

import java.util.ArrayList;
import java.util.List;

public class QueryParamSet implements IQueryParam {

	private boolean canUseSet = true;
	private String key;
	private List<QueryParam> queryParams = new ArrayList<QueryParam>();
	
	public static QueryParamSet createAddParamSet(WriterContext context) {
		if (context.extractParams) {
			context.currentParamSet = new QueryParamSet();
			StringBuilder sb = new StringBuilder();
			sb.append("props_");
			sb.append(context.getParamSetIndex());
			context.currentParamSet.setKey(sb.toString());
			return context.currentParamSet;
		}
		return null;
	}
	
	public static void finishParamSet(WriterContext context) {
		if (context.extractParams) {
			if (context.currentParamSet != null) {
				if (context.queryParams == null)
					context.queryParams = new ArrayList<IQueryParam>();
				context.queryParams.add(context.currentParamSet);
				if (context.currentParamSet.canUseSet())
					context.incrementParamSetIndex();
				context.currentParamSet = null;
			}
		}
	}
	
	public static void disableUseSet(WriterContext context) {
		if (context.currentParamSet != null)
			context.currentParamSet.setCanUseSet(false);
	}
	
	public static boolean canUseSet(WriterContext context) {
		if (context.currentParamSet != null)
			return context.currentParamSet.canUseSet();
		return false;
	}
	
	public static void addQueryParam(QueryParam param, WriterContext context) {
		if (context.currentParamSet != null) {
			context.currentParamSet.getQueryParams().add(param);
		}
	}
	
	public static QueryParamSet getCurrentSet(WriterContext context) {
		if (context.currentParamSet != null) {
			return context.currentParamSet;
		}
		return null;
	}
	
	public static List<IQueryParam> getQueryParams(WriterContext context) {
		return context.queryParams;
	}
	
	public boolean canUseSet() {
		return canUseSet;
	}
	public void setCanUseSet(boolean canUseSet) {
		this.canUseSet = canUseSet;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public List<QueryParam> getQueryParams() {
		return queryParams;
	}
	public void setQueryParams(List<QueryParam> queryParams) {
		this.queryParams = queryParams;
	}
}
