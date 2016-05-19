/************************************************************************
 * Copyright (c) 2015-2016 IoT-Solutions e.U.
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
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.internal.DOWalker;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.QueryLoader;
import iot.jcypher.domainquery.QueryMemento;
import iot.jcypher.domainquery.QueryPersistor;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.internal.JSONConverter;
import iot.jcypher.domainquery.internal.QueryRecorder;
import iot.jcypher.domainquery.internal.QueryRecorder.QueriesPerThread;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;
import test.AbstractTestSuite;
import test.genericmodel.DOToString;
import test.genericmodel.LoadUtil;
import util.TestDataReader;

//@Ignore
public class GenericQueryPersistorTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	
	@Test
	public void testPersist_01() {
		
		TestDataReader tdr = new TestDataReader("/test/querypersist/Test_EXEC_01.txt");
		
		IGenericDomainAccess gda1 = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		
		GDomainQuery q = gda1.createQuery();
		QueryPersistor qPersistor = gda1.createQueryPersistor(q);
		
		Parameter lastName = q.parameter("lastName");
		lastName.setValue("Smith");
		DomainObjectMatch<DomainObject> smiths = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		q.WHERE(smiths.atttribute("lastName")).EQUALS(lastName);
		
		DomainObjectMatch<DomainObject> europe = q.createMatch("iot.jcypher.samples.domain.people.model.Area");
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");
		
		DomainObjectMatch<DomainObject> smithAreas = q.TRAVERSE_FROM(smiths).FORTH("pointsOfContact").FORTH("area").FORTH("partOf").DISTANCE(0, -1)
			.TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		
		DomainObjectMatch<DomainObject> smithsInEurope = q.SELECT_FROM(smiths).ELEMENTS(
				q.WHERE(smithAreas).CONTAINS(europe)
		);
		
		QueriesPerThread qpt = QueryRecorder.getCreateQueriesPerThread();
		QueryRecorder.queryCompleted(q);
		assertTrue(qpt.isCleared());

		qPersistor.augment(smiths, "smiths")
		.augment(smithsInEurope, "smithsInEurope")
		.augment(europe, "europe")
		.augment(smithAreas, "smithAreas");
		QueryMemento qm = qPersistor.createMemento();
		
		RecordedQuery rq_2 = new JSONConverter().fromJSON(qm.getQueryJSON());
		//System.out.println(rq_2.toString());
		
		assertEquals(qm.getQueryJava(), rq_2.toString());
		
		qPersistor.storeAs("TestQuery_02");
		
		/*************************************************/
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		QueryLoader<GDomainQuery> gQLoader = gda.createQueryLoader("TestQuery_02");
		GDomainQuery gq = gQLoader.load();
		
		DomainQueryResult gResult = gq.execute();
		DomainObjectMatch<DomainObject> sm_areas_Match = gQLoader.getDomainObjectMatch("smithAreas", DomainObject.class);
		DomainObjectMatch<DomainObject> sm_ie_Match = gQLoader.getDomainObjectMatch("smithsInEurope", DomainObject.class);
		
		List<DomainObject> sm_areas = gResult.resultOf(sm_areas_Match);
		List<DomainObject> sm_ie = gResult.resultOf(sm_ie_Match);
		assertTrue(sm_ie.size() == 1);
		
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(sm_ie.get(0), doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println(str);
		
		assertEquals(tdr.getTestData("EXEC_02"), str);
		
		return;
	}
	
	@BeforeClass
	public static void before() {
		domainName = "PEOPLE-DOMAIN"; // "QTEST-DOMAIN";
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
//		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "jcypher");
		
//		QueriesPrintObserver.addOutputStream(System.out);
//		
//		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
//		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
//		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOMAIN_INFO, ContentToObserve.CYPHER);
		
		// init db
		List<JcError> errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		LoadUtil.loadPeopleDomain(dbAccess);
		
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		QueriesPrintObserver.removeAllEnabledQueries();
	}
}
