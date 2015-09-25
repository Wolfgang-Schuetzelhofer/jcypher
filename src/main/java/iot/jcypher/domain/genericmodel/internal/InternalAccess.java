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

package iot.jcypher.domain.genericmodel.internal;

import iot.jcypher.domain.genericmodel.DOType;

/**
 * For internal use only
 * @author wolfgang
 *
 */
public class InternalAccess {

	public static DomainModel createDomainModel(String domainName, String domainLabel) {
		return new DomainModel(domainName, domainLabel);
	}
	
	public static void addDOTypeIfNeeded(DomainModel domainModel, DOType doType) {
		domainModel.addDOTypeIfNeeded(doType);
	}
}
