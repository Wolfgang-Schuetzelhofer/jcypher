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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
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
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;
import test.AbstractTestSuite;
import test.DBAccessSettings;
import test.domainquery.Population;
import test.domainquery.model.Person;
import test.domainquery.model.PoBox;
import test.domainquery.model.PointOfContact;
import test.domainquery.model.VirtualAddress;
import util.TestDataReader;

public class ModelAndInfoReloadTest extends AbstractTestSuite {
	
	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	private static ByteArrayOutputStream queriesStream;

	@Test
	public void testReloadModelAndInfo_01() {
		String qCypher;
		Locking lockingStrategy = Locking.OPTIMISTIC;
		
		TestDataReader tdr = new TestDataReader("/test/concurrency/Test_CONCURRENCY_01.txt");

		/******* first client loading j_smith ******/
		queriesStream.reset();
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);
		
		Person jer_smith = ConcurrencyTest.findPerson(da1, "Smith", "Jeremy");
		
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery("CONCURRENCY_01", qCypher, tdr.getTestData("CONCURRENCY_01"));
		
		String domModel = ((IIntDomainAccess)da1).getInternalDomainAccess().domainModelAsString();
		//System.out.println(domModel);
		assertEquals("MODEL_01", tdr.getTestData("MODEL_01"), domModel);
		
		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);
		Person j_smith2 = ConcurrencyTest.findPerson(da2, "Smith", "John");
		
		/******* third client loading smith ******/
		IDomainAccess da3 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);
		Person a_smith = ConcurrencyTest.findPerson(da3, "Smith", "Angelina");
		
		/******* second client extending model ******/
		queriesStream.reset();
		PoBox poBox = new PoBox();
		poBox.setNumber(12345);
		List<PointOfContact> pocs = j_smith2.getPointsOfContact();
		pocs.add(poBox);
		List<JcError> errors = da2.store(j_smith2);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}
		
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery("CONCURRENCY_02", qCypher, tdr.getTestData("CONCURRENCY_02"));
		
		domModel = ((IIntDomainAccess)da2).getInternalDomainAccess().domainModelAsString();
		//System.out.println(domModel);
		assertEquals("MODEL_02", tdr.getTestData("MODEL_02"), domModel);
		
		/******* first client extending model ******/
		queriesStream.reset();
		poBox = new PoBox();
		poBox.setNumber(456789);
		pocs = jer_smith.getPointsOfContact();
		pocs.add(poBox);
		errors = da1.store(jer_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}
		
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery("CONCURRENCY_03", qCypher, tdr.getTestData("CONCURRENCY_03"));
		
		String domModel2 = ((IIntDomainAccess)da1).getInternalDomainAccess().domainModelAsString();
		//System.out.println(domModel);
		assertEquals("MODEL_04", tdr.getTestData("MODEL_04"), domModel2);
		
		/******* third client extending model ******/
		queriesStream.reset();
		VirtualAddress va = new VirtualAddress();
		va.setAddressName("a virtual address");
		pocs = a_smith.getPointsOfContact();
		pocs.add(va);
		errors = da3.store(a_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}
		
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery("CONCURRENCY_04", qCypher, tdr.getTestData("CONCURRENCY_04"));
		
		domModel = ((IIntDomainAccess)da3).getInternalDomainAccess().domainModelAsString();
		//System.out.println(domModel);
		assertEquals("MODEL_03", tdr.getTestData("MODEL_03"), domModel);
		
		/******* fourth client loading smith ******/
		IDomainAccess da4 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);
		a_smith = ConcurrencyTest.findPerson(da4, "Smith", "Angelina");
		jer_smith = ConcurrencyTest.findPerson(da4, "Smith", "Jeremy");
		
		assertEquals(2, a_smith.getPointsOfContact().size());
		assertEquals(2, jer_smith.getPointsOfContact().size());
		
		assertTrue(a_smith.getPointsOfContact().get(1) instanceof VirtualAddress);
		assertTrue(jer_smith.getPointsOfContact().get(1) instanceof PoBox);
		
		assertTrue(((VirtualAddress)a_smith.getPointsOfContact().get(1)).getAddressName().equals("a virtual address"));
		assertTrue(((PoBox)jer_smith.getPointsOfContact().get(1)).getNumber() == 456789);
		
		return;
	}
	
	@BeforeClass
	public static void before() {
		domainName = "QTEST-DOMAIN";
		dbAccess = DBAccessSettings.createDBAccess();
	
		Population population = new Population();
	
		storedDomainObjects = population.createPopulation();
	
		QueriesPrintObserver.addOutputStream(System.out);
		queriesStream = new ByteArrayOutputStream();
		QueriesPrintObserver.addOutputStream(queriesStream);
	
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOMAIN_INFO,
				ContentToObserve.CYPHER);
		
		initDB(Locking.OPTIMISTIC);
	}

	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		storedDomainObjects = null;
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
	}

	private static void initDB(Locking lockingStrategy) {
		List<JcError> errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);
		errors = da.store(storedDomainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
	}
	
}
