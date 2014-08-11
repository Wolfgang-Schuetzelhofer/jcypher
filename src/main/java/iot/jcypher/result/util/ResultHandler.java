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

import iot.jcypher.JcQuery;
import iot.jcypher.JcQueryResult;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrAccess;
import iot.jcypher.graph.GrLabel;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrPath;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrPropertyContainer;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.graph.PersistableItem;
import iot.jcypher.graph.SyncState;
import iot.jcypher.graph.internal.ChangeListener;
import iot.jcypher.graph.internal.GrId;
import iot.jcypher.graph.internal.LocalId;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.pattern.Node;
import iot.jcypher.query.api.pattern.Relation;
import iot.jcypher.query.api.start.StartPoint;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.START;
import iot.jcypher.query.values.JcBoolean;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcElement;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcPath;
import iot.jcypher.query.values.JcProperty;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcString;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueWriter;
import iot.jcypher.query.writer.WriterContext;
import iot.jcypher.result.JcError;
import iot.jcypher.result.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

public class ResultHandler {

	private IDBAccess dbAccess;
	
	private Graph graph;
	private LocalElements localElements;
	private JcQueryResult queryResult;
	// needed to support multiple queries
	private int queryIndex;
	private NodeRelationListener nodeRelationListener;
	private Map<Long, GrNode> nodesById;
	// contains changed and removed (deleted) nodes
	private Map<Long, GrNode> changedNodesById;
	private Map<Long, GrRelation> relationsById;
	// contains changed and removed (deleted) relations
	private Map<Long, GrRelation> changedRelationsById;
	private Map<String, List<GrNode>> nodeColumns;
	private Map<String, List<GrRelation>> relationColumns;
	private Map<String, List<GrPath>> pathColumns;
	@SuppressWarnings("rawtypes")
	private Map<String, ValueList> valueColumns;
	private List<String> columns;
	// only needed to lookup nodes and relations
	private List<String> unResolvedColumns;

	/**
	 * construct a ResultHandler initialized with a queryResult
	 * @param queryResult
	 * @param queryIndex
	 */
	public ResultHandler(JcQueryResult queryResult, int queryIndex, IDBAccess dbAccess) {
		super();
		this.dbAccess = dbAccess;
		this.queryResult = queryResult;
		this.queryIndex = queryIndex;
		this.localElements = new LocalElements();
		this.graph = GrAccess.createGraph(this);
		GrAccess.setGraphState(this.graph, SyncState.SYNC);
	}
	
	public LocalElements getLocalElements() {
		return localElements;
	}

	public Graph getGraph() {
		return this.graph;
	}
	
	public List<GrNode> getNodes(JcNode node) {
		String colKey =  ValueAccess.getName(node);
		List<GrNode> nds = getNodes(colKey);
		nds = filterRemovedItems(nds);
		return Collections.unmodifiableList(nds);
	}
	
	private <T extends PersistableItem> List<T> filterRemovedItems(List<T> items) {
		ArrayList<T> rItems = new ArrayList<T>();
		for (T item : items) {
			if (GrAccess.getState(item) != SyncState.REMOVED)
				rItems.add(item);
		}
		return rItems;
	}

