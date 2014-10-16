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

package iot.jcypher.domain.mapping.surrogate;

import iot.jcypher.domain.mapping.DomainState.SourceField2TargetKey;
import iot.jcypher.domain.mapping.DomainState.SourceFieldKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SurrogateState {

	private Map<SurrogateMap_Key, ReferredMap> map2SurrogateMap;
	private static ThreadLocal<ChangeStore> changes =
			new ThreadLocal<ChangeStore>() {
				@Override
				protected ChangeStore initialValue() {
					return new ChangeStore();
				}
			};
	
	public SurrogateState() {
		super();
		this.map2SurrogateMap = new HashMap<SurrogateMap_Key, ReferredMap>();
	}

	private void addMap2ReferredMap(Map<Object, Object> map, ReferredMap refMap) {
		this.map2SurrogateMap.put(new SurrogateMap_Key(map), refMap);
	}
	
	private ReferredMap getReferredMap (Map<Object, Object> map) {
		return this.map2SurrogateMap.get(new SurrogateMap_Key(map));
	}
	
	public iot.jcypher.domain.mapping.surrogate.Map getCreateMapSurrogateFor (Map<Object, Object> map,
			SourceFieldKey reference) {
		ReferredMap refMap = getReferredMap(map);
		if (refMap == null) {
			refMap = new ReferredMap(new iot.jcypher.domain.mapping.surrogate.Map(map));
			addMap2ReferredMap(map, refMap);
		}
		refMap.addReference(reference);
		return refMap.getMap();
	}
	
	public void removeReference(SourceField2TargetKey ref) {
		Object target = ref.getTarget();
		if (target instanceof iot.jcypher.domain.mapping.surrogate.Map) {
			ReferredMap refMap = getReferredMap(
					((iot.jcypher.domain.mapping.surrogate.Map)target).getContent());
			if (refMap != null) {
				refMap.removeReference(ref.getSourceFieldKey());
			}
		}
	}
	
	public void startChangeSession() {
		changes.remove();
	}
	
	public void applyChanges() {
		changes.get().applyChanges();
		changes.remove();
	}
	
	/********************************/
	public static class SurrogateMap_Key {
		private Map<?, ?> map;
		
		private SurrogateMap_Key(Map<?, ?> map) {
			super();
			this.map = map;
		}

		@Override
		public final boolean equals(Object o) {
	        if (!(o instanceof SurrogateMap_Key))
	            return false;
	        SurrogateMap_Key e = (SurrogateMap_Key)o;
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
	public static class ReferredMap {
		private iot.jcypher.domain.mapping.surrogate.Map map;
		private List<SourceFieldKey> references;
		
		public ReferredMap(iot.jcypher.domain.mapping.surrogate.Map map) {
			super();
			this.map = map;
			this.references = new ArrayList<SourceFieldKey>();
		}

		public iot.jcypher.domain.mapping.surrogate.Map getMap() {
			return map;
		}
		
		public void addReference(SourceFieldKey reference) {
			if (!references.contains(reference))
				references.add(reference);
		}
		
		public void removeReference(SourceFieldKey reference) {
			references.remove(reference);
		}
		
		public int getReferenceCount() {
			return references.size();
		}
	}
	
	/********************************/
	private static class ChangeStore {
		private Set<SourceField2TargetKey> added = new HashSet<SourceField2TargetKey>();
		private Set<SourceField2TargetKey> removed = new HashSet<SourceField2TargetKey>();
		
		private void applyChanges() {
			
		}
	}
}
