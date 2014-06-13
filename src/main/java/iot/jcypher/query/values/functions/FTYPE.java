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

package iot.jcypher.query.values.functions;

public class FTYPE {
	
	/*****************************/
	public enum String {
		TRIM, REPLACE, SUBSTRING, LEFT, RIGHT, LTRIM, RTRIM,
		LOWER, UPPER
	}
	
	/*****************************/
	public enum Common {
		ENCLOSE, TIMESTAMP, COALESCE, STR, TOINT, TOFLOAT
	}
	
	/*****************************/
	public enum Collection {
		LENGTH, HEAD, LAST
	}
	
	/*****************************/
	public enum Relation {
		TYPE, STARTNODE, ENDNODE
	}
	
	/*****************************/
	public enum Node {
		LABELS
	}
	
	/*****************************/
	public enum PropertyContainer {
		ID
	}
	
	/*****************************/
	public enum Path {
		NODES, RELATIONS
	}
	
	/*****************************/
	public enum Math {
		ABS, ACOS, ASIN, ATAN, ATAN2, COS, COT,
		DEGREES, RADIANS, E, EXP, FLOOR, HAVERSIN,
		LOG, LOG10, PI, RAND, ROUND, SIGN, SIN,
		SQRT, TAN
	}
}
