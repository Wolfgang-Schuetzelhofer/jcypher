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

import java.util.List;

public class Array extends AbstractSurrogate {

	private Object[] a_content;
	private transient List<Object> listContent;
	private transient SurrogateState surrogateState;
	
	public Array() {
		super();
	}

	public Array(Object[] a_content) {
		super();
		this.a_content = a_content;
	}

	@Override
	public Object[] getContent() {
		if (this.a_content == null && this.listContent != null) {
			this.a_content = this.listContent.toArray();
			this.surrogateState.addOriginal2Surrogate(this.a_content, this);
		}
		return this.a_content;
	}
	
	public void setContent(Object[] content) {
		this.a_content = content;
	}

	public List<Object> getListContent() {
		return listContent;
	}

	public void setListContent(List<Object> listContent) {
		this.listContent = listContent;
	}

	public void setSurrogateState(SurrogateState surrogateState) {
		this.surrogateState = surrogateState;
	}

}
