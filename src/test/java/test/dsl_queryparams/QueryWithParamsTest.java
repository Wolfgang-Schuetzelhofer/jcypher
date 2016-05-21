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

package test.dsl_queryparams;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryParameter;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.JC;
import iot.jcypher.query.factories.clause.CASE;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.CREATE_INDEX;
import iot.jcypher.query.factories.clause.CREATE_UNIQUE;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.DROP_INDEX;
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
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.factories.clause.UNION;
import iot.jcypher.query.factories.clause.USING;
import iot.jcypher.query.factories.clause.WHEN;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.factories.clause.WITH;
import iot.jcypher.query.factories.xpression.C;
import iot.jcypher.query.factories.xpression.F;
import iot.jcypher.query.factories.xpression.I;
import iot.jcypher.query.factories.xpression.P;
import iot.jcypher.query.factories.xpression.X;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcPath;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.writer.CypherWriter;
import iot.jcypher.query.writer.Format;
import iot.jcypher.query.writer.JSONWriter;
import iot.jcypher.query.writer.PreparedQuery;
import iot.jcypher.query.writer.QueryParam;
import iot.jcypher.query.writer.WriterContext;
import iot.jcypher.util.Util;
import test.AbstractTestSuite;
import util.TestDataReader;

//@Ignore
public class QueryWithParamsTest extends AbstractTestSuite {
	
	@Test
	public void testQueryParams_01() {
		String cypher;
		String json;
		String testId;
		setDoPrint(true);
		setDoAssert(true);
		
		TestDataReader tdr = new TestDataReader("/test/Test_MERGE_01.txt");
		
		/*******************************/
		
		JcQueryParameter memberName = new JcQueryParameter("memberName");
		memberName.setValue("John");
		JcQueryParameter songName = new JcQueryParameter("songName");
		songName.setValue("Song_1");
		
		JcNode n = new JcNode("n");
		JcNode s = new JcNode("s");
		JcRelation r = new JcRelation("r");
		IClause[] clauses = new IClause[] {
				// match nodes (in this sample for 'John' and 'Song_1')
				MATCH.node(n).label("Member").property("name").value(memberName),
				MATCH.node(s).label("Song").property("name").value(songName),
				// match (or create if not exists) a relation
				MERGE.node(n).relation(r).out().type("PLAYED").node(s),
				
				// initialize the 'views' property to 1
				ON_CREATE.SET(r.property("views")).to(1),
				ON_CREATE.SET(r.property("created")).byExpression(JC.timeStamp()),
				
				// increment the 'views' property
				ON_MATCH.SET(r.property("views")).byExpression(
						r.numberProperty("views").plus(1)),
		};
		
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		JcQuery query_1 = new JcQuery();
		query_1.setClauses(clauses);
		List<JcQuery> queries = new ArrayList<JcQuery>();
		queries.add(query);
		queries.add(query_1);
		
		cypher = Util.toCypher(query, Format.PRETTY_1);
		json = Util.toJSON(queries, Format.PRETTY_1);
		PreparedQuery pq = Util.toPreparedQuery(query, Format.PRETTY_1);
		memberName.setValue("Angelina");
		String json_1 = Util.toJSON(pq);
		
		System.out.println(cypher);
		System.out.println(json);
//		testId = "MERGE_01";
//		assertQuery(testId, result, tdr.getTestData(testId));
		
		return;
	}
}
