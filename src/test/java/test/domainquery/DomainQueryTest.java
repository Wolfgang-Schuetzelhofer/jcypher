/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

package test.domainquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domainquery.CountQueryResult;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainquery.model.AbstractArea;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.AreaType;
import test.domainquery.model.Company;
import test.domainquery.model.DateHolder;
import test.domainquery.model.EContact;
import test.domainquery.model.EContact.EContactType;
import test.domainquery.model.Gender;
import test.domainquery.model.NumberHolder;
import test.domainquery.model.Person;
import test.domainquery.model.PointOfContact;
import test.domainquery.model.SubNumberHolder;
import test.domainquery.model.Subject;
import test.domainquery.util.CompareUtil;
import util.TestDataReader;

public class DomainQueryTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	private static ByteArrayOutputStream queriesStream;
	
	@BeforeClass
	public static void before() {
		QueryRecorder.blockRecording.set(Boolean.TRUE);
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
		
		NumberHolder n1 = new NumberHolder("n_123", new int[]{1, 2, 3});
		NumberHolder n2 = new NumberHolder("n_456", new int[]{4, 5, 6});
		SubNumberHolder sn1 = new SubNumberHolder("subn_347", new int[]{3, 4, 7});
		List<Object> nhs = new ArrayList<Object>();
		nhs.add(n1);
		nhs.add(n2);
		nhs.add(sn1);
		
		List<Date> dates = new ArrayList<Date>();
		long millis = 292517846786l;
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(1960, 1, 8, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date date = cal.getTime();
		long tm = date.getTime(); //(date.getTime() / 1000) * 1000;
		dates.add(new Timestamp(tm));
		dates.add(new Timestamp(millis));
		DateHolder dh = new DateHolder("w", date);
		dh.setSqlTimestamp(new Timestamp(millis));
		nhs.add(dh);
		cal = GregorianCalendar.getInstance();
		cal.set(1963, 9, 24, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date = cal.getTime();
		tm = date.getTime(); //(date.getTime() / 1000) * 1000;
		dates.add(new Timestamp(tm));
		dh.setDates(dates);
		DateHolder dh1 = new DateHolder("d", date);
		dh1.setSqlTimestamp(new Timestamp(millis));
		nhs.add(dh1);
		
		QueriesPrintObserver.addOutputStream(System.out);
		queriesStream = new ByteArrayOutputStream();
		QueriesPrintObserver.addOutputStream(queriesStream);
		
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
		
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
		errors = da.store(nhs);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		storedDomainObjects = null;
		try {
			queriesStream.close();
		} catch (IOException e) {}
		queriesStream = null;
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
		QueryRecorder.blockRecording.remove();
	}
	
	@Test
	public void testDomainQuery_Concatenation_02() {
		IDomainAccess da1;
		DomainQuery q, q1;
		DomainQueryResult result, result1;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_CONCAT_01.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "CONCAT_02";
		
		q = da1.createQuery();
		DomainObjectMatch<Person> smith = q.createMatch(Person.class);
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		result = q.execute();
		
		List<Person> smithResult = result.resultOf(smith);
		
		queriesStream.reset();
		q1 = da1.createQuery();
		DomainObjectMatch<Person> j_smith = q1.createMatchFor(smithResult, Person.class);
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		result1 = q1.execute();
		
		List<Person> j_smithResult = result1.resultOf(j_smith);
		assertEquals(1, j_smithResult.size());
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), j_smithResult.get(0));
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQueryByLines(testId, qCypher, tdr.getTestData(testId));
		
		/** 02 ****************************************/
		testId = "CONCAT_03";
		
		q = da1.createQuery();
		smith = q.createMatch(Person.class);
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		result = q.execute();
		
		smithResult = result.resultOf(smith);
		
		queriesStream.reset();
		q1 = da1.createQuery();
		DomainObjectMatch<Person> smith_2 = q1.createMatchFor(smithResult, Person.class);
		q1.ORDER(smith_2).BY("firstName");
		
		result1 = q1.execute();
		
		List<Person> smith_2Result = result1.resultOf(smith_2);
		List<Object> orderedSmiths = new ArrayList<Object>();
		orderedSmiths.addAll(population.getSmiths());
		Collections.sort(orderedSmiths, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((Person)o1).getFirstName().compareTo(((Person)o2).getFirstName());
			}
		});
		equals = CompareUtil.equalsList(orderedSmiths, smith_2Result);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQueryByLines(testId, qCypher, tdr.getTestData(testId));
		
		/** 03 ****************************************/
		testId = "CONCAT_04";
		
		q = da1.createQuery();
		smith = q.createMatch(Person.class);
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		result = q.execute();
		
		smithResult = result.resultOf(smith);
		
		queriesStream.reset();
		q1 = da1.createQuery();
		DomainObjectMatch<Person> smith_3 = q1.createMatchFor(smithResult, Person.class);
		q1.ORDER(smith_3).BY("firstName");
		smith_3.setPage(2, 2);
		
		result1 = q1.execute();
		
		List<Person> smith_3Result = result1.resultOf(smith_3);
		List<Object> orderedSmiths2 = orderedSmiths.subList(2, 4);
		equals = CompareUtil.equalsList(orderedSmiths2, smith_3Result);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQueryByLines(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Concatenation_01() {
		IDomainAccess da1;
		DomainQuery q, q1;
		DomainQueryResult result1;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_CONCAT_01.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "CONCAT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Person> smith = q.createMatch(Person.class);
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		//result = q.execute();
		
		q1 = da1.createQuery();
		DomainObjectMatch<Person> j_smith = q1.createMatchFrom(smith);
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		result1 = q1.execute();
		
		//List<Person> smithResult = result.resultOf(smith);
		List<Person> j_smithResult = result1.resultOf(j_smith);
		assertEquals(1, j_smithResult.size());
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), j_smithResult.get(0));
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQueryByLines(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Union_Intersection_05() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		List<Object> added = addPersons();
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_UNION_INTERSECTION_01.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "INTERSECTION_02";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Person> personsMatch = q.createMatch(Person.class);

		DomainObjectMatch<Person> m_childrenMatch = q.TRAVERSE_FROM(personsMatch).FORTH("mother")
				.BACK("mother").TO(Person.class);
		DomainObjectMatch<Person> f_childrenMatch = q.TRAVERSE_FROM(personsMatch).FORTH("father")
				.BACK("father").TO(Person.class);
		DomainObjectMatch<Person> siblingsMatch = q.INTERSECTION(m_childrenMatch, f_childrenMatch);
		
		DomainObjectMatch<Person> siblings2Match = q.SELECT_FROM(personsMatch).ELEMENTS(
				q.WHERE(siblingsMatch.COUNT()).GTE(2)
		);
		result = q.execute();
		
		List<Person> siblings2 = result.resultOf(siblings2Match);
		
		List<Object> comp = new ArrayList<Object>();
		comp.add(population.getChrista_berghammer_globcom().get(0));
		comp.addAll(added);
		equals = CompareUtil.equalsUnorderedList(comp, siblings2);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		
		/** 02 ****************************************/
		testId = "INTERSECTION_03";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Subject> personsMatch1 = q.createMatch(Subject.class);

		DomainObjectMatch<Subject> m_childrenMatch1 = q.TRAVERSE_FROM(personsMatch1).FORTH("mother")
				.BACK("mother").TO(Subject.class);
		DomainObjectMatch<Subject> f_childrenMatch1 = q.TRAVERSE_FROM(personsMatch1).FORTH("father")
				.BACK("father").TO(Subject.class);
		DomainObjectMatch<Subject> siblingsMath1 = q.INTERSECTION(m_childrenMatch1, f_childrenMatch1);
		
		DomainObjectMatch<Subject> siblings2Match1 = q.SELECT_FROM(personsMatch1).ELEMENTS(
				q.WHERE(siblingsMath1.COUNT()).GTE(2)
		);
		result = q.execute();
		
		List<Subject> siblings21 = result.resultOf(siblings2Match1);
		
		equals = CompareUtil.equalsUnorderedList(comp, siblings21);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		// reset
		before();
		return;
	}
	
	/**
	 * @return some of the newly created objects
	 */
	List<Object> addPersons() {
		List<Object> ret = new ArrayList<Object>();
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		DomainQuery q = da1.createQuery();
		
		DomainObjectMatch<Person> gerda_berghammerMatch = q.createMatch(Person.class);
		DomainObjectMatch<Person> hans_berghammerMatch = q.createMatch(Person.class);
		DomainObjectMatch<Person> herbert_maierMatch = q.createMatch(Person.class);
		q.WHERE(gerda_berghammerMatch.atttribute("firstName")).EQUALS("Gerda");
		q.WHERE(gerda_berghammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		q.WHERE(hans_berghammerMatch.atttribute("firstName")).EQUALS("Hans");
		q.WHERE(hans_berghammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		q.WHERE(herbert_maierMatch.atttribute("firstName")).EQUALS("Herbert");
		q.WHERE(herbert_maierMatch.atttribute("lastName")).EQUALS("Maier");
		
		DomainQueryResult result = q.execute();
		
		Person gerda_berghammer = result.resultOf(gerda_berghammerMatch).get(0);
		Person hans_berghammer = result.resultOf(hans_berghammerMatch).get(0);
		Person herbert_maier = result.resultOf(herbert_maierMatch).get(0);
		
		List<Object> toStore = new ArrayList<Object>();
		
		Person fritz_berhammer = new Person("Fritz", "Berghammer", Gender.MALE);
		fritz_berhammer.setMatchString("berghammer");
		fritz_berhammer.getPointsOfContact().add(gerda_berghammer.getPointsOfContact().get(0));
		fritz_berhammer.setMother(gerda_berghammer);
		fritz_berhammer.setFather(herbert_maier);
		toStore.add(fritz_berhammer);
		
		Person hannah_berhammer = new Person("Hannah", "Berghammer", Gender.FEMALE);
		hannah_berhammer.setMatchString("berghammer");
		hannah_berhammer.getPointsOfContact().add(gerda_berghammer.getPointsOfContact().get(0));
		hannah_berhammer.setMother(gerda_berghammer);
		hannah_berhammer.setFather(hans_berghammer);
		toStore.add(hannah_berhammer);
		ret.add(hannah_berhammer);
		
		Person max_berhammer = new Person("Max", "Berghammer", Gender.MALE);
		max_berhammer.setMatchString("berghammer");
		max_berhammer.getPointsOfContact().add(gerda_berghammer.getPointsOfContact().get(0));
		max_berhammer.setMother(gerda_berghammer);
		max_berhammer.setFather(hans_berghammer);
		toStore.add(max_berhammer);
		ret.add(max_berhammer);
		
		List<JcError> errors = da1.store(toStore);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		return ret;
	}

	@Test
	public void testDomainQuery_Union_Intersection_04() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_UNION_INTERSECTION_01.txt");
		
		Population population = new Population();
		List<Object> domObjects = population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "UNION_06";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Subject> j_smith = q.createMatch(Subject.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<PointOfContact> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(PointOfContact.class);
		DomainObjectMatch<AbstractArea> j_smith_d_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.TO(AbstractArea.class);
		DomainObjectMatch<AbstractArea> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		DomainObjectMatch<AbstractArea> j_smith_all_Areas = q.UNION(j_smith_d_Areas, j_smith_Areas);
		
		DomainObjectMatch<PointOfContact> j_smith_FilteredPocs =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_all_Areas.COUNT()).EQUALS(5)
				);
		result = q.execute();
		
		List<AbstractArea> j_smith_all = result.resultOf(j_smith_all_Areas);
		List<PointOfContact> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		
		assertEquals(13, j_smith_all.size());
		List<Object> pocComp = new ArrayList<Object>();
		pocComp.add(population.getSchwedenPlatz_32());
		pocComp.add(population.getMarketStreet_20());
		equals = CompareUtil.equalsUnorderedList(pocComp, j_smith_FilteredPoCsResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Union_Intersection_03() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_UNION_INTERSECTION_01.txt");
		
		Population population = new Population();
		List<Object> domObjects = population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "UNION_05";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Subject> j_smith = q.createMatch(Subject.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Address> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Address.class);
		DomainObjectMatch<Area> j_smith_d_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.TO(Area.class);
		DomainObjectMatch<Area> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		DomainObjectMatch<Area> j_smith_all_Areas = q.UNION(j_smith_d_Areas, j_smith_Areas);
		q.WHERE(j_smith_all_Areas.atttribute("name")).EQUALS("San Francisco");
		
		DomainObjectMatch<Address> j_smith_FilteredPocs =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_all_Areas.COUNT()).EQUALS(1)
				);
		result = q.execute();
		
		List<Area> j_smith_all = result.resultOf(j_smith_all_Areas);
		List<Address> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		
		assertEquals(1, j_smith_all.size());
		equals = CompareUtil.equalsObjects(population.getSanFrancisco(), j_smith_all.get(0));
		assertTrue(equals);
		assertEquals(1, j_smith_FilteredPoCsResult.size());
		equals = CompareUtil.equalsObjects(population.getMarketStreet_20(), j_smith_FilteredPoCsResult.get(0));
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Union_Intersection_02() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_UNION_INTERSECTION_01.txt");
		
		Population population = new Population();
		List<Object> domObjects = population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "UNION_04";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Subject> j_smith = q.createMatch(Subject.class);
		DomainObjectMatch<Area> sf = q.createMatch(Area.class);
		
		q.WHERE(sf.atttribute("name")).EQUALS("San Francisco");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Address> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Address.class);
		DomainObjectMatch<Area> j_smith_d_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.TO(Area.class);
		DomainObjectMatch<Area> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		DomainObjectMatch<Area> j_smith_all_Areas = q.UNION(j_smith_d_Areas, j_smith_Areas);
		q.WHERE(j_smith_all_Areas.atttribute("name")).EQUALS("San Francisco");
		
		DomainObjectMatch<Address> j_smith_FilteredPocs =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_all_Areas).CONTAINS(sf)
				);
		result = q.execute();
		
		List<Area> j_smith_all = result.resultOf(j_smith_all_Areas);
		List<Address> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		
		assertEquals(1, j_smith_all.size());
		equals = CompareUtil.equalsObjects(population.getSanFrancisco(), j_smith_all.get(0));
		assertTrue(equals);
		assertEquals(1, j_smith_FilteredPoCsResult.size());
		equals = CompareUtil.equalsObjects(population.getMarketStreet_20(), j_smith_FilteredPoCsResult.get(0));
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Union_Intersection_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_UNION_INTERSECTION_01.txt");
		
		Population population = new Population();
		List<Object> domObjects = population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "UNION_01";
		queriesStream.reset();
		q = da1.createQuery();
		DomainObjectMatch<Subject> smithMatch = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> bergHammerMatch = q.createMatch(Subject.class);
		
		q.WHERE(smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(bergHammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		DomainObjectMatch<Subject> unionMatch = q.UNION(smithMatch, bergHammerMatch);
		
		result = q.execute();
		
		List<Subject> smith = result.resultOf(smithMatch);
		List<Subject> bergHammer = result.resultOf(bergHammerMatch);
		List<Subject> union = result.resultOf(unionMatch);
		
		equals = CompareUtil.equalsUnorderedList(population.getSmiths(), smith);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getBerghammers(), bergHammer);
		assertTrue(equals);
		List<Object> sAndB = new ArrayList<Object>();
		sAndB.addAll(population.getSmiths());
		sAndB.addAll(population.getBerghammers());
		equals = CompareUtil.equalsUnorderedList(sAndB, union);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 02 ****************************************/
		testId = "INTERSECTION_01";
		queriesStream.reset();
		q = da1.createQuery();
		DomainObjectMatch<Subject> smith_ChristaMatch = q.createMatch(Subject.class);
		bergHammerMatch = q.createMatch(Subject.class);
		
		q.WHERE(smith_ChristaMatch.atttribute("lastName")).EQUALS("Smith");
		q.OR();
		q.BR_OPEN();
			q.WHERE(smith_ChristaMatch.atttribute("lastName")).EQUALS("Berghammer");
			q.WHERE(smith_ChristaMatch.atttribute("firstName")).EQUALS("Christa");
		q.BR_CLOSE();
		
		q.WHERE(bergHammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		
		DomainObjectMatch<Subject> intersectionMatch = q.INTERSECTION(smithMatch, bergHammerMatch);
		
		result = q.execute();
		
		List<Subject> smith_Christa = result.resultOf(smith_ChristaMatch);
		List<Subject> intersection = result.resultOf(intersectionMatch);
		
		List<Object> sAndChrista = new ArrayList<Object>();
		sAndChrista.addAll(population.getSmiths_christa_berghammer_globcom());
		sAndChrista.remove(sAndChrista.size() - 1);
		equals = CompareUtil.equalsUnorderedList(sAndChrista, smith_Christa);
		assertTrue(equals);
		List<Object> intersectComp = new ArrayList<Object>();
		intersectComp.addAll(population.getChrista_berghammer_globcom());
		intersectComp.remove(intersectComp.size() - 1);
		equals = CompareUtil.equalsUnorderedList(intersectComp, intersection);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 03 ****************************************/
		testId = "UNION_02";
		queriesStream.reset();
		q = da1.createQuery();
		smithMatch = q.createMatch(Subject.class);
		bergHammerMatch = q.createMatch(Subject.class);
		
		q.WHERE(smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(bergHammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		unionMatch = q.UNION(smithMatch, bergHammerMatch);
		q.WHERE(unionMatch.atttribute("firstName")).EQUALS("Angelina");
		
		result = q.execute();
		
		union = result.resultOf(unionMatch);
		
		equals = CompareUtil.equalsUnorderedList(population.getAngelina_smith(), union);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 04 ****************************************/
		testId = "UNION_03";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		result = q.execute();
		
		List<Object> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		
		assertEquals(1, j_smith_FilteredPoCsResult.size());
		equals = CompareUtil.equalsObjects(population.getMarketStreet_20(), j_smith_FilteredPoCsResult.get(0));
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Date_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		long millis = 292517846786l;
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<DateHolder> dateHolderMatch = q.createMatch(DateHolder.class);
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(1960, 1, 8, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date date = cal.getTime();
		
		q.WHERE(dateHolderMatch.atttribute("date")).EQUALS(date);
		
		result = q.execute();
		
		List<DateHolder> dateHolders = result.resultOf(dateHolderMatch);
		
		Timestamp ts = new Timestamp(millis);
		assertEquals(1, dateHolders.size());
		DateHolder dh = dateHolders.get(0);
		assertEquals("w", dh.getName());
		assertTrue(ts.equals(dh.getSqlTimestamp()));
		
		List<Date> dates = new ArrayList<Date>();
		cal = GregorianCalendar.getInstance();
		cal.set(1960, 1, 8, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date = cal.getTime();
		long tm = date.getTime(); //(date.getTime() / 1000) * 1000;
		dates.add(new Timestamp(tm));
		dates.add(new Timestamp(millis));
		cal = GregorianCalendar.getInstance();
		cal.set(1963, 9, 24, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date = cal.getTime();
		tm = date.getTime(); //(date.getTime() / 1000) * 1000;
		dates.add(new Timestamp(tm));
		
		CompareUtil.equalsList(dates, dh.getDates());
		
		/** 02 ****************************************/
		queriesStream.reset();
		
		q = da1.createQuery();
		dateHolderMatch = q.createMatch(DateHolder.class);
		
		cal = GregorianCalendar.getInstance();
		cal.set(1963, 9, 24, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date = cal.getTime();
		
		q.WHERE(dateHolderMatch.atttribute("date")).EQUALS(date);
		
		result = q.execute();
		
		dateHolders = result.resultOf(dateHolderMatch);
		
		assertEquals(1, dateHolders.size());
		dh = dateHolders.get(0);
		assertEquals("d", dh.getName());
		assertTrue(ts.equals(dh.getSqlTimestamp()));
		
		assertTrue(dh.getDates() == null);
		
		return;
	}
	
	@Test
	public void testDomainQuery_Collect_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_COLLECT_01.txt");
		
		Population population = new Population();
		List<Object> objs = population.createPopulation();
		List<Subject> allSubjects = new ArrayList<Subject>();
		for (Object obj : objs) {
			if (obj instanceof Subject)
				allSubjects.add((Subject)obj);
		}
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "COLLECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Subject> subjectsMatch = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> subjectsMatch2 = q.createMatch(Subject.class);
		
		q.ORDER(subjectsMatch).BY("firstName");
		q.ORDER(subjectsMatch2).BY("lastName").DESCENDING();
		DomainObjectMatch<String> firstNamesMatch = q.COLLECT(subjectsMatch.atttribute("firstName")).AS(String.class);
		firstNamesMatch.setPage(0, 5);
		DomainObjectMatch<String> lastNamesMatch = q.COLLECT(subjectsMatch2.atttribute("lastName")).AS(String.class);
		
		CountQueryResult countResult = q.executeCount();
		result = q.execute();
		
		long numFirstNames = countResult.countOf(firstNamesMatch);
		long numLastNames = countResult.countOf(lastNamesMatch);
		long numSubjects = countResult.countOf(subjectsMatch);
		List<String> firstNames = result.resultOf(firstNamesMatch);
		List<String> lastNames = result.resultOf(lastNamesMatch);
		List<Subject> subjects = result.resultOf(subjectsMatch);
//		List<Subject> subjects2 = result.resultOf(subjectsMatch2);
		
		assertEquals(10, numFirstNames);
		assertEquals(5, numLastNames);
		assertEquals(13, numSubjects);
		assertEquals("[Angelina, Caroline, Christa, Gerda, Hans]", firstNames.toString());
		assertEquals("[Watson, Smith, Maier, Clark, Berghammer]", lastNames.toString());
		equals = CompareUtil.equalsUnorderedList(allSubjects, subjects);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 02 ****************************************/
		testId = "COLLECT_02";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Object> objectsMatch = q.createMatch(Object.class);
		
//		q.ORDER(objectsMatch).BY("name");
		// if we don't do this, query execution performance will be very poor
		q.WHERE(objectsMatch.atttribute("name")).LIKE(".*");
		
		DomainObjectMatch<String> objNamesMatch = q.COLLECT(objectsMatch.atttribute("name")).AS(String.class);
//		objNamesMatch.setPage(0, 5);
		
		countResult = q.executeCount();
		result = q.execute();
		
		long numObjNames = countResult.countOf(objNamesMatch);
		List<String> objNames = result.resultOf(objNamesMatch);
		
		assertEquals(21, numObjNames);
		assertEquals("[San Francisco, Global Company, addressee_01, n_123, w, n_456," +
				" d, Small Company, California, USA, North America, Earth, Innere Stadt, Vienna," +
				" Austria, Europe, Munich, Germany, New York City, New York, Hernals]",
				objNames.toString());
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Reject_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_REJECT_01.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "REJECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		
		result = q.execute();
		
		List<PointOfContact> j_smith_FilteredPoCs_pResult = result.resultOf(j_smith_FilteredPocs_p);
		
		List<Object> compPocs = new ArrayList<Object>();
		for (Object obj : population.getJohn_smith_addresses()) {
			if (((Address)obj).getStreet().equals("Market Street"))
				compPocs.add(obj);
		}
		compPocs.add(population.getJohn_smith_econtact());
		equals = CompareUtil.equalsUnorderedList(compPocs, j_smith_FilteredPoCs_pResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 02 ****************************************/
		testId = "REJECT_02";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		
		result = q.execute();
		
		List<Object> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		equals = CompareUtil.equalsUnorderedList(compPocs, j_smith_FilteredPoCsResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Select_04() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_SELECT_01.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 11 ****************************************/
		testId = "SELECT_11";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		result = q.execute();
		
		List<Person> num_addressesResult = result.resultOf(num_addresses);
		List<Person> j_smithResult = result.resultOf(j_smith);
		assertEquals(1, num_addressesResult.size());
		assertEquals(1, j_smithResult.size());
		assertTrue(num_addressesResult.get(0) == j_smithResult.get(0));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 12 ****************************************/
		testId = "SELECT_12";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Person.class);
		DomainObjectMatch<Person> jer_smith = q.createMatch(Person.class);

		q.WHERE(jer_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(jer_smith.atttribute("firstName")).EQUALS("Jeremy");
		
		addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
		num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.atttribute("number")).EQUALS(20),
				//q.OR(),
				q.WHERE(subjects.atttribute("firstName")).EQUALS("Jeremy")
			);
		result = q.execute();
		
		num_addressesResult = result.resultOf(num_addresses);
		List<Person> jer_smithResult = result.resultOf(jer_smith);
		assertEquals(1, num_addressesResult.size());
		assertEquals(1, jer_smithResult.size());
		assertTrue(num_addressesResult.get(0) == jer_smithResult.get(0));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 13 ****************************************/
		testId = "SELECT_13";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Person.class);
		
		addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
		num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.atttribute("number")).EQUALS(20),
				q.OR(),
				q.WHERE(areas.atttribute("name")).EQUALS("Europe")
			);
		result = q.execute();
		
		num_addressesResult = result.resultOf(num_addresses);
		List<Person> europeans = new ArrayList<Person>();
		for (Object obj : population.getSubjectsInEurope()) {
			if (obj instanceof Person)
				europeans.add((Person) obj);
		}
		for (Object obj : population.getSmithFamily_no_john()) {
			Person per = (Person)obj;
			if (!europeans.contains(per))
				europeans.add(per);
		}
		equals = CompareUtil.equalsUnorderedList(europeans, num_addressesResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 14 ****************************************/
		testId = "SELECT_14";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Person.class);
		
		addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
		num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.atttribute("number")).EQUALS(20),
				q.WHERE(areas.atttribute("name")).EQUALS("Europe")
			);
		result = q.execute();
		
		num_addressesResult = result.resultOf(num_addresses);
		assertTrue(num_addressesResult.size() == 1);
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), num_addressesResult.get(0));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 15 ****************************************/
		testId = "SELECT_15";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		result = q.execute();
		
		num_addressesResult = result.resultOf(num_addresses);
		assertTrue(num_addressesResult.size() == 1);
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), num_addressesResult.get(0));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 16 ****************************************/
		testId = "SELECT_16";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Object> j_smith1 = q.createMatch(Object.class);
		DomainObjectMatch<Object> europe1 = q.createMatch(Object.class);
		DomainObjectMatch<Object> compl = q.createMatch(Object.class);
		
		q.WHERE(europe1.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith1.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith1.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Object> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith1).FORTH("pointsOfContact").TO(Object.class);
		DomainObjectMatch<Object> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Object.class);
		
		DomainObjectMatch<Object> j_smith_FilteredPocs =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_Areas).CONTAINS(europe1)
				);
		
		// build complementary set
		q.WHERE(compl).IN(j_smith_Addresses);
		q.WHERE(compl).NOT().IN(j_smith_FilteredPocs);
		
		result = q.execute();
		
		List<Object> complResult = result.resultOf(compl);
		
		List<Object> compPocs = new ArrayList<Object>();
		for (Object obj : population.getJohn_smith_addresses()) {
			if (((Address)obj).getStreet().equals("Market Street"))
				compPocs.add(obj);
		}
		compPocs.add(population.getJohn_smith_econtact());
		equals = CompareUtil.equalsUnorderedList(compPocs, complResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Select_03() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_SELECT_01.txt");
		
		Population population = new Population();
		List<Object> domObjects = population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 07 ****************************************/
		testId = "SELECT_07";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		result = q.execute();
		
		List<Object> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		
		List<Object> pocComp = new ArrayList<Object>();
		pocComp.add(population.getSchwedenPlatz_32());
		pocComp.add(population.getStachus_1());
		equals = CompareUtil.equalsUnorderedList(pocComp, j_smith_FilteredPoCsResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 08 ****************************************/
		testId = "SELECT_08";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Subject> subjects = q.createMatch(Subject.class);
		
		DomainObjectMatch<Object> addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);

		DomainObjectMatch<Subject> num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.COUNT()).EQUALS(4)
			);
		result = q.execute();
		
		List<Subject> num_addressesResult = result.resultOf(num_addresses);
		List<Subject> subjectsResult = result.resultOf(subjects);
		List<Object> addressesResult = result.resultOf(addresses);
		
		List<Object> allPocs = population.getAll_PointsOfContact();
		equals = CompareUtil.equalsUnorderedList(allPocs, addressesResult);
		assertTrue(equals);
		
		List<Subject> compSubjects = new ArrayList<Subject>();
		for (Object obj : domObjects) {
			if (obj instanceof Subject)
				compSubjects.add((Subject) obj);
		}
		equals = CompareUtil.equalsUnorderedList(compSubjects, subjectsResult);
		assertTrue(equals);
		assertTrue(num_addressesResult.size() == 1);
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), num_addressesResult.get(0));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 09 ****************************************/
		testId = "SELECT_09";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		result = q.execute();
		
		num_addressesResult = result.resultOf(num_addresses);
		
		assertTrue(num_addressesResult.size() == 1);
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), num_addressesResult.get(0));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 10 ****************************************/
		testId = "SELECT_10";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Subject.class);
		DomainObjectMatch<Area> europe_1 = q.createMatch(Area.class);
		
		q.WHERE(europe_1.atttribute("name")).EQUALS("Europe");
		
		addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		DomainObjectMatch<Object> areas_1 = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Object.class);

		num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.COUNT()).EQUALS(4),
				q.WHERE(areas_1).CONTAINS(europe_1)
			);
		result = q.execute();
		
		num_addressesResult = result.resultOf(num_addresses);
		
		assertTrue(num_addressesResult.size() == 1);
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), num_addressesResult.get(0));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDomainQuery_Select_02() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_SELECT_01.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 04 ****************************************/
		testId = "SELECT_04";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Object> j_smith = q.createMatch(Object.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Object> j_smith_PoCs =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Object.class);
		DomainObjectMatch<Object> j_smith_Areas = q.TRAVERSE_FROM(j_smith_PoCs).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Object.class);
		
		DomainObjectMatch<Object> j_smith_FilteredPoCs =
				q.SELECT_FROM(j_smith_PoCs).ELEMENTS(
						q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("Austria"),
						q.OR(),
						q.WHERE(j_smith_Areas.atttribute("region")).EQUALS("region_1")
				);
		result = q.execute();
		
		List<Object> j_smith_PoCsResult = result.resultOf(j_smith_PoCs);
		List<Object> j_smith_AreasResult = result.resultOf(j_smith_Areas);
		List<Object> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPoCs);
		
		List<PointOfContact> pocComp = new ArrayList<PointOfContact>();
		for (Object obj : population.getJohn_smith_addresses()) {
			pocComp.add((PointOfContact) obj);
		}
		pocComp.add(population.getJohn_smith_econtact());
		equals = CompareUtil.equalsUnorderedList(pocComp, j_smith_PoCsResult);
		assertTrue(equals);
		List<AbstractArea> compAreas = new ArrayList<AbstractArea>();
		for (int i = 0; i < population.getJohn_smith_addresses().size(); i++) {
			Address addr = (Address)population.getJohn_smith_addresses().get(i);
			AbstractArea ar = ((Address)addr).getArea();
			ar = ar.getPartOf();
			while (ar != null) {
				if (!compAreas.contains(ar))
					compAreas.add(ar);
				ar = ar.getPartOf();
			}
		}
		compAreas.add(population.getJohn_smith_econtact().getArea().getPartOf());
		equals = CompareUtil.equalsUnorderedList(compAreas, j_smith_AreasResult);
		assertTrue(equals);
		pocComp = new ArrayList<PointOfContact>();
		pocComp.add(population.getSchwedenPlatz_32());
		pocComp.add(population.getJohn_smith_econtact());
		equals = CompareUtil.equalsUnorderedList(pocComp, j_smith_FilteredPoCsResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 05 ****************************************/
		testId = "SELECT_05";
		queriesStream.reset();
		
		q = da1.createQuery();
		j_smith = q.createMatch(Object.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		j_smith_PoCs =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Object.class);
		j_smith_Areas = q.TRAVERSE_FROM(j_smith_PoCs).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Object.class);
		q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("Austria");
		q.OR();
		q.WHERE(j_smith_Areas.atttribute("region")).EQUALS("region_1");
		
		j_smith_FilteredPoCs =
				q.SELECT_FROM(j_smith_PoCs).ELEMENTS(
						q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("Austria"),
						q.OR(),
						q.WHERE(j_smith_Areas.atttribute("areaType")).EQUALS(AreaType.ELECTRONIC),
						q.OR(),
						q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("USA")
				);
		result = q.execute();
		
		j_smith_AreasResult = result.resultOf(j_smith_Areas);
		j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPoCs);
		
		List<Object> compAustReg1 = new ArrayList<Object>();
		compAustReg1.add(population.getAustria());
		compAustReg1.add(population.getElectronicAreaUSA());
		equals = CompareUtil.equalsUnorderedList(compAustReg1, j_smith_AreasResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(pocComp, j_smith_FilteredPoCsResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 06 ****************************************/
		testId = "SELECT_06";
		queriesStream.reset();
		
		q = da1.createQuery();
		j_smith = q.createMatch(Object.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		j_smith_PoCs =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Object.class);
		j_smith_Areas = q.TRAVERSE_FROM(j_smith_PoCs).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Object.class);
		q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("Austria");
		q.OR();
		q.WHERE(j_smith_Areas.atttribute("region")).EQUALS("region_1");
		
		j_smith_FilteredPoCs =
				q.SELECT_FROM(j_smith_PoCs).ELEMENTS(
						q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("Austria"),
						q.OR(),
						q.WHERE(j_smith_Areas.atttribute("areaType")).EQUALS(AreaType.ELECTRONIC),
						q.OR(),
						q.WHERE(j_smith_Areas.atttribute("name")).EQUALS("USA")
				);
		q.WHERE(j_smith_FilteredPoCs.atttribute("number")).EQUALS(32);
		q.OR();
		q.WHERE(j_smith_FilteredPoCs.atttribute("type")).EQUALS(EContactType.EMAIL);
		result = q.execute();
		
		j_smith_AreasResult = result.resultOf(j_smith_Areas);
		j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPoCs);
		
		equals = CompareUtil.equalsUnorderedList(compAustReg1, j_smith_AreasResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(pocComp, j_smith_FilteredPoCsResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Select_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_SELECT_01.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "SELECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		result = q.execute();
		
		List<Area> europeResult = result.resultOf(europe);
		List<Person> j_smithResult = result.resultOf(j_smith);
		List<Address> j_smith_AddressesResult = result.resultOf(j_smith_Addresses);
		List<Area> j_smith_AreasResult = result.resultOf(j_smith_Areas);
		List<Address> j_smith_FilteredAddressesResult = result.resultOf(j_smith_FilteredAddresses);
		
		assertTrue(europeResult.size() == 1);
		equals = CompareUtil.equalsObjects(population.getEurope(), europeResult.get(0));
		assertTrue(equals);
		assertTrue(j_smithResult.size() == 1);
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), j_smithResult.get(0));
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getJohn_smith_addresses(), j_smith_AddressesResult);
		assertTrue(equals);
		
		List<AbstractArea> compAreas = new ArrayList<AbstractArea>();
		for (int i = 0; i < population.getJohn_smith_addresses().size(); i++) {
			Address addr = (Address)population.getJohn_smith_addresses().get(i);
			AbstractArea ar = ((Address)addr).getArea();
			ar = ar.getPartOf();
			while (ar != null) {
				if (!compAreas.contains(ar))
					compAreas.add(ar);
				ar = ar.getPartOf();
			}
		}
		equals = CompareUtil.equalsUnorderedList(compAreas, j_smith_AreasResult);
		assertTrue(equals);
		
		List<Address> m20_sch32 = new ArrayList<Address>();
		m20_sch32.add(population.getMarketStreet_20());
		m20_sch32.add(population.getSchwedenPlatz_32());
		equals = CompareUtil.equalsUnorderedList(m20_sch32, j_smith_FilteredAddressesResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 02 ****************************************/
		testId = "SELECT_02";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		result = q.execute();
		
		List<PointOfContact> j_smith_PoCsResult = result.resultOf(j_smith_PoCs);
		j_smith_AreasResult = result.resultOf(j_smith_Areas);
		List<PointOfContact> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPoCs);
		
		List<PointOfContact> pocComp = new ArrayList<PointOfContact>();
		for (Object obj : population.getJohn_smith_addresses()) {
			pocComp.add((PointOfContact) obj);
		}
		pocComp.add(population.getJohn_smith_econtact());
		equals = CompareUtil.equalsUnorderedList(pocComp, j_smith_PoCsResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(compAreas, j_smith_AreasResult);
		assertTrue(equals);
		assertTrue(j_smith_FilteredPoCsResult.size() == 1);
		equals = CompareUtil.equalsObjects(population.getSchwedenPlatz_32(), j_smith_FilteredPoCsResult.get(0));
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 03 ****************************************/
		testId = "SELECT_03";
		queriesStream.reset();
		
		q = da1.createQuery();
		j_smith = q.createMatch(Person.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		j_smith_PoCs =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(PointOfContact.class);
		DomainObjectMatch<AbstractArea> j_smith_AbstAreas = q.TRAVERSE_FROM(j_smith_PoCs).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
		j_smith_FilteredPoCs =
				q.SELECT_FROM(j_smith_PoCs).ELEMENTS(
						q.WHERE(j_smith_AbstAreas.atttribute("name")).EQUALS("Austria"),
						q.OR(),
						q.WHERE(j_smith_AbstAreas.atttribute("region")).EQUALS("region_1")
				);
		result = q.execute();
		
		List<AbstractArea> j_smith_AbstAreasResult = result.resultOf(j_smith_AbstAreas);
		j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPoCs);
		
		compAreas = new ArrayList<AbstractArea>();
		for (int i = 0; i < population.getJohn_smith_addresses().size(); i++) {
			Address addr = (Address)population.getJohn_smith_addresses().get(i);
			AbstractArea ar = ((Address)addr).getArea();
			ar = ar.getPartOf();
			while (ar != null) {
				if (!compAreas.contains(ar))
					compAreas.add(ar);
				ar = ar.getPartOf();
			}
		}
		compAreas.add(population.getJohn_smith_econtact().getArea().getPartOf());
		equals = CompareUtil.equalsUnorderedList(compAreas, j_smith_AbstAreasResult);
		assertTrue(equals);
		
		pocComp = new ArrayList<PointOfContact>();
		pocComp.add(population.getSchwedenPlatz_32());
		pocComp.add(population.getJohn_smith_econtact());
		equals = CompareUtil.equalsUnorderedList(pocComp, j_smith_FilteredPoCsResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Traversals_02() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_TRAVERSAL_02.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "BACK_01";
		queriesStream.reset();
		
//		q = da1.createQuery();
//		DomainObjectMatch<Address> j_smith_Address = q.createMatch(Address.class);
//
//		q.WHERE(j_smith_Address.atttribute("street")).EQUALS("Market Street");
//		q.WHERE(j_smith_Address.atttribute("number")).EQUALS(20);
//		
//		DomainObjectMatch<Object> j_smith =
//				q.TRAVERSE_FROM(j_smith_Address).BACK("pointsOfContact").TO(Object.class);
//		
//		result = q.execute();
//		
//		List<Address> j_smith_AddressResult = result.resultOf(j_smith_Address);
//		List<Object> j_smith_Result = result.resultOf(j_smith);
//		
//		equals = CompareUtil.equalsObjects(population.getMarketStreet_20(), j_smith_AddressResult.get(0));
//		assertTrue(equals);
//		equals = CompareUtil.equalsUnorderedList(population.getSmithFamily_addressee(), j_smith_Result);
//		assertTrue(equals);
//		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
//		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 02 ****************************************/
		testId = "BACK_02";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		
		result = q.execute();
		
//		List<Area> europeResult = result.resultOf(europe);
//		List<Area> usaResult = result.resultOf(usa);
		List<Subject> inEuropeResult = result.resultOf(inEurope);
		List<Subject> inUsaResult = result.resultOf(inUsa);
		List<Subject> inEuropeAndUsaResult = result.resultOf(inEuropeAndUsa);
		
		equals = CompareUtil.equalsUnorderedList(population.getSubjectsInEurope(), inEuropeResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getSubjectsInUsa(), inUsaResult);
		assertTrue(equals);
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), inEuropeAndUsaResult.get(0));
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 03 ****************************************/
		testId = "BACK_03";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		
		// execute the query
		result = q.execute();
		
		// retrieve the list of matching domain objects.
		// It will contain all other persons living at 'John Smith's' Address(es).
		List<Person> j_smith_residents = result.resultOf(j_smith_residentsMatch);
		
		equals = CompareUtil.equalsUnorderedList(population.getSmithFamily_no_john(), j_smith_residents);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		return;
	}
	
	@Test
	public void testDomainQuery_Traversals_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_TRAVERSAL_01.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "TRAVERSAL_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Person> j_smith = q.createMatch(Person.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Address> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Address.class);
		
		result = q.execute();
		
		List<Address> j_smith_AddressesResult = result.resultOf(j_smith_Addresses);
		
		equals = CompareUtil.equalsUnorderedList(population.getJohn_smith_addresses(), j_smith_AddressesResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 02 ****************************************/
		testId = "TRAVERSAL_02";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		
		result = q.execute();
		
		List<Subject> subsResult = result.resultOf(subs);
		List<PointOfContact> subs_ContactsResult = result.resultOf(subs_Contacts);
		List<Area> areasResult = result.resultOf(areas);
		
		equals = CompareUtil.equalsUnorderedList(population.getJohn_smith_globcom(), subsResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getJohn_smith_globcom_contacts(), subs_ContactsResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getAreas_sf_vienna_01_munich(), areasResult);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 03 ************** with pagination ********************/
		testId = "TRAVERSAL_03";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		
		result = q.execute();
		
		List<Subject> subs_1Result = result.resultOf(subs_1);
		List<PointOfContact> subs_1_ContactsResult = result.resultOf(subs_1_Contacts);
		List<Area> areas_1Result = result.resultOf(areas_1);
		
		List<Object> subs_1Comp = population.getJohn_smith_globcom().subList(1, 2);
		equals = CompareUtil.equalsList(subs_1Comp, subs_1Result);
		assertTrue(equals);
		List<Object> contactsComp = population.getJohn_smith_globcom_contacts().subList(2, 4);
//		System.out.println(contactsComp);
//		System.out.println(subs_1_ContactsResult);
		equals = CompareUtil.equalsList(contactsComp, subs_1_ContactsResult);
		assertTrue(equals);
		List<Object> areas_1Comp = population.getAreas_sf_vienna_01_munich().subList(0, 1);
//		System.out.println(areas_1Comp);
//		System.out.println(areas_1Result);
		equals = CompareUtil.equalsList(areas_1Comp, areas_1Result);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 04 ****************************************/
		testId = "TRAVERSAL_04";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Person> j_smith_1 = q.createMatch(Person.class);

		q.WHERE(j_smith_1.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith_1.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Area> j_smith_1_Areas2 =
				q.TRAVERSE_FROM(j_smith_1).FORTH("pointsOfContact")
					.FORTH("area").FORTH("partOf").DISTANCE(1, -1).TO(Area.class);
		
		result = q.execute();
		
		List<Area> j_smith_1_Areas2Result = result.resultOf(j_smith_1_Areas2);
		equals = CompareUtil.equalsUnorderedList(population.getAreas_calif_vienna_munich_up(), j_smith_1_Areas2Result);
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 05 ****************************************/
		testId = "TRAVERSAL_05";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<Subject> j_smith_comp = q.createMatch(Subject.class);

		q.WHERE(j_smith_comp.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith_comp.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<PointOfContact> pocs = q.TRAVERSE_FROM(j_smith_comp)
				.FORTH("pointsOfContact").TO(PointOfContact.class);
		q.WHERE(pocs.atttribute("street")).LIKE("Mark.*");
		
		result = q.execute();
		
		List<PointOfContact> pocsResult = result.resultOf(pocs);
		equals = CompareUtil.equalsObjects(population.getMarketStreet_20(), pocsResult.get(0));
		assertTrue(equals);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 06 ****************************************/
		testId = "TRAVERSAL_06";
		queriesStream.reset();
		
		q = da1.createQuery();
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
		
		result = q.execute();
		CountQueryResult cResult = q.executeCount();
		
//		areas_1Result = result.resultOf(areas_1);
//		List<Area> areas_2Result = result.resultOf(areas_2);
		areasResult = result.resultOf(areas);
		equals = CompareUtil.equalsUnorderedList(population.getAreas_sf_vienna_munich(), areasResult);
		assertTrue(equals);
		
		long areasCount = cResult.countOf(areas);
		assertEquals(3, areasCount);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 07 ****************************************/
		testId = "TRAVERSAL_07";
		queriesStream.reset();
		
		q = da1.createQuery();
		// create a DomainObjectMatch for objects of type Subject
		DomainObjectMatch<Subject> smith_globcomMatch = q.createMatch(Subject.class);
		
		// Constrain the set of Subjects to contain
		// 'John Smith' and 'Global Company' only
		q.WHERE(smith_globcomMatch.atttribute("name")).EQUALS("Global Company");
		q.OR();
		q.BR_OPEN();
			q.WHERE(smith_globcomMatch.atttribute("lastName")).EQUALS("Smith");
			q.WHERE(smith_globcomMatch.atttribute("firstName")).EQUALS("John");
		q.BR_CLOSE();
		
		// Start with the set containing 'John Smith' and 'Global Company'.
		// navigate forward via attribute 'pointsOfContact'
		//          (defined in abstract super class 'Subject'),
		// navigate forward via attribute 'area',
		// end matching objects of type Area
		// (these are the immediate areas referenced by addresses
		// possibly: Cities, Urban Districts, Villages, ...).
		DomainObjectMatch<Area> immediateAreasMatch =
			q.TRAVERSE_FROM(smith_globcomMatch).FORTH("pointsOfContact")
				.FORTH("area").TO(Area.class);
		q.WHERE(immediateAreasMatch.atttribute("areaType")).EQUALS(AreaType.CITY);
		
		// execute the query
		result = q.execute();
		
		List<Area> immediateAreas = result.resultOf(immediateAreasMatch);
		List<Area> sf = new ArrayList<Area>();
		sf.add(population.getSanFrancisco());
		sf.add(population.getMunich());
		equals = CompareUtil.equalsUnorderedList(sf, immediateAreas);
		assertTrue(equals);
		
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 08 ****************************************/
		testId = "TRAVERSAL_08";
		queriesStream.reset();
		
		q = da1.createQuery();
		j_smith = q.createMatch(Person.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<AbstractArea> jsAreas = q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		result = q.execute();
		
		List<AbstractArea> jsAreasResult = result.resultOf(jsAreas);
		
		Person js = population.getJohn_smith();
		List<AbstractArea> compAreas = new ArrayList<AbstractArea>();
		for (PointOfContact poc : js.getPointsOfContact()) {
			if (poc instanceof Address) {
				AbstractArea ar = ((Address)poc).getArea();
				ar = ar.getPartOf();
				while (ar != null) {
					if (!compAreas.contains(ar))
						compAreas.add(ar);
					ar = ar.getPartOf();
				}
			}
			if (poc instanceof EContact) {
				AbstractArea ar = ((EContact)poc).getArea();
				ar = ar.getPartOf();
				while (ar != null) {
					if (!compAreas.contains(ar))
						compAreas.add(ar);
					ar = ar.getPartOf();
				}
			}
		}
		
		equals = CompareUtil.equalsUnorderedList(compAreas, jsAreasResult);
		assertTrue(equals);
		
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_Contains_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_PREDICATES_01.txt");
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "PREDICATES_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		DomainObjectMatch<NumberHolder> nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(2);
		
		result = q.execute();
		
		List<NumberHolder> nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 1 && equalsIntArrays(nhResult.get(0).getNumbers(), new int[]{1,2,3}));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 02 ****************************************/
		testId = "PREDICATES_02";
		queriesStream.reset();
		
		q = da1.createQuery();
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(2);
		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(5);
		
		result = q.execute();
		
		nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 0);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 03 ****************************************/
		testId = "PREDICATES_03";
		queriesStream.reset();
		
		q = da1.createQuery();
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(2);
		q.OR();
		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(5);
		
		result = q.execute();
		
		nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 2 && (equalsIntArrays(nhResult.get(0).getNumbers(), new int[]{1,2,3}) ||
				equalsIntArrays(nhResult.get(1).getNumbers(), new int[]{1,2,3})) &&
				(equalsIntArrays(nhResult.get(0).getNumbers(), new int[]{4,5,6}) ||
						equalsIntArrays(nhResult.get(1).getNumbers(), new int[]{4,5,6})));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 04 ****************************************/
		testId = "PREDICATES_04";
		queriesStream.reset();
		
		q = da1.createQuery();
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(2, 3);
		
		result = q.execute();
		
		nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 1 && equalsIntArrays(nhResult.get(0).getNumbers(), new int[]{1,2,3}));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 05 ****************************************/
		testId = "PREDICATES_05";
		queriesStream.reset();
		
		q = da1.createQuery();
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(2, 5);
		
		result = q.execute();
		
		nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 0);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 06 ****************************************/
		testId = "PREDICATES_06";
		queriesStream.reset();
		
		q = da1.createQuery();
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(3, 4);
		
		result = q.execute();
		
		nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 1 && equalsIntArrays(nhResult.get(0).getNumbers(), new int[]{3,4,7}));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 07 ****************************************/
		testId = "PREDICATES_07";
		queriesStream.reset();
		
		q = da1.createQuery();
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers")).CONTAINS_elements(3);
		
		result = q.execute();
		
		nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 2 && (equalsIntArrays(nhResult.get(0).getNumbers(), new int[]{1,2,3}) ||
				equalsIntArrays(nhResult.get(1).getNumbers(), new int[]{1,2,3})) &&
				(equalsIntArrays(nhResult.get(0).getNumbers(), new int[]{3,4,7}) ||
						equalsIntArrays(nhResult.get(1).getNumbers(), new int[]{3,4,7})));
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 08 ****************************************/
		testId = "PREDICATES_08";
		queriesStream.reset();
		
		q = da1.createQuery();
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers").length()).EQUALS(3);
		
		result = q.execute();
		
		nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 3);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		/** 09 ****************************************/
		testId = "PREDICATES_09";
		queriesStream.reset();
		
		q = da1.createQuery();
		nh = q.createMatch(NumberHolder.class);

		q.WHERE(nh.collectionAtttribute("numbers").length()).EQUALS(2);
		
		result = q.execute();
		
		nhResult = result.resultOf(nh);
		assertTrue(nhResult.size() == 0);
		qCypher = TestDataReader.trimComments(queriesStream.toString().trim());
		assertQuery(testId, qCypher, tdr.getTestData(testId));
		
		return;
	}
	
	@Test
	public void testDomainQuery_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		
		Population population = new Population();
		population.createPopulation();
		
		List<Object> domainObjects = storedDomainObjects;
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/******************************************/
		q = da1.createQuery();
		DomainObjectMatch<Subject> smith_0 = q.createMatch(Subject.class);
		q.WHERE(smith_0.stringAtttribute("firstName")
				.concat(smith_0.stringAtttribute("lastName"))).EQUALS("JohnSmith");
		
		result = q.execute();
		
		List<Subject> smith_0Result = result.resultOf(smith_0);
		assertTrue(smith_0Result.size() == 1);
		equals = CompareUtil.equalsObjects(population.getJohn_smith(), smith_0Result.get(0));
		assertTrue(equals);
		
		/******************************************/
		q = da1.createQuery();
		DomainObjectMatch<Subject> smith_false = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> bergHammer = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> smith_true = q.createMatch(Subject.class);
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
		
		q.WHERE(smith_true.stringAtttribute("firstName")).EQUALS("Angelina");
		q.WHERE(smith_true.stringAtttribute("matchString")).EQUALS("smith");
		
		q.parameter("lastName").setValue("Smith");
		
		result = q.execute();

		List<Subject> smith_falseResult = result.resultOf(smith_false);
		List<Subject> bergHammerResult = result.resultOf(bergHammer);
		List<Subject> smith_trueResult = result.resultOf(smith_true);
		
		assertTrue(smith_falseResult.isEmpty());
		equals = CompareUtil.equalsUnorderedList(population.getBerghammers(), bergHammerResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getAngelina_smith(), smith_trueResult);
		assertTrue(equals);
		
		/******************************************/
		q = da1.createQuery();
		DomainObjectMatch<Subject> smith_1 = q.createMatch(Subject.class);
		q.WHERE(smith_1.stringAtttribute("lastName")).EQUALS(q.parameter("lastName"));
		q.WHERE(smith_1.stringAtttribute("firstName")).NOT().EQUALS("Caroline");
		q.WHERE(smith_1.stringAtttribute("firstName")).NOT().EQUALS("Angelina");
		
		q.parameter("lastName").setValue("Smith");
		
		result = q.execute();
		
		List<Subject> smith_1Result = result.resultOf(smith_1);
		equals = CompareUtil.equalsUnorderedList(population.getJohn_jery_smith(), smith_1Result);
		assertTrue(equals);
		
		/******************************************/
		q = da1.createQuery();
		DomainObjectMatch<Subject> has_clark_firstName = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> has_clark_firstName_2 = q.createMatch(Subject.class);
		DomainObjectMatch<Subject> clark = q.createMatch(Subject.class);
		q.WHERE(clark.atttribute("lastName")).EQUALS("Clark");
		q.WHERE(has_clark_firstName.atttribute("firstName")).EQUALS(clark.atttribute("firstName"));
		q.WHERE(has_clark_firstName_2.atttribute("firstName")).EQUALS(clark.atttribute("firstName"));
		q.WHERE(has_clark_firstName_2).NOT().IN(clark);
		
		result = q.execute();
		List<Subject> clarkResult = result.resultOf(clark);
		List<Subject> has_clark_firstNameResult = result.resultOf(has_clark_firstName);
		List<Subject> has_clark_firstName_2Result = result.resultOf(has_clark_firstName_2);
		equals = CompareUtil.equalsUnorderedList(population.getAngie_clark(), clarkResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getAngies(), has_clark_firstNameResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getAngelina_smith(), has_clark_firstName_2Result);
		assertTrue(equals);
		
		/******************************************/
		q = da1.createQuery();
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
		
		result = q.execute();
		List<Subject> matchingExcludeResult = result.resultOf(matchingExclude);
		List<Subject> matchingIncludeResult = result.resultOf(matchingInclude);
		List<Subject> matchResult = result.resultOf(match);
		List<Subject> allSubjectsResult = result.resultOf(allSubjects);
		equals = CompareUtil.equalsUnorderedList(population.getWatson_company(), matchResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getMaier_clark(), matchingExcludeResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(domainObjects.subList(0, 13), allSubjectsResult);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getWatson_company(), matchingIncludeResult);
		assertTrue(equals);
		
		/************ for explorative testing ******************************/
