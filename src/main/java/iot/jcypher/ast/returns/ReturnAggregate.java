package iot.jcypher.ast.returns;

import iot.jcypher.values.ValueElement;

public class ReturnAggregate extends ReturnValue {

	private AggregateFunctionType type;
	private boolean distinct = false;
	private ValueElement argument;
	private Number percentile;
	
	/*********************************/
	public enum AggregateFunctionType {
		SUM, AVG, PERCENTILE_DISC, PERCENTILE_CONT,
		STDEV, STDEVP, MAX, MIN
	}

	public AggregateFunctionType getType() {
		return type;
	}

	public void setType(AggregateFunctionType type) {
		this.type = type;
	}

	public ValueElement getArgument() {
		return argument;
	}

	public void setArgument(ValueElement argument) {
		this.argument = argument;
	}

	public Number getPercentile() {
		return percentile;
	}

	public void setPercentile(Number percentile) {
		this.percentile = percentile;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct() {
		this.distinct = true;
	}
}
