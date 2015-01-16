/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

package test.domainquery.model;

public class Area {

	private String areaCode;
	private String name;
	private AreaType areaType;
	private Area partOf;
	
	public Area() {
		super();
	}

	public Area(String areaCode, String name, AreaType areaType) {
		super();
		this.areaCode = areaCode;
		this.name = name;
		this.areaType = areaType;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public String getName() {
		return name;
	}

	public AreaType getAreaType() {
		return areaType;
	}

	public Area getPartOf() {
		return partOf;
	}

	public void setPartOf(Area partOf) {
		this.partOf = partOf;
	}

	@Override
	public String toString() {
		return "Area [name=" + name + "]";
	}
	
}
