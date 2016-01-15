/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
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

package iot.jcypher.domain.internal;

import java.util.List;

import iot.jcypher.concurrency.Locking;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.SyncInfo;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DOTypeBuilderFactory;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;
import iot.jcypher.domain.internal.DomainAccessFactoryImpl.SyncType;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.query.result.JcError;
import iot.jcypher.transaction.ITransaction;

public class DomainAccessSync implements IDomainAccess, IIntDomainAccess {

	private SyncType syncType;
	private DomainAccess delegate;
	private GenericDomainAccessSync genericDomainAccess;
	
	/**
	 * @param dbAccess the graph database connection
	 * @param domainName
	 * @param domainLabelUse
	 */
	DomainAccessSync(IDBAccess dbAccess, String domainName, DomainLabelUse domainLabelUse,
			SyncType st) {
		super();
		this.syncType = st;
		this.delegate = new DomainAccess(dbAccess, domainName, domainLabelUse);
	}

	@Override
	public synchronized List<SyncInfo> getSyncInfos(List<Object> domainObjects) {
		return getDelegate().getSyncInfos(domainObjects);
	}

	@Override
	public synchronized SyncInfo getSyncInfo(Object domainObject) {
		return getDelegate().getSyncInfo(domainObject);
	}

	@Override
	public synchronized <T> List<T> loadByIds(Class<T> domainObjectClass, int resolutionDepth, long... ids) {
		return getDelegate().loadByIds(domainObjectClass, resolutionDepth, ids);
	}

	@Override
	public synchronized <T> T loadById(Class<T> domainObjectClass, int resolutionDepth, long id) {
		return getDelegate().loadById(domainObjectClass, resolutionDepth, id);
	}

	@Override
	public synchronized <T> List<T> loadByType(Class<T> domainObjectClass, int resolutionDepth, int offset, int count) {
		return getDelegate().loadByType(domainObjectClass, resolutionDepth, offset, count);
	}

	@Override
	public synchronized List<JcError> store(List<?> domainObjects) {
		return getDelegate().store(domainObjects);
	}

	@Override
	public synchronized List<JcError> store(Object domainObject) {
		return getDelegate().store(domainObject);
	}

	@Override
	public synchronized long numberOfInstancesOf(Class<?> type) {
		return getDelegate().numberOfInstancesOf(type);
	}

	@Override
	public synchronized List<Long> numberOfInstancesOf(List<Class<?>> types) {
		return getDelegate().numberOfInstancesOf(types);
	}

	@Override
	public DomainQuery createQuery() {
		return getDelegate().createQuery();
	}

	@Override
	public synchronized ITransaction beginTX() {
		return getDelegate().beginTX();
	}

	@Override
	public IDomainAccess setLockingStrategy(Locking locking) {
		getDelegate().setLockingStrategy(locking);
		return this;
	}

	@Override
	public IGenericDomainAccess getGenericDomainAccess() {
		if (this.genericDomainAccess == null)
			this.genericDomainAccess = new GenericDomainAccessSync();
		return this.genericDomainAccess;
	}

	@Override
	public InternalDomainAccess getInternalDomainAccess() {
		return getDelegate().getInternalDomainAccess();
	}

	private DomainAccess getDelegate() {
		return this.delegate;
	}
	
	/**********************************************************************/
	public class GenericDomainAccessSync implements IGenericDomainAccess, IIntDomainAccess {

		@Override
		public synchronized List<SyncInfo> getSyncInfos(List<DomainObject> domainObjects) {
			return getDelegate().getGenericDomainAccess().getSyncInfos(domainObjects);
		}

		@Override
		public synchronized SyncInfo getSyncInfo(DomainObject domainObject) {
			return getDelegate().getGenericDomainAccess().getSyncInfo(domainObject);
		}

		@Override
		public synchronized List<JcError> store(DomainObject domainObject) {
			return getDelegate().getGenericDomainAccess().store(domainObject);
		}

		@Override
		public synchronized List<JcError> store(List<DomainObject> domainObjects) {
			return getDelegate().getGenericDomainAccess().store(domainObjects);
		}

		@Override
		public synchronized List<DomainObject> loadByIds(String domainObjectClassName, int resolutionDepth, long... ids) {
			return getDelegate().getGenericDomainAccess().loadByIds(domainObjectClassName, resolutionDepth, ids);
		}

		@Override
		public synchronized DomainObject loadById(String domainObjectClassName, int resolutionDepth, long id) {
			return getDelegate().getGenericDomainAccess().loadById(domainObjectClassName, resolutionDepth, id);
		}

		@Override
		public synchronized List<DomainObject> loadByType(String domainObjectClassName, int resolutionDepth, int offset, int count) {
			return getDelegate().getGenericDomainAccess().loadByType(domainObjectClassName, resolutionDepth, offset, count);
		}

		@Override
		public synchronized long numberOfInstancesOf(String typeName) {
			return getDelegate().getGenericDomainAccess().numberOfInstancesOf(typeName);
		}

		@Override
		public synchronized List<Long> numberOfInstancesOf(List<String> typeNames) {
			return getDelegate().getGenericDomainAccess().numberOfInstancesOf(typeNames);
		}

		@Override
		public GDomainQuery createQuery() {
			return getDelegate().getGenericDomainAccess().createQuery();
		}

		@Override
		public synchronized ITransaction beginTX() {
			return getDelegate().getGenericDomainAccess().beginTX();
		}

		@Override
		public IGenericDomainAccess setLockingStrategy(Locking locking) {
			getDelegate().getGenericDomainAccess().setLockingStrategy(locking);
			return this;
		}

		@Override
		public DOTypeBuilderFactory getTypeBuilderFactory() {
			return getDelegate().getGenericDomainAccess().getTypeBuilderFactory();
		}

		@Override
		public synchronized DOType getDomainObjectType(String typeName) {
			return getDelegate().getGenericDomainAccess().getDomainObjectType(typeName);
		}

		@Override
		public IDomainAccess getDomainAccess() {
			return DomainAccessSync.this;
		}

		@Override
		public InternalDomainAccess getInternalDomainAccess() {
			return DomainAccessSync.this.getInternalDomainAccess();
		}
		
	}
}
