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

package iot.jcypher.query.api.returns;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.api.APIObjectAccess;
import iot.jcypher.query.api.collection.CFactory;
import iot.jcypher.query.api.collection.ICollectExpression;
import iot.jcypher.query.api.pattern.IElement;
import iot.jcypher.query.api.pattern.XFactory;
import iot.jcypher.query.api.predicate.Concatenator;
import iot.jcypher.query.ast.collection.CollectExpression;
import iot.jcypher.query.ast.collection.CollectionSpec;
import iot.jcypher.query.ast.predicate.PredicateExpression;
import iot.jcypher.query.ast.returns.ReturnAggregate;
import iot.jcypher.query.ast.returns.ReturnBoolean;
import iot.jcypher.query.ast.returns.ReturnCollection;
import iot.jcypher.query.ast.returns.ReturnElement;
import iot.jcypher.query.ast.returns.ReturnExpression;
import iot.jcypher.query.ast.returns.ReturnPattern;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcValue;

public class RFactory {

	public static RTerminal ALL() {
		ReturnExpression rx = new ReturnExpression();
		ReturnElement elem = new ReturnElement();
		elem.setAll();
		rx.setReturnValue(elem);
		return new RTerminal(rx);
	}
	
	public static RSortable value(JcValue element) {
		ReturnExpression rx = new ReturnExpression();
		ReturnElement elem = new ReturnElement();
		elem.setElement(element);
		rx.setReturnValue(elem);
		return new RSortable(rx);
	}

	public static RElement<RElement<?>> resultOf(Concatenator predicateExpression) {
		CollectExpression cx = CFactory.getRootCollectExpression(predicateExpression);
		if (cx != null) {
			return resultOfCollection(cx);
		}
		
		ReturnExpression rx = new ReturnExpression();
		ReturnBoolean bool = new ReturnBoolean();
		PredicateExpression px = (PredicateExpression) APIObjectAccess.getAstNode(predicateExpression);
		bool.setPredicateExpression(px);
		rx.setReturnValue(bool);
		return new RElement<RElement<?>>(rx);
	}
	
	public static RElement<RElement<?>> existsPattern(IElement expression) {
		ReturnExpression rx = new ReturnExpression();
		ReturnPattern pat = new ReturnPattern();
		pat.setPatternExpression(
				XFactory.getExpression(expression));
		rx.setReturnValue(pat);
		return new RElement<RElement<?>>(rx);
	}
	
	public static RElement<RElement<?>> resultOf(ICollectExpression xpr) {
		return resultOfCollection(CFactory.getRootCollectExpression((APIObject)xpr));
	}
	
	public static RElement<RElement<?>> resultOf(JcCollection collection) {
		ReturnExpression rx = new ReturnExpression();
		ReturnCollection coll = new ReturnCollection();
		CollectionSpec cs = new CollectionSpec(collection);
		CollectExpression cx = new CollectExpression();
		cx.setCollectionToOperateOn(cs);
		coll.setCollectExpression(cx);
		rx.setReturnValue(coll);
		return new RElement<RElement<?>>(rx);
	}
	
	public static RCount count() {
		ReturnExpression rx = new ReturnExpression();
		rx.setCount();
		return new RCount(rx);
	}
	
	public static AggregateDistinct aggregate() {
		ReturnExpression rx = new ReturnExpression();
		ReturnAggregate ra = new ReturnAggregate();
		rx.setReturnValue(ra);
		return new AggregateDistinct(rx);
	}
	
	public static RDistinct DISTINCT() {
		ReturnExpression rx = new ReturnExpression();
		rx.setDistinct();
		return new RDistinct(rx);
	}
	
	private static RElement<RElement<?>> resultOfCollection(CollectExpression collXpr) {
		ReturnExpression rx = new ReturnExpression();
		ReturnCollection coll = new ReturnCollection();
		coll.setCollectExpression(collXpr);
		rx.setReturnValue(coll);
		return new RElement<RElement<?>>(rx);
	}
}
