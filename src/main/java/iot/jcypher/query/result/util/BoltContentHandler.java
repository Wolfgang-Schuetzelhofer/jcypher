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

package iot.jcypher.query.result.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.neo4j.driver.v1.types.Entity;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.driver.v1.types.Type;
import org.neo4j.driver.v1.util.Pair;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrAccess;
import iot.jcypher.graph.GrLabel;
import iot.jcypher.graph.SyncState;
import iot.jcypher.query.InternalQAccess;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.result.util.ResultHandler.AContentHandler;
import iot.jcypher.query.result.util.ResultHandler.ElemType;
import iot.jcypher.query.result.util.ResultHandler.ElementInfo;
import iot.jcypher.query.result.util.ResultHandler.PathInfo;
import iot.jcypher.query.result.util.ResultHandler.RelationInfo;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.util.ResultSettings;
import iot.jcypher.util.Util;

public class BoltContentHandler extends AContentHandler {
	
	private List<Record> records;
	private Reloaded reloaded;

	public BoltContentHandler(StatementResult statementResult, ResultHandler rh) {
		if (statementResult != null)
			this.records = statementResult.list(); // don't use streaming mode
		else
			this.records = new ArrayList<Record>();
		this.reloaded = new Reloaded(rh.getDbAccess());
	}

	@Override
	public List<String> getColumns() {
		List<String> columns = new ArrayList<String>();
		if (this.records.size() > 0) {
			Record rec = this.records.get(0);
			columns.addAll(rec.keys());
		}
		return columns;
	}

	@Override
	public int getColumnIndex(String colKey) {
		if (this.records.size() > 0) {
			Record rec = this.records.get(0);
			int idx;
			try {
				idx = rec.index(colKey);
			} catch (NoSuchElementException e) {
				idx = -1;
			}
			return idx;
		}
		return -1;
	}

	@Override
	public Iterator<RowOrRecord> getDataIterator() {
		return new RecordIterator();
	}

	@Override
	public Object convertContentValue(Object value) {
		if (value instanceof Value) {
			Value val = (Value) value;
			Object ret = null;
			Type typ = val.type();
			InternalTypeSystem ts = InternalTypeSystem.TYPE_SYSTEM;
			if (typ == ts.NUMBER())
				ret = new BigDecimal(val.asNumber().toString());
			else if (typ == ts.INTEGER())
				ret = new BigDecimal(val.asNumber().toString());
			else if (typ == ts.FLOAT())
				ret = new BigDecimal(val.asNumber().toString());
			else if (typ == ts.STRING())
				ret = val.asString();
			else if (typ == ts.BOOLEAN())
				ret = Boolean.valueOf(val.asBoolean());
			else if (typ == ts.LIST()) {
				List<Object> orgVals = val.asList();
				List<Object> vals = new ArrayList<Object>();
				int sz = orgVals.size();
				for (int i = 0; i < sz; i++) {
					Object v = orgVals.get(i);
					vals.add(convertContentValue(v));
				}
				ret = vals;
			} else if (typ == ts.ANY()) {
				ret = val.asObject();
			}
			return ret;
		}
		return value;
	}

	@Override
	public Iterator<PropEntry> getPropertiesIterator(long id, int rowIndex, ElemType typ) {
		Entity propertiesObject = getPropertiesObject(id, rowIndex, typ);
		Iterator<Entry<String, Object>> esIt = propertiesObject.asMap().entrySet().iterator();
		return new PropertiesIterator(esIt); 
	}

	@Override
	public String getRelationType(long relationId, int rowIndex) {
		if (rowIndex >= 0) {
			Relationship rel = (Relationship) getPropertiesObject(relationId, rowIndex, ElemType.RELATION);
			return rel.type();
		}
		return null;
	}

	@Override
	public List<GrLabel> getNodeLabels(long nodeId, int rowIndex) {
		List<GrLabel> labels = new ArrayList<GrLabel>();
		if (rowIndex >= 0) {
			Node nd = (Node) getPropertiesObject(nodeId, rowIndex, ElemType.NODE);
			for (String lab : nd.labels()) {
				GrLabel label = GrAccess.createLabel(lab);
				GrAccess.setState(label, SyncState.SYNC);
				labels.add(label);
			}
		}
		return labels;
	}
	
	private Entity getPropertiesObject(long id, int rowIndex, ElemType typ) {
		Record rec = this.records.get(rowIndex);
		List<Pair<String, Value>> flds = rec.fields();
		for (Pair<String, Value> pair : flds) {
			if (typ == ElemType.NODE && pair.value() instanceof NodeValue) {
				Node nd = pair.value().asNode();
				if (nd.id() == id)
					return nd;
			} else if (typ == ElemType.RELATION && pair.value() instanceof RelationshipValue) {
				Relationship rel = pair.value().asRelationship();
				if (rel.id() == id)
					return rel;
			}
		}
		
		// element with id may not have been loaded
		return this.reloaded.getEntity(id, typ);
	}

	/************************************/
	public class RecordIterator implements Iterator<RowOrRecord> {

		private Iterator<Record> recordIterator;
		
		public RecordIterator() {
			super();
			this.recordIterator = records.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.recordIterator.hasNext();
		}

