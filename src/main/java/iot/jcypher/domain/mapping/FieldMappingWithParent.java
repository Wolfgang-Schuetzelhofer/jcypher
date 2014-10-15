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

package iot.jcypher.domain.mapping;

import java.util.ArrayList;
import java.util.List;


public class FieldMappingWithParent extends FieldMapping {

	private List<FieldMapping> parentFields;
	
	public FieldMappingWithParent(FieldMapping source, FieldMapping parentField) {
		super(source.getField(), source.getPropertyOrRelationName());
		this.parentFields = new ArrayList<FieldMapping>();
		this.parentFields.add(parentField);
	}

	@Override
	public String getClassFieldName() {
		StringBuilder sb = new StringBuilder();
		sb.append('-');
		sb.append(getFieldName());
		sb.append('-');
		sb.append(this.getDOClassFieldName());
		return sb.toString();
	}
	
	@Override
	public String getPropertyOrRelationName() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.propertyName);
		sb.append('_');
		sb.append(this.getDOPropertyOrRelationName());
		return sb.toString();
	}
	
	public void addParentField(FieldMapping fm) {
		this.parentFields.add(fm);
	}

	@Override
	protected String getDOClassFieldName() {
		return this.parentFields.get(0).getDOClassFieldName();
	}

	@Override
	protected String getDOPropertyOrRelationName() {
		return this.parentFields.get(0).getDOPropertyOrRelationName();
	}

}
