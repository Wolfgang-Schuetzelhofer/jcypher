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

package test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domainquery.internal.Settings;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrPath;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.JC;
import iot.jcypher.query.factories.clause.CASE;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.CREATE_UNIQUE;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.ELSE;
import iot.jcypher.query.factories.clause.END;
import iot.jcypher.query.factories.clause.FOR_EACH;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.MERGE;
import iot.jcypher.query.factories.clause.NATIVE;
import iot.jcypher.query.factories.clause.ON_CREATE;
import iot.jcypher.query.factories.clause.ON_MATCH;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
import iot.jcypher.query.factories.clause.WHEN;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.factories.clause.WITH;
import iot.jcypher.query.factories.xpression.C;
import iot.jcypher.query.factories.xpression.X;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcPath;
import iot.jcypher.query.values.JcPrimitive;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.QueriesPrintObserver;
import iot.jcypher.util.Util;
import test.domainquery.Population;
import util.TestDataReader;

@Ignore
public class TempTest extends AbstractTestSuite {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;
	
	@Test
	public void test_15_maurits() {
		IClause[] clauses;
		JcQuery q;
		JcQueryResult result;
		String cypher;
//		dbAccess.clearDatabase();
//		
//		// add some sample data
//		clauses = new IClause[]{
//				CREATE.node().label("ActiveIn")
//		};
//		q = new JcQuery();
//		q.setClauses(clauses);
//		cypher = print(clauses, Format.PRETTY_1);
//		result = dbAccess.execute(q);
//		assertFalse(result.hasErrors());
		
		// before literal lists:
//		JcNode activeIn = new JcNode("a");
//		JcCollection empty = new JcCollection("[]");
//		q = new JcQuery();
//		q.setClauses(new IClause[]{
//				MATCH.node(activeIn).label("ActiveIn"),
//				DO.SET(activeIn.property("ratings")).byExpression(JC.coalesce(activeIn.property("ratings"), empty).asCollection().add(3))
//			});
		
		// after literal lists:
		JcNode activeIn = new JcNode("a");
		JcCollection empty = new JcCollection(new ArrayList<>());
		q = new JcQuery();
		q.setClauses(new IClause[]{
				MATCH.node(activeIn).label("ActiveIn"),
				DO.SET(activeIn.property("ratings")).byExpression(JC.coalesce(activeIn.property("ratings"), empty).asCollection().add(3))
			});
		cypher = print(q, Format.PRETTY_1);
	
		result = dbAccess.execute(q);
		assertFalse(result.hasErrors());
		
		return;
	}
	
	@Test
	public void test_14() {
		IClause[] clauses;
		JcQuery q;
		JcQueryResult result;
		String cypher;
		dbAccess.clearDatabase();
		
		// add some sample data
		clauses = new IClause[]{
				CREATE.node().label("Chapter").property("chapter").value("Chapter 1")
					.relation().out().type("hasTexts")
					.node().label("Text").property("Text").value("Text 1")
					.relation().out().type("hasAudio")
					.node().label("Audio").property("Audio").value("Audio 1"),
				CREATE.node().label("Chapter").property("chapter").value("Chapter 2")
					.relation().out().type("hasTexts")
					.node().label("Text").property("Text").value("Text 2")
					.relation().out().type("hasAudio")
					.node().label("Audio").property("Audio").value("Audio 2"),
				CREATE.node().label("Chapter").property("chapter").value("Chapter 3")
					.relation().out().type("hasTexts")
					.node().label("Text").property("Text").value("Text 3"),
		};
		q = new JcQuery();
		q.setClauses(clauses);
		cypher = print(clauses, Format.PRETTY_1);
		result = dbAccess.execute(q);
		
		// two queries in a single request
		JcPath p1 = new JcPath("p1");
		JcPath p2 = new JcPath("p2");
		JcNode n1 = new JcNode("n1");
		q = new JcQuery();
		q.setClauses(new IClause[]{
				MATCH.path(p1).node().label("Chapter")
					.relation().type("hasTexts").out()
					.node().label("Text")
					.relation().type("hasAudio").out()
					.node().label("Audio"),
				RETURN.value(p1)
			});
		JcQuery q2 = new JcQuery();
		q2.setClauses(new IClause[]{
				MATCH.path(p2).node().label("Chapter")
					.relation().type("hasTexts").out()
					.node(n1).label("Text"),
				// make sure only paths with no attached Audio nodes are returned
				WHERE.NOT().existsPattern(X.node(n1).relation().type("hasAudio").out().node().label("Audio")),
				RETURN.value(p2)
			});
		List<JcQuery> queries = new ArrayList<JcQuery>();
		queries.add(q);
		queries.add(q2);
		cypher = print(q, Format.PRETTY_1);
		String cypher2 = print(q2, Format.PRETTY_1);
		List<JcQueryResult> results = dbAccess.execute(queries);
		
		List<GrPath> p1res = results.get(0).resultOf(p1);
		int idx = 0;
		System.out.println("P1---------------------------");
		for (GrPath p : p1res) {
			System.out.println("Length: " + p.getLength());
			System.out.println("Startnode: " + p.getStartNode().getProperty("chapter").getValue().toString());
			idx++;
		}
		List<GrPath> p2res = results.get(1).resultOf(p2);
		idx = 0;
		System.out.println("P2---------------------------");
		for (GrPath p : p2res) {
			System.out.println("Length: " + p.getLength());
			System.out.println("Startnode: " + p.getStartNode().getProperty("chapter").getValue().toString());
			idx++;
		}
		
		return;
	}
	
