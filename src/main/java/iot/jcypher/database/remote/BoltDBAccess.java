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

package iot.jcypher.database.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.util.Function;

import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.internal.DBUtil;
import iot.jcypher.database.util.QParamsUtil;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.writer.CypherWriter;
import iot.jcypher.query.writer.QueryParam;
import iot.jcypher.query.writer.WriterContext;
import iot.jcypher.transaction.ITransaction;

public class BoltDBAccess extends AbstractRemoteDBAccess {

	private static final String pathPrefix = "://";
	private static final String bolt = "bolt";
	
	private ThreadLocal<BoltTransactionImpl> transaction = new ThreadLocal<BoltTransactionImpl>();
	private Properties properties;
	private String userId;
	private String passWord;
	private Driver driver;
	private Session session;
	
	@Override
	public List<JcQueryResult> execute(List<JcQuery> queries) {
		Driver drv = getDriver();
		List<Statement> statements = new ArrayList<Statement>(queries.size());
		for (JcQuery query : queries) {
			WriterContext context = new WriterContext();
			QueryParam.setExtractParams(query.isExtractParams(), context);
			CypherWriter.toCypherExpression(query, context);
			String cypher = context.buffer.toString();
			Map<String, Object> paramsMap = QParamsUtil.createQueryParams(context);
			statements.add(new Statement(cypher, paramsMap));
		}
		
		Transaction tx;
		BoltTransactionImpl btx = this.transaction.get();
		if (btx != null)
			tx = btx.getTransaction();
		else
			tx = getSession().beginTransaction();
		
		Throwable dbException = null;
		List<JcQueryResult> ret = new ArrayList<JcQueryResult>(queries.size());
		StatementResult result;
		try {
			for (Statement statement : statements) {
				if (statement.parameterMap != null)
					result = tx.run(statement.cypher, statement.parameterMap);
				else
					result = tx.run(statement.cypher);
				ret.add(new JcQueryResult(result, this));
			}
			if (btx == null)
				tx.success();
		} catch (Throwable e) {
			dbException = e;
			if (btx != null)
				btx.failure();
			tx.failure();
		} finally {
			if (btx == null && tx != null) {
				try {
					tx.close();
				} catch(Throwable e1) {
					dbException = e1;
				}
			}
		}
		
		if (dbException != null) {
			String typ = dbException.getClass().getSimpleName();
			String msg = dbException.getLocalizedMessage();
			JcError err = new JcError(typ, msg, DBUtil.getStacktrace(dbException));
			if (ret.isEmpty()) {
				JcQueryResult res = new JcQueryResult(null, this);
				res.getDBErrors().add(err);
			} else {
				JcQueryResult res = ret.get(ret.size() - 1);
				res.getDBErrors().add(err); // the last one must have been erroneous
			}
		}
		
		return ret;
	}

	@Override
	public ITransaction beginTX() {
		BoltTransactionImpl tx = this.transaction.get();
		if (tx == null) {
			tx = new BoltTransactionImpl(this);
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DBType getDBType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(Properties properties) {
		this.properties = properties;
		if (this.properties == null)
			throw new RuntimeException("missing properties in database configuration");
		if (this.properties.getProperty(DBProperties.SERVER_ROOT_URI) == null)
			throw new RuntimeException("missing property: '" +
					DBProperties.SERVER_ROOT_URI + "' in database configuration");
		
	}

	@Override
	public void setAuth(String userId, String password) {
		this.userId = userId;
		this.passWord = password;
	}
	
	void removeTx() {
		this.transaction.remove();
	}

	private Driver getDriver() {
		if (this.driver == null) {
			String uri = this.properties.getProperty(DBProperties.SERVER_ROOT_URI);
			int idx = uri.indexOf(pathPrefix);
			if (idx >= 0)
				uri = uri.substring(idx + pathPrefix.length());
			uri = bolt.concat(pathPrefix).concat(uri);
			if (this.userId != null && this.passWord != null)
				this.driver = GraphDatabase.driver(uri, AuthTokens.basic(this.userId, this.passWord));
			else
				this.driver = GraphDatabase.driver(uri);
		}
		return this.driver;
	}
	
	public synchronized Session getSession() {
		if (this.session == null)
			this.session = getDriver().session();
		return this.session;
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
