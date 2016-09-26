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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import iot.jcypher.graph.GrAccess;
import iot.jcypher.graph.GrLabel;
import iot.jcypher.graph.SyncState;
import iot.jcypher.query.result.util.ResultHandler.AContentHandler;
import iot.jcypher.query.result.util.ResultHandler.ElemType;
import iot.jcypher.query.result.util.ResultHandler.ElementInfo;
import iot.jcypher.query.result.util.ResultHandler.PathInfo;
import iot.jcypher.query.result.util.ResultHandler.RelationInfo;

public class JSONContentHandler extends AContentHandler {
	
	private JsonObject jsonResult;
	// needed to support multiple queries
	private int queryIndex;
	private List<String> columns;
	private Map<String, Integer> columnIndices;

	public JSONContentHandler(JsonObject jsonResult, int queryIndex) {
		this.jsonResult = jsonResult;
		this.queryIndex = queryIndex;
		this.columnIndices = new HashMap<String, Integer>();
	}

	@Override
	public List<String> getColumns() {
		if (this.columns == null) {
			List<String> colmns = new ArrayList<String>();
			JsonArray cols = ((JsonObject)this.jsonResult.getJsonArray("results").get(
					this.queryIndex)).getJsonArray("columns");
			int sz = cols.size();
			for (int i = 0;i < sz; i++) {
				colmns.add(cols.getString(i));
			}
			this.columns = colmns;
		}
		return this.columns;
	}
	
	@Override
	public Iterator<RowOrRecord> getDataIterator() {
		return new RowIterator();
	}

	@Override
	public int getColumnIndex(String colKey) {
		Integer idx = this.columnIndices.get(colKey);
		if (idx == null) {
			List<String> cols = getColumns();
			for (int i = 0; i < cols.size(); i++) {
				if (cols.get(i).equals(colKey)) {
					idx = new Integer(i);
				}
			}
			if (idx == null)
				idx = new Integer(-1);
			this.columnIndices.put(colKey, idx);
		}
		return idx.intValue();
	}
	
	@Override
	public Object convertContentValue(Object value) {
		if (value instanceof JsonValue) {
			JsonValue val = (JsonValue) value;
			Object ret = null;
			ValueType typ = val.getValueType();
			if (typ == ValueType.NUMBER)
				ret = ((JsonNumber)val).bigDecimalValue();
			else if (typ == ValueType.STRING)
				ret = ((JsonString)val).getString();
			else if (typ == ValueType.FALSE)
				ret = Boolean.FALSE;
			else if (typ == ValueType.TRUE)
				ret = Boolean.TRUE;
			else if (typ == ValueType.ARRAY) {
				JsonArray arr = (JsonArray)val;
				List<Object> vals = new ArrayList<Object>();
				int sz = arr.size();
				for (int i = 0; i < sz; i++) {
					JsonValue v = arr.get(i);
					vals.add(convertContentValue(v));
				}
				ret = vals;
			} else if (typ == ValueType.OBJECT) {
				//JsonObject obj = (JsonObject)val;
			}
			return ret;
		}
		return value;
	}

	@Override
	public Iterator<PropEntry> getPropertiesIterator(long id, int rowIndex, ElemType typ) {
		JsonObject propertiesObject = getPropertiesObject(id, rowIndex, typ);
		Iterator<Entry<String, JsonValue>> esIt = propertiesObject.entrySet().iterator();
		return new PropertiesIterator(esIt);
	}