	@Test
	public void test_13() {
		IClause[] clauses;
		JcQuery q;
		JcQueryResult result;
		String cypher;
//		dbAccess.clearDatabase();
		// add some sample data
		JcNode t1 = new JcNode("t1");
		JcNode t2 = new JcNode("t2");
		JcNode t3 = new JcNode("t3");
		JcNode a1 = new JcNode("a1");
		JcNode a2 = new JcNode("a2");
		JcNode a3 = new JcNode("a3");
//		clauses = new IClause[]{
//				CREATE.node(t1).label("Text"),
//				CREATE.node(t2).label("Text"),
//				CREATE.node(t3).label("Text"),
//				CREATE.node(a1).label("Audio"),
//				CREATE.node(a2).label("Audio"),
//				CREATE.node(a3).label("Audio"),
//		};
		
//		clauses = new IClause[]{
//				CREATE.node().label("Text").property("text").value("Text 1")
//					.relation().out().type("hasAudio")
//					.node().label("Audio").property("Audio").value("Audio 1")
//					.relation().out().type("interpretedBy")
//					.node(t1).label("Artist").property("name").value("John Doe"),
//				CREATE.node().label("Text").property("text").value("Text 2")
//					.relation().out().type("hasAudio")
//					.node().label("Audio").property("Audio").value("Audio 2")
//					.relation().out().type("interpretedBy")
//					.node(t2).label("Artist").property("name").value("Patty Smith"),
//				CREATE.node(t1).relation().out().type("knows").node(t2)
//		};
//		q = new JcQuery();
//		q.setClauses(clauses);
//		cypher = print(clauses, Format.PRETTY_1);
//		result = dbAccess.execute(q);
		
		JcNode n = new JcNode("n");
		JcNode a = new JcNode("a");
		JcRelation r = new JcRelation("r");
//		clauses = new IClause[]{
//				MATCH.node(n).label("Text"),
//				OPTIONAL_MATCH.node(n).relation(r).type("hasAudio").node(a).label("Audio"),
//				RETURN.value(n),
//				RETURN.value(r),
//				RETURN.value(a)
//		};
		JcPath p = new JcPath("p");
		clauses = new IClause[]{
				MATCH.node(n).label("Text").property("text").value("Text 1"),
				MATCH.path(p).node(n).relation(r).hopsUnbound().node(a),
				RETURN.value(p),
				RETURN.value(n),
				RETURN.value(r),
				RETURN.value(a)
		};
		q = new JcQuery();
		q.setClauses(clauses);
		cypher = print(clauses, Format.PRETTY_1);
		result = dbAccess.execute(q);
		
		List<GrPath> pres = result.resultOf(p);
		List<GrNode> nres = result.resultOf(n);
		List<GrRelation> rres = result.resultOf(r);
		List<GrNode> ares = result.resultOf(a);
		
		int[] sizes = new int[]{pres.size(), nres.size(), rres.size(), ares.size()};
		int[] pathLengths = new int[pres.size()];
		int idx = 0;
		for (GrPath pth : pres) {
			pathLengths[idx] = pth.getRelations().size();
			idx++;
		}
		
		List<GrRelation> rels = pres.get(pres.size() - 1).getRelations();
		
		return;
	}
	
