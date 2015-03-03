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
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainquery.model.AbstractArea;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.AreaType;
import test.domainquery.model.Company;
import test.domainquery.model.EContact;
import test.domainquery.model.NumberHolder;
import test.domainquery.model.SubNumberHolder;
import test.domainquery.model.EContact.EContactType;
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
		
		NumberHolder n1 = new NumberHolder("n_123", new int[]{1, 2, 3});
		NumberHolder n2 = new NumberHolder("n_456", new int[]{4, 5, 6});
		SubNumberHolder sn1 = new SubNumberHolder("subn_347", new int[]{3, 4, 7});
		List<Object> nhs = new ArrayList<Object>();
		nhs.add(n1);
		nhs.add(n2);
		nhs.add(sn1);
		
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
//		errors = da.store(nhs);
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
	
	/** CONTAINS */
	@Test
	public void testDomainQuery_Collections_09() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
//		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_TRAVERSAL_02.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "SELECT_01";
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
		
		DomainObjectMatch<AbstractArea> areas_1 = q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);

		DomainObjectMatch<Person> num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.COUNT()).EQUALS(4),
				q.WHERE(areas.COUNT()).EQUALS(9)
			);
		result = q.execute();
		
		List<Person> num_addressesResult = result.resultOf(num_addresses);
		List<AbstractArea> areas_1Result = result.resultOf(areas_1);
		
		/** 02 ****************************************/
		testId = "SELECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Person.class);
		europe = q.createMatch(Area.class);

		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		
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
		
		/** 03 ****************************************/
		testId = "SELECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Person.class);
		europe = q.createMatch(Area.class);

		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		
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
		
		/** 04 ****************************************/
		testId = "SELECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Person.class);
		europe = q.createMatch(Area.class);

		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		
		addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
		num_addresses = q.SELECT_FROM(subjects).ELEMENTS(
				q.WHERE(addresses.atttribute("number")).EQUALS(20),
//				q.OR(),
				q.WHERE(areas).CONTAINS(europe)
			);
		result = q.execute();
		
		num_addressesResult = result.resultOf(num_addresses);
		
		/** 05 ****************************************/
		testId = "SELECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Person.class);
		europe = q.createMatch(Area.class);

		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		
		addresses = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").TO(Object.class);
		areas = q.TRAVERSE_FROM(subjects).FORTH("pointsOfContact").FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);
		
//		DomainObjectMatch<PointOfContact> poc = q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact")
//				.TO(PointOfContact.class);
//		DomainObjectMatch<AbstractArea> areas_2 = q.TRAVERSE_FROM(areas_1)
//				.FORTH("partOf").DISTANCE(0, -1).TO(AbstractArea.class);

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
//		List<Person> subjectsResult = result.resultOf(subjects);
//		List<Object> addressesResult = result.resultOf(addresses);
//		List<AbstractArea> areasResult = result.resultOf(areas);
//		List<AbstractArea> areas_2Result = result.resultOf(areas_2);
//		List<PointOfContact> pocResult = result.resultOf(poc);
		
		return;
	}
	
	/** CONTAINS */
	@Test
	public void testDomainQuery_Collections_08() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
//		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_TRAVERSAL_02.txt");
		
		Population population = new Population();
		population.createPopulation();
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "SELECT_01";
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
		
		/** 02 ****************************************/
		testId = "SELECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Subject.class);
		DomainObjectMatch<Area> europe = q.createMatch(Area.class);
		
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
		subjectsResult = result.resultOf(subjects);
		addressesResult = result.resultOf(addresses);
		
		/** 03 ****************************************/
		testId = "SELECT_01";
		queriesStream.reset();
		
		q = da1.createQuery();
		subjects = q.createMatch(Subject.class);
		DomainObjectMatch<Object> europe_1 = q.createMatch(Object.class);
		
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
		subjectsResult = result.resultOf(subjects);
		addressesResult = result.resultOf(addresses);
		
		return;
	}
	
	@Test
	public void testDomainQuery_Collections_03() {
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
		
		return;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDomainQuery_Collections_02() {
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
		pocComp.addAll((Collection<? extends PointOfContact>) population.getJohn_smith_addresses());
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDomainQuery_Collections_01() {
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
		pocComp.addAll((Collection<? extends PointOfContact>) population.getJohn_smith_addresses());
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
		boolean equals;
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
