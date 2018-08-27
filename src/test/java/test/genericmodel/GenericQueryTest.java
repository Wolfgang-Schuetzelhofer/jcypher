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

package test.genericmodel;

import static org.junit.Assert.assertEquals;
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
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.api.DomainObjectMatch;
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
import test.DBAccessSettings;
import util.TestDataReader;

//@Ignore
public class GenericQueryTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	
	@BeforeClass
	public static void before() {
		domainName = "PEOPLE-DOMAIN"; // "QTEST-DOMAIN";
		dbAccess = DBAccessSettings.createDBAccess();
		
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
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENQUERY_03.txt");
		
		/** 01 ****************************************/
		q = gda.createQuery();
		DomainObjectMatch<DomainObject> smith = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//result = q.execute();
		
		q1 = gda.createQuery();
		DomainObjectMatch<DomainObject> j_smith = q1.createMatchFrom(smith);
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainQueryResult result1 = q1.execute();
		
		//List<Person> smithResult = result.resultOf(smith);
		List<DomainObject> j_smithResult = result1.resultOf(j_smith);
		
		DOToString doToString = new DOToString(Format.PRETTY_1, 0);
		DOWalker walker = new DOWalker(j_smithResult, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData("CONCAT_01"), str);
		
		/** 02 ****************************************/
		gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		q = gda.createQuery();
		smith = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		q.ORDER(smith).BY("firstName");
		
		q.WHERE(smith.atttribute("lastName")).EQUALS("Smith");
		//q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		DomainQueryResult result = q.execute();
		
		List<DomainObject> smithResult = result.resultOf(smith);
		
		q1 = gda.createQuery();
		j_smith = q1.createMatchFor(smithResult, "iot.jcypher.samples.domain.people.model.Subject");
		q1.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		result1 = q1.execute();
		
		j_smithResult = result1.resultOf(j_smith);
		
		doToString = new DOToString(Format.PRETTY_1, 0);
		walker = new DOWalker(smithResult, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData("CONCAT_02"), str);
		
		doToString = new DOToString(Format.PRETTY_1, 0);
		walker = new DOWalker(j_smithResult, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData("CONCAT_03"), str);
		
		return;
	}
	
	@Test
	public void testGenericQueryIntersection_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENQUERY_03.txt");
		
		/** 01 ****************************************/
		q = gda.createQuery();
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
		
		List<DomainObject> siblings1 = result.resultOf(siblings1Match);
		List<DomainObject> siblings2 = result.resultOf(siblings2Match);
		List<DomainObject> siblings1Plus = result.resultOf(siblings1PlusMatch);
		
		DOToString doToString = new DOToString(Format.PRETTY_1, 0);
		DOWalker walker = new DOWalker(siblings1, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData("INTERSECTION_01"), str);
		
		doToString = new DOToString(Format.PRETTY_1, 0);
		walker = new DOWalker(siblings2, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData("INTERSECTION_02"), str);
		
		doToString = new DOToString(Format.PRETTY_1, 0);
		walker = new DOWalker(siblings1Plus, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData("INTERSECTION_03"), str);
		
		return;
	}
	
	@Test
	public void testGenericQueryUnion_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		String testId;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENQUERY_03.txt");
		
		/** 01 ****************************************/
		testId = "UNION_01";
		q = gda.createQuery();
		DomainObjectMatch<DomainObject> smithMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		DomainObjectMatch<DomainObject> bergHammerMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");
		
		q.WHERE(smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(bergHammerMatch.atttribute("lastName")).EQUALS("Berghammer");
		DomainObjectMatch<DomainObject> unionMatch = q.UNION(smithMatch, bergHammerMatch);
		q.ORDER(unionMatch).BY("lastName");
		q.ORDER(unionMatch).BY("firstName");
		
		DomainQueryResult result = q.execute();
		
		List<DomainObject> smith = result.resultOf(smithMatch);
		List<DomainObject> bergHammer = result.resultOf(bergHammerMatch);
		List<DomainObject> union = result.resultOf(unionMatch);
		
		assertEquals(4, smith.size());
		assertEquals(6, bergHammer.size());
		assertEquals(10, union.size());
		
		DOToString doToString = new DOToString(Format.PRETTY_1, 1);
		DOWalker walker = new DOWalker(union, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData(testId), str);
		
		/** 02 ****************************************/
		addAddress();
		gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		testId = "UNION_02";
		
		q = gda.createQuery();
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
				.FORTH("partOf").DISTANCE(1, -1).TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
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
		
		List<DomainObject> j_smith_Addresses = result.resultOf(j_smith_AddressesMatch);
		List<DomainObject> j_smith_allAreas = result.resultOf(j_smith_all_Areas);
		List<DomainObject> j_smith_Filtered_4Result = result.resultOf(j_smith_Filtered_4);
		List<DomainObject> j_smith_Filtered_5Result = result.resultOf(j_smith_Filtered_5);
		
		assertEquals(4, j_smith_Addresses.size());
		assertEquals(11, j_smith_allAreas.size());
		assertEquals(1, j_smith_Filtered_4Result.size());
		assertEquals(2, j_smith_Filtered_5Result.size());
		
		doToString = new DOToString(Format.PRETTY_1, 1);
		walker = new DOWalker(j_smith_Filtered_4Result, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData("UNION_02"), str);
		
		j_smith_Filtered_5Result = Util.sortAddresses(j_smith_Filtered_5Result);
		doToString = new DOToString(Format.PRETTY_1, 1);
		walker = new DOWalker(j_smith_Filtered_5Result, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData("UNION_03"), str);
		
		initDB();
		
		return;
	}
	
	@Test
	public void testGenericQueryCollect_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		/** 01 ****************************************/
		
		q = gda.createQuery();
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
		
		long numFirstNames = countResult.countOf(firstNamesMatch);
		long numLastNames = countResult.countOf(lastNamesMatch);
		long numSubjects = countResult.countOf(subjectsMatch);
		List<String> firstNames = result.resultOf(firstNamesMatch);
		List<String> lastNames = result.resultOf(lastNamesMatch);
		
		assertEquals(14, numFirstNames);
		assertEquals(6, numLastNames);
		assertEquals(17, numSubjects);
		
		assertEquals("[Angelina, Caroline, Christa, Clark, Fritz]", firstNames.toString());
		assertEquals("[Watson, Smith, Maier, Kent, Clark, Berghammer]", lastNames.toString());
		
		return;
	}
	
	@Test
	public void testGenericQueryReject_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		String testId;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENQUERY_02.txt");
		
		/** 01 ****************************************/
		q = gda.createQuery();
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
		
		List<DomainObject> j_smith_Addresses_pResult = result.resultOf(j_smith_Addresses_p);
		List<DomainObject> j_smith_FilteredPoCs_pResult = result.resultOf(j_smith_FilteredPocs_p);
		
		testId = "GENQUERY_2_01";
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(j_smith_Addresses_pResult, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData(testId), str);
		
		testId = "GENQUERY_2_02";
		doToString = new DOToString(Format.PRETTY_1);
		walker = new DOWalker(j_smith_FilteredPoCs_pResult, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData(testId), str);
		
		return;
	}
	
	@Test
	public void testGenericQuerySelect_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		String testId;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENQUERY_01.txt");
		
		/** 01 ****************************************/
		testId = "GENQUERY_03";
		q = gda.createQuery();
		
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
		
