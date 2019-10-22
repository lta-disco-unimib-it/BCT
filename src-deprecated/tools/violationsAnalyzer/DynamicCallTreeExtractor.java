package tools.violationsAnalyzer;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class DynamicCallTreeExtractor {
	

	public static SimpleDirectedWeightedGraph<String, String> getTree(ThreadElement te) {
		
		SimpleDirectedWeightedGraph<String,String> g = new SimpleDirectedWeightedGraph<String, String>(String.class);
		
		for ( CallElement child : te.getChildren() ){
			if ( child.getReachableViolations().size() > 0 ){
				String fc = child.getId();
				g.addVertex(fc);
				populateTree( child, g, fc );
			}
		}
		return g;
	}

	private static void populateTree(CallElement current, SimpleDirectedGraph<String, String> g, String vertex) {
		for ( CallElement child : current.getChildren() ){
			if ( child.getReachableViolations().size() > 0 ){
				String cN = child.getId();
				g.addVertex(cN);
				g.addEdge(vertex, cN);
				populateTree( child, g, cN );
			}
		}
	}
}