//		q = da1.createQuery();
//		DomainObjectMatch<Subject> set_1 = q.createMatch(Subject.class);
//		DomainObjectMatch<Subject> set_2 = q.createMatch(Subject.class);
//		DomainObjectMatch<Subject> intersection = q.createMatch(Subject.class);
		// a set with all smiths and christa berghammer and global company
//		q.BR_OPEN();
//		q.WHERE(intersection.atttribute("lastName")).EQUALS("Berghammer");
//		q.OR(); // 2
//		q.BR_OPEN();
//			q.WHERE(set_1.atttribute("lastName")).EQUALS("Smith");
//			q.WHERE(intersection.atttribute("lastName")).EQUALS("Berghammer");
//			q.OR();	// 2, 5
//			q.BR_OPEN();
//				q.WHERE(set_1.atttribute("lastName")).EQUALS("Berghammer");
//				q.WHERE(set_1.atttribute("firstName")).EQUALS("Christa");
//			q.BR_CLOSE();
//			q.OR();	// 5, 8
//			q.WHERE(set_1.atttribute("name")).EQUALS("Global Company");
//		q.BR_CLOSE();
		
		// a set with all berghammers and global company
//		q.WHERE(set_2.atttribute("lastName")).EQUALS("Berghammer");
//		q.OR();	// 7, 10
//		q.WHERE(set_1.atttribute("name")).EQUALS("Global Company");
		
		// the intersction of both sets