	private List<GrNode> getNodes(String colKey) {
		List<GrNode> rNodes = getNodeColumns().get(colKey);
		if (rNodes == null) {
			rNodes = new ArrayList<GrNode>();
			int colIdx = getColumnIndex(colKey);
			if (colIdx == -1)
				throw new RuntimeException("no result column: " + colKey);
			Iterator<JsonValue> it = getDataIterator();
			int rowIdx = -1;
			while(it.hasNext()) { // iterate over rows
				rowIdx++;
				JsonObject dataObject = (JsonObject) it.next();
				ElementInfo ei = getElementInfo(dataObject, colIdx);
				GrNode rNode = getNodesById().get(ei.id);
				if (rNode == null) {
					rNode = GrAccess.createNode(this, new GrId(ei.id), rowIdx);
					GrAccess.setState(rNode, SyncState.SYNC);
					GrAccess.addChangeListener(getNodeRelationListener(), rNode);
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
		List<GrRelation> rels = getRelations(colKey);
		rels = filterRemovedItems(rels);
		return Collections.unmodifiableList(rels);
	}
	
	private List<GrRelation> getRelations(String colKey) {
		List<GrRelation> rRelations = getRelationColumns().get(colKey);
		if (rRelations == null) {
			rRelations = new ArrayList<GrRelation>();
			int colIdx = getColumnIndex(colKey);
			if (colIdx == -1)
				throw new RuntimeException("no result column: " + colKey);
			Iterator<JsonValue> it = getDataIterator();
			int rowIdx = -1;
			while(it.hasNext()) { // iterate over rows
				rowIdx++;
				JsonObject dataObject = (JsonObject) it.next();
				ElementInfo ei = getElementInfo(dataObject, colIdx);
				RelationInfo ri = getRelationInfo(dataObject, colIdx);
				GrRelation rRelation = getRelationsById().get(ei.id);
				if (rRelation == null) {
					rRelation = GrAccess.createRelation(this, new GrId(ei.id),
							new GrId(ri.startNodeId), new GrId(ri.endNodeId), rowIdx);
					GrAccess.setState(rRelation, SyncState.SYNC);
					GrAccess.addChangeListener(getNodeRelationListener(), rRelation);
					getRelationsById().put(ei.id, rRelation);
				}
				rRelations.add(rRelation);
			}
			getRelationColumns().put(colKey, rRelations);
			getUnresolvedColumns().remove(colKey);
		}
		
		return rRelations;
	}
	
	public List<GrPath> getPaths(JcPath path) {
		String colKey =  ValueAccess.getName(path);
		List<GrPath> rPaths = getPathColumns().get(colKey);
		if (rPaths == null) {
			rPaths = new ArrayList<GrPath>();
			int colIdx = getColumnIndex(colKey);
			if (colIdx == -1)
				throw new RuntimeException("no result column: " + colKey);
			Iterator<JsonValue> it = getDataIterator();
			int rowIdx = -1;
			while(it.hasNext()) { // iterate over rows
				rowIdx++;
				JsonObject dataObject = (JsonObject) it.next();
				JsonObject pathObject = getPathObject(dataObject, colIdx);
				String str = pathObject.getString("start");
				long startId = Long.parseLong(str.substring(str.lastIndexOf('/') + 1));
				str = pathObject.getString("end");
				long endId = Long.parseLong(str.substring(str.lastIndexOf('/') + 1));
				JsonArray rels = pathObject.getJsonArray("relationships");
				JsonArray nodes = null;
				List<GrId> relIds = new ArrayList<GrId>();
				int sz = rels.size();
				long sid;
				long eid = startId;
				for (int i = 0; i < sz; i++) {
					String rel = rels.getString(i);
					long rid = Long.parseLong(rel.substring(rel.lastIndexOf('/') + 1));
					GrRelation rRelation = getRelationsById().get(rid);
					if (rRelation == null) {
						if (nodes == null)
							nodes = pathObject.getJsonArray("nodes");
						sid = eid;
						str = nodes.getString(i + 1);
						eid = Long.parseLong(str.substring(str.lastIndexOf('/') + 1));
						rRelation = GrAccess.createRelation(this, new GrId(rid),
								new GrId(sid), new GrId(eid), rowIdx);
						GrAccess.setState(rRelation, SyncState.SYNC);
						GrAccess.addChangeListener(getNodeRelationListener(), rRelation);
						getRelationsById().put(rid, rRelation);
					}
					relIds.add(new GrId(rid));
				}
				GrPath rPath = GrAccess.createPath(this, new GrId(startId), new GrId(endId), relIds, rowIdx);
				rPaths.add(rPath);
			}
			getPathColumns().put(colKey, rPaths);
		}
		return Collections.unmodifiableList(rPaths);
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
	
	public GrNode getNode(GrId id, int rowIdx) {
		if (id instanceof LocalId)
			return getLocalNode(id.getId());
		else
			return getNode(id.getId(), rowIdx);
	}
	
	private GrNode getLocalNode(long id) {
		return this.localElements.getNode(id);
	}

	private GrNode getNode(long id, int rowIdx) {
		GrNode rNode = getNodesById().get(id);
		if (rNode == null) {
			// first resolve unresolved columns
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
						ElementInfo ei = getElementInfo(dataObject, colIdx);
						if (ei != null) { // relation or node
							if (ElemType.NODE == ei.type) {
								isNodeColumn = true;
							}
						} else
							getUnresolvedColumns().remove(colKey);
						break;
					}
					if (isNodeColumn) {
						// resolve nodes of column
						getNodes(colKey);
						// test if node has been resolved
						rNode = getNodesById().get(id);
						if (rNode != null)
							return rNode;
					}
				}
			}
		}
		if (rNode == null) {
			rNode = GrAccess.createNode(this, new GrId(id), rowIdx);
			GrAccess.setState(rNode, SyncState.SYNC);
			GrAccess.addChangeListener(getNodeRelationListener(), rNode);
			getNodesById().put(id, rNode);
		}
		return rNode;
	}
	
	public GrRelation getRelation(GrId id) {
		if (id instanceof LocalId) {
			return getLocalRelation(id.getId());
		} else
			return getRelationsById().get(id.getId());
	}
	
	private GrRelation getLocalRelation(long id) {
		return this.localElements.getRelation(id);
	}

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
	
	public List<GrProperty> getNodeProperties(GrId nodeId, int rowIndex) {
		if (nodeId instanceof LocalId)
			return new ArrayList<GrProperty>();
		else
			return getNodeProperties(nodeId.getId(), rowIndex);
	}
	
	private List<GrProperty> getNodeProperties(long nodeId, int rowIndex) {
		List<GrProperty> props = new ArrayList<GrProperty>();
		JsonObject propertiesObject = getPropertiesObject(nodeId, rowIndex, ElemType.NODE);
		Iterator<Entry<String, JsonValue>> esIt = propertiesObject.entrySet().iterator();
		while (esIt.hasNext()) {
			Entry<String, JsonValue> entry = esIt.next();
			GrProperty prop = GrAccess.createProperty(entry.getKey());
			prop.setValue(convertJsonValue(entry.getValue()));
			GrAccess.setState(prop, SyncState.SYNC);
			props.add(prop);
		}
		return props;
	}
	
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
	
	public List<GrProperty> getRelationProperties(GrId relationId, int rowIndex) {
		if (relationId instanceof LocalId)
			return new ArrayList<GrProperty>();
		else
			return getRelationProperties(relationId.getId(), rowIndex);
	}
	
	private List<GrProperty> getRelationProperties(long relationId, int rowIndex) {
		List<GrProperty> props = new ArrayList<GrProperty>();
		JsonObject propertiesObject = getPropertiesObject(relationId, rowIndex, ElemType.RELATION);
		Iterator<Entry<String, JsonValue>> esIt = propertiesObject.entrySet().iterator();
		while (esIt.hasNext()) {
			Entry<String, JsonValue> entry = esIt.next();
			GrProperty prop = GrAccess.createProperty(entry.getKey());
			prop.setValue(convertJsonValue(entry.getValue()));
			GrAccess.setState(prop, SyncState.SYNC);
			props.add(prop);
		}
		return props;
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
	
	private Map<String, List<GrPath>> getPathColumns() {
		if (this.pathColumns == null)
			this.pathColumns = new HashMap<String, List<GrPath>>();
		return this.pathColumns;
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
			JsonArray cols = ((JsonObject)jsres.getJsonArray("results").get(
					this.queryIndex)).getJsonArray("columns");
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
		JsonArray datas = ((JsonObject)jsres.getJsonArray("results").get(
				this.queryIndex)).getJsonArray("data");
		return datas.iterator();
	}
	
	private JsonArray getRestArray(JsonObject dataObject) {
		return dataObject.getJsonArray("rest");
	}
	
	private JsonObject getDataObject(int rowIndex) {
		JsonObject jsres = this.queryResult.getJsonResult();
		JsonArray datas = ((JsonObject)jsres.getJsonArray("results").get(
				this.queryIndex)).getJsonArray("data");
		return datas.getJsonObject(rowIndex);
	}
	
	private JsonObject getGraphObject(int rowIndex) {
		JsonObject dataObject = getDataObject(rowIndex);
		return dataObject.getJsonObject("graph");
	}
	
	private RelationInfo getRelationInfo(JsonObject dataObject, int colIdx) {
		JsonArray restArray = getRestArray(dataObject);
		JsonObject restObject = restArray.getJsonObject(colIdx);
		String startString = restObject.getString("start");
		String endString = restObject.getString("end");
		RelationInfo ri = RelationInfo.parse(startString, endString);
		return ri;
	}
	
	private ElementInfo getElementInfo(JsonObject dataObject, int colIdx) {
		JsonArray restArray = getRestArray(dataObject);
		JsonValue obj = restArray.get(colIdx);
		if (obj.getValueType() == ValueType.OBJECT) {
			JsonObject restObject = (JsonObject)obj;
			if (restObject.containsKey("self")) {
				String selfString = restObject.getString("self");
				if (selfString != null) {
					ElementInfo ei = ElementInfo.parse(selfString);
					return ei;
				}
			}
		}
		return null;
	}
	
	private JsonObject getPathObject(JsonObject dataObject, int colIdx) {
		JsonArray restArray = getRestArray(dataObject);
		return restArray.getJsonObject(colIdx);
	}
	
	private void addValue(JsonObject dataObject, int colIdx, ValueList<?> vals) {
		JsonArray restArray = getRestArray(dataObject);
		JsonValue restVal = restArray.get(colIdx);
		vals.add(restVal);
	}
	
	private NodeRelationListener getNodeRelationListener() {
		if (this.nodeRelationListener == null)
			this.nodeRelationListener = new NodeRelationListener();
		return this.nodeRelationListener;
	}

	private static Object convertJsonValue(JsonValue val) {
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
				vals.add(convertJsonValue(v));
			}
			ret = vals;
		} else if (typ == ValueType.OBJECT) {
			//JsonObject obj = (JsonObject)val;
		}
		return ret;
	}
	
