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

package iot.jcypher.values.functions;

import iot.jcypher.values.Function;

public class FUNCTION {

	/*********************************************/
	public static class String {
		public static final Function TRIM = new Function("trim(", ")", FTYPE.String.TRIM);
		public static final Function LTRIM = new Function("ltrim(", ")", FTYPE.String.LTRIM);
		public static final Function RTRIM = new Function("rtrim(", ")", FTYPE.String.RTRIM);
		public static final Function REPLACE = new Function("replace(", ")", FTYPE.String.REPLACE);
		public static final Function SUBSTRING = new Function("substring(", ")", FTYPE.String.SUBSTRING);
		public static final Function LEFT = new Function("left(", ")", FTYPE.String.LEFT);
		public static final Function RIGHT = new Function("right(", ")", FTYPE.String.RIGHT);
		public static final Function LOWER = new Function("lower(", ")", FTYPE.String.LOWER);
		public static final Function UPPER = new Function("upper(", ")", FTYPE.String.UPPER);
	}
	
	/*********************************************/
	public static class Common {
		public static final Function ENCLOSE = new Function("(", ")", FTYPE.Common.ENCLOSE);
		public static final Function TIMESTAMP = new Function("timestamp(", ")", FTYPE.Common.TIMESTAMP);
		public static final Function COALESCE = new Function("coalesce(", ")", FTYPE.Common.COALESCE);
		public static final Function STR = new Function("str(", ")", FTYPE.Common.STR);
		public static final Function TOINT = new Function("toInt(", ")", FTYPE.Common.TOINT);
		public static final Function TOFLOAT = new Function("toFloat(", ")", FTYPE.Common.TOFLOAT);
	}
	
	/*********************************************/
	public static class Collection {
		public static final Function LENGTH = new Function("length(", ")", FTYPE.Collection.LENGTH);
		public static final Function HEAD = new Function("head(", ")", FTYPE.Collection.HEAD);
		public static final Function LAST = new Function("last(", ")", FTYPE.Collection.LAST);
	}
	
	/*********************************************/
	public static class PropertyContainer {
		public static final Function ID = new Function("id(", ")", FTYPE.PropertyContainer.ID);
	}
	
	/*********************************************/
	public static class Relation {
		public static final Function TYPE = new Function("type(", ")", FTYPE.Relation.TYPE);
		public static final Function STARTNODE = new Function("startNode(", ")", FTYPE.Relation.STARTNODE);
		public static final Function ENDNODE = new Function("endNode(", ")", FTYPE.Relation.ENDNODE);
	}
	
	/*********************************************/
	public static class Node {
		public static final Function LABELS = new Function("labels(", ")", FTYPE.Node.LABELS);
	}
	
	/*********************************************/
	public static class Path {
		public static final Function NODES = new Function("nodes(", ")", FTYPE.Path.NODES);
		public static final Function RELATIONS = new Function("relationships(", ")", FTYPE.Path.RELATIONS);
	}
}
