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

import iot.jcypher.domain.genericmodel.DOType.DOClassBuilder;
import iot.jcypher.domain.genericmodel.DOType.DOEnumBuilder;
import iot.jcypher.domain.genericmodel.DOType.DOInterfaceBuilder;

public interface DOTypeBuilderFactory {

	/**
	 * Create a ClassBuilder which allows to build a generic domain object type representing a <b>Class</b>.
	 * @param typeName fully qualified name e.g. <b>iot.jcypher.samples.domain.people.model.Person</b>
	 * @return a DOClassBuilder
	 */
	public DOClassBuilder createClassBuilder(String typeName);
	
	/**
	 * Create a ClassBuilder which allows to build a generic domain object type representing an <b>Interface</b>.
	 * @param typeName fully qualified name e.g. <b>iot.jcypher.samples.domain.people.model.PointOfContact</b>
	 * @return a DOInterfaceBuilder
	 */
	public DOInterfaceBuilder createInterfaceBuilder(String typeName);
	
	/**
	 * Create a ClassBuilder which allows to build a generic domain object type representing an <b>Enum</b>.
	 * @param typeName fully qualified name e.g. <b>iot.jcypher.samples.domain.people.model.Gender</b>
	 * @return a DOEnumBuilder
	 */
	public DOEnumBuilder createEnumBuilder(String typeName);
}