	/**
	 * Update the underlying database with changes made on the graph
	 * @return a list of errors, which is empty if no errors occurred
	 */
	public List<JcError> store() {
		Map<GrNode, JcNumber> createdNodeToIdMap = new HashMap<GrNode, JcNumber>();
		Map<GrRelation, JcNumber> createdRelationToIdMap = new HashMap<GrRelation, JcNumber>();
		List<JcQuery> queries = createUpdateQueries(createdNodeToIdMap, createdRelationToIdMap);
		List<JcError> errors = new ArrayList<JcError>();
		if (queries.size() > 0) {
			List<JcQueryResult> results = dbAccess.execute(queries);
//			Util.printResults(results, "UPDATE", Format.PRETTY_1);
			errors.addAll(Util.collectErrors(results));
			if (errors.isEmpty()) { // success
				this.setToSynchronized(results, createdNodeToIdMap,
						createdRelationToIdMap);
			}
		}
		return errors;
	}
	
	/**
	 * create a list of queries which would apply the changes of the graph to the
	 * underlying database. You can use it to have a look which queries will be executed
	 * by a store operation.
	 * @return a list of JcQueries
	 */
	public List<JcQuery> createUpdateQueries() {
		Map<GrNode, JcNumber> createdNodeToIdMap = new HashMap<GrNode, JcNumber>();
		Map<GrRelation, JcNumber> createdRelationToIdMap = new HashMap<GrRelation, JcNumber>();
		return createUpdateQueries(createdNodeToIdMap, createdRelationToIdMap);
	}
	
