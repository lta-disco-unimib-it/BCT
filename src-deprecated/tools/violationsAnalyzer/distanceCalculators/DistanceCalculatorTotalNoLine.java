package tools.violationsAnalyzer.distanceCalculators;

import tools.violationsAnalyzer.ViolationPoint;

public class DistanceCalculatorTotalNoLine extends DistanceCalculator {

	public DistanceCalculatorTotalNoLine(String name) {
		super(name);
	}

	@Override
	public double calculate(DistanceTable distanceTable, ViolationPoint child,
			ViolationPoint parent) {
		return DistanceCalculatorUtil.calculateDistanceWithoutLines( distanceTable.getDistanceUp(child,parent) ) + DistanceCalculatorUtil.calculateDistanceWithoutLines( distanceTable.getDistanceDown(child,parent) ); 
	}

}
