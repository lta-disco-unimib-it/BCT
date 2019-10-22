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
package tools.violationsAnalyzer.dynamicCallTree;

import java.util.List;
import java.util.Map;
import java.util.Set;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctModelViolation;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import tools.violationsAnalyzer.GraphUtil;

public class DynamicCallTree {
	
	//violation id, node name
	private Map<BctModelViolation,String> violationNodesMap;
	
	private DirectedGraph<String,DefaultEdge> graph;
	private AsUndirectedGraph<String,DefaultEdge> ugraph;
	private List<String> roots;
	
	public DynamicCallTree(DirectedGraph<String,DefaultEdge> graph,Map<BctModelViolation,String> violationNodesMap){
		this.graph = graph;
		this.violationNodesMap = violationNodesMap;
		this.ugraph = new AsUndirectedGraph<String, DefaultEdge>(graph);
		roots = GraphUtil.getRoots( graph );
		
	}
	
	public int getUndirectedViolationDistance( BctModelViolation violationA, BctModelViolation violationB ){
		if ( ! ( violationNodesMap.containsKey(violationA) && violationNodesMap.containsKey(violationB) ) ){ 
			return -1;
		}

		String vertexA = violationNodesMap.get(violationA);
		String vertexB = violationNodesMap.get(violationB);
		List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(ugraph, vertexA , vertexB);
		if ( path == null ){
			//This is the case in which we are comparing distinct threads
			//TODO: are we sure that this is the optimal solution?
			return violationA.getStackTrace().length+violationB.getStackTrace().length+2;
		}
		
		return path.size();
		//return getUndirectedViolationDistance(violationNodesMap.get(violationA),violationNodesMap.get(violationB));
	}
	
//	public int getUndirectedViolationDistance(String vertexA, String vertexB ){
//		List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(ugraph, vertexA , vertexB);
//		if ( path == null ){
//			ugraph.degreeOf();
//			return -1;
//		}
//		return path.size();
//	}
	
	public String toString(){
		//StringBuffer sb = new StringBuffer();
		
		return graph.toString();
	}
	
	public Set<BctModelViolation> getModelViolations(){
		return violationNodesMap.keySet();
	}

	public List<DefaultEdge> getPath(BctRuntimeData v5) {
		String vpos = violationNodesMap.get(v5);
		for ( String root : roots ){
			
			List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(ugraph, root , vpos);
			if ( path != null ){
				return path;
			}
		}
		return null;
		
	}
}
