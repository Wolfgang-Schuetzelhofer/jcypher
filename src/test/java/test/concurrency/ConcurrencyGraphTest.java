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
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.writer.Format;
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

public class ConcurrencyGraphTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	
	@Test
	public void testGraphConcurrency_04() {
		
		// second client deletes node (+ adjacent relations)
		// first client tries to change same node
		
		Locking lockingStrategy = Locking.OPTIMISTIC;
		initDB(lockingStrategy);
		
		/******* first client loading j_smith ******/
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName)
				.setLockingStrategy(lockingStrategy);
		
		Person j_smith = ConcurrencyTest.findPerson(da1, "Smith", "John");
		long j_smithId = da1.getSyncInfo(j_smith).getId();
		
		QResult res = queryResult(j_smithId, lockingStrategy);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy);
		
		/******* second client deleting j_smith ******/
		for (GrRelation relat : res2.pocRelations) {
			relat.remove();
		}
		res2.person.remove();
		
		List<JcError> errors = res2.graph.store();
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		/******* first client modifying j_smith ******/
		GrProperty prop = res.person.getProperty("firstName");
		prop.setValue("Johnny Boy");
		
		errors = res.graph.store();
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		return;
	}
	
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
		
		QResult res = queryResult(j_smithId, lockingStrategy);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy);
		
		/******* second client deleting j_smith ******/
		for (GrRelation relat : res2.pocRelations) {
			relat.remove();
		}
		res2.person.remove();
		
		List<JcError> errors = res2.graph.store();
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		/******* first client modifying j_smith ******/
		GrProperty prop = res.person.getProperty("firstName");
		prop.setValue("Johnny Boy");
		
		errors = res.graph.store();
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
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
		
		QResult res = queryResult(j_smithId, lockingStrategy);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy);
		
		/******* second client modifying j_smith ******/
		GrProperty prop2 = res2.person.getProperty("firstName");
		prop2.setValue("Johnny");
		
		List<JcError> errors = res2.graph.store();
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		/******* first client modifying j_smith ******/
		GrProperty prop = res.person.getProperty("firstName");
		prop.setValue("Johnny Boy");
		
		errors = res.graph.store();
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
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
		
		QResult res = queryResult(j_smithId, lockingStrategy);
		
		/******* second client loading j_smith ******/
		QResult res2 = queryResult(j_smithId, lockingStrategy);
		
		/******* second client modifying j_smith ******/
		GrProperty prop2 = res2.person.getProperty("firstName");
		prop2.setValue("Johnny");
		
		List<JcError> errors = res2.graph.store();
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
		}
		
		/******* first client modifying j_smith ******/
		GrProperty prop = res.person.getProperty("firstName");
		prop.setValue("Johnny Boy");
		
		errors = res.graph.store();
		if (errors.size() > 0) {
			printErrors(errors);
			//throw new JcResultException(errors);
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
	
	private QResult queryResult(long personId, Locking lockingStrategy) {
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
		JcQueryResult result = dbAccess.execute(query);
		res.person = result.resultOf(persN).get(0);
		res.pocRelations = result.resultOf(pocsR);
		res.pocs = result.resultOf(pocsN);
		res.graph = result.getGraph().setLockingStrategy(lockingStrategy);
		
		return res;
	}
	
	/*********************************/
	private static class QResult {
		private GrNode person;
		private List<GrRelation> pocRelations;
		private List<GrNode> pocs;
		private Graph graph;
	}
}
