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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import iot.jcypher.concurrency.Locking;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.concurrency.ConcurrencyGraphTest.QResult;
import test.domainquery.Population;
import test.domainquery.model.Person;
import test.domainquery.model.PointOfContact;
import test.domainquery.util.CompareUtil;

public class ConcurrencyTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;

	@Test
	public void testRelConcurrency_04() {

		// second client deletes object
		// first client tries to change same object

		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);

		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith = findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();

		/******* second client loading j_smith ******/
		/******* second client deleting relation ******/
		QResult res2 = ConcurrencyGraphTest.queryResult2(j_smithId,
				lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		res2.relations.get(0).remove();

		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		boolean del = ConcurrencyGraphTest.testDeleted(relId, dbAccess);
		assertTrue(del);

		/******* first client modifying j_smith ******/
		List<PointOfContact> pocs = j_smith.getPointsOfContact();
		PointOfContact poc = pocs.remove(0);
		pocs.add(poc);

		errors = da1.store(j_smith);
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals(
					"Optimistic locking failed (an element was deleted by another client)",
					error.getMessage());
			// throw new JcResultException(errors);
		}
		
		return;
	}
	
	@Test
	public void testRelConcurrency_03() {

		// second client deletes object
		// first client tries to change same object

		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);

		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith = findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();

		/******* second client loading j_smith ******/
		/******* second client deleting relation ******/
		QResult res2 = ConcurrencyGraphTest.queryResult2(j_smithId,
				lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		res2.relations.get(0).remove();

		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		boolean del = ConcurrencyGraphTest.testDeleted(relId, dbAccess);
		assertTrue(del);

		/******* first client modifying j_smith ******/
		List<PointOfContact> pocs = j_smith.getPointsOfContact();
		PointOfContact poc = pocs.remove(0);
		pocs.add(poc);

		errors = da1.store(j_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}
		
		// in the Locking.NONE case missing elements have been recreated
		
		IDomainAccess da3 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith3 = findPerson(da3, "Smith", "John");
		boolean ok = CompareUtil.equalsList(pocs, j_smith3.getPointsOfContact());
		assertTrue(ok);

		return;
	}

	@Test
	public void testRelConcurrency_02() {

		// second client changes object
		// first client tries to change same object

		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);

		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith = findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();

		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith2 = findPerson(da2, "Smith", "John");

		/******* second client modifying j_smith ******/
		List<PointOfContact> pocs2 = j_smith2.getPointsOfContact();
		PointOfContact poc2 = pocs2.remove(0);
		pocs2.add(poc2);

		List<JcError> errors = da2.store(j_smith2);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		QResult pocRes = ConcurrencyGraphTest.queryResult2(j_smithId,
				lockingStrategy, dbAccess);
		ConcurrencyGraphTest.assertVersions(0, pocRes, 1, 1, 1, 1);
		assertEquals("John", pocRes.node.getProperty("firstName").getValue()
				.toString());

		/******* first client modifying j_smith (now same poc order as j_smith2) ******/
		List<PointOfContact> pocs = j_smith.getPointsOfContact();
		PointOfContact poc = pocs.remove(0);
		pocs.add(poc);

		errors = da1.store(j_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		pocRes = ConcurrencyGraphTest.queryResult2(j_smithId, lockingStrategy,
				dbAccess);
		ConcurrencyGraphTest.assertVersions(0, pocRes, 1, 1, 1, 1);
		assertEquals("John", pocRes.node.getProperty("firstName")
				.getValue().toString());
		
		/******* first client modifying j_smith again******/
		pocs = j_smith.getPointsOfContact();
		poc = pocs.remove(0);
		pocs.add(poc);

		errors = da1.store(j_smith);
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals(
					"Optimistic locking failed (an element was changed by another client)",
					error.getMessage());
			// throw new JcResultException(errors);
		}

		pocRes = ConcurrencyGraphTest.queryResult2(j_smithId, lockingStrategy,
				dbAccess);
		ConcurrencyGraphTest.assertVersions(0, pocRes, 1, 1, 1, 1);
		assertEquals("John", pocRes.node.getProperty("firstName")
				.getValue().toString());
		
		IDomainAccess da3 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith3 = findPerson(da3, "Smith", "John");
		boolean ok = CompareUtil.equalsList(pocs2, j_smith3.getPointsOfContact());
		assertTrue(ok);
		
		return;
	}

	@Test
	public void testRelConcurrency_01() {

		// second client changes object
		// first client tries to change same object

		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);

		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith = findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();

		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith2 = findPerson(da2, "Smith", "John");

		/******* second client modifying j_smith ******/
		List<PointOfContact> pocs2 = j_smith2.getPointsOfContact();
		PointOfContact poc2 = pocs2.remove(0);
		pocs2.add(poc2);

		List<JcError> errors = da2.store(j_smith2);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		QResult pocRes = ConcurrencyGraphTest.queryResult2(j_smithId,
				lockingStrategy, dbAccess);
		ConcurrencyGraphTest.assertVersions(0, pocRes, 1, 1, 1, 1);
		assertEquals("John", pocRes.node.getProperty("firstName").getValue()
				.toString());

		/******* first client modifying j_smith (now same poc order as j_smith2) ******/
		List<PointOfContact> pocs = j_smith.getPointsOfContact();
		PointOfContact poc = pocs.remove(0);
		pocs.add(poc);

		errors = da1.store(j_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		pocRes = ConcurrencyGraphTest.queryResult2(j_smithId, lockingStrategy,
				dbAccess);
		ConcurrencyGraphTest.assertVersions(0, pocRes, 1, 1, 1, 1);
		assertEquals("John", pocRes.node.getProperty("firstName")
				.getValue().toString());
		
		/******* first client modifying j_smith again******/
		pocs = j_smith.getPointsOfContact();
		poc = pocs.remove(0);
		pocs.add(poc);

		errors = da1.store(j_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		pocRes = ConcurrencyGraphTest.queryResult2(j_smithId, lockingStrategy,
				dbAccess);
		ConcurrencyGraphTest.assertVersions(0, pocRes, 2, 2, 2, 2);
		assertEquals("John", pocRes.node.getProperty("firstName")
				.getValue().toString());
		
		IDomainAccess da3 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith3 = findPerson(da3, "Smith", "John");
		boolean ok = CompareUtil.equalsList(pocs, j_smith3.getPointsOfContact());
		assertTrue(ok);
		
		return;
	}

	@Test
	public void testConcurrency_04() {

		// second client deletes object
		// first client tries to change same object

		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);

		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith = findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();

		/******* second client loading j_smith ******/
		/******* second client deleting j_smith ******/
		QResult res2 = ConcurrencyGraphTest.queryResult(j_smithId,
				lockingStrategy, dbAccess);
		for (GrRelation relat : res2.relations) {
			relat.remove();
		}
		res2.node.remove();

		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		boolean del = ConcurrencyGraphTest.testDeleted(j_smithId, dbAccess);
		assertTrue(del);

		/******* first client modifying j_smith ******/

		j_smith.setFirstName("Johnny boy");

		errors = da1.store(j_smith);
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals(
					"Optimistic locking failed (an element was deleted by another client)",
					error.getMessage());
			// throw new JcResultException(errors);
		}

		return;
	}

	@Test
	public void testConcurrency_03() {

		// second client deletes object
		// first client tries to change same object

		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);

		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith = findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();

		/******* second client loading j_smith ******/
		/******* second client deleting j_smith ******/
		QResult res2 = ConcurrencyGraphTest.queryResult(j_smithId,
				lockingStrategy, dbAccess);
		for (GrRelation relat : res2.relations) {
			relat.remove();
		}
		res2.node.remove();

		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		boolean del = ConcurrencyGraphTest.testDeleted(j_smithId, dbAccess);
		assertTrue(del);

		/******* first client modifying j_smith ******/

		j_smith.setFirstName("Johnny boy");

		errors = da1.store(j_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		return;
	}

	@Test
	public void testConcurrency_02() {

		// second client changes object
		// first client tries to change same object

		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);

		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith = findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();

		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith2 = findPerson(da2, "Smith", "John");

		/******* second client modifying j_smith ******/
		j_smith2.setFirstName("Johnny");

		List<JcError> errors = da2.store(j_smith2);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		QResult pocRes = ConcurrencyGraphTest.queryResult2(j_smithId,
				lockingStrategy, dbAccess);
		ConcurrencyGraphTest.assertVersions(1, pocRes, 0, 0, 0, 0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue()
				.toString());

		/******* first client modifying j_smith ******/

		j_smith.setFirstName("Johnny boy");

		errors = da1.store(j_smith);
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals(
					"Optimistic locking failed (an element was changed by another client)",
					error.getMessage());
			assertEquals("element id: " + j_smithId, error.getAdditionalInfo());
			// throw new JcResultException(errors);
		}

		pocRes = ConcurrencyGraphTest.queryResult2(j_smithId, lockingStrategy,
				dbAccess);
		ConcurrencyGraphTest.assertVersions(1, pocRes, 0, 0, 0, 0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue()
				.toString());

		return;
	}

	@Test
	public void testConcurrency_01() {

		// second client changes object
		// first client tries to change same object

		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);

		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith = findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();

		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess,
				domainName).setLockingStrategy(lockingStrategy);

		Person j_smith2 = findPerson(da2, "Smith", "John");

		/******* second client modifying j_smith ******/
		j_smith2.setFirstName("Johnny");

		List<JcError> errors = da2.store(j_smith2);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		QResult pocRes = ConcurrencyGraphTest.queryResult2(j_smithId,
				lockingStrategy, dbAccess);
		ConcurrencyGraphTest.assertVersions(1, pocRes, 0, 0, 0, 0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue()
				.toString());

		/******* first client modifying j_smith ******/

		j_smith.setFirstName("Johnny boy");

		errors = da1.store(j_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			// throw new JcResultException(errors);
		}

		pocRes = ConcurrencyGraphTest.queryResult2(j_smithId, lockingStrategy,
				dbAccess);
		ConcurrencyGraphTest.assertVersions(2, pocRes, 0, 0, 0, 0);
		assertEquals("Johnny boy", pocRes.node.getProperty("firstName")
				.getValue().toString());

		return;
	}

	public static Person findPerson(IDomainAccess da, String lastName,
			String firstName) {
		DomainQuery q = da.createQuery();
		DomainObjectMatch<Person> personMatch = q.createMatch(Person.class);

		q.WHERE(personMatch.atttribute("lastName")).EQUALS(lastName);
		q.WHERE(personMatch.atttribute("firstName")).EQUALS(firstName);

		DomainQueryResult result = q.execute();

		Person person = result.resultOf(personMatch).get(0);
		return person;
	}

	@BeforeClass
	public static void before() {
		domainName = "QTEST-DOMAIN";
		Properties props = new Properties();

		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");

		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
		// dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props,
		// "neo4j", "jcypher");

		// init db
		Population population = new Population();

		storedDomainObjects = population.createPopulation();

		QueriesPrintObserver.addOutputStream(System.out);

		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY,
				ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY,
				ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.UPDATE_QUERY,
				ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.CLOSURE_QUERY,
				ContentToObserve.CYPHER);
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
