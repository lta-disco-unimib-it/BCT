package tools.violationsAnalyzer.distanceCalculators;

import tools.violationsAnalyzer.ViolationPoint;


public abstract class DistanceCalculator {

	private String name;

	public DistanceCalculator(String name){
		this.name = name;
	}
	
	public abstract double calculate(DistanceTable distanceTable, ViolationPoint from, ViolationPoint to);

	public String getName(){
		return name;
	}

}
