package iot.jcypher.graph.internal;

public class LocalId extends GrId {

	public LocalId(long id) {
		super(id);
	}

	@Override
	public String toString() {
		return "LOCAL_id: " + String.valueOf(getId());
	}
}
