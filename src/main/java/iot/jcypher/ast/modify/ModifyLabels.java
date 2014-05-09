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

package iot.jcypher.ast.modify;

import iot.jcypher.values.JcNode;

import java.util.ArrayList;
import java.util.List;

public class ModifyLabels {

	private JcNode targetNode;
	private List<String> labels = new ArrayList<String>();

	public List<String> getLabels() {
		return labels;
	}
	
	public void addLabel(String label) {
		this.labels.add(label);
	}
	
	public void addLabel(int index, String label) {
		this.labels.add(index, label);
	}

	public JcNode getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(JcNode targetNode) {
		this.targetNode = targetNode;
	}
}
