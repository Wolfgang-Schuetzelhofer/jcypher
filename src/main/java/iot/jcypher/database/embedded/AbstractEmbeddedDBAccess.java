/************************************************************************
 * Copyright (c) 2014-2016 IoT-Solutions e.U.
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

package iot.jcypher.database.embedded;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.database.internal.DBUtil;
import iot.jcypher.database.internal.IDBAccessInit;
import iot.jcypher.database.util.QParamsUtil;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.writer.CypherWriter;
import iot.jcypher.query.writer.IQueryParam;
import iot.jcypher.query.writer.QueryParam;
import iot.jcypher.query.writer.QueryParamSet;
import iot.jcypher.query.writer.WriterContext;
import iot.jcypher.transaction.ITransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.neo4j.driver.v1.AuthToken;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import scala.collection.convert.Wrappers.SeqWrapper;

public abstract class AbstractEmbeddedDBAccess implements IDBAccessInit {

	private ThreadLocal<ETransactionImpl> transaction = new ThreadLocal<ETransactionImpl>();
	protected Properties properties;
	private GraphDatabaseService graphDb;
	private Thread shutdownHook;
	private boolean registerShutdownHook = true;

	public AbstractEmbeddedDBAccess() {
		super();
	}

	public AbstractEmbeddedDBAccess(GraphDatabaseService gdbs) {
		super();
		this.graphDb = gdbs;
	}

	@Override
	public JcQueryResult execute(JcQuery query) {
		List<JcQuery> qList = new ArrayList<JcQuery>();
		qList.add(query);
		List<JcQueryResult> qrList = execute(qList);
		return qrList.get(0);
	}

	@Override
	public List<JcQueryResult> execute(List<JcQuery> queries) {
		List<Statement> statements = new ArrayList<Statement>(queries.size());
		for (JcQuery query : queries) {
			WriterContext context = new WriterContext();
			QueryParam.setExtractParams(query.isExtractParams(), context);
			CypherWriter.toCypherExpression(query, context);
			String cypher = context.buffer.toString();
			Map<String, Object> paramsMap = QParamsUtil.createQueryParams(context);
			statements.add(new Statement(cypher, paramsMap));
		}
		JsonBuilderContext builderContext = new JsonBuilderContext();
		initJsonBuilderContext(builderContext);
		Throwable exception = null;
		GraphDatabaseService engine = null;
		try {
			engine = getGraphDB();
		} catch (Throwable e) {
			exception = e;
		}
		Result result = null;
		Transaction tx = null;
		Throwable dbException = null;
		if (engine != null) {
			ETransactionImpl etx = null;
			if ((etx = this.transaction.get()) != null)
				tx = etx.getTransaction();
			else
				tx = getGraphDB().beginTx();
			try {
				for (Statement statement : statements) {
					if (statement.parameterMap != null)
						result = engine.execute(statement.cypher, statement.parameterMap);
					else
						result = engine.execute(statement.cypher);

					addInnerResultsAndDataArray(builderContext);
					List<String> cols = result.columns();
					addColumns(builderContext, cols);
					while (result.hasNext()) {
						// that is one row
						Map<String, Object> row = result.next();
						addRow(builderContext, row, cols);
					}
				}
				if (etx == null)
					tx.success();
			} catch (Throwable e) {
				dbException = e;
				if (tx != null)
					tx.failure();
			} finally {
				if (etx == null && tx != null) {
					try {
						tx.close();
					} catch (Throwable e1) {
						dbException = e1;
					}
				}
			}
		}

		if (dbException != null) {
			addDBError(builderContext, dbException);
		}
		JsonObject jsonResult = builderContext.build();

		List<JcQueryResult> ret = new ArrayList<JcQueryResult>(queries.size());
		for (int i = 0; i < queries.size(); i++) {
			JcQueryResult qr = new JcQueryResult(jsonResult, i, this);
			ret.add(qr);
			if (exception != null) {
				String typ = exception.getClass().getSimpleName();
				String msg = exception.getLocalizedMessage();
				qr.addGeneralError(new JcError(typ, msg, DBUtil.getStacktrace(exception)));
			}
		}
		return ret;
	}

	@Override
	public List<JcError> clearDatabase() {
		return DBUtil.clearDatabase(this);
	}

	@Override
	public ITransaction beginTX() {
		ETransactionImpl tx = this.transaction.get();
		if (tx == null) {
			tx = new ETransactionImpl(this);
			this.transaction.set(tx);
		}
		return tx;
	}

	@Override
	public ITransaction getTX() {
		return this.transaction.get();
	}

	@Override
	public boolean isDatabaseEmpty() {
		return DBUtil.isDatabaseEmpty(this);
	}

	@Override
	public synchronized void close() {
		if (this.shutdownHook != null) {
			Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
			this.shutdownHook = null;
		}

		shutDown();
		this.graphDb = null;
	}

	@Override
	public void setAuth(String userId, String password) {
		// nop
	}

	@Override
	public void setAuth(AuthToken authToken) {
		// nop
	}

	@Override
	public IDBAccess removeShutdownHook() {
		this.registerShutdownHook = false;
		if (this.shutdownHook != null) {
			Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
			this.shutdownHook = null;
		}
		return this;
	}

	@Override
	public IDBAccess addShutdownHook() {
		this.registerShutdownHook = true;
		if (this.shutdownHook == null) {
			Thread hook = new Thread() {
				@Override
				public void run() {
					shutDown();
				}
			};
			Runtime.getRuntime().addShutdownHook(hook);
			this.shutdownHook = hook;
		}
		return this;
	}

	protected void shutDown() {
		try {
			if (this.graphDb != null)
				this.graphDb.shutdown();
		} catch (Throwable e) {
			// do nothing
		}
	}

	private void addDBError(JsonBuilderContext builderContext, Throwable exception) {
		String code = exception.getClass().getSimpleName();
		String msg = exception.getLocalizedMessage();
		JsonObjectBuilder errorObject = Json.createObjectBuilder();
		errorObject.add("code", code);
		if (msg == null)
			msg = code;
		errorObject.add("message", msg);
		errorObject.add("info", DBUtil.getStacktrace(exception));
		builderContext.errorsArray.add(errorObject);
	}

	protected abstract GraphDatabaseService createGraphDB();

	public synchronized GraphDatabaseService getGraphDB() {
		if (this.graphDb == null) {
			this.graphDb = createGraphDB();
			this.shutdownHook = registerShutdownHook();
		}
		return this.graphDb;
	}

	private void initJsonBuilderContext(JsonBuilderContext builderContext) {
		builderContext.resultObject = Json.createObjectBuilder();
		builderContext.resultsArray = Json.createArrayBuilder();
		builderContext.errorsArray = Json.createArrayBuilder();
		builderContext.innerResultsObjects = new ArrayList<JsonObjectBuilder>();
		builderContext.dataArrays = new ArrayList<JsonArrayBuilder>();
	}

	private void addInnerResultsAndDataArray(JsonBuilderContext builderContext) {
		builderContext.innerResultsObjects.add(Json.createObjectBuilder());
		builderContext.dataArrays.add(Json.createArrayBuilder());
	}

	private void addColumns(JsonBuilderContext builderContext, List<String> cols) {
		JsonArrayBuilder columns = Json.createArrayBuilder();
		for (String col : cols) {
			columns.add(col);
		}
		builderContext.innerResultsObjects.get(builderContext.innerResultsObjects.size() - 1).add("columns", columns);
	}

	private void addRow(JsonBuilderContext builderContext, Map<String, Object> row, List<String> cols) {

		JsonObjectBuilder rowObject = Json.createObjectBuilder();
		JsonArrayBuilder restArray = Json.createArrayBuilder();
		JsonObjectBuilder graphObject = Json.createObjectBuilder();
		JsonArrayBuilder nodesArray = Json.createArrayBuilder();
		JsonArrayBuilder relationsArray = Json.createArrayBuilder();

		Object restObject;
		List<NodeHolder> nodes = new ArrayList<NodeHolder>();
		List<RelationHolder> relations = new ArrayList<RelationHolder>();
		for (int i = 0; i < cols.size(); i++) {
			restObject = null;
			String key = cols.get(i);
			Object val = row.get(key);
			restObject = buildRestObject(val, nodes, relations);
			if (restObject == null)
				writeLiteralValue(val, restArray);
			else if (restObject instanceof JsonObjectBuilder)
				restArray.add((JsonObjectBuilder) restObject);
			else if (restObject instanceof JsonArrayBuilder)
				restArray.add((JsonArrayBuilder) restObject);
		}

		Collections.sort(nodes);
		Collections.sort(relations);
		for (NodeHolder nh : nodes) {
			nodesArray.add(nh.nodeObject);
		}
		for (RelationHolder rh : relations) {
			relationsArray.add(rh.relationObject);
		}

		rowObject.add("rest", restArray);
		graphObject.add("nodes", nodesArray);
		graphObject.add("relationships", relationsArray);
		rowObject.add("graph", graphObject);
		builderContext.dataArrays.get(builderContext.dataArrays.size() - 1).add(rowObject);
	}

	private Object buildRestObject(Object val, List<NodeHolder> nodes, List<RelationHolder> relations) {
		JsonObjectBuilder restObject = null;
		JsonArrayBuilder jsarr = null;

		if (val instanceof Node) {
			restObject = Json.createObjectBuilder();
			Node node = (Node) val;
			restObject.add("self", "node/".concat(String.valueOf(node.getId())));
			addNode(node, nodes);
		} else if (val instanceof Relationship) {
			restObject = Json.createObjectBuilder();
			Relationship relation = (Relationship) val;
			restObject.add("start", "node/".concat(String.valueOf(relation.getStartNode().getId())));
			restObject.add("self", "relationship/".concat(String.valueOf(relation.getId())));
			restObject.add("end", "node/".concat(String.valueOf(relation.getEndNode().getId())));
			RelationHolder rh = new RelationHolder(relation);
			if (!relations.contains(rh)) {
				RelationHolder.RelationNodes nds = rh.init(relation);
				addNode(nds.startNode, nodes);
				addNode(nds.endNode, nodes);
				relations.add(rh);
			}
		} else if (val instanceof Path) {
			restObject = Json.createObjectBuilder();
			Path path = (Path) val;
			restObject.add("start", "node/".concat(String.valueOf(path.startNode().getId())));
			JsonArrayBuilder pNodesArray = Json.createArrayBuilder();
			Iterator<Node> nit = path.nodes().iterator();
			while (nit.hasNext()) {
				Node node = nit.next();
				pNodesArray.add("node/".concat(String.valueOf(node.getId())));
				addNode(node, nodes);
			}
			restObject.add("nodes", pNodesArray);
			restObject.add("length", path.length());
			JsonArrayBuilder pRelationsArray = Json.createArrayBuilder();
			Iterator<Relationship> rit = path.relationships().iterator();
			while (rit.hasNext()) {
				Relationship relation = rit.next();
				pRelationsArray.add("relationship/".concat(String.valueOf(relation.getId())));
				RelationHolder rh = new RelationHolder(relation);
				if (!relations.contains(rh)) {
					rh.init(relation);
					// nodes have already been added for the path
					relations.add(rh);
				}
			}
			restObject.add("relationships", pRelationsArray);
			restObject.add("end", "node/".concat(String.valueOf(path.endNode().getId())));
		} else if (val instanceof SeqWrapper<?>) {
			jsarr = Json.createArrayBuilder();
			int sz = ((SeqWrapper<?>) val).size();
			boolean restFound = false;
			for (int i = 0; i < sz; i++) {
				Object obj = ((SeqWrapper<?>) val).get(i);
				Object ro = buildRestObject(obj, nodes, relations);
				if (ro instanceof JsonObjectBuilder) {
					jsarr.add((JsonObjectBuilder) ro);
					restFound = true;
				} else if (ro instanceof JsonArrayBuilder) {
					jsarr.add((JsonArrayBuilder) ro);
					restFound = true;
				}
			}
			if (!restFound)
				jsarr = null;
		}

		if (restObject != null)
			return restObject;
		if (jsarr != null)
			return jsarr;
		return null;
	}

	private static void writeLiteralValue(Object val, JsonArrayBuilder array) {
		if (val == null)
			array.addNull();
		else if (val instanceof String)
			array.add(val.toString());
		else if (val instanceof Number) {
			if (val instanceof Long)
				array.add(((Long) val).longValue());
			else if (val instanceof Integer)
				array.add(((Integer) val).intValue());
			else if (val instanceof Double)
				array.add(((Double) val).doubleValue());
			else if (val instanceof Float)
				array.add(((Float) val).floatValue());
		} else if (val instanceof Boolean) {
			array.add(((Boolean) val).booleanValue());
		} else if (val != null && val.getClass().isArray()) {
			JsonArrayBuilder jsarr = Json.createArrayBuilder();
			if (val instanceof int[]) {
				int[] arr = (int[]) val;
				for (int v : arr) {
					writeLiteralValue(v, jsarr);
				}
			} else if (val instanceof long[]) {
				long[] arr = (long[]) val;
				for (long v : arr) {
					writeLiteralValue(v, jsarr);
				}
			} else if (val instanceof float[]) {
				float[] arr = (float[]) val;
				for (float v : arr) {
					writeLiteralValue(v, jsarr);
				}
			} else if (val instanceof double[]) {
				double[] arr = (double[]) val;
				for (double v : arr) {
					writeLiteralValue(v, jsarr);
				}
			} else {
				Object[] arr = (Object[]) val;
				for (Object v : arr) {
					writeLiteralValue(v, jsarr);
				}
			}
			array.add(jsarr);
		} else if (val instanceof SeqWrapper<?>) {
			JsonArrayBuilder jsarr = Json.createArrayBuilder();
			int sz = ((SeqWrapper<?>) val).size();
			for (int i = 0; i < sz; i++) {
				Object obj = ((SeqWrapper<?>) val).get(i);
				writeLiteralValue(obj, jsarr);
			}
			array.add(jsarr);
		}
	}

	private void addNode(Node node, List<NodeHolder> nodes) {
		if (node != null) {
			NodeHolder nh = new NodeHolder(node);
			if (!nodes.contains(nh)) {
				nh.init(node);
				nodes.add(nh);
			}
		}
	}

	void removeTx() {
		this.transaction.remove();
	}

	private Thread registerShutdownHook() {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Thread hook = null;
		if (this.registerShutdownHook) {
			hook = new Thread() {
				@Override
				public void run() {
					shutDown();
				}
			};
			Runtime.getRuntime().addShutdownHook(hook);
		}
		return hook;
	}

	/*************************************************/
	private static class JsonBuilderContext {
		private JsonObjectBuilder resultObject;
		private JsonArrayBuilder resultsArray;
		private JsonArrayBuilder errorsArray;
		private List<JsonObjectBuilder> innerResultsObjects;
		private List<JsonArrayBuilder> dataArrays;

		private JsonObject build() {
			for (int i = 0; i < this.innerResultsObjects.size(); i++) {
				this.innerResultsObjects.get(i).add("data", this.dataArrays.get(i));
				this.resultsArray.add(this.innerResultsObjects.get(i));
			}
			this.resultObject.add("results", this.resultsArray);
			this.resultObject.add("errors", this.errorsArray);
			return this.resultObject.build();
		}
	}

	/*************************************************/
	private static class NodeHolder implements Comparable<NodeHolder> {

		private long id;
		private JsonObjectBuilder nodeObject;

		private NodeHolder(Node node) {
			super();
			this.id = node.getId();
		}

		private void init(Node node) {
			JsonObjectBuilder nd = Json.createObjectBuilder();
			nd.add("id", String.valueOf(node.getId()));
			JsonArrayBuilder labels = Json.createArrayBuilder();
			Iterator<Label> lblIter = node.getLabels().iterator();
			boolean hasLabels = false;
			while (lblIter.hasNext()) {
				hasLabels = true;
				Label lab = lblIter.next();
				labels.add(lab.name());
			}
			if (hasLabels)
				nd.add("labels", labels);
			JsonObjectBuilder props = Json.createObjectBuilder();
			Iterator<String> pit = node.getPropertyKeys().iterator();
			while (pit.hasNext()) {
				String pKey = pit.next();
				Object pval = node.getProperty(pKey);
				writeLiteral(pKey, pval, props);
			}
			nd.add("properties", props);
			this.nodeObject = nd;
		}

		private static void writeLiteral(String key, Object val, JsonObjectBuilder props) {
			if (val instanceof String)
				props.add(key, val.toString());
			else if (val instanceof Number) {
				if (val instanceof Long)
					props.add(key, ((Long) val).longValue());
				else if (val instanceof Integer)
					props.add(key, ((Integer) val).intValue());
				else if (val instanceof Double)
					props.add(key, ((Double) val).doubleValue());
				else if (val instanceof Float)
					props.add(key, ((Float) val).floatValue());
			} else if (val instanceof Boolean) {
				props.add(key, ((Boolean) val).booleanValue());
			} else if (val != null && val.getClass().isArray()) {
				JsonArrayBuilder jsarr = Json.createArrayBuilder();
				if (val instanceof int[]) {
					int[] arr = (int[]) val;
					for (int v : arr) {
						writeLiteralValue(v, jsarr);
					}
				} else if (val instanceof long[]) {
					long[] arr = (long[]) val;
					for (long v : arr) {
						writeLiteralValue(v, jsarr);
					}
				} else if (val instanceof float[]) {
					float[] arr = (float[]) val;
					for (float v : arr) {
						writeLiteralValue(v, jsarr);
					}
				} else if (val instanceof double[]) {
					double[] arr = (double[]) val;
					for (double v : arr) {
						writeLiteralValue(v, jsarr);
					}
				} else {
					Object[] arr = (Object[]) val;
					for (Object v : arr) {
						writeLiteralValue(v, jsarr);
					}
				}
				props.add(key, jsarr);
			}
		}

		@Override
		public int hashCode() {
			return (int) this.id;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NodeHolder)
				return ((NodeHolder) obj).id == this.id;
			return false;
		}

		@Override
		public int compareTo(NodeHolder o) {
			return Long.compare(this.id, o.id);
		}

	}

	/*************************************************/
	private static class RelationHolder implements Comparable<RelationHolder> {

		private long id;
		private JsonObjectBuilder relationObject;

		private RelationHolder(Relationship relation) {
			super();
			this.id = relation.getId();
		}

		private RelationNodes init(Relationship relation) {
			RelationNodes ret = new RelationNodes();
			JsonObjectBuilder rel = Json.createObjectBuilder();
			rel.add("id", String.valueOf(relation.getId()));
			RelationshipType typ = relation.getType();
			if (typ != null)
				rel.add("type", typ.name());
			Node sn = relation.getStartNode();
			if (sn != null) {
				rel.add("startNode", String.valueOf(sn.getId()));
				ret.startNode = sn;
			}
			Node en = relation.getEndNode();
			if (en != null) {
				rel.add("endNode", String.valueOf(en.getId()));
				ret.endNode = en;
			}
			JsonObjectBuilder props = Json.createObjectBuilder();
			Iterator<String> pit = relation.getPropertyKeys().iterator();
			while (pit.hasNext()) {
				String pKey = pit.next();
				Object pval = relation.getProperty(pKey);
				writeLiteral(pKey, pval, props);
			}
			rel.add("properties", props);
			this.relationObject = rel;
			return ret;
		}

		private void writeLiteral(String key, Object val, JsonObjectBuilder props) {
			if (val instanceof String)
				props.add(key, val.toString());
			else if (val instanceof Number) {
				if (val instanceof Long)
					props.add(key, ((Long) val).longValue());
				else if (val instanceof Integer)
					props.add(key, ((Integer) val).intValue());
				else if (val instanceof Double)
					props.add(key, ((Double) val).doubleValue());
				else if (val instanceof Float)
					props.add(key, ((Float) val).floatValue());
			} else if (val instanceof Boolean)
				props.add(key, ((Boolean) val).booleanValue());
		}

		@Override
		public int hashCode() {
			return (int) this.id;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RelationHolder)
				return ((RelationHolder) obj).id == this.id;
			return false;
		}

		@Override
		public int compareTo(RelationHolder o) {
			return Long.compare(this.id, o.id);
		}

		/*****************************************/
		private static class RelationNodes {
			private Node startNode;
			private Node endNode;
		}
	}

	/*******************************************/
	private static class Statement {
		private String cypher;
		private Map<String, Object> parameterMap;

		private Statement(String cypher, Map<String, Object> parameterMap) {
			super();
			this.cypher = cypher;
			this.parameterMap = parameterMap;
		}
	}
}