//		List<DomainObject> j_smithResult = result.resultOf(j_smith);
//		List<DomainObject> europeResult = result.resultOf(europe);
//		List<DomainObject> j_smith_AddressesResult = result.resultOf(j_smith_Addresses);
//		List<DomainObject> j_smith_AreasResult = result.resultOf(j_smith_Areas);
		List<DomainObject> j_smith_FilteredPocsResult = result.resultOf(j_smith_FilteredPocs);
		
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(j_smith_FilteredPocsResult, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData(testId), str);
		
		/** 02 ****************************************/
		testId = "GENQUERY_05";
		
		q = gda.createQuery();
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
		
		List<DomainObject> allSubjectsResult = result.resultOf(allSubjects);
		List<DomainObject> num_addressesResult = result.resultOf(num_addresses);
		
		doToString = new DOToString(Format.PRETTY_1, 1);
		walker = new DOWalker(allSubjectsResult, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData(testId), str);
		
		testId = "GENQUERY_06";
		doToString = new DOToString(Format.PRETTY_1, 1);
		walker = new DOWalker(num_addressesResult, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData(testId), str);
		
		CountQueryResult cres = q.executeCount();
		
		long allCount = cres.countOf(allSubjects);
		long subCount = cres.countOf(num_addresses);
		
		assertEquals(allCount, 17);
		assertEquals(subCount, 12);
		
		return;
	}
	
	@Test
	public void testGenericQueryTraverse_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		String testId;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENQUERY_01.txt");
		
		/****** Forward-Backward Traversal ***************/
		// create a DomainQuery object
		q = gda.createQuery();
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
		
		// retrieve the list of matching domain objects.
		// It will contain all other persons living at 'John Smith's' Address(es).
		List<DomainObject> j_smith_residents = result.resultOf(j_smith_residentsMatch);
		
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(j_smith_residents, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		testId = "GENQUERY_04";
		assertEquals(tdr.getTestData(testId), str);
		
		return;
	}
	
	@Test
	public void testGenericQuery_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		String testId;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENQUERY_01.txt");
		
		/** 01 ****************************************/
		testId = "GENQUERY_01";
		q = gda.createQuery();
		DomainObjectMatch<DomainObject> j_smithMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<DomainObject> j_smith_AddressesMatch =
				q.TRAVERSE_FROM(j_smithMatch).FORTH("pointsOfContact")
					.TO_GENERIC("iot.jcypher.samples.domain.people.model.PointOfContact");
		q.ORDER(j_smith_AddressesMatch).BY("street").DESCENDING();
		
		DomainQueryResult result = q.execute();
		
		List<DomainObject> j_smith = result.resultOf(j_smithMatch);
		List<DomainObject> j_smith_Addresses = result.resultOf(j_smith_AddressesMatch);
		
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(j_smith, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData(testId), str);
		
		testId = "GENQUERY_02";
		doToString = new DOToString(Format.PRETTY_1);
		walker = new DOWalker(j_smith_Addresses, doToString);
		walker.walkDOGraph();
		str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		assertEquals(tdr.getTestData(testId), str);
		
		return;
	}
}
