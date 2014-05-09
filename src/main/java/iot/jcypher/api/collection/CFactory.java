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

package iot.jcypher.api.collection;

import iot.jcypher.api.APIObject;
import iot.jcypher.api.APIObjectAccess;
import iot.jcypher.ast.ASTNode;
import iot.jcypher.ast.collection.CollectExpression;
import iot.jcypher.ast.collection.CollectionSpec;
import iot.jcypher.ast.collection.DoEvalExpression;
import iot.jcypher.ast.collection.ExtractEvalExpression;
import iot.jcypher.ast.collection.PredicateEvalExpression;
import iot.jcypher.ast.collection.PredicateFunctionEvalExpression;
import iot.jcypher.ast.collection.PropertyEvalExpresssion;
import iot.jcypher.ast.collection.ReduceEvalExpression;
import iot.jcypher.ast.collection.CollectExpression.CollectXpressionType;
import iot.jcypher.ast.predicate.PredicateExpression;
import iot.jcypher.ast.predicate.PredicateFunction.PredicateFunctionType;
import iot.jcypher.values.JcCollection;
import iot.jcypher.values.JcValue;

public class CFactory {
	
	public static ExtractExpression EXTRACT() {
		CollectExpression collXpr = new CollectExpression();
		collXpr.setType(CollectXpressionType.EXTRACT);
		collXpr.setEvalExpression(new ExtractEvalExpression());
		return new ExtractExpression(collXpr);
	}
	
	public static EXProperty<CollectFrom> COLLECT() {
		CollectExpression collXpr = new CollectExpression();
		collXpr.setType(CollectXpressionType.COLLECT);
		collXpr.setEvalExpression(new PropertyEvalExpresssion());
		
		CollectFrom connector = new CollectFrom(collXpr);
		
		EXProperty<CollectFrom> ret = new EXProperty<CollectFrom>(collXpr, connector);
		return ret;
	}
	
	public static CFrom<CWhere> FILTER() {
		CollectExpression collXpr = new CollectExpression();
		collXpr.setType(CollectXpressionType.FILTER);
		collXpr.setEvalExpression(new PredicateEvalExpression());
		
		CWhere connector = new CWhere(collXpr);
		CFrom<CWhere> ret = new CFrom<CWhere>(collXpr, connector);
		return ret;
	}
	
	public static CFrom<ReduceTo> REDUCE() {
		CollectExpression collXpr = new CollectExpression();
		collXpr.setType(CollectXpressionType.REDUCE);
		collXpr.setEvalExpression(new ReduceEvalExpression());
		ReduceTo connector = new ReduceTo(collXpr);
		return new CFrom<ReduceTo>(collXpr, connector);
	}
	
	public static Collection TAIL() {
		CollectExpression collXpr = new CollectExpression();
		collXpr.setType(CollectXpressionType.TAIL);
		
		Collection ret = new Collection(collXpr);
		return ret;
	}
	
	public static CTerminal TAIL(JcCollection collection) {
		CollectExpression collXpr = new CollectExpression();
		collXpr.setType(CollectXpressionType.TAIL);
		
		CollectionSpec cs = new CollectionSpec(collection);
		collXpr.setCollectionToOperateOn(cs);
		CTerminal ret = new CTerminal(collXpr);
		return ret;
	}
	
	public static InCollection<CWhere> forAll(JcValue jcValue) {
		return createFor(jcValue, PredicateFunctionType.ALL);
	}
	
	public static InCollection<CWhere> forAny(JcValue jcValue) {
		return createFor(jcValue, PredicateFunctionType.ANY);
	}
	
	public static InCollection<CWhere> forSingle(JcValue jcValue) {
		return createFor(jcValue, PredicateFunctionType.SINGLE);
	}
	
	public static InCollection<CWhere> forNone(JcValue jcValue) {
		return createFor(jcValue, PredicateFunctionType.NONE);
	}
	
	public static InCollection<EachDoConcat> element(JcValue jcValue) {
		CollectExpression collXpr = new CollectExpression();
		collXpr.setType(CollectXpressionType.FOREACH);
		DoEvalExpression doEval = new DoEvalExpression();
		collXpr.setEvalExpression(doEval);
		collXpr.setIterationVariable(jcValue);
		
		EachDoConcat edc = new EachDoConcat(collXpr);
		InCollection<EachDoConcat> inColl = new InCollection<EachDoConcat>(collXpr, edc);
		return inColl;
	}
	
	private static InCollection<CWhere> createFor(JcValue jcValue, PredicateFunctionType type) {
		CollectExpression collXpr = new CollectExpression();
		collXpr.setType(CollectXpressionType.PREDICATE_FUNCTION);
		PredicateFunctionEvalExpression pfEval = new PredicateFunctionEvalExpression();
		pfEval.setType(type);
		collXpr.setEvalExpression(pfEval);
		collXpr.setIterationVariable(jcValue);
		
		CWhere cwhere = new CWhere(collXpr);
		InCollection<CWhere> inColl = new InCollection<CWhere>(collXpr, cwhere);
		return inColl;
	}
	
	public static CollectExpression getRootCollectExpression(APIObject obj) {
		CollectExpression ret = null;
		ASTNode xpr = APIObjectAccess.getAstNode(obj);
		if (xpr instanceof CollectExpression) {
			ret = (CollectExpression) xpr;
		} else if (xpr instanceof PredicateExpression) {
			PredicateExpression px = (PredicateExpression) xpr;
			while(px != null) {
				CollectExpression collXpr = px.getContainingCollectExpression();
				if (collXpr != null)
					ret = collXpr;
				px = px.getParent();
			}
		}
		
		if (ret != null) {
			CollectExpression par = ret.getParent();
			while(par != null) {
				ret = par;
				par = ret.getParent();
			}
		}
		return ret;
	}
}