		@Override
		public RowOrRecord next() {
			Record nextVal = this.recordIterator.next();
			return new Rec(nextVal);
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		/*****************************/
		public class Rec extends RowOrRecord {
			private Record record;
			
			public Rec(Record record) {
				super();
				this.record = record;
			}

			@Override
			public ElementInfo getElementInfo(String colKey) {
				Value val;
				try {
					val = this.record.get(colKey);
				} catch (NoSuchRecordException e) {
					throw new RuntimeException("no result column: " + colKey);
				}
				return ElementInfo.fromRecordValue(val);
			}

			@Override
			public RelationInfo getRelationInfo(String colKey) {
				Value val = this.record.get(colKey);
				return RelationInfo.fromRecordValue(val);
			}

			@Override
			public PathInfo getPathInfo(String colKey) {
				PathInfo pathInfo = null;
				Value val;
				try {
					val = this.record.get(colKey);
				} catch (NoSuchRecordException e) {
					throw new RuntimeException("no result column: " + colKey);
				}
				String typName = val.type().name();
				if ("PATH".equals(typName)) {
					Path p = val.asPath();
					long startId = p.start().id();
					long endId = p.end().id();
					List<Long> relIds = new ArrayList<Long>();
					Iterator<Relationship> it = p.relationships().iterator();
					while(it.hasNext()) {
						Relationship rel = it.next();
						relIds.add(Long.valueOf(rel.id()));
					}
					pathInfo = new PathInfo(startId, endId, relIds, p);
				}
				return pathInfo;
			}

			@SuppressWarnings("unchecked")
			@Override
			public long gePathtNodeIdAt(PathInfo pathInfo, int index) {
				Object obj = pathInfo.getContentObject();
				List<Node> nodes = null;
				if (obj instanceof List<?>)
					nodes = (List<Node>) obj;
				else if (obj instanceof Path) {
					nodes = new ArrayList<Node>();
					Iterator<Node> it = ((Path)obj).nodes().iterator();
					while(it.hasNext())
						nodes.add(it.next());
					pathInfo.setContentObject(nodes);
				}
				return nodes.get(index).id();
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> void addValue(String colKey, List<T> vals) {
				Value val;
				try {
					val = this.record.get(colKey);
				} catch (NoSuchRecordException e) {
					throw new RuntimeException("no result column: " + colKey);
				}
				Object v = convertContentValue(val);
				if (v != null)
					vals.add((T) v);
				else {
					if (ResultHandler.includeNullValues.get().booleanValue() 
							|| ResultSettings.includeNullValuesAndDuplicates.get().booleanValue())
						vals.add((T) v);
				}
			}
			
		}
	}
	
	/************************************/
	public class PropertiesIterator implements Iterator<PropEntry> {

		private Iterator<Entry<String, Object>> iterator;
		
		public PropertiesIterator(Iterator<Entry<String, Object>> iterator) {
			super();
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public PropEntry next() {
			Entry<String, Object> next = this.iterator.next();
			return new PropEntry(next.getKey(), next.getValue());
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/************************************/
	private class Reloaded {
		private Map<Long, Node> nodesById;
		private Map<Long, Relationship> relationsById;
		private IDBAccess dbAccess;
		
		private Reloaded(IDBAccess dba) {
			super();
			this.dbAccess = dba;
		}

		private Entity getEntity(long id, ElemType typ) {
			if (typ == ElemType.NODE)
				return this.getNode(id);
			else
				return this.getRelation(id);
		}
		
		private Node getNode(long id) {
			if (this.nodesById == null)
				this.nodesById = new HashMap<Long, Node>();
			Node ret = this.nodesById.get(id);
			if (ret == null) {
				ret = (Node) this.loadPropertiesObject(id, ElemType.NODE);
				this.nodesById.put(id, ret);
			}
			return ret;
		}
		
		private Relationship getRelation(long id) {
			if (this.relationsById == null)
				this.relationsById = new HashMap<Long, Relationship>();
			Relationship ret = this.relationsById.get(id);
			if (ret == null) {
				ret = (Relationship) this.loadPropertiesObject(id, ElemType.RELATION);
				this.relationsById.put(id, ret);
			}
			return ret;
		}
		
		private Entity loadPropertiesObject(long id, ElemType typ) {
			IClause[] clauses;
			JcNode n;
			JcRelation r;
			if (typ == ElemType.NODE) {
				n = new JcNode("n");
				clauses = new IClause[] {
						START.node(n).byId(id),
						RETURN.value(n)
				};
			} else {
				r = new JcRelation("r");
				clauses = new IClause[] {
						START.relation(r).byId(id),
						RETURN.value(r)
				};
			}
			JcQuery q = new JcQuery();
			q.setClauses(clauses);
			JcQueryResult result = this.dbAccess.execute(q);
			if (result.hasErrors()) {
				StringBuilder sb = new StringBuilder();
				Util.appendErrorList(Util.collectErrors(result), sb);
				throw new RuntimeException(sb.toString());
			}
			BoltContentHandler ch = (BoltContentHandler) InternalQAccess.getResultHandler(result).getContentHandler();
			return ch.getPropertiesObject(id, 0, typ);
		}
	}
}
