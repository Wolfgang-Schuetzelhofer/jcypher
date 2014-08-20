package iot.jcypher.domain.mapping;

import iot.jcypher.graph.GrNode;

import java.util.ArrayList;
import java.util.List;

public class NodeLabelMapping {

	private List<String> labels;
	
	public void mapLabels(Object domainObject, GrNode rNode) {
		if (this.labels != null) {
			for(String label : this.labels) {
				if (rNode.getLabel(label) == null)
					rNode.addLabel(label);
			}
		}
	}

	public List<String> getLabels() {
		if (this.labels == null)
			this.labels = new ArrayList<String>();
		return this.labels;
	}
}
