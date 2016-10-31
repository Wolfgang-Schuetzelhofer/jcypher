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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import iot.jcypher.query.JcQuery;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.JC;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.SEPARATE;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.ValueElement;
import iot.jcypher.query.writer.Format;
import util.TestDataReader;

public class CollectionAsValueTest extends AbstractTestSuite {

	@Test
	public void testAsValue_01() {
		String result;
		String testId;
		setDoPrint(true);
		setDoAssert(true);
		
		TestDataReader tdr = new TestDataReader("/test/Test_COLLECTION_AS_VALUE_01.txt");
		
		ValueElement xpr;
		JcCollection empty = new JcCollection("empty");
		List<Object> myVals = new ArrayList<>();
		myVals.add(1);
		myVals.add("Hallo");
		myVals.add(true);
		JcCollection primList = new JcCollection(myVals);
		JcCollection emptyVal = new JcCollection(Collections.emptyList());
		/*******************************/
		xpr = JC.coalesce(emptyVal, empty);
		result = print(xpr, Format.PRETTY_1);
		testId = "CAV_01";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		xpr = JC.coalesce(primList, empty);
		result = print(xpr, Format.PRETTY_1);
		testId = "CAV_02";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		/*******************************/
		JcNode activeIn = new JcNode("a");
		JcQuery q = new JcQuery();
		q.setClauses(new IClause[]{
//				MATCH.node(activeIn).label("ActiveIn"),
				DO.SET(activeIn.property("ratings")).byExpression(JC.coalesce(activeIn.property("ratings"), primList).asCollection().add(3)),
				SEPARATE.nextClause(),
				DO.SET(activeIn.property("ratings")).byExpression(JC.coalesce(activeIn.property("ratings"), empty).asCollection().add(3))
			});
		result = print(q, Format.PRETTY_1);
		testId = "CAV_03";
		assertQuery(testId, result, tdr.getTestData(testId));
		
		return;
	}

}
