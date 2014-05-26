package iot.jcypher.writer;

import java.util.ArrayList;
import java.util.List;

public class QueryParamSet implements IQueryParam {

	private boolean canUseSet = true;
	private String key;
	private List<QueryParam> queryParams = new ArrayList<QueryParam>();
	
	public static QueryParamSet createAddParamSet(WriterContext context) {
		if (context.extractParams) {
			context.currentParamOrSet = new QueryParamSet();
			StringBuilder sb = new StringBuilder();
			sb.append("props_");
			sb.append(context.getParamSetIndex());
			((QueryParamSet)context.currentParamOrSet).setKey(sb.toString());
			return (QueryParamSet)context.currentParamOrSet;
		}
		return null;
	}
	
	public static void finishParamSet(WriterContext context) {
		if (context.extractParams) {
			if (context.currentParamOrSet instanceof QueryParamSet) {
				if (context.queryParams == null)
					context.queryParams = new ArrayList<IQueryParam>();
				context.queryParams.add(context.currentParamOrSet);
				if (((QueryParamSet)context.currentParamOrSet).canUseSet())
					context.incrementParamSetIndex();
				context.currentParamOrSet = null;
			}
		}
	}
	
	public static void disableUseSet(WriterContext context) {
		if (context.currentParamOrSet instanceof QueryParamSet)
			((QueryParamSet)context.currentParamOrSet).setCanUseSet(false);
	}
	
	public static boolean canUseSet(WriterContext context) {
		if (context.currentParamOrSet instanceof QueryParamSet)
			return ((QueryParamSet)context.currentParamOrSet).canUseSet();
		return false;
	}
	
	public static void addQueryParam(QueryParam param, WriterContext context) {
		if (context.currentParamOrSet instanceof QueryParamSet) {
			((QueryParamSet)context.currentParamOrSet).getQueryParams().add(param);
		}
	}
	
	public static QueryParamSet getCurrentSet(WriterContext context) {
		if (context.currentParamOrSet instanceof QueryParamSet) {
			return (QueryParamSet)context.currentParamOrSet;
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
