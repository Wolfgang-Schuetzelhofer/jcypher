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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
import iot.jcypher.query.LiteralMapList;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
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
	public void testLiteralMap_02() {
		List<JcError> errors = dbAccess.clearDatabase();
		assertTrue(errors.isEmpty());

		IClause[] clauses = new IClause[]{
				CREATE.node().label("Movie").property("name").value("movie_1")
					.property("actorCount").value(5),
				CREATE.node().label("Movie").property("name").value("movie_2")
					.property("actorCount").value(6),
				CREATE.node().label("Movie").property("name").value("movie_3")
					.property("actorCount").value(7),
				CREATE.node().label("Movie").property("name").value("movie_4"),
				CREATE.node().label("Movie").property("actorCount").value(9),
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
		
		LiteralMapList mapList = result.resultMapListOf(name, rank, extraInfo);
		assertEquals(5, mapList.size());
		assertEquals(1, mapList.select(name, "movie_1").size());
		assertEquals(1, mapList.select("name", "movie_1").size());
		assertEquals(5, mapList.selectFirst(name, "movie_1").get(extraInfo).intValue());
		assertEquals("5/7", mapList.selectFirst(name, "movie_1").get(rank));
		
		assertEquals(1, mapList.select(name, "movie_2").size());
		assertEquals(1, mapList.select("name", "movie_2").size());
		assertEquals(6, mapList.selectFirst(name, "movie_2").get(extraInfo).intValue());
		assertEquals("5/7", mapList.selectFirst(name, "movie_2").get(rank));
		
		assertEquals(1, mapList.select(name, "movie_3").size());
		assertEquals(1, mapList.select("name", "movie_3").size());
		assertEquals(7, mapList.selectFirst(name, "movie_3").get(extraInfo).intValue());
		assertEquals("5/7", mapList.selectFirst(name, "movie_3").get(rank));
		
		assertEquals(1, mapList.select(name, "movie_4").size());
		assertEquals(1, mapList.select("name", "movie_4").size());
		assertNull(mapList.selectFirst(name, "movie_4").get(extraInfo));
		assertEquals("5/7", mapList.selectFirst(name, "movie_4").get(rank));
		
		assertEquals(0, mapList.select(name, "movie_5").size());
		assertEquals(0, mapList.select("name", "movie_5").size());
		assertEquals(1, mapList.select(extraInfo, 9).size());
		assertEquals("5/7", mapList.selectFirst(extraInfo, 9).get(rank));
		
		
		clauses = new IClause[]{
				CREATE.node().label("Actor").property("name").value("Actor_1"),
				CREATE.node().label("Actor").property("name").value("Actor_2")
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		assertFalse(result.hasErrors());
		
		JcNode m = new JcNode("m");
		JcString actorName = new JcString("actorName");
		clauses = new IClause[]{
				MATCH.node(n).label("Movie"),
				MATCH.node(m).label("Actor"),
				RETURN.value(n.property("name")).AS(name),
				RETURN.value(n.property("actorCount")).AS(extraInfo),
				RETURN.value(m.property("name")).AS(actorName),
				RETURN.value(literal).AS(rank)
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		
		// you can look at the CYPHER query which is generated in the background
		String str1 = Util.toCypher(query, Format.PRETTY_1);
		System.out.println(str1);
		
		result = dbAccess.execute(query);
		if (result.hasErrors())
			printErrors(result);
		assertFalse(result.hasErrors());
		
		mapList = result.resultMapListOf(name, rank, extraInfo, actorName);
		assertEquals(10, mapList.size());
		
		return;
	}
	
	@Test
	public void testLiteralMap_01() {
		List<JcError> errors = dbAccess.clearDatabase();
		assertTrue(errors.isEmpty());
		
		IClause[] clauses = new IClause[]{
				CREATE.node().label("Movie").property("name").value("movie_1")
					.property("actorCount").value(5),
				CREATE.node().label("Movie").property("name").value("movie_2")
					.property("actorCount").value(6),
				CREATE.node().label("Movie").property("name").value("movie_3")
					.property("actorCount").value(7)
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
		LiteralMapList mapList = result.resultMapListOf(name, rank, extraInfo);
		assertEquals(3, mapList.size());
		assertEquals(1, mapList.select(name, "movie_1").size());
		assertEquals(1, mapList.select("name", "movie_1").size());
		assertEquals(5, mapList.selectFirst(name, "movie_1").get(extraInfo).intValue());
		assertEquals("5/7", mapList.selectFirst(name, "movie_1").get(rank));
		
		assertEquals(1, mapList.select(name, "movie_2").size());
		assertEquals(1, mapList.select("name", "movie_2").size());
		assertEquals(6, mapList.selectFirst(name, "movie_2").get(extraInfo).intValue());
		assertEquals("5/7", mapList.selectFirst(name, "movie_2").get(rank));
		
		assertEquals(1, mapList.select(name, "movie_3").size());
		assertEquals(1, mapList.select("name", "movie_3").size());
		assertEquals(7, mapList.selectFirst(name, "movie_3").get(extraInfo).intValue());
		assertEquals("5/7", mapList.selectFirst(name, "movie_3").get(rank));
		
		return;
	}
	
	@BeforeClass
	public static void before() {
		dbAccess = DBAccessSettings.createDBAccess();
		
	}
	
	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
	}
}
