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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.driver.v1.types.Type;

import iot.jcypher.graph.GrLabel;
import iot.jcypher.query.result.util.ResultHandler.AContentHandler;
import iot.jcypher.query.result.util.ResultHandler.ElemType;
import iot.jcypher.query.result.util.ResultHandler.ElementInfo;
import iot.jcypher.query.result.util.ResultHandler.PathInfo;
import iot.jcypher.query.result.util.ResultHandler.RelationInfo;

public class BoltContentHandler extends AContentHandler {
	
	private List<Record> records;

	public BoltContentHandler(StatementResult statementResult) {
		this.records = statementResult.list(); // don't use streaming mode
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
	public Iterator<Entry<String, ?>> getPropertiesIterator(long id, int rowIndex, ElemType typ) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRelationType(long relationId, int rowIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GrLabel> getNodeLabels(long nodeId, int rowIndex) {
		// TODO Auto-generated method stub
		return null;
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
		
		/*****************************/
		public class Rec extends RowOrRecord {
			private Record record;
			
			public Rec(Record record) {
				super();
				this.record = record;
			}

			@Override
			public ElementInfo getElementInfo(String colKey) {
				Value val = this.record.get(colKey);
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
				Value val = this.record.get(colKey);
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

			@Override
			public <T> void addValue(String colKey, List<T> vals) {
				Value val = this.record.get(colKey);
				Object v = convertContentValue(val);
				if (v != null)
					vals.add((T) v);
				else {
					if (ResultHandler.includeNullValues.get().booleanValue())
						vals.add((T) v);
				}
			}
			
		}
	}
}
