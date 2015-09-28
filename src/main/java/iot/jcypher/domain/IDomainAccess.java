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

package iot.jcypher.domain;

import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.query.result.JcError;
import iot.jcypher.transaction.ITransaction;

import java.util.List;

/**
 * Provides methods to access a domain graph.
 *
 */
public interface IDomainAccess {

	/**
	 * For every domain object stored in the graph database a SyncInfo object can be retrieved.
	 *  A SyncInfo can be asked for the object's id (i.e. the id of the node in the graph to which the object was mapped).
	 *  A SyncInfo can be asked for the object's resolution depth, which can be 'SHALLOW' or 'DEEP'.
	 * @param domainObjects a list of domain objects that have been stored in the domain graph.
	 * @return a list of SyncInfo objects, one for every object in domainObjects
	 */
	public abstract List<SyncInfo> getSyncInfos(List<Object> domainObjects);

	/**
	 * For every domain object stored in the graph database a SyncInfo object can be retrieved.
	 *  A SyncInfo can be asked for the object's id (i.e. the id of the node in the graph to which the object was mapped).
	 *  A SyncInfo can be asked for the object's resolution depth, which can be 'SHALLOW' or 'DEEP'.
	 * @param domainObject a domain object that has been stored in the domain graph.
	 * @return a SyncInfo object
	 */
	public abstract SyncInfo getSyncInfo(Object domainObject);

	/**
	 * Load a list of domain objects from the graph database.
	 * By means of the second parameter you can specify the resolution depth.
	 * 0 would mean you only load simple attributes into the objects (i.e. attributes which have been stored
	 * to a node property; strings, numbers, booleans, or dates).
	 * Complex attributes are stored in separate nodes accessed via relations.
	 * So with the second parameter you can specify how deep exactly
	 * the graph should be navigated when loading objects.
	 * -1 means resolution as deep as possible (until leafs of the graph,
	 * i.e. objects which at most have simple attributes, are reached or until a cycle (loop) is detected).
	 * @param domainObjectClass
	 * @param resolutionDepth
	 * @param ids a list of object ids (i.e. the ids of the nodes in the graph to which the objects were mapped).
	 * @return a list of domain objects
	 */
	public <T> List<T> loadByIds(Class<T> domainObjectClass, int resolutionDepth, long... ids);

	/**
	 * Load a domain object from the graph database.
	 * By means of the second parameter you can specify the resolution depth.
	 * 0 would mean you only load simple attributes into the object (i.e. attributes which have been stored
	 * to a node property; strings, numbers, booleans, or dates).
	 * Complex attributes are stored in separate nodes accessed via relations.
	 * So with the second parameter you can specify how deep exactly
	 * the graph should be navigated when loading the object.
	 * -1 means resolution as deep as possible (until leafs of the graph,
	 * i.e. objects which at most have simple attributes, are reached or until a cycle (loop) is detected).
	 * @param domainObjectClass
	 * @param resolutionDepth
	 * @param id an object id (i.e. the id of the node in the graph to which the object was mapped).
	 * @return a domain object
	 */
	public <T> T loadById(Class<T> domainObjectClass, int resolutionDepth, long id);
	
	/**
	 * All objects of the specified type or any subtype of it
	 * will be loaded from the domain graph and returned in a list.
	 * You can even use 'Object.class' in which case all domain objects in the graph will be returned.
	 * Again you can specify the resolution depth.
	 * Additionally, you can specify an offset (parameter 3) and a count (parameter 4) of objects to be returned
	 * with respect to a list containing the total number of objects of the specified type.
	 * This feature provides the possibility to do pagination.
	 * Offset 0 and count -1 will return a list of all objeccts of the specified type.
	 * To really make use of the pagination feature you need to know the total number of objects of a certain type.
	 * The method 'numberOfInstancesOf(...)' provides this information
	 * @param domainObjectClass specifies the type of objects that should be loaded.
	 * @param resolutionDepth
	 * @param offset
	 * @param count
	 * @return a list of domain objects.
	 */
	public <T> List<T> loadByType(Class<T> domainObjectClass, int resolutionDepth, int offset, int count);

	/**
	 * Store a list of domain objects. The entire object graphs rooted by the domain objects
	 * are mapped to the graph database.
	 * @param domainObjects
	 * @return a possibly emty list of errors.
	 */
	public abstract List<JcError> store(List<?> domainObjects);

	/**
	 * Store a domain object. The entire object graph rooted by the domain object
	 * is mapped to the graph database.
	 * @param domainObject
	 * @return a possibly emty list of errors.
	 */
	public abstract List<JcError> store(Object domainObject);
	
	/**
	 * answer the number of instances of a certain type and of all of that type's subtypes stored in the domain graph
	 * @param type
	 * @return the number of instances
	 */
	public long numberOfInstancesOf(Class<?> type);
	
	/**
	 * answer the numbers of instances of the specified types and of all of these type's subtypes stored in the domain graph
	 * @param types a list of types
	 * @return a list containing the instances counts
	 */
	public List<Long> numberOfInstancesOf(List<Class<?>> types);
	
	/**
	 * create a new domain query
	 * @return a DomainQuery
	 */
	public DomainQuery createQuery();
	
	/**
	 * create a transaction
	 * @return an instance of ITransaction
	 */
	public ITransaction beginTX();
	
	/**
	 * Answer a domain access object which works with a generic domain model.
	 * @return an IGenericDomainAccess.
	 */
	public IGenericDomainAccess getGenericDomainAccess();
	
	public enum DomainLabelUse {
		AUTO, ALWAYS, NEVER
	}

}
