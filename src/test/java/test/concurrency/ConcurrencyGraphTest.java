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
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
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

public class ConcurrencyGraphTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	
	@Test
	public void testGraphRelConcurrency_08() {
		
		// second client deletes relation
		// first client tries to delete same relation
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, res, 0,0,0,0);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult2(j_smithId, lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		
		/******* second client modifying relation ******/
		res2.relations.get(0).remove();
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(relId, dbAccess);
		assertTrue(del);
		
		/******* first client deleting relation ******/
		assertEquals(relId, res.relations.get(0).getId());
		res.relations.get(0).remove();
		
		errors = res.graph.store();
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals("Optimistic locking failed (an element was deleted by another client)", error.getMessage());
			//throw new JcResultException(errors);
		}
		
		del = testDeleted(relId, dbAccess);
		assertTrue(del);
		
		return;
	}
	
	@Test
	public void testGraphRelConcurrency_07() {
		
		// second client deletes relation
		// first client tries to delete same relation
		
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, res, 0,0,0,0);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult2(j_smithId, lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		
		/******* second client modifying relation ******/
		res2.relations.get(0).remove();
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(relId, dbAccess);
		assertTrue(del);
		
		/******* first client deleting relation ******/
		assertEquals(relId, res.relations.get(0).getId());
		res.relations.get(0).remove();
		
		errors = res.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		del = testDeleted(relId, dbAccess);
		assertTrue(del);
		
		return;
	}
	
	@Test
	public void testGraphRelConcurrency_06() {
		
		// second client changes relation
		// first client tries to delete same relation
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, res, 0,0,0,0);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult2(j_smithId, lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		
		/******* second client modifying relation ******/
		GrProperty prop2 = res2.relations.get(0).getProperty("key");
		prop2.setValue(100);
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 1,0,0,0);
		assertEquals(100, ((Number)pocRes.relations.get(0).getProperty("key").getValue()).intValue());
		
		/******* first client deleting relation ******/
		assertEquals(relId, res.relations.get(0).getId());
		res.relations.get(0).remove();
		
		errors = res.graph.store();
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals("Optimistic locking failed (an element was changed by another client)", error.getMessage());
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 1,0,0,0);
		assertEquals(100, ((Number)pocRes.relations.get(0).getProperty("key").getValue()).intValue());
		
		return;
	}
	
	@Test
	public void testGraphRelConcurrency_05() {
		
		// second client changes relation
		// first client tries to delete same relation
		
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, res, 0,0,0,0);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult2(j_smithId, lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		
		/******* second client modifying relation ******/
		GrProperty prop2 = res2.relations.get(0).getProperty("key");
		prop2.setValue(100);
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 1,0,0,0);
		assertEquals(100, ((Number)pocRes.relations.get(0).getProperty("key").getValue()).intValue());
		
		/******* first client deleting relation ******/
		assertEquals(relId, res.relations.get(0).getId());
		res.relations.get(0).remove();
		
		errors = res.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(res.relations.get(0).getId(), dbAccess);
		assertTrue(del);
		
		return;
	}
	
	@Test
	public void testGraphRelConcurrency_04() {
		
		// second client deletes relation
		// first client tries to change same relation
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, res, 0,0,0,0);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult2(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client deleting relation ******/
		long relId = res2.relations.get(0).getId();
		res2.relations.get(0).remove();
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(relId, dbAccess);
		assertTrue(del);
		
		/******* first client modifying relation ******/
		assertEquals(relId, res.relations.get(0).getId());
		GrProperty prop = res.relations.get(0).getProperty("key");
		prop.setValue(101);
		
		errors = res.graph.store();
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals("Optimistic locking failed (an element was deleted by another client)", error.getMessage());
			assertEquals("element id: " + res.relations.get(0).getId(), error.getAdditionalInfo());
			//throw new JcResultException(errors);
		}
		
		del = testDeleted(res.relations.get(0).getId(), dbAccess);
		assertTrue(del);
		
		return;
	}
	
	/**
	 * Note: Locking.NONE: changes to a changed relation will be applied,
	 * changes to a deleted relation will be ignored, the relation is still deleted
	 */
	@Test
	public void testGraphRelConcurrency_03() {
		
		// second client deletes relation
		// first client tries to change same relation
		
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, res, 0,0,0,0);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult2(j_smithId, lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		
		/******* second client deleting relation ******/
		res2.relations.get(0).remove();
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(relId, dbAccess);
		assertTrue(del);
		
		/******* first client modifying relation ******/
		assertEquals(relId, res.relations.get(0).getId());
		GrProperty prop = res.relations.get(0).getProperty("key");
		prop.setValue(101);
		
		errors = res.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		del = testDeleted(res.relations.get(0).getId(), dbAccess);
		assertTrue(del);
		
		return;
	}
	
	@Test
	public void testGraphRelConcurrency_02() {
		
		// second client changes relation
		// first client tries to change same relation
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, res, 0,0,0,0);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult2(j_smithId, lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		
		/******* second client modifying relation ******/
		GrProperty prop2 = res2.relations.get(0).getProperty("key");
		prop2.setValue(100);
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 1,0,0,0);
		assertEquals(100, ((Number)pocRes.relations.get(0).getProperty("key").getValue()).intValue());
		
		/******* first client modifying relation ******/
		assertEquals(relId, res.relations.get(0).getId());
		GrProperty prop = res.relations.get(0).getProperty("key");
		prop.setValue(101);
		
		errors = res.graph.store();
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals("Optimistic locking failed (an element was changed by another client)", error.getMessage());
			assertEquals("element id: " + res.relations.get(0).getId(), error.getAdditionalInfo());
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 1,0,0,0);
		assertEquals(100, ((Number)pocRes.relations.get(0).getProperty("key").getValue()).intValue());
		
		return;
	}
	
	@Test
	public void testGraphRelConcurrency_01() {
		
		// second client changes relation
		// first client tries to change same relation
		
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, res, 0,0,0,0);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult2(j_smithId, lockingStrategy, dbAccess);
		long relId = res2.relations.get(0).getId();
		
		/******* second client modifying relation ******/
		GrProperty prop2 = res2.relations.get(0).getProperty("key");
		prop2.setValue(100);
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 1,0,0,0);
		assertEquals(100, ((Number)pocRes.relations.get(0).getProperty("key").getValue()).intValue());
		
		/******* first client modifying relation ******/
		assertEquals(relId, res.relations.get(0).getId());
		GrProperty prop = res.relations.get(0).getProperty("key");
		prop.setValue(101);
		
		errors = res.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 2,0,0,0);
		assertEquals(101, ((Number)pocRes.relations.get(0).getProperty("key").getValue()).intValue());
		
		return;
	}
	
	@Test
	public void testGraphConcurrency_08() {
		
		// second client deletes node
		// first client tries to delete same node
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 0,0,0,0);
		
		QResult res = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client deletes j_smith ******/
		for (GrRelation relat : res2.relations) {
			relat.remove();
		}
		res2.node.remove();
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		/******* first client trying to delete j_smith ******/
		for (GrRelation relat : res.relations) {
			relat.remove();
		}
		res.node.remove();
		
		errors = res.graph.store();
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals("Optimistic locking failed (an element was deleted by another client)", error.getMessage());
			//throw new JcResultException(errors);
		}
		
		del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		return;
	}
	
	@Test
	public void testGraphConcurrency_07() {
		
		// second client deletes node
		// first client tries to delete same node
		
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 0,0,0,0);
		
		QResult res = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client deletes j_smith ******/
		for (GrRelation relat : res2.relations) {
			relat.remove();
		}
		res2.node.remove();
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		/******* first client trying to delete j_smith ******/
		for (GrRelation relat : res.relations) {
			relat.remove();
		}
		res.node.remove();
		
		errors = res.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		return;
	}
	
	@Test
	public void testGraphConcurrency_06() {
		
		// second client changes node
		// first client tries to delete same node
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 0,0,0,0);
		
		QResult res = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client modifying j_smith ******/
		GrProperty prop2 = res2.node.getProperty("firstName");
		prop2.setValue("Johnny");
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(1, pocRes, 0,0,0,0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue().toString());
		
		/******* first client trying to delete j_smith ******/
		for (GrRelation relat : res.relations) {
			relat.remove();
		}
		res.node.remove();
		
		errors = res.graph.store();
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals("Optimistic locking failed (an element was changed by another client)", error.getMessage());
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(1, pocRes, 0,0,0,0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue().toString());
		
		return;
	}
	
	@Test
	public void testGraphConcurrency_05() {
		
		// second client changes node
		// first client tries to delete same node
		
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 0,0,0,0);
		
		QResult res = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client modifying j_smith ******/
		GrProperty prop2 = res2.node.getProperty("firstName");
		prop2.setValue("Johnny");
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(1, pocRes, 0,0,0,0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue().toString());
		
		/******* first client trying to delete j_smith ******/
		for (GrRelation relat : res.relations) {
			relat.remove();
		}
		res.node.remove();
		
		errors = res.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		return;
	}
	
	@Test
	public void testGraphConcurrency_04() {
		
		// second client deletes node (+ adjacent relations)
		// first client tries to change same node
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* modifying j_smith to increment version ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 0,0,0,0);
		
		List<PointOfContact> pocs = j_smith.getPointsOfContact();
		PointOfContact poc = pocs.remove(0);
		pocs.add(poc);
		j_smith.setFirstName("Johnny");
		List<JcError> errors = da1.store(j_smith);
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(1, pocRes, 1,1,1,1);
		
		/******* first client loading j_smith ******/
		QResult res = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client deleting j_smith ******/
		for (GrRelation relat : res2.relations) {
			relat.remove();
		}
		res2.node.remove();
		
		errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		/******* first client modifying j_smith ******/
		GrProperty prop = res.node.getProperty("firstName");
		prop.setValue("Johnny Boy");
		
		errors = res.graph.store();
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals("Optimistic locking failed (an element was deleted by another client)", error.getMessage());
			assertEquals("element id: " + j_smithId, error.getAdditionalInfo());
			//throw new JcResultException(errors);
		}
		
		del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		return;
	}
	
	/**
	 * Note: Locking.NONE: changes to a changed node will be applied,
	 * changes to a deleted node will be ignored, the node is still deleted
	 */
	@Test
	public void testGraphConcurrency_03() {
		
		// second client deletes node (+ adjacent relations)
		// first client tries to change same node
		
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 0,0,0,0);
		
		QResult res = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client deleting j_smith ******/
		for (GrRelation relat : res2.relations) {
			relat.remove();
		}
		res2.node.remove();
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		boolean del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		/******* first client modifying j_smith ******/
		GrProperty prop = res.node.getProperty("firstName");
		prop.setValue("Johnny Boy");
		
		errors = res.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		del = testDeleted(j_smithId, dbAccess);
		assertTrue(del);
		
		return;
	}
	
	@Test
	public void testGraphConcurrency_02() {
		
		// second client changes node
		// first client tries to change same node
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 0,0,0,0);
		
		QResult res = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client modifying j_smith ******/
		GrProperty prop2 = res2.node.getProperty("firstName");
		prop2.setValue("Johnny");
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(1, pocRes, 0,0,0,0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue().toString());
		
		/******* first client modifying j_smith ******/
		GrProperty prop = res.node.getProperty("firstName");
		prop.setValue("Johnny Boy");
		
		errors = res.graph.store();
		assertTrue(!errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			JcError error = errors.get(0);
			assertEquals("Optimistic locking failed (an element was changed by another client)", error.getMessage());
			assertEquals("element id: " + j_smithId, error.getAdditionalInfo());
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(1, pocRes, 0,0,0,0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue().toString());
		
		return;
	}
	
	@Test
	public void testGraphConcurrency_01() {
		
		// second client changes node
		// first client tries to change same node
		
		Locking lockingStrategy = Locking.NONE;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(0, pocRes, 0,0,0,0);
		
		QResult res = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy, dbAccess);
		
		/******* second client modifying j_smith ******/
		GrProperty prop2 = res2.node.getProperty("firstName");
		prop2.setValue("Johnny");
		
		List<JcError> errors = res2.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(1, pocRes, 0,0,0,0);
		assertEquals("Johnny", pocRes.node.getProperty("firstName").getValue().toString());
		
		/******* first client modifying j_smith ******/
		GrProperty prop = res.node.getProperty("firstName");
		prop.setValue("Johnny Boy");
		
		errors = res.graph.store();
		assertTrue(errors.isEmpty());
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		pocRes = queryResult2(j_smithId, lockingStrategy, dbAccess);
		assertVersions(2, pocRes, 0,0,0,0);
		assertEquals("Johnny Boy", pocRes.node.getProperty("firstName").getValue().toString());
		
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
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
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
	
	/**
	 * return a node, the node's relations, and the node's adjacent nodes.
	 * @param personId
	 * @param lockingStrategy
	 * @return
	 */
	static QResult queryResult(long personId, Locking lockingStrategy, IDBAccess dba) {
		QResult res = new QResult();
		
		JcNode persN = new JcNode("a");
		JcRelation pocsR = new JcRelation("r");
		JcNode pocsN = new JcNode("b");
		IClause[] clauses = new IClause[] {
				MATCH.node(persN).relation(pocsR).node(pocsN),
				WHERE.valueOf(persN.id()).EQUALS(personId),
				RETURN.ALL()
		};
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		//String qStr = print(query, Format.PRETTY_1);
		JcQueryResult result = dba.execute(query);
		res.node = result.resultOf(persN).get(0);
		res.relations = result.resultOf(pocsR);
		res.relatedNodes = result.resultOf(pocsN);
		res.graph = result.getGraph().setLockingStrategy(lockingStrategy);
		
		return res;
	}
	
	/**
	 * return a node, the node's pointOfContact relations, and the node's PointsOfContact.
	 * @param personId
	 * @param lockingStrategy
	 * @return
	 */
	static QResult queryResult2(long personId, Locking lockingStrategy, IDBAccess dba) {
		QResult res = new QResult();
		
		JcNode persN = new JcNode("a");
		JcRelation pocsR = new JcRelation("r");
		JcNode pocsN = new JcNode("b");
		IClause[] clauses = new IClause[] {
				MATCH.node(persN).relation().type("pointsOfContact").out().node()
					.relation(pocsR).out().node(pocsN),
				WHERE.valueOf(persN.id()).EQUALS(personId),
				RETURN.ALL()
		};
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		//String qStr = print(query, Format.PRETTY_1);
		JcQueryResult result = dba.execute(query);
		res.node = result.resultOf(persN).get(0);
		res.relations = result.resultOf(pocsR);
		res.relatedNodes = result.resultOf(pocsN);
		res.graph = result.getGraph().setLockingStrategy(lockingStrategy);
		
		return res;
	}
	
	static boolean testDeleted(long nodeId, IDBAccess dba) {
		JcNode a = new JcNode("a");
		IClause[] clauses = new IClause[] {
				OPTIONAL_MATCH.node(a),
				WHERE.valueOf(a.id()).EQUALS(nodeId),
				RETURN.ALL()
		};
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		//String qStr = print(query, Format.PRETTY_1);
		JcQueryResult result = dba.execute(query);
		List<GrNode> ares = result.resultOf(a);
		return ares.size() == 1 && ares.get(0) == null;
	}
	
	static void assertVersions(int nodeVersion, QResult qResult, int... relationVersions) {
		assertEquals(relationVersions.length, qResult.relations.size());
		assertEquals(nodeVersion, ((Number)qResult.node.getProperty("_c_version_").getValue()).intValue());
		for (int i = 0; i < relationVersions.length; i++) {
			assertEquals("at index: " + i, relationVersions[i], ((Number)qResult.relations.get(i).getProperty("_c_version_").getValue()).intValue());
		}
	}
	
	/*********************************/
	static class QResult {
		GrNode node;
		List<GrRelation> relations;
		private List<GrNode> relatedNodes;
		Graph graph;
	}
}
