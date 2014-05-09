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

package iot.jcypher.ast.collection;

import iot.jcypher.values.JcCollection;

import java.util.List;

public class CollectionSpec {

	private List<?> collectionValues;
	private CollectExpression collection;
	private JcCollection jcCollection;
	
	public CollectionSpec(List<?> collectionValues) {
		super();
		this.collectionValues = collectionValues;
	}

	public CollectionSpec(CollectExpression collection) {
		super();
		this.collection = collection;
	}

	public CollectionSpec(JcCollection jcCollection) {
		super();
		this.jcCollection = jcCollection;
	}

	public List<?> getCollectionValues() {
		return collectionValues;
	}

	public CollectExpression getCollection() {
		return collection;
	}

	public JcCollection getJcCollection() {
		return jcCollection;
	}
}
