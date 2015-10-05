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

package iot.jcypher.domain.genericmodel.internal;

import iot.jcypher.domain.genericmodel.DOField;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DomainObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DOWalker {

	private List<DomainObject> domainObjects;
	private IDOVisitor visitor;
	
	public DOWalker(DomainObject domainObject, IDOVisitor visitor) {
		List<DomainObject> dobjs = new ArrayList<DomainObject>();
		dobjs.add(domainObject);
		this.domainObjects = dobjs;
		this.visitor = visitor;
	}
	
	public DOWalker(List<DomainObject> dobjs, IDOVisitor visitor) {
		this.domainObjects = dobjs;
		this.visitor = visitor;
	}

	public void walkDOGraph() {
		this.visitor.startVisitDomainObjects(this.domainObjects);
		for (DomainObject dobj : this.domainObjects) {
			walkDO(dobj, null, 0);
		}
		this.visitor.endVisitDomainObjects(this.domainObjects);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void walkDO(DomainObject dobj, Field theField, int depth) {
		this.visitor.startVisitDomainObject(dobj, theField, depth);
		
		DOType dot = dobj.getDomainObjectType();
		List<DOField> fields = dot.getFields();
		for (DOField field : fields) {
			Object val = dobj.getFieldValue(field.getName());
			this.visitor.startVisitField(field, val, depth + 1);
			if (val instanceof DomainObject) {
				walkDO((DomainObject)val, new Field(field), depth + 2);
			} else if (val instanceof List<?>) {
				List<?> list = (List<?>)val;
				int len = list.size();
				for (int i = 0; i < len; i++) {
					Object cval = dobj.getFieldValue(field.getName(), i);
					if (cval instanceof DomainObject)
						walkDO((DomainObject)cval, new IndexedField(i, len, field), depth + 2);
				}
			} else if (val != null && val.getClass().isArray()) {
				int len = Array.getLength(val);
				for (int i = 0; i < len; i++) {
					Object aval = dobj.getFieldValue(field.getName(), i);
					if (aval instanceof DomainObject)
						walkDO((DomainObject)aval, new IndexedField(i, len, field), depth + 2);
				}
			} else if (val instanceof Map<?, ?>) { //TODO not correctly functional yet
				Map map = (Map)val;
				Iterator<Entry> it = map.entrySet().iterator();
				while(it.hasNext()) {
					Entry entry = it.next();
					if (entry.getValue() instanceof DomainObject)
						walkDO((DomainObject)entry.getValue(), new KeyedField(entry.getKey(), field), depth + 2);
				}
			}
			this.visitor.endVisitField(field, val, depth + 1);
		}
		
		this.visitor.endVisitDomainObject(dobj, theField, depth);
	}
	
	/***********************************/
	public class Field {
		private DOField field;

		private Field(DOField field) {
			super();
			this.field = field;
		}

		public DOField getField() {
			return field;
		}
		
	}
	
	/***********************************/
	public class IndexedField extends Field {
		private int size;
		private int index;

		private IndexedField(int index, int size, DOField field) {
			super(field);
			this.index = index;
			this.size = size;
		}

		public int getIndex() {
			return index;
		}

		public int getSize() {
			return size;
		}
		
	}
	
	/***********************************/
	public class KeyedField extends Field {
		private Object key;

		private KeyedField(Object key, DOField field) {
			super(field);
			this.key = key;
		}

		public Object getKey() {
			return key;
		}
		
	}
}
