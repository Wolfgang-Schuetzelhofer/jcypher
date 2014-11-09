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

import iot.jcypher.query.result.JcError;

import java.util.List;

public interface IDomainAccess {

	public abstract List<SyncInfo> getSyncInfos(List<Object> domainObjects);

	public abstract SyncInfo getSyncInfo(Object domainObject);

	public <T> List<T> loadByIds(Class<T> domainObjectClass, int resolutionDepth, long... ids);

	public <T> T loadById(Class<T> domainObjectClass, int resolutionDepth, long id);
	
	public <T> List<T> loadByType(Class<T> domainObjectClass, int resolutionDepth, int offset, int count);

	public abstract List<JcError> store(List<Object> domainObjects);

	public abstract List<JcError> store(Object domainObject);
	
	/**
	 * answer the number of instances of a certain type stored in the domain graph
	 * @param type
	 * @return the number of instances
	 */
	public long numberOfInstancesOf(Class<?> type);
	
	/**
	 * answer the numbers of instances of the specified types stored in the domain graph
	 * @param types a list of types
	 * @return a list containing the instances counts
	 */
	public List<Long> numberOfInstancesOf(List<Class<?>> types);

}
