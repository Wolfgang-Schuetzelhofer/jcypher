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

package test.detach_delete;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrNode;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import test.DBAccessSettings;
import test.bookingtest.Booking;
import test.bookingtest.JCypherClient;

public class DetachDeleteTest {

	private static IDBAccess dbAccess;
	
	@Test
	public void testDetachDelete() {
		JCypherClient jCypherClient = new JCypherClient(dbAccess);
		jCypherClient.insertBooking(new Booking(1l, "FRAUD", "CONTRACT", "iam@fraudst.er"));
		jCypherClient.insertBooking(new Booking(2l, "LEGIT", "INIT", "iam@fraudst.er"));
		
		JcNode n = new JcNode("n");
		IClause[] clauses = new IClause[]{
				MATCH.node(n).label(JCypherClient.BOOKING_LABEL),
				DO.DETACH_DELETE(n)
		};
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(query);
		assertFalse(result.hasErrors());
		
		clauses = new IClause[]{
				MATCH.node(n).label(JCypherClient.BOOKING_LABEL),
				RETURN.value(n)
		};
		query = new JcQuery();
		query.setClauses(clauses);
		result = dbAccess.execute(query);
		
		List<GrNode> nds = result.resultOf(n);
		assertTrue(nds.isEmpty());
		return;
	}
	
	@BeforeClass
	public static void before() {
		dbAccess = DBAccessSettings.createDBAccess();
		List<JcError> errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
			throw new JcResultException(errors);
		}
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
	}
}
