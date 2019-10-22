package tools.violationsAnalyzer;

import org.jgrapht.graph.DefaultWeightedEdge;

public class ViolationEdge extends DefaultWeightedEdge {

	private ViolationPoint to;
	private ViolationPoint from;

	public ViolationEdge(ViolationPoint from, ViolationPoint to) {
		this.from = from;
		this.to = to;
	}

	public String toString(){
		return from +" - "+to;
	}

	public ViolationPoint getFrom() {
		return from;
	}

	public ViolationPoint getTo() {
		return to;
	}
}
