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

package test;

import iot.jcypher.JC;
import iot.jcypher.api.IClause;
import iot.jcypher.factories.clause.CREATE;
import iot.jcypher.factories.clause.CREATE_INDEX;
import iot.jcypher.factories.clause.CREATE_UNIQUE;
import iot.jcypher.factories.clause.DO;
import iot.jcypher.factories.clause.DROP_INDEX;
import iot.jcypher.factories.clause.FOR_EACH;
import iot.jcypher.factories.clause.MATCH;
import iot.jcypher.factories.clause.NATIVE;
import iot.jcypher.factories.clause.RETURN;
import iot.jcypher.factories.clause.START;
import iot.jcypher.factories.clause.UNION;
import iot.jcypher.factories.clause.USING;
import iot.jcypher.factories.clause.WHERE;
import iot.jcypher.factories.clause.WITH;
import iot.jcypher.factories.xpression.C;
import iot.jcypher.factories.xpression.F;
import iot.jcypher.factories.xpression.I;
import iot.jcypher.factories.xpression.P;
import iot.jcypher.factories.xpression.X;
import iot.jcypher.values.JcCollection;
import iot.jcypher.values.JcNode;
import iot.jcypher.values.JcNumber;
import iot.jcypher.values.JcPath;
import iot.jcypher.values.JcRelation;
import iot.jcypher.values.JcString;
import iot.jcypher.values.JcValue;
import iot.jcypher.writer.Format;

import org.junit.Test;

import util.TestDataReader;

//@Ignore
public class ClauseTest extends AbstractTestSuite {

