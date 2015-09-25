/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.domain.genericmodel;

import java.lang.reflect.Field;

import iot.jcypher.domain.genericmodel.internal.DomainModel;

public class DOField {
	
	private String name;
	private String typeName;
	private boolean buidInType;
	private DOType ownerType;
	private Field field;
	
	/**
	 * Create a field i.e. an attribute defined in a domain object type.
	 * @param name
	 * @param typeName qualified type name
	 */
	DOField(String name, String typeName, DOType ownerType) {
		super();
		this.name = name;
		this.typeName = typeName;
		this.buidInType = DomainModel.isBuildIn(typeName);
		this.ownerType = ownerType;
	}

	public String getName() {
		return name;
	}

	public String getTypeName() {
		return typeName;
	}

	public boolean isBuidInType() {
		return buidInType;
	}
	
	void setValue(Object target, Object value) {
		try {
			getField().set(target, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	Object getValue(Object target) {
		try {
			return getField().get(target);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String asString(String indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent);
		sb.append(this.name);
		sb.append(" : ");
		sb.append(this.typeName);
		sb.append(" (buildIn: ");
		sb.append(this.buidInType);
		sb.append(')');
		return sb.toString();
	}
	
	private Field getField() {
		if (this.field == null) {
			Class<?> rawType;
			try {
				rawType = this.ownerType.getRawType();
				this.field = rawType.getDeclaredField(this.name);
				this.field.setAccessible(true);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return this.field;
	}

}