	private List<JcQuery> createUpdateQueries(Map<GrNode, JcNumber> createdNodeToIdMap,
			Map<GrRelation, JcNumber> createdRelationToIdMap) {
		QueryBuilder queryBuilder = new QueryBuilder();
		List<JcQuery> queries = queryBuilder.buildUpdateAndRemoveQueries();
		queries.add(queryBuilder.buildCreateQuery(createdNodeToIdMap,
				createdRelationToIdMap));
//		Util.printQueries(queries, "UPDATE", Format.PRETTY_1);
		return queries;
	}
	
	private void setToSynchronized(List<JcQueryResult> results,
			Map<GrNode, JcNumber> createdNodeToIdMap,
			Map<GrRelation, JcNumber> createdRelationToIdMap) {
		
		List<Long> toRemove = new ArrayList<Long>();
		Iterator<Entry<Long, GrNode>> nbyId = this.getNodesById().entrySet().iterator();
		while(nbyId.hasNext()) {
			Entry<Long, GrNode> entry = nbyId.next();
			checkRemovedSetSynchronized(toRemove, entry.getValue(), entry.getKey());
		}
		for (Long id : toRemove) {
			this.getNodesById().remove(id);
		}
		this.changedNodesById = null;
		
		toRemove.clear();
		Iterator<Entry<Long, GrRelation>> rbyId = this.getRelationsById().entrySet().iterator();
		while(rbyId.hasNext()) {
			Entry<Long, GrRelation> entry = rbyId.next();
			checkRemovedSetSynchronized(toRemove, entry.getValue(), entry.getKey());
		}
		for (Long id : toRemove) {
			this.getRelationsById().remove(id);
		}
		this.changedRelationsById = null;
		
		JcQueryResult createResult = results.get(results.size() - 1);
		Iterator<Entry<GrNode, JcNumber>> nit = createdNodeToIdMap.entrySet().iterator();
		while(nit.hasNext()) {
			Entry<GrNode, JcNumber> entry = nit.next();
			long id = exchangeGrId(entry.getKey(), entry.getValue(), createResult);
			GrAccess.setToSynchronized(entry.getKey());
			this.getNodesById().put(Long.valueOf(id), entry.getKey());
		}
		
		Iterator<Entry<GrRelation, JcNumber>> rit = createdRelationToIdMap.entrySet().iterator();
		while (rit.hasNext()) {
			Entry<GrRelation, JcNumber> entry = rit.next();
			long id = exchangeGrId(entry.getKey(), entry.getValue(), createResult);
			GrAccess.setToSynchronized(entry.getKey());
			this.getRelationsById().put(Long.valueOf(id), entry.getKey());
		}
		this.localElements.clear();
		
		GrAccess.setGraphState(getGraph(), SyncState.SYNC);
		
	}
	
