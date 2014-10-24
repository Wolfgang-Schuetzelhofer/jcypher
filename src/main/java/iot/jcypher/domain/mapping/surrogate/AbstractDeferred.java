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
	private List<IDeferred> downInTree;
	
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
	
	protected void modifyNextUp() {
		for(IDeferred def :this.upInTree) {
			def.modifiedBy(this);
		}
	}
}
