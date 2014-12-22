package test.domainmapping.inner;

import test.domainmapping.inner.MyClass.MyInnerClass;
import test.domainmapping.inner.MyClass.MyStaticInnerClass;
import test.domainmapping.inner.MyClass.MyInnerClass.MyInnerInnerClass;

public class MyOtherClass {

	private MyInnerClass myInnerClass;
	private MyInnerInnerClass myInnerInnerClass;
	private MyStaticInnerClass myStaticInnerClass;
	public MyInnerClass getMyInnerClass() {
		return myInnerClass;
	}
	public void setMyInnerClass(MyInnerClass myInnerClass) {
		this.myInnerClass = myInnerClass;
	}
	public MyInnerInnerClass getMyInnerInnerClass() {
		return myInnerInnerClass;
	}
	public void setMyInnerInnerClass(MyInnerInnerClass myInnerInnerClass) {
		this.myInnerInnerClass = myInnerInnerClass;
	}
	public MyStaticInnerClass getMyStaticInnerClass() {
		return myStaticInnerClass;
	}
	public void setMyStaticInnerClass(MyStaticInnerClass myStaticInnerClass) {
		this.myStaticInnerClass = myStaticInnerClass;
	}
	
}
