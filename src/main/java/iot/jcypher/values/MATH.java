/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
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

package iot.jcypher.values;

import iot.jcypher.values.functions.FUNCTION;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER Factory for math functions</i></b></div>
 */
public class MATH {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the constant e (2.718281828459045, the base of natural log).</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>MATH.e()</b></i></div>
	 * <br/>
	 */
	public static JcNumber e() {
		return new JcNumber(null, null,
				new FunctionInstance(FUNCTION.Math.E));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the mathmatical constant pi (3.141592653589793).</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>MATH.pi()</b></i></div>
	 * <br/>
	 */
	public static JcNumber pi() {
		return new JcNumber(null, null,
				new FunctionInstance(FUNCTION.Math.PI));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a random double between 0.0 and 1.0.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>MATH.rand()</b></i></div>
	 * <br/>
	 */
	public static JcNumber rand() {
		return new JcNumber(null, null,
				new FunctionInstance(FUNCTION.Math.RAND));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the arctangent2 of a set of coordinates (x, y), in radians;</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the x coordinate</i></div>
	 * <br/>
	 */
	public static Atan2 atan2_x(Number xval) {
		JcNumber sub = new JcNumber(xval, null, null);
		return new Atan2(sub);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the arctangent2 of a set of coordinates (x, y), in radians;</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the x coordinate</i></div>
	 * <br/>
	 */
	public static Atan2 atan2_x(JcNumber xval) {
		return new Atan2(xval);
	}
}
