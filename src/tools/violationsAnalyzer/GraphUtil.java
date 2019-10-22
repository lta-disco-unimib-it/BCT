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
package tools.violationsAnalyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.AsUnweightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import tools.violationsAnalyzer.anomalyGraph.UndirectedAnomalyGraph;

public class GraphUtil {

	/**
	 * Returns the maximum weight in a weighted graph
	 * 
	 * @param graph
	 * @return
	 */
	public static Double getMaxWeight(
			WeightedGraph<String, DefaultWeightedEdge> graph) {
		
		double max = Double.NEGATIVE_INFINITY;
		Set<DefaultWeightedEdge> edges = graph.edgeSet();
		
		for( DefaultWeightedEdge edge : edges){
			double w = graph.getEdgeWeight(edge);
			if ( w > max ){
				max = w;
			}
		}
		
		
		return max;
	}

	/**
	 * Returns all the edges in a graph that have a weight of the given value
	 * @param graph
	 * @param weight
	 * @return
	 */
	public static Set<DefaultWeightedEdge> getEdgesWithWeight(
			DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph, double weight) {
		Set<DefaultWeightedEdge> edges = new HashSet<DefaultWeightedEdge>();
		
		for ( DefaultWeightedEdge edge : graph.edgeSet() ){
			if ( graph.getEdgeWeight(edge) == weight ){
				edges.add(edge);
			}
		}
		
		return edges;
	}

	/**
	 * Returns the connected components of a graph
	 * 
	 * @param graph
	 * @return
	 */
	public static List<Set<String>> getConnectedComponents(
			WeightedGraph<String, DefaultWeightedEdge> graph) {
		AsUnweightedGraph<String, DefaultWeightedEdge> dg = new AsUnweightedGraph<String, DefaultWeightedEdge>(graph);
		
		UndirectedAnomalyGraph<String, DefaultWeightedEdge> uag = new UndirectedAnomalyGraph<String, DefaultWeightedEdge>(dg);
		
		ConnectivityInspector<String, DefaultWeightedEdge> c = new ConnectivityInspector<String, DefaultWeightedEdge>(uag);
		
		List<Set<String>> result = new ArrayList<Set<String>>();
		for ( Set<String> origSet : c.connectedSets() ){
			HashSet<String> newSet = new HashSet<String>();
			newSet.addAll(origSet);
			result.add(newSet);
		}
		return result;
		
	}

	/**
	 * Returns the root nodes of  a graph
	 * 
	 * @param graph
	 * @return
	 */
	public static List<String> getRoots(DirectedGraph<String, DefaultEdge> graph) {
		
		List<String> roots = new ArrayList<String>();
		
		for ( String vertex : graph.vertexSet() ){
			if ( graph.incomingEdgesOf(vertex).size() == 0 ){
				roots.add(vertex);
			}
		}
		
		return roots;
	}

	
	public static List<String> getRoots(
			DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph) {
		List<String> roots = new ArrayList<String>();
		
		for ( String vertex : graph.vertexSet() ){
			if ( graph.incomingEdgesOf(vertex).size() == 0 ){
				roots.add(vertex);
			}
		}
		
		return roots;
	}

}
