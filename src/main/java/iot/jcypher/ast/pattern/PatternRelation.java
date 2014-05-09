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

package iot.jcypher.ast.pattern;

import iot.jcypher.values.JcRelation;

import java.util.ArrayList;
import java.util.List;

public class PatternRelation extends PatternElement {

	private List<String> types = new ArrayList<String>();
	private Direction direction = Direction.BOTH;
	private int minHops = 1;
	private int maxHops = 1;
	
	public PatternRelation(JcRelation jcRelation) {
		super(jcRelation);
	}
	
	public List<String> getTypes() {
		return types;
	}

	public void in() {
		this.direction = Direction.IN;
	}
	
	public void out() {
		this.direction = Direction.OUT;
	}
	
	public void minHops(int minHops) {
		this.minHops = minHops;
	}
	
	public void maxHops(int maxHops) {
		this.maxHops = maxHops;
	}
	
	public void maxHopsUnbound() {
		this.maxHops = -1;
	}
	
	public void hopsUnbound() {
		this.maxHops = -1;
		this.minHops = 0;
	}
	
	public void hops(int hops) {
		this.maxHops = hops;
		this.minHops = hops;
	}
	
	public Direction getDirection() {
		return direction;
	}

	public int getMinHops() {
		return minHops;
	}

	public int getMaxHops() {
		return maxHops;
	}

	/*****************************************************************/
	public enum Direction {
		IN, OUT, BOTH
	}
}