//		q.WHERE(intersection).IN(set_1);
//		q.OR();	// 9, 12
//		q.WHERE(intersection).IN(set_2);
//		q.OR();	// 11, 14
//		q.WHERE(set_1.atttribute("name")).EQUALS("Global Company2");
//		q.OR();	// 11, 15
//		q.WHERE(set_1.atttribute("name")).EQUALS("Global Company3");
//		q.BR_CLOSE();
		
//		result = q.execute();
//		List<Subject> set_1Result = result.resultOf(set_1);
//		List<Subject> set_2Result = result.resultOf(set_2);
//		List<Subject> intersectionResult = result.resultOf(intersection);
		
		/******************************************/
		q = da1.createQuery();
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
		
		result = q.execute();
		List<Subject> set_1Result = result.resultOf(set_1);
		List<Subject> set_2Result = result.resultOf(set_2);
		List<Subject> intersectionResult = result.resultOf(intersection);
		
		equals = CompareUtil.equalsUnorderedList(population.getSmiths_christa_berghammer_globcom(), set_1Result);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getBerghammers_globcom(), set_2Result);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(population.getChrista_berghammer_globcom(), intersectionResult);
		assertTrue(equals);
		
		/******************************************/
		q = da1.createQuery();
		DomainObjectMatch<Subject> subjects = q.createMatch(Subject.class);
		q.ORDER(subjects).BY("lastName");
		q.ORDER(subjects).BY("firstName").DESCENDING();
		q.ORDER(subjects).BY("name").DESCENDING();
		
		CountQueryResult countResult = q.executeCount();
		long count = countResult.countOf(subjects);
		
		assertEquals(13, count);
		
		result = q.execute();
		List<Subject> subjectsResult = result.resultOf(subjects);
		List<Subject> sortedSubjects = new ArrayList<Subject>();
		for (Object obj : domainObjects.subList(0, 13)) {
			sortedSubjects.add((Subject) obj);
		}
		Collections.sort(sortedSubjects, new SubjectComparator());
		equals = CompareUtil.equalsList(sortedSubjects, subjectsResult);
		assertTrue(equals);
		
		/******************************************/
		q = da1.createQuery();
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
		
