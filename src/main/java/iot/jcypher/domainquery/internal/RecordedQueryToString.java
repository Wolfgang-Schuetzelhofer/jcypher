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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import iot.jcypher.domainquery.internal.RecordedQuery.Assignment;
import iot.jcypher.domainquery.internal.RecordedQuery.DOMatchRef;
import iot.jcypher.domainquery.internal.RecordedQuery.Invocation;
import iot.jcypher.domainquery.internal.RecordedQuery.Literal;
import iot.jcypher.domainquery.internal.RecordedQuery.Reference;
import iot.jcypher.domainquery.internal.RecordedQuery.Statement;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.values.ValueAccess;

public class RecordedQueryToString {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss.SSS");
	
	public static String queryToString(RecordedQuery query) {
		Context context = new Context();
		context.generic = query.isGeneric();
		context.sb.append(query.isGeneric() ? "Generic-DomainQuery" : "DomainQuery");
		context.sb.append("\n");
		List<Statement> stmts = query.getStatements();
		statementsToString(stmts, context);
		return context.sb.toString();
	}
	
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
						// if s is a stacked statement on the domain query
						if (s instanceof Invocation && ((Invocation)s).getOnObjectRef().equals(QueryRecorder.QUERY_ID)) {
							context.sb.append(",\n");
							context.sb.append(context.indent.getIndent());
						} else
							context.sb.append(", ");
					} else {
						context.sb.append(";\n");
						context.sb.append(context.indent.getIndent());
					}
				} else if (context.callDepth > 0) {
					// start of stacked statements
					if (s instanceof Invocation && ((Invocation)s).getOnObjectRef().equals(QueryRecorder.QUERY_ID)) {
						context.sb.append('\n');
						context.sb.append(context.indent.getIndent());
					}
				}
				if (context.callDepth == 0)
					context.topStatementStart = context.sb.length(); // needed for assignment statements
				statementToString(s, context);
			} else // concatenate statements
				callToString((Invocation)s, context); // must be an Invocation
			context.indent.calcAfter(s, context.callDepth);
			if (context.callDepth == 0 && i == statements.size() - 1) // the last one
				context.sb.append(';');
			prev = s;
		}
	}
	
	private static void statementToString(Statement statement, Context context) {
		if (statement instanceof Literal) {
			if (((Literal)statement).getValue() != null)
				PrimitiveWriter.writePrimitiveValue(((Literal)statement).getValue(), context.sb);
			else
				context.sb.append("null");
		} else if (statement instanceof Invocation) {
			invocationToString((Invocation)statement, context);
		} else if (statement instanceof DOMatchRef) {
			context.sb.append(((DOMatchRef)statement).getRef());
		} else if (statement instanceof Reference) {
			context.sb.append(((Reference)statement).getRefId());
		}
	}
	
	private static void invocationToString(RecordedQuery.Invocation invocation, Context context) {
		context.sb.append(invocation.getOnObjectRef());
		context.sb.append('.');
		callToString(invocation, context);
	}
	
	private static void callToString(RecordedQuery.Invocation invocation, Context context) {
		context.sb.append(invocation.getMethod());
		context.sb.append('(');
		List<Statement> params = invocation.getParams();
		context.callDepth++;
		context.indent.increment();
		if (params != null)
			statementsToString(params, context);
		context.indent.decrement();
		context.callDepth--;
		context.sb.append(')');
		if (invocation instanceof Assignment) {
			context.sb.insert(context.topStatementStart, " = ");
			context.sb.insert(context.topStatementStart, invocation.getReturnObjectRef());
		}
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
			String hint = statement.getHint();
			if (BR_CLOSE.equals(hint)) {
				if (level > 0) {
					level--;
					buildIndent();
				}
			}
		}
		
		private void calcAfter(Statement statement, int callDepth) {
			String hint = statement.getHint();
			if (BR_OPEN.equals(hint)) {
				level++;
				buildIndent();
			}
		}
		
		private void increment() {
			level++;
			buildIndent();
		}
		
		private void decrement() {
			if (level > 0) {
				level--;
				buildIndent();
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
		private int topStatementStart;
		private boolean generic;
		private StringBuilder sb = new StringBuilder();
	}
	
	/****************************************/
	private static class PrimitiveWriter {
		
		private static void writePrimitiveValue(Object val, StringBuilder sb) {
			if (val instanceof Number) {
				sb.append(val.toString());
			} else if (val instanceof Boolean) {
				sb.append(val.toString());
			} else if (val instanceof List<?>) {
				List<?> list = (List<?>)val;
				for (int i = 0; i < list.size(); i++) {
					if (i > 0)
						sb.append(", ");
					writePrimitiveValue(list.get(i), sb);
				}
			} else if (val.getClass().isArray()) {
				int len = Array.getLength(val);
				for (int i = 0; i < len; i++) {
					if (i > 0)
						sb.append(", ");
					writePrimitiveValue(Array.get(val, i), sb);
				}
			} else if (val instanceof JcValue) {
				sb.append(ValueAccess.getName((JcValue)val));
			} else if (val instanceof Date) {
				sb.append(dateFormat.format((Date)val));
			} else {
				sb.append('\'');
				sb.append(val.toString());
				sb.append('\'');
			}
		}
	}
}
