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

package iot.jcypher.domainquery.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonString;
import javax.json.JsonValue;

import iot.jcypher.domainquery.internal.RecordedQuery.Literal;
import iot.jcypher.domainquery.internal.RecordedQuery.Statement;

public class ConversionUtil {

	private static final String STRING = "java.lang.String";
	private static final String INTEGER = "java.lang.Integer";
	private static final String SHORT = "java.lang.Short";
	private static final String LONG = "java.lang.Long";
	private static final String FLOAT = "java.lang.Float";
	private static final String DOUBLE = "java.lang.Double";
	private static final String BOOL = "java.lang.Boolean";
	
	private static final String P_INTEGER = "int";
	private static final String P_SHORT = "short";
	private static final String P_LONG = "long";
	private static final String P_FLOAT = "float";
	private static final String P_DOUBLE = "double";
	private static final String P_BOOL = "boolean";
	
	private static final String ARRAY = "Array(";
	
	public static Object fromJSON(String type, JsonValue jsonValue) {
		Object val;
		if (jsonValue instanceof JsonArray) {
			val = ConversionUtil.fromList(type, (JsonArray)jsonValue);
		} else {
			String lVal = ((JsonString)jsonValue).getString();
			val = ConversionUtil.from(type, lVal);
		}
		return val;
	}
	
	public static Object from(String type, String value) {
		if (STRING.equals(type))
			return value;
		else if (INTEGER.equals(type))
			return Integer.valueOf(value);
		else if (SHORT.equals(type))
			return Short.valueOf(value);
		else if (LONG.equals(type))
			return Long.valueOf(value);
		else if (FLOAT.equals(type))
			return Float.valueOf(value);
		else if (DOUBLE.equals(type))
			return Double.valueOf(value);
		else if (BOOL.equals(type))
			return Boolean.valueOf(value);

		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static Object fromList(String type, JsonArray jsonArray) {
		try {
			if (type.startsWith(ARRAY)) {
				String compType = type.substring(type.indexOf('(') + 1);
				compType = compType.substring(0, compType.indexOf(')'));
				Class<?> cls = null;
				try {
					cls = Class.forName(compType);
				} catch (ClassNotFoundException ce) {
					cls = getPrimitiveClass(compType);
				}
				int sz = jsonArray.size();
				Object arr = Array.newInstance(cls, sz);
				for (int i = 0; i < sz; i++) {
					addToArray(jsonArray.get(i), arr, i);
				}
				return arr;
			} else {
				Class<?> cls = Class.forName(type);
				Collection coll = (Collection) cls.newInstance();
				Iterator<JsonValue> it = jsonArray.iterator();
				while(it.hasNext()) {
					JsonValue jVal = it.next();
					addToCollection(jVal, coll);
				}
				return coll;
			}
		} catch(Throwable e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			else
				throw new RuntimeException(e);
		}
	}
	
	private static Class<?> getPrimitiveClass(String compType) {
		if (P_INTEGER.equals(compType))
			return Integer.TYPE;
		else if (P_SHORT.equals(compType))
			return Short.TYPE;
		else if (P_LONG.equals(compType))
			return Long.TYPE;
		else if (P_FLOAT.equals(compType))
			return Float.TYPE;
		else if (P_DOUBLE.equals(compType))
			return Double.TYPE;
		else if (P_BOOL.equals(compType))
			return Boolean.TYPE;
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addToCollection(JsonValue jVal, Collection coll) {
		Statement statement = convertStatement(jVal);
		if (statement instanceof Literal) {
			Object val = ((Literal)statement).getRawValue();
			coll.add(val);
		}
	}
	
	private static void addToArray(JsonValue jVal, Object arr, int index) {
		Statement statement = convertStatement(jVal);
		if (statement instanceof Literal) {
			Object val = ((Literal)statement).getRawValue();
			Array.set(arr, index, val);
		}
	}
	
	private static Statement convertStatement(JsonValue jVal) {
		RecordedQuery rq = new RecordedQuery(false);
		JSONConverter jc = new JSONConverter();
		List<Statement> statements = new ArrayList<Statement>();
		JSONConverterAccess.readStatement(jc, jVal, statements, rq);
		return statements.get(0);
	}
}