	@Test
	public void test_12() {

		JcNode user = new JcNode("u");
		JcNode team = new JcNode("t");
		JcRelation r = new JcRelation("r");
		JcNode score = new JcNode("Score");
		JcNode partupId = new JcNode("partupId");
		JcNumber num_0 = new JcNumber(0);
		IClause[] clauses = new IClause[] {
				MATCH.node(user).label("User").relation(r).type("RECOMMEND").node(team).label("Team"),
				WHERE.valueOf(user.property("_id")).EQUALS("..."),
				WITH.value(JC.coalesce(r.property("nearbyTeams"), num_0).asNumber().plus(
						JC.coalesce(r.property("nearbyTeamsinNetworks"), num_0).asNumber().plus(
								JC.coalesce(r.property("daysActive"), num_0).asNumber().plus(
										JC.coalesce(r.property("parnersLimit"), num_0).asNumber().plus(
												JC.coalesce(r.property("sameCity"), num_0).asNumber().plus(
														JC.coalesce(r.property("sameCountry"), num_0).asNumber().plus(
																JC.coalesce(r.property("sameLanguage"), num_0).asNumber().plus(
																		JC.coalesce(r.property("sameTags"), num_0).asNumber())))))))).AS(score),
				WITH.value(team.property("_id")).AS(partupId),
				RETURN.value(score),
				RETURN.value(partupId)
		};
		String result = print(clauses, Format.PRETTY_1);
		
		return;
	}
	
	@Test
	public void test_11() {
		// the query !
		JcRelation activeIn = new JcRelation("activeIn");
		JcQuery q = new JcQuery();
		
		// that will be possible with 3.4.0-M01
		q.setClauses(new IClause[]{
				// match activeIn first
				DO.SET(activeIn.property("ratings")).byExpression(activeIn.collectionProperty("ratings").add(1))
		});
		
		// show the created CYPHER statements
		String str = Util.toCypher(q, Format.PRETTY_1);
		// show the created JSON
		String json = Util.toJSON(q, Format.PRETTY_1);
		
		// that's how you need to do it today
		q.setClauses(new IClause[]{
				// match activeIn first
				NATIVE.cypher("SET activeIn.ratings = activeIn.ratings + 1")
		});
		
		// show the created CYPHER statements
		String str_1 = Util.toCypher(q, Format.PRETTY_1);
		// show the created JSON
		String json_1 = Util.toJSON(q, Format.PRETTY_1);
		
		return;
	}
	
	@Test
	public void test_10() {
		// clear the database
		dbAccess.clearDatabase();
		
		// add some sample data
		IClause[] clauses = new IClause[]{
				CREATE.node().label("Member")
		};
		JcQuery q = new JcQuery();
		q.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(q);
		
		// the query !
		JcNode n = new JcNode("n");
		JcValue x = new JcValue("x");
		List<Integer> ratings = new ArrayList<Integer>();
		ratings.add(1);
		ratings.add(2);
		
		q = new JcQuery();
		q.setClauses(new IClause[]{
				MATCH.node(n).label("Member"),
				
				// DO().SET(...) is called exactly once when the comments property exists
				// (the case expression returns an array with one element)
				FOR_EACH.element(x).IN(C.CREATE(new IClause[]{
						CASE.result(),
						WHEN.valueOf(n.property("ratings")).IS_NULL(),
							NATIVE.cypher("[]"),
						ELSE.perform(),
							NATIVE.cypher("[1]"),
						END.caseXpr()
				})).DO().SET(n.property("ratings")).byExpression(
						n.collectionProperty("ratings").addAll(ratings)),
				
				// DO().SET(...) is called exactly once when the comments property does not exist
				// (the case expression returns an array with one element)
				FOR_EACH.element(x).IN(C.CREATE(new IClause[]{
						CASE.result(),
						WHEN.valueOf(n.property("ratings")).IS_NULL(),
							NATIVE.cypher("[1]"),
						ELSE.perform(),
							NATIVE.cypher("[]"),
						END.caseXpr()
				})).DO().SET(n.property("ratings")).to(new ArrayList<>()),
		});
		
		String str = Util.toCypher(q, Format.PRETTY_1);
		
		// the ratings property is initialized to []
		result = dbAccess.execute(q);
		
		// an element is added to the ratings property
		result = dbAccess.execute(q);
		
		// an element is added to the ratings property
		result = dbAccess.execute(q);
		
		return;
	}
	
