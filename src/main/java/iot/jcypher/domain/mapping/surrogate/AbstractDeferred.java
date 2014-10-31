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
import java.util.Iterator;
import java.util.List;

public abstract class AbstractDeferred implements IDeferred{

	protected List<IDeferred> upInTree;
	protected List<IDeferred> downInTree;
	
	public AbstractDeferred() {
		super();
		this.downInTree = new ArrayList<IDeferred>();
		this.upInTree = new ArrayList<IDeferred>();
	}

	@Override
	public boolean isLeaf() {
		return this.downInTree.isEmpty();
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public Iterator<IDeferred> nextUp() {
		return this.upInTree.iterator();
	}
	
	@Override
	public void addNextUpInTree(IDeferred deferred) {
		if (!this.upInTree.contains(deferred)) {
			this.upInTree.add(deferred);
			((AbstractDeferred)deferred).addDownInTree(this);
		}
	}
	
	public void addDownInTree(IDeferred dit) {
		this.downInTree.add(dit);
	}
	
	@Override
	public void modifiedBy(IDeferred changer) {
		this.downInTree.remove(changer);
	}
	
	public void removeFromDownTree(IDeferred def) {
		this.downInTree.remove(def);
	}
	
	public void removeFromUpTree(IDeferred def) {
		this.upInTree.remove(def);
	}
	
	protected void modifyNextUp() {
		for(IDeferred def :this.upInTree) {
			def.modifiedBy(this);
		}
	}

	@Override
	public void breakLoop() {
		List<IDeferred> loopEnds = new ArrayList<IDeferred>();
		loopsTo(this, loopEnds);
		// I am in downTree of each one in loopEnds
		for (IDeferred deferred : loopEnds) {
			((AbstractDeferred)deferred).removeFromDownTree(this);
			removeFromUpTree(deferred);
		}
	}
	
	private void loopsTo(IDeferred def, List<IDeferred> loopEnds) {
		for (IDeferred deferred : this.downInTree) {
			if (deferred.equals(def))
				loopEnds.add(this);
			else
				((AbstractDeferred)deferred).loopsTo(def, loopEnds);
		}
	}
	
}
