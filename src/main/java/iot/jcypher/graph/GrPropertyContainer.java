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

package iot.jcypher.graph;

import java.util.List;

import iot.jcypher.result.util.ResultHandler;

public abstract class GrPropertyContainer extends GrElement {

	private long id;
	/** result column name, optional */
	private String name;
	private List<GrProperty> properties;

	GrPropertyContainer(ResultHandler resultHandler, long id, String name,
			int rowIdx) {
		super(resultHandler, rowIdx);
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public List<GrProperty> getProperties() {
		if (this.properties == null)
			this.properties = resolveProperties();
		return this.properties;
	}

	private List<GrProperty> resolveProperties() {
		if (this instanceof GrNode)
			return this.resultHandler.getNodeProperties(this.id, this.rowIndex);
		else if (this instanceof GrRelation)
			return this.resultHandler.getRelationProperties(this.id, this.rowIndex);
		return null;
	}
	
}
