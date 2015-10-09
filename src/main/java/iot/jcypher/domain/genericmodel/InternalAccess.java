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

import iot.jcypher.domain.genericmodel.DOType.Builder;
import iot.jcypher.domain.genericmodel.DOType.DOClassBuilder;
import iot.jcypher.domain.genericmodel.DOType.DOEnumBuilder;
import iot.jcypher.domain.genericmodel.DOType.DOInterfaceBuilder;
import iot.jcypher.domain.genericmodel.DOType.Kind;
import iot.jcypher.domain.genericmodel.internal.DomainModel;

/**
 * For internal use only
 * @author wolfgang
 *
 */
public class InternalAccess {

	public static void setRawObject(DomainObject dObj, Object rawObject) {
		dObj.setRawObject(rawObject);
	}
	
	public static DOType createDOType(String typeName, DomainModel domainModel) {
		return new DOType(typeName, domainModel);
	}
	
	public static void setKind(Builder builder, Kind kind) {
		builder.setKind(kind);
	}
	
	public static Builder createBuilder(DOType doType) {
		return doType.createClassBuilder();
	}
	
	public static void setSuperType(Builder builder, DOType superType) {
		builder.setSuperTypeInternal(superType);
	}
	
	public static void setNodeId(DOType doType, long nid) {
		doType.setNodeId(nid);
	}
	
	public static DOField createDOField(String name, String typeName, DOType ownerType) {
		return new DOField(name, typeName, ownerType);
	}
	
	public static DOClassBuilder createClassBuilder(String typeName, DomainModel domainModel) {
		DOType doType = new DOType(typeName, domainModel);
		return doType.createClassBuilder();
	}
	
	public static DOInterfaceBuilder createInterfaceBuilder(String typeName, DomainModel domainModel) {
		DOType doType = new DOType(typeName, domainModel);
		return doType.createInterfaceBuilder();
	}
	
	public static DOEnumBuilder createEnumBuilder(String typeName, DomainModel domainModel) {
		DOType doType = new DOType(typeName, domainModel);
		return doType.createEnumBuilder();
	}
	
	public static Object getRawObject(DomainObject domainObject) {
		return domainObject.getRawObject();
	}
	
	public static void setComponentTypeName(DOField doField, String componentTypeName) {
		doField.setComponentTypeName(componentTypeName);
	}
	
	public static DomainObject createDomainObject(DOType doType) {
		return new DomainObject(doType, false); // don't add to nursery
	}
}
