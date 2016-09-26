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

public class ConcurrentGenericQueryReplayTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	
	@Test
	public void replayGenericQuery_01() {
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
		
		/******* second client loading j_smith ******/
		IGenericDomainAccess da2 = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainObject j_smith2 = ConcurrencyTest.findGenericPerson(da2, "Smith", "John");
		
		/******* second client extending model ******/
		//j_smith2.setFirstName("Johnny");
		DOType pointOfContact = da2.getDomainObjectType("iot.jcypher.samples.domain.people.model.PointOfContact");
		
		DOTypeBuilderFactory tpf = da2.getTypeBuilderFactory();
		
		DOClassBuilder poBoxBuilder = tpf.createClassBuilder("mytest.model.PoBox");
		poBoxBuilder.addInterface(pointOfContact);
		poBoxBuilder.addField("number", int.class.getName());
		DOType poBoxType = poBoxBuilder.build();
		
		DomainObject poBox = new DomainObject(poBoxType);
		poBox.setFieldValue("number", 12345);
		
		int pocLength = j_smith2.getListFieldLength("pointsOfContact");
		j_smith2.addListFieldValue("pointsOfContact", poBox);
		
		pocLength = j_smith2.getListFieldLength("pointsOfContact");
		assertTrue(pocLength == 4);
		
		List<JcError> errors = da2.store(j_smith2);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}
		String domModel2 = ((IIntDomainAccess)da2.getDomainAccess()).getInternalDomainAccess().domainModelAsString();
		String domModel1 = ((IIntDomainAccess)da1.getDomainAccess()).getInternalDomainAccess().domainModelAsString();
		
		assertFalse(domModel2.equals(domModel1));
		
		/******* temp client loading smith ******/
		IGenericDomainAccess da3 = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName)
					.setLockingStrategy(lockingStrategy);
		DomainObject j_smithTemp = ConcurrencyTest.findGenericPerson(da3, "Smith", "John");
		
		assertTrue(j_smith1.getListFieldLength("pointsOfContact") == 3);
		assertTrue(j_smithTemp.getListFieldLength("pointsOfContact") == 4);
		assertTrue(((DomainObject)j_smithTemp.getListFieldValue("pointsOfContact", 3)).getDomainObjectType().getName()
				.equals("mytest.model.PoBox"));
		assertTrue(((Number)((DomainObject)j_smithTemp.getListFieldValue("pointsOfContact", 3))
				.getFieldValue("number")).intValue() == 12345);
		
		/******* first client performing a query ******/
		DomainObject j_smith = ConcurrencyTest.findGenericPerson(da1, "Smith", "John");
		
		String domModel11 = ((IIntDomainAccess)da1.getDomainAccess()).getInternalDomainAccess().domainModelAsString();
		assertEquals(domModel2, domModel11);
		
		assertTrue(j_smith1 == j_smith);
		assertTrue(j_smith.getListFieldLength("pointsOfContact") == 4);
		assertTrue(((DomainObject)j_smith.getListFieldValue("pointsOfContact", 3)).getDomainObjectType().getName()
				.equals("mytest.model.PoBox"));
		assertTrue(((Number)((DomainObject)j_smith.getListFieldValue("pointsOfContact", 3))
				.getFieldValue("number")).intValue() == 12345);
		
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
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.UPDATE_QUERY, ContentToObserve.CYPHER);
		
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
