package tools.violationsAnalyzer.distanceCalculators;

import tools.violationsAnalyzer.ViolationPoint;


public class DistanceCalculatorTotal extends DistanceCalculator {

	public DistanceCalculatorTotal(String name) {
		super(name);
	}

	public double calculate(DistanceTable distanceTable, ViolationPoint from, ViolationPoint to) {
		return distanceTable.getDistanceUp(from,to) + distanceTable.getDistanceDown(from,to); 
	}


}
