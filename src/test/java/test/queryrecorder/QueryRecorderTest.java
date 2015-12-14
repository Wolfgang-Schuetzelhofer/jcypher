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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.domainquery.internal.QueryRecorder.QueriesPerThread;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.domainquery.internal.RecordedQueryPlayer;
import iot.jcypher.domainquery.internal.Settings;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import test.AbstractTestSuite;
import test.domainquery.Population;
import test.domainquery.model.AbstractArea;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.AreaType;
import test.domainquery.model.DateHolder;
import test.domainquery.model.NumberHolder;
import test.domainquery.model.Person;
import test.domainquery.model.PointOfContact;
import test.domainquery.model.Subject;
import test.domainquery.util.CompareUtil;
import util.TestDataReader;

public class QueryRecorderTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	private static RecordedQuery reccordedQuery_14;
	
	@Test
	public void testRecordQuery_14() {
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_14";
		StringBuilder sb = new StringBuilder();
		
		Population population = new Population();
		List<Object> domObjects = population.createPopulation();
		
		/************************************************/
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(reccordedQuery_14, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		
		DomainQueryResult result = q2.execute();
		
		DomainObjectMatch<?> j_smith_FilteredPocs = q2.getReplayedQueryContext().getById("obj25");
		List<?> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		
		assertEquals(1, j_smith_FilteredPoCsResult.size());
		boolean equals = CompareUtil.equalsObjects(population.getMarketStreet_20(), j_smith_FilteredPoCsResult.get(0));
		assertTrue(equals);
		
		return;
	}
	
	@Test
	public void testRecordQuery_13() {
		//DomainQueryTest.testDomainQuery_Concatenation_01()
		//DomainQueryTest.testDomainQuery_Concatenation_02()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_13";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Person> smith = q.createMatch(Person.class);
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		//result = q.execute();
		
		DomainQuery q1 = da1.createQuery();
		RecordedQuery recordedQuery_1 = QueryRecorder.getRecordedQuery(q1);
		DomainObjectMatch<Person> j_smith = q1.createMatchFrom(smith);
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		QueryRecorder.queryCompleted(q);
		QueryRecorder.queryCompleted(q1);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery_1.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		DomainQuery q3 = new RecordedQueryPlayer().replayQuery(recordedQuery_1, da1);
		RecordedQuery recordedQuery3 = QueryRecorder.getRecordedQuery(q3);
		QueryRecorder.queryCompleted(q3);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery_1.toString(), recordedQuery3.toString());
		
		//DomainQueryTest.testDomainQuery_Concatenation_02()
		/** 01 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		smith = q.createMatch(Person.class);
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		DomainQueryResult result = q.execute();
		
		List<Person> smithResult = result.resultOf(smith);
		
		q1 = da1.createQuery();
		recordedQuery_1 = QueryRecorder.getRecordedQuery(q1);
		j_smith = q1.createMatchFor(smithResult, Person.class);
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		QueryRecorder.queryCompleted(q);
		QueryRecorder.queryCompleted(q1);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery_1.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		q3 = new RecordedQueryPlayer().replayQuery(recordedQuery_1, da1);
		recordedQuery3 = QueryRecorder.getRecordedQuery(q3);
		QueryRecorder.queryCompleted(q3);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery_1.toString(), recordedQuery3.toString());
		
		/** new: test empty queries per thread with execute query *********/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		smith = q.createMatch(Person.class);
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		result = q.execute();
		
		smithResult = result.resultOf(smith);
		
		q1 = da1.createQuery();
		recordedQuery_1 = QueryRecorder.getRecordedQuery(q1);
		j_smith = q1.createMatchFor(smithResult.get(0));
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		QueryRecorder.queryCompleted(q);
		QueryRecorder.queryCompleted(q1);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery_1.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		q3 = new RecordedQueryPlayer().replayQuery(recordedQuery_1, da1);
		recordedQuery3 = QueryRecorder.getRecordedQuery(q3);
		QueryRecorder.queryCompleted(q3);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery_1.toString(), recordedQuery3.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_12() {
		//DomainQueryTest.testDomainQuery_Union_Intersection_01()
		//DomainQueryTest.testDomainQuery_Union_Intersection_05()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_12";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> smithMatch = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> bergHammerMatch = q.createMatch(Subject.class);
		
		q.WHERE(smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(bergHammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		DomainObjectMatch<Subject> unionMatch = q.UNION(smithMatch, bergHammerMatch);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 02 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> smith_ChristaMatch = q.createMatch(Subject.class);
		bergHammerMatch = q.createMatch(Subject.class);
		
		q.WHERE(smith_ChristaMatch.atttribute("lastName")).EQUALS("Smith");
		q.OR();
		q.BR_OPEN();
			q.WHERE(smith_ChristaMatch.atttribute("lastName")).EQUALS("Berghammer");
			q.WHERE(smith_ChristaMatch.atttribute("firstName")).EQUALS("Christa");
		q.BR_CLOSE();
		
		q.WHERE(bergHammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		
		DomainObjectMatch<Subject> intersectionMatch = q.INTERSECTION(smith_ChristaMatch, bergHammerMatch);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		//DomainQueryTest.testDomainQuery_Union_Intersection_05()
		/** 01 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Person> personsMatch = q.createMatch(Person.class);

		DomainObjectMatch<Person> m_childrenMatch = q.TRAVERSE_FROM(personsMatch).FORTH("mother")
				.BACK("mother").TO(Person.class);
		DomainObjectMatch<Person> f_childrenMatch = q.TRAVERSE_FROM(personsMatch).FORTH("father")
				.BACK("father").TO(Person.class);
		DomainObjectMatch<Person> siblingsMatch = q.INTERSECTION(m_childrenMatch, f_childrenMatch);
		
		DomainObjectMatch<Person> siblings2Match = q.SELECT_FROM(personsMatch).ELEMENTS(
				q.WHERE(siblingsMatch.COUNT()).GTE(2)
		);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_11() {
		//DomainQueryTest.testDomainQuery_Date_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_11";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<DateHolder> dateHolderMatch = q.createMatch(DateHolder.class);
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(1960, 1, 8, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date date = cal.getTime();
		
		q.WHERE(dateHolderMatch.atttribute("date")).EQUALS(date);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_10() {
		//DomainQueryTest.testDomainQuery_Collect_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_10";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> subjectsMatch = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> subjectsMatch2 = q.createMatch(Subject.class);
		
		q.ORDER(subjectsMatch).BY("firstName");
		q.ORDER(subjectsMatch2).BY("lastName").DESCENDING();
		DomainObjectMatch<String> firstNamesMatch = q.COLLECT(subjectsMatch.atttribute("firstName")).AS(String.class);
		firstNamesMatch.setPage(0, 5);
		DomainObjectMatch<String> lastNamesMatch = q.COLLECT(subjectsMatch2.atttribute("lastName")).AS(String.class);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 02 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Object> objectsMatch = q.createMatch(Object.class);
		
		q.WHERE(objectsMatch.atttribute("name")).LIKE(".*");
		
		DomainObjectMatch<String> objNamesMatch = q.COLLECT(objectsMatch.atttribute("name")).AS(String.class);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_09() {
		//DomainQueryTest.testDomainQuery_Reject_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_09";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Person> j_smith_p = q.createMatch(Person.class);
		DomainObjectMatch<Area> europe_a = q.createMatch(Area.class);
		
		q.WHERE(europe_a.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith_p.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith_p.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<PointOfContact> j_smith_Addresses_p =
				q.TRAVERSE_FROM(j_smith_p).FORTH("pointsOfContact").TO(PointOfContact.class);
		DomainObjectMatch<Area> j_smith_Areas_a = q.TRAVERSE_FROM(j_smith_Addresses_p).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		
		DomainObjectMatch<PointOfContact> j_smith_FilteredPocs_p =
				q.REJECT_FROM(j_smith_Addresses_p).ELEMENTS(
						q.WHERE(j_smith_Areas_a).CONTAINS(europe_a)
				);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 02 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Object> j_smith = q.createMatch(Object.class);
		DomainObjectMatch<Object> europe = q.createMatch(Object.class);
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Object> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Object.class);
		DomainObjectMatch<Object> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Object.class);
		
		DomainObjectMatch<Object> j_smith_FilteredPocs =
				q.REJECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_Areas).CONTAINS(europe)
				);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_08() {
		//DomainQueryTest.testDomainQuery_Select_04()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_08";
		StringBuilder sb = new StringBuilder();
		
		/** 11 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Person> subjects = q.createMatch(Person.class);
		DomainObjectMatch<Area> europe = q.createMatch(Area.class);
		DomainObjectMatch<Person> j_smith = q.createMatch(Person.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		
		DomainObjectMatch<Object> addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		DomainObjectMatch<AbstractArea> areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
		DomainObjectMatch<Person> num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.COUNT()).EQUALS(4),
				q.WHERE(areas.COUNT()).EQUALS(9)
			);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 15 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		subjects = q.createMatch(Person.class);
		
		addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
		num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(subjects.atttribute("lastName")).EQUALS("Smith"),
				q.BR_OPEN(),
					q.WHERE(addresses.COUNT()).EQUALS(4),
					q.OR(),
					q.WHERE(areas.COUNT()).EQUALS(9),
				q.BR_CLOSE()
			);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_07() {
		//DomainQueryTest.testDomainQuery_Select_03()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_07";
		StringBuilder sb = new StringBuilder();
		
		/** 07 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Object> j_smith = q.createMatch(Object.class);
		DomainObjectMatch<Object> europe = q.createMatch(Object.class);
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Object> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Object.class);
		DomainObjectMatch<Object> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Object.class);
		
		DomainObjectMatch<Object> j_smith_FilteredPocs =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_Areas).CONTAINS(europe)
				);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 08 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> subjects = q.createMatch(Subject.class);
		
		DomainObjectMatch<Object> addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);

		DomainObjectMatch<Subject> num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.COUNT()).EQUALS(4)
			);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 09 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		subjects = q.createMatch(Subject.class);
		europe = q.createMatch(Object.class);
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		
		addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		DomainObjectMatch<Area> areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Area.class);

		num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.COUNT()).EQUALS(4),
				q.WHERE(areas).CONTAINS(europe)
			);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_06() {
		//DomainQueryTest.testDomainQuery_Select_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_06";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Person> j_smith = q.createMatch(Person.class);
		DomainObjectMatch<Area> europe = q.createMatch(Area.class);
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Address> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Address.class);
		DomainObjectMatch<Area> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		
//		q.WHERE(j_smith_Areas).CONTAINS(europe); // must throw exception
		
		DomainObjectMatch<Address> j_smith_FilteredAddresses =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("Austria"),
						q.OR(),
						q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("USA"),
						q.BR_OPEN(),
							q.WHERE(j_smith_Addresses.atttribute("number")).EQUALS(32),
							q.OR(),
							q.WHERE(j_smith_Addresses.atttribute("number")).EQUALS(20),
						q.BR_CLOSE()
				);
		DomainObjectMatch<Person> dummy = q.createMatch(Person.class);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 02 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		j_smith = q.createMatch(Person.class);
		europe = q.createMatch(Area.class);
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<PointOfContact> j_smith_PoCs =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(PointOfContact.class);
		j_smith_Areas = q.TRAVERSE_FROM(j_smith_PoCs).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		
		DomainObjectMatch<PointOfContact> j_smith_FilteredPoCs =
				q.SELECT_FROM(j_smith_PoCs).ELEMENTS(
						q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("Austria")
				);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_05() {
		//DomainQueryTest.testDomainQuery_Traversals_02()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_05";
		StringBuilder sb = new StringBuilder();
		
		/** 02 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Area> europe = q.createMatch(Area.class);
		DomainObjectMatch<Area> usa = q.createMatch(Area.class);
		DomainObjectMatch<Subject> inEuropeAndUsa = q.createMatch(Subject.class);

		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		q.WHERE(usa.atttribute("name")).EQUALS("USA");
		
		DomainObjectMatch<Subject> inEurope = q.TRAVERSE_FROM(europe).BACK("partOf").DISTANCE(1, -1)
				.BACK("area").BACK("pointsOfContact").TO(Subject.class);
		DomainObjectMatch<Subject> inUsa = q.TRAVERSE_FROM(usa).BACK("partOf").DISTANCE(1, -1)
				.BACK("area").BACK("pointsOfContact").TO(Subject.class);
		
		q.WHERE(inEuropeAndUsa).IN(inEurope);
		q.WHERE(inEuropeAndUsa).IN(inUsa);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 03 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Person> j_smithMatch = q.createMatch(Person.class);

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
		DomainObjectMatch<Person> j_smith_residentsMatch =
			q.TRAVERSE_FROM(j_smithMatch).FORTH("pointsOfContact")
				.BACK("pointsOfContact").TO(Person.class);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_04() {
		//DomainQueryTest.testDomainQuery_Traversals_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_04";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Person> j_smith = q.createMatch(Person.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Address> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Address.class);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 02 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> subs = q.createMatch(Subject.class);

		q.WHERE(subs.atttribute("name")).EQUALS("Global Company");
		q.OR();
		q.BR_OPEN();
			q.WHERE(subs.atttribute("lastName")).EQUALS("Smith");
			q.WHERE(subs.atttribute("firstName")).EQUALS("John");
		q.BR_CLOSE();
		
		DomainObjectMatch<PointOfContact> subs_Contacts =
				q.TRAVERSE_FROM(subs).FORTH("pointsOfContact").TO(PointOfContact.class);
		DomainObjectMatch<Area> areas = q.TRAVERSE_FROM(subs_Contacts).FORTH("area").TO(Area.class);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 03 ************** with pagination ********************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> subs_1 = q.createMatch(Subject.class);

		q.WHERE(subs_1.atttribute("name")).EQUALS("Global Company");
		q.OR();
		q.BR_OPEN();
			q.WHERE(subs_1.atttribute("lastName")).EQUALS("Smith");
			q.WHERE(subs_1.atttribute("firstName")).EQUALS("John");
		q.BR_CLOSE();
		subs_1.setPage(1, 1);
		
		DomainObjectMatch<PointOfContact> subs_1_Contacts =
				q.TRAVERSE_FROM(subs_1).FORTH("pointsOfContact").TO(PointOfContact.class);
		subs_1_Contacts.setPage(2, 2);
		q.ORDER(subs_1_Contacts).BY("street");
		DomainObjectMatch<Area> areas_1 = q.TRAVERSE_FROM(subs_1_Contacts).FORTH("area").TO(Area.class);
		areas_1.setPage(0, 1);
		q.ORDER(areas_1).BY("name").DESCENDING();
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 04 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Person> j_smith_1 = q.createMatch(Person.class);

		q.WHERE(j_smith_1.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith_1.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Area> j_smith_1_Areas2 =
				q.TRAVERSE_FROM(j_smith_1).FORTH("pointsOfContact")
					.FORTH("area").FORTH("partOf").DISTANCE(1, -1).TO(Area.class);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 05 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> j_smith_comp = q.createMatch(Subject.class);

		q.WHERE(j_smith_comp.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith_comp.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<PointOfContact> pocs = q.TRAVERSE_FROM(j_smith_comp)
				.FORTH("pointsOfContact").TO(PointOfContact.class);
		q.WHERE(pocs.atttribute("street")).LIKE("Mark.*");
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 06 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> jer_smith_comp = q.createMatch(Subject.class);
		areas = q.createMatch(Area.class);

		q.WHERE(jer_smith_comp.atttribute("name")).EQUALS("Global Company");
		q.OR();
		q.BR_OPEN();
			q.WHERE(jer_smith_comp.atttribute("lastName")).EQUALS("Smith");
			q.WHERE(jer_smith_comp.atttribute("firstName")).EQUALS("John");
		q.BR_CLOSE();
		
		areas_1 = q.TRAVERSE_FROM(jer_smith_comp)
				.FORTH("pointsOfContact")
				.FORTH("area")
				.TO(Area.class);
		DomainObjectMatch<Area> areas_2 = q.TRAVERSE_FROM(areas_1)
				.FORTH("partOf").DISTANCE(1, -1)
				.TO(Area.class);
		
		// build union of areas
		q.BR_OPEN();
			q.WHERE(areas).IN(areas_1);
			q.OR();
			q.WHERE(areas).IN(areas_2);
		q.BR_CLOSE();
		q.WHERE(areas.atttribute("areaType")).EQUALS(AreaType.CITY);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 08 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		j_smith = q.createMatch(Person.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<AbstractArea> jsAreas = q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_03() {
		//DomainQueryTest.testDomainQuery_Contains_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_03";
		StringBuilder sb = new StringBuilder();
		
		/** 01 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<NumberHolder> nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(2);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 02 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(2);
		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(5);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 04 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(2, 3);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/** 08 ****************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers").length()).EQUALS(3);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_02() {
		//DomainQueryTest.testDomainQuery_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_02";
		StringBuilder sb = new StringBuilder();
		
		/******************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> has_clark_firstName = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> has_clark_firstName_2 = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> clark = q.createMatch(Subject.class);
		q.WHERE(clark.atttribute("lastName")).EQUALS("Clark");
		q.WHERE(has_clark_firstName.atttribute("firstName")).EQUALS(clark.atttribute("firstName"));
		q.WHERE(has_clark_firstName_2.atttribute("firstName")).EQUALS(clark.atttribute("firstName"));
		q.WHERE(has_clark_firstName_2).NOT().IN(clark);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> allSubjects = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> matchingExclude = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> matchingInclude = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> match = q.createMatch(Subject.class);
		q.BR_OPEN();
			q.WHERE(match.atttribute("lastName")).EQUALS("Watson");
			q.OR();
			q.WHERE(match.atttribute("name")).EQUALS("Global Company");
		q.BR_CLOSE();
		
		q.WHERE(matchingExclude.atttribute("matchString")).EQUALS(match.atttribute("matchString"));
		q.WHERE(matchingExclude).NOT().IN(match);
		q.WHERE(matchingInclude.atttribute("matchString")).EQUALS(match.atttribute("matchString"));
		q.WHERE(matchingInclude).IN(match);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> set_1 = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> set_2 = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> intersection = q.createMatch(Subject.class);
		// a set with all smiths and christa berghammer and global company
		q.BR_OPEN();
			q.WHERE(set_1.atttribute("lastName")).EQUALS("Smith");
			q.OR();
			q.BR_OPEN();
				q.WHERE(set_1.atttribute("lastName")).EQUALS("Berghammer");
				q.WHERE(set_1.atttribute("firstName")).EQUALS("Christa");
			q.BR_CLOSE();
			q.OR();
			q.WHERE(set_1.atttribute("name")).EQUALS("Global Company");
		q.BR_CLOSE();
		
		// a set with all berghammers and global company
		q.WHERE(set_2.atttribute("lastName")).EQUALS("Berghammer");
		q.OR();
		q.WHERE(set_2.atttribute("name")).EQUALS("Global Company");
		
		// the intersection of both sets
		q.WHERE(intersection).IN(set_1);
		q.WHERE(intersection).IN(set_2);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> subjects = q.createMatch(Subject.class);
		q.ORDER(subjects).BY("lastName");
		q.ORDER(subjects).BY("firstName").DESCENDING();
		q.ORDER(subjects).BY("name").DESCENDING();
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> set_00 = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> set_01 = q.createMatch(Subject.class);
		set_01.setPage(5, 5);
		DomainObjectMatch<Subject> set_02 = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> intersectionPage = q.createMatch(Subject.class);
		intersectionPage.setPage(1, 5);
		
		q.WHERE(set_01).IN(set_00);
		q.WHERE(set_02).IN(set_00);
		q.WHERE(intersectionPage).IN(set_01);
		q.WHERE(intersectionPage).IN(set_02);
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	@Test
	public void testRecordQuery_01() {
		//DomainQueryTest.testDomainQuery_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "RECORDED_QUERY_01";
		StringBuilder sb = new StringBuilder();
		
		/******************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		RecordedQuery recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> smiths = q.createMatch(Subject.class);
		
		q.WHERE(smiths.stringAtttribute("lastName")).EQUALS("Smith");
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		//System.out.println(recordedQuery2.toString());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		qpt = QueryRecorder.getCreateQueriesPerThread();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> j_smith = q.createMatch(Subject.class);
		
		q.WHERE(j_smith.stringAtttribute("firstName")
				.concat(j_smith.stringAtttribute("lastName")).concat(j_smith.stringAtttribute("firstName"))).EQUALS("JohnSmithJohn");
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		qpt = QueryRecorder.getCreateQueriesPerThread();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		j_smith = q.createMatch(Subject.class);
		
		q.WHERE(j_smith.numberAtttribute("firstName").math().acos()).EQUALS(1);
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> smith_false = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> bergHammer = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> smith_true = q.createMatch(Subject.class);
		j_smith = q.createMatch(Subject.class);
		q.BR_OPEN();
			q.WHERE(smith_false.stringAtttribute("lastName")).EQUALS(q.parameter("lastName"));
			q.WHERE(smith_false.stringAtttribute("matchString")).EQUALS("smith_family");
			q.BR_OPEN();
				q.WHERE(smith_false.stringAtttribute("firstName")).EQUALS("Angelina");
				q.OR();
				q.WHERE(smith_false.stringAtttribute("firstName")).EQUALS("Jeremy");
			q.BR_CLOSE();
			q.BR_OPEN();
				q.WHERE(bergHammer.stringAtttribute("lastName")).EQUALS("Berghammer");
				q.OR();
				q.WHERE(bergHammer.stringAtttribute("matchString")).EQUALS("berghammer_family");
			q.BR_CLOSE();
		q.BR_CLOSE();
		//String str = recordedQuery.toString();
		
		q.WHERE(j_smith.stringAtttribute("firstName")
				.concat(j_smith.stringAtttribute("lastName"))).EQUALS("JohnSmith");
		
		q.WHERE(smith_true.stringAtttribute("firstName")).EQUALS("Angelina");
		q.WHERE(smith_true.stringAtttribute("matchString")).EQUALS("smith");
		
		q.parameter("lastName").setValue("Smith");
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Subject> smith_1 = q.createMatch(Subject.class);
		q.WHERE(smith_1.stringAtttribute("lastName")).EQUALS(q.parameter("lastName"));
		q.WHERE(smith_1.stringAtttribute("firstName")).NOT().EQUALS("Caroline");
		q.WHERE(smith_1.stringAtttribute("firstName")).NOT().EQUALS("Angelina");
		
		q.parameter("lastName").setValue("Smith");
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		smith_1 = q.createMatch(Subject.class);
		q.WHERE(smith_1.stringAtttribute("lastName")).EQUALS(q.parameter("lastName"));
		q.WHERE(smith_1.stringAtttribute("firstName")).IN_list("Caroline", "Angelina");
		
		q.parameter("lastName").setValue("Smith");
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		/******************************************/
		q = da1.createQuery();
		recordedQuery = QueryRecorder.getRecordedQuery(q);
		smith_1 = q.createMatch(Subject.class);
		q.WHERE(smith_1.stringAtttribute("lastName")).EQUALS(q.parameter("lastName"));
		q.WHERE(smith_1.stringAtttribute("firstName")).NOT().IN_list("Caroline", "Angelina");
		
		q.parameter("lastName").setValue("Smith");
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n\n").append(recordedQuery.toString());
		
		q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
		assertEquals(testId, tdr.getTestData(testId), sb.toString());
		
		return;
	}
	
	private static void initTest_14() {
		//DomainQueryTest.testDomainQuery_Union_Intersection_01()
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 04 ****************************************/
		DomainQuery q = da1.createQuery();
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		reccordedQuery_14 = QueryRecorder.getRecordedQuery(q);
		DomainObjectMatch<Object> j_smith = q.createMatch(Object.class);
		DomainObjectMatch<Object> sf = q.createMatch(Object.class);
		
		q.WHERE(sf.atttribute("name")).EQUALS("San Francisco");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Object> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Object.class);
		DomainObjectMatch<Object> j_smith_d_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.TO(Object.class);
		DomainObjectMatch<Object> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Object.class);
		DomainObjectMatch<Object> j_smith_all_Areas = q.UNION(j_smith_d_Areas, j_smith_Areas);
		
		DomainObjectMatch<Object> j_smith_FilteredPocs =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_all_Areas).CONTAINS(sf)
				);
		
		DomainQueryResult result = q.execute(); // queries per thread should be cleared
		assertTrue(qpt.isCleared());
		
		return;
	}
	
	@BeforeClass
	public static void before() {
		Settings.TEST_MODE = true;
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
		
		List<JcError> errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		errors = da.store(storedDomainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		initTest_14();
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
		Settings.TEST_MODE = false;
	}
}
