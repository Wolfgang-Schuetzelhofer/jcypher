package iot.jcypher.graph.internal;

public class GrId {
	private long id;

	public GrId(long id) {
		super();
		this.id = id;
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "DB_id: " + String.valueOf(this.id);
	}
	
}
