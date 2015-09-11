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

package iot.jcypher.domain;

import iot.jcypher.domain.genericmodel.DomainObject;

import java.util.List;

/**
 * Provides methods to access a domain graph
 * based on a generic domain model.
 *
 */
public interface IGenericDomainAccess {
	
	/**
	 * Load a list of generic domain objects from the graph database.
	 * By means of the second parameter you can specify the resolution depth.
	 * 0 would mean you only load simple attributes into the objects (i.e. attributes which have been stored
	 * to a node property; strings, numbers, booleans, or dates).
	 * Complex attributes are stored in separate nodes accessed via relations.
	 * So with the second parameter you can specify how deep exactly
	 * the graph should be navigated when loading objects.
	 * -1 means resolution as deep as possible (until leafs of the graph,
	 * i.e. objects which at most have simple attributes, are reached or until a cycle (loop) is detected).
	 * @param domainObjectClassName
	 * @param resolutionDepth
	 * @param ids a list of object ids (i.e. the ids of the nodes in the graph to which the objects were mapped).
	 * @return a list of domain objects
	 */
	public List<DomainObject> loadByIds(String domainObjectClassName, int resolutionDepth, long... ids);

	/**
	 * Load a generic domain object from the graph database.
	 * By means of the second parameter you can specify the resolution depth.
	 * 0 would mean you only load simple attributes into the object (i.e. attributes which have been stored
	 * to a node property; strings, numbers, booleans, or dates).
	 * Complex attributes are stored in separate nodes accessed via relations.
	 * So with the second parameter you can specify how deep exactly
	 * the graph should be navigated when loading the object.
	 * -1 means resolution as deep as possible (until leafs of the graph,
	 * i.e. objects which at most have simple attributes, are reached or until a cycle (loop) is detected).
	 * @param domainObjectClassName
	 * @param resolutionDepth
	 * @param id an object id (i.e. the id of the node in the graph to which the object was mapped).
	 * @return a domain object
	 */
	public DomainObject loadById(String domainObjectClassName, int resolutionDepth, long id);
	
	/**
	 * All objects of the type specified by domainObjectClassName or of any subtype
	 * will be loaded as generic domain objects from the domain graph and returned in a list.
	 * You can even use 'java.lang.Object' in which case all domain objects in the graph will be returned.
	 * You also can specify the resolution depth.
	 * Additionally, you can specify an offset (parameter 3) and a count (parameter 4) of objects to be returned
	 * with respect to a list containing the total number of objects of the specified type.
	 * This feature provides the possibility to do pagination.
	 * Offset 0 and count -1 will return a list of all objeccts of the specified type.
	 * To really make use of the pagination feature you need to know the total number of objects of a certain type.
	 * The method 'numberOfInstancesOf(...)' provides this information
	 * @param domainObjectClassName specifies the type of objects that should be loaded.
	 * @param resolutionDepth
	 * @param offset
	 * @param count
	 * @return a list of domain objects.
	 */
	public List<DomainObject> loadByType(String domainObjectClassName, int resolutionDepth, int offset, int count);
	
	/**
	 * Answer a domain access object.
	 * @return an IDomainAccess.
	 */
	public IDomainAccess getDomainAccess();

}
