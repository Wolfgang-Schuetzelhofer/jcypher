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


public class Surrogate2ListEntry extends AbstractDeferred implements ISurrogate2Entry {

	private int index;
	private AbstractSurrogate surrogate;
	private ListEntriesUpdater listUpdater;
	
	public Surrogate2ListEntry(int index, ListEntriesUpdater listUpdater, AbstractSurrogate surrogate) {
		super();
		this.index = index;
		this.listUpdater = listUpdater;
		this.surrogate = surrogate;
	}

	@Override
	public void performUpdate() {
		this.listUpdater.updateFrom(this);
		modifyNextUp();
	}

	public AbstractSurrogate getSurrogate() {
		return surrogate;
	}

	public ListEntriesUpdater getListUpdater() {
		return listUpdater;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public void addNextUpInTree(IDeferred deferred) {
		if (!upInTree.isEmpty())
			throw new RuntimeException("can only have one parent!");
		super.addNextUpInTree(deferred);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((listUpdater == null) ? 0 : listUpdater.hashCode());
		result = prime * result
				+ index;
		result = prime * result
				+ ((surrogate == null) ? 0 : surrogate.hashCode());
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
		Surrogate2ListEntry other = (Surrogate2ListEntry) obj;
		if (listUpdater == null) {
			if (other.listUpdater != null)
				return false;
		} else if (!listUpdater.equals(other.listUpdater))
			return false;
		if (index != other.index)
			return false;
		if (surrogate != other.surrogate)
			return false;
		return true;
	}

	@Override
	public Object entry2Update() {
		return this.listUpdater;
	}
	
}
