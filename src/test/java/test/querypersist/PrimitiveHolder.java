/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package test.querypersist;

import java.util.List;

public class PrimitiveHolder {
	
	private String theString;
	private int theInt;
	private short theShort;
	private long theLong;
	private float theFloat;
	private double theDouble;
	private boolean theBool;
	private List<?> thePrimList;
	
	public String getTheString() {
		return theString;
	}
	public void setTheString(String theString) {
		this.theString = theString;
	}
	public int getTheInt() {
		return theInt;
	}
	public void setTheInt(int theInt) {
		this.theInt = theInt;
	}
	public short getTheShort() {
		return theShort;
	}
	public void setTheShort(short theShort) {
		this.theShort = theShort;
	}
	public long getTheLong() {
		return theLong;
	}
	public void setTheLong(long theLong) {
		this.theLong = theLong;
	}
	public float getTheFloat() {
		return theFloat;
	}
	public void setTheFloat(float theFloat) {
		this.theFloat = theFloat;
	}
	public double getTheDouble() {
		return theDouble;
	}
	public void setTheDouble(double theDouble) {
		this.theDouble = theDouble;
	}
	public boolean isTheBool() {
		return theBool;
	}
	public void setTheBool(boolean theBool) {
		this.theBool = theBool;
	}
	public List<?> getThePrimList() {
		return thePrimList;
	}
	public void setThePrimList(List<?> thePrimList) {
		this.thePrimList = thePrimList;
	}

}
