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

package iot.jcypher.query.writer;

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.query.ast.ClauseType;

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
				if (((QueryParamSet)context.currentParamOrSet).canUseSet() &&
						((QueryParamSet)context.currentParamOrSet).getQueryParams().size() > 1)
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
