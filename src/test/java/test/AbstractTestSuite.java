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

import static org.junit.Assert.assertEquals;
import iot.jcypher.CypherWriter;
import iot.jcypher.api.IClause;
import iot.jcypher.values.ValueElement;
import iot.jcypher.values.ValueWriter;
import iot.jcypher.writer.Format;
import iot.jcypher.writer.WriterContext;

public class AbstractTestSuite {

	private boolean print = true;
	private boolean doAssert = true;
	
	protected void setDoPrint(boolean print) {
		this.print = print;
	}
	
	protected void setDoAssert(boolean doAssert) {
		this.doAssert = doAssert;
	}

	protected void assertQuery(String testId, String query, String testData) {
		if (this.doAssert) {
			assertEquals(testId, testData, query);
		}
	}

	protected String print(IClause iclause, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		CypherWriter.toCypherExpression(iclause, 0, context);
		if (this.print) {
			System.out.println("");
			System.out.println(context.buffer.toString());
		}
		return context.buffer.toString();
	}
	
	protected String print(IClause[] iclauses, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		CypherWriter.toCypherExpression(iclauses, 0, context);
		if (this.print) {
			System.out.println("");
			System.out.println(context.buffer.toString());
		}
		return context.buffer.toString();
	}
	
	protected String print(ValueElement valueElem, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		ValueWriter.toValueExpression(valueElem, context);
		if (this.print) {
			System.out.println("");
			System.out.println(context.buffer.toString());
		}
		return context.buffer.toString();
	}
}