	@Test
	public void test_09() {
		// clear the database
		dbAccess.clearDatabase();
		
		// add some sample data
		IClause[] clauses = new IClause[]{
				CREATE.node().label("Member")
		};
		JcQuery q = new JcQuery();
		q.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(q);
		
		// the query !
		JcNode n = new JcNode("n");
		JcValue x = new JcValue("x");
		
		q = new JcQuery();
		q.setClauses(new IClause[]{
				MATCH.node(n).label("Member"),
				
				// DO().SET(...) is called exactly once when the comments property exists
				// (the case expression returns an array with one element)
				FOR_EACH.element(x).IN(C.CREATE(new IClause[]{
						CASE.result(),
						WHEN.valueOf(n.property("comments")).GTE(0),
							NATIVE.cypher("[1]"),
						ELSE.perform(),
							NATIVE.cypher("[]"),
						END.caseXpr()
				})).DO().SET(n.property("comments")).byExpression(
						n.numberProperty("comments").plus(1)),
				
				// DO().SET(...) is called exactly once when the comments property does not exist
				// (the case expression returns an array with one element)
				FOR_EACH.element(x).IN(C.CREATE(new IClause[]{
						CASE.result(),
						WHEN.valueOf(n.property("comments")).GTE(0),
							NATIVE.cypher("[]"),
						ELSE.perform(),
							NATIVE.cypher("[1]"),
						END.caseXpr()
				})).DO().SET(n.property("comments")).to(0),
		});
		
		// the comments property is initialized to 0
		result = dbAccess.execute(q);
		
		// the comments property is incremented
		result = dbAccess.execute(q);
		
		return;
	}
	
	@Test
	public void test_08() {
		List<JcQueryResult> results;
		
		IClause[] clauses = new IClause[]{
				CREATE.node().label("Member")
		};
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(query);
		
		JcNode n = new JcNode("n");
		
		List<JcQuery> queries = new ArrayList<JcQuery>();
		JcQuery q = new JcQuery();
		q.setClauses(clauses = new IClause[]{
				MATCH.node(n).label("Member"),
				WHERE.has(n.property("comments")),
				DO.SET(n.property("comments")).byExpression(
						n.numberProperty("comments").plus(1)),
		});
		queries.add(q);
		q = new JcQuery();
		q.setClauses(new IClause[]{
				MATCH.node(n).label("Member"),
				WHERE.NOT().has(n.property("comments")),
				DO.SET(n.property("comments")).to(0),
		});
		queries.add(q);
		results = dbAccess.execute(queries);
		
		results = dbAccess.execute(queries);
		
		return;
	}
	
	@Test
	public void test_07() {
		IClause[] clauses;
		JcQuery query;
		JcQueryResult result;
		
		// to start the sample with an empty database
//		dbAccess.clearDatabase();
		
		// now add some sample data
//		clauses = new IClause[]{
//				CREATE.node().label("Movie").property("name").value("movie_1")
//					.property("actorCount").value(5),
//				CREATE.node().label("Movie").property("name").value("movie_2")
//					.property("actorCount").value(6),
//				CREATE.node().label("Movie").property("name").value("movie_3")
//					.property("actorCount").value(7),
//		};
//		
//		query = new JcQuery();
//		query.setClauses(clauses);
//		result = dbAccess.execute(query);
		
		// you can look into the database using the Neo4j browser
		
		// now perform a query, looking for nodes
		// with more than one outgoing relationship (degree > 1)
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
		List<Map<JcPrimitive, Object>> maps = resultAsMap(result, name, extraInfo, rank);
		
		// access values in a map
		Map<JcPrimitive, Object> map = maps.get(0);
		Object v_name = map.get(name);
		Object v_extraInfo = map.get(extraInfo);
		Object v_rank = map.get(rank);

		return;
	}
	
