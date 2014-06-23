package test;

import iot.jcypher.JcQuery;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.writer.Format;

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
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				RETURN.value(n).LIMIT(3).SKIP(1)
		});
		
		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_11";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				DO.SET(n.property("surname")).to("Taylor")
		});
		
		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_12";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				CREATE.node(n).label("Person")
					.property("numbers").value("A", "B", "C")
		});
		
		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "EXTRACT_13";
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
