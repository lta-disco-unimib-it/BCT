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
/**
 * 
 */
package tools.violationsAnalyzer.anomalyGraph;

import java.util.Collection;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUnweightedGraph;

public class UndirectedAnomalyGraph<V,E> implements UndirectedGraph<V, E>{

	private AsUnweightedGraph<V, E> graph;

	public UndirectedAnomalyGraph(AsUnweightedGraph<V, E> graph) {
		this.graph = graph;
	}

	public boolean addEdge(V arg0, V arg1, E arg2) {
		return graph.addEdge(arg0, arg1, arg2);
	}

	public E addEdge(V arg0, V arg1) {
		return graph.addEdge(arg0, arg1);
	}

	public boolean addVertex(V arg0) {
		return graph.addVertex(arg0);
	}

	public boolean containsEdge(E arg0) {
		return graph.containsEdge(arg0);
	}

	public boolean containsEdge(V arg0, V arg1) {
		return graph.containsEdge(arg0, arg1);
	}

	public boolean containsVertex(V arg0) {
		return graph.containsVertex(arg0);
	}

	public int degreeOf(V arg0) {
		return graph.degreeOf(arg0);
	}

	public Set<E> edgeSet() {
		return graph.edgeSet();
	}

	public Set<E> edgesOf(V arg0) {
		return graph.edgesOf(arg0);
	}

	public boolean equals(Object obj) {
		return graph.equals(obj);
	}

	public Set<E> getAllEdges(V arg0, V arg1) {
		return graph.getAllEdges(arg0, arg1);
	}

	public E getEdge(V arg0, V arg1) {
		return graph.getEdge(arg0, arg1);
	}

	public EdgeFactory<V, E> getEdgeFactory() {
		return graph.getEdgeFactory();
	}

	public V getEdgeSource(E arg0) {
		return graph.getEdgeSource(arg0);
	}

	public V getEdgeTarget(E arg0) {
		return graph.getEdgeTarget(arg0);
	}

	public double getEdgeWeight(E arg0) {
		return graph.getEdgeWeight(arg0);
	}

	public int hashCode() {
		return graph.hashCode();
	}

	public Set<E> incomingEdgesOf(V arg0) {
		return graph.incomingEdgesOf(arg0);
	}

	public int inDegreeOf(V arg0) {
		return graph.inDegreeOf(arg0);
	}

	public int outDegreeOf(V arg0) {
		return graph.outDegreeOf(arg0);
	}

	public Set<E> outgoingEdgesOf(V arg0) {
		return graph.outgoingEdgesOf(arg0);
	}

	public boolean removeAllEdges(Collection<? extends E> arg0) {
		return graph.removeAllEdges(arg0);
	}

	public Set<E> removeAllEdges(V arg0, V arg1) {
		return graph.removeAllEdges(arg0, arg1);
	}

	public boolean removeAllVertices(Collection<? extends V> arg0) {
		return graph.removeAllVertices(arg0);
	}

	public boolean removeEdge(E arg0) {
		return graph.removeEdge(arg0);
	}

	public E removeEdge(V arg0, V arg1) {
		return graph.removeEdge(arg0, arg1);
	}

	public boolean removeVertex(V arg0) {
		return graph.removeVertex(arg0);
	}

	public void setEdgeWeight(E arg0, double arg1) {
		graph.setEdgeWeight(arg0, arg1);
	}

	public String toString() {
		return graph.toString();
	}

	public Set<V> vertexSet() {
		return graph.vertexSet();
	}
	
}