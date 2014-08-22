package test.domainmapping;

import java.util.Date;

public class Person {

	private String firstName;
	private String lastName;
	private Date birthDate;
	private Address address;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.firstName);
		sb.append(' ');
		sb.append(this.lastName);
		sb.append(" born: ");
		sb.append(this.birthDate);
		if (this.address != null) {
			sb.append(' ');
			sb.append(this.address.toString());
		}
		return sb.toString();
	}
	
}
