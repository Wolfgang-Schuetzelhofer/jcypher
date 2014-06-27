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

package iot.jcypher.graph;

import iot.jcypher.graph.internal.ChangeListener;
import iot.jcypher.result.util.ResultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrNode extends GrPropertyContainer {

	private List<GrLabel> labels;
	private LabelChangeListener labelChangeListener;
	
	GrNode(ResultHandler resultHandler, long id, int rowIdx) {
		super(resultHandler, id, rowIdx);
	}

	/**
	 * @return an unmodifiable list of node labels
	 */
	public List<GrLabel> getLabels() {
		if (this.labels == null) {
			this.labels = this.resultHandler.getNodeLabels(getId(), this.rowIndex);
			if (this.labelChangeListener == null)
				this.labelChangeListener = new LabelChangeListener();
			for (GrLabel label : this.labels) {
				label.addChangeListener(this.labelChangeListener);
			}
		}

		// Build a new Array
			// to allow iterating over the labels with a for loop and to remove labels.
			// That is done by calling remove() on the label,
			// which leads to removing the label from this.labels
			// That may not break the for loop
		ArrayList<GrLabel> list = new ArrayList<GrLabel>(this.labels);
		return Collections.unmodifiableList(list);
	}

	/********************************************/
	private class LabelChangeListener implements ChangeListener {

		@Override
		public void changed(Object theChanged, SyncState oldState,
				SyncState newState) {
		}
	}
}
