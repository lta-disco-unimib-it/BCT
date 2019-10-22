package tools.violationsAnalyzer;

import java.util.Set;


public class ReachabilityInfo {
	private Set<ViolationPoint> children;
	private Set<ViolationPoint> reachable;
	private Set<ViolationPoint> reachableParent;
	private Set<ViolationPoint> reachableUseful;
	
	public ReachabilityInfo(Set<ViolationPoint> children, Set<ViolationPoint> reachableParent, Set<ViolationPoint> reachable, Set<ViolationPoint> reachableUseful) {
		this.children = children;
		this.reachableParent = reachableParent;
		this.reachableUseful = reachableUseful;
		this.reachable = reachable;
	}

	public Set<ViolationPoint> getChildren() {
		return children;
	}

	public Set<ViolationPoint> getReachable() {
		return reachable;
	}

	public Set<ViolationPoint> getReachableParent() {
		return reachableParent;
	}

	public Set<ViolationPoint> getReachableUseful() {
		return reachableUseful;
	}
	
}
