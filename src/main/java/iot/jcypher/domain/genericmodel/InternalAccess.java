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
import iot.jcypher.domain.genericmodel.DOType.Kind;

/**
 * For internal use only
 * @author wolfgang
 *
 */
public class InternalAccess {

	public static void setRawObject(DomainObject dObj, Object rawObject) {
		dObj.setRawObject(rawObject);
	}
	
	public static DOType createDOType(String typeName) {
		return new DOType(typeName);
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
	
	public static DomainModel createDomainModel(String domainName, String domainLabel) {
		return new DomainModel(domainName, domainLabel);
	}
}