	@Test
	public void testStart_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);
		
		TestDataReader tdr = new TestDataReader("/test/Test_START_01.txt");
		
		JcNode n = new JcNode("n");
		JcNode n1 = new JcNode("n1");
		JcRelation r = new JcRelation("r");

		/*******************************/
		IClause start = START.node(n).byIndex("Persons").property("name")
				.value("Tobias");
		
		result = print(start, Format.PRETTY_1);
		testId = "START_01";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		start = START.relation(r).byIndex("Addresses").property("type")
				.value("delivery");
		
		result = print(start, Format.PRETTY_1);
		testId = "START_02";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		start = START.node(n).byIndex("Persons").query("name:A");
		
		result = print(start, Format.PRETTY_1);
		testId = "START_03";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		start = START.node(n1).byId(0, 1);
		
		result = print(start, Format.PRETTY_1);
		testId = "START_04";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		start = START.relation(r).byId(2, 3);
		
		result = print(start, Format.PRETTY_1);
		testId = "START_05";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		start = START.node(n).all();
		
		result = print(start, Format.PRETTY_1);
		testId = "START_06";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		start = START.relation(r).all();
		
		result = print(start, Format.PRETTY_1);
		testId = "START_07";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		IClause[] clauses = new IClause[] {
				START.node(n).byIndex("Persons").property("name").value("Tobias"),
				START.relation(r).byId(2, 3) };
		
		result = print(clauses, Format.PRETTY_1);
		testId = "START_08";
		assertQuery(testId, result, tdr.getTestData(testId));

	}

	@Test
	public void testMatch_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);
		
		TestDataReader tdr = new TestDataReader("/test/Test_MATCH_01.txt");
		
		JcNode n = new JcNode("n");
		JcNode x = new JcNode("x");
		JcNode movie = new JcNode("movie");
		JcNode director = new JcNode("director");
		JcNode charlie = new JcNode("charlie");
		JcNode martin = new JcNode("martin");
		JcNode wallstreet = new JcNode("wallstreet");
		JcNode actor = new JcNode("actor");
		JcNode person = new JcNode("person");
		JcNode co_actor = new JcNode("co_actor");
		JcNode michael = new JcNode("michael");
		JcNode oliver = new JcNode("oliver");
		
		JcRelation r = new JcRelation("r");
		JcRelation charly_martin = new JcRelation("charly_martin");
		
		JcPath p = new JcPath("p");

		/*******************************/
		IClause match = MATCH.node(n);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_01";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(movie).label("Movie");
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_02";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(director).property("name").value("Oliver Stone")
				.relation().node(movie);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_03";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(charlie).label("Person").property("name")
				.value("Charlie Sheen").relation().node(movie).label("Movie");
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_04";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(martin).property("name").value("Martin Sheen")
				.relation().out().node(movie);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_05";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(martin).property("name").value("Martin Sheen")
				.relation(r).out().node(movie);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_06";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(wallstreet).property("title").value("Wall Street")
				.relation().in().type("ACTED_IN").node(actor);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_07";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(wallstreet).property("title").value("Wall Street")
				.relation().in().type("ACTED_IN").type("DIRECTED")
				.node(person);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_08";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(wallstreet).property("title").value("Wall Street")
				.relation(r).in().type("ACTED_IN").node(actor);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_09";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(charlie).property("name").value("Charlie Sheen")
				.relation().out().type("ACTED_IN").node(movie).relation()
				.in().type("DIRECTED").node(director);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_10";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(martin).property("name").value("Martin Sheen")
				.relation().type("ACTED_IN").maxHops(2).node(x);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_11";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(charlie).label("Person").property("firstName")
				.value("Charly").property("lastName").value("Sheen")
				.relation(charly_martin).maxHops(15).property("blocked")
				.value(false).node(martin).label("Person")
				.property("firstName").value("Martin").property("lastName")
				.value("Sheen");
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_12";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.node(actor).property("name").value("Martin Sheen")
				.relation(r).type("ACTED_IN").maxHops(2).node(co_actor);
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_13";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		match = MATCH.path(p).node(michael).property("name")
				.value("Michael Douglas").relation().out().node();
		
		result = print(match, Format.PRETTY_1);
		testId = "MATCH_14";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		IClause[] clauses = new IClause[] {
				MATCH.node(martin).label("Person").property("name")
						.value("Martin Sheen"),
				MATCH.node(oliver).label("Person").property("name")
						.value("Oliver Stone"),
				MATCH.shortestPath(p).node(martin).relation().minHops(0)
						.maxHops(15).node(oliver) };
		
		result = print(clauses, Format.PRETTY_3);
		testId = "MATCH_15";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		clauses = new IClause[] {
				MATCH.node(martin).label("Person").property("name")
						.value("Martin Sheen"),
				MATCH.node(michael).label("Person").property("name")
						.value("Michael Douglas"),
				MATCH.allShortestPaths(p).node(martin).relation()
						.hopsUnbound().node(michael) };
		
		result = print(clauses, Format.PRETTY_3);
		testId = "MATCH_16";
		assertQuery(testId, result, tdr.getTestData(testId));
	}

	@Test
	public void testWhere_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);
		
		TestDataReader tdr = new TestDataReader("/test/Test_WHERE_01.txt");
		
		JcNode n = new JcNode("n");
		JcNode m = new JcNode("m");
		JcNode nd = new JcNode("nd");
		JcNode tobias = new JcNode("tobias");
		JcNode others = new JcNode("others");
		JcNode charlie = new JcNode("charlie");
		
		JcRelation r = new JcRelation("r");
		
		JcPath p = new JcPath("p");

		/*******************************/
		IClause where = WHERE.valueOf(charlie.property("firstName"))
				.EQUALS("charlie").AND().valueOf(charlie.property("lastName"))
				.EQUALS("sheen").OR().BR_OPEN().valueOf(charlie
				.property("lastName")).EQUALS("Huber").BR_CLOSE();
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_01";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.BR_OPEN().valueOf(charlie.property("firstName"))
				.EQUALS("charlie").AND().valueOf(charlie.property("lastName"))
				.EQUALS("sheen").BR_CLOSE().OR().NOT().BR_OPEN()
				.valueOf(charlie.property("lastName")).EQUALS("Huber")
				.BR_CLOSE();
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_02";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.has(n.label("Swedish"));
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_03";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.valueOf(n.property("age")).LT(30);
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_04";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.	has(n.property("belt"));
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_05";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.valueOf(n.property("name")).REGEX("Tob.*");
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_06";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.valueOf(others.property("name")).IN_list("Andreas", "Peter")
				.AND()
				.existsPattern(X.node(tobias).relation().in().node(others));
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_07";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.valueOf(n.property("name")).EQUALS("Andres").AND()
				.valueOf(r.type()).REGEX("K.*");
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_08";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.valueOf(n.property("belt")).EQUALS("white").OR()
				.valueOf(n.property("belt")).IS_NULL();
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_09";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		where = WHERE.valueOf(n.property("name")).EQUALS(m.property("name")).AND()
				.valueOf(r.type()).REGEX("K.*");
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_10";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		where = WHERE.valueOf(others.property("name")).IN(C.EXTRACT().valueOf(nd.property("name")).fromAll(nd).IN_nodes(p));
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_11";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		where = WHERE.valueOf(n).IN(p.nodes());
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_12";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		where = WHERE.valueOf(r).IN(p.relations());
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_13";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		where = WHERE.valueOf(tobias.property("labelProp")).IN(n.labels());
		
		result = print(where, Format.PRETTY_1);
		testId = "WHERE_14";
		assertQuery(testId, result, tdr.getTestData(testId));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPredicateFunctions_Where_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);
		
		TestDataReader tdr = new TestDataReader("/test/Test_PF_WHERE_01.txt");
		
		JcValue x = new JcValue("x");
		JcNode var = new JcNode("var");
		JcNode n = new JcNode("n");
		JcNode a = new JcNode("a");
		JcPath p = new JcPath("p");

		/*******************************/
		IClause where = WHERE.holdsTrue(I.forAll(x).IN_list("AB", "BC", 1234)
				.WHERE().valueOf(x).REGEX("A.*"));
		
		result = print(where, Format.PRETTY_1);
		testId = "PF_WHERE_01";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.holdsTrue(I.forAll(n).IN_nodes(p).WHERE()
				.valueOf(n.property("age")).GT(30));
		
		result = print(where, Format.PRETTY_1);
		testId = "PF_WHERE_02";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.holdsTrue(I.forAny(x).IN(a.collectionProperty("array")).WHERE()
				.valueOf(x).EQUALS("one"));
		
		result = print(where, Format.PRETTY_1);
		testId = "PF_WHERE_03";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE.holdsTrue(I.forNone(n).IN(p.nodes()).WHERE()
				.valueOf(n.property("age")).EQUALS(25));
		
		result = print(where, Format.PRETTY_1);
		testId = "PF_WHERE_04";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		where = WHERE
				.valueOf(n.property("name"))
				.EQUALS("Alice")
				.AND()
				.BR_OPEN()
				.holdsTrue(
						I.forSingle(var).IN_nodes(p).WHERE()
								.valueOf(var.property("eyes")).EQUALS("blue")).BR_CLOSE();
		
		result = print(where, Format.PRETTY_1);
		testId = "PF_WHERE_05";
		assertQuery(testId, result, tdr.getTestData(testId));
	}

	@Test
	public void testReturn_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_RETURN_01.txt");
		
		JcNode a = new JcNode("a");
		JcNode b = new JcNode("b");
		JcNode n = new JcNode("n");
		JcPath p = new JcPath("p");
		JcNumber somethingTotallyDifferent = new JcNumber("SomethingTotallyDifferent");
		JcValue alias = new JcValue("alias");
		JcCollection uniqueRelations = new JcCollection("UniqueRelations");
		JcString personName = new JcString("personName");
		JcNode thePerson = new JcNode("thePerson");
		

		/*******************************/
		IClause returns = RETURN.value(n);

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_01";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.value(n.property("name"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_02";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.ALL();

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_03";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.value(a.property("age"))
				.AS(somethingTotallyDifferent);

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_04";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.evalPredicate(P.valueOf(a.property("age")).GT(30));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_05";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		IClause[] clauses = new IClause[] {
				RETURN.evalPredicate(P.valueOf(a.property("age")).GT(30)),
				RETURN.existsPattern(X.node(a).relation().out().node()).AS(alias) };
		result = print(clauses, Format.PRETTY_3);

		testId = "RETURN_06";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.existsPattern(X.node(a).relation().out().node());

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_07";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.DISTINCT().value(b);

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_08";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.DISTINCT().ALL();

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_09";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.DISTINCT().existsPattern(X.node().relation().out().node());

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_10";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.collection(p.nodes());

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_11";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		returns = RETURN.DISTINCT().collection(p.relations())
				.AS(uniqueRelations);

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_12";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		clauses = new IClause[] {
				RETURN.value(n.property("name")).AS(personName),
				RETURN.value(n).AS(thePerson).ORDER_BY("age")
						.ORDER_BY_DESC("name").LIMIT(3).SKIP(1) };
		result = print(clauses, Format.PRETTY_2);

		testId = "RETURN_13";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.count().value(p.nodes());

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_14";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().sum(n.property("age"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_15";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().avg(n.property("age"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_16";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().percentileDisc(0.5).over(n.property("age"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_17";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().percentileCont(0.4).over(n.property("age"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_18";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().stdev(n.property("age"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_19";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().stdevp(n.property("age"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_20";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().max(n.property("age"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_21";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().min(n.property("age"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_22";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		clauses = new IClause[] {
				MATCH.node(a).label("Person").property("name").value("A").relation().out().node(b),
				RETURN.count().DISTINCT().value(b.property("eyeColor")) };
		result = print(clauses, Format.PRETTY_1);

		testId = "RETURN_23";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.aggregate().DISTINCT().sum(n.property("value"));

		result = print(returns, Format.PRETTY_1);
		testId = "RETURN_24";
		assertQuery(testId, result, tdr.getTestData(testId));
	}

	@Test
	public void testReturn_Collection_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_RETURN_COLLECTION_01.txt");
		
		JcCollection x = new JcCollection("x");
		JcCollection nds = new JcCollection("nds");
		JcCollection friends = new JcCollection("friends");
		JcValue label = new JcValue("label");
		JcNumber totalAge = new JcNumber("totalAge");
		JcNumber totalAmount = new JcNumber("totalAmount");
		JcNode n = new JcNode("n");
		JcNode a = new JcNode("a");
		JcNode y = new JcNode("y");
		JcRelation r = new JcRelation("r");
		JcPath p = new JcPath("p");
		JcCollection extracted = new JcCollection("extracted");
		JcNumber reduction = new JcNumber("reduction");
		JcCollection totalAmounts = new JcCollection("totalAmounts");

		/*******************************/
		IClause returns = RETURN.collection(
				C.COLLECT().property("name").from(nds)
		);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.EXTRACT().valueOf(n.property("name")).fromAll(n).IN(nds)
		);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.EXTRACT().valueOf(n.property("age")).fromAll(n).IN_nodes(p)
		).AS(extracted);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.EXTRACT().valueOf(n.property("age")).fromAll(n).IN(p.nodes())
		).AS(extracted);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.evalPredicate(
				C.FILTER().fromAll(x).IN(a.collectionProperty("array")).WHERE().
				BR_OPEN().valueOf(x.length()).EQUALS(3).OR().valueOf(x.length()).GT(5).
				BR_CLOSE().AND().NOT().
				BR_OPEN().valueOf(x).EQUALS("hallo").BR_CLOSE()
		);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.TAIL(p.nodes())
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_06";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.TAIL().TAIL().COLLECT().property("name").from(nds)
		);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_07";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.TAIL().EXTRACT().valueOf(n.property("age")).fromAll(n).IN_nodes(p)
		);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_08";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.TAIL().COLLECT().property("name").from(friends)
		);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_09";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.evalPredicate(
				C.TAIL().FILTER().fromAll(n)
				.IN(C.FILTER().fromAll(y).IN_nodes(p).WHERE().valueOf(y.property("name")).EQUALS("Hans"))
				.WHERE().valueOf(n.property("age")).GT(30)
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_10";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.evalPredicate(
				C.FILTER().fromAll(r).IN(p.relations()).WHERE().has(r.property("name"))
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_11";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.evalPredicate(
				C.FILTER().fromAll(r).IN_relations(p).WHERE().has(r.property("name"))
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_12";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.evalPredicate(
				C.FILTER().fromAll(r).IN_relations(p).WHERE().NOT().valueOf(r.property("name")).IS_NULL()
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_13";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				a.labels()
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_14";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.evalPredicate(
				C.FILTER().fromAll(label).IN(n.labels()).WHERE().valueOf(label).EQUALS("Address")
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_15";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.evalPredicate(
				C.FILTER().fromAll(label).IN_labels(n).WHERE().valueOf(label).EQUALS("Address")
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_16";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.evalPredicate(
				C.FILTER().fromAll(n).IN_nodes(p).WHERE().has(n.label("Person"))
		);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_17";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.REDUCE().fromAll(n).IN_nodes(p).to(totalAge).by(totalAge.plus(n.numberProperty("age"))).startWith(0)
		)
		.AS(reduction);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_18";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.REDUCE().fromAll(n).IN(
						C.TAIL(p.nodes()))
				.to(totalAge).by(totalAge.plus(n.numberProperty("age"))).startWith(0)
		)
		.AS(reduction);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_19";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.REDUCE().fromAll(n).IN_nodes(p).to(totalAmount).by(totalAmount.plus(n.numberProperty("amount")))
				.startWith(a.property("amount"))
		)
		.AS(reduction);

		result = print(returns, Format.PRETTY_2);
		testId = "COLLECTION_20";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.DISTINCT().evalPredicate(
				C.FILTER().fromAll(x).IN(a.collectionProperty("array")).WHERE().
				BR_OPEN().valueOf(x.length()).EQUALS(3).OR().valueOf(x.length()).GT(5).
				BR_CLOSE().AND().NOT().
				BR_OPEN().valueOf(x).EQUALS("hallo").BR_CLOSE()
		);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_21";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		returns = RETURN.collection(
				C.EXTRACT().valueOf(n.numberProperty("amount1").plus(n.numberProperty("amount2"))).fromAll(n).IN_nodes(p)
		)
		.AS(totalAmounts);

		result = print(returns, Format.PRETTY_1);
		testId = "COLLECTION_22";
		assertQuery(testId, result, tdr.getTestData(testId));
		
	}

	/**
	 * WITH provides the same expressions as RETURN
	 */
	@Test
	public void testWith_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_WITH_01.txt");
		
		JcNode n = new JcNode("n");
		JcString resultName = new JcString("resultName");
		JcString personName = new JcString("personName");
		JcNode thePerson = new JcNode("thePerson");

		/*******************************/
		IClause with = WITH.value(n);

		result = print(with, Format.PRETTY_1);
		testId = "WITH_01";
		assertQuery(testId, result, tdr.getTestData(testId));

		/*******************************/
		with = WITH.value(n.property("name")).AS(resultName);

		result = print(with, Format.PRETTY_1);
		testId = "WITH_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		IClause[] clauses = new IClause[] {
				WITH.value(n.property("name")).AS(personName),
				WITH.value(n).AS(thePerson).ORDER_BY("age")
						.ORDER_BY_DESC("name").LIMIT(3).SKIP(1) };
		result = print(clauses, Format.PRETTY_2);

		testId = "WITH_03";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testForEach_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_FOREACH_01.txt");
		
		JcValue val = new JcValue("val");
		JcValue a = new JcValue("a");
		JcNode n = new JcNode("n");
		JcNode source = new JcNode("source");
		JcNode movie = new JcNode("movie");
		JcNode actor = new JcNode("actor");
		JcRelation r = new JcRelation("r");
		JcPath p = new JcPath("p");
		
		JcCollection coll = new JcCollection("coll");
		
		JcValue x = new JcValue("x");

		/*******************************/
		IClause foreach = FOR_EACH.element(n).IN_nodes(p).DO()
				.SET(n.property("marked")).to(true).AND_DO()
				.SET(n.property("name")).to("John");

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		foreach = FOR_EACH.element(r).IN_relations(p).DO()
				.SET(r.property("marked")).to(true);

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		foreach = FOR_EACH.element(val).IN_list("a", "b", "c").
				DO().
				SET(n.property("value")).byExpression(val);

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		foreach = FOR_EACH.element(a).IN_list("Neo", "{name: \"Keanu\"}", 1234).DO().
				CREATE_UNIQUE(X.node(movie).relation().out().node(actor)).AND_DO().
				SET(actor.property("id")).byExpression(JC.coalesce(actor.property("id"), coll.last()));

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		foreach = FOR_EACH.element(n).IN_nodes(p).DO()
				.FOR_EACH(F.element(x).IN_list("Stan", "Will", "Henry").DO().
						CREATE_UNIQUE(X.node().property("name").value(x).relation().in().node(n)));

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		foreach = FOR_EACH.element(n).IN_nodes(p).DO()
				.DELETE(n);

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_06";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		foreach = FOR_EACH.element(r).IN_relations(p).DO()
				.DELETE(r);

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_07";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		foreach = FOR_EACH.element(n).IN_nodes(p).DO()
				.REMOVE(n.property("age"));

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_08";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		foreach = FOR_EACH.element(n).IN_nodes(p).DO()
				.copyPropertiesFrom(source).to(n);

		result = print(foreach, Format.PRETTY_1);
		testId = "FOREACH_09";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testCreate_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_CREATE_01.txt");
		
		JcNode n = new JcNode("n");
		JcNode a = new JcNode("a");
		JcNode b = new JcNode("b");
		JcNode andres = new JcNode("andres");
		JcNode neo = new JcNode("neo");
		JcNode michael = new JcNode("michael");
		
		JcRelation r = new JcRelation("r");
		
		JcPath p = new JcPath("p");

		/*******************************/
		IClause create = CREATE.node(n);

		result = print(create, Format.PRETTY_1);
		testId = "CREATE_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		create = CREATE.node(n).label("Person");

		result = print(create, Format.PRETTY_1);
		testId = "CREATE_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		create = CREATE.node(n).label("Person").label("Swedish");

		result = print(create, Format.PRETTY_1);
		testId = "CREATE_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		create = CREATE.node(n).label("Person").
				property("name").value("Andres").
				property("title").value("Developer");

		result = print(create, Format.PRETTY_1);
		testId = "CREATE_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		create = CREATE.node(a).relation(r).out().type("RELTYPE").node(b);

		result = print(create, Format.PRETTY_1);
		testId = "CREATE_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		create = CREATE.node(a).relation(r).out().type("RELTYPE").
				property("name")
				.value(a.stringProperty("name").concat("<->").concat(b.stringProperty("name")))
				.node(b);

		result = print(create, Format.PRETTY_1);
		testId = "CREATE_06";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		create = CREATE.path(p).
				node(andres).property("name").value("Andres").
				relation().out().type("WORKS_AT").node(neo).
				relation().in().type("WORKS_AT").
				node(michael).property("name").value("Micheal");

		result = print(create, Format.PRETTY_1);
		testId = "CREATE_07";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		IClause[] clauses = new IClause[] {
				CREATE.node(a).relation(r).out().type("RELTYPE").node(b),
				CREATE.path(p).
				node(andres).property("name").value("Andres").
				relation().out().type("WORKS_AT").node(neo).
				relation().in().type("WORKS_AT").
				node(michael).property("name").value("Micheal") };
		result = print(clauses, Format.PRETTY_3);

		testId = "CREATE_08";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testCreateUnique_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_CREATE_U_01.txt");
		
		JcNode a = new JcNode("a");
		JcNode b = new JcNode("b");
		JcNode andres = new JcNode("andres");
		JcNode neo = new JcNode("neo");
		JcNode michael = new JcNode("michael");
		
		JcRelation r = new JcRelation("r");
		
		JcPath p = new JcPath("p");
		
		/*******************************/
		IClause create = CREATE_UNIQUE.node(a).relation(r).out().type("RELTYPE").node(b);

		result = print(create, Format.PRETTY_1);
		testId = "CREATE_U_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		IClause[] clauses = new IClause[] {
				CREATE_UNIQUE.node(a).relation(r).out().type("RELTYPE").node(b),
				CREATE_UNIQUE.path(p).
				node(andres).property("name").value("Andres").
				relation().out().type("WORKS_AT").node(neo).
				relation().in().type("WORKS_AT").
				node(michael).property("name").value("Micheal") };
		result = print(clauses, Format.PRETTY_3);

		testId = "CREATE_U_02";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testUse_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_USE_01.txt");
		
		JcNode n = new JcNode("n");
		JcNode m = new JcNode("m");
		
		/*******************************/
		IClause use = USING.INDEX("Swedish").on(n.property("surname"));

		result = print(use, Format.PRETTY_1);
		testId = "USE_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		IClause[] clauses = new IClause[] {
				USING.INDEX("German").on(m.property("surname")),
				USING.INDEX("Swedish").on(n.property("surname")) };
		result = print(clauses, Format.PRETTY_1);

		testId = "USE_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		use = USING.LABEL_SCAN("German").on(m);

		result = print(use, Format.PRETTY_1);
		testId = "USE_03";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testSet_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_SET_01.txt");
		
		JcNode n = new JcNode("n");
		JcNode m = new JcNode("m");
		JcNode sum = new JcNode("sum");
		JcNode at = new JcNode("at");
		JcNode pn = new JcNode("pn");
		
		/*******************************/
		IClause set = DO.SET(n.property("surname")).to("Taylor");

		result = print(set, Format.PRETTY_1);
		testId = "SET_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		set = DO.SET(sum.property("amount"))
						.byExpression(n.numberProperty("amount").plus(m.numberProperty("amount")));

		result = print(set, Format.PRETTY_1);
		testId = "SET_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		set = DO.SET(n.property("name")).toNull();

		result = print(set, Format.PRETTY_1);
		testId = "SET_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		set = DO.copyPropertiesFrom(pn).to(at);

		result = print(set, Format.PRETTY_1);
		testId = "SET_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		set = DO.SET(n.label("German"));

		result = print(set, Format.PRETTY_1);
		testId = "SET_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		set = DO.SET(n.label("Swedish").label("Bossman"));

		result = print(set, Format.PRETTY_1);
		testId = "SET_06";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testDelete_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_DELETE_01.txt");
		
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		
		/*******************************/
		IClause delete = DO.DELETE(n);

		result = print(delete, Format.PRETTY_1);
		testId = "DELETE_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		IClause[] clauses = new IClause[] {
				DO.DELETE(n),
				DO.DELETE(r) };
		result = print(clauses, Format.PRETTY_1);

		testId = "DELETE_02";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testRemove_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_REMOVE_01.txt");
		
		JcNode andres = new JcNode("andres");
		JcNode n = new JcNode("n");
		
		/*******************************/
		IClause remove = DO.REMOVE(andres.property("age"));

		result = print(remove, Format.PRETTY_1);
		testId = "REMOVE_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		remove = DO.REMOVE(n.label("German"));

		result = print(remove, Format.PRETTY_1);
		testId = "REMOVE_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		remove = DO.REMOVE(n.label("German").label("Swedish"));

		result = print(remove, Format.PRETTY_1);
		testId = "REMOVE_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		IClause[] clauses = new IClause[] {
				DO.REMOVE(andres.property("age")),
				DO.REMOVE(n.label("German").label("Swedish")) };
		result = print(clauses, Format.PRETTY_1);

		testId = "REMOVE_04";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testIndex_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_INDEX_01.txt");
		
		/*******************************/
		IClause index = CREATE_INDEX.onLabel("Person").forProperty("name");

		result = print(index, Format.PRETTY_1);
		testId = "INDEX_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		index = DROP_INDEX.onLabel("Person").forProperty("name");

		result = print(index, Format.PRETTY_1);
		testId = "INDEX_02";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testNative_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_NATIVE_01.txt");
		
		/*******************************/
		IClause nativ = NATIVE.cypher(
				"MERGE (keanu:Person { name:'Keanu Reeves' })",
				"ON CREATE SET keanu.created = timestamp()",
				"ON MATCH SET keanu.lastSeen = timestamp()");

		result = print(nativ, Format.PRETTY_1);
		testId = "NATIVE_01";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testUnion_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/Test_UNION_01.txt");
		
		/*******************************/
		IClause union = UNION.distinct();

		result = print(union, Format.PRETTY_1);
		testId = "UNION_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		union = UNION.all();

		result = print(union, Format.PRETTY_1);
		testId = "UNION_02";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
}
