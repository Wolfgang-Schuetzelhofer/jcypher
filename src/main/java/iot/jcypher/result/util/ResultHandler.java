package iot.jcypher.result.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import iot.jcypher.JcQueryResult;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.result.model.JcrAccess;
import iot.jcypher.result.model.JcrElement;
import iot.jcypher.result.model.JcrNode;
import iot.jcypher.result.model.JcrRelation;

public class ResultHandler {

	private JcQueryResult queryResult;
	private Map<Long, JcrNode> nodesById;
	private Map<Long, JcrRelation> relationsById;
	private Map<String, List<JcrNode>> nodeColumns;
	private Map<String, List<JcrRelation>> relationColumns;
	private List<String> columns;

	public ResultHandler(JcQueryResult queryResult) {
		super();
		this.queryResult = queryResult;
	}
	
	public JcrNode getNode(JcNode node) {
		String colKey =  ValueAccess.getName(node);
		List<JcrNode> rNodes = getNodeColumns().get(colKey);
		if (rNodes == null) {
			rNodes = new ArrayList<JcrNode>();
			int colIdx = getColumnIndex(colKey);
			Iterator<JsonValue> it = getDataIterator();
			while(it.hasNext()) { // iterate over rows
				JsonObject dataObject = (JsonObject) it.next();
				ElementInfo ei = getElementInfo(dataObject, colIdx);
				JcrNode rNode = getNodesById().get(ei.id);
				if (rNode == null) {
					rNode = JcrAccess.createNode(this, ei.id, colKey);
					getNodesById().put(ei.id, rNode);
				}
				rNodes.add(rNode);
			}
			getNodeColumns().put(colKey, rNodes);
		}
		if (rNodes.size() > 0)
			return rNodes.get(0);
		return null;
	}
	
	public JcrRelation getRelation(JcRelation relation) {
		String colKey =  ValueAccess.getName(relation);
		List<JcrRelation> rRelations = getRelationColumns().get(colKey);
		if (rRelations == null) {
			rRelations = new ArrayList<JcrRelation>();
			int colIdx = getColumnIndex(colKey);
			Iterator<JsonValue> it = getDataIterator();
			while(it.hasNext()) { // iterate over rows
				JsonObject dataObject = (JsonObject) it.next();
				ElementInfo ei = getElementInfo(dataObject, colIdx);
				RelationInfo ri = getRelationInfo(dataObject, colIdx);
				JcrRelation rRelation = getRelationsById().get(ei.id);
				if (rRelation == null) {
					rRelation = JcrAccess.createRelation(this, ei.id, colKey,
							ri.startNodeId, ri.endNodeId);
					getRelationsById().put(ei.id, rRelation);
				}
				rRelations.add(rRelation);
			}
			getRelationColumns().put(colKey, rRelations);
		}
		
		if (rRelations.size() > 0)
			return rRelations.get(0);
		return null;
	}

	private Map<String, List<JcrNode>> getNodeColumns() {
		if (this.nodeColumns == null)
			this.nodeColumns = new HashMap<String, List<JcrNode>>();
		return this.nodeColumns;
	}
	
	private Map<String, List<JcrRelation>> getRelationColumns() {
		if (this.relationColumns == null)
			this.relationColumns = new HashMap<String, List<JcrRelation>>();
		return this.relationColumns;
	}
	
	private Map<Long, JcrNode> getNodesById() {
		if (this.nodesById == null)
			this.nodesById = new HashMap<Long, JcrNode>();
		return this.nodesById;
	}
	
	private Map<Long, JcrRelation> getRelationsById() {
		if (this.relationsById == null)
			this.relationsById = new HashMap<Long, JcrRelation>();
		return this.relationsById;
	}
	
	private int getColumnIndex(String colKey) {
		if (this.columns == null) {
			this.columns = new ArrayList<String>();
			JsonObject jsres = this.queryResult.getJsonResult();
			JsonArray cols = ((JsonObject)jsres.getJsonArray("results").get(0)).getJsonArray("columns");
			int sz = cols.size();
			for (int i = 0;i < sz; i++) {
				this.columns.add(cols.getString(i));
			}
		}
		for (int i = 0; i < this.columns.size(); i++) {
			if (this.columns.get(i).equals(colKey))
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
	
	@SuppressWarnings("unchecked")
	public <T extends JcrElement<?>> void fillInWholeColumn(String colKey, List<T> list) {
		if (getNodeColumns().containsKey(colKey)) {
			List<JcrNode> nds = getNodeColumns().get(colKey);
			list.addAll((Collection<? extends T>) nds);
		} else if (getRelationColumns().containsKey(colKey)) {
			List<JcrRelation> rels = getRelationColumns().get(colKey);
			list.addAll((Collection<? extends T>) rels);
		}
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
}