	private void checkRemovedSetSynchronized(List<Long> toRemove,
			PersistableItem item, Long id) {
		if (GrAccess.getState(item) == SyncState.REMOVED)
			toRemove.add(id);
		else if (GrAccess.getState(item) != SyncState.SYNC)
			GrAccess.setToSynchronized(item);
	}
	
	/**
	 * @param pc
	 * @param jcId
	 * @param createResult
	 * @return the id
	 */
	private long exchangeGrId(GrPropertyContainer pc, JcNumber jcId,
			JcQueryResult createResult) {
		List<BigDecimal> bdIds = createResult.resultOf(jcId);
		long id = bdIds.get(0).longValue();
		GrId grId = new GrId(id);
		GrAccess.setGrId(grId, pc);
		return id;
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
		
		@SuppressWarnings("unchecked")
		private void add (JsonValue val) {
			Object v = convertJsonValue(val);
			if (v != null)
				this.values.add((T) v);
		}
	}
	
	/**************************************/
	private class NodeRelationListener implements ChangeListener {

		@Override
		public void changed(Object theChanged, SyncState oldState,
				SyncState newState) {
			boolean possiblyReturnedToSync = false;
			
			if (newState == SyncState.CHANGED || newState == SyncState.REMOVED) {
				if (theChanged instanceof GrNode) {
					if (changedNodesById == null)
						changedNodesById = new HashMap<Long, GrNode>();
					changedNodesById.put(((GrNode)theChanged).getId(), (GrNode)theChanged);
				} else if (theChanged instanceof GrRelation) {
					if (changedRelationsById == null)
						changedRelationsById = new HashMap<Long, GrRelation>();
					changedRelationsById.put(((GrRelation)theChanged).getId(), (GrRelation)theChanged);
				}
				if (GrAccess.getGraphState(getGraph()) == SyncState.SYNC)
					GrAccess.setGraphState(getGraph(), SyncState.CHANGED);
			} else if (newState == SyncState.SYNC) {
				if (theChanged instanceof GrNode) {
					if (changedNodesById != null)
						changedNodesById.remove(((GrNode)theChanged).getId());
				} else if (theChanged instanceof GrRelation) {
					if (changedRelationsById != null)
						changedRelationsById.remove(((GrRelation)theChanged).getId());
				}
			}  else if (newState == SyncState.NEW) {
				if (GrAccess.getGraphState(getGraph()) == SyncState.SYNC)
					GrAccess.setGraphState(getGraph(), SyncState.CHANGED);
			}  else if (newState == SyncState.NEW_REMOVED) {
				if (theChanged instanceof GrNode) {
					localElements.removeNode(((GrNode)theChanged).getId());
				} else if (theChanged instanceof GrRelation) {
					localElements.removeRelation(((GrRelation)theChanged).getId());
				}
			}
			
			if (newState == SyncState.SYNC || newState == SyncState.NEW_REMOVED) {
				if ((changedNodesById == null || changedNodesById.size() == 0) &&
						(changedRelationsById == null || changedRelationsById.size() == 0) &&
						localElements.isEmpty())
					possiblyReturnedToSync = true;
			}
			
			if (possiblyReturnedToSync && GrAccess.getGraphState(getGraph()) == SyncState.CHANGED) {
				GrAccess.setGraphState(getGraph(), SyncState.SYNC);
			}
		}
	}
	
	/**************************************/
	private class QueryBuilder {
		
