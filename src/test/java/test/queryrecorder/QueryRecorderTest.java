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
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.domainquery.internal.QueryRecorder.QueriesPerThread;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.domainquery.internal.RecordedQueryPlayer;
import iot.jcypher.domainquery.internal.Settings;
import iot.jcypher.util.QueriesPrintObserver;
import test.AbstractTestSuite;
import test.domainquery.Population;
import test.domainquery.model.AbstractArea;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.AreaType;
import test.domainquery.model.NumberHolder;
import test.domainquery.model.Person;
import test.domainquery.model.PointOfContact;
import test.domainquery.model.Subject;
import util.TestDataReader;

public class QueryRecorderTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	
	@Test
	public void testRecordQuery_06() {
		//DomainQueryTest.testDomainQuery_Traversals_02()
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
		
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		System.out.println("\n" + recordedQuery.toString());
		sb.append("\n").append(recordedQuery.toString());
		
		DomainQuery q2 = new RecordedQueryPlayer().replayQuery(recordedQuery, da1);
		RecordedQuery recordedQuery2 = QueryRecorder.getRecordedQuery(q2);
		QueryRecorder.queryCompleted(q2);
		assertTrue(qpt.isCleared());
		assertEquals(recordedQuery.toString(), recordedQuery2.toString());
		
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
		
		assertEquals(testId, sb.toString(), tdr.getTestData(testId));
		
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
		
		assertEquals(testId, sb.toString(), tdr.getTestData(testId));
		
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
		
		assertEquals(testId, sb.toString(), tdr.getTestData(testId));
		
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
		
		assertEquals(testId, sb.toString(), tdr.getTestData(testId));
		
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
		
		assertEquals(testId, sb.toString(), tdr.getTestData(testId));
		
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
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
//		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "jcypher");
		
		// init db
		Population population = new Population();
		storedDomainObjects = population.createPopulation();
		
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
