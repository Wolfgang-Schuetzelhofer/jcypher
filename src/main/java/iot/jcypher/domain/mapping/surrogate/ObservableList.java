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
import java.util.List;

public class ObservableList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 1L;

	private List<IObserver> observers;
	
	public ObservableList() {
		super();
	}

	public ObservableList(Collection<? extends E> c) {
		super(c);
	}

	public ObservableList(int initialCapacity) {
		super(initialCapacity);
	}
	
	@Override
	public E set(int index, E element) {
		E ret = super.set(index, element);
		this.changed();
		return ret;
	}

	@Override
	public boolean add(E e) {
		boolean ret = super.add(e);
		this.changed();
		return ret;
	}

	@Override
	public void add(int index, E element) {
		super.add(index, element);
		this.changed();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean ret = super.addAll(c);
		this.changed();
		return ret;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean ret = super.addAll(index, c);
		this.changed();
		return ret;
	}

	public void addObserver(IObserver observer) {
		if (this.observers == null)
			this.observers = new ArrayList<ObservableList.IObserver>();
		if (!this.observers.contains(observer))
			this.observers.add(observer);
	}
	
	public void removeObserver(IObserver observer) {
		if (this.observers != null)
			this.observers.remove(observer);
	}
	
	private void changed() {
		if (this.observers != null) {
			for (IObserver observer : this.observers) {
				observer.changed(this);
			}
		}
	}

	/***********************************/
	public interface IObserver {
		public void changed(ObservableList<?> list);
	}
}
