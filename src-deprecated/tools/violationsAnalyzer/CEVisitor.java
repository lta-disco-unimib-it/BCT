package tools.violationsAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import tools.violationsAnalyzer.distanceCalculators.DistanceTable;


public class CEVisitor {
	private int count = 0;
	private Set<ViolationPoint> vps;
	
	private DistanceTable distanceTable;
	
	public DistanceTable getDistanceTable(){
		return distanceTable;
	}
	
	public CEVisitor(Set<ViolationPoint> vps ) {
		this.vps = vps;
		distanceTable = new DistanceTable(vps);
	}


	public boolean before(CallElement ce) {
		++count;
		
		if ( ce.hasViolations() ){
			fillViols( ce );
		}
		return true;
	}
	
	private void fillViols(CallElement ce) {
		
		
		for ( ViolationPoint  vp : ce.getViolationPoints() ){
			if ( vps.contains(vp) )
				fillViols(ce,vp);
		}
	}
	
	
	private void fillViols(CallElement ce, ViolationPoint vp ) {
		CallElement parent = ce;
		int timeStamp = vp.getTimeStamp();
		int count = 0;
		HashSet<ViolationPoint> toFill = new HashSet<ViolationPoint>( vps );

//
//		for ( ViolationPoint viol : vps ){
//			if ( ce.getReachableViolations().contains(viol) && viol.getTimeStamp() <= timeStamp ){
//				distanceTable.addDistanceUp(vp,viol, 0);
//				toFill.remove(viol);
//			}
//		}
		
		
		
		
		while ( ( ( parent = parent.getParent() ) != null ) && ( toFill.size() > 0 ) ){
			++count;
			
//			for ( ViolationPoint parentViolation : parent.getViolationPoints() ){
//				System.out.println("PV "+parentViolation.getViolation().getId());
//				distanceTable.addDistanceUp(vp,parentViolation,count);
//				distanceTable.addDistanceDown(vp,parentViolation,0);
//				toFill.remove(parentViolation);
//			}
			
			for ( Entry<ViolationPoint,Integer> ev : parent.getReachableViolations() ){
				ViolationPoint rv = ev.getKey();
				Integer steps = ev.getValue();
				
				
				
				if ( toFill.contains(rv) ){
					System.out.println("distance from "+vp.getViolation().getId()+" to "+rv.getViolation().getId()+", up "+count+", down "+steps);
					if ( rv.getTimeStamp() >= timeStamp ){
						
						if ( count == 1 && steps == 1 ){//Add sibling as 0 distance
							distanceTable.addDistanceUp(vp,rv,0);
							distanceTable.addDistanceDown(vp,rv,0);
						} else {
							distanceTable.addDistanceUp(vp,rv,count);
							distanceTable.addDistanceDown(vp,rv,steps);
						}
					} else {
						distanceTable.addDistanceUp(vp,rv,-1);
					}
					toFill.remove(rv);
				}
			}	
		}
	}



	public boolean after(CallElement element) {
		--count;
		return true;
	}
	
	

	public Set<ViolationPoint> getVps() {
		return vps;
	}


	
	
}
