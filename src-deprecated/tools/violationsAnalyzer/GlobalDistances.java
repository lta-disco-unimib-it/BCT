package tools.violationsAnalyzer;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;



public class GlobalDistances {
	private HashMap<Violation,HashMap<Violation,DistCount>> distances = new HashMap<Violation, HashMap<Violation,DistCount>>();
	
	public void put(Violation parent, Violation child, double totalDist) {
		HashMap<Violation, DistCount> parentDists = getParentDists(parent);
		
		DistCount dist = parentDists.get(child);
		if ( dist == null ){
			dist = new DistCount();
		}
		dist.addValue(totalDist);
	}

	private HashMap<Violation, DistCount> getParentDists(Violation parent) {
		HashMap<Violation, DistCount> result = distances.get(parent);
		if ( result == null ){
			result = new HashMap<Violation, DistCount>();
			distances.put(parent, result);
		}
		return result;
	}

	public Set<Entry<Violation, HashMap<Violation, DistCount>>> getEntries(){
		return distances.entrySet();
	}

	public  Set<Violation> getViolations() {
		return distances.keySet();
	}
}
