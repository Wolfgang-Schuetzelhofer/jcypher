/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
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
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.AreaType;
import test.domainquery.model.Company;
import test.domainquery.model.Person;
import test.domainquery.model.PointOfContact;
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
		domainName = "QTEST-DOMAIN";
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
		
		// init db
		Population population = new Population();
		
		storedDomainObjects = population.createPopulation();
		
		QueriesPrintObserver.addOutputStream(System.out);
		queriesStream = new ByteArrayOutputStream();
		QueriesPrintObserver.addOutputStream(queriesStream);
		
		QueriesPrintObserver.addToEnabledQueries("COUNT QUERY", ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries("DOM QUERY", ContentToObserve.CYPHER);
		
//		List<JcError> errors = dbAccess.clearDatabase();
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
//		
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
		storedDomainObjects = null;
		try {
			queriesStream.close();
		} catch (IOException e) {}
		queriesStream = null;
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
	}
	
	@Test
	public void testDomainQuery_Traversals_02() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		String testId;
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 07 ****************************************/
//		testId = "TRAVERSAL_07";
//		queriesStream.reset();
//		
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
		
		/** 08 ****************************************/
		testId = "TRAVERSAL_08";
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
		
		List<Area> europeResult = result.resultOf(europe);
		List<Area> usaResult = result.resultOf(usa);
		List<Subject> inEuropeResult = result.resultOf(inEurope);
		List<Subject> inUsaResult = result.resultOf(inUsa);
		List<Subject> inEuropeAndUsaResult = result.resultOf(inEuropeAndUsa);
		
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
		equals = CompareUtil.equalsUnorderedList(population.getAreas_sf_vienna_01(), areasResult);
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
		List<Object> areas_1Comp = population.getAreas_sf_vienna_01().subList(0, 1);
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
		equals = CompareUtil.equalsUnorderedList(population.getAreas_calif_vienna_up(), j_smith_1_Areas2Result);
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
		equals = CompareUtil.equalsUnorderedList(population.getAreas_sf_vienna(), areasResult);
		assertTrue(equals);
		
		long areasCount = cResult.countOf(areas);
		assertEquals(2, areasCount);
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
		equals = CompareUtil.equalsUnorderedList(domainObjects, allSubjectsResult);
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
		
		// the intersction of both sets
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
		for (Object obj : domainObjects) {
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
		
		equals = CompareUtil.equalsUnorderedList(domainObjects, set_00Result);
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
		
		return;
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
