package iot.jcypher.graph;

public class GrProperty {

	private String name;
	private Object value;

	GrProperty(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
