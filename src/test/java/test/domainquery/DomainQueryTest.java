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

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainquery.model.Person;

public class DomainQueryTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	
	@BeforeClass
	public static void before() {
		domainName = "QTEST-DOMAIN";
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
	}
	
	@Test
	public void testDomainQuery_01() {
		List<JcError> errors;
		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		IDomainAccess da1;
		
//		Population domainPopulator = new Population();
//		
//		List<Object> domainObjects = domainPopulator.createPopulation();
//		
//		errors = dbAccess.clearDatabase();
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
//		
//		errors = da.store(domainObjects);
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
		
		da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		DomainQuery q = da1.createQuery();
		DomainObjectMatch<Person> smith = q.createMatch(Person.class);
		q.WHERE(smith.stringAtttribute("firstName")).EQUALS("John");
		q.WHERE(smith.stringAtttribute("firstName")).NOT().EQUALS("Caroline");
		q.WHERE(smith.stringAtttribute("lastName")).EQUALS(q.parameter("lastName"));
		q.BR_OPEN();
			q.WHERE(smith.stringAtttribute("firstName")).EQUALS("Angelina");
			q.OR();
			q.WHERE(smith.stringAtttribute("firstName")).EQUALS("Jeremy");
		q.BR_CLOSE();
		
		q.parameter("lastName").setValue("Smith");
		
		DomainQueryResult result = q.execute();
		
		return;
	}
}
