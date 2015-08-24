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

package test.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import iot.jcypher.transaction.ITransaction;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainquery.Population;
import test.domainquery.model.Company;
import test.domainquery.model.Person;

public class TransactionTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static ByteArrayOutputStream queriesStream;
	private boolean threadDone;
	
	@BeforeClass
	public static void before() {
		domainName = "QTEST-DOMAIN";
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
		
		QueriesPrintObserver.addOutputStream(System.out);
		queriesStream = new ByteArrayOutputStream();
		QueriesPrintObserver.addOutputStream(queriesStream);
		
		QueriesPrintObserver.addToEnabledQueries("COUNT QUERY", ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries("DOM QUERY", ContentToObserve.CYPHER);
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		try {
			queriesStream.close();
		} catch (IOException e) {}
		queriesStream = null;
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
	}
	
	@Test
	public void testRollback() {
		IDomainAccess da;
		DomainQuery q;
		DomainQueryResult result = null;
		List<JcError> errors;
		
		errors = dbAccess.clearDatabase();
		assertEquals(0, errors.size());
		da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		Population population = new Population();
		List<Object> domObjects;
		
		/** 01 ****************************************/
		ITransaction tx = da.beginTX();
		domObjects = population.createBerghammerFamily();
		errors = da.store(domObjects);
		assertEquals(0, errors.size());
		domObjects = population.createCompanies();
		errors = da.store(domObjects);
		assertEquals(0, errors.size());
		
		tx.failure();
		errors = tx.close();
		assertEquals(0, errors.size());
		
		q = da.createQuery();
		DomainObjectMatch<Person> bergammerMatch = q.createMatch(Person.class);
		DomainObjectMatch<Company> companiesMatch = q.createMatch(Company.class);
		q.WHERE(bergammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		
		result = q.execute();
		
		List<Person> berghammer = result.resultOf(bergammerMatch);
		List<Company> companies = result.resultOf(companiesMatch);
		assertEquals(0, berghammer.size());
		assertEquals(0, companies.size());
		
		return;
	}
	
	@Test
	public void testCommit() {
		IDomainAccess da;
		DomainQuery q;
		DomainQueryResult result = null;
		List<JcError> errors;
		
		errors = dbAccess.clearDatabase();
		assertEquals(0, errors.size());
		da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		Population population = new Population();
		List<Object> domObjects;
		
		/** 01 ****************************************/
		ITransaction tx = da.beginTX();
		domObjects = population.createBerghammerFamily();
		errors = da.store(domObjects);
		assertEquals(0, errors.size());
		domObjects = population.createCompanies();
		errors = da.store(domObjects);
		assertEquals(0, errors.size());
		
		errors = tx.close();
		assertEquals(0, errors.size());
		
		q = da.createQuery();
		DomainObjectMatch<Person> bergammerMatch = q.createMatch(Person.class);
		DomainObjectMatch<Company> companiesMatch = q.createMatch(Company.class);
		q.WHERE(bergammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		
		result = q.execute();
		
		List<Person> berghammer = result.resultOf(bergammerMatch);
		List<Company> companies = result.resultOf(companiesMatch);
		assertEquals(3, berghammer.size());
		assertEquals(2, companies.size());
		
		return;
	}
	
	@Test
	public void testFailure() {
		DomainQuery q;
		DomainQueryResult result = null;
		List<JcError> errors;
		
		errors = dbAccess.clearDatabase();
		assertEquals(0, errors.size());
		final IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		Population population = new Population();
		List<Object> domObjects;
		
		/** 01 ****************************************/
		ITransaction tx = da.beginTX();
		domObjects = population.createBerghammerFamily();
		errors = da.store(domObjects);
		assertEquals(0, errors.size());
		domObjects = population.createCompanies();
		errors = da.store(domObjects);
		assertEquals(0, errors.size());
		
		tx.failure();
		errors = tx.close();
		assertEquals(0, errors.size());
		Throwable ex = null;
		try {
			errors = tx.close();
		} catch (Throwable e) {
			ex = e;
			assertEquals("transaction has already been closed", e.getMessage());
		}
		assertTrue(ex != null);
		
		/** 02 ****************************************/
		this.threadDone = false;
		final ITransaction[] txs = new ITransaction[1];
		Runnable r = new Runnable() {
			@Override
			public void run() {
				txs[0] = da.beginTX();
				synchronized (TransactionTest.this) {
					threadDone = true;
				}
				return;
			}
		};
		new Thread(r).start();
		boolean goOn = false;
		while(!goOn) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			synchronized (TransactionTest.this) {
				goOn = threadDone;
			}
		}
		
		ex = null;
		try {
			errors = txs[0].close();
		} catch (Throwable e) {
			ex = e;
			assertEquals("close() must be called from within the same thread which created this transaction", e.getMessage());
		}
		assertTrue(ex != null);
		return;
	}
	
	@Test
	public void testRollback_Commit() {
		IDomainAccess da;
		DomainQuery q;
		DomainQueryResult result = null;
		List<JcError> errors;
		
		errors = dbAccess.clearDatabase();
		assertEquals(0, errors.size());
		da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		Population population = new Population();
		List<Object> domObjects_1;
		List<Object> domObjects_2;
		
		/** 01 ****************************************/
		ITransaction tx = da.beginTX();
		domObjects_1 = population.createBerghammerFamily();
		errors = da.store(domObjects_1);
		assertEquals(0, errors.size());
		domObjects_2 = population.createCompanies();
		errors = da.store(domObjects_2);
		assertEquals(0, errors.size());
		
		tx.failure();
		errors = tx.close();
		assertEquals(0, errors.size());
		
		q = da.createQuery();
		DomainObjectMatch<Person> bergammerMatch = q.createMatch(Person.class);
		DomainObjectMatch<Company> companiesMatch = q.createMatch(Company.class);
		q.WHERE(bergammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		
		result = q.execute();
		
		List<Person> berghammer = result.resultOf(bergammerMatch);
		List<Company> companies = result.resultOf(companiesMatch);
		assertEquals(0, berghammer.size());
		assertEquals(0, companies.size());
		
		/** 02 ****************************************/
		tx = da.beginTX();
		errors = da.store(domObjects_1);
		assertEquals(0, errors.size());
		errors = da.store(domObjects_2);
		assertEquals(0, errors.size());
		
		errors = tx.close();
		assertEquals(0, errors.size());
		
		q = da.createQuery();
		bergammerMatch = q.createMatch(Person.class);
		companiesMatch = q.createMatch(Company.class);
		q.WHERE(bergammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		
		result = q.execute();
		
		berghammer = result.resultOf(bergammerMatch);
		companies = result.resultOf(companiesMatch);
		assertEquals(3, berghammer.size());
		assertEquals(2, companies.size());
		
		return;
	}
}
