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

package test.concurrency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import iot.jcypher.concurrency.Locking;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DOType.DOClassBuilder;
import iot.jcypher.domain.genericmodel.DOTypeBuilderFactory;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.internal.DOWalker;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;
import test.AbstractTestSuite;
import test.DBAccessSettings;
import test.genericmodel.DOToString;
import test.genericmodel.LoadUtil;

public class ConcurrentGenericQueryReplayTest_2 extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	
	@Test
	public void replayGenericQuery_02() {
		Locking lockingStrategy = Locking.OPTIMISTIC;
		
		/******* first client loading smith ******/
		IGenericDomainAccess da1 = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainObject a_smith = ConcurrencyTest.findGenericPerson(da1, "Smith", "Angelina");
		DomainObject j_smith1 = ConcurrencyTest.findGenericPerson(da1, "Smith", "John");
		
		DOToString doToString = new DOToString(Format.PRETTY_1, 2);
		DOWalker walker = new DOWalker(a_smith, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		doToString = new DOToString(Format.PRETTY_1, 2);
		walker = new DOWalker(j_smith1, doToString);
		walker.walkDOGraph();
		String str_1 = doToString.getBuffer().toString();
		
		/******* another client extending model ******/
		//j_smith2.setFirstName("Johnny");
		LoadUtil.loadPeopleDomainExtension(dbAccess);
		
		/******* first client performing a query ******/
		DomainObject j_smith = ConcurrencyTest.findGenericPerson(da1, "Smith", "John");
		
		String domModel11 = ((IIntDomainAccess)da1.getDomainAccess()).getInternalDomainAccess().domainModelAsString();
		
		assertTrue(j_smith1 == j_smith);
		assertTrue(j_smith.getListFieldLength("pointsOfContact") == 4);
		assertTrue(((DomainObject)j_smith.getListFieldValue("pointsOfContact", 3)).getDomainObjectType().getName()
				.equals("mytest.model.VirtualAddress"));
		assertTrue(((String)((DomainObject)j_smith.getListFieldValue("pointsOfContact", 3))
				.getFieldValue("addressName")).equals("a virtual address"));
		
		/******* temp client loading model ******/
		IGenericDomainAccess da3 = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName)
					.setLockingStrategy(lockingStrategy);

		String domModel3 = ((IIntDomainAccess)da3.getDomainAccess()).getInternalDomainAccess().domainModelAsString();
		assertEquals(domModel3, domModel11);
		
		return;
	}
	
	@BeforeClass
	public static void before() {
		domainName = "PEOPLE-DOMAIN"; // "QTEST-DOMAIN";
		dbAccess = DBAccessSettings.createDBAccess();
		
		QueriesPrintObserver.addOutputStream(System.out);
		
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOMAIN_INFO, ContentToObserve.CYPHER);
		
		// init db
		List<JcError> errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		LoadUtil.loadPeopleDomain(dbAccess);
		
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		QueriesPrintObserver.removeAllEnabledQueries();
	}
}