	@Override
	public String getRelationType(long relationId, int rowIndex) {
		if (rowIndex >= 0) {
			JsonObject graphObject = getGraphObject(rowIndex);
			JsonArray elemsArray = graphObject.getJsonArray("relationships");
			int sz = elemsArray.size();
			for (int i = 0; i < sz; i++) {
				JsonObject elem = elemsArray.getJsonObject(i);
				String idStr = elem.getString("id");
				long elemId;
				try {
					elemId = Long.parseLong(idStr);
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
				if (relationId == elemId) {
					return elem.getString("type");
				}
			}
		}
		return null;
	}

	@Override
	public List<GrLabel> getNodeLabels(long nodeId, int rowIndex) {
		List<GrLabel> labels = new ArrayList<GrLabel>();
		if (rowIndex >= 0) {
			JsonArray labelsArray = getNodeLabelsObject(nodeId, rowIndex);
			int sz = labelsArray.size();
			for (int i = 0; i < sz; i++) {
				GrLabel label = GrAccess.createLabel(labelsArray.getString(i));
				GrAccess.setState(label, SyncState.SYNC);
				labels.add(label);
			}
		}
		return labels;
	}

	private JsonArray getNodeLabelsObject(long id, int rowIndex) {
		JsonObject graphObject = getGraphObject(rowIndex);
		JsonArray elemsArray = graphObject.getJsonArray("nodes");
		int sz = elemsArray.size();
		for (int i = 0; i < sz; i++) {
			JsonObject elem = elemsArray.getJsonObject(i);
			String idStr = elem.getString("id");
			long elemId;
			try {
				elemId = Long.parseLong(idStr);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			if (id == elemId) {
				return elem.getJsonArray("labels");
			}
		}
		return null;
	}
	
	private JsonArray getRestArray(JsonObject dataObject) {
		return dataObject.getJsonArray("rest");
	}
	
	private JsonValue getRestValue(JsonArray restArray, int colIdx) {
		JsonValue obj = restArray.get(colIdx);
		if (obj.getValueType() == ValueType.ARRAY && ((JsonArray)obj).size() > 0)
			obj = ((JsonArray)obj).get(0);
		return obj;
	}
	
	private JsonObject getRestObject(JsonArray restArray, int colIdx) {
		JsonValue obj = getRestValue(restArray, colIdx);
		if (obj.getValueType() == ValueType.OBJECT)
			return (JsonObject) obj;
		return null;
	}
	
	private JsonArray getDataArray() {
		return ((JsonObject)jsonResult.getJsonArray("results").get(
				queryIndex)).getJsonArray("data");
	}
	
	private JsonObject getDataObject(int rowIndex) {
		JsonArray datas = getDataArray();
		return datas.getJsonObject(rowIndex);
	}
	
	private JsonObject getGraphObject(int rowIndex) {
		JsonObject dataObject = getDataObject(rowIndex);
		return dataObject.getJsonObject("graph");
	}
	
	private JsonObject getPropertiesObject(long id, int rowIndex, ElemType typ) {
		JsonObject graphObject = getGraphObject(rowIndex);
		JsonArray elemsArray = null;
		if (typ == ElemType.NODE)
			elemsArray = graphObject.getJsonArray("nodes");
		else if (typ == ElemType.RELATION)
			elemsArray = graphObject.getJsonArray("relationships");
		int sz = elemsArray.size();
		for (int i = 0; i < sz; i++) {
			JsonObject elem = elemsArray.getJsonObject(i);
			String idStr = elem.getString("id");
			long elemId;
			try {
				elemId = Long.parseLong(idStr);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			if (id == elemId) {
				return elem.getJsonObject("properties");
			}
		}
		return null;
	}
	
	/************************************/
	public class RowIterator implements Iterator<RowOrRecord> {

		private Iterator<JsonValue> jsonIterator;
		
		public RowIterator() {
			super();
			JsonArray datas = getDataArray();
			this.jsonIterator = datas.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.jsonIterator.hasNext();
		}

		@Override
		public RowOrRecord next() {
			JsonValue nextVal = this.jsonIterator.next();
			return new Row(nextVal);
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		/*****************************/
		public class Row extends RowOrRecord {
			private JsonValue jsonValue;

			public Row(JsonValue jsonValue) {
				super();
				this.jsonValue = jsonValue;
			}

			@Override
			public ElementInfo getElementInfo(String colKey) {
				int colIdx = getColumnIndex(colKey);
				if (colIdx == -1)
					throw new RuntimeException("no result column: " + colKey);
				JsonObject dataObject = (JsonObject) this.jsonValue;
				JsonArray restArray = getRestArray(dataObject);
				JsonValue obj = getRestValue(restArray, colIdx);
				if (obj.getValueType() == ValueType.OBJECT) {
					JsonObject restObject = (JsonObject)obj;
					if (restObject.containsKey("self")) {
						String selfString = restObject.getString("self");
						if (selfString != null) {
							ElementInfo ei = ElementInfo.parse(selfString);
							return ei;
						}
					}
				} else if (obj.getValueType() == ValueType.NULL) {
					ElementInfo ei = ElementInfo.nullElement();
					return ei;
				}
				return null;
			}

			@Override
			public RelationInfo getRelationInfo(String colKey) {
				JsonObject dataObject = (JsonObject) this.jsonValue;
				JsonArray restArray = getRestArray(dataObject);
				JsonObject restObject = getRestObject(restArray, getColumnIndex(colKey));
				String startString = restObject.getString("start");
				String endString = restObject.getString("end");
				RelationInfo ri = RelationInfo.parse(startString, endString);
				return ri;
			}

			@Override
			public PathInfo getPathInfo(String colKey) {
				PathInfo pathInfo = null;
				int colIdx = getColumnIndex(colKey);
				if (colIdx == -1)
					throw new RuntimeException("no result column: " + colKey);
				JsonObject dataObject = (JsonObject) this.jsonValue;
				JsonArray restArray = getRestArray(dataObject);
				JsonValue restValue = getRestValue(restArray, colIdx);
				
				if (restValue.getValueType() == ValueType.OBJECT) {
					JsonObject pathObject = (JsonObject) restValue;
					String str = pathObject.getString("start");
					long startId = Long.parseLong(str.substring(str.lastIndexOf('/') + 1));
					str = pathObject.getString("end");
					long endId = Long.parseLong(str.substring(str.lastIndexOf('/') + 1));
					JsonArray rels = pathObject.getJsonArray("relationships");
					List<Long> relIds = new ArrayList<Long>();
					int sz = rels.size();
					for (int i = 0; i < sz; i++) {
						String rel = rels.getString(i);
						long rid = Long.parseLong(rel.substring(rel.lastIndexOf('/') + 1));
						relIds.add(Long.valueOf(rid));
					}
					pathInfo = new PathInfo(startId, endId, relIds, pathObject);
				}
				return pathInfo;
			}

			@Override
			public long gePathtNodeIdAt(PathInfo pathInfo, int index) {
				Object obj = pathInfo.getContentObject();
				JsonArray nodes = null;
				if (obj instanceof JsonArray)
					nodes = (JsonArray)obj;
				else if (obj instanceof JsonObject) {
					nodes = ((JsonObject)obj).getJsonArray("nodes");
					pathInfo.setContentObject(nodes);
				}
				String str = nodes.getString(index);
				return Long.parseLong(str.substring(str.lastIndexOf('/') + 1));
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> void addValue(String colKey, List<T> vals) {
				int colIdx = getColumnIndex(colKey);
				if (colIdx == -1)
					throw new RuntimeException("no result column: " + colKey);
				JsonValue restVal = getRestValue(getRestArray((JsonObject) this.jsonValue), colIdx);
				Object v = convertContentValue(restVal);
				if (v != null)
					vals.add((T) v);
				else {
					if (ResultHandler.includeNullValues.get().booleanValue())
						vals.add((T) v);
				}
			}
		}
	}
	
	/************************************/
	public class PropertiesIterator implements Iterator<PropEntry> {

		private Iterator<Entry<String, JsonValue>> iterator;
		
		public PropertiesIterator(Iterator<Entry<String, JsonValue>> iterator) {
			super();
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public PropEntry next() {
			Entry<String, JsonValue> next = this.iterator.next();
			return new PropEntry(next.getKey(), next.getValue());
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
