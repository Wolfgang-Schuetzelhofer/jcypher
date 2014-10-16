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

import java.util.HashMap;
import java.util.Map;

public class SurrogateState {

	private Map<Map_Key, iot.jcypher.domain.mapping.surrogate.Map> map2SurrogateMap;
	
	public SurrogateState() {
		super();
		this.map2SurrogateMap = new HashMap<Map_Key, iot.jcypher.domain.mapping.surrogate.Map>();
	}

	public void addMap2Surrogate(Map<Object, Object> map, iot.jcypher.domain.mapping.surrogate.Map surrogate) {
		this.map2SurrogateMap.put(new Map_Key(map), surrogate);
	}
	
	public iot.jcypher.domain.mapping.surrogate.Map getMapSurrogateFor (Map<Object, Object> map) {
		return this.map2SurrogateMap.get(new Map_Key(map));
	}
	
	public iot.jcypher.domain.mapping.surrogate.Map getCreateMapSurrogateFor (Map<Object, Object> map) {
		iot.jcypher.domain.mapping.surrogate.Map surrogate = getMapSurrogateFor(map);
		if (surrogate == null) {
			surrogate = new iot.jcypher.domain.mapping.surrogate.Map(map);
			addMap2Surrogate(map, surrogate);
		}
		return surrogate;
	}
	
	/********************************/
	private static class Map_Key {
		private Map<?, ?> map;
		
		private Map_Key(Map<?, ?> map) {
			super();
			this.map = map;
		}

		@Override
		public final boolean equals(Object o) {
	        if (!(o instanceof Map_Key))
	            return false;
	        Map_Key e = (Map_Key)o;
	        Object k1 = this.map;
	        Object k2 = e.map;
	        return k1 == k2;
	    }

		@Override
	    public final int hashCode() {
	        return map==null   ? 0 : map.hashCode();
	    }
	}
}