		/**
		 * @param createdNodeIds
		 * @return a Query to create elements
		 */
		JcQuery buildCreateQuery(Map<GrNode, JcNumber> createdNodeToIdMap,
				Map<GrRelation, JcNumber> createdRelationToIdMap) {
			List<GrNode2JcNode> nodesToCreate = new ArrayList<GrNode2JcNode>();
			List<IClause> createNodeClauses = new ArrayList<IClause>();
			Map<GrNode, JcNode> localNodeMap = new HashMap<GrNode, JcNode>();
			for (GrNode node : localElements.getLocalNodes()) {
				addCreateNodeClause(node, createNodeClauses,
						localNodeMap, nodesToCreate);
			}
			
			List<GrRelation2JcRelation> relationsToCreate = new ArrayList<GrRelation2JcRelation>();
			List<IClause> startNodeClauses = new ArrayList<IClause>();
			List<IClause> createRelationClauses = new ArrayList<IClause>();
			Map<GrNode, JcNode> dbNodeMap = new HashMap<GrNode, JcNode>();
			for (GrRelation relation : localElements.getLocalRelations()) {
				addCreateRelationClause(relation, createRelationClauses,
						localNodeMap, dbNodeMap, startNodeClauses, relationsToCreate);
			}
			List<IClause> clauses = startNodeClauses;
			clauses.addAll(createNodeClauses);
			clauses.addAll(createRelationClauses);
			for (GrNode2JcNode grn2jcn : nodesToCreate) {
				JcNumber nid = new JcNumber("NID_".concat(ValueAccess.getName(grn2jcn.jcNode)));
				createdNodeToIdMap.put(grn2jcn.grNode, nid);
				clauses.add(RETURN.value(grn2jcn.jcNode.id()).AS(nid));
			}
			for (GrRelation2JcRelation grr2jcr : relationsToCreate) {
				JcNumber rid = new JcNumber("RID_".concat(ValueAccess.getName(grr2jcr.jcRelation)));
				createdRelationToIdMap.put(grr2jcr.grRelation, rid);
				clauses.add(RETURN.value(grr2jcr.jcRelation.id()).AS(rid));
			}
			IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
			JcQuery ret = new JcQuery();
			ret.setClauses(clausesArray);
			return ret;
		}
		
		List<JcQuery> buildUpdateAndRemoveQueries() {
			List<JcQuery> ret = new ArrayList<JcQuery>();
			List<GrPropertyContainer> removedNodes = new ArrayList<GrPropertyContainer>();
			if (changedNodesById != null) {
				Iterator<GrNode> nit = changedNodesById.values().iterator();
				while (nit.hasNext()) {
					GrNode node = nit.next();
					SyncState state = GrAccess.getState(node);
					if (state == SyncState.CHANGED) {
						ret.add(buildChangedNodeOrRelationQuery(node));
					} else if (state == SyncState.REMOVED) {
						removedNodes.add(node);
					}
				}
			}
			
			List<GrPropertyContainer> removedRelations = new ArrayList<GrPropertyContainer>();
			if (changedRelationsById != null) {
				Iterator<GrRelation> rit = changedRelationsById.values().iterator();
				while (rit.hasNext()) {
					GrRelation relation = rit.next();
					SyncState state = GrAccess.getState(relation);
					if (state == SyncState.CHANGED) {
						ret.add(buildChangedNodeOrRelationQuery(relation));
					} else if (state == SyncState.REMOVED) {
						removedRelations.add(relation);
					}
				}
			}
			
			if (removedRelations.size() > 0)
				ret.add(buildRemovedNodeOrRelationQuery(removedRelations));
			if (removedNodes.size() > 0)
				ret.add(buildRemovedNodeOrRelationQuery(removedNodes));
			
			return ret;
		}
		
		private void addCreateNodeClause(GrNode node,
				List<IClause> clauses, Map<GrNode, JcNode> localNodeMap,
				List<GrNode2JcNode> nodesToCreate) {
			String nm = "ln_".concat(String.valueOf(clauses.size()));
			JcNode n = new JcNode(nm);
			nodesToCreate.add(new GrNode2JcNode(node, n));
			Node create = CREATE.node(n);
			for (GrLabel label : node.getLabels()) {
				create = create.label(label.getName());
			}
			for (GrProperty prop : node.getProperties()) {
				create = create.property(prop.getName()).value(prop.getValue());
			}
			clauses.add(create);
			localNodeMap.put(node, n);
		}

