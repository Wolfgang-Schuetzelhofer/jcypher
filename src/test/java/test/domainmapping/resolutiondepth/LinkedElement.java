package test.domainmapping.resolutiondepth;

public class LinkedElement {

	private String description;
	private LinkedElement next;
	
	public LinkedElement() {
		super();
	}

	public LinkedElement(String description) {
		super();
		this.description = description;
	}

	public LinkedElement(String description, LinkedElement next) {
		super();
		this.description = description;
		this.next = next;
	}

	public LinkedElement getNext() {
		return next;
	}

	public void setNext(LinkedElement next) {
		this.next = next;
	}

	public String getDescription() {
		return description;
	}
	
}
