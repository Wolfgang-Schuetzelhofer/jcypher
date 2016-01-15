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

import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IDomainAccess.DomainLabelUse;
import iot.jcypher.domain.IDomainAccessFactory;
import iot.jcypher.domain.IGenericDomainAccess;

public class DomainAccessFactoryImpl implements IDomainAccessFactory {

	private SyncType syncType;
	
	public DomainAccessFactoryImpl(SyncType st) {
		super();
		this.syncType = st;
	}

	@Override
	public IDomainAccess createDomainAccess(IDBAccess dbAccess, String domainName) {
		if (this.syncType == SyncType.NONE)
			return new DomainAccess(dbAccess, domainName, DomainLabelUse.AUTO);
		else
			return new DomainAccessSync(dbAccess, domainName, DomainLabelUse.AUTO, this.syncType);
	}

	@Override
	public IDomainAccess createDomainAccess(IDBAccess dbAccess, String domainName, DomainLabelUse domainLabelUse) {
		if (this.syncType == SyncType.NONE)
			return new DomainAccess(dbAccess, domainName, domainLabelUse);
		else
			return new DomainAccessSync(dbAccess, domainName, domainLabelUse, this.syncType);
	}

	@Override
	public IGenericDomainAccess createGenericDomainAccess(IDBAccess dbAccess, String domainName) {
		if (this.syncType == SyncType.NONE)
			return new DomainAccess(dbAccess, domainName, DomainLabelUse.AUTO).getGenericDomainAccess();
		else
			return new DomainAccessSync(dbAccess, domainName, DomainLabelUse.AUTO, this.syncType).getGenericDomainAccess();
	}

	@Override
	public IGenericDomainAccess createGenericDomainAccess(IDBAccess dbAccess, String domainName,
			DomainLabelUse domainLabelUse) {
		if (this.syncType == SyncType.NONE)
			return new DomainAccess(dbAccess, domainName, domainLabelUse).getGenericDomainAccess();
		else
			return new DomainAccessSync(dbAccess, domainName, domainLabelUse, this.syncType).getGenericDomainAccess();
	}

	/***************************************/
	public enum SyncType {
		NONE, SYNCHRONIZED
	}
}