	private List<Map<JcPrimitive, Object>> resultAsMap(JcQueryResult result, JcPrimitive... key) {
		List<List<?>> results = new ArrayList<List<?>>();
		List<Map<JcPrimitive, Object>> ret = new ArrayList<Map<JcPrimitive,Object>>();
		for (JcPrimitive k : key) {
			List<?> r = result.resultOf(k);
			results.add(r);
			for (int i = 0; i < r.size(); i++) {
				Map<JcPrimitive, Object> map;
				if (i > ret.size() - 1) {
					map = new HashMap<JcPrimitive, Object>();
					ret.add(map);
				} else
					map = ret.get(i);
				map.put(k, r.get(i));
			}
		}
		return ret;
	}
	
	@Test
	public void test_06() {
		IClause[] clauses;
		JcQuery query;
		JcQueryResult result;
		
		// to start the sample with an empty database
		dbAccess.clearDatabase();
		
		// now add some sample data
		JcNode mem_1 = new JcNode("mem_1");
		JcNode mem_2 = new JcNode("mem_2");
		JcNode s1 = new JcNode("s1");
		JcNode s2 = new JcNode("s2");
		JcNode s3 = new JcNode("s3");
		clauses = new IClause[]{
				CREATE.node(mem_1).label("Member").property("name").value("John"),
				CREATE.node(mem_2).label("Member").property("name").value("Henry"),
				CREATE.node(s1).label("Song").property("name").value("Song_1"),
				CREATE.node(s2).label("Song").property("name").value("Song_2"),
				CREATE.node(s3).label("Song").property("name").value("Song_3"),
				CREATE.node(mem_1).relation().out().type("PLAYED").node(s1),
				CREATE.node(mem_1).relation().out().type("PLAYED").node(s2),
				CREATE.node(mem_2).relation().out().type("PLAYED").node(s3)
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		result = dbAccess.execute(query);
		
		// you can look into the database using the Neo4j browser
		
		// now perform a query, looking for nodes
		// with more than one outgoing relationship (degree > 1)
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		JcNumber degree = new JcNumber("degree");
		clauses = new IClause[]{
				MATCH.node(n).relation(r).out().node(),
				WITH.count().value(r).AS(degree),
				WITH.value(n),
				WHERE.valueOf(degree).GT(1),
				RETURN.value(n),
				RETURN.value(degree)
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		
		// you can look at the CYPHER query which is generated in the background
		String str = Util.toCypher(query, Format.PRETTY_1);
		System.out.println(str);
		
		result = dbAccess.execute(query);
		List<GrNode> nResult = result.resultOf(n);
		List<BigDecimal> degreeResult = result.resultOf(degree);

		return;
	}
	
	@Test
	public void test_05() {
		IClause[] clauses;
		JcQuery query;
		JcQueryResult result;
		
		// to start the sample with an empty database
		dbAccess.clearDatabase();
		
		// now add some data
		clauses = new IClause[]{
				CREATE.node().label("Member").property("name").value("John"),
				CREATE.node().label("Song").property("name").value("Song_1")
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		result = dbAccess.execute(query);
		
		// you can look into the database using the Neo4j browser
		
		// now optionally create a 'PLAYED' relationship
		JcNode n = new JcNode("n");
		JcNode s = new JcNode("s");
		JcRelation r = new JcRelation("r");
		clauses = new IClause[]{
				// match a pattern (in this sample for 'John' and 'Song_1')
				MATCH.node(n).label("Member").property("name").value("John"),
				MATCH.node(s).label("Song").property("name").value("Song_1"),
				
				// create a 'PLAYED' relationship only if it does not already exist
				MERGE.node(n).relation(r).out().type("PLAYED")
					.node(s),
					
				// initialize the 'views' property to 1
				ON_CREATE.SET(r.property("views")).to(1),
				
				// increment the 'views' property
				ON_MATCH.SET(r.property("views")).byExpression(
						r.numberProperty("views").plus(1))
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		result = dbAccess.execute(query);

		// you can look into the database using the Neo4j browser
		
		// you can vary the code by e.g. not clearing the database
		// and not inserting new sample data

		return;
	}
	
	@Test
	public void test_04() {
		List<JcError> errors;
		
		// to start the sample with an empty database
		dbAccess.clearDatabase();
		
		// now add some data
		IClause[] clauses;
		clauses = new IClause[]{
				CREATE.node().label("Member").property("name").value("John"),
				CREATE.node().label("Song").property("name").value("Song_1")
		};
		
		JcQuery query;
		query = new JcQuery();
		query.setClauses(clauses);
		JcQueryResult result;
		result = dbAccess.execute(query);
		
		// you can look into the database using the Neo4j browser
		
		// now optionally create a 'PLAYED' relationship
		JcNode n = new JcNode("n");
		JcNode s = new JcNode("s");
		JcRelation r = new JcRelation("r");
		clauses = new IClause[]{
				// match a pattern (in this sample for 'John' and 'Song_1')
				MATCH.node(n).label("Member").property("name").value("John"),
				MATCH.node(s).label("Song").property("name").value("Song_1"),
				
				// create a 'PLAYED' relationship only if it does not already exist
				// note: if newly created, the 'PLAYED' relationship has no 'views' property
				CREATE_UNIQUE.node(n).relation().out().type("PLAYED")
					.node(s),
		};
		
		List<JcQuery> queries = new ArrayList<JcQuery>();
		query = new JcQuery();
		query.setClauses(clauses);
		queries.add(query);
		
		// increment the 'views' property if it exists
		clauses = new IClause[]{
				// match the 'PLAYED' relationship only if it has a 'views property'
				MATCH.node().label("Member").property("name").value("John")
					.relation(r).out().type("PLAYED")
					.node().label("Song").property("name").value("Song_1"),
				WHERE.has(r.property("views")),
				
				// increment the 'views' property
				DO.SET(r.property("views")).byExpression(
						r.numberProperty("views").plus(1)),
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		queries.add(query);
		
		// initialize the 'views' property if it does not exist
		clauses = new IClause[]{
				// match the 'PLAYED' relationship only if it does not a 'views property'
				MATCH.node().label("Member").property("name").value("John")
					.relation(r).out().type("PLAYED")
					.node().label("Song").property("name").value("Song_1"),
				WHERE.NOT().has(r.property("views")),
				
				// initialize the 'views' property to 1
				DO.SET(r.property("views")).to(1),
		};
		
		query = new JcQuery();
		query.setClauses(clauses);
		queries.add(query);
		
		// execute all three query parts in a single request to the database
		// (note: a single database request with a single JSON body
		// containing three statements is executed, this is a feature provided by Neo4j)
		List<JcQueryResult> results = dbAccess.execute(queries);
		
		// you can look into the database using the Neo4j browser
		
		// you can vary the code by e.g. not clearing the database
		// and not inserting new sample data

		return;
	}
	
	@Test
	public void test_03() {
		List<JcError> errors;
		
		// to start the sample with an empty database
		dbAccess.clearDatabase();
		
		// now add some data
		IClause[] clauses = new IClause[]{
				CREATE.node().label("Member").property("name").value("John"),
				CREATE.node().label("Song").property("name").value("Song_1")
		};
		
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(query);
		
		// you can look into the database using the Neo4j browser
		
		// now increment the 'views' property of the 'PLAYED' relationship
		// starting at 'Member' 'John'.
		JcNode n = new JcNode("n");
		JcNode s = new JcNode("s");
		JcRelation r = new JcRelation("r");
		clauses = new IClause[]{
				// match a pattern (in this sample for 'John')
				MATCH.node(n).label("Member").property("name").value("John")
					.relation(r).out().type("PLAYED")
					.node(s).label("Song").property("name").value("Song_1"),
				// modify the 'views' property
				DO.SET(r.property("views")).byExpression(r.numberProperty("views").plus(1)),
				RETURN.value(r)
		};
		String str = print(clauses, Format.PRETTY_1);
		
		// you can look into the database using the Neo4j browser
		
		query = new JcQuery();
		query.setClauses(clauses);
		result = dbAccess.execute(query);
		List<GrRelation> played = result.resultOf(r);
		if (played.isEmpty()) {
			clauses = new IClause[]{
					MATCH.node(n).label("Member").property("name").value("John"),
					MATCH.node(s).label("Song").property("name").value("Song_1"),
					CREATE.node(n).relation(r).out().type("PLAYED").property("views").value(0)
							.node(s)
			};
			query = new JcQuery();
			query.setClauses(clauses);
			result = dbAccess.execute(query);
		}
				
		return;
	}
	
	@Test
	public void test_02() {
		List<JcError> errors;
		
		// to start the sample with an empty database
		dbAccess.clearDatabase();
		
		// now add some data
		IClause[] clauses = new IClause[]{
				CREATE.node().label("Member").property("name").value("John")
					.relation().out().type("PLAYED").property("views").value(124)
					.node().label("Song"),
				CREATE.node().label("Member").property("name").value("Bill")
					.relation().out().type("PLAYED").property("views").value(20)
					.node().label("Song"),
				CREATE.node().label("Member").property("name").value("Cindy")
					.relation().out().type("PLAYED").property("views").value(320)
					.node().label("Song")
		};
		
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(query);
		
		// you can look into the database using the Neo4j browser
		
		// now increment the 'views' property of the 'PLAYED' relationship
		// starting at 'Member' 'John'.
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		clauses = new IClause[]{
				// match a pattern (in this sample for 'John')
				MATCH.node(n).label("Member").relation(r).out().type("PLAYED")
					.node().label("Song"),
				WHERE.valueOf(n.property("name")).EQUALS("John"),
				// modify the 'views' property
				DO.SET(r.property("views")).byExpression(r.numberProperty("views").plus(1))
		};
		
		// you can look into the database using the Neo4j browser
		
		query = new JcQuery();
		query.setClauses(clauses);
		result = dbAccess.execute(query);
				
		return;
	}
	
	@Test
	public void test_01() {
		List<JcError> errors;
//		errors = dbAccess.clearDatabase();
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
		
		IClause[] clauses = new IClause[]{
				CREATE.node().label("Member").relation().out().type("PLAYED").property("views").value("124")
					.node().label("Song")
		};
		
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		JcQueryResult result;
//		result = dbAccess.execute(query);
//		if (result.hasErrors()) {
//			List<JcError> errs = new ArrayList<JcError>();
//			errs.addAll(result.getDBErrors());
//			errs.addAll(result.getGeneralErrors());
//			printErrors(errs);
//			throw new JcResultException(errors);
//		}
		
		JcPath p = new JcPath("p");
		JcRelation x = new JcRelation("x");
		clauses = new IClause[]{
				MATCH.path(p).node().label("Member").relation().out().type("PLAYED")
					.node().label("Song"),
				FOR_EACH.element(x).IN_relations(p).DO().SET(x.property("views")).byExpression(
						x.stringProperty("views").concat("5"))
		};
		String str = print(clauses, Format.PRETTY_1);
		
		query = new JcQuery();
		query.setClauses(clauses);
		result = dbAccess.execute(query);
		if (result.hasErrors()) {
			List<JcError> errs = new ArrayList<JcError>();
			errs.addAll(result.getDBErrors());
			errs.addAll(result.getGeneralErrors());
			printErrors(errs);
			throw new JcResultException(errs);
		}
				
		return;
	}
	
	@BeforeClass
	public static void before() {
		Settings.TEST_MODE = true;
		domainName = "QTEST-DOMAIN";
		Properties props = new Properties();
		
		// properties for remote access and for embedded access
		// (not needed for in memory access)
		//props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.SERVER_ROOT_URI, "bolt://localhost:7687");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/02");
		
		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
//		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props, "neo4j", "jcypher");
		
		// init db
		Population population = new Population();
		storedDomainObjects = population.createPopulation();
		
//		List<JcError> errors = dbAccess.clearDatabase();
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
//		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
//		errors = da.store(storedDomainObjects);
//		if (errors.size() > 0) {
//			printErrors(errors);
//			throw new JcResultException(errors);
//		}
	}
	
	@AfterClass
	public static void after() {
		TestDataReader tdr = new TestDataReader("/test/queryrecorder/Test_QueryRecorder_01.txt");
		String testId = "UNION_06";
		//assertEquals(testId, qCypher, tdr.getTestData(testId));
		
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
		Settings.TEST_MODE = false;
	}
}
