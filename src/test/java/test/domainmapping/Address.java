package test.domainmapping;

public class Address {

	private String city;
	private String street;
	private int number;
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	@Override
	public String toString() {
		return "Address [city=" + city + ", street=" + street + ", number="
				+ number + "]";
	}
	
}
