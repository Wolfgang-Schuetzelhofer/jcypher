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

import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MappingUtil {
	
	public static ThreadLocal<InternalDomainAccess> internalDomainAccess =
			new ThreadLocal<InternalDomainAccess>();
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
		return isSimpleType(type);
	}
	
	public static boolean isSimpleType(Class<?> type) {
		return type.isPrimitive() ||
				String.class.isAssignableFrom(type) ||
				Number.class.isAssignableFrom(type) ||
				Boolean.class.isAssignableFrom(type) ||
				Date.class.isAssignableFrom(type) ||
				Enum.class.isAssignableFrom(type) ||
				type.isArray() && type.getComponentType().isPrimitive();
		// TODO arrays containing Date types ?
	}
	
	@SuppressWarnings("rawtypes")
	public static Object convertToProperty(Object value) {
		if (value != null) {
			if (Date.class.isAssignableFrom(value.getClass())) {
				return dateToLong((Date) value);
			} else if (Enum.class.isAssignableFrom(value.getClass())) {
				return ((Enum<?>)value).name();
			} else if (value instanceof Map) {
				Map map = (Map)value;
				if (map.isEmpty()) { // empty maps are mapped to empty lists
					return Collections.EMPTY_LIST;
				}
			}
		}
		return value;
	}
	
	/**
	 * @param value
	 * @param targetType
	 * @return
	 */
	public static Object convertFromProperty(Object value, Class<?> targetType) {
		return MappingUtil.convertFromProperty(value, targetType, null, null);
	}
	
	/**
	 * @param value
	 * @param targetType
	 * @param componentType may be null in case when targetType is not a collection,
	 * or when the listComponentType cannot be determined
	 * @param concreteFieldType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object convertFromProperty(Object value, Class<?> targetType,
			Class<?>componentType, Class<?> concreteFieldType) {
		if (value != null) {
			if (Date.class.isAssignableFrom(targetType) && value instanceof Number) {
				return longToDate(((Number)value).longValue());
			} else if (Enum.class.isAssignableFrom(targetType)) {
				Object[] enums=targetType.getEnumConstants();
				for (int i = 0; i < enums.length; i++) {
					if (((Enum<?>)enums[i]).name().equals(value.toString()))
						return enums[i];
				}
				return value;
			} else if (Collection.class.isAssignableFrom(targetType)) {
				if (componentType != null) {
					List<Object> converted = new ArrayList<>();
					Collection<Object> coll = (Collection<Object>)value;
					for (Object elem : coll) {
						converted.add(convertFromProperty(elem, componentType, null, null));
					}
					coll.clear();
					coll.addAll(converted);
				}
			} else if (targetType.isArray()) {
				if (componentType != null) {
					if (value instanceof Collection<?>) {
						List<Object> converted = new ArrayList<>();
						Collection<Object> coll = (Collection<Object>)value;
						for (Object elem : coll) {
							converted.add(convertFromProperty(elem, componentType, null, null));
						}
						if (componentType.isPrimitive()) {
							Object array = Array.newInstance(componentType, coll.size());
							for (int i = 0; i < converted.size(); i++) {
								Array.set(array, i, converted.get(i));
							}
							return array;
						} else {
							coll.clear();
							coll.addAll(converted);
						}
					}
				}
			} else if (Map.class.isAssignableFrom(targetType)) { // only possible for empty maps
				if (concreteFieldType != null) {
					try {
						return concreteFieldType.newInstance();
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				}
			} else if (targetType.equals(value.getClass())) {
				return value;
			} else if (targetType.isPrimitive()) {
				return convertToPrimitive(value, targetType);
			} else if (targetType.isAssignableFrom(value.getClass())) {
				return targetType.cast(value);
			} else if (Number.class.isAssignableFrom(targetType) && value instanceof Number) {
				return convertToDistinctNumber(value, targetType);
			}
		}
		return value;
	}
	
	private static Object convertToPrimitive(Object value, Class<?> targetType) {
		if (targetType.equals(Short.TYPE) && value instanceof Number)
			return ((Number)value).shortValue();
		else if (targetType.equals(Integer.TYPE) && value instanceof Number)
			return ((Number)value).intValue();
		else if (targetType.equals(Long.TYPE) && value instanceof Number)
			return ((Number)value).longValue();
		else if (targetType.equals(Float.TYPE) && value instanceof Number)
			return ((Number)value).floatValue();
		else if (targetType.equals(Double.TYPE) && value instanceof Number)
			return ((Number)value).doubleValue();
		else if (targetType.equals(Boolean.TYPE) && value instanceof Boolean)
			return ((Boolean)value).booleanValue();
		return value;
	}
	
	private static Object convertToDistinctNumber(Object value, Class<?> targetType) {
		if (targetType.equals(Short.class))
			return ((Number)value).shortValue();
		else if (targetType.equals(Integer.class))
			return ((Number)value).intValue();
		else if (targetType.equals(Long.class))
			return ((Number)value).longValue();
		else if (targetType.equals(Float.class))
			return ((Number)value).floatValue();
		else if (targetType.equals(Double.class))
			return ((Number)value).doubleValue();
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
