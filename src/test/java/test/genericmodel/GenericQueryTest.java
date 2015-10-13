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
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.internal.DOWalker;
import iot.jcypher.domainquery.CountQueryResult;
import iot.jcypher.domainquery.DomainQuery;
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

import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainquery.Population;
import test.domainquery.model.AbstractArea;
import test.domainquery.model.Area;
import test.domainquery.model.PointOfContact;
import test.domainquery.model.Subject;
import util.TestDataReader;

//@Ignore
public class GenericQueryTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	
	@BeforeClass
	public static void before() {
		domainName = "PEOPLE-DOMAIN"; // "QTEST-DOMAIN";
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
//		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "jcypher");
		
		QueriesPrintObserver.addOutputStream(System.out);
		
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOMAIN_INFO, ContentToObserve.CYPHER);
		
		// init db
//		List<JcError> errors = dbAccess.clearDatabase();
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
//		LoadUtil.loadPeopleDomain(dbAccess);
		return;
	}
	
	@Test
	public void testQueryUnion_Intersection_01() {
		IDomainAccess da1;
		DomainQuery q;
		
		initTest();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		q = da1.createQuery();
		DomainObjectMatch<Subject> j_smith = q.createMatch(Subject.class);

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<PointOfContact> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(PointOfContact.class);
		DomainObjectMatch<Area> j_smith_d_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.TO(Area.class);
		DomainObjectMatch<Area> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		DomainObjectMatch<Area> j_smith_all_Areas = q.UNION(j_smith_d_Areas, j_smith_Areas);
		
		DomainObjectMatch<PointOfContact> j_smith_FilteredPocs =
				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
						q.WHERE(j_smith_all_Areas.COUNT()).EQUALS(5)
				);
		
		DomainQueryResult result = q.execute();
		
		List<Area> j_smith_all = result.resultOf(j_smith_all_Areas);
		List<PointOfContact> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		
		return;
	}
	
	private void initTest() {
		Population population = new Population();
		
		List<Object> storedDomainObjects = population.createPopulation();
		
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
	}
	
	@Test
	public void testGenericQueryUnion_Intersection_01() {
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
		testId = "UNION_02";
		
		q = gda.createQuery();
		DomainObjectMatch<DomainObject> j_smith = q.createMatch("iot.jcypher.samples.domain.people.model.Subject");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<DomainObject> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO_GENERIC("iot.jcypher.samples.domain.people.model.PointOfContact");
		DomainObjectMatch<DomainObject> j_smith_d_Areas =
				q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		DomainObjectMatch<DomainObject> j_smith_Areas =
				q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		@SuppressWarnings("unchecked")
		DomainObjectMatch<DomainObject> j_smith_all_Areas = q.UNION(j_smith_d_Areas, j_smith_Areas);
		
		DomainObjectMatch<DomainObject> j_smith_FilteredPocs =
			q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
				q.WHERE(j_smith_all_Areas.COUNT()).EQUALS(5)
		);
		result = q.execute();
		
		List<DomainObject> j_smith_all = result.resultOf(j_smith_all_Areas);
		List<DomainObject> j_smith_FilteredPoCsResult = result.resultOf(j_smith_FilteredPocs);
		
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
