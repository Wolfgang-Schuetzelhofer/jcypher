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
	
	// in a domain there can only exist unambiguous mappings between objects and nodes
	private Map<Long, Object> id2ObjectMap;
	private Map<Object, List<IRelation>> object2RelationsMap;
	private Map<SourceField2TargetKey, List<KeyedRelation>> objectField2KeyedRelationsMap;
	private Map<SourceFieldKey, List<KeyedRelation>> multiRelationsMap;
	private Map<MapSurrogate_Key, iot.jcypher.domain.mapping.Map> map2SurrogateMap;
	private Map<iot.jcypher.domain.mapping.Map, Map<Object, Object>> map2Map_Map;
	
	public DomainState() {
		super();
		this.object2IdMap = new HashMap<Object, LoadInfo>();
		this.relation2IdMap = new HashMap<IRelation, Long>();
		this.id2ObjectMap = new HashMap<Long, Object>();
		this.object2RelationsMap = new HashMap<Object, List<IRelation>>();
		this.objectField2KeyedRelationsMap = new HashMap<SourceField2TargetKey, List<KeyedRelation>>();
		this.multiRelationsMap = new HashMap<SourceFieldKey, List<KeyedRelation>>();
		this.map2SurrogateMap = new HashMap<MapSurrogate_Key, iot.jcypher.domain.mapping.Map>();
		this.map2Map_Map = new HashMap<iot.jcypher.domain.mapping.Map, Map<Object, Object>>();
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
	
	public void add_Id2Relation(IRelation relat, Long value) {
		IRelation toPut = relat; 
		if (relat instanceof KeyedRelationToChange) {
			KeyedRelation oldOne = ((KeyedRelationToChange)relat).existingOne;
			this.relation2IdMap.remove(oldOne);
			SourceField2TargetKey key = new SourceField2TargetKey(oldOne.getStart(),
					oldOne.getType(), oldOne.getEnd());
			List<KeyedRelation> rels = this.objectField2KeyedRelationsMap.get(key);
			if (rels != null) {
				rels.remove(oldOne);
			}
			SourceFieldKey fieldKey = key.getSourceFieldKey();
			rels = this.multiRelationsMap.get(fieldKey);
			if (rels != null) {
				rels.remove(oldOne);
			}
			KeyedRelation newOne = ((KeyedRelationToChange)relat).getNewOne();
			toPut = newOne;
		}
		this.relation2IdMap.put(toPut, value);
		
		if (toPut instanceof KeyedRelation) {
			SourceField2TargetKey key = new SourceField2TargetKey(toPut.getStart(),
					toPut.getType(), toPut.getEnd());
			List<KeyedRelation> rels = this.objectField2KeyedRelationsMap.get(key);
			if (rels == null) {
				rels = new ArrayList<KeyedRelation>();
				this.objectField2KeyedRelationsMap.put(key, rels);
			}
			if (!rels.contains(toPut))
				rels.add((KeyedRelation) toPut);
			SourceFieldKey fieldKey = key.getSourceFieldKey();
			rels = this.multiRelationsMap.get(fieldKey);
			if (rels == null) {
				rels = new ArrayList<KeyedRelation>();
				this.multiRelationsMap.put(fieldKey, rels);
			}
			if (!rels.contains(toPut))
				rels.add((KeyedRelation) toPut);
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
		if (relat instanceof KeyedRelationToChange)
			key = ((KeyedRelationToChange)relat).existingOne;
		else
			key = relat;
		return this.relation2IdMap.get(key);
	}
	
	public void addTo_Id2ObjectMap(Object obj, Long id) {
		this.id2ObjectMap.put(id, obj);
	}
	
	public void add_Id2Object(Object obj, Long id, ResolutionDepth resolutionDepth) {
		this.addTo_Id2ObjectMap(obj, id);
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
	
	public List<KeyedRelation> getKeyedRelations(SourceField2TargetKey key) {
		return this.objectField2KeyedRelationsMap.get(key);
	}
	
	public List<KeyedRelation> getKeyedRelations(SourceFieldKey key) {
		return this.multiRelationsMap.get(key);
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
		if (relat instanceof KeyedRelation) {
			SourceField2TargetKey key = new SourceField2TargetKey(relat.getStart(),
					relat.getType(), relat.getEnd());
			List<KeyedRelation> rels = this.objectField2KeyedRelationsMap.get(key);
			if (rels != null) {
				rels.remove(relat);
			}
			SourceFieldKey fieldKey = key.getSourceFieldKey();
			rels = this.multiRelationsMap.get(fieldKey);
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
	
	public Object getFrom_Id2ObjectMap(Long id) {
		return this.id2ObjectMap.get(id);
	}
	
	public void addMap2Surrogate(Map<Object, Object> map, iot.jcypher.domain.mapping.Map surrogate) {
		this.map2SurrogateMap.put(new MapSurrogate_Key(map), surrogate);
		this.map2Map_Map.put(surrogate, map);
	}
	
	public iot.jcypher.domain.mapping.Map getMapSurrogateFor (Map<Object, Object> map) {
		return this.map2SurrogateMap.get(new MapSurrogate_Key(map));
	}
	
	public Map<Object, Object> getMapForSurrogate(iot.jcypher.domain.mapping.Map surrogate) {
		return this.map2Map_Map.get(surrogate);
	}
	
	public iot.jcypher.domain.mapping.Map getCreateMapSurrogateFor (Map<Object, Object> map) {
		iot.jcypher.domain.mapping.Map surrogate = getMapSurrogateFor(map);
		if (surrogate == null) {
			surrogate = new iot.jcypher.domain.mapping.Map(map);
			addMap2Surrogate(map, surrogate);
		}
		return surrogate;
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
	public static class KeyedRelation extends Relation {

		// must be of simple type so that it can be mapped to a property
		private Object key;
		// must be either null or
		// must be of simple type so that it can be mapped to a property
		private Object value;
		
		public KeyedRelation(String type, Object key, Object start, Object end) {
			super(type, start, end);
			this.key = key;
		}
		
		public Object getKey() {
			return this.key;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			KeyedRelation other = (KeyedRelation) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "KeyedRelation [key=" + key + ", getType()=" + getType()
					+ ", getStart()=" + getStart() + ", getEnd()=" + getEnd()
					+ "]";
		}
		
	}
	
	/***********************************/
	public static class KeyedRelationToChange implements IRelation {
		private KeyedRelation existingOne;
		private KeyedRelation newOne;
		
		public KeyedRelationToChange(KeyedRelation existingOne, KeyedRelation newOne) {
			super();
			this.existingOne = existingOne;
			this.newOne = newOne;
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

		public KeyedRelation getNewOne() {
			return this.newOne;
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

		public SourceFieldKey getSourceFieldKey() {
			return new SourceFieldKey(this.source, this.fieldName);
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
	public static class SourceFieldKey {
		private Object source;
		private String fieldName;
		
		public SourceFieldKey(Object src, String fieldName) {
			super();
			this.source = src;
			this.fieldName = fieldName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((fieldName == null) ? 0 : fieldName.hashCode());
			result = prime * result
					+ ((source == null) ? 0 : source.hashCode());
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
			SourceFieldKey other = (SourceFieldKey) obj;
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
			return true;
		}
		
	}
	
	/********************************/
	public static class MapSurrogate_Key {
		private Map<?, ?> map;
		
		public MapSurrogate_Key(Map<?, ?> map) {
			super();
			this.map = map;
		}

		@Override
		public final boolean equals(Object o) {
	        if (!(o instanceof MapSurrogate_Key))
	            return false;
	        MapSurrogate_Key e = (MapSurrogate_Key)o;
	        Object k1 = this.map;
	        Object k2 = e.map;
	        return k1 == k2;
	    }

		@Override
	    public final int hashCode() {
	        return map==null   ? 0 : map.hashCode();
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
		public void setResolutionDepth(ResolutionDepth resolutionDepth) {
			this.resolutionDepth = resolutionDepth;
		}
		
	}
}
