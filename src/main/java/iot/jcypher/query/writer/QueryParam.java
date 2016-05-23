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

import iot.jcypher.query.JcQueryParameter;

public class QueryParam implements IQueryParam {

	private String key;
	private Object value;
	private String orgName;
	
	public static QueryParam createParam(String name, Object value, WriterContext context) {
		QueryParam qp = new QueryParam();
		if (value instanceof JcQueryParameter) {
			qp.setKey(((JcQueryParameter)value).getName());
			context.doesHaveDSLParams();
		} else
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
		if (this.value instanceof JcQueryParameter) {
			Object val = ((JcQueryParameter)this.value).getValue();
			if (val == null)
				val = "NOT_SET";
			return val;
		}
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(": ");
		sb.append(value);
		sb.append(": ");
		sb.append(orgName);
		return sb.toString();
	}
	
}
