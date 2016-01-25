/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
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

package iot.jcypher.query;

import java.util.ArrayList;

import iot.jcypher.query.values.JcPrimitive;
import iot.jcypher.query.values.ValueAccess;

public class LiteralMapList extends ArrayList<LiteralMap> {

	private static final long serialVersionUID = 1L;

	LiteralMapList() {
		super();
	}

	public LiteralMapList(int initialSize) {
		super(initialSize);
	}

	public LiteralMapList select(JcPrimitive key, Object value) {
		LiteralMapList ret = new LiteralMapList();
		for (LiteralMap lm : this) {
			if (value.equals(lm.get(ValueAccess.getName(key))))
				ret.add(lm);
		}
		return ret;
	}
	
	public LiteralMapList select(String key, Object value) {
		LiteralMapList ret = new LiteralMapList();
		for (LiteralMap lm : this) {
			if (value.equals(lm.get(key)))
				ret.add(lm);
		}
		return ret;
	}
	
	public LiteralMap selectFirst(JcPrimitive key, Object value) {
		for (LiteralMap lm : this) {
			if (value.equals(lm.get(ValueAccess.getName(key))))
				return lm;
		}
		return null;
	}
	
	public LiteralMap selectFirst(String key, Object value) {
		for (LiteralMap lm : this) {
			if (value.equals(lm.get(key)))
				return lm;
		}
		return null;
	}
}
