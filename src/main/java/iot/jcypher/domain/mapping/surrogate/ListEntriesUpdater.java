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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListEntriesUpdater extends AbstractDeferred implements IEntryUpdater {

	private Collection<?> collection;
	private List<Surrogate2ListEntry> entries;
	
	public ListEntriesUpdater(Collection<?> collection) {
		super();
		this.collection = collection;
		this.entries = new ArrayList<Surrogate2ListEntry>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void performUpdate() {
		Collections.sort(this.entries, new Comparator<Surrogate2ListEntry>() {
			@Override
			public int compare(Surrogate2ListEntry o1, Surrogate2ListEntry o2) {
				int x = o1.getIndex();
				int y = o2.getIndex();
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
		});
		if (this.collection instanceof List<?>) {
			List<Object> list = (List<Object>) this.collection;
			for (Surrogate2ListEntry entry : this.entries) {
				list.add(entry.getIndex(), entry.getSurrogate().getContent());
			}
		}
	}
	
	public void updateFrom(Surrogate2ListEntry s2ListEntry) {
		this.entries.add(s2ListEntry);
	}

	public Collection<?> getCollection() {
		return collection;
	}

	@Override
	public Object objectToUpdate() {
		return getCollection();
	}
	
	@Override
	public Object entry2Update() {
		return this;
	}
}
