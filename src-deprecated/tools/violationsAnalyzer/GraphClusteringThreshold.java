package tools.violationsAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import tools.violationsAnalyzer.distanceCalculators.DistanceCalculator;
import tools.violationsAnalyzer.distanceCalculators.DistanceTable;

/**
 * This clustering method split an initial graph in several components by removing the edges with a weight that is higher than a specific threshold.
 * The threshold is selected by iteratively splitting by descrescent threshold values since an optimal configuration is reached.
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class GraphClusteringThreshold {
	
	/**
	 * Returns a grap with connected nodes clustered
	 * 
	 * @param v
	 * @param dc
	 * @param integer
	 * @return
	 */
	public HashMap<SimpleDirectedWeightedGraph<String, ViolationEdge>,Double> getGraphs(DistanceTable v, DistanceCalculator dc) {
		HashMap<SimpleDirectedWeightedGraph<String, ViolationEdge>,Double> results = new HashMap<SimpleDirectedWeightedGraph<String, ViolationEdge>,Double>();
		Set<Double> distances = getDistances(v, dc);
		
		SimpleDirectedWeightedGraph<String, ViolationEdge> oldGraph = getGraph(v, dc, Double.MAX_VALUE);
		
		double oldAvg = getMaxEdgeWeigthAvg(oldGraph);
		double lastIncrement = 0;
		Object[] ds = distances.toArray();
		
		ArrayList<String> roots = getRoots( oldGraph );
		int oldRootsNumber = roots.size();
		for ( String root:roots){
			System.out.println(root);
		}
		
		results.put(oldGraph,Double.MAX_VALUE);
		
		System.out.println("DISTANCE\tdistance\troots\tnodes\tmaxEdgeAvg\tavgMaxPath\tmaxPath\tavgMaxShortPath\ttotalEdgeWeigthAvg");
		System.out.println("DISTANCE\t"+ds[distances.size()-1]+"\t"+1+"\t"+oldGraph.vertexSet().size()+"\t"+oldAvg+"\t"+getAvgMaxPath(oldGraph)+"\t"+getMaxPath(oldGraph)+"\t"+getAvgMaxShortPath(oldGraph)+"\t"+getTotalEdgeWeigthAvg(oldGraph));
		for ( int i = distances.size()-1; i >= 0; --i ){
			double distance = (Double) ds[i]-1;
			SimpleDirectedWeightedGraph<String, ViolationEdge> newGraph = getGraph(v, dc, distance);
			
			ArrayList<String> newRoots = getRoots( newGraph );
			System.out.println("Rn "+oldRootsNumber+" "+newRoots.size());
			if ( newRoots.size() == oldRootsNumber )
				continue;
			for ( String root:newRoots){
				System.out.println("ROOT "+root);
			}	
			double newAvg = getMaxEdgeWeigthAvg(newGraph);
			
			System.out.println("Distance: "+distance+", old "+oldAvg+" new "+newAvg);
			
			double increment = oldAvg-newAvg;
			
			results.put(newGraph,distance);
			
			System.out.println("DISTANCE\t"+distance+"\t"+newRoots.size()+"\t"+newGraph.vertexSet().size()+"\t"+newAvg+"\t"+getAvgMaxPath(newGraph)+"\t"+getMaxPath(newGraph)+"\t"+getAvgMaxShortPath(newGraph)+"\t"+getTotalEdgeWeigthAvg(newGraph));
			System.out.println(increment);
//			if ( increment < lastIncrement )
//				break;
			
			oldRootsNumber = newRoots.size();
			oldGraph = newGraph;
			oldAvg = newAvg;
			lastIncrement = increment;
		}
		
		return results;
	}
	
	private double getTotalEdgeWeigthAvg(SimpleDirectedWeightedGraph<String, ViolationEdge> graph) {
		ArrayList<String> roots = getRoots(graph);
		double max=0;
		for ( String root: roots ){
			double avg = getEdgeWeigthAvg(graph,root);
			max+=avg;
		}
		return max/roots.size();
	}
	
	private double getMaxEdgeWeigthAvg(SimpleDirectedWeightedGraph<String, ViolationEdge> graph) {
		ArrayList<String> roots = getRoots(graph);
		double max=0;
		for ( String root: roots ){
			double avg = getEdgeWeigthAvg(graph,root);
			if ( avg > max ){
				max = avg;
			}
		}
		return max;
	}

	private double getEdgeWeigthAvg(SimpleDirectedWeightedGraph<String, ViolationEdge> graph, String root) {
		BreadthFirstIterator<String, ViolationEdge> it = new BreadthFirstIterator<String, ViolationEdge>(graph);
		ViolationTraversalListener tl = new ViolationTraversalListener(graph);
		double c = graph.edgeSet().size();
		it.addTraversalListener(tl);

		while(it.hasNext()){
			it.next();
//			++c;
		}
		
		return tl.getTotalW()/c;
	}

	/**
	 * Return the max path among all the max shortest path to cluster leaf
	 * 
	 * @param graph
	 * @return
	 */
	private double getMaxPath(SimpleDirectedWeightedGraph<String, ViolationEdge> graph) {
		ArrayList<String> roots = getRoots(graph);

		double max=0;
		for ( String rootVertex : roots ){
			ArrayList<ViolationEdge> longestPath = getLongestShortPath(graph,rootVertex);
			double len = getPathLen(graph, longestPath);
			if ( len > max )
				max = len;
		}
		
		return max;		
	}

	public Set<Double> getDistances(DistanceTable v, DistanceCalculator dc){
		Set<Double> distances = new TreeSet<Double>();
		Set<ViolationPoint> vps = v.getVps();
		
		SimpleDirectedWeightedGraph<String, String> g = new SimpleDirectedWeightedGraph<String, String>(String.class);
		
		ArrayList<ViolationPoint> nodes = new ArrayList<ViolationPoint>();
		
		for ( ViolationPoint vp : vps ){
			if (  vp.getViolation().getTcPassOccurrencies() == 0 ){
				String name = getVpName(vp);
				g.addVertex(name);
				nodes.add(vp);
			
			}
		}
		
		
		
		for ( ViolationPoint from : nodes ){
			for ( ViolationPoint to : nodes ){
				
				
				
				double dist = dc.calculate(v, from,to);
				
				if ( from != to
						&& to.getTimeStamp() >= from.getTimeStamp()
						&& dist>=0 
						){
					distances.add(dist);
					
				}
			}
		}
		return distances;
	}
	
	public SimpleDirectedWeightedGraph<String, ViolationEdge> getGraph(DistanceTable v, DistanceCalculator dc,  double threshOld) {
		Set<ViolationPoint> vps = v.getVps();
		
		SimpleDirectedWeightedGraph<String, ViolationEdge> g = new SimpleDirectedWeightedGraph<String, ViolationEdge>(ViolationEdge.class);
		
		ArrayList<ViolationPoint> nodes = new ArrayList<ViolationPoint>();
		
		for ( ViolationPoint vp : vps ){
			if (  vp.getViolation().getTcPassOccurrencies() == 0 ){
				String name = getVpName(vp);
				g.addVertex(name);
				nodes.add(vp);
			
			}
		}
		
		
		
		for ( ViolationPoint from : nodes ){
			for ( ViolationPoint to : nodes ){
				double dist = dc.calculate(v, from,to);
				if ( from != to
						&& to.getTimeStamp() >= from.getTimeStamp()
						&& dist>=0 
						&& dist <= threshOld
						){
		
					ViolationEdge edge = new ViolationEdge(from,to);
					g.addEdge(getVpName(from), getVpName(to), edge );
					System.out.println("EDGE "+getVpName(from)+" "+getVpName(to));
					g.setEdgeWeight(edge, dist);
				}
			}
		}
		
		return g;
	}
	
	/**
	 * Returns the average of the max length of a graph
	 * @param oldGraph
	 * @return
	 */
	private double getAvgMaxPath(SimpleDirectedWeightedGraph<String, ViolationEdge> oldGraph) {
		ArrayList<String> roots = getRoots(oldGraph);

		double totalLen=0;
		int interestingNodes = 0;
		for ( String rootVertex : roots ){
			ArrayList<ViolationEdge> longestPath = getLongestPath(oldGraph,rootVertex);
			if ( longestPath != null && longestPath.size() != 0 ){
				totalLen += getPathLen(oldGraph, longestPath);
				++interestingNodes ;
			}
		}
		
		return totalLen/interestingNodes;
		
		
	}

	/**
	 * Returns the average of the max length of a graph
	 * @param oldGraph
	 * @return
	 */
	private double getAvgMaxShortPath(SimpleDirectedWeightedGraph<String, ViolationEdge> oldGraph) {
		ArrayList<String> roots = getRoots(oldGraph);

		double totalLen=0;
		int interestingNodes = 0;
		for ( String rootVertex : roots ){
			ArrayList<ViolationEdge> longestPath = getLongestShortPath(oldGraph,rootVertex);
			if ( longestPath != null && longestPath.size() != 0 ){
				totalLen += getPathLen(oldGraph, longestPath);
				++interestingNodes ;
			}
		}
		
		return totalLen/interestingNodes;
		
		
	}
	
	/**
	 * Returns the longest path in a graph
	 * 
	 * @param oldGraph
	 * @param rootVertex
	 * @return
	 */
	private ArrayList<ViolationEdge> getLongestPath(SimpleDirectedWeightedGraph<String, ViolationEdge> oldGraph, String rootVertex) {
		ArrayList<ArrayList<ViolationEdge>> allPaths = getAllPathsToLeafs(oldGraph,rootVertex);
		
		double max=-1;
		ArrayList<ViolationEdge> longestPath = null;
		System.out.println("S "+allPaths.size());
		for ( int i = 0; i < allPaths.size(); i++ ){
			ArrayList<ViolationEdge> path = allPaths.get(i);
			
			double length=getPathLen(oldGraph,path);
			
			if ( length > max ){
				longestPath = path;
			}
		}
		return longestPath;
	}

	/**
	 * Returns the longest path in a graph
	 * 
	 * @param oldGraph
	 * @param rootVertex
	 * @return
	 */
	private ArrayList<ViolationEdge> getLongestShortPath(SimpleDirectedWeightedGraph<String, ViolationEdge> oldGraph, String rootVertex) {
		ArrayList<ArrayList<ViolationEdge>> allPaths = getAllPathsToLeafs(oldGraph,rootVertex);
		
		double max=0;
		ArrayList<ViolationEdge> longestPath = null;
		System.out.println("S "+allPaths.size());
		HashMap<ViolationPoint,Double> leafs = new HashMap<ViolationPoint,Double>();
		HashMap<ViolationPoint,ArrayList<ViolationEdge>> paths = new HashMap<ViolationPoint,ArrayList<ViolationEdge>>();
		
		//populate the map with the shortest path to arrive to a leaf
		for ( int i = 0; i < allPaths.size(); i++ ){
			ArrayList<ViolationEdge> path = allPaths.get(i);
			
			double length=getPathLen(oldGraph,path);
			
			if ( length == 0 )
				continue;
			
			ViolationEdge lastStep = path.get(path.size()-1);
			
			ViolationPoint to = lastStep.getTo();
			Double value = leafs.get(to);
			if ( value == null){
				leafs.put(to, length);
				paths.put(to, path);
			}else{
				if ( length < value ){
					leafs.put(to, length);
					paths.put(to, path);	
				}
			}
		}
		
		for( ViolationPoint vp : leafs.keySet() ){
			Double len = leafs.get(vp);
			if ( len > max ){
				longestPath = paths.get(vp);
			}
		}
		
		return longestPath;
	}
	
	
	private double getPathLen(SimpleDirectedWeightedGraph<String, ViolationEdge> oldGraph, ArrayList<ViolationEdge> path) {
		double length=0;
		if ( path == null )
			return 0;
		for ( ViolationEdge edge : path ){
			length+=oldGraph.getEdgeWeight(edge);
		}
		//System.out.println(length);
		return length;
	}

	/**
	 * Returns all the paths to leaf in a graph
	 * 
	 * @param oldGraph
	 * @param rootVertex
	 * @return
	 */
	private ArrayList<ArrayList<ViolationEdge>> getAllPathsToLeafs(SimpleDirectedWeightedGraph<String, ViolationEdge> oldGraph, String rootVertex) {
		ArrayList<ArrayList<ViolationEdge>> allPaths = new ArrayList<ArrayList<ViolationEdge>>();
		
		Stack<String> nodes = new Stack<String>();
		
		nodes.add(rootVertex);
		

		
		getAllPathsToLeafs( oldGraph, rootVertex, nodes, allPaths);
		
		return allPaths;
		
		
	}

	private ArrayList<String> getLeafs(SimpleDirectedWeightedGraph<String, ViolationEdge> g) {
		ArrayList<String> leafs = new ArrayList<String>();
		for ( String vertex : g.vertexSet() ){
			if ( g.outDegreeOf(vertex) == 0 ){
				leafs.add(vertex);
			}
		}
		return leafs;
	}

	private void getAllPathsToLeafs(SimpleDirectedWeightedGraph<String, ViolationEdge> graph, String vertex, Stack<String> visitingNodes, ArrayList<ArrayList<ViolationEdge>> allPaths ) {
		Set<ViolationEdge> edges = graph.outgoingEdgesOf(vertex);
		if ( edges.size() == 0 ){
			allPaths.add(getPath(graph,visitingNodes));
		} else {
			for ( ViolationEdge edge : edges ){
				String to = graph.getEdgeTarget(edge);
				visitingNodes.push(to);
				getAllPathsToLeafs(graph, to, visitingNodes, allPaths);
				visitingNodes.pop();
			}
		}
	}

	/**
	 * Return the path that correspond to the sequence of visited nodes in the passed stack
	 * 
	 * @param g
	 * @param nodes
	 * @return
	 */
	private ArrayList<ViolationEdge> getPath(SimpleDirectedWeightedGraph<String, ViolationEdge> g, Stack<String> nodes) {
		ArrayList<ViolationEdge> path = new ArrayList<ViolationEdge>(nodes.size());
		for(int i =0;i<nodes.size()-1;++i){
			
			ViolationEdge edge = g.getEdge(nodes.get(i), nodes.get(i+1));
			
			path.add(edge);
		}
		
		return path;
	}

	/**
	 * Returns all the root nodes in a graph, i.e. the vertex without incoming edges.
	 * 
	 * @param oldGraph
	 * @return
	 */
	private ArrayList<String> getRoots(SimpleDirectedWeightedGraph<String, ViolationEdge> oldGraph) {
		TreeSet<String> roots = new TreeSet<String>();
		for ( String v : oldGraph.vertexSet() ){
			if ( oldGraph.incomingEdgesOf(v).size() == 0 ){
				roots.add(v);
			}
		}
		
		ArrayList<String> toInspect = new ArrayList<String>();
		toInspect.addAll(roots);
		
		//remove multiple roots
		for ( String from : roots ){
			if ( !toInspect.contains(from) )
				continue;
			
			for ( String to : roots ){
				if ( to == from )
					continue;
				if ( !toInspect.contains(to) )
					continue;
				
				AsUndirectedGraph<String, ViolationEdge> aug = new AsUndirectedGraph<String, ViolationEdge>(oldGraph);
				List<ViolationEdge> path = DijkstraShortestPath.findPathBetween(aug, from, to);
				if ( path != null ){
					System.out.println("Removing "+to+" reachable from "+from);
					toInspect.remove(to);
				}
			}
		}
		
		
		return toInspect;
	}

	private static String getVpName(ViolationPoint vp) {
		
		return String.format("%3d", Integer.valueOf(vp.getViolation().getId()) )+":"+String.format("%3d", Integer.valueOf(vp.getTimeStamp()));
	}

}
