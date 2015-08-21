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

package iot.jcypher.domain.mapping.surrogate;

import iot.jcypher.domain.mapping.DomainState;
import iot.jcypher.domain.mapping.DomainState.IRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SurrogateState {

	private Map<Surrogate_Key, ReferredSurrogate<?>> map2SurrogateMap;
	
	public SurrogateState() {
		super();
		this.map2SurrogateMap = new HashMap<Surrogate_Key, ReferredSurrogate<?>>();
	}
	
	public SurrogateState createCopy(Map<IRelation, IRelation> copiedRels,
			DomainState ds) {
		SurrogateState ret = new SurrogateState();
		
		Map<Surrogate_Key, Surrogate_Key> copiedSurrks = new IdentityHashMap<Surrogate_Key, Surrogate_Key>();
		Map<ReferredSurrogate<?>, ReferredSurrogate<?>> copiedRefSurrs =
				new IdentityHashMap<ReferredSurrogate<?>, ReferredSurrogate<?>>();
		Iterator<Entry<Surrogate_Key, ReferredSurrogate<?>>> it = this.map2SurrogateMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Surrogate_Key, ReferredSurrogate<?>> entry = it.next();
			ret.map2SurrogateMap.put(copySurrks(entry.getKey(), copiedSurrks),
					copyRefSurr(entry.getValue(), copiedRefSurrs, copiedRels, ds));
		}
		
		return ret;
	}
	
	private ReferredSurrogate<?> copyRefSurr(ReferredSurrogate<?> toCopy,
			Map<ReferredSurrogate<?>, ReferredSurrogate<?>> copiedRefSurrs,
			Map<IRelation, IRelation> copiedRels,
			DomainState ds) {
		ReferredSurrogate<?> ret = copiedRefSurrs.get(toCopy);
		if (ret == null) {
			ret = new ReferredSurrogate<>(toCopy.surrogate);
			for (IRelation rel : toCopy.references) {
				IRelation crel = copyRelation(rel, copiedRels, ds);
				ret.references.add(crel);
			}
			copiedRefSurrs.put(toCopy, ret);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends IRelation> T copyRelation(T toCopy, Map<IRelation, IRelation> copiedRels,
			DomainState ds) {
		T crel = (T) copiedRels.get(toCopy);
		if (crel == null) {
			crel = (T) toCopy.createCopy(ds);
			copiedRels.put(toCopy, crel);
		}
		return crel;
	}
	
	private Surrogate_Key copySurrks(Surrogate_Key toCopy,
			Map<Surrogate_Key, Surrogate_Key> copiedSurrks) {
		Surrogate_Key ret = copiedSurrks.get(toCopy);
		if (ret == null) {
			ret = new Surrogate_Key(toCopy.original);
			copiedSurrks.put(toCopy, ret);
		}
		return ret;
	}

	private <T extends AbstractSurrogate> void addOriginal2ReferredSurrogate(Object original, ReferredSurrogate<T> refMap) {
		this.map2SurrogateMap.put(new Surrogate_Key(original), refMap);
	}
	
	@SuppressWarnings("rawtypes")
	private ReferredSurrogate getReferredSurrogate (Object original) {
		return this.map2SurrogateMap.get(new Surrogate_Key(original));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractSurrogate> void addOriginal2Surrogate(Object original, T surrogate) {
		ReferredSurrogate<T> refSurrogate = getReferredSurrogate(original);
		if (refSurrogate != null && refSurrogate.getSurrogate() != surrogate)
			throw new RuntimeException("error existing surrogate map");
		if (refSurrogate == null) {
			refSurrogate = new ReferredSurrogate<T>(surrogate);
			addOriginal2ReferredSurrogate(original, refSurrogate);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends AbstractSurrogate> T getCreateSurrogateFor (Object original,
			Class<T> surrogateClass) {
		ReferredSurrogate<T> refSurrogate = getReferredSurrogate(original);
		if (refSurrogate == null) {
			refSurrogate = new ReferredSurrogate(AbstractSurrogate.createSurrogate(original));
			addOriginal2ReferredSurrogate(original, refSurrogate);
		}
		return refSurrogate.getSurrogate();
	}
	
	public void addReference(IRelation ref) {
		Object target = ref.getEnd();
		if (target instanceof AbstractSurrogate) {
			ReferredSurrogate<?> refSurrogate = getReferredSurrogate(
					((AbstractSurrogate)target).getContent());
			if (refSurrogate != null) {
				refSurrogate.addReference(ref);
			}
		}
	}
	
	public void removeReference(IRelation ref) {
		Object target = ref.getEnd();
		if (target instanceof AbstractSurrogate) {
			ReferredSurrogate<?> refSurrogate = getReferredSurrogate(
					((AbstractSurrogate)target).getContent());
			if (refSurrogate != null) {
				refSurrogate.removeReference(ref);
			}
		}
	}
	
	public void removeUnreferenced() {
		List<Surrogate_Key> toRemove = new ArrayList<Surrogate_Key>();
		Iterator<Entry<Surrogate_Key, ReferredSurrogate<?>>> it = this.map2SurrogateMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Surrogate_Key, ReferredSurrogate<?>> entry = it.next();
			if (entry.getValue().getReferenceCount() == 0)
				toRemove.add(entry.getKey());
		}
		for(Surrogate_Key k : toRemove) {
			this.map2SurrogateMap.remove(k);
		}
	}
	
	public int size() {
		return this.map2SurrogateMap.size();
	}
	
	/********************************/
	public static class Surrogate_Key {
		private Object original;
		
		private Surrogate_Key(Object org) {
			super();
			this.original = org;
		}

		@Override
		public final boolean equals(Object o) {
	        if (!(o instanceof Surrogate_Key))
	            return false;
	        Surrogate_Key e = (Surrogate_Key)o;
	        Object k1 = this.original;
	        Object k2 = e.original;
	        return k1 == k2;
	    }

		@Override
	    public final int hashCode() {
			// cannot return the hashcode of the map or collection
			// as it changes when the content of the map or collection changes
			// this occurs during filling the map or collection
			// TODO find a better solution
			if (this.original instanceof Map<?, ?>)
				return 0;
			else
				return 1;
	    }
	}
	
	/********************************/
	public static class ReferredSurrogate<T extends AbstractSurrogate> {
		// the surrogate is either a surrogate.Map or a surrogate.Collection
		private T surrogate;
		private List<IRelation> references;
		
		public ReferredSurrogate(T surrogate) {
			super();
			this.surrogate = surrogate;
			this.references = new ArrayList<IRelation>();
		}

		public T getSurrogate() {
			return this.surrogate;
		}
		
		public void addReference(IRelation reference) {
			if (!references.contains(reference))
				references.add(reference);
		}
		
		public void removeReference(IRelation reference) {
			references.remove(reference);
		}
		
		public int getReferenceCount() {
			return references.size();
		}
	}
}
