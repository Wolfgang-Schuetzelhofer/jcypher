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

package iot.jcypher.query.api.collection;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.ast.collection.CollectExpression;
import iot.jcypher.query.ast.collection.CollectionSpec;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcPath;

import java.util.ArrayList;
import java.util.List;


public class InCollection<T extends APIObject> extends APIObject implements ICollection<T> {

	private T connector;

	InCollection(CollectExpression cx, T connector) {
		super();
		this.astNode = cx;
		this.connector = connector;
	}
	
	@Override
	public T IN(ICollectExpression C) {
		CollectionSpec cs = new CollectionSpec(CFactory.getRootCollectExpression((APIObject)C));
		return this.in(cs);
	}
	
	@Override
	public T IN(JcCollection collection) {
		CollectionSpec cs = new CollectionSpec(collection);
		return this.in(cs);
	}
	
	@Override
	public T IN_list(Object... value) {
		ArrayList<Object> list = new ArrayList<Object>();
		Object[] val = value;
		if (value.length == 1 && value[0].getClass().isArray())
			val = (Object[]) value[0];

		for (int i = 0; i < val.length; i++)
			list.add(val[i]);
		CollectionSpec cs = new CollectionSpec(list);
		return this.in(cs);
	}
	
	@Override
	public T IN_nodes(JcPath path) {
		return this.IN(path.nodes());
	}

	@Override
	public T IN_relations(JcPath path) {
		return this.IN(path.relations());
	}

	@Override
	public T IN_labels(JcNode node) {
		return this.IN(node.labels());
	}

	private T in(CollectionSpec cs) {
		((CollectExpression)this.astNode).setCollectionToOperateOn(cs);
		return this.connector;
	}
}
