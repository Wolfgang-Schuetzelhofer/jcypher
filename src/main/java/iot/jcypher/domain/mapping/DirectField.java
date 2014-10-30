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

import java.lang.reflect.Field;

public class DirectField implements IField {

	private Field field;
	
	public DirectField(Field field) {
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
	public Object get(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return this.field.get(obj);
	}

	@Override
	public void set(Object target, Object value) throws IllegalArgumentException, IllegalAccessException {
		this.field.set(target, value);
	}

	@Override
	public Class<?> getType() {
		return this.field.getType();
	}

	@Override
	public Class<?> getDeclaringClass() {
		return this.field.getDeclaringClass();
	}

}
