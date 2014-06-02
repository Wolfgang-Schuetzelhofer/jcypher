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

package iot.jcypher.samples;

import iot.jcypher.CypherWriter;
import iot.jcypher.JSONWriter;
import iot.jcypher.JcQuery;
import iot.jcypher.api.IClause;
import iot.jcypher.factories.clause.CREATE;
import iot.jcypher.factories.clause.MATCH;
import iot.jcypher.factories.clause.RETURN;
import iot.jcypher.factories.clause.START;
import iot.jcypher.factories.clause.WHERE;
import iot.jcypher.values.JcNode;
import iot.jcypher.values.JcRelation;
import iot.jcypher.values.JcString;
import iot.jcypher.writer.Format;
import iot.jcypher.writer.WriterContext;

/**
 * This JCypher sample is constructing and querying the 'Shakespeare Graph',
 * which is provided as a sample in the book 'Graph Databases' by Ian Robinson, Jim Webber, and Emil Eifrem
 */
public class ShakespeareGraph {

	public static void main(String[] args) {
		
		JcQuery query = new JcQuery();
		
		JcNode shakespeare = new JcNode("shakespeare");
		JcNode juliusCaesar = new JcNode("juliusCaesar");
		JcNode theTempest = new JcNode("theTempest");
		JcNode rsc = new JcNode("rsc");
		JcNode production1 = new JcNode("production1");
		JcNode production2 = new JcNode("production2");
		JcNode performance1 = new JcNode("performance1");
		JcNode performance2 = new JcNode("performance2");
		JcNode performance3 = new JcNode("performance3");
		JcNode billy = new JcNode("billy");
		JcNode review = new JcNode("review");
		JcNode theatreRoyal = new JcNode("theatreRoyal");
		JcNode greyStreet = new JcNode("greyStreet");
		JcNode newcastle = new JcNode("newcastle");
		JcNode tyneAndWear = new JcNode("tyneAndWear");
		JcNode england = new JcNode("england");
		JcNode stratford = new JcNode("stratford");
		
		JcNode theater = new JcNode("theater");
		JcNode bard = new JcNode("bard");
		JcRelation w = new JcRelation("w");
		JcNode play = new JcNode("play");
		JcString playTitle = new JcString("playTitle");
		
		query.setClauses(new IClause[] {
				CREATE.node(shakespeare).property("firstname").value("William").property("lastname").value("Shakespeare"),
				CREATE.node(juliusCaesar).property("title").value("Julius Caesar"),
				CREATE.node(shakespeare).relation().out().type("WROTE_PLAY").property("year").value(1599).node(juliusCaesar),
				CREATE.node(theTempest).property("title").value("The Tempest"),
				CREATE.node(shakespeare).relation().out().type("WROTE_PLAY").property("year").value(1610).node(theTempest),
				
				CREATE.node(rsc).property("name").value("RSC"),
				
				CREATE.node(production1).property("name").value("Julius Caesar"),
				CREATE.node(rsc).relation().out().type("PRODUCED").node(production1),
				CREATE.node(production1).relation().out().type("PRODUCTION_OF").node(juliusCaesar),
				CREATE.node(performance1).property("date").value(20120729),
				CREATE.node(performance1).relation().out().type("PERFORMANCE_OF").node(production1),
				
				CREATE.node(production2).property("name").value("The Tempest"),
				CREATE.node(rsc).relation().out().type("PRODUCED").node(production2),
				CREATE.node(production2).relation().out().type("PRODUCTION_OF").node(theTempest),
				CREATE.node(performance2).property("date").value(20061121),
				CREATE.node(performance2).relation().out().type("PERFORMANCE_OF").node(production2),
				
				CREATE.node(performance3).property("date").value(20120730),
				CREATE.node(performance3).relation().out().type("PERFORMANCE_OF").node(production1),
				
				CREATE.node(billy).property("name").value("Billy"),
				CREATE.node(review).property("rating").value(5).property("review").value("This was awesome!"),
				CREATE.node(billy).relation().out().type("WROTE_REVIEW").node(review),
				CREATE.node(review).relation().out().type("RATED").node(performance1),
				
				CREATE.node(theatreRoyal).property("name").value("Theatre Royal"),
				CREATE.node(performance1).relation().out().type("VENUE").node(theatreRoyal),
				CREATE.node(performance2).relation().out().type("VENUE").node(theatreRoyal),
				CREATE.node(performance3).relation().out().type("VENUE").node(theatreRoyal),
				
				CREATE.node(greyStreet).property("name").value("Grey Street"),
				CREATE.node(theatreRoyal).relation().out().type("STREET").node(greyStreet),
				CREATE.node(newcastle).property("name").value("Newcastle"),
				CREATE.node(greyStreet).relation().out().type("CITY").node(newcastle),
				CREATE.node(tyneAndWear).property("name").value("Tyne and Wear"),
				CREATE.node(newcastle).relation().out().type("COUNTY").node(tyneAndWear),
				CREATE.node(england).property("name").value("England"),
				CREATE.node(tyneAndWear).relation().out().type("COUNTRY").node(england),
				CREATE.node(stratford).property("name").value("Stratford upon Avon"),
				CREATE.node(stratford).relation().out().type("COUNTRY").node(england),
				CREATE.node(rsc).relation().out().type("BASED_IN").node(stratford),
				CREATE.node(shakespeare).relation().out().type("BORN_IN").node(stratford),
		});
		
		// map to Cypher
		String cypher = toCypher(query, Format.PRETTY_3);
		System.out.println("CYPHER --------------------");
		System.out.println(cypher);
		
		// map to JSON
		String json = toJSON(query, Format.PRETTY_3);
		System.out.println("JSON   --------------------");
		System.out.println(json);
		
		query = new JcQuery();
		query.setClauses(new IClause[] {
				START.node(theater).byIndex("venue").property("name").value("Theatre Royal"),
				START.node(newcastle).byIndex("city").property("name").value("Newcastle"),
				START.node(bard).byIndex("author").property("lastname").value("Shakespeare"),
				MATCH.node(newcastle).relation().in().maxHops(2).type("STREET").type("CITY").node(theater)
							.relation().in().type("VENUE").node().relation().out().type("PERFORMANCE_OF")
							.node().relation().out().type("PRODUCTION_OF").node(play)
							.relation(w).in().type("WROTE_PLAY").node(bard),
				WHERE.valueOf(w.property("year")).GT(1608),
				RETURN.DISTINCT().value(play.property("title")).AS(playTitle)
		});
		
		// map to Cypher
		cypher = toCypher(query, Format.PRETTY_3);
		System.out.println("CYPHER --------------------");
		System.out.println(cypher);
		
		// map to JSON
		json = toJSON(query, Format.PRETTY_3);
		System.out.println("JSON   --------------------");
		System.out.println(json);
		return;
	}
	
	private static String toCypher(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		CypherWriter.toCypherExpression(query, context);
		return context.buffer.toString();
	}
	
	private static String toJSON(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		JSONWriter.toJSON(query, context);
		return context.buffer.toString();
	}
}
