package tools.violationsAnalyzer.distanceCalculators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import tools.violationsAnalyzer.ViolationPoint;

public class DistanceTable {
	private HashMap<ViolationPoint,HashMap<ViolationPoint,Double>> violsUp = new HashMap<ViolationPoint, HashMap<ViolationPoint,Double> >();
	private HashMap<ViolationPoint,HashMap<ViolationPoint,Double>> violsDown = new HashMap<ViolationPoint, HashMap<ViolationPoint,Double> >();
	private Set<ViolationPoint> violationPoints = new HashSet<ViolationPoint>();
	
	
	
	public DistanceTable(Set<ViolationPoint> vps) {
		violationPoints.addAll(vps);
		
		for ( ViolationPoint vp : vps ){
			HashMap<ViolationPoint, Double> mapUp = new HashMap<ViolationPoint,Double>();
			for ( ViolationPoint viol : vps  )
				mapUp.put(viol, -2.0);
			
			violsUp.put(vp,mapUp);
			
			
			
			HashMap<ViolationPoint, Double> mapDown = new HashMap<ViolationPoint,Double>();
			for ( ViolationPoint viol : vps  )
				mapDown.put(viol, -2.0);
			
			violsDown.put(vp,mapDown);
			
		}
	}



	public void addDistanceUp( ViolationPoint from, ViolationPoint to, double value ){
		System.out.println("ADD UP from "+from.getViolation().getId() +" to "+to.getViolation().getId()+" : "+value);
		HashMap<ViolationPoint, Double> tos = violsUp.get(from);
		tos.put(to, value);
	}



	public void addDistanceDown(ViolationPoint from, ViolationPoint to, double value) {
		System.out.println("ADD DOWN from "+from.getViolation().getId() +" to "+to.getViolation().getId()+" : "+value);
		HashMap<ViolationPoint, Double> tos = violsDown.get(from);
		tos.put(to, value);
	}

	
	
	
	public List<List<Double>> getDistances() {
		ArrayList<List<Double>> res = new ArrayList<List<Double>>();
		for ( Entry<ViolationPoint,HashMap<ViolationPoint,Double>> viol : violsUp.entrySet() ){
			ArrayList<Double> arr = new ArrayList<Double>();	
			for ( double dist : viol.getValue().values() ){
				arr.add(dist);
			}
			res.add(arr);
		}
		return res;
	}

	
	public Double getDistanceUp(ViolationPoint from, ViolationPoint to) {
		HashMap<ViolationPoint, Double> tos = violsUp.get(from);
		return tos.get(to);
	}


	public Double getDistanceDown(ViolationPoint from, ViolationPoint to) {
		HashMap<ViolationPoint, Double> tos = violsDown.get(from);
		return tos.get(to);
	}



	public Set<ViolationPoint> getVps() {
		return new HashSet<ViolationPoint>(violationPoints);
	}
	
}
