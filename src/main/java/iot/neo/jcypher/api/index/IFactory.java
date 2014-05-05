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

package iot.neo.jcypher.api.index;

import iot.neo.jcypher.ast.index.IndexExpression;
import iot.neo.jcypher.ast.index.IndexExpression.IndexAction;

public class IFactory {

	public static IndexFor createOnLabel(String labelName) {
		IndexExpression ix = new IndexExpression(IndexAction.CREATE);
		ix.setLabelName(labelName);
		return new IndexFor(ix);
	}
	
	public static IndexFor dropOnLabel(String labelName) {
		IndexExpression ix = new IndexExpression(IndexAction.DROP);
		ix.setLabelName(labelName);
		return new IndexFor(ix);
	}
}
