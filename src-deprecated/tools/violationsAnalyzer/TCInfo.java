package tools.violationsAnalyzer;

import java.util.HashMap;

import tools.violationsAnalyzer.distanceCalculators.DistanceTable;


public class TCInfo  {
	
	private DistanceTable distanceTable;
	private HashMap<ViolationPoint, ReachabilityInfo> usefulViolations;

	public TCInfo(HashMap<ViolationPoint, ReachabilityInfo> usefulViolations, DistanceTable table) {
		System.out.println("NEW TCINFO"+usefulViolations+" "+table);
		this.distanceTable = table;
		this.usefulViolations = usefulViolations;
	}

	public DistanceTable getDistanceTable() {
		return distanceTable;
	}

	public HashMap<ViolationPoint, ReachabilityInfo> getUsefulViolations() {
		return usefulViolations;
	}

}
