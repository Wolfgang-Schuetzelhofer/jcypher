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
	
	public static Object from(String type, String value) {
		if (STRING.equals(type))
			return value;
		else if (INTEGER.equals(type))
			return Integer.valueOf(value);

		return null;
	}
}
