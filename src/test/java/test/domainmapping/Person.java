package test.domainmapping;

import java.util.Date;
import java.util.List;

public class Person {

	private String firstName;
	private String lastName;
	private Date birthDate;
	private Address mainAddress;
	private Contact contact;
	private Person bestFriend;
	private List<Integer> luckyNumbers;
	@SuppressWarnings("rawtypes")
	private List addresses;
	
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
	public Address getMainAddress() {
		return mainAddress;
	}
	public void setMainAddress(Address address) {
		this.mainAddress = address;
	}
	public Contact getContact() {
		return contact;
	}
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	public Person getBestFriend() {
		return bestFriend;
	}
	public void setBestFriend(Person friend) {
		this.bestFriend = friend;
	}
	public List<Integer> getLuckyNumbers() {
		return luckyNumbers;
	}
	public void setLuckyNumbers(List<Integer> luckyNumbers) {
		this.luckyNumbers = luckyNumbers;
	}
	@SuppressWarnings("rawtypes")
	public List getAddresses() {
		return addresses;
	}
	@SuppressWarnings("rawtypes")
	public void setAddresses(List addresses) {
		this.addresses = addresses;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.firstName);
		sb.append(' ');
		sb.append(this.lastName);
		sb.append(" born: ");
		sb.append(this.birthDate);
		if (this.mainAddress != null) {
			sb.append(' ');
			sb.append(this.mainAddress.toString());
		}
		if (this.contact != null) {
			sb.append(' ');
			sb.append(this.contact.toString());
		}
		return sb.toString();
	}
	
}
