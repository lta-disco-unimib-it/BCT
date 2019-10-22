package tools.violationsAnalyzer.distanceCalculators;

import tools.violationsAnalyzer.ViolationPoint;


public class DistanceCalculatorDown extends DistanceCalculator {

	public DistanceCalculatorDown(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double calculate(DistanceTable distanceTable, ViolationPoint from,
			ViolationPoint to) {
		return distanceTable.getDistanceDown(from,to);
	}

}
