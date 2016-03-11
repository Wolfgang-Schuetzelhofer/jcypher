package test.bookingtest;

public class Booking {
	private final Long id;
	private final String fraud;
	private final String status;
	private final String email;

	public Booking(Long id, String fraud, String status, String email) {
		this.id = id;
		this.fraud = fraud;
		this.status = status;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public String getFraud() {
		return fraud;
	}

	public String getStatus() {
		return status;
	}

	public String getEmail() {
		return email;
	}
}
