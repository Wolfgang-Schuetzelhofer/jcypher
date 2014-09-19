package test.domainmapping.ambiguous;

public abstract class AbstractPerson implements IPerson {

	private String namePart1;
	private String namePart2;
	
	public String getNamePart1() {
		return namePart1;
	}
	public void setNamePart1(String namePart1) {
		this.namePart1 = namePart1;
	}
	public String getNamePart2() {
		return namePart2;
	}
	public void setNamePart2(String namePart2) {
		this.namePart2 = namePart2;
	}
	@Override
	public String toString() {
		return "AbstractPerson [namePart1=" + namePart1 + ", namePart2="
				+ namePart2 + "]";
	}
	
}
