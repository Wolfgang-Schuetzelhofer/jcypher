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

package test.queryrecorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.internal.DOWalker;
import iot.jcypher.domainquery.CountQueryResult;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.domainquery.internal.RecordedQueryPlayer;
import iot.jcypher.domainquery.internal.Settings;
import iot.jcypher.domainquery.internal.QueryRecorder.QueriesPerThread;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
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
import test.genericmodel.LoadUtil;
import util.TestDataReader;

//@Ignore
public class GenericQueryRecorderTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	
	@BeforeClass
	public static void before() {
		Settings.TEST_MODE = true;
		domainName = "PEOPLE-DOMAIN"; // "QTEST-DOMAIN";
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
//		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "jcypher");
		
		QueriesPrintObserver.addOutputStream(System.out);
		
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOMAIN_INFO, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.CLOSURE_QUERY, ContentToObserve.CYPHER);
		
		// init db
		initDB();
		return;
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		QueriesPrintObserver.removeFromEnabledQueries(QueryToObserve.DOMAIN_INFO);
		QueriesPrintObserver.removeFromEnabledQueries(QueryToObserve.CLOSURE_QUERY);
		Settings.TEST_MODE = false;
	}
	
	private static void initDB() {
		List<JcError> errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		LoadUtil.loadPeopleDomain(dbAccess);
	}
	
	private void addAddress() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q = gda.createQuery();
		DomainObjectMatch<DomainObject> j_smithMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		DomainObjectMatch<DomainObject> munichMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Area");
		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		q.WHERE(munichMatch.atttribute("name")).EQUALS("Munich");
		
		DomainQueryResult result = q.execute();
		
		DomainObject j_smith = result.resultOf(j_smithMatch).get(0);
		DomainObject munich = result.resultOf(munichMatch).get(0);
		
		DOType addressType = gda.getDomainObjectType("iot.jcypher.samples.domain.people.model.Address");
		DomainObject address = new DomainObject(addressType);
		address.setFieldValue("street", "Karlsplatz Stachus");
		address.setFieldValue("number", 1);
		address.setFieldValue("area", munich);
		
		j_smith.addListFieldValue("pointsOfContact", address);
		
		List<JcError> errors = gda.store(j_smith);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
