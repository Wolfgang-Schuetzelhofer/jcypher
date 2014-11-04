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

import iot.jcypher.query.factories.JC;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcPath;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.MATH;
import iot.jcypher.query.values.ValueElement;
import iot.jcypher.query.writer.Format;

import org.junit.Test;

import util.TestDataReader;

//@Ignore
public class ValueTest extends AbstractTestSuite {

	@Test
	public void testString_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/value/Test_STRING_01.txt");
		
		JcNode n, a, b;
		JcRelation r;
		JcCollection x;
		ValueElement xpr;
		
		JcString str;
		JcNumber num;

		/*******************************/
		n = new JcNode("n");
		
		result = print(n, Format.PRETTY_1);
		testId = "STRING_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		n = new JcNode("n");
		xpr = n.property("name");
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		r = new JcRelation("r");
		xpr = r.property("livesAt");
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		b = new JcNode("b");
		xpr = a.stringProperty("name").concat("<->").concat(b.stringProperty("name"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		n = new JcNode("n");
		xpr = n.stringProperty("name").length();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		x = new JcCollection("x");
		xpr = x.length();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_06";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		b = new JcNode("b");
		xpr = a.stringProperty("name").concat("<->").concat(b.stringProperty("name")).length();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_07";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		b = new JcNode("b");
		xpr = a.stringProperty("name").trim().concat("<->")
				.concat(b.stringProperty("name"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_08";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		b = new JcNode("b");
		xpr = a.stringProperty("name").trim().concat("<->")
				.concat(b.stringProperty("name").trim()).length();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_09";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		b = new JcNode("b");
		xpr = a.stringProperty("name").concat("<->").trim()
				.concat(b.stringProperty("name").trim()).length();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_10";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		xpr = a.stringProperty("name").replace("get").with("set");
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_11";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		xpr = a.stringProperty("name").substring(2).concat("Hallo");
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_12";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		xpr = a.stringProperty("name").substring(2).subLength(5).concat("Hallo");
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_13";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		xpr = a.stringProperty("name").left(3);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_14";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		a = new JcNode("a");
		xpr = a.stringProperty("name").right(4);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_15";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		str = new JcString("str", "a string");
		xpr = str.trimLeft();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_16";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		str = new JcString("str", "a string");
		xpr = str.trimRight().length();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_17";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		n = new JcNode("n");
		xpr = n.stringProperty("name").lower();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_18";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		str = new JcString("str", "a string");
		xpr = str.upper();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_19";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		num = new JcNumber(320);
		xpr = num.div(10).str();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "STRING_20";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testNumber_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/value/Test_NUMBER_01.txt");
		
		JcNode n = new JcNode("n");
		JcNode a = new JcNode("a");
		JcNode b = new JcNode("b");
		JcString str = new JcString("str", "42");
		JcString str2 = new JcString("str2", "11.5");
		JcNumber num4 = new JcNumber("num4", 4);
		JcNumber num2 = new JcNumber("num2", 2);
		
		ValueElement xpr;

		/*******************************/
		xpr = n.numberProperty("amount").plus(30.5);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").plus(b.numberProperty("amount"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").minus(b.numberProperty("amount")).minus(20);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").mult(b.numberProperty("amount")).mult(2);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").div(b.numberProperty("amount")).div(2);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").div(b.numberProperty("amount")).enclose().mult(2).enclose()
				.plus(30);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_06";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = str.toInt();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_07";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = str2.toFloat();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_08";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").mod(3);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_09";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").mod(b.numberProperty("modVal"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_10";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").pow(3);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_11";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.numberProperty("amount").pow(b.numberProperty("exponent"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_12";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num4.pow(2);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_13";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num4.pow(num2);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "NUMBER_14";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testScalar_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/value/Test_SCALAR_01.txt");
		
		JcNode a = new JcNode("a");
		JcRelation r = new JcRelation("r");
		JcPath p = new JcPath("p");
		
		JcNode actor = new JcNode("actor");
		JcCollection coll = new JcCollection("coll");
		
		ValueElement xpr;

		/*******************************/
		xpr = r.type();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.id();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.collectionProperty("array").head();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.collectionProperty("array").last();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = JC.timeStamp();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = r.startNode();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_06";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = r.endNode();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_07";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = r.startNode().collectionProperty("array").length();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_08";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = JC.coalesce(a.property("hairColor"), a.property("eyes"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_09";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = JC.coalesce(actor.property("id"), coll.last());
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_10";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = JC.coalesce(a, p.nodes().last(), actor.property("id"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_11";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = JC.coalesce(p.nodes().last(), coll.last(), actor.property("id"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_12";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = JC.coalesce(p.nodes().last(), coll.last(), JC.coalesce(p.nodes().last(), coll.last()), a);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_13";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = p.length();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "SCALAR_14";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testCollections_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/value/Test_COLLECTION_01.txt");
		
		JcNode a = new JcNode("a");
		JcPath p = new JcPath("p");
		ValueElement xpr;

		/*******************************/
		xpr = p.nodes();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "COLLECTION_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = p.relations();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "COLLECTION_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = a.labels();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "COLLECTION_03";
		assertQuery(testId, result, tdr.getTestData(testId));
	}
	
	@Test
	public void testMathFunctions_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);

		TestDataReader tdr = new TestDataReader("/test/value/Test_MATH_FUNCTIONS_01.txt");
		
		ValueElement xpr;
		JcNode n = new JcNode("n");
		JcNode a = new JcNode("a");
		JcNode e = new JcNode("e");
		JcNumber num = new JcNumber("num", 0.5);
		JcNumber pi = new JcNumber("pi", 3.14159);
		JcNumber num2 = new JcNumber("num2", 2);
		JcNumber num27 = new JcNumber("num27", 27);
		JcNumber num180 = new JcNumber("num180", 180);
		JcNumber num256 = new JcNumber("num256", 256);

		/*******************************/
		xpr = a.numberProperty("age").minus(e.numberProperty("age")).math().abs();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().acos();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().asin();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().atan();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_04";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().cos();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_05";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = pi.math().degrees();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_06";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = MATH.e();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_07";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num2.math().exp();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_08";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = n.numberProperty("number").math().exp();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_09";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().floor();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_10";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().haversin();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_11";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num27.math().log();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_12";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num27.math().log10();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_13";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = MATH.pi();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_14";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num180.math().radians();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_15";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = MATH.rand();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_16";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = pi.math().round();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_17";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().sign();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_18";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().sin();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_19";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num256.math().sqrt();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_20";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = num.math().tan();
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_21";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = MATH.atan2_x(0.5).y(0.6);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_22";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = MATH.atan2_x(a.numberProperty("x_coord")).y(0.6);
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_23";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = MATH.atan2_x(a.numberProperty("x_coord")).y(a.numberProperty("y_coord"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_24";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		xpr = MATH.atan2_x(0.5).y(e.numberProperty("y_coord"));
		
		result = print(xpr, Format.PRETTY_1);
		testId = "MATH_25";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		return;
	}
}


