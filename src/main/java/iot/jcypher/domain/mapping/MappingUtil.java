/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
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

package iot.jcypher.domain.mapping;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MappingUtil {
	
	private static SimpleDateFormat simpleDateFormat;

	public static String dateToString(Date date) {
		return getSimpleDateFormat().format(date);
	}
	
	public static Date stringToDate(String date) {
		try {
			return getSimpleDateFormat().parse(date);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static long dateToLong(Date date) {
		return date.getTime();
	}
	
	public static Date longToDate(long millis) {
		return new Date(millis);
	}
	
	public static boolean mapsToProperty(Class<?> type) {
		return type.isPrimitive() ||
				String.class.isAssignableFrom(type) ||
				Number.class.isAssignableFrom(type) ||
				Boolean.class.isAssignableFrom(type) ||
				Date.class.isAssignableFrom(type);
	}
	
	public static boolean convertsToProperty(Class<?> type) {
		return Date.class.isAssignableFrom(type);
	}
	
	public static Object convertToProperty(Object value) {
		if (value != null) {
			if (Date.class.isAssignableFrom(value.getClass())) {
				return dateToLong((Date) value);
			}
		}
		return value;
	}
	
	public static Object convertFromProperty(Object value, Class<?> targetType) {
		if (value != null) {
			if (Date.class.isAssignableFrom(targetType) && value instanceof Number) {
				return longToDate(((Number)value).longValue());
			}
		}
		return value;
	}
	
	private static SimpleDateFormat getSimpleDateFormat() {
		if (simpleDateFormat == null) {
			simpleDateFormat =
					new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS", new Locale("de", "AT"));
		}
		return simpleDateFormat;
	}
}
