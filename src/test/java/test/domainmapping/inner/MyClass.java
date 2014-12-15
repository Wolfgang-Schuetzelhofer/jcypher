package test.domainmapping.inner;

public class MyClass {

	private MyInnerClass myInnerClass;
	
	public MyInnerClass getMyInnerClass() {
		return myInnerClass;
	}

	public void setMyInnerClass(MyInnerClass myInnerClass) {
		this.myInnerClass = myInnerClass;
	}
	
	public MyInnerClass createInnerClass() {
		return new MyInnerClass();
	}

	/********************************/
	public class MyInnerClass {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
