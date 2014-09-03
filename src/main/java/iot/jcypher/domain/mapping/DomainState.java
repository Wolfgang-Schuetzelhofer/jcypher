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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainState {

	private Map<Object, Long> object2IdMap;
	private Map<Relation, Long> relation2IdMap;
	private Map<Long, List<Object>> id2ObjectsMap;
	
	public DomainState() {
		super();
		this.object2IdMap = new HashMap<Object, Long>();
		this.relation2IdMap = new HashMap<Relation, Long>();
		this.id2ObjectsMap = new HashMap<Long, List<Object>>();
	}
	
	private void addTo_Object2IdMap(Object key, Long value) {
		this.object2IdMap.put(key, value);
	}
	
	public Long getFrom_Object2IdMap(Object key) {
		return this.object2IdMap.get(key);
	}
	
	public Object checkForMappedObject (Class<?> doClass, Long id) {
		List<Object> objs = this.getFrom_Id2ObjectsMap(id);
		for (Object obj : objs) {
			if (obj.getClass().equals(doClass)) {
				return obj;
			}
		}
		return null;
	}
	
	public void addTo_Relation2IdMap(Relation key, Long value) {
		this.relation2IdMap.put(key, value);
	}
	
	public Long getFrom_Relation2IdMap(Relation key) {
		return this.relation2IdMap.get(key);
	}
	
	public void addTo_Id2ObjectsMap(Object obj, Long id) {
		List<Object> objs = this.getFrom_Id2ObjectsMap(id);
		if (!objs.contains(obj))
			objs.add(obj);
	}
	
	public void connect_Id2Object(Object obj, Long id) {
		this.addTo_Id2ObjectsMap(obj, id);
		this.addTo_Object2IdMap(obj, id);
	}
	
	private List<Object> getFrom_Id2ObjectsMap(Long id) {
		List<Object> objs = this.id2ObjectsMap.get(id);
		if (objs == null) {
			objs = new ArrayList<Object>();
			this.id2ObjectsMap.put(id, objs);
		}
		return objs;
	}

	/***********************************/
	public static class Relation {
		private String type;
		private Object start;
		private Object end;
		
		public Relation(String type, Object start, Object end) {
			super();
			this.type = type;
			this.start = start;
			this.end = end;
		}

		public String getType() {
			return type;
		}

		public Object getStart() {
			return start;
		}

		public Object getEnd() {
			return end;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((end == null) ? 0 : end.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Relation other = (Relation) obj;
			if (end == null) {
				if (other.end != null)
					return false;
			} else if (!end.equals(other.end))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Relation [type=" + type + ", start=" + start + ", end="
					+ end + "]";
		}
	}
}
