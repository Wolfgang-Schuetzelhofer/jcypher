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

package test.genericmodel;

import static org.junit.Assert.assertEquals;

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
import iot.jcypher.domain.DomainInformation;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.internal.DOWalker;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;
import test.AbstractTestSuite;
import test.DBAccessSettings;
import util.TestDataReader;

//@Ignore
public class GenericModelTest extends AbstractTestSuite {

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
	
	@Test
	public void testGenericModel_01() {
		String testId;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENMODEL_01.txt");
		
		/** 01 ****************************************/
		testId = "GENMODEL_01";
		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		((IIntDomainAccess)da).getInternalDomainAccess().loadDomainInfoIfNeeded();
		String domModel = ((IIntDomainAccess)da).getInternalDomainAccess().domainModelAsString();
		
		assertEquals(tdr.getTestData(testId), domModel);
		return;
	}
	
	@Test
	public void testLoadGenericModel_01() {
		IGenericDomainAccess gda;
		String testId;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENMODEL_01.txt");
		
		DomainInformation di = DomainInformation.forDomain(dbAccess, domainName);
		gda = di.getGenericDomainAccess();
		List<DomainObject> objects = gda.loadByType("iot.jcypher.samples.domain.people.model.Person", -1, 0, -1);
		objects = Util.sortPersons(objects);
		
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(objects, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		//System.out.println("\nObjectGraph:" + str);
		
		testId = "GENMODEL_02";
		assertEquals(tdr.getTestData(testId), str);
		
		return;
	}
	
	@Test
	public void testLoadGenericModel_02() {
		IGenericDomainAccess gda;
		String testId;
		
		TestDataReader tdr = new TestDataReader("/test/genericmodel/Test_GENMODEL_01.txt");
		
		gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		List<DomainObject> objects = gda.loadByType("iot.jcypher.samples.domain.people.model.Person", -1, 0, -1);
		long carolineId = -1;
		for (DomainObject obj : objects) {
			if (obj.getFieldValue("lastName").toString().equals("Smith") &&
					obj.getFieldValue("firstName").toString().equals("Caroline")) {
				carolineId = gda.getSyncInfo(obj).getId();
				break;
			}
		}
		// create a new domain access
		gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		DomainObject object = gda.loadById("iot.jcypher.samples.domain.people.model.Person", -1, carolineId);
		
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(object, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		
		testId = "GENMODEL_03";
		assertEquals(tdr.getTestData(testId), str);
		
		return;
	}
}
