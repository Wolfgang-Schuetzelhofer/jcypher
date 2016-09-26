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

package test.facade;

import static org.junit.Assert.assertEquals;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.DomainInformation;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DOTypeBuilderFactory;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DOType.DOClassBuilder;
import iot.jcypher.domain.genericmodel.DOType.DOInterfaceBuilder;
import iot.jcypher.domain.genericmodel.internal.DOWalker;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.facade.JSONDBFacade;
import iot.jcypher.facade.JSONDomainFacade;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.DBAccessSettings;
import test.genericmodel.LoadUtil;
import util.TestDataReader;

//@Ignore
public class JSONFacadeTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	@BeforeClass
	public static void before() {
		domainName = "PEOPLE-DOMAIN"; // "QTEST-DOMAIN";
		dbAccess = DBAccessSettings.createDBAccess();
		
		QueriesPrintObserver.addOutputStream(System.out);
		
//		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
//		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOMAIN_INFO, ContentToObserve.CYPHER);
		
		// init db
		List<JcError> errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		LoadUtil.loadPeopleDomain(dbAccess);
		addTypes();
	}
	
	private static void addTypes() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		DOTypeBuilderFactory tpf = gda.getTypeBuilderFactory();
		
		DOType poc = gda.getDomainObjectType("iot.jcypher.samples.domain.people.model.PointOfContact");
		
		DOInterfaceBuilder extendedIfBuilder = tpf.createInterfaceBuilder("mytest.model.additional.ExtendedInterface");
		extendedIfBuilder.addInterface(poc);
		DOType eif = extendedIfBuilder.build();
		
		DOClassBuilder addressBuilder = tpf.createClassBuilder("mytest.model.additional.Address");
		addressBuilder.addInterface(eif);
		addressBuilder.addField("street", String.class.getName());
		addressBuilder.addField("number", int.class.getName());
		DOType addressType = addressBuilder.build();
		
		DomainObject anAddress = new DomainObject(addressType);
		anAddress.setFieldValue("street", "Market Street");
		anAddress.setFieldValue("number", 102);
		
		List<JcError> errors = gda.store(anAddress);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		return;
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
	public void testJSONDomainFacade_02() {
		JSONDBFacade dbFacade = new JSONDBFacade(dbAccess).setPrettyFormat(Format.PRETTY_1);
		
		TestDataReader tdr = new TestDataReader("/test/facade/Test_JSONFACADE_01.txt");
		
		String testId = "FACADE_03";
		String domains = dbFacade.getDomains();
		assertEquals(tdr.getTestData(testId), domains);
		
		return;
	}
	
	@Test
	public void testJSONDomainFacade_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		JSONDomainFacade domainFacade = new JSONDomainFacade(gda).setPrettyFormat(Format.PRETTY_1);
		
		TestDataReader tdr = new TestDataReader("/test/facade/Test_JSONFACADE_01.txt");
		
		/** 01 ****************************************/
		String testId = "FACADE_01";
		String name = domainFacade.getDomainName();
		assertEquals(tdr.getTestData(testId), name);
		
		/** 02 ****************************************/
		testId = "FACADE_02";
		String json = domainFacade.getDomainModel();
		assertEquals(tdr.getTestData(testId), json);
		
		return;
	}
}
