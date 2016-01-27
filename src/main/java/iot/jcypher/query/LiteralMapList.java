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

package iot.jcypher.query;

import java.math.BigDecimal;
import java.util.ArrayList;

import iot.jcypher.query.values.JcPrimitive;
import iot.jcypher.query.values.ValueAccess;

/**
 * A list of literal maps
 *
 */
public class LiteralMapList extends ArrayList<LiteralMap> {

	private static final long serialVersionUID = 1L;

	LiteralMapList() {
		super();
	}

	LiteralMapList(int initialSize) {
		super(initialSize);
	}

	/**
	 * Answer a LiteralMapList containing only literal maps with the given key and value
	 * @param key
	 * @param value
	 * @return
	 */
	public LiteralMapList select(JcPrimitive key, Object value) {
		LiteralMapList ret = new LiteralMapList();
		for (LiteralMap lm : this) {
			if (isEqual(value, lm.get(ValueAccess.getName(key))))
				ret.add(lm);
		}
		return ret;
	}
	
	/**
	 * Answer a LiteralMapList containing only literal maps with the given key and value
	 * @param key
	 * @param value
	 * @return
	 */
	public LiteralMapList select(String key, Object value) {
		LiteralMapList ret = new LiteralMapList();
		for (LiteralMap lm : this) {
			if (isEqual(value, lm.get(key)))
				ret.add(lm);
		}
		return ret;
	}
	
	/**
	 * Answer the first literal map with the given key and value
	 * @param key
	 * @param value
	 * @return
	 */
	public LiteralMap selectFirst(JcPrimitive key, Object value) {
		for (LiteralMap lm : this) {
			if (isEqual(value, lm.get(ValueAccess.getName(key))))
				return lm;
		}
		return null;
	}
	
	/**
	 * Answer the first literal map with the given key and value
	 * @param key
	 * @param value
	 * @return
	 */
	public LiteralMap selectFirst(String key, Object value) {
		for (LiteralMap lm : this) {
			if (isEqual(value, lm.get(key)))
				return lm;
		}
		return null;
	}
	
	private boolean isEqual(Object value, Object mapValue) {
		if (mapValue instanceof BigDecimal) {
			if (value instanceof Integer)
				return ((Number)value).intValue() == ((BigDecimal)mapValue).intValue();
			else if (value instanceof Long)
				return ((Number)value).longValue() == ((BigDecimal)mapValue).longValue();
			else if (value instanceof Short)
				return ((Number)value).shortValue() == ((BigDecimal)mapValue).shortValue();
			else if (value instanceof Byte)
				return ((Number)value).byteValue() == ((BigDecimal)mapValue).byteValue();
			else if (value instanceof Double)
				return ((Number)value).doubleValue() == ((BigDecimal)mapValue).doubleValue();
			else if (value instanceof Float)
				return ((Number)value).floatValue() == ((BigDecimal)mapValue).floatValue();
		}
		return value.equals(mapValue);
	}
}
