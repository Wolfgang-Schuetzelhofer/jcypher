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

package test.genericmodel;

import java.util.List;

import iot.jcypher.domain.genericmodel.DOField;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.internal.DOWalker.Field;
import iot.jcypher.domain.genericmodel.internal.DOWalker.IndexedField;
import iot.jcypher.domain.genericmodel.internal.IDOVisitor;
import iot.jcypher.query.writer.Format;

public class DOToString implements IDOVisitor {

	private StringBuilder buffer;
	private Format format;
	private String indent;
	private List<DomainObject> theDomainObjects;
	
	public DOToString(Format format) {
		this(format, "  ");
	}
	
	public DOToString(Format format, String indent) {
		this.buffer = new StringBuilder();
		this.format = format;
		this.indent = indent;
	}

	@Override
	public void startVisitDomainObjects(List<DomainObject> domainObjects) {
		this.theDomainObjects = domainObjects;
		if (domainObjects.size() > 1)
			buffer.append('[');
	}

	@Override
	public void endVisitDomainObjects(List<DomainObject> domainObjects) {
		if (domainObjects.size() > 1)
			buffer.append(']');
		this.theDomainObjects = null;
	}

	@Override
	public void startVisitDomainObject(DomainObject domainObject, Field field,
			int depth) {
		String ind = initLine(depth);
		buffer.append(ind);
		DOType dot = domainObject.getDomainObjectType();
		buffer.append(dot.getName());
		buffer.append(" {");
	}

	@Override
	public void endVisitDomainObject(DomainObject domainObject, Field field,
			int depth) {
		String ind = initLine(depth);
		buffer.append(ind);
		buffer.append('}');
		if (field instanceof IndexedField) {
			if (((IndexedField) field).getIndex() < ((IndexedField) field).getSize() - 1)
				buffer.append(',');
		} else if (depth == 0 && this.theDomainObjects.indexOf(domainObject) < 
				this.theDomainObjects.size() - 1) {
			buffer.append(',');
		}
	}

	@Override
	public void startVisitField(DOField field, Object fieldValue, int depth) {
		String ind = initLine(depth);
		buffer.append(ind);
		buffer.append(field.getName());
		buffer.append(" :");
		if (field.isListOrArray())
			buffer.append(" [");
		else if (!(fieldValue instanceof DomainObject)) {
			buffer.append(' ');
			buffer.append(stringRepresentationOf(fieldValue));
		}
	}

	@Override
	public void endVisitField(DOField field, Object fieldValue, int depth) {
		if (field.isListOrArray())
			buffer.append(']');
	}

	public StringBuilder getBuffer() {
		return buffer;
	}

	private String buildIndent(int depth) {
		String ret = "";
		for (int i = 0; i < depth; i++) {
			ret = ret.concat(indent);
		}
		return ret;
	}
	
	private String stringRepresentationOf(Object obj) {
		String ret;
		if (obj == null)
			ret = "null";
		else if (obj instanceof String)
			ret = "\"".concat((String)obj).concat("\"");
		else
			ret = obj.toString();
		return ret;
	}
	
	private String initLine(int depth) {
		String ind;
		if (format != null && format != Format.NONE) {
			ind = buildIndent(depth);
			buffer.append("\n");
		} else {
			ind = " ";
		}
		return ind;
	}
}
