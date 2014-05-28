package test;

import iot.jcypher.JcQuery;
import iot.jcypher.api.IClause;
import iot.jcypher.factories.clause.CREATE;
import iot.jcypher.factories.clause.MATCH;
import iot.jcypher.factories.clause.RETURN;
import iot.jcypher.factories.clause.START;
import iot.jcypher.factories.clause.WHERE;
import iot.jcypher.values.JcNode;
import iot.jcypher.values.JcRelation;
import iot.jcypher.writer.Format;

import org.junit.Test;

import util.TestDataReader;

public class JsonTest extends AbstractTestSuite {

	@Test
	public void testExtractParams_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/json/Test_EXTRACT_PARAMS_01.txt");
		
		JcQuery query;
		JcNode n = new JcNode("n");
		JcNode a = new JcNode("a");
		JcRelation r = new JcRelation("r");
		JcNode x = new JcNode("x");
		JcNode friend = new JcNode("friend");
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				 CREATE.node(n).label("Person").
					property("name").value("Andres").
					property("title").value("Developer")
		});

		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				CREATE.node(n).label("Person").
					property("name").value("Andres").
					property("title").value(a.property("title"))
		});

		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				 CREATE.node(n).label("Person").
					property("name").value("Andres").
					property("age").value(21).
					property("amount").value(320.53)
		});

		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				 CREATE.node(n).label("Person").
					property("name").value("Andres").
					property("age").value(21).
					property("amount").value(a.property("amount"))
		});

		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				 CREATE.node(n).label("Person")
					.property("name").value("Andres")
					.property("age").value(21)
					.property("amount").value(a.property("amount"))
					.relation(r).type("LINK").out()
					.property("marked").value(true)
					.property("name").value("connection")
					.node(a)
					.property("city").value("San Francisco")
		});

		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				 MATCH.node(x).property("name").value("I").relation(r).node(friend),
				 WHERE.valueOf(friend.property("name")).EQUALS("you"),
				 RETURN.value(r.type())
		});

		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_06";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				START.node(n).byIndex("Persons").property("name").value("Tobias")
		});
		
		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_07";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				START.node(n).byIndex("Persons").query("name:A")
		});
		
		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_08";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				START.node(n).byId(0)
		});
		
		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_09";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				START.relation(r).byId(2, 3)
		});
		
		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_10";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testJson_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/json/Test_JSON_01.txt");
		
		JcNode shakespeare = new JcNode("shakespeare");
		JcNode juliusCaesar = new JcNode("juliusCaesar");
		
		/*******************************/
		JcQuery query = new JcQuery();
		query.setExtractParams(false);
		query.setClauses(new IClause[] {
				CREATE.node(shakespeare).property("firstname").value("William").property("lastname").value("Shakespeare"),
				CREATE.node(juliusCaesar).property("title").value("Julius Caesar")
		});
		
		result = printJSON(query, Format.PRETTY_3);
		testId = "JSON_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				CREATE.node(shakespeare).property("firstname").value("William").property("lastname").value("Shakespeare"),
				CREATE.node(juliusCaesar).property("title").value("Julius Caesar")
		});
		
		result = printJSON(query, Format.PRETTY_3);
		testId = "JSON_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		return;
	}
}
