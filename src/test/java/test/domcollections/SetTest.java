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

package test.domcollections;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.util.QueriesPrintObserver;
import test.AbstractTestSuite;
import test.DBAccessSettings;

public class SetTest extends AbstractTestSuite {

	private static IDBAccess dbAccess;
	private static ByteArrayOutputStream queriesStream;
	public static String domainName;

	@Test
	public void testSets() {
		AddressNode a_0 = new AddressNode();
		a_0.setGUID("0");

		AddressNode a_1 = new AddressNode();
		a_1.setGUID("1");
		HashSet<AddressNode> nodeSet = new HashSet<AddressNode>();
		nodeSet.add(a_0);
		HashSet<String> idSet = new HashSet<String>();
		idSet.add(a_0.getGUID());
		a_1.setPreAddressNodes(nodeSet);
		a_1.setPreAddressNodeGUIDs(idSet);

		AddressNode a_2 = new AddressNode();
		a_2.setGUID("2");
		nodeSet = new HashSet<AddressNode>();
		nodeSet.add(a_0);
		nodeSet.add(a_1);
		idSet = new HashSet<String>();
		idSet.add(a_0.getGUID());
		idSet.add(a_1.getGUID());
		a_2.setPreAddressNodes(nodeSet);
		a_2.setPreAddressNodeGUIDs(idSet);

		List<Object> domainObjects = new ArrayList<Object>();
		domainObjects.add(a_0);
		domainObjects.add(a_1);
		domainObjects.add(a_2);

		IDomainAccess da1 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		List<JcError> res = da1.store(domainObjects);
		assertTrue(res.isEmpty());

		/*********** load *************************/
		String gid = "2";
		IDomainAccess da2 = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
		DomainQuery q = da2.createQuery();
		// create DomainObjectMatches
		DomainObjectMatch<AddressNode> match = q.createMatch(AddressNode.class);
		q.WHERE(match.atttribute("GUID")).EQUALS(gid);
		DomainQueryResult queryResult = q.execute();
		List<AddressNode> results = queryResult.resultOf(match);
		assertTrue(results.size() == 1);
		
		AddressNode an0 = results.get(0);
		assertTrue(an0.getGUID().equals("2"));
		assertTrue(an0.getPreAddressNodeGUIDs().size() == 2);
		assertTrue(an0.getPreAddressNodes().size() == 2);

		return;
	}

	@BeforeClass
	public static void before() {
		domainName = "SET_TEST";
		dbAccess = DBAccessSettings.createDBAccess();

		QueriesPrintObserver.addOutputStream(System.out);
		queriesStream = new ByteArrayOutputStream();
		QueriesPrintObserver.addOutputStream(queriesStream);

		// QueriesPrintObserver.addToEnabledQueries(QueryToObserve.DOM_QUERY,
		// ContentToObserve.CYPHER);

		List<JcError> errors = dbAccess.clearDatabase();
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
		try {
			queriesStream.close();
		} catch (IOException e) {
		}
		queriesStream = null;
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
	}
}
