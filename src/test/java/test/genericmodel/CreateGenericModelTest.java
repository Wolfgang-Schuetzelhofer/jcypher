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

import static org.junit.Assert.*;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DOType.DOClassBuilder;
import iot.jcypher.domain.genericmodel.DOType.DOEnumBuilder;
import iot.jcypher.domain.genericmodel.DOType.DOInterfaceBuilder;
import iot.jcypher.domain.genericmodel.internal.DOWalker;
import iot.jcypher.domain.genericmodel.DOTypeBuilderFactory;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.InternalAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;

//@Ignore
public class CreateGenericModelTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	
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
		
		QueriesPrintObserver.addOutputStream(System.out);
		
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOMAIN_INFO, ContentToObserve.CYPHER);
	}
	
	@Test
	public void testCreateGenericModel_01() {
		IGenericDomainAccess gda;
		List<JcError> errors;
		
		errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		DOTypeBuilderFactory tpf = gda.getTypeBuilderFactory();
		
		DOEnumBuilder subjectTypesBuilder = tpf.createEnumBuilder("mytest.model.SubjectTypes");
		subjectTypesBuilder.addEnumValue("NAT_PERSON");
		subjectTypesBuilder.addEnumValue("JUR_PERSON");
		DOType subjectTypes = subjectTypesBuilder.build();
		
		DOInterfaceBuilder pointOfContactBuilder = tpf.createInterfaceBuilder("mytest.model.PointOfContact");
		DOType pointOfContact = pointOfContactBuilder.build();
		
		DOClassBuilder addressBuilder = tpf.createClassBuilder("mytest.model.Address");
		addressBuilder.addInterface(pointOfContact);
		addressBuilder.addField("street", String.class.getName());
		addressBuilder.addField("number", int.class.getName());
		DOType addressType = addressBuilder.build();
		
		DOClassBuilder subjectTypeBuilder = tpf.createClassBuilder("mytest.model.Subject");
		subjectTypeBuilder.setAbstract();
		subjectTypeBuilder.addField("subjectType", subjectTypes.getName());
		subjectTypeBuilder.addListField("pointsOfContact", "mytest.model.PointOfContact");
		DOType subject = subjectTypeBuilder.build();
		
		DOClassBuilder personTypeBuilder = tpf.createClassBuilder("mytest.model.Person");
		personTypeBuilder.addField("firstName", String.class.getName());
		personTypeBuilder.addField("lastName", String.class.getName());
		personTypeBuilder.addField("birthDate", Date.class.getName());
		personTypeBuilder.setSuperType(subject);
		DOType personType = personTypeBuilder.build();
		
		String domModel_0 = ((IIntDomainAccess)gda.getDomainAccess()).getInternalDomainAccess().domainModelAsString();
		
		DomainObject anAddress = new DomainObject(addressType);
		anAddress.setFieldValue("street", "Market Street");
		anAddress.setFieldValue("number", 42);
		
		DomainObject aPerson = new DomainObject(personType);
		aPerson.setFieldValue("firstName", "Maxwell");
		aPerson.setFieldValue("lastName", "Smart");
		GregorianCalendar cal = new GregorianCalendar(1940, 0, 22);
		Date birthDate = cal.getTime();
		aPerson.setFieldValue("birthDate", birthDate);
		aPerson.setFieldValue("subjectType", subjectTypes.getEnumValue("NAT_PERSON"));
		
		aPerson.addListFieldValue("pointsOfContact", anAddress);
		
		Object address = aPerson.getListFieldValue("pointsOfContact", 0);
		
		assertTrue(anAddress == address);
		assertTrue(InternalAccess.getRawObject(anAddress) == InternalAccess.getRawObject((DomainObject) address));
		
		String domModel_1 = ((IIntDomainAccess)gda.getDomainAccess()).getInternalDomainAccess().domainModelAsString();
		assertEquals(domModel_0, domModel_1);
		
		Object[] enumVals = subjectTypes.getEnumValues();
		assertEquals("NAT_PERSON", enumVals[0].toString());
		assertEquals("JUR_PERSON", enumVals[1].toString());
		List<DomainObject> persons = new ArrayList<DomainObject>();
		persons.add(aPerson);
		
//		errors = gda.store(aPerson);
		errors = gda.store(persons);
		if (errors.size() > 0) {
			printErrors(errors);
			throw new JcResultException(errors);
		}
		
		String domModel_2 = ((IIntDomainAccess)gda.getDomainAccess()).getInternalDomainAccess().domainModelAsString();
		assertEquals(domModel_1, domModel_2);
		
		List<DomainObject> objects = gda.loadByType("mytest.model.Person", -1, 0, -1);
		assertTrue(aPerson == objects.get(0));
		assertTrue(InternalAccess.getRawObject(aPerson) == InternalAccess.getRawObject((DomainObject) objects.get(0)));
		
		IGenericDomainAccess gda1 = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		List<DomainObject> objects_2 = gda1.loadByType("mytest.model.Person", -1, 0, -1);
		
		assertFalse(objects.get(0) == objects_2.get(0));
		assertFalse(InternalAccess.getRawObject(objects.get(0)) == InternalAccess.getRawObject((DomainObject) objects_2.get(0)));
		
		DOToString doToString = new DOToString(Format.PRETTY_1);
		DOWalker walker = new DOWalker(aPerson, doToString);
		walker.walkDOGraph();
		String str = doToString.getBuffer().toString();
		
		doToString = new DOToString(Format.PRETTY_1);
		walker = new DOWalker(objects_2.get(0), doToString);
		walker.walkDOGraph();
		String str2 = doToString.getBuffer().toString();
		
		assertEquals(str, str2);
		
		return;
	}
}
