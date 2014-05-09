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

package iot.jcypher.writer;

import iot.jcypher.clause.ClauseType;

public class WriterContext {

	public StringBuilder buffer = new StringBuilder();
	public Format cypherFormat = Format.NONE;
	public boolean inFunction = false;
	public ClauseType currentClause;
	public ClauseType previousClause;
	private int level = 0;
	
	public void incrementLevel() {
		this.level++;
	}
	
	public void decrementLevel() {
		this.level--;
	}
	
	public String getLevelIndent() {
		String indent = new String();
		for (int i = 0; i < this.level;i++) {
			indent = indent.concat(Pretty.INDENT);
		}
		return indent;
	}
}
