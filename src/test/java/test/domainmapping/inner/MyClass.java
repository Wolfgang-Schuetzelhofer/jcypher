package test.domainmapping.inner;

public class MyClass {

	private MyInnerClass myInnerClass;
	private MyStaticInnerClass myStaticInnerClass;
	private MyInterface myInterface;
	
	public MyInnerClass getMyInnerClass() {
		return myInnerClass;
	}

	public void setMyInnerClass(MyInnerClass myInnerClass) {
		this.myInnerClass = myInnerClass;
	}
	
	public MyStaticInnerClass getMyStaticInnerClass() {
		return myStaticInnerClass;
	}

	public void setMyStaticInnerClass(MyStaticInnerClass myStaticInnerClass) {
		this.myStaticInnerClass = myStaticInnerClass;
	}

	public MyInterface getMyInterface() {
		return myInterface;
	}

	public void setMyInterface(MyInterface myInterface) {
		this.myInterface = myInterface;
	}

	public MyInnerClass createInnerClass() {
		return new MyInnerClass();
	}

	/********************************/
	public class MyInnerClass {
		private String name;
		private MyInnerInnerClass myInnerInnerClass;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		public MyInnerInnerClass getMyInnerInnerClass() {
			return myInnerInnerClass;
		}

		public void setMyInnerInnerClass(MyInnerInnerClass myInnerInnerClass) {
			this.myInnerInnerClass = myInnerInnerClass;
		}

		public MyInnerInnerClass createInnerInnerClass() {
			return new MyInnerInnerClass();
		}
		
		/********************************/
		public class MyInnerInnerClass {
			private String name;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}
		}
	}
	
	/********************************/
	public static class MyStaticInnerClass {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
