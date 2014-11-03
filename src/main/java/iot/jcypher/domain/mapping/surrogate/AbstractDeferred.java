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
	public void breakLoops() {
		LoopContext context = new LoopContext();
		detectLoops(context);
	}
	
	private void detectLoops(LoopContext context) {
		context.addToPath(this);
		this.walkDown(context);
		context.removePathElement(this);
	}
	
	private void walkDown(LoopContext context) {
		int sz = this.downInTree.size();
		for (int i = sz - 1; i >= 0; i--) {
			IDeferred def = this.downInTree.get(i);
			IDeferred lpc = context.findLoopConnector(def);
			if (lpc != null) { // loop detected
				this.removeFromDownTree(lpc);
				((AbstractDeferred)lpc).removeFromUpTree(this);
			} else { // continue walk down
				((AbstractDeferred)def).detectLoops(context);
			}
		}
	}
	
	/***************************************/
	private static class LoopContext {
		private List<IDeferred> path;
		
		public LoopContext() {
			super();
			this.path = new ArrayList<IDeferred>();
		}
		
		private void addToPath(IDeferred deferred) {
			if (this.path.contains(deferred))
				throw new RuntimeException("error in add to path");
			this.path.add(deferred);
		}
		
		private void removePathElement(IDeferred deferred) {
			int level = this.path.size() - 1;
			IDeferred def = this.path.remove(level);
			if (!deferred.equals(def))
				throw new RuntimeException("error in remove from path");
		}

		private IDeferred findLoopConnector(IDeferred deferred) {
			for (IDeferred def : this.path) {
				if (def.equals(deferred)) {
					return def;
				}
			}
			return null;
		}
		
		/*******************************/
//		private class PathElement {
//			private IDeferred start;
//
//			private PathElement(IDeferred start) {
//				super();
//				this.start = start;
//			}
//			
//		}
	}
}
