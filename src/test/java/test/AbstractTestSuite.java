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

import java.util.List;

import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.values.ValueElement;
import iot.jcypher.query.values.ValueWriter;
import iot.jcypher.query.writer.CypherWriter;
import iot.jcypher.query.writer.Format;
import iot.jcypher.query.writer.JSONWriter;
import iot.jcypher.query.writer.WriterContext;

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
		ValueWriter.toValueExpression(valueElem, context, context.buffer);
		if (this.print) {
			System.out.println("");
			System.out.println(context.buffer.toString());
		}
		return context.buffer.toString();
	}
	
	protected String print(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		CypherWriter.toCypherExpression(query, context);
		if (this.print) {
			System.out.println("");
			System.out.println(context.buffer.toString());
		}
		return context.buffer.toString();
	}
	
	protected void print(String resultString) {
		if (this.print) {
			System.out.println("");
			System.out.println(resultString);
		}
	}
	
	protected String printJSON(JcQuery query, Format pretty) {
		WriterContext context = new WriterContext();
		context.cypherFormat = pretty;
		JSONWriter.toJSON(query, context);
		if (this.print) {
			System.out.println("");
			System.out.println(context.buffer.toString());
		}
		return context.buffer.toString();
	}
	
	protected String printErrors(JcQueryResult result) {
		StringBuilder sb = new StringBuilder();
		sb.append("---------------General Errors:");
		appendErrorList(result.getGeneralErrors(), sb);
		sb.append("\n---------------DB Errors:");
		appendErrorList(result.getDBErrors(), sb);
		sb.append("\n---------------end Errors:");
		String str = sb.toString();
		if (this.print) {
			System.out.println("");
			System.out.println(str);
		}
		return str;
	}
	
	/**
	 * print errors to System.out
	 * @param result
	 */
	protected static void printErrors(List<JcError> errors) {
		StringBuilder sb = new StringBuilder();
		sb.append("---------------Errors:");
		appendErrorList(errors, sb);
		sb.append("\n---------------end Errors:");
		String str = sb.toString();
		System.out.println("");
		System.out.println(str);
	}
	
	private static void appendErrorList(List<JcError> errors, StringBuilder sb) {
		int num = errors.size();
		for (int i = 0; i < num; i++) {
			JcError err = errors.get(i);
			sb.append('\n');
			if (i > 0) {
				sb.append("-------------------\n");
			}
			sb.append("codeOrType: ");
			sb.append(err.getCodeOrType());
			sb.append("\nmessage: ");
			sb.append(err.getMessage());
		}
	}
}
