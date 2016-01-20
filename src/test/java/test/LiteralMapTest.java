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

package test;

import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.LiteralMap;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.Util;

public class LiteralMapTest extends AbstractTestSuite {
	
	private static IDBAccess dbAccess;

	@Test
	public void testLiteralMap_01() {
		IClause[] clauses = new IClause[]{
				CREATE.node().label("Movie").property("name").value("movie_1")
					.property("actorCount").value(5),
				CREATE.node().label("Movie").property("name").value("movie_2")
					.property("actorCount").value(6),
				CREATE.node().label("Movie").property("name").value("movie_3")
					.property("actorCount").value(7),
		};
		
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		assertFalse(result.hasErrors());
		
		JcNode n = new JcNode("n");
		JcString name = new JcString("name");
		JcNumber extraInfo = new JcNumber("extraInfo");
		// if JcString is constructed with a value, the name is ignored
		// and it is taken as a literal
		JcString literal = new JcString(null, "5/7");
		JcString rank = new JcString("rank");
		clauses = new IClause[]{
				MATCH.node(n).label("Movie"),
				RETURN.value(n.property("name")).AS(name),
				RETURN.value(n.property("actorCount")).AS(extraInfo),
				RETURN.value(literal).AS(rank)
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		
		// you can look at the CYPHER query which is generated in the background
		String str = Util.toCypher(query, Format.PRETTY_1);
		System.out.println(str);
		
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		assertFalse(result.hasErrors());
		List<LiteralMap> maps = result.resultMapOf(name, rank, extraInfo);
		
		return;
	}
	
	@BeforeClass
	public static void before() {
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
		
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
	}
}
