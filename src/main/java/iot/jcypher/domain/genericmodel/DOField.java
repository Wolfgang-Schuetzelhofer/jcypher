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

public class DOField {
	
	private String name;
	private String typeName;
	private boolean buidInType;
	
	public DOField(String name, String type, boolean buildIn) {
		super();
		this.name = name;
		this.typeName = type;
		this.buidInType = buildIn;
	}

	public String getName() {
		return name;
	}

	public String getTypeName() {
		return typeName;
	}

	public boolean isBuidInType() {
		return buidInType;
	}
	
	public String asString(String indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent);
		sb.append(this.name);
		sb.append(" : ");
		sb.append(this.typeName);
		sb.append(" (buildIn: ");
		sb.append(this.buidInType);
		sb.append(')');
		return sb.toString();
	}

}
