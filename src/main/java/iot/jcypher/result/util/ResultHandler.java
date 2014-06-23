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

package iot.jcypher.result.util;

import iot.jcypher.JcQueryResult;
import iot.jcypher.graph.GrAccess;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.values.JcBoolean;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcProperty;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueWriter;
import iot.jcypher.query.writer.WriterContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

public class ResultHandler {

	private JcQueryResult queryResult;
	private Map<Long, GrNode> nodesById;
	private Map<Long, GrRelation> relationsById;
	private Map<String, List<GrNode>> nodeColumns;
	private Map<String, List<GrRelation>> relationColumns;
	@SuppressWarnings("rawtypes")
	private Map<String, ValueList> valueColumns;
	private List<String> columns;
	// only needed to lookup nodes and relations
	private List<String> unResolvedColumns;

	public ResultHandler(JcQueryResult queryResult) {
		super();
		this.queryResult = queryResult;
	}
	
	public List<GrNode> getNodes(JcNode node) {
		String colKey =  ValueAccess.getName(node);
		return getNodes(colKey);
	}
	
	private List<GrNode> getNodes(String colKey) {
		List<GrNode> rNodes = getNodeColumns().get(colKey);
		if (rNodes == null) {
			rNodes = new ArrayList<GrNode>();
			int colIdx = getColumnIndex(colKey);
			if (colIdx == -1)
				throw new RuntimeException("no result column: " + colKey);
			Iterator<JsonValue> it = getDataIterator();
			while(it.hasNext()) { // iterate over rows
				JsonObject dataObject = (JsonObject) it.next();
				ElementInfo ei = getElementInfo(dataObject, colIdx);
				GrNode rNode = getNodesById().get(ei.id);
				if (rNode == null) {
					rNode = GrAccess.createNode(this, ei.id, colKey);
					getNodesById().put(ei.id, rNode);
				}
				rNodes.add(rNode);
			}
			getNodeColumns().put(colKey, rNodes);
			getUnresolvedColumns().remove(colKey);
		}
		return rNodes;
	}
	
	public List<GrRelation> getRelations(JcRelation relation) {
		String colKey =  ValueAccess.getName(relation);
		return getRelations(colKey);
	}
	
	private List<GrRelation> getRelations(String colKey) {
		List<GrRelation> rRelations = getRelationColumns().get(colKey);
		if (rRelations == null) {
			rRelations = new ArrayList<GrRelation>();
			int colIdx = getColumnIndex(colKey);
			if (colIdx == -1)
				throw new RuntimeException("no result column: " + colKey);
			Iterator<JsonValue> it = getDataIterator();
			while(it.hasNext()) { // iterate over rows
				JsonObject dataObject = (JsonObject) it.next();
				ElementInfo ei = getElementInfo(dataObject, colIdx);
				RelationInfo ri = getRelationInfo(dataObject, colIdx);
				GrRelation rRelation = getRelationsById().get(ei.id);
				if (rRelation == null) {
					rRelation = GrAccess.createRelation(this, ei.id, colKey,
							ri.startNodeId, ri.endNodeId);
					getRelationsById().put(ei.id, rRelation);
				}
				rRelations.add(rRelation);
			}
			getRelationColumns().put(colKey, rRelations);
			getUnresolvedColumns().remove(colKey);
		}
		
		return rRelations;
	}
	
	public List<BigDecimal> getNumbers(JcNumber number) {
		return this.getValues(number);
	}
	
	public List<String> getStrings(JcString string) {
		return this.getValues(string);
	}
	
	public List<Boolean> getBooleans(JcBoolean bool) {
		return this.getValues(bool);
	}
	
	public List<List<?>> getCollections(JcCollection collection) {
		return this.getValues(collection);
	}
	
	public List<?> getObjects(JcValue val) {
		return this.getValues(val);
	}
	
