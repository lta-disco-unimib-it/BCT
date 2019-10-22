package tools.violationsAnalyzer.distanceCalculators;

import tools.violationsAnalyzer.ViolationPoint;

public class DistanceCalculatorWeighted extends DistanceCalculator {

	private double upWeigth;
	private double downWeight;

	public DistanceCalculatorWeighted( String name, double upWeigth, double downWeight ){
		super(name);
		this.upWeigth = upWeigth;
		this.downWeight = downWeight;
	}
	
	public double calculate(DistanceTable distanceTable, ViolationPoint child,
			ViolationPoint parent) {
		return upWeigth*distanceTable.getDistanceUp(child, parent)+downWeight*distanceTable.getDistanceDown(child, parent);
	}

	
}
