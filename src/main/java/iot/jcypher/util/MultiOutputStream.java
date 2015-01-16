/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MultiOutputStream extends OutputStream {

	private List<OutputStream> delegates = new ArrayList<OutputStream>();

	@Override
	public void write(int toWrite) throws IOException {
		for (OutputStream out : this.delegates) {
			out.write(toWrite);
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		for (OutputStream out : this.delegates) {
			out.close();
		}
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		for (OutputStream out : this.delegates) {
			out.flush();
		}
	}
	
	public void addDelegate(OutputStream delegate) {
		if (!this.delegates.contains(delegate))
			this.delegates.add(delegate);
	}
	
	public void removeDelegate(OutputStream delegate) {
		this.delegates.remove(delegate);
	}
	
	public void removeAllDelegates() {
		this.delegates.clear();
	}
}
