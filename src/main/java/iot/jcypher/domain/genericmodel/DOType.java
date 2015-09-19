/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.domain.genericmodel;

import java.util.ArrayList;
import java.util.List;

public class DOType {

	private long nodeId;
	
	private String name;
	private Kind kind;
	private DOType superType;
	private List<DOType> interfaces;
	private List<DOField> fields;
	private boolean buildIn;

	public DOType(String name, boolean bldIn) {
		super();
		this.name = name;
		this.fields = new ArrayList<DOField>();
		this.interfaces = new ArrayList<DOType>();
		this.nodeId = -1;
		this.buildIn = bldIn;
	}

	public String getName() {
		return name;
	}

	public List<DOField> getFields() {
		return fields;
	}

	public DOType getSuperType() {
		return superType;
	}

	public List<DOType> getInterfaces() {
		return interfaces;
	}

	public void setSuperType(DOType superType) {
		this.superType = superType;
	}

	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}
	
	public boolean isBuildIn() {
		return buildIn;
	}

	/******************************/
	public enum Kind {
		CLASS, ABSTRACT_CLASS, INTERFACE, ENUM
	}
	
}
