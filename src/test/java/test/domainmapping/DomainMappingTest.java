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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import iot.jcypher.JcQuery;
import iot.jcypher.JcQueryResult;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccess;
import iot.jcypher.domain.SyncInfo;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrPropertyContainer;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.result.JcError;
import iot.jcypher.result.JcResultException;
import iot.jcypher.result.Util;

import java.sql.Clob;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainmapping.ambiguous.Broker;
import test.domainmapping.ambiguous.IPerson;
import test.domainmapping.ambiguous.JPerson;
import test.domainmapping.ambiguous.NPerson;

public class DomainMappingTest extends AbstractTestSuite{

	private static IDBAccess dbAccess;
	private static String domainName;
	
	@BeforeClass
	public static void before() {
		domainName = "PEOPLE-DOMAIN";
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
	
	//@Test
	public void testClearDomain() {
		DomainAccess da = new DomainAccess(dbAccess, domainName);
		List<JcError> errors;
		
		Person john = new Person();
		Address address = new Address();
		Contact phone = new Contact();
		Contact email = new Contact();
		Person james = new Person();
		
		buildInitialDomainObjects_1(john, james, address, phone, email, null, null);
		
		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(john);
		domainObjects.add(james);
		
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		boolean check = dbAccess.isDatabaseEmpty();
		assertFalse("Test Domain not empty", check);
		
		errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		check = dbAccess.isDatabaseEmpty();
		assertTrue("Test Domain is empty", check);
		return;
	}
	
	@Test
	public void testAmbiguous() {
		List<JcError> errors;
		DomainAccess da = new DomainAccess(dbAccess, domainName);
		DomainAccess da1;
		Broker broker1 = new Broker();
		Broker broker2 = new Broker();
		Broker broker21;
		Broker broker22;
		
		buildAmbiguousTestObjects(broker1, broker2);
		
		errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(broker1);
		domainObjects.add(broker2);
		
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		IPerson person1 = broker1.getWorksWith();
		IPerson person2 = broker2.getWorksWith();
		SyncInfo syncInfo_1 = da.getSyncInfo(broker1);
		SyncInfo syncInfo_2 = da.getSyncInfo(broker2);
		SyncInfo syncInfo_3 = da.getSyncInfo(person1);
		SyncInfo syncInfo_4 = da.getSyncInfo(person2);
		
		IPerson person21;
		IPerson person22;
		
		da1 = new DomainAccess(dbAccess, domainName);
		person21 = da1.loadById(IPerson.class, syncInfo_3.getId());
		person22 = da1.loadById(IPerson.class, syncInfo_4.getId());
		broker21 = da1.loadById(Broker.class, syncInfo_1.getId());
		return;
	}
	
	@SuppressWarnings({ "unused", "rawtypes" })
	//@Test
	public void testUpdateComplex_EmptyList2NotEmptyList() {
		
		List<JcError> errors;
		DomainAccess da = new DomainAccess(dbAccess, domainName);
		
		Person john = new Person();
		
		buildInitialDomainObjects_2(john);
		List addresses = john.getAddresses();
		john.setAddresses(new ArrayList());
		
		errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		// Store empty array without generics
		errors = da.store(john);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		SyncInfo syncInfo = da.getSyncInfo(john);
		
		// Store non-empty array without generics
		john.setAddresses(addresses);
		errors = da.store(john);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		// test if property for empty collection has been removed
//		da = new DomainAccess(dbAccess, domainName);
//		errors = da.store(john);
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
		
		da = new DomainAccess(dbAccess, domainName);
		Person john_1;
		john_1 = da.loadById(Person.class, syncInfo.getId());
		
		return;
	}
	
	@SuppressWarnings("unchecked")
	//@Test
	public void testUpdateSimple_EmptyList2NotEmptyList() {
		
		List<JcError> errors;
		DomainAccess da = new DomainAccess(dbAccess, domainName);
		
		Person john = new Person();
		Address address = new Address();
		Contact phone = new Contact();
		Contact email = new Contact();
		Person james = new Person();
		Company skynet = new Company();
		Company globCom = new Company();
		
		buildInitialDomainObjects_1(john, james, address, phone, email, skynet, globCom);
		
		errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		// Store empty array without generics
		errors = da.store(globCom);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		SyncInfo syncInfo = da.getSyncInfo(globCom);
		
		globCom.getAreaCodes().add(2);
		globCom.getAreaCodes().add(3);
		// Store non-empty array without generics
		errors = da.store(globCom);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		da = new DomainAccess(dbAccess, domainName);
		Company globCom_1;
		globCom_1 = da.loadById(Company.class, syncInfo.getId());
		
		boolean isEqual = equalsCompany(globCom, globCom_1);
		assertTrue("Test for equality of domain objects", isEqual);
		
		// Store non-empty array with generics
		da = new DomainAccess(dbAccess, domainName);
		james.setLuckyNumbers(new ArrayList<Integer>());
		errors = da.store(james);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		syncInfo = da.getSyncInfo(james);
		
		james.getLuckyNumbers().add(24);
		james.getLuckyNumbers().add(48);
		errors = da.store(james);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		da = new DomainAccess(dbAccess, domainName);
		Person james_1;
		james_1 = da.loadById(Person.class, syncInfo.getId());
		
		isEqual = equalsPerson(james, james_1, null);
		assertTrue("Test for equality of domain objects", isEqual);
		
		return;
	}
	
	//@Test
	public void testLoadEmptyLists() {
		
		List<JcError> errors;
		DomainAccess da = new DomainAccess(dbAccess, domainName);
		
		Person john = new Person();
		Address address = new Address();
		Contact phone = new Contact();
		Contact email = new Contact();
		Person james = new Person();
		Company skynet = new Company();
		Company globCom = new Company();
		
		buildInitialDomainObjects_1(john, james, address, phone, email, skynet, globCom);
		
		errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		// Test empty array without generics
		errors = da.store(globCom);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		SyncInfo syncInfo = da.getSyncInfo(globCom);
		
		da = new DomainAccess(dbAccess, domainName);
		Company globCom_1;
		globCom_1 = da.loadById(Company.class, syncInfo.getId());
		
		boolean isEqual = equalsCompany(globCom, globCom_1);
		assertTrue("Test for equality of domain objects", isEqual);
		
		// Test empty array with generics
		james.setLuckyNumbers(new ArrayList<Integer>());
		errors = da.store(james);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		syncInfo = da.getSyncInfo(james);
		
		da = new DomainAccess(dbAccess, domainName);
		Person james_1;
		james_1 = da.loadById(Person.class, syncInfo.getId());
		
		isEqual = equalsPerson(james, james_1, null);
		assertTrue("Test for equality of domain objects", isEqual);
		
		return;
	}
	
	//@Test
	public void testStoreDomainObjects() {
		Person john_1, james_1;
		Address addr_1;
		
		List<JcError> errors;
		DomainAccess da = new DomainAccess(dbAccess, domainName);
		DomainAccess da1;
		
		Person john = new Person();
		Address address = new Address();
		Contact phone = new Contact();
		Contact email = new Contact();
		Person james = new Person();
		Company skynet = new Company();
		Company globCom = new Company();
		
		buildInitialDomainObjects_1(john, james, address, phone, email, skynet, globCom);
		
		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(john);
		domainObjects.add(james);
		
		errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		// check for an empty graph
		boolean check = dbAccess.isDatabaseEmpty();
		assertTrue("Test for empty graph", check);
		
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		SyncInfo syncInfo = da.getSyncInfo(john);
		john_1 = da.loadById(Person.class, syncInfo.getId());
		boolean isIdentical = john == john_1;
		assertTrue("Test for identity of domain objects", isIdentical);
		
		// add a new class to the domain to check another
		// initialize DomainInfo scenario
		da1 = new DomainAccess(dbAccess, domainName);
		errors = da1.store(skynet);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		check = checkDomainInfoNodeAgainst(
				"[Address=test.domainmapping.Address, Company=test.domainmapping.Company," +
					" Contact=test.domainmapping.Contact, Person=test.domainmapping.Person]");
		assertTrue("Test Domain Info node", check);
		
		da1 = new DomainAccess(dbAccess, domainName);
		try {
			john_1 = da1.loadById(Person.class, syncInfo.getId());
		} catch (Exception e) {
			if (e instanceof JcResultException) {
				errors = ((JcResultException)e).getErrors();
				printErrors(errors);
				return;
			}
			throw e;
		}
		boolean isEqual = equalsPerson(john, john_1, null);
		assertTrue("Test for equality of domain objects", isEqual);
		
		List<NodesToCheck> ntc = new ArrayList<NodesToCheck>();
		ntc.add(new NodesToCheck("Person", 2));
		ntc.add(new NodesToCheck("Address", 2));
		ntc.add(new NodesToCheck("Contact", 1));
		ntc.add(new NodesToCheck("Company", 1));
		List<RelationsToCheck> rtc = new ArrayList<RelationsToCheck>();
		rtc.add(new RelationsToCheck("address", 2));
		rtc.add(new RelationsToCheck("contact", 1));
		
		check = checkForNodesAndRelations(ntc, rtc);
		assertTrue("Test for nodes and relations in graph", check);
		
		james.setLuckyNumbers(new ArrayList<Integer>());
		errors = da.store(james);
		if (errors.size() > 0) {
			printErrors(errors);
		}
		
		// test if update query is empty (or no update query at all)
		errors = da.store(james);
		if (errors.size() > 0) {
			printErrors(errors);
		}
		
		errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		da1 = new DomainAccess(dbAccess, domainName);
		errors = da1.store(globCom);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
//		
//		keanu.setFirstName("Keanu_1");
		john.setContact(null);
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
		}
		
		james.setContact(email);
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
		}
		