		private void addCreateRelationClause(GrRelation relation,
				List<IClause> createRelationClauses,
				Map<GrNode, JcNode> localNodeMap, Map<GrNode, JcNode> dbNodeMap,
				List<IClause> startNodeClauses, List<GrRelation2JcRelation> relationsToCreate) {
			String nm = "lr_".concat(String.valueOf(createRelationClauses.size()));
			JcRelation r = new JcRelation(nm);
			relationsToCreate.add(new GrRelation2JcRelation(relation, r));
			GrNode sNode = relation.getStartNode();
			GrNode eNode = relation.getEndNode();
			JcNode sn = getNode(sNode, localNodeMap, dbNodeMap, startNodeClauses);
			JcNode en = getNode(eNode, localNodeMap, dbNodeMap, startNodeClauses);
			Relation create = CREATE.node(sn).relation(r).out();
			if (relation.getType() != null)
				create = create.type(relation.getType());
			for (GrProperty prop : relation.getProperties()) {
				create = create.property(prop.getName()).value(prop.getValue());
			}
			createRelationClauses.add(create.node(en));
		}
		
		private JcNode getNode(GrNode grNode, Map<GrNode, JcNode> localNodeMap,
				Map<GrNode, JcNode> dbNodeMap, List<IClause> startNodeClauses) {
			GrId grId = GrAccess.getGrId(grNode);
			JcNode n;
			if (grId instanceof LocalId) {
				n = localNodeMap.get(grNode);
			} else {
				n = dbNodeMap.get(grNode);
				if (n == null) {
					String nm = "rn_".concat(String.valueOf(startNodeClauses.size()));
					n = new JcNode(nm);
					StartPoint start = START.node(n).byId(grId.getId());
					startNodeClauses.add(start);
					dbNodeMap.put(grNode, n);
				}
			}
			return n;
		}

		private JcQuery buildRemovedNodeOrRelationQuery(List<GrPropertyContainer> elements) {
			List<IClause> clauses = new ArrayList<IClause>();
			List<String> elemNames = new ArrayList<String>(elements.size());
			for (int i = 0; i < elements.size(); i++) {
				String nm = "elem_".concat(String.valueOf(i));
				elemNames.add(nm);
				if (elements.get(0) instanceof GrNode) {
					JcNode elem = new JcNode(nm);
					clauses.add(START.node(elem).byId(elements.get(i).getId()));
				} else if (elements.get(0) instanceof GrRelation) {
					JcRelation elem = new JcRelation(nm);
					clauses.add(START.relation(elem).byId(elements.get(i).getId()));
				}
			}
			for (String nm : elemNames) {
				JcElement elem = null;
				if (elements.get(0) instanceof GrNode)
					elem = new JcNode(nm);
				else if (elements.get(0) instanceof GrRelation)
					elem = new JcRelation(nm);
				clauses.add(DO.DELETE(elem));
			}
			
			
			IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
			JcQuery query = new JcQuery();
			//query.setExtractParams(false);
			query.setClauses(clausesArray);
			return query;
		}

		private JcQuery buildChangedNodeOrRelationQuery(GrPropertyContainer element) {
			List<IClause> clauses = new ArrayList<IClause>();
			clauses.add(buildStartClause(element));
			clauses.addAll(buildChangedPropertiesClauses(element));
			clauses.addAll(buildChangedLabelsClauses(element));
			IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
			JcQuery query = new JcQuery();
			query.setClauses(clausesArray);
			return query;
		}

		private Collection<? extends IClause> buildChangedLabelsClauses(
				GrPropertyContainer element) {
			List<IClause> ret = new ArrayList<IClause>();
			if (element instanceof GrNode) {
				GrNode node = (GrNode)element;
				List<GrLabel> modified = GrAccess.getModifiedLabels(node);
				Iterator<GrLabel> lit = modified.iterator();
				while (lit.hasNext()) {
					GrLabel lab = lit.next();
					SyncState state = GrAccess.getState(lab);
					JcNode elem = new JcNode("elem");
					IClause c = null;
					// a label can only be created and added or it can be removed
					// but a label can never be changed
					if (state == SyncState.NEW) {
						c = DO.SET(elem.label(lab.getName()));
					} else if (state == SyncState.REMOVED) {
						c = DO.REMOVE(elem.label(lab.getName()));
					}
					ret.add(c);
				}
			}
			return ret;
		}

		private Collection<? extends IClause> buildChangedPropertiesClauses(
				GrPropertyContainer element) {
			List<IClause> ret = new ArrayList<IClause>();
			List<GrProperty> modified = GrAccess.getModifiedProperties(element);
			Iterator<GrProperty> pit = modified.iterator();
			while(pit.hasNext()) {
				GrProperty prop = pit.next();
				SyncState state = GrAccess.getState(prop);
				JcElement elem = null;
				if (element instanceof GrNode)
					elem = new JcNode("elem");
				else
					elem = new JcRelation("elem");
				
				IClause c = null;
				if (state == SyncState.CHANGED || state == SyncState.NEW) {
					c = DO.SET(elem.property(prop.getName())).to(prop.getValue());
				} else if (state == SyncState.REMOVED) {
					c = DO.REMOVE(elem.property(prop.getName()));
				}
				ret.add(c);
			}
			return ret;
		}

