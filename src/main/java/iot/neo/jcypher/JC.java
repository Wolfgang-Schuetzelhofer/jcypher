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

package iot.neo.jcypher;

import iot.neo.jcypher.values.ValueElement;
import iot.neo.jcypher.values.ValueAccess;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER FACTORY</i></b></div>
 */
public class JC {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the current time, returns the same value during a whole query</i></div>
	 * <br/>
	 */
	public static ValueElement timeStamp() {
		return ValueAccess.timeStamp();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return the first non-NULL value in the list of expressions</i></div>
	 * <br/>
	 */
	public static ValueElement coalesce(ValueElement... expression) {
		return ValueAccess.coalesce(expression);
	}
}
