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

package iot.neo.jcypher.ast.start;

import iot.neo.jcypher.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class IndexOrId extends ASTNode {

	private List<Long> ids;
	private String indexName;
	
	public IndexOrId(String indexName) {
		super();
		this.indexName = indexName;
	}

	public IndexOrId(long... ids) {
		super();
		this.ids = new ArrayList<Long>();
		for (int i = 0; i < ids.length; i++) {
			this.ids.add(ids[i]);
		}
	}

	public List<Long> getIds() {
		return ids;
	}

	public String getIndexName() {
		return indexName;
	}
}
