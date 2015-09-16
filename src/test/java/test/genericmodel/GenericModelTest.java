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

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.DomainInformation;
import iot.jcypher.domain.DomainInformation.DomainObjectType;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainquery.Population;
import test.domainquery.model.DateHolder;
import test.domainquery.model.NumberHolder;
import test.domainquery.model.Person;
import test.domainquery.model.SubNumberHolder;

public class GenericModelTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	
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
		QueriesPrintObserver.addToEnabledQueries("DOMAIN INFO", ContentToObserve.CYPHER);
		
		// init db
//		Population population = new Population();
//		
//		storedDomainObjects = population.createPopulation();
//		
//		QueriesPrintObserver.addOutputStream(System.out);
//		
//		QueriesPrintObserver.addToEnabledQueries("COUNT QUERY", ContentToObserve.CYPHER);
//		QueriesPrintObserver.addToEnabledQueries("DOM QUERY", ContentToObserve.CYPHER);
//		
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
	
	@Test
	public void testLoadGenericModel_01() {
		IGenericDomainAccess gda;
		List<JcError> errors;
		DomainInformation di = DomainInformation.forDomain(dbAccess, domainName);
		List<DomainObjectType> types = di.getDomainObjectTypes();
		DomainObjectType type = null;
		for (DomainObjectType t : types) {
			if (t.getTypeName().equals("iot.jcypher.samples.domain.people.model.Person")) {
				type = t;
				break;
			}
		}
		gda = di.getGenericDomainAccess();
		List<DomainObject> objects = gda.loadByType(type.getTypeName(), -1, 0, -1);
		
		
		return;
	}
}