	public GrNode getNode(long id) {
		GrNode rNode = getNodesById().get(id);
		if (rNode == null) {
			// first resolve unresolved columns, so the column name is set
			if (getUnresolvedColumns().size() > 0) {
				List<String> ucols = new ArrayList<String>();
				ucols.addAll(getUnresolvedColumns());
				for (int i = 0; i < ucols.size(); i++) {
					String colKey = ucols.get(i);
					int colIdx = getColumnIndex(colKey);
					Iterator<JsonValue> it = getDataIterator();
					boolean isNodeColumn = false;
					while(it.hasNext()) { // iterate over just one row
						JsonObject dataObject = (JsonObject) it.next();
						if (isColumnOfType(ElemType.NODE, dataObject, colIdx)) {
							isNodeColumn = true;
						}
						break;
					}
					if (isNodeColumn) {
						// resolve nodes of column
						getNodes(colKey);
						// test if node has been resolved
						rNode = getNodesById().get(id);
						if (rNode != null)
							return rNode;
					} else
						getUnresolvedColumns().remove(colKey);
				}
			}
		}
		if (rNode == null) {
			rNode = GrAccess.createNode(this, id, null);
			getNodesById().put(id, rNode);
		}
		return rNode;
	}
	
	private GrNode resolveUptoNode(long id) {
		Iterator<JsonValue> it = getDataIterator();
		while(it.hasNext()) { // iterate over rows
			JsonObject dataObject = (JsonObject) it.next();
			JsonObject graphObject = getGraphOject(dataObject);
			JsonArray nodesArray = graphObject.getJsonArray("nodes");
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> getValues(JcValue jcValue) {
		String colKey;
		if (jcValue instanceof JcProperty) {
			WriterContext ctxt = new WriterContext();
			ValueWriter.toValueExpression(jcValue, ctxt);
			colKey = ctxt.buffer.toString();
		} else
			colKey =  ValueAccess.getName(jcValue);
		ValueList<T> vals = getValueColumns().get(colKey);
		if (vals == null) {
			vals = new ValueList<T>();
			int colIdx = getColumnIndex(colKey);
			if (colIdx == -1)
				throw new RuntimeException("no result column: " + colKey);
			Iterator<JsonValue> it = getDataIterator();
			while(it.hasNext()) { // iterate over rows
				JsonObject dataObject = (JsonObject) it.next();
				 addValue(dataObject, colIdx, vals);
			}
			getValueColumns().put(colKey, vals);
			getUnresolvedColumns().remove(colKey);
		}
		return vals.values;
	}

	private Map<String, List<GrNode>> getNodeColumns() {
		if (this.nodeColumns == null)
			this.nodeColumns = new HashMap<String, List<GrNode>>();
		return this.nodeColumns;
	}
	
	private Map<String, List<GrRelation>> getRelationColumns() {
		if (this.relationColumns == null)
			this.relationColumns = new HashMap<String, List<GrRelation>>();
		return this.relationColumns;
	}
	
	@SuppressWarnings("rawtypes")
	private Map<String, ValueList> getValueColumns() {
		if (this.valueColumns == null)
			this.valueColumns = new HashMap<String, ValueList>();
		return this.valueColumns;
	}
	
	private Map<Long, GrNode> getNodesById() {
		if (this.nodesById == null)
			this.nodesById = new HashMap<Long, GrNode>();
		return this.nodesById;
	}
	
	private Map<Long, GrRelation> getRelationsById() {
		if (this.relationsById == null)
			this.relationsById = new HashMap<Long, GrRelation>();
		return this.relationsById;
	}
	
	private List<String> getColumns() {
		if (this.columns == null) {
			this.columns = new ArrayList<String>();
			JsonObject jsres = this.queryResult.getJsonResult();
			JsonArray cols = ((JsonObject)jsres.getJsonArray("results").get(0)).getJsonArray("columns");
			int sz = cols.size();
			for (int i = 0;i < sz; i++) {
				this.columns.add(cols.getString(i));
			}
		}
		return this.columns;
	}
	
	private List<String> getUnresolvedColumns() {
		if (this.unResolvedColumns == null) {
			this.unResolvedColumns = new ArrayList<String>();
			this.unResolvedColumns.addAll(getColumns());
		}
		return this.unResolvedColumns;
	}
	
	private int getColumnIndex(String colKey) {
		List<String> cols = getColumns();
		for (int i = 0; i < cols.size(); i++) {
			if (cols.get(i).equals(colKey))
				return i;
		}
		return -1;
	}
	
	private Iterator<JsonValue> getDataIterator() {
		JsonObject jsres = this.queryResult.getJsonResult();
		JsonArray datas = ((JsonObject)jsres.getJsonArray("results").get(0)).getJsonArray("data");
		return datas.iterator();
	}
	
	private JsonArray getRestArray(JsonObject dataObject) {
		return dataObject.getJsonArray("rest");
	}
	
	private JsonObject getGraphOject(JsonObject dataObject) {
		return dataObject.getJsonObject("graph");
	}
	
	private ElementInfo getElementInfo(JsonObject dataObject, int colIdx) {
		JsonArray restArray = getRestArray(dataObject);
		JsonObject restObject = restArray.getJsonObject(colIdx);
		String selfString = restObject.getString("self");
		ElementInfo ei = ElementInfo.parse(selfString);
		return ei;
	}
	
	private RelationInfo getRelationInfo(JsonObject dataObject, int colIdx) {
		JsonArray restArray = getRestArray(dataObject);
		JsonObject restObject = restArray.getJsonObject(colIdx);
		String startString = restObject.getString("start");
		String endString = restObject.getString("end");
		RelationInfo ri = RelationInfo.parse(startString, endString);
		return ri;
	}
	
	private boolean isColumnOfType(ElemType typ, JsonObject dataObject, int colIdx) {
		JsonArray restArray = getRestArray(dataObject);
		JsonValue obj = restArray.get(colIdx);
		if (obj.getValueType() == ValueType.OBJECT) {
			JsonObject restObject = (JsonObject)obj;
			if (restObject.containsKey("self")) {
				String selfString = restObject.getString("self");
				if (selfString != null) {
					ElementInfo ei = ElementInfo.parse(selfString);
					return ei.type == typ;
				}
			}
		}
		return false;
	}
	
	private void addValue(JsonObject dataObject, int colIdx, ValueList<?> vals) {
		JsonArray restArray = getRestArray(dataObject);
		JsonValue restVal = restArray.get(colIdx);
		vals.add(restVal);
	}

	/**************************************/
	private enum ElemType {
		NODE, RELATION
	}
	
	/**************************************/
	private static class ElementInfo {
		private long id;
		private ElemType type;
		
		private static ElementInfo parse(String selfString) {
			ElementInfo ret = new ElementInfo();
			int lidx = selfString.lastIndexOf('/');
			ret.id = Long.parseLong(selfString.substring(lidx + 1));
			String preString = selfString.substring(0, lidx);
			lidx = preString.lastIndexOf('/');
			String typeString;
			if (lidx != -1)
				typeString = preString.substring(lidx + 1);
			else
				typeString = preString;
			
			if ("node".equals(typeString))
				ret.type = ElemType.NODE;
			else if ("relationship".equals(typeString))
				ret.type = ElemType.RELATION;
			
			return ret;
		}
	}
	
	/**************************************/
	private static class RelationInfo {
		private long startNodeId;
		private long endNodeId;
		
		private static RelationInfo parse(String startString, String endString) {
			RelationInfo ret = new RelationInfo();
			ret.startNodeId = ret.parseId(startString);
			ret.endNodeId = ret.parseId(endString);
			return ret;
		}
		
		private long parseId(String str) {
			int lidx = str.lastIndexOf('/');
			return Long.parseLong(str.substring(lidx + 1));
		}
	}
	
	/**************************************/
	private static class ValueList<T> {
		private List<T> values = new ArrayList<T>();
		
		private void add (JsonValue val) {
			this.addTo(val, this.values);
		}
		
		@SuppressWarnings("unchecked")
		private <E> void addTo (JsonValue val, List<E> list) {
			ValueType typ = val.getValueType();
			if (typ == ValueType.NUMBER)
				list.add((E) ((JsonNumber)val).bigDecimalValue());
			else if (typ == ValueType.STRING)
				list.add((E) ((JsonString)val).getString());
			else if (typ == ValueType.FALSE)
				list.add((E) Boolean.FALSE);
			else if (typ == ValueType.TRUE)
				list.add((E) Boolean.TRUE);
			else if (typ == ValueType.ARRAY) {
				JsonArray arr = (JsonArray)val;
				List<Object> vals = new ArrayList<Object>();
				int sz = arr.size();
				for (int i = 0; i < sz; i++) {
					JsonValue v = arr.get(i);
					this.addTo(v, vals);
				}
				list.add((E) vals);
			} else if (typ == ValueType.OBJECT) {
				//JsonObject obj = (JsonObject)val;
			}
		}
	}
}
