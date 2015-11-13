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
import test.domainquery.Population;
import test.domainquery.model.Person;
import test.domainquery.model.PointOfContact;

public class ConcurrencyTest extends AbstractTestSuite {
	
	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	
	@Test
	public void testConcurrency_04() {
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainQuery q = da1.createQuery();
		DomainObjectMatch<Person> j_smithMatch = q.createMatch(Person.class);

		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result = q.execute();
		
		Person j_smith = result.resultOf(j_smithMatch).get(0);
		
		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainQuery q2 = da2.createQuery();
		DomainObjectMatch<Person> j_smithMatch2 = q2.createMatch(Person.class);

		q2.WHERE(j_smithMatch2.atttribute("lastName")).EQUALS("Smith");
		q2.WHERE(j_smithMatch2.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result2 = q2.execute();
		
		Person j_smith2 = result2.resultOf(j_smithMatch2).get(0);
		
		/******* second client modifying j_smith pointsOfContact ******/
		List<PointOfContact> pocs2 = j_smith2.getPointsOfContact();
		PointOfContact poc2 = pocs2.remove(pocs2.size() - 1);
		pocs2.add(0, poc2);
		
		List<JcError> errors = da2.store(j_smith2);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		/******* first client modifying j_smith pointsOfContact ******/
		
		List<PointOfContact> pocs = j_smith.getPointsOfContact();
		PointOfContact poc = pocs.remove(0);
		pocs.add(poc);
		
		errors = da1.store(j_smith);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		return;
	}
	
	@Test
	public void testConcurrency_03() {
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainQuery q = da1.createQuery();
		DomainObjectMatch<Person> j_smithMatch = q.createMatch(Person.class);

		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result = q.execute();
		
		Person j_smith = result.resultOf(j_smithMatch).get(0);
		
		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainQuery q2 = da2.createQuery();
		DomainObjectMatch<Person> j_smithMatch2 = q2.createMatch(Person.class);

		q2.WHERE(j_smithMatch2.atttribute("lastName")).EQUALS("Smith");
		q2.WHERE(j_smithMatch2.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result2 = q2.execute();
		
		Person j_smith2 = result2.resultOf(j_smithMatch2).get(0);
		
		/******* second client modifying j_smith pointsOfContact ******/
		List<PointOfContact> pocs2 = j_smith2.getPointsOfContact();
		PointOfContact poc2 = pocs2.remove(pocs2.size() - 1);
		pocs2.add(0, poc2);
		
		List<JcError> errors = da2.store(j_smith2);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		/******* first client modifying j_smith pointsOfContact ******/
		
		List<PointOfContact> pocs = j_smith.getPointsOfContact();
		PointOfContact poc = pocs.remove(0);
		pocs.add(poc);
		
		errors = da1.store(j_smith);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		return;
	}
	
	@Test
	public void testConcurrency_02() {
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainQuery q = da1.createQuery();
		DomainObjectMatch<Person> j_smithMatch = q.createMatch(Person.class);

		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result = q.execute();
		
		Person j_smith = result.resultOf(j_smithMatch).get(0);
		
		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainQuery q2 = da2.createQuery();
		DomainObjectMatch<Person> j_smithMatch2 = q2.createMatch(Person.class);

		q2.WHERE(j_smithMatch2.atttribute("lastName")).EQUALS("Smith");
		q2.WHERE(j_smithMatch2.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result2 = q2.execute();
		
		Person j_smith2 = result2.resultOf(j_smithMatch2).get(0);
		
		/******* second client modifying j_smith ******/
		j_smith2.setFirstName("Johnny");
//		Address addr = (Address)j_smith2.getPointsOfContact().get(0);
//		addr.setNumber(addr.getNumber() + 1);
		
		List<JcError> errors = da2.store(j_smith2);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		/******* first client modifying j_smith ******/
		
		j_smith.setFirstName("Johnny boy");
		
		errors = da1.store(j_smith);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		return;
	}
	
	@Test
	public void testConcurrency_01() {
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(Locking.OPTIMISTIC);
		DomainQuery q = da1.createQuery();
		DomainObjectMatch<Person> j_smithMatch = q.createMatch(Person.class);

		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result = q.execute();
		
		Person j_smith = result.resultOf(j_smithMatch).get(0);
		
		/******* second client loading j_smith ******/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		DomainQuery q2 = da2.createQuery();
		DomainObjectMatch<Person> j_smithMatch2 = q2.createMatch(Person.class);

		q2.WHERE(j_smithMatch2.atttribute("lastName")).EQUALS("Smith");
		q2.WHERE(j_smithMatch2.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result2 = q2.execute();
		
		Person j_smith2 = result2.resultOf(j_smithMatch2).get(0);
		
		/******* second client modifying j_smith ******/
		j_smith2.setFirstName("Johnny");
//		Address addr = (Address)j_smith2.getPointsOfContact().get(0);
//		addr.setNumber(addr.getNumber() + 1);
		
		List<JcError> errors = da2.store(j_smith2);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		/******* first client modifying j_smith ******/
		
		j_smith.setFirstName("Johnny boy");
		
		errors = da1.store(j_smith);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		return;
	}
	
	@BeforeClass
	public static void before() {
		domainName = "QTEST-DOMAIN";
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
//		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "jcypher");
		
		// init db
		Population population = new Population();
		
		storedDomainObjects = population.createPopulation();
		
		QueriesPrintObserver.addOutputStream(System.out);
		
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.UPDATE_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.CLOSURE_QUERY, ContentToObserve.CYPHER);
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
		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		errors = da.store(storedDomainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
	}
	
}