//		List<JcError> errors = dbAccess.clearDatabase();
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
//		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
//		errors = da.store(storedDomainObjects);
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
		return;
	}
	
	@Test
	public void testGenericQueryConcatenation_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q, q1;
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_GenericQueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_08";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		q = gda.createQuery();
		QueriesPerThread qpt = QueryRecorder.getQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DomainObject> smith = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.execute();
		
		q1 = gda.createQuery();
		RecordedQuery recordedQuery_1 = QueryRecorder.getRecordedQuery(q1);
		DomainObjectMatch<DomainObject> j_smith = q1.createMatchFrom(smith);
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result1 = q1.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n").append(recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery_1.toString());
		GDomainQuery q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		GDomainQuery q3 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery_1, gda);
		RecordedQuery recordedQuery_3 = QueryRecorder.getRecordedQuery(q3);
		QueryRecorder.queryCompleted(q3);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery_1.toString(), recordedQuery_3.toString());
		
		/** 02 ****************************************/
		gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		q = gda.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		smith = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		q.ORDER(smith).BY("firstName");
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		DomainQueryResult result = q.execute();
		assertTrue(qpt.isCleared());
		
		List<DomainObject> smithResult = result.resultOf(smith);
		
		q1 = gda.createQuery();
		recordedQuery_1 = QueryRecorder.getRecordedQuery(q1);
		j_smith = q1.createMatchFor(smithResult, "iot.jcypher.samples.domain.people.model.Subject");
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		result1 = q1.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n\n").append(recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery_1.toString());
		q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		q3 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery_1, gda);
		recordedQuery_3 = QueryRecorder.getRecordedQuery(q3);
		QueryRecorder.queryCompleted(q3);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery_1.toString(), recordedQuery_3.toString());
		
		/** new ****************************************/
		gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		q = gda.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		smith = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(smith.atttribute("firstName")).EQUALS("John");
		result = q.execute();
		assertTrue(qpt.isCleared());
		
		smithResult = result.resultOf(smith);
		
		q1 = gda.createQuery();
		recordedQuery_1 = QueryRecorder.getRecordedQuery(q1);
		j_smith = q1.createMatchFor(smithResult.get(0));
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		result1 = q1.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n\n").append(recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery_1.toString());
		q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		q3 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery_1, gda);
		recordedQuery_3 = QueryRecorder.getRecordedQuery(q3);
		QueryRecorder.queryCompleted(q3);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery_1.toString(), recordedQuery_3.toString());
		
		return;
	}
	
	@Test
	public void testGenericQueryIntersection_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_GenericQueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_07";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		q = gda.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DomainObject> personsMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Person");

		DomainObjectMatch<DomainObject> m_childrenMatch = q.TRAVERSE_FROM(personsMatch).FORTH("mother")
				.BACK("mother").TO_GENERIC("iot.jcypher.samples.domain.people.model.Person");
		DomainObjectMatch<DomainObject> f_childrenMatch = q.TRAVERSE_FROM(personsMatch).FORTH("father")
				.BACK("father").TO_GENERIC("iot.jcypher.samples.domain.people.model.Person");
		@SuppressWarnings("unchecked")
		DomainObjectMatch<DomainObject> siblingsMatch = q.INTERSECTION(m_childrenMatch, f_childrenMatch);
		
		DomainObjectMatch<DomainObject> siblings1Match = q.SELECT_FROM(personsMatch).ELEMENTS(
				q.WHERE(siblingsMatch.COUNT()).EQUALS(1)
		);
		q.ORDER(siblings1Match).BY("lastName");
		q.ORDER(siblings1Match).BY("firstName");
		DomainObjectMatch<DomainObject> siblings2Match = q.SELECT_FROM(personsMatch).ELEMENTS(
				q.WHERE(siblingsMatch.COUNT()).EQUALS(2)
		);
		q.ORDER(siblings2Match).BY("lastName");
		q.ORDER(siblings2Match).BY("firstName");
		DomainObjectMatch<DomainObject> siblings1PlusMatch = q.SELECT_FROM(personsMatch).ELEMENTS(
				q.WHERE(siblingsMatch.COUNT()).GTE(1)
		);
		q.ORDER(siblings1PlusMatch).BY("lastName");
		q.ORDER(siblings1PlusMatch).BY("firstName");
		DomainQueryResult result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n").append(recordedQuery.toString());
		GDomainQuery q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testGenericQueryUnion_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_GenericQueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_06";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		q = gda.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DomainObject> smithMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		DomainObjectMatch<DomainObject> bergHammerMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		
		q.WHERE(smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(bergHammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		DomainObjectMatch<DomainObject> unionMatch = q.UNION(smithMatch, bergHammerMatch);
		q.ORDER(unionMatch).BY("lastName");
		q.ORDER(unionMatch).BY("firstName");
		
		DomainQueryResult result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n").append(recordedQuery.toString());
		GDomainQuery q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 02 ****************************************/
		gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		
		q = gda.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DomainObject> j_smith = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<DomainObject> j_smith_AddressesMatch =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO_GENERIC("iot.jcypher.samples.domain.people.model.PointOfContact");
		DomainObjectMatch<DomainObject> j_smith_d_Areas =
				q.TRAVERSE_FROM(j_smith_AddressesMatch).FORTH("area")
				.TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		DomainObjectMatch<DomainObject> j_smith_Areas =
				q.TRAVERSE_FROM(j_smith_AddressesMatch).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		@SuppressWarnings("unchecked")
		DomainObjectMatch<DomainObject> j_smith_all_Areas = q.UNION(j_smith_d_Areas, j_smith_Areas);
		
		DomainObjectMatch<DomainObject> j_smith_Filtered_4 =
			q.SELECT_FROM(j_smith_AddressesMatch).ELEMENTS(
				q.WHERE(j_smith_all_Areas.COUNT()).EQUALS(4)
		);
		DomainObjectMatch<DomainObject> j_smith_Filtered_5 =
				q.SELECT_FROM(j_smith_AddressesMatch).ELEMENTS(
					q.WHERE(j_smith_all_Areas.COUNT()).EQUALS(5)
			);
		result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n\n").append(recordedQuery.toString());
		q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testGenericQueryCollect_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_GenericQueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_05";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		
		q = gda.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DomainObject> subjectsMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		DomainObjectMatch<DomainObject> subjectsMatch2 = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		
		q.ORDER(subjectsMatch).BY("firstName");
		q.ORDER(subjectsMatch2).BY("lastName").DESCENDING();
		DomainObjectMatch<String> firstNamesMatch = q.COLLECT(subjectsMatch.atttribute("firstName"))
				.AS(String.class);
		firstNamesMatch.setPage(0, 5);
		DomainObjectMatch<String> lastNamesMatch = q.COLLECT(subjectsMatch2.atttribute("lastName"))
				.AS(String.class);
		
		CountQueryResult countResult = q.executeCount();
		DomainQueryResult result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n").append(recordedQuery.toString());
		GDomainQuery q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testGenericQueryReject_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_GenericQueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_04";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		q = gda.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DomainObject> j_smith_p = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		DomainObjectMatch<DomainObject> europe_a = q.createMatch("iot.jcypher.samples.domain.people.model.Area");
		
		q.WHERE(europe_a.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith_p.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith_p.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<DomainObject> j_smith_Addresses_p = q.TRAVERSE_FROM(j_smith_p).FORTH("pointsOfContact")
			.TO_GENERIC("iot.jcypher.samples.domain.people.model.Address");
		q.ORDER(j_smith_Addresses_p).BY("street");
		 DomainObjectMatch<DomainObject> j_smith_Areas_a = q.TRAVERSE_FROM(j_smith_Addresses_p).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		
		DomainObjectMatch<DomainObject> j_smith_FilteredPocs_p =
				q.REJECT_FROM(j_smith_Addresses_p).ELEMENTS(
						q.WHERE(j_smith_Areas_a).CONTAINS(europe_a)
		);
		
		DomainQueryResult result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n").append(recordedQuery.toString());
		GDomainQuery q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testGenericQuerySelect_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_GenericQueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_03";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		q = gda.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		
		DomainObjectMatch<DomainObject> j_smith = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		DomainObjectMatch<DomainObject> europe = q.createMatch("iot.jcypher.samples.domain.people.model.Area");
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<DomainObject> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact")
					.TO_GENERIC("iot.jcypher.samples.domain.people.model.PointOfContact");
		DomainObjectMatch<DomainObject> j_smith_Areas =
				q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
					.FORTH("partOf").DISTANCE(0, -1).TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		
		DomainObjectMatch<DomainObject> j_smith_FilteredPocs =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_Areas).CONTAINS(europe)
					);
		DomainQueryResult result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n").append(recordedQuery.toString());
		GDomainQuery q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 02 ****************************************/
		q = gda.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DomainObject> subjects = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		DomainObjectMatch<DomainObject> allSubjects = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		q.ORDER(allSubjects).BY("lastName");
		q.ORDER(allSubjects).BY("firstName");
		
		DomainObjectMatch<DomainObject> addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact")
				.TO_GENERIC("iot.jcypher.samples.domain.people.model.PointOfContact");
		DomainObjectMatch<DomainObject> areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		
		DomainObjectMatch<DomainObject> num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.atttribute("number")).EQUALS(20),
				q.OR(),
				q.WHERE(areas.atttribute("name")).EQUALS("Europe")
			);
		q.ORDER(num_addresses).BY("lastName");
		q.ORDER(num_addresses).BY("firstName");
		
		result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n\n").append(recordedQuery.toString());
		q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testGenericQueryTraverse_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_GenericQueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_02";
		StringBuilder sb = new StringBuilder();
		
		/****** Forward-Backward Traversal ***************/
		// create a DomainQuery object
		q = gda.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		// create a DomainObjectMatch for objects of type Person
		DomainObjectMatch<DomainObject> j_smithMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Person");

		// Constrain the set of Persons to contain
		// John Smith only
		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		
		// Start with 'John Smith'
		// (j_smithMatch is constraint to match 'John Smith' only),
		// navigate forward via attribute 'pointsOfContact',
		// this will lead to 'John Smith's' Address(es),
		// then navigate backward via attribute 'pointsOfContact',
		// end matching objects of type Person.
		// This will lead to all other persons living at 'John Smith's' Address(es).
		DomainObjectMatch<DomainObject> j_smith_residentsMatch =
				q.TRAVERSE_FROM(j_smithMatch).FORTH("pointsOfContact")
					.BACK("pointsOfContact").TO_GENERIC("iot.jcypher.samples.domain.people.model.Person");
		q.ORDER(j_smith_residentsMatch).BY("lastName");
		q.ORDER(j_smith_residentsMatch).BY("firstName");
		
		// execute the query
		DomainQueryResult result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n").append(recordedQuery.toString());
		GDomainQuery q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testGenericQuery_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_GenericQueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_02";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		q = gda.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DomainObject> j_smithMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<DomainObject> j_smith_AddressesMatch =
				q.TRAVERSE_FROM(j_smithMatch).FORTH("pointsOfContact")
					.TO_GENERIC("iot.jcypher.samples.domain.people.model.PointOfContact");
		q.ORDER(j_smith_AddressesMatch).BY("street").DESCENDING();
		
		DomainQueryResult result = q.execute();
		assertTrue(qpt.isCleared());
		
		sb.append("\n").append(recordedQuery.toString());
		GDomainQuery q2 = new RecordedQueryPlayer().replayGenericQuery(recordedQuery, gda);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
}
