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

import iot.jcypher.domain.mapping.surrogate.ObservableList.IObserver;

import java.util.List;

public class Array extends AbstractSurrogate {

	private Object[] a_content;
	private transient List<Object> listContent;
	private transient SurrogateState surrogateState;
	private transient int size;
	private transient Observer observer;
	
	public Array() {
		super();
		this.size = -1;
	}

	public Array(Object[] a_content) {
		this();
		this.a_content = a_content;
	}

	@Override
	public Object[] getContent() {
		if (this.a_content == null && this.listContent != null) {
			if (this.size == -1 && (this.listContent instanceof ObservableList<?>))
				throw new RuntimeException("internal error array surrogate size");
			if (this.size == -1 || this.listContent.size() == this.size)
				this.a_content = this.listContent.toArray();
			else {
				this.a_content = new Object[this.size];
				fill(this.a_content, this.listContent);
			}
			this.surrogateState.addOriginal2Surrogate(this.a_content, this);
		}
		return this.a_content;
	}
	
	private void fill(Object[] a_content2, List<Object> listContent2) {
		for (int i = 0; i < listContent2.size(); i++) {
			a_content2[i] = listContent2.get(i);
		}
	}

	public void setContent(Object[] content) {
		this.a_content = content;
	}

	public List<Object> getListContent() {
		return listContent;
	}

	public void setListContent(List<Object> listContent) {
		this.listContent = listContent;
		if (listContent instanceof ObservableList<?>) {
			if (this.observer == null)
				this.observer = new Observer();
			((ObservableList<Object>)listContent).addObserver(this.observer);
		}
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setSurrogateState(SurrogateState surrogateState) {
		this.surrogateState = surrogateState;
	}

	@Override
	public Object objectToUpdate() {
		return getListContent();
	}
	
	private class Observer implements IObserver {

		@Override
		public void changed(ObservableList<?> list) {
			if (a_content != null) {
				if (size < listContent.size())
					throw new RuntimeException("internal error array surrogate size");
				fill(a_content, listContent);
			}
		}
	}
}