//		CountQueryResult countRes = q.executeCount();
//		long numSubjects = countRes.countOf(subjectsPage);
		
		result = q.execute();
		List<Subject> set_00Result = result.resultOf(set_00);
		List<Subject> set_01Result = result.resultOf(set_01);
		List<Subject> set_02Result = result.resultOf(set_02);
		List<Subject> intersectionPageResult = result.resultOf(intersectionPage);
		
		equals = CompareUtil.equalsUnorderedList(domainObjects.subList(0, 13), set_00Result);
		assertTrue(equals);
		List<Subject> set_01Comp = set_00Result.subList(5, 10);
		equals = CompareUtil.equalsList(set_01Comp, set_01Result);
		assertTrue(equals);
		equals = CompareUtil.equalsUnorderedList(set_00Result, set_02Result);
		assertTrue(equals);
		List<Subject> intersectionPageComp = set_00Result.subList(1, 6);
		equals = CompareUtil.equalsList(intersectionPageComp, intersectionPageResult);
		assertTrue(equals);
		
		intersectionPage.setPage(4, 5); // change page
		intersectionPageResult = result.resultOf(intersectionPage);
		
		intersectionPageComp = set_00Result.subList(4, 9);
		equals = CompareUtil.equalsList(intersectionPageComp, intersectionPageResult);
		assertTrue(equals);
		equals = CompareUtil.listHasIdentContent(intersectionPageComp, intersectionPageResult);
		assertTrue(equals);
		
		/******************************************/
		q = da1.createQuery();
		smith_1 = q.createMatch(Subject.class);
		q.WHERE(smith_1.stringAtttribute("lastName")).EQUALS(q.parameter("lastName"));
		q.WHERE(smith_1.stringAtttribute("firstName")).IN_list("Caroline", "Angelina");
		
		q.parameter("lastName").setValue("Smith");
		
		result = q.execute();
		
		smith_1Result = result.resultOf(smith_1);
		equals = CompareUtil.equalsUnorderedList(population.getCaro_angie_smith(), smith_1Result);
		assertTrue(equals);
		
		/******************************************/
		q = da1.createQuery();
		smith_1 = q.createMatch(Subject.class);
		q.WHERE(smith_1.stringAtttribute("lastName")).EQUALS(q.parameter("lastName"));
		q.WHERE(smith_1.stringAtttribute("firstName")).NOT().IN_list("John", "Jeremy");
		
		q.parameter("lastName").setValue("Smith");
		
		result = q.execute();
		
		smith_1Result = result.resultOf(smith_1);
		equals = CompareUtil.equalsUnorderedList(population.getCaro_angie_smith(), smith_1Result);
		assertTrue(equals);
		
		return;
	}
	
	private boolean equalsIntArrays(int[] a1, int[] a2) {
		if (a1 == a2)
			return true;
		if (a1 == null || a2 == null)
			return false;
		if (a1.length != a2.length)
			return false;
		for (int i = 0; i < a1.length; i++) {
			if (a1[i] != a2[i])
				return false;
		}
		return true;
	}
	
	/***************************************/
	public static class SubjectComparator implements Comparator<Subject> {

		@Override
		public int compare(Subject o1, Subject o2) {
			if (o1.getClass() != o2.getClass())
				return o1.getClass().getName().compareTo(o2.getClass().getName());
			if (o1.getClass().equals(Company.class))
				return ((Company)o2).getName().compareTo(((Company)o1).getName());
			if (o1.getClass().equals(Person.class)) {
				Person p1 = (Person)o1;
				Person p2 = (Person)o2;
				if (!p1.getLastName().equals(p2.getLastName()))
					return p1.getLastName().compareTo(p2.getLastName());
				return p2.getFirstName().compareTo(p1.getFirstName());
			}
			return 0;
		}
		
	}
}
