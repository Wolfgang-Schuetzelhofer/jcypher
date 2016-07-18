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
import static org.junit.Assert.assertTrue;

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
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.internal.DOWalker;
import iot.jcypher.domainquery.AbstractDomainQuery;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.InternalAccess;
import iot.jcypher.domainquery.QueryLoader;
import iot.jcypher.domainquery.QueryMemento;
import iot.jcypher.domainquery.QueryPersistor;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.internal.JSONConverter;
import iot.jcypher.domainquery.internal.QueryExecutor;
import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.domainquery.internal.QueryRecorder.QueriesPerThread;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import test.AbstractTestSuite;
import test.DBAccessSettings;
import test.domainquery.Population;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.Person;
import test.domainquery.util.CompareUtil;
import test.genericmodel.DOToString;
import util.TestDataReader;

//@Ignore
public class QueryPersistorTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	private static Population population;
	
	@Test
	public void testPersist_03() {
		IDomainAccess da1;
		DomainQuery q;
		DomainQueryResult result = null;
		
		TestDataReader tdr = new TestDataReader("/test/querypersist/Test_EXEC_01.txt");
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
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
		
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		result = q.execute();
		List<Person> sie = result.resultOf(smithsInEurope);
		//QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());
		assertTrue(sie.size() == 1);
		boolean ok = CompareUtil.equalsObjects(population.getJohn_smith(), sie.get(0));
		assertTrue(ok);
		
		QueryMemento qmNoAugment = qPersistor.createMemento();

		qPersistor.augment(smiths, "smiths")
		.augment(smithsInEurope, "smithsInEurope")
		.augment(europe, "europe")
		.augment(smithAreas, "smithAreas");
		QueryMemento qm = qPersistor.createMemento();
		
		RecordedQuery rq_2 = new JSONConverter().fromJSON(qm.getQueryJSON());
		//System.out.println(rq_2.toString());
		
		assertEquals(qm.getQueryJava(), rq_2.toString());
		
		qPersistor.storeAs("TestQuery_01");
		
		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		QueryLoader<DomainQuery> qLoader = da.createQueryLoader("TestQuery_01");
		//QueryMemento qm1 = qLoader.loadMemento();
		DomainQuery q1 = qLoader.load();
		assertTrue(qpt.isCleared());
		
		String qJava = da.createQueryPersistor(q1).createMemento().getQueryJava();
		assertEquals(qmNoAugment.getQueryJava(), qJava);
		
		List<String> params = q1.getParameterNames();
		assertEquals("[lastName]", params.toString());
		
		Parameter param = q1.parameter("lastName");
		assertEquals("Smith", param.getValue().toString());
		
		List<String> augNames = qLoader.getAugmentedDOMNames();
		assertEquals("[europe, smithAreas, smiths, smithsInEurope]", augNames.toString());
		
		List<String> intNames = qLoader.getInternalDOMNames();
		assertEquals("[obj0, obj12, obj16, obj4]", intNames.toString());
		
		DomainQueryResult result1 = q1.execute();
		DomainObjectMatch<?> dom = qLoader.getDomainObjectMatch("smithsInEurope");
		List<?> sie_1 = result1.resultOf(dom);
		assertTrue(sie_1.size() == 1);
		ok = CompareUtil.equalsObjects(population.getJohn_smith(), sie_1.get(0));
		assertTrue(ok);
		
		/*********************************************/
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		QueryLoader<DomainQuery> qLoader2= da2.createQueryLoader("TestQuery_01");
		DomainQuery q2 = qLoader2.load();
		assertTrue(qpt.isCleared());
		
		DomainQueryResult result2 = q2.execute();
		Throwable th = null;
		try {
			DomainObjectMatch<Area> dom2Err = qLoader2.getDomainObjectMatch("obj16", Area.class);
		} catch (Throwable e) {
			th = e;
		}
		assertTrue(th != null);
		assertTrue(th.getClass().equals(ClassCastException.class));
		DomainObjectMatch<Person> dom2 = qLoader2.getDomainObjectMatch("obj16", Person.class);
		List<Person> sie_2 = result2.resultOf(dom2);
		assertTrue(sie_2.size() == 1);
		ok = CompareUtil.equalsObjects(population.getJohn_smith(), sie_2.get(0));
		assertTrue(ok);
		
		/*************************************************/
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		QueryLoader<GDomainQuery> gQLoader = gda.createQueryLoader("TestQuery_01");
		GDomainQuery gq = gQLoader.load();
		
		DomainQueryResult gResult = gq.execute();
		DomainObjectMatch<DomainObject> sm_ie_Match = gQLoader.getDomainObjectMatch("smithsInEurope", DomainObject.class);
		
		List<DomainObject> sm_ie = gResult.resultOf(sm_ie_Match);
		assertTrue(sm_ie.size() == 1);
		
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(sm_ie.get(0), doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println(str);
		
		assertEquals(tdr.getTestData("EXEC_01"), str);
		
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
		
		QueryRecorder.queryCompleted(q);
		
		RecordedQuery rq = q.getRecordedQuery();
		//System.out.println(rq.toString());
		String query = new JSONConverter().setPrettyFormat(Format.PRETTY_1).toJSON(rq);
		//System.out.println(query);
		
		RecordedQuery rq_2 = new JSONConverter().fromJSON(query);
		//System.out.println(rq_2.toString());
		
		assertEquals(rq.toString(), rq_2.toString());
		
		return;
	}
	
	@Test
	public void testPersist_01() {
		IDomainAccess da1;
		DomainQuery q;

		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		
		/** 01 ****************************************/
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
		QueryRecorder.queryCompleted(q);

		RecordedQuery rq = q.getRecordedQuery();
		String query = new JSONConverter().setPrettyFormat(Format.PRETTY_1).toJSON(rq);
		//System.out.println(rq.toString());
		
		RecordedQuery rq_2 = new JSONConverter().fromJSON(query);
		
		assertEquals(rq.toString(), rq_2.toString());
		
		return;
	}
	
	@BeforeClass
	public static void before() {
		domainName = "QTEST-DOMAIN";
		dbAccess = DBAccessSettings.createDBAccess();
		
		// init db
		population = new Population();
		
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