		private IClause buildStartClause(GrPropertyContainer element) {
			long id = element.getId();
			IClause ret = null;
			if (element instanceof GrNode) {
				JcNode elem = new JcNode("elem");
				ret = START.node(elem).byId(id);
			} else if (element instanceof GrRelation) {
				JcRelation elem = new JcRelation("elem");
				ret = START.relation(elem).byId(id);
			}
			return ret;
		}
		
		/*************************************/
		private class GrNode2JcNode {
			private GrNode grNode;
			private JcNode jcNode;
			
			private GrNode2JcNode(GrNode grNode, JcNode jcNode) {
				super();
				this.grNode = grNode;
				this.jcNode = jcNode;
			}
		}
		
		/*************************************/
		private class GrRelation2JcRelation {
			private GrRelation grRelation;
			private JcRelation jcRelation;
			
			private GrRelation2JcRelation(GrRelation grRelation, JcRelation jcRelation) {
				super();
				this.grRelation = grRelation;
				this.jcRelation = jcRelation;
			}
		}
	}
	
	/**************************************/
	public class LocalElements {
		private LocalIdBuilder nodeIdBuilder;
		private LocalIdBuilder relationIdBuilder;
		
		private Map<Long, GrNode> localNodesById;
		private Map<Long, GrRelation> localRelationsById;
		
		public GrNode createNode() {
			if (this.nodeIdBuilder == null)
				this.nodeIdBuilder = new LocalIdBuilder();
			LocalId lid =new LocalId(this.nodeIdBuilder.getId());
			GrNode node = GrAccess.createNode(ResultHandler.this, lid, -1);
			if (this.localNodesById == null)
				this.localNodesById = new HashMap<Long, GrNode>();
			GrAccess.addChangeListener(getNodeRelationListener(), node);
			this.localNodesById.put(lid.getId(), node);
			GrAccess.notifyState(node);
			return node;
		}
		
		public GrRelation createRelation(String type, GrNode startNode, GrNode endNode) {
			if (this.relationIdBuilder == null)
				this.relationIdBuilder = new LocalIdBuilder();
			LocalId lid =new LocalId(this.relationIdBuilder.getId());
			GrRelation relation = GrAccess.createRelation(ResultHandler.this, lid,
					GrAccess.getGrId(startNode), GrAccess.getGrId(endNode), type);
			if (this.localRelationsById == null)
				this.localRelationsById = new HashMap<Long, GrRelation>();
			GrAccess.addChangeListener(getNodeRelationListener(), relation);
			this.localRelationsById.put(lid.getId(), relation);
			GrAccess.notifyState(relation);
			return relation;
		}
		
		private GrNode getNode(long id) {
			if (this.localNodesById != null)
				return this.localNodesById.get(id);
			return null;
		}
		
		private GrRelation getRelation(long id) {
			if (this.localRelationsById != null)
				return this.localRelationsById.get(id);
			return null;
		}
		
		private void removeNode(long id) {
			if (this.localNodesById != null)
				this.localNodesById.remove(id);
		}
		
		private void removeRelation(long id) {
			if (this.localRelationsById != null)
				this.localRelationsById.remove(id);
		}
		
		private List<GrNode> getLocalNodes() {
			List<GrNode> ret = new ArrayList<GrNode>();
			if (this.localNodesById != null) {
				for (GrNode node : this.localNodesById.values()) {
					ret.add(node);
				}
			}
			return ret;
		}
		
		private List<GrRelation> getLocalRelations() {
			List<GrRelation> ret = new ArrayList<GrRelation>();
			if (this.localRelationsById != null) {
				for (GrRelation relation : this.localRelationsById.values()) {
					ret.add(relation);
				}
			}
			return ret;
		}
		
		private void clear() {
			this.nodeIdBuilder = null;
			this.localNodesById = null;
			this.relationIdBuilder = null;
			this.localRelationsById = null;
		}
		
		public boolean isEmpty() {
			return (this.localNodesById == null || this.localNodesById.size() == 0) &&
					(this.localRelationsById == null || this.localRelationsById.size() == 0);
		}
	}
}
