/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.domainquery.internal;

import java.util.List;

import iot.jcypher.domainquery.internal.RecordedQuery.Assignment;
import iot.jcypher.domainquery.internal.RecordedQuery.Invocation;
import iot.jcypher.domainquery.internal.RecordedQuery.Literal;
import iot.jcypher.domainquery.internal.RecordedQuery.Statement;
import iot.jcypher.query.writer.CypherWriter;

public class RecordedQueryToString {

	public static String queryToString(RecordedQuery<?> query) {
		Context context = new Context();
		context.sb.append(query.isGeneric() ? "Generic-DomainQuery" : "DomainQuery");
		context.sb.append("\n");
		List<Statement> stmts = query.getStatements();
		statementsToString(stmts, context);
		return context.sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private static void statementsToString(List<Statement> statements, Context context) {
		Statement prev = null;
		for(int i = 0; i < statements.size(); i++) {
			Statement s = statements.get(i);
			context.indent.calcBefore(s, context.callDepth);
			boolean separator = false;
			if (prev instanceof Invocation && !(prev instanceof Assignment) && s instanceof Invocation) {
				if (((Invocation)prev).getReturnObjectRef().equals(((Invocation)s).getOnObjectRef())) {
					context.sb.append('.');
					separator = true;
				}
			}
			if (!separator) { // start new statement
				if (prev != null) {
					if (context.callDepth > 0) {
						context.sb.append(", ");
					} else {
						context.sb.append(";\n");
						context.sb.append(context.indent.getIndent());
					}
				}
				statementToString(s, context);
			} else // concatenate statements
				callToString((Invocation)s, context); // must be an Invocation
			context.indent.calcAfter(s, context.callDepth);
			if (context.callDepth == 0 && i == statements.size() - 1) // the last one
				context.sb.append(';');
			prev = s;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static void statementToString(Statement statement, Context context) {
		if (statement instanceof Literal) {
			if (((Literal)statement).getValue() != null)
				CypherWriter.PrimitiveCypherWriter.writePrimitiveValue(((Literal)statement).getValue(), null, context.sb);
			else
				context.sb.append("null");
		} else if (statement instanceof Assignment) {
			context.sb.append(((Assignment)statement).getReturnObjectRef());
			context.sb.append(" = ");
			invocationToString((Assignment)statement, context);
		} else if (statement instanceof Invocation) {
			invocationToString((Invocation)statement, context);
		}
	}
	
	private static void invocationToString(RecordedQuery<?>.Invocation invocation, Context context) {
		context.sb.append(invocation.getOnObjectRef());
		context.sb.append('.');
		callToString(invocation, context);
	}
	
	private static void callToString(RecordedQuery<?>.Invocation invocation, Context context) {
		context.sb.append(invocation.getMethod());
		context.sb.append('(');
		List<Statement> params = invocation.getParams();
		context.callDepth++;
		if (params != null)
			statementsToString(params, context);
		context.callDepth--;
		context.sb.append(')');
	}
	
	/******************************************/
	private static class Indent {
		
		private static final String BR_OPEN = "BR_OPEN";
		private static final String BR_CLOSE = "BR_CLOSE";
		private static final String IN = "   ";
		
		private int level = 0;
		private String indent = new String();
		
		private String getIndent() {
			return indent;
		}

		private void calcBefore(Statement statement, int callDepth) {
			if (callDepth == 0) {
				String hint = statement.getHint();
				if (BR_CLOSE.equals(hint)) {
					if (level > 0) {
						level--;
						buildIndent();
					}
				}
			}
		}
		
		private void calcAfter(Statement statement, int callDepth) {
			if (callDepth == 0) {
				String hint = statement.getHint();
				if (BR_OPEN.equals(hint)) {
					level++;
					buildIndent();
				}
			}
		}
		
		private void buildIndent() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < level; i++) {
				sb.append(IN);
			}
			indent = sb.toString();
		}
	}
	
	/******************************************/
	private static class Context {
		private Indent indent = new Indent();
		private int callDepth = 0;
		private StringBuilder sb = new StringBuilder();
	}
}
