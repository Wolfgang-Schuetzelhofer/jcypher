/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
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

package test.querypersist;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
import iot.jcypher.domainquery.QueryLoader;
import iot.jcypher.domainquery.QueryMemento;
import iot.jcypher.domainquery.QueryPersistor;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.internal.JSONConverter;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import test.AbstractTestSuite;
import test.domainquery.Population;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.Person;
import util.TestDataReader;

//@Ignore
public class QueryPersistorTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	
	@Test
	public void testPersist_03() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_SELECT_01.txt");
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 03 ****************************************/
		testId = "PERSIST_01";
		
		q = da1.createQuery();
		QueryPersistor qPersistor = da1.createQueryPersistor(q);
		
		Parameter lastName = q.parameter("lastName");
		lastName.setValue("Smith");
		DomainObjectMatch<Person> smiths = q.createMatch(Person.class);
		q.WHERE(smiths.atttribute("lastName")).EQUALS(lastName);
		
		DomainObjectMatch<Area> europe = q.createMatch(Area.class);
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		
		DomainObjectMatch<Area> smithAreas = 
				q.TRAVERSE_FROM(smiths).FORTH("pointsOfContact").FORTH("area").FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		
		DomainObjectMatch<Person> smithsInEurope = q.SELECT_FROM(smiths).ELEMENTS(
				q.WHERE(smithAreas).CONTAINS(europe)
		);

		qPersistor.augment(smiths, "smiths")
		.augment(smithsInEurope, "smithsInEurope")
		.augment(europe, "europe")
		.augment(smithAreas, "smithAreas");
		QueryMemento qm = qPersistor.createMemento();
		
		RecordedQuery rq_2 = new JSONConverter().fromJSON(qm.getQueryJSON());
		System.out.println(rq_2.toString());
		
		assertEquals(qm.getQueryJava(), rq_2.toString());
		
		qPersistor.storeAs("TestQuery_01");
		
		QueryLoader<DomainQuery> qLoader = da1.createQueryLoader("TestQuery_01");
		QueryMemento qm1 = qLoader.loadMemento();
		DomainQuery q1 = qLoader.load();
		
		return;
	}
	
	@Test
	public void testPersist_02() {
		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 02 ****************************************/
		
		List<Object> primList = new ArrayList<>();
		primList.add("PrimHolder");
		primList.add((int)2);
		primList.add((short)3);
		primList.add((long)4);
		primList.add((float)1.25);
		primList.add((double)7.765E8);
		primList.add(true);
		
		Object[] primArray = new Object[]{
			"PrimHolder",
			(int)2,
			(short)3,
			(long)4,
			(float)1.25,
			(double)7.765E8,
			true
		};
		
		int[] intArray = new int[]{
				2,
				3,
				4
			};
		
		List<String> stringList = new ArrayList<String>();
		stringList.add("String nr. 1");
		stringList.add("String nr. 2");
		
		DomainQuery q = da1.createQuery();
		
		DomainObjectMatch<PrimitiveHolder> primHolder = q.createMatch(PrimitiveHolder.class);
		DomainObjectMatch<List> plMatch = q.TRAVERSE_FROM(primHolder).FORTH("primList").TO(List.class);
		
		q.WHERE(primHolder.atttribute("theString")).EQUALS("PrimHolder");
		q.WHERE(primHolder.atttribute("theInt")).EQUALS((int)2);
		q.WHERE(primHolder.atttribute("theShort")).EQUALS((short)3);
		q.WHERE(primHolder.atttribute("theLong")).EQUALS((long)4);
		q.WHERE(primHolder.atttribute("theFloat")).EQUALS((float)1.25);
		q.WHERE(primHolder.atttribute("theDouble")).EQUALS((double)7.765E8);
		q.WHERE(primHolder.atttribute("theBool")).EQUALS(true);
		DomainObjectMatch<PrimitiveHolder> selectePrimHolder =
				q.SELECT_FROM(primHolder).ELEMENTS(q.WHERE(plMatch).CONTAINS_elements("PrimHolder",
						(int)2,
						(short)3,
						(long)4,
						(float)1.25,
						(double)7.765E8,
						true));
		
		RecordedQuery rq = q.getRecordedQuery();
		System.out.println(rq.toString());
		String query = new JSONConverter().setPrettyFormat(Format.PRETTY_1).toJSON(rq);
		System.out.println(query);
		
		RecordedQuery rq_2 = new JSONConverter().fromJSON(query);
		System.out.println(rq_2.toString());
		
		assertEquals(rq.toString(), rq_2.toString());
		
		return;
	}
	
	@Test
	public void testPersist_01() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		boolean equals;
		String testId;
		String qCypher;
		
		TestDataReader tdr = new TestDataReader("/test/domainquery/Test_SELECT_01.txt");
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
		testId = "PERSIST_01";
		
		q = da1.createQuery();
		Parameter lastName = q.parameter("lastName");
		lastName.setValue("Smith");
		DomainObjectMatch<Person> j_smith = q.createMatch(Person.class);
		DomainObjectMatch<Area> europe = q.createMatch(Area.class);
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS(lastName);
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<Address> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact").TO(Address.class);
		DomainObjectMatch<Area> j_smith_Areas = q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
				.FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		
		DomainObjectMatch<Address> j_smith_FilteredPocs = q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
				q.WHERE(j_smith_Areas).CONTAINS(europe)
		);

		RecordedQuery rq = q.getRecordedQuery();
		String query = new JSONConverter().setPrettyFormat(Format.PRETTY_1).toJSON(rq);
		System.out.println(rq.toString());
		
		RecordedQuery rq_2 = new JSONConverter().fromJSON(query);
		
		assertEquals(rq.toString(), rq_2.toString());
		
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
		storedDomainObjects = null;
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
	}
}
