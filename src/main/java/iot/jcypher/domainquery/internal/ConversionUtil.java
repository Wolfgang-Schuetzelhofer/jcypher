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

public class ConversionUtil {

	private static final String STRING = "java.lang.String";
	private static final String INTEGER = "java.lang.Integer";
	private static final String SHORT = "java.lang.Short";
	private static final String LONG = "java.lang.Long";
	private static final String FLOAT = "java.lang.Float";
	private static final String DOUBLE = "java.lang.Double";
	private static final String BOOL = "java.lang.Boolean";
	
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
}
