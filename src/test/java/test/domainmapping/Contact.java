package test.domainmapping;

public class Contact {

	private ContactType type;
	private String nummer;
	
	public ContactType getType() {
		return type;
	}
	public void setType(ContactType type) {
		this.type = type;
	}
	public String getNummer() {
		return nummer;
	}
	public void setNummer(String nummer) {
		this.nummer = nummer;
	}
	@Override
	public String toString() {
		return "Contact [type=" + type + ", nummer=" + nummer + "]";
	}
	
}
