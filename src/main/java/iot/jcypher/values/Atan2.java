package iot.jcypher.values;

import iot.jcypher.values.functions.FUNCTION;
import iot.jcypher.values.operators.OPERATOR;

public class Atan2 {

	private JcNumber xVal;
	
	Atan2(JcNumber xval) {
		this.xVal = xval;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the arctangent2 of a set of coordinates (x, y), in radians;</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the y coordinate, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber y(Number yval) {
		JcNumber sub = new JcNumber(yval, this.xVal, OPERATOR.Common.COMMA_SEPARATOR); 
		return new JcNumber(null, sub,
				new FunctionInstance(FUNCTION.Math.ATAN2, 2));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the arctangent2 of a set of coordinates (x, y), in radians;</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the y coordinate, return a <b>JcNumber</b></i></div>
	 * <br/>
	 */
	public JcNumber y(JcNumber yval) {
		JcNumber sub = new JcNumber(yval, this.xVal, OPERATOR.Common.COMMA_SEPARATOR); 
		return new JcNumber(null, sub,
				new FunctionInstance(FUNCTION.Math.ATAN2, 2));
	}
}
