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

package test.querypersist;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;

import iot.jcypher.domainquery.internal.JSONConverter;
import iot.jcypher.domainquery.internal.JSONConverterAccess;
import iot.jcypher.domainquery.internal.RecordedQuery;
import iot.jcypher.domainquery.internal.RecordedQuery.Literal;
import iot.jcypher.domainquery.internal.RecordedQuery.Statement;
import test.AbstractTestSuite;
import util.TestDataReader;

public class ConversionUtilTest extends AbstractTestSuite {

	@Test
	public void testConversion_01() {
		String input;
		Statement statement;
		TestDataReader tdr = new TestDataReader("/test/querypersist/Test_CONVERSION_01.txt");
		
		/******************************************/
		input = tdr.getTestData("CONVERSION_01");
		statement = convert(input);
		assertEquals(Literal.class, statement.getClass());
		assertEquals(2, ((Literal)statement).getRawValue());
		
		/******************************************/
		input = tdr.getTestData("CONVERSION_02");
		statement = convert(input);
		List<String> stringList = new ArrayList<String>();
		stringList.add("String nr. 1");
		stringList.add("String nr. 2");
		assertEquals(Literal.class, statement.getClass());
		assertArrayEquals(stringList.toArray(), ((List<?>)((Literal)statement).getRawValue()).toArray());
		
		/******************************************/
		input = tdr.getTestData("CONVERSION_03");
		statement = convert(input);
		List<Object> primList = new ArrayList<>();
		primList.add("PrimHolder");
		primList.add((int)2);
		primList.add((short)3);
		primList.add((long)4);
		primList.add((float)1.25);
		primList.add((double)7.765E8);
		primList.add(true);
		assertEquals(Literal.class, statement.getClass());
		assertArrayEquals(primList.toArray(), ((List<?>)((Literal)statement).getRawValue()).toArray());
		//assertArrayEquals(primList, (List<?>)((Literal)statement).getRawValue());
		
		/******************************************/
		input = tdr.getTestData("CONVERSION_04");
		statement = convert(input);
		Object[] primArray = new Object[]{
				"PrimHolder",
				(int)2,
				(short)3,
				(long)4,
				(float)1.25,
				(double)7.765E8,
				true
			};
		assertEquals(Literal.class, statement.getClass());
		assertArrayEquals(primArray, (Object[])((Literal)statement).getRawValue());
		
		/******************************************/
		input = tdr.getTestData("CONVERSION_05");
		statement = convert(input);
		int[] intArray = new int[]{
				2,
				3,
				4
			};
		assertEquals(Literal.class, statement.getClass());
		assertArrayEquals(intArray, (int[])((Literal)statement).getRawValue());
		
		return;
	}
	
	private Statement convert(String input) {
		RecordedQuery rq = new RecordedQuery(false);
		StringReader sr = new StringReader(input);
		JsonReader reader = Json.createReader(sr);
		JsonObject jsonResult = reader.readObject();
		JSONConverter jc = new JSONConverter();
		List<Statement> statements = new ArrayList<Statement>();
		JSONConverterAccess.readStatement(jc, jsonResult, statements, rq);
		return statements.get(0);
	}
}
