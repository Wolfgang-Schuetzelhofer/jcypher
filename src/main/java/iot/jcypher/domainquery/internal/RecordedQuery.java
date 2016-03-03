/************************************************************************
 * Copyright (c) 2015-2016 IoT-Solutions e.U.
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

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.domainquery.AbstractDomainQuery;

public class RecordedQuery {

	private boolean generic;
	private List<Statement> statements;
	
	public <E extends AbstractDomainQuery> RecordedQuery(boolean generic) {
		super();
		this.generic = generic;
		this.statements = new ArrayList<Statement>();
	}
	
	public void addInvocation(String on, String method, String retObjectRef, List<Statement> params) {
		Invocation inv = new Invocation(on, method, retObjectRef, params);
		this.statements.add(inv);
	}
	
	public void addAssignment(String on, String method, String retObjectRef, List<Statement> params) {
		Assignment ass = new Assignment(on, method, retObjectRef, params);
		this.statements.add(ass);
	}
	
	public Literal literal(Object value) {
		return new Literal(value);
	}
	
	public Reference reference(Object value, String refId) {
		return new Reference(value, refId);
	}
	
	public DOMatchRef doMatchRef(String ref) {
		return new DOMatchRef(ref);
	}
	
	public List<Statement> getStatements() {
		return statements;
	}

	public boolean isGeneric() {
		return generic;
	}
	
	@Override
	public String toString() {
		return RecordedQueryToString.queryToString(this);
	}

	/**************************************/
	public interface Statement {
		public String getHint();
		public RecordedQuery getRecordedQuery();
	}

	/**************************************/
	public class Invocation implements Statement {
		private String onObjectRef;
		private String method;
		private String returnObjectRef;
		private List<Statement> params;
		
		public Invocation(String onObjectRef, String method, String retObjectRef, List<Statement> params) {
			super();
			this.onObjectRef = onObjectRef;
			this.method = method;
			this.returnObjectRef = retObjectRef;
			this.params = params;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public void setOnObjectRef(String onObjectRef) {
			this.onObjectRef = onObjectRef;
		}

		public String getOnObjectRef() {
			return onObjectRef;
		}

		public String getReturnObjectRef() {
			return returnObjectRef;
		}

		public void setReturnObjectRef(String returnObjectRef) {
			this.returnObjectRef = returnObjectRef;
		}
		
		public List<Statement> getParams() {
			return params;
		}

		@Override
		public String getHint() {
			return this.method;
		}

		@Override
		public RecordedQuery getRecordedQuery() {
			return RecordedQuery.this;
		}
	}
	
	/**************************************/
	public class Assignment extends Invocation {

		public Assignment(String onObjectRef, String method, String retObjectRef, List<Statement> params) {
			super(onObjectRef, method, retObjectRef, params);
		}
		
	}
	
	/**************************************/
	public class DOMatchRef implements Statement {

		private String ref;
		
		public DOMatchRef(String ref) {
			super();
			this.ref = ref;
		}

		public String getRef() {
			return ref;
		}

		@Override
		public String getHint() {
			return this.ref;
		}

		@Override
		public RecordedQuery getRecordedQuery() {
			return RecordedQuery.this;
		}
		
	}
	
	/**************************************/
	public class Literal implements Statement {
		
		private Object value;

		public Literal(Object value) {
			super();
			this.value = value;
		}

		public Object getValue() {
			if (this.value instanceof iot.jcypher.domainquery.ast.Parameter)
				return ((iot.jcypher.domainquery.ast.Parameter)this.value).getValue();
			return this.value;
		}
		
		@Override
		public String getHint() {
			if (this.value != null) {
				if (this.value instanceof iot.jcypher.domainquery.ast.Parameter)
					return "_QueryParameter_";
				return this.value.toString();
			} else
				return new String();
		}

		@Override
		public RecordedQuery getRecordedQuery() {
			return RecordedQuery.this;
		}
	}
	
	/**************************************/
	public class Reference implements Statement {

		private Object value;
		private String refId;

		public Reference(Object value, String refId) {
			super();
			this.value = value;
			this.refId= refId;
		}
		
		public Object getValue() {
			return value;
		}

		public String getRefId() {
			return refId;
		}

		@Override
		public String getHint() {
			return "_Reference_";
		}

		@Override
		public RecordedQuery getRecordedQuery() {
			return RecordedQuery.this;
		}
		
	}
}
