package tools.violationsAnalyzer;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class ViolationTraversalListener implements
		TraversalListener<String, ViolationEdge> {

	private SimpleDirectedWeightedGraph<String, ViolationEdge> graph;
	private double totalW;

	public ViolationTraversalListener(SimpleDirectedWeightedGraph<String, ViolationEdge> graph) {
		this.graph = graph;
	}

	public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void edgeTraversed(EdgeTraversalEvent<String, ViolationEdge> arg0) {
		totalW += graph.getEdgeWeight(arg0.getEdge());
	}

	public void vertexFinished(VertexTraversalEvent<String> arg0) {
		// TODO Auto-generated method stub

	}

	public void vertexTraversed(VertexTraversalEvent<String> arg0) {
		// TODO Auto-generated method stub

	}

	public double getTotalW() {
		return totalW;
	}

}
