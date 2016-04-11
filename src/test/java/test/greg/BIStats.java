package test.greg;

public class BIStats {

	private Env measuredIn;
	private App measuredFor;
	private BIStats follows;
	
	public BIStats() {
	}

	public Env getMeasuredIn() {
		return measuredIn;
	}

	public void setMeasuredIn(Env measuredIn) {
		this.measuredIn = measuredIn;
	}

	public App getMeasuredFor() {
		return measuredFor;
	}

	public void setMeasuredFor(App measuredFor) {
		this.measuredFor = measuredFor;
	}

	public BIStats getFollows() {
		return follows;
	}

	public void setFollows(BIStats follows) {
		this.follows = follows;
	}

}
