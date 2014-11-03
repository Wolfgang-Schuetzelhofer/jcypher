package test.domainmapping.maps;

public class Mark {

	private String name;

	public Mark() {
		super();
	}

	public Mark(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Mark [name=" + name + "]";
	}
	
}
