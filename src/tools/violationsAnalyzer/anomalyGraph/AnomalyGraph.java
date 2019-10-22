/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package tools.violationsAnalyzer.anomalyGraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctModelViolation;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import tools.violationsAnalyzer.GraphUtil;

/**
 * This class represent an anomaly graph
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class AnomalyGraph implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph;
	private Set<BctModelViolation> violations;
	private List<Set<String>> connectedComponents;
	private Double maxWeigth;
	private List<String> roots;
	

	public AnomalyGraph(DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g,
			Set<BctModelViolation> violations) {
		this.violations = violations;
		this.graph = g;
		this.roots = GraphUtil.getRoots(graph);
	}

	/**
	 * Returns the edge that connect two violations. If the vilations are not connected null is returned.
	 * @param v2
	 * @param v3
	 * @return
	 */
	public DefaultWeightedEdge getEdge(BctRuntimeData v2, BctRuntimeData v3) {
		return graph.getEdge(v2.getId(), v3.getId());
	}
	
	/**
	 * Return the weight of the edge that connects two violations.
	 * If no edge connects the two violations -1 is returned
	 * 
	 * @param v2
	 * @param v3
	 * @return
	 */
	public double getEdgeWeigth(BctRuntimeData v2, BctRuntimeData v3) {
		DefaultWeightedEdge e = getEdge(v2,v3);
		if ( e == null ){
			return -1;
		}
		return graph.getEdgeWeight(e);
	}

	/**
	 * This method returns all the connected components of a graph
	 * 
	 * @return
	 */
	public List<Set<String>> getConnectedComponents() {
		if ( connectedComponents == null ){
			connectedComponents = GraphUtil.getConnectedComponents(graph);
				
			
		}
		return connectedComponents;
		
		
	}

	/**
	 * This method returns the inverseCohesion of the graph calculated as the mean of the inverse cohesionof its connected components
	 * 
	 * @return
	 */
	public double getGraphInverseCohesion(){
		List<Set<String>> components = getConnectedComponents();
		double inverseCohesion = 0;
		for ( Set<String> component : components ){
			inverseCohesion += getInverseCohesion(component);
		}
		return inverseCohesion/components.size();
	}

	/**
	 * This method return the inverse cohesion of a connected component, calculated as the average weight of its vertexes
	 * 
	 * @param component
	 * @return
	 */
	private double getInverseCohesion(Set<String> component) {
		
		Set<DefaultWeightedEdge> edges = getAllEdges(component);
		
		if ( edges.size() == 0 ){
			return 0;
		}
		
		double cohesion = 0;
		for ( DefaultWeightedEdge edge : edges ){
			cohesion+=graph.getEdgeWeight(edge);
		}
		return cohesion/edges.size();
	}

	/**
	 * This method returns all the edges that connect a set of vertexes
	 * 
	 * @param component
	 * @return
	 */
	private Set<DefaultWeightedEdge> getAllEdges(Set<String> component) {
		Set<DefaultWeightedEdge> edges = new HashSet<DefaultWeightedEdge>();
		
		for (String from : component ){
			for (String to : component ){
				DefaultWeightedEdge edge = graph.getEdge(from, to);
				if ( edge != null ){
					edges.add(edge);
				}
			}	
		}
		
		return edges;
	}
	
	/**
	 * Return the maximum edges weight
	 * 
	 * @return
	 */
	public double getMaxWeigth(){
		
		if ( maxWeigth == null ){
			maxWeigth = GraphUtil.getMaxWeight(graph);
		}
		
		return maxWeigth == Double.NEGATIVE_INFINITY ? 0 : maxWeigth;
	}

	public int getEdgesNumber() {
		return graph.edgeSet().size();
	}

	public DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> getGraph() {
		DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g = new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		for ( String vertex : graph.vertexSet() ){
			g.addVertex(vertex);
		}
		
		for ( DefaultWeightedEdge edge : graph.edgeSet() ){
			DefaultWeightedEdge newEdge = new DefaultWeightedEdge();
			g.addEdge(graph.getEdgeSource(edge),graph.getEdgeTarget(edge),newEdge);
			g.setEdgeWeight(newEdge, graph.getEdgeWeight(edge));
		}
		
		return g;
	}

	public Set<BctModelViolation> getViolations() {
		return violations;
	}

	public void setConnectedComponents(List<Set<String>> connectedComponents) {
		this.connectedComponents = connectedComponents;
	}

	/**
	 * Return the root violations
	 * 
	 * @return
	 */
	public Set<BctModelViolation> getRootViolations() {
		Set<BctModelViolation> viols = new HashSet<BctModelViolation>();
		
		for ( BctModelViolation v : violations ){
			if ( roots.contains( v.getId() ) ){
				viols.add(v);
			}
		}
		
		return viols;
	}
	
	/**
	 * Return the root violations ids
	 * 
	 * @return
	 */
	public Set<String> getRootViolationsIds() {
		Set<BctModelViolation> viols = getRootViolations();
		
		Set<String> ids = new TreeSet<String>();
		for ( BctRuntimeData viol : viols ){
			ids.add(viol.getId());
		}
		
		return ids;
	}

	/**
	 * Rrturn the number of vertexes in the graph
	 * 
	 * @return
	 */
	public int getVertexesNumber() {
		return graph.vertexSet().size();
	}
}
