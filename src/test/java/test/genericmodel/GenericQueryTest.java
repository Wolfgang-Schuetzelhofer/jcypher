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
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.GDomainQuery;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.NATIVE;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.QueriesPrintObserver.ContentToObserve;
import iot.jcypher.util.QueriesPrintObserver.QueryToObserve;
import iot.jcypher.util.Util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import test.AbstractTestSuite;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.Person;

public class GenericQueryTest extends AbstractTestSuite {

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
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
//		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "jcypher");
		
		QueriesPrintObserver.addOutputStream(System.out);
		
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.COUNT_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY, ContentToObserve.CYPHER);
		QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOMAIN_INFO, ContentToObserve.CYPHER);
		
		// init db
//		List<JcError> errors = dbAccess.clearDatabase();
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
//		loadPeopleDomain();
		
	}
	
	private static void loadPeopleDomain() {
		InputStreamReader ir = null;
		try {
			InputStream in = GenericQueryTest.class.getResourceAsStream("/test/load/people_domain.txt");
			ir = new InputStreamReader(in);
			
			LineNumberReader lnr = new LineNumberReader(ir);
			List<String> lns = new ArrayList<String>();
			String line = lnr.readLine();
			while(line != null) {
				lns.add(line);
				line = lnr.readLine();
			}
			String[] lines = lns.toArray(new String[lns.size()]);
			IClause[] clauses = new IClause[] {
					NATIVE.cypher(lines)
			};
			JcQuery q = new JcQuery();
			q.setClauses(clauses);
			
			JcQueryResult result = dbAccess.execute(q);
			if (result.hasErrors()) {
				printErrors(result, true);
				throw new JcResultException(Util.collectErrors(result));
			}
			
		} catch(Throwable e) {
			throw new RuntimeException(e);
		} finally {
			try {
				ir.close();
			} catch (Throwable e) {}
		}
	}
	
	@Test
	public void testGenericQuerySelect_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		/** 01 ****************************************/
		q = gda.createQuery();
		
		DomainObjectMatch<DomainObject> j_smith = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		DomainObjectMatch<DomainObject> europe = q.createMatch("iot.jcypher.samples.domain.people.model.Area");
		
		q.WHERE(europe.atttribute("name")).EQUALS("Europe");

		q.WHERE(j_smith.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smith.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<DomainObject> j_smith_Addresses =
				q.TRAVERSE_FROM(j_smith).FORTH("pointsOfContact")
					.TO_GENERIC("iot.jcypher.samples.domain.people.model.PointOfContact");
		DomainObjectMatch<DomainObject> j_smith_Areas =
				q.TRAVERSE_FROM(j_smith_Addresses).FORTH("area")
					.FORTH("partOf").DISTANCE(0, -1).TO_GENERIC("iot.jcypher.samples.domain.people.model.Area");
		
//		DomainObjectMatch<DomainObject> j_smith_FilteredPocs =
//				q.SELECT_FROM(j_smith_Addresses).ELEMENTS(
//						q.WHERE(j_smith_Areas).CONTAINS(europe)
//					);
		DomainQueryResult result = q.execute();
		
		List<DomainObject> j_smithResult = result.resultOf(j_smith);
		List<DomainObject> europeResult = result.resultOf(europe);
		List<DomainObject> j_smith_AddressesResult = result.resultOf(j_smith_Addresses);
		List<DomainObject> j_smith_AreasResult = result.resultOf(j_smith_Areas);
//		List<DomainObject> j_smith_FilteredPocsResult = result.resultOf(j_smith_FilteredPocs);
		
		return;
	}
	
	@Test
	public void testGenericQuery_01() {
		IGenericDomainAccess gda = DomainAccessFactory.createGenericDomainAccess(dbAccess, domainName);
		GDomainQuery q;
		
		/** 01 ****************************************/
		q = gda.createQuery();
		DomainObjectMatch<DomainObject> j_smithMatch = q.createMatch("iot.jcypher.samples.domain.people.model.Person");
		q.WHERE(j_smithMatch.atttribute("lastName")).EQUALS("Smith");
		q.WHERE(j_smithMatch.atttribute("firstName")).EQUALS("John");
		
		DomainObjectMatch<DomainObject> j_smith_AddressesMatch =
				q.TRAVERSE_FROM(j_smithMatch).FORTH("pointsOfContact")
					.TO_GENERIC("iot.jcypher.samples.domain.people.model.PointOfContact");
		
		DomainQueryResult result = q.execute();
		
		List<DomainObject> j_smith = result.resultOf(j_smithMatch);
		List<DomainObject> j_smith_Addresses = result.resultOf(j_smith_AddressesMatch);
		
		return;
	}
}
