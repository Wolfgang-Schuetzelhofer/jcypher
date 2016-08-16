/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
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

package iot.jcypher.database.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iot.jcypher.query.writer.IQueryParam;
import iot.jcypher.query.writer.QueryParam;
import iot.jcypher.query.writer.QueryParamSet;
import iot.jcypher.query.writer.WriterContext;

public class QParamsUtil {

	public static Map<String, Object> createQueryParams(WriterContext context) {
		Map<String, Object> paramsMap = null;
		if (QueryParam.isExtractParams(context)) {
			List<IQueryParam> params = QueryParamSet.getQueryParams(context);
			if (params != null) {
				for (IQueryParam iparam : params) {
					if (paramsMap == null)
						paramsMap = new HashMap<String, Object>();
					if (iparam instanceof QueryParamSet) {
						QueryParamSet paramSet = (QueryParamSet)iparam;
						if (paramSet.canUseSet() && paramSet.getQueryParams().size() > 1)
							writeAsSet(paramSet, paramsMap);
						else
							writeAsParams(paramSet, paramsMap);
					} else if (iparam instanceof QueryParam) {
						String key = ((QueryParam)iparam).getKey();
						Object val = ((QueryParam)iparam).getValue();
						val = convertVal(val);
						paramsMap.put(key, val);
					}
				}
			}
		}
		return paramsMap;
	}
	
	private static void writeAsSet(QueryParamSet paramSet,
			Map<String, Object> paramsMap) {
		Map<String, Object> set = new HashMap<String, Object>();
		paramsMap.put(paramSet.getKey(), set);
		for (QueryParam param : paramSet.getQueryParams()) {
			String key = param.getOrgName();
			Object val = param.getValue();
			val = convertVal(val);
			set.put(key, val);
		}
	}
	
	private static void writeAsParams(QueryParamSet paramSet,
			Map<String, Object> paramsMap) {
		for (QueryParam param : paramSet.getQueryParams()) {
			String key = param.getKey();
			Object val = param.getValue();
			val = convertVal(val);
			paramsMap.put(key, val);
		}
	}
	
	private static Object convertVal(Object val) {
		if (!(val instanceof Number) && !(val instanceof Boolean) && !(val instanceof List<?>))
			return val.toString();
		return val;
	}
}
