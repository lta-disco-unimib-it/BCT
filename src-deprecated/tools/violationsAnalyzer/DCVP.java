package tools.violationsAnalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import tools.violationsAnalyzer.distanceCalculators.DistanceCalculator;


public class DCVP {
	private Set<DistanceCalculator> calculators = new HashSet<DistanceCalculator>();
	
	private class ReachabilityData {
		private HashMap<DistanceCalculator,TCInfo> data = new HashMap<DistanceCalculator, TCInfo>();
		
		public void put(DistanceCalculator dc, TCInfo tcInfo) {
			calculators.add(dc);
			data.put(dc,tcInfo);
		}
		
		public TCInfo get(DistanceCalculator dc){
			return data.get(dc);
		}
		
	}

	HashMap<TestCaseInfo,ReachabilityData> testCasesData = new HashMap<TestCaseInfo, ReachabilityData>();
	
	public void put(DistanceCalculator dc, HashMap<TestCaseInfo, TCInfo> usefulViolations) {
		for ( TestCaseInfo tc :  usefulViolations.keySet() ){
			addData(dc,tc,usefulViolations.get(tc));
		}
	}

	private void addData(DistanceCalculator dc, TestCaseInfo tc, TCInfo tcInfo) {
		ReachabilityData data = getReachabilityData(tc);
		data.put(dc, tcInfo );
		
	}

	private ReachabilityData getReachabilityData(TestCaseInfo tc) {
		ReachabilityData data = testCasesData.get(tc);
		if ( data == null ){
			data = new ReachabilityData();
			testCasesData.put(tc, data);
		}
		return data;
	}

	public HashMap<TestCaseInfo, HashMap<DistanceCalculator, TCInfo>> getData(){
		HashMap<TestCaseInfo, HashMap<DistanceCalculator,TCInfo>> result = 
			new HashMap<TestCaseInfo, HashMap<DistanceCalculator,TCInfo>>();
		
		for (TestCaseInfo tc : testCasesData.keySet() ){
			HashMap<DistanceCalculator,TCInfo> entries = 
				new HashMap<DistanceCalculator, TCInfo>();
			ReachabilityData tcData = testCasesData.get(tc);
			for ( DistanceCalculator dc : calculators ){
				entries.put(dc,tcData.get(dc));
			}
			result.put(tc, entries);

		}
		
		return result;
	}
	
	
	public Set<Entry<DistanceCalculator, TCInfo>> getData( TestCaseInfo tc ){
		HashMap<DistanceCalculator,TCInfo> entries = 
			new HashMap<DistanceCalculator, TCInfo>();
		ReachabilityData tcData = testCasesData.get(tc);
		for ( DistanceCalculator dc : calculators ){
			entries.put(dc,tcData.get(dc));
		}
		
		return entries.entrySet();

		
	}

	public Set<DistanceCalculator> getCalculators() {
		return calculators;
	}
}
