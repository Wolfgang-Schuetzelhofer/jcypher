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

package test.domainmapping;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccess;
import iot.jcypher.result.JcError;
import iot.jcypher.result.JcResultException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;

public class DomainMappingTest extends AbstractTestSuite{

	private static IDBAccess dbAccess;
	
	@BeforeClass
	public static void before() {
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
	public void testStoreDomainObject() {
		List<JcError> errors;
		DomainAccess da = new DomainAccess(dbAccess);
		
		Person keanu = new Person();
		keanu.setFirstName("Keanu");
		keanu.setLastName("Reeves");
		Calendar cal = Calendar.getInstance();
		cal.set(1964, 8, 2, 0, 0, 0);
		clearMillis(cal);
		keanu.setBirthDate(cal.getTime());
		
		Address addr = new Address();
		addr.setCity("Vienna");
		addr.setStreet("Main Street");
		addr.setNumber(9);
		keanu.setAddress(addr);
		
		Contact contact = new Contact();
		contact.setType(ContactType.TELEPHONE);
		contact.setNummer("12345");
		keanu.setContact(contact);
		
		Person laurence = new Person();
		laurence.setFirstName("Laurence");
		laurence.setLastName("Fishburne");
		cal = Calendar.getInstance();
		cal.set(1961, 6, 30, 0, 0, 0);
		clearMillis(cal);
		laurence.setBirthDate(cal.getTime());
		
		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(keanu);
		domainObjects.add(laurence);
		
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
		}

		Person keanu_1, lawrence_1;
		Address addr_1;
		try {
			//keanu_1 = dc.loadById(Person.class, 0);
//			List<Person> persons = dc.loadByIds(Person.class, 0, 3);
			addr_1 = da.loadById(Address.class, 1);
			addr_1 = da.loadById(Address.class, 1);
			
			DomainAccess da1 = new DomainAccess(dbAccess);
			keanu_1 = da1.loadById(Person.class, 0);
			keanu_1 = da.loadById(Person.class, 0);
			lawrence_1 = da.loadById(Person.class, 3);
			keanu_1.setFirstName("Keanu Kevin");
			da.store(keanu_1);
		} catch (Exception e) {
			if (e instanceof JcResultException) {
				errors = ((JcResultException)e).getErrors();
				printErrors(errors);
				return;
			}
			throw e;
		}
		return;
	}
	
	private void clearMillis(Calendar cal) {
		long millis = cal.getTimeInMillis();
		long nMillis = millis / 1000;
		nMillis = nMillis * 1000;
		nMillis = nMillis - 1000;
		cal.setTimeInMillis(nMillis);
	}
}
