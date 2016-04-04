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

package test.queryparamsmerge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.MERGE;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.Util;
import test.AbstractTestSuite;
import util.TestDataReader;

public class QueryParameterTest extends AbstractTestSuite {

	private static IDBAccess dbAccess;
	private static ByteArrayOutputStream queriesStream;

	@Test
	public void testQueryParams() {
		TestDataReader tdr = new TestDataReader("/test/queryparams/Test_MERGE_01.txt");

		JcNode n = new JcNode("n");
		JcNode applicant1 = new JcNode("a");
		JcNode applicant2 = new JcNode("b");
		JcNode applicant3 = new JcNode("c");
		IClause[] clauses = new IClause[] {
				CREATE.node(applicant1).label("Applicant").property("firstName").value("Jerry").property("lastName")
						.value("Smith").property("UniqueId").value("JerrySmith"),
				MERGE.node(applicant2).label("Applicant").property("firstName").value("John").property("lastName")
						.value("Smith").property("UniqueId").value("JohnSmith"),
				CREATE.node(applicant3).label("Applicant").property("firstName").value("Angie").property("lastName")
						.value("Smith").property("UniqueId").value("AngieSmith") };
		JcQuery q = new JcQuery();
		q.setClauses(clauses);

		String cypher = Util.toCypher(q, Format.PRETTY_1);
		String json = Util.toJSON(q, Format.PRETTY_1);

		assertEquals(tdr.getTestData("MERGE_01"), cypher);
		assertEquals(tdr.getTestData("MERGE_02"), json);

		JcQueryResult result = dbAccess.execute(q);
		if (result.hasErrors()) {
			String err = printErrors(result);
			System.out.println(err);
		}
		assertFalse(result.hasErrors());

		clauses = new IClause[] { 
				MATCH.node(n).label("Applicant"),
				RETURN.value(n)
		};
		q = new JcQuery();
		q.setClauses(clauses);
		result = dbAccess.execute(q);
		if (result.hasErrors()) {
			String err = printErrors(result);
			System.out.println(err);
		}
		assertFalse(result.hasErrors());
		
		List<GrNode> applicants = result.resultOf(n);
		assertEquals(3, applicants.size());
		boolean exists = existsNode("Jerry", "Smith", "JerrySmith", applicants);
		assertTrue(exists);
		exists = existsNode("Angie", "Smith", "AngieSmith", applicants);
		assertTrue(exists);
		exists = existsNode("John", "Smith", "JohnSmith", applicants);
		assertTrue(exists);
		exists = existsNode("Cathy", "Smith", "CathySmith", applicants);
		assertFalse(exists);
	}

	private boolean existsNode(String firstName, String lastName, String uniqueId, List<GrNode> nodes) {
		for(GrNode node : nodes) {
			GrProperty prop = node.getProperty("UniqueId");
			if (prop == null || !uniqueId.equals(prop.getValue().toString()))
				continue;
			prop = node.getProperty("lastName");
			if (prop == null || !lastName.equals(prop.getValue().toString()))
				continue;
			prop = node.getProperty("firstName");
			if (prop == null || !firstName.equals(prop.getValue().toString()))
				continue;
			return true;
		}
		return false;
	}

	@BeforeClass
	public static void before() {
		Properties props = new Properties();

		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");

		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
		// dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props,
		// "neo4j", "jcypher");

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
