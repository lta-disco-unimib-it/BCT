package tools.violationsAnalyzer.distanceCalculators;

import tools.violationsAnalyzer.ViolationPoint;


public class DistanceCalculatorParent extends DistanceCalculator {

	public DistanceCalculatorParent(String name) {
		super(name);
	}

	public double calculate(DistanceTable distanceTable, ViolationPoint child, ViolationPoint parent) {
		return distanceTable.getDistanceUp(child, parent);
	}

}
