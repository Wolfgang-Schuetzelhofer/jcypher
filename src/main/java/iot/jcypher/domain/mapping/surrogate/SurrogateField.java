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

package iot.jcypher.domain.mapping.surrogate;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import iot.jcypher.domain.mapping.IField;
import iot.jcypher.domain.mapping.MappingUtil;

public class SurrogateField implements IField {

	private Field field;
	
	public SurrogateField(Field field) {
		super();
		this.field = field;
	}

	@Override
	public String getName() {
		return this.field.getName();
	}

	@Override
	public void setAccessible(boolean b) {
		this.field.setAccessible(b);
	}

	@Override
	public Object get(Object target) throws IllegalArgumentException,
			IllegalAccessException {
		Object obj = this.field.get(target);
		if (obj != null) {
			return MappingUtil.internalDomainAccess.get().getDomainState()
							.getSurrogateState().getCreateSurrogateFor(obj, getSurrogateClass(obj));
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends AbstractSurrogate> Class<T> getSurrogateClass(Object obj) {
		if (obj instanceof Map<?, ?>)
			return (Class<T>) iot.jcypher.domain.mapping.surrogate.Map.class;
		else if (obj instanceof Collection<?>)
			return (Class<T>) iot.jcypher.domain.mapping.surrogate.Collection.class;
		else if (obj instanceof Object[])
			return (Class<T>) iot.jcypher.domain.mapping.surrogate.Array.class;
		return null;
	}

	@Override
	public void set(Object target, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		AbstractSurrogate surrogate = (AbstractSurrogate) value;
		this.field.set(target, surrogate.getContent());
	}

	@Override
	public Class<?> getType() {
		if (Map.class.isAssignableFrom(this.field.getType()))
				return iot.jcypher.domain.mapping.surrogate.Map.class;
		else if (Collection.class.isAssignableFrom(this.field.getType()))
				return iot.jcypher.domain.mapping.surrogate.Collection.class;
		else if (this.field.getType().isArray())
			return iot.jcypher.domain.mapping.surrogate.Array.class;
		return null;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return this.field.getDeclaringClass();
	}

}
