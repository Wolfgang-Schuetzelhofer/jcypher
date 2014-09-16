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

import iot.jcypher.domain.ResolutionDepth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainState {

	private Map<Object, LoadInfo> object2IdMap;
	private Map<IRelation, Long> relation2IdMap;
	private Map<Long, List<Object>> id2ObjectsMap;
	private Map<Object, List<IRelation>> object2RelationsMap;
	private Map<SourceField2TargetKey, List<IndexedRelation>> objectField2IndexedRelationsMap;
	
	public DomainState() {
		super();
		this.object2IdMap = new HashMap<Object, LoadInfo>();
		this.relation2IdMap = new HashMap<IRelation, Long>();
		this.id2ObjectsMap = new HashMap<Long, List<Object>>();
		this.object2RelationsMap = new HashMap<Object, List<IRelation>>();
		this.objectField2IndexedRelationsMap = new HashMap<SourceField2TargetKey, List<IndexedRelation>>();
	}
	
	private void addTo_Object2IdMap(Object key, Long value, ResolutionDepth resolutionDepth) {
		LoadInfo loadInfo = new LoadInfo();
		loadInfo.id = value;
		loadInfo.resolutionDepth = resolutionDepth;
		this.object2IdMap.put(key, loadInfo);
	}
	
	public ResolutionDepth getResolutionDepth(Object key) {
		LoadInfo loadInfo = this.object2IdMap.get(key);
		if (loadInfo != null)
			return loadInfo.resolutionDepth;
		return null;
	}
	
	public Long getIdFrom_Object2IdMap(Object key) {
		LoadInfo loadInfo = this.object2IdMap.get(key);
		if (loadInfo != null)
			return loadInfo.id;
		return null;
	}
	
	public LoadInfo getLoadInfoFrom_Object2IdMap(Object key) {
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
	
	public void add_Id2Relation(IRelation relat, Long value) {
		IRelation toPut = relat; 
		if (relat instanceof IndexedRelationToChange) {
			IndexedRelation oldOne = ((IndexedRelationToChange)relat).existingOne;
			this.relation2IdMap.remove(oldOne);
			SourceField2TargetKey key = new SourceField2TargetKey(oldOne.getStart(),
					oldOne.getType(), oldOne.getEnd());
			List<IndexedRelation> rels = this.objectField2IndexedRelationsMap.get(key);
			if (rels != null) {
				rels.remove(oldOne);
			}
			IndexedRelation newOne = new IndexedRelation(oldOne.getType(),
					((IndexedRelationToChange)relat).newIndex,
					oldOne.getStart(), oldOne.getEnd());
			toPut = newOne;
		}
		this.relation2IdMap.put(toPut, value);
		
		if (toPut instanceof IndexedRelation) {
			SourceField2TargetKey key = new SourceField2TargetKey(toPut.getStart(),
					toPut.getType(), toPut.getEnd());
			List<IndexedRelation> rels = this.objectField2IndexedRelationsMap.get(key);
			if (rels == null) {
				rels = new ArrayList<IndexedRelation>();
				this.objectField2IndexedRelationsMap.put(key, rels);
			}
			if (!rels.contains(toPut))
				rels.add((IndexedRelation) toPut);
		} else {
			List<IRelation> rels = this.object2RelationsMap.get(toPut.getStart());
			if (rels == null) {
				rels = new ArrayList<IRelation>();
				this.object2RelationsMap.put(toPut.getStart(), rels);
			}
			if (!rels.contains(toPut))
				rels.add(toPut);
		}
	}
	
	public Long getFrom_Relation2IdMap(IRelation relat) {
		IRelation key;
		if (relat instanceof IndexedRelationToChange)
			key = ((IndexedRelationToChange)relat).existingOne;
		else
			key = relat;
		return this.relation2IdMap.get(key);
	}
	
	public void addTo_Id2ObjectsMap(Object obj, Long id) {
		List<Object> objs = this.getFrom_Id2ObjectsMap(id);
		if (!objs.contains(obj))
			objs.add(obj);
	}
	
	public void add_Id2Object(Object obj, Long id, ResolutionDepth resolutionDepth) {
		this.addTo_Id2ObjectsMap(obj, id);
		this.addTo_Object2IdMap(obj, id, resolutionDepth);
	}
	
	public boolean existsRelation(IRelation relat) {
		List<IRelation> rels = this.object2RelationsMap.get(relat.getStart());
		if (rels != null) {
			for (IRelation r : rels) {
				if (r.equals(relat))
					return true;
			}
		}
		return false;
	}
	
	public List<IndexedRelation> getIndexedRelations(SourceField2TargetKey key) {
		return this.objectField2IndexedRelationsMap.get(key);
	}
	
	public IRelation findRelation(Object start, String type) {
		List<IRelation> rels = this.object2RelationsMap.get(start);
		if (rels != null) {
			for (IRelation r : rels) {
				if (r.getType().equals(type))
					return r;
			}
		}
		return null;
	}
	
	public void removeRelation(IRelation relat) {
		if (relat instanceof IndexedRelation) {
			SourceField2TargetKey key = new SourceField2TargetKey(relat.getStart(),
					relat.getType(), relat.getEnd());
			List<IndexedRelation> rels = this.objectField2IndexedRelationsMap.get(key);
			if (rels != null) {
				rels.remove(relat);
			}
		} else {
			List<IRelation> rels = this.object2RelationsMap.get(relat.getStart());
			if (rels != null) {
				rels.remove(relat);
			}
		}
		this.relation2IdMap.remove(relat);
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
	public interface IRelation {
		public String getType();
		public Object getStart();
		public Object getEnd();
	}

	/***********************************/
	public static class Relation implements IRelation {
		private String type;
		private Object start;
		private Object end;
		
		public Relation(String type, Object start, Object end) {
			super();
			this.type = type;
			this.start = start;
			this.end = end;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public Object getStart() {
			return start;
		}

		@Override
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
	
	/***********************************/
	public static class IndexedRelation extends Relation {

		private long index;
		
		public IndexedRelation(String type, long idx, Object start, Object end) {
			super(type, start, end);
			this.index = idx;
		}
		
		public long getIndex() {
			return index;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + (int) (index ^ (index >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!super.equals(obj))
				return false;
			IndexedRelation other = (IndexedRelation) obj;
			if (index != other.index)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "IndexedRelation [type=" + getType() + ", index=" + index + ", start=" + getStart() + ", end="
					+ getEnd() + "]";
		}
	}
	
	/***********************************/
	public static class IndexedRelationToChange implements IRelation {
		private IndexedRelation existingOne;
		private long newIndex;
		
		public IndexedRelationToChange(IndexedRelation existingOne, long newIndex) {
			super();
			this.existingOne = existingOne;
			this.newIndex = newIndex;
		}

		@Override
		public String getType() {
			return this.existingOne.getType();
		}

		@Override
		public Object getStart() {
			return this.existingOne.getStart();
		}

		@Override
		public Object getEnd() {
			return this.existingOne.getEnd();
		}

		public long getNewIndex() {
			return newIndex;
		}
		
	}
	
	/********************************/
	public static class SourceField2TargetKey {
		private Object source;
		private String fieldName;
		private Object target;
		
		public SourceField2TargetKey(Object src, String fieldName, Object target) {
			super();
			this.source = src;
			this.fieldName = fieldName;
			this.target = target;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((fieldName == null) ? 0 : fieldName.hashCode());
			result = prime * result
					+ ((source == null) ? 0 : source.hashCode());
			result = prime * result
					+ ((target == null) ? 0 : target.hashCode());
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
			SourceField2TargetKey other = (SourceField2TargetKey) obj;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}
	}
	
	/********************************/
	public static class LoadInfo {
		private Long id;
		private ResolutionDepth resolutionDepth;
		
		public Long getId() {
			return id;
		}
		public ResolutionDepth getResolutionDepth() {
			return resolutionDepth;
		}
		
	}
}
