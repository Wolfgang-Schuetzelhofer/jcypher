package test;

import iot.jcypher.JcQuery;
import iot.jcypher.api.IClause;
import iot.jcypher.api.pattern.Node;
import iot.jcypher.factories.clause.CREATE;
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

		TestDataReader tdr = new TestDataReader("/test/Test_CREATE_01.txt");
		
		JcQuery query;
		JcNode n = new JcNode("n");
		JcNode a = new JcNode("a");
		JcRelation r = new JcRelation("r");
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				 CREATE.node(n).label("Person").
					property("name").value("Andres").
					property("title").value("Developer")
		});

		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "CREATE_04";
		//assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		query = new JcQuery();
		query.setClauses(new IClause[] {
				CREATE.node(n).label("Person").
					property("name").value("Andres").
					property("title").value(a.property("title"))
		});

		//result = print(query, Format.PRETTY_1);
		result = printJSON(query, Format.PRETTY_1);
		testId = "CREATE_04";
		//assertQuery(testId, result, tdr.getTestData(testId));
		
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
		testId = "CREATE_04";
		//assertQuery(testId, result, tdr.getTestData(testId));
		
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
		testId = "CREATE_04";
		//assertQuery(testId, result, tdr.getTestData(testId));
		
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
		testId = "CREATE_04";
		//assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testJson_01() {
		String result;
		JcQuery query = new JcQuery();
		
		JcNode shakespeare = new JcNode("shakespeare");
		JcNode juliusCaesar = new JcNode("juliusCaesar");
		
		query.setClauses(new IClause[] {
				CREATE.node(shakespeare).property("firstname").value("William").property("lastname").value("Shakespeare"),
				CREATE.node(juliusCaesar).property("title").value("Julius Caesar")
		});
		
		result = print(query, Format.PRETTY_3);
		result = printJSON(query, Format.PRETTY_3);
		
		return;
	}
}