		james.setMainAddress(address);
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
		}
		
		john.setBestFriend(james);
		james.setBestFriend(john);
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
		}
		
		john.setBestFriend(null);
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
		}
		
		da1 = new DomainAccess(dbAccess, domainName); 
		try {
			john_1 = da1.loadById(Person.class, syncInfo.getId());
		} catch (Exception e) {
			if (e instanceof JcResultException) {
				errors = ((JcResultException)e).getErrors();
				printErrors(errors);
				return;
			}
			throw e;
		}
		
		james.setBestFriend(null);
		errors = da.store(domainObjects);
		if (errors.size() > 0) {
			printErrors(errors);
		}

		try {
			//keanu_1 = dc.loadById(Person.class, 0);
//			List<Person> persons = dc.loadByIds(Person.class, 0, 3);
			addr_1 = da.loadById(Address.class, 1);
			addr_1 = da.loadById(Address.class, 1);
			
			john_1 = da1.loadById(Person.class, 0);
			john_1 = da.loadById(Person.class, 0);
			james_1 = da.loadById(Person.class, 3);
			john_1.setFirstName("Keanu Kevin");
			da.store(john_1);
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
	
	private boolean checkDomainInfoNodeAgainst(String expected) {
		JcNode info = new JcNode("info");
		JcQuery query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(info).label("DomainInfo"),
				WHERE.valueOf(info.property("name"))
					.EQUALS(domainName),
				RETURN.value(info)
		});
		JcQueryResult result = dbAccess.execute(query);
		List<JcError> errors = Util.collectErrors(result);
		if (!errors.isEmpty()) {
			throw new JcResultException(errors);
		}
		GrNode rInfo = result.resultOf(info).get(0);
		GrProperty prop = rInfo.getProperty("label2ClassMap");
		Object val = prop.getValue();
		String returned = val.toString();
		return expected.equals(returned);
	}

	private void buildAmbiguousTestObjects(Broker broker1, Broker broker2) {
		Address address = new Address();
		address.setCity("Munich");
		address.setStreet("Main Street");
		address.setNumber(9);
		
		NPerson nPerson = new NPerson();
		nPerson.setNamePart1("Sam");
		nPerson.setNamePart2("Smith");
		nPerson.setSocialSecurityNumber("123456");
		nPerson.setHomeAddress(address);
		
		address = new Address();
		address.setCity("San Francisco");
		address.setStreet("Kearny Street");
		address.setNumber(28);
		
		JPerson jPerson = new JPerson();
		jPerson.setNamePart1("Global Company");
		jPerson.setNamePart2("incorporated");
		jPerson.setCompanyNumber(42);
		jPerson.setCompanyAddress(address);
		
		broker1.setWorksWith(nPerson);
		broker2.setWorksWith(jPerson);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void buildInitialDomainObjects_1(Person john, Person james,
			Address address, Contact phone, Contact email, Company skynet,
			Company globCom) {
		john.setFirstName("John");
		john.setLastName("Reeves");
		Calendar cal = Calendar.getInstance();
		cal.set(1964, 8, 2, 0, 0, 0);
		clearMillis(cal);
		john.setBirthDate(cal.getTime());
		
		List<Integer> luckyNumbers = new ArrayList<Integer>();
		luckyNumbers.add(3);
		luckyNumbers.add(9);
		luckyNumbers.add(12);
		luckyNumbers.add(15);
		luckyNumbers.add(23);
		john.setLuckyNumbers(luckyNumbers);
		
		address.setCity("Vienna");
		address.setStreet("Main Street");
		address.setNumber(9);
		john.setMainAddress(address);
		
		phone.setType(ContactType.TELEPHONE);
		phone.setNummer("12345");
		john.setContact(phone);
		
		email.setType(ContactType.EMAIL);
		email.setNummer("dj@nowhere.org");
		
		james.setFirstName("James");
		james.setLastName("Fishburne");
		cal = Calendar.getInstance();
		cal.set(1961, 6, 30, 0, 0, 0);
		clearMillis(cal);
		james.setBirthDate(cal.getTime());
		
		if (skynet != null) {
			skynet.setName("Sky-Net");
			Address addr = new Address();
			addr.setCity("Global City");
			addr.setStreet("Graphstreet");
			addr.setNumber(42);
			skynet.setAddress(addr);
			
			List areaCodes = new ArrayList();
			areaCodes.add(42);
			areaCodes.add(43);
			areaCodes.add(44);
			skynet.setAreaCodes(areaCodes);
		}
		
		if (globCom != null) {
			globCom.setName("Glob-Com");
			Address addr = new Address();
			addr.setCity("Global City");
			addr.setStreet("Mainstreet");
			addr.setNumber(1);
			globCom.setAddress(addr);
			
			globCom.setAreaCodes(new ArrayList());
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildInitialDomainObjects_2(Person john) {
		john.setFirstName("John");
		john.setLastName("Reeves");
		Calendar cal = Calendar.getInstance();
		cal.set(1964, 8, 2, 0, 0, 0);
		clearMillis(cal);
		john.setBirthDate(cal.getTime());
		john.setAddresses(new ArrayList());
		
		Address address = new Address();
		address.setCity("Munich");
		address.setStreet("Main Street");
		address.setNumber(9);
		john.getAddresses().add(address);
		
		address = new Address();
		address.setCity("San Francisco");
		address.setStreet("Kearny Street");
		address.setNumber(28);
		john.getAddresses().add(address);
		
		address = new Address();
		address.setCity("Paris");
		address.setStreet("boulevard de clichy");
		address.setNumber(108);
		john.getAddresses().add(address);
	}
	
	private boolean equalsPerson(Person person1, Person person2,
			List<AlreadyCompared> alreadyCompareds) {
		
		if (person1 == null || person2 == null)
			return false;
		
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = alreadyCompared(person1, person2, acs);
		if (ac != null) // avoid infinite loops
			return ac.result;
		
		ac = new AlreadyCompared(person1, person2);
		acs.add(ac);

		if (person1.getMainAddress() == null) {
			if (person2.getMainAddress() != null)
				return ac.setResult(false);
		} else if (!equalsAddress(person1.getMainAddress(), person2.getMainAddress()))
			return ac.setResult(false);
		if (person1.getBirthDate() == null) {
			if (person2.getBirthDate() != null)
				return ac.setResult(false);
		} else if (!person1.getBirthDate().equals(person2.getBirthDate()))
			return ac.setResult(false);
		if (person1.getContact() == null) {
			if (person2.getContact() != null)
				return ac.setResult(false);
		} else if (!equalsContact(person1.getContact(), person2.getContact()))
			return ac.setResult(false);
		if (person1.getFirstName() == null) {
			if (person2.getFirstName() != null)
				return ac.setResult(false);
		} else if (!person1.getFirstName().equals(person2.getFirstName()))
			return ac.setResult(false);
		if (person1.getLastName() == null) {
			if (person2.getLastName() != null)
				return ac.setResult(false);
		} else if (!person1.getLastName().equals(person2.getLastName()))
			return ac.setResult(false);
		if (person1.getLuckyNumbers() == null) {
			if (person2.getLuckyNumbers() != null)
				return ac.setResult(false);
		} else if (!person1.getLuckyNumbers().equals(person2.getLuckyNumbers()))
			return ac.setResult(false);
		
		ac.setResult(true); // equal so far
		if (person1.getBestFriend() == null) {
			if (person2.getBestFriend() != null)
				return ac.setResult(false);
		} else if (!equalsPerson(person1.getBestFriend(), person2.getBestFriend(), acs))
			return ac.setResult(false);
		return true;
	}
	
	private boolean equalsAddress(Address address1, Address address2) {
		if (address1 == null || address2 == null)
			return false;
		
		if (address1.getCity() == null) {
			if (address2.getCity() != null)
				return false;
		} else if (!address1.getCity().equals(address2.getCity()))
			return false;
		if (address1.getNumber() != address2.getNumber())
			return false;
		if (address1.getStreet() == null) {
			if (address2.getStreet() != null)
				return false;
		} else if (!address1.getStreet().equals(address2.getStreet()))
			return false;
		return true;
	}
	
	private boolean equalsContact(Contact contact1, Contact contact2) {
		if (contact1 == null || contact2 == null)
			return false;
		
		if (contact1.getNummer() == null) {
			if (contact2.getNummer() != null)
				return false;
		} else if (!contact1.getNummer().equals(contact2.getNummer()))
			return false;
		if (contact1.getType() != contact2.getType())
			return false;
		return true;
	}
	
	private boolean equalsCompany(Company company_1, Company company_2) {
		if (company_1 == null || company_2 == null)
			return false;
		
		if (company_1.getName() == null) {
			if (company_2.getName() != null)
				return false;
		} else if (!company_1.getName().equals(company_2.getName()))
			return false;
		if (company_1.getAddress() == null) {
			if (company_2.getAddress() != null)
				return false;
		} else if (!equalsAddress(company_1.getAddress(), company_2.getAddress()))
			return false;
		if (company_1.getAreaCodes() == null) {
			if (company_2.getAreaCodes() != null)
				return false;
		} else if (!company_1.getAreaCodes().equals(company_2.getAreaCodes()))
			return false;
		return true;
	}
	
	private AlreadyCompared alreadyCompared(Object obj1, Object obj2,
			List<AlreadyCompared> alreadyCompareds) {
		for (AlreadyCompared ac : alreadyCompareds) {
			if ((ac.object1 == obj1 && ac.object2 == obj2) ||
					(ac.object1 == obj2 && ac.object2 == obj2))
				return ac;
		}
		return null;
	}
	
	/**
	 * @param nodesToCheck
	 * @param relationsToCheck
	 * @return true for success
	 */
	private boolean checkForNodesAndRelations(List<NodesToCheck> nodesToCheck,
			List<RelationsToCheck> relationsToCheck) {
		boolean ret = true;
		boolean checkForNodes = nodesToCheck != null && nodesToCheck.size() > 0;
		boolean checkForRelations = relationsToCheck != null && relationsToCheck.size() > 0;
		if (checkForNodes || checkForRelations) {
			List<IClause> clauses = new ArrayList<IClause>();
			if (checkForNodes) {
				int idx = -1;
				for (NodesToCheck ntc : nodesToCheck) {
					idx++;
					JcNode n = new JcNode("n_".concat(String.valueOf(idx)));
					if (idx > 0)
						clauses.add(SEPARATE.nextClause());
					if (ntc.label != null)
						clauses.add(MATCH.node(n).label(ntc.label));
					else
						clauses.add(MATCH.node(n));
				}
			}
			
			if (checkForRelations) {
				int idx = -1;
				for (RelationsToCheck rtc : relationsToCheck) {
					idx++;
					JcRelation r = new JcRelation("r_".concat(String.valueOf(idx)));
					if (clauses.size() > 0)
						clauses.add(SEPARATE.nextClause());
					if (rtc.type != null)
						clauses.add(MATCH.node().relation(r).type(rtc.type).node());
					else
						clauses.add(MATCH.node().relation(r).node());
				}
			}
			clauses.add(RETURN.ALL());
			JcQuery query = new JcQuery();
			query.setClauses(clauses.toArray(new IClause[clauses.size()]));
//			Util.printQuery(query, "CHECK", Format.PRETTY_1);
			JcQueryResult result = dbAccess.execute(query);
			if (result.hasErrors()) {
				List<JcError> errors = Util.collectErrors(result);
				throw new JcResultException(errors);
			}
//			Util.printResult(result, "CHECK", Format.PRETTY_1);
			
			// perform check
			if (checkForNodes) {
				int idx = -1;
				for (NodesToCheck ntc : nodesToCheck) {
					idx++;
					JcNode n = new JcNode("n_".concat(String.valueOf(idx)));
					List<GrNode> nodes = result.resultOf(n);
					nodes = removeMultiple(nodes);
					ret = nodes.size() == ntc.count;
					if (!ret)
						break;
				}
			}
			
			if (checkForRelations && ret) {
				int idx = -1;
				for (RelationsToCheck rtc : relationsToCheck) {
					idx++;
					JcRelation r = new JcRelation("r_".concat(String.valueOf(idx)));
					List<GrRelation> relations = result.resultOf(r);
					relations = removeMultiple(relations);
					ret = relations.size() == rtc.count;
					if (!ret)
						break;
				}
			}
		} else { // check for an empty graph
			ret = dbAccess.isDatabaseEmpty();
		}
		return ret;
	}
	
	private <T extends GrPropertyContainer> List<T> removeMultiple(List<T> source) {
		List<T> ret = new ArrayList<T>();
		for (T elem : source) {
			if (!containsElement(ret, elem))
				ret.add(elem);
		}
		return ret;
	}
	
	private <T extends GrPropertyContainer> boolean containsElement(List<T> elems, T elem) {
		for (T pc : elems) {
			if (pc.getId() == elem.getId())
				return true;
		}
		return false;
	}
	
	private void clearMillis(Calendar cal) {
		long millis = cal.getTimeInMillis();
		long nMillis = millis / 1000;
		nMillis = nMillis * 1000;
		nMillis = nMillis - 1000;
		cal.setTimeInMillis(nMillis);
	}
	
	/************************************/
	private static class AlreadyCompared {
		private Object object1;
		private Object object2;
		private boolean result;
		
		private AlreadyCompared(Object object1, Object object2) {
			super();
			this.object1 = object1;
			this.object2 = object2;
		}
		
		private boolean setResult(boolean b) {
			this.result = b;
			return b;
		}
	}
	
	/************************************/
	private static class NodesToCheck {
		private String label;
		private int count;
		
		private NodesToCheck(String label, int count) {
			super();
			this.label = label;
			this.count = count;
		}
	}
	
	/************************************/
	private static class RelationsToCheck {
		private String type;
		private int count;
		
		private RelationsToCheck(String type, int count) {
			super();
			this.type = type;
			this.count = count;
		}
	}
}
