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

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import modelsViolations.BctModelViolation;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * This class creates a DynamicCallTree given a list of BctModelsViolations
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class DynamicCallTreeCreator {

	private static class GraphCreator {
		
		private DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class); 
		private HashMap<String,String> threadRoot = new HashMap<String, String>();
		private int nodes = 0;
		private HashMap<DefaultEdge,String> edgeNames = new HashMap<DefaultEdge, String>();
		
		private HashMap<BctModelViolation,String> violationsVertexes = new HashMap<BctModelViolation, String>();
		
		public void addViolation( BctModelViolation violation ){
			String[] stackTrace = violation.getStackTrace();
			
			//get thread root
			String vertex = getThreadRoot(violation.getThreadId());
			
			int topLimit = 0;
			
//			if ( violation.getViolatedModelType() == BctModelViolation.ViolatedModelsTypes.FSA ){
//				topLimit = 1; //problem is in the caller
//			} else {
//				topLimit = 0;
//			}
			
			for ( int i = stackTrace.length-1; i >= topLimit; i-- ){
				vertex = addEdge( vertex, stackTrace[i].split(":")[0]);//FIXME: problem with overloading, we need to identify the real method signature
			}
			
			violationsVertexes.put(violation, vertex);
		}

		private String addEdge(String vertex, String method) {
			Set<DefaultEdge> outs = graph.outgoingEdgesOf(vertex);
			
			for ( DefaultEdge edge : outs ){
				if ( method.equals(getEdgeName(edge))){
					return graph.getEdgeTarget(edge);
				}
			}
			
			return newEdge( vertex, method );
		}

		/**
		 * Create a new edge. Associate the name method to it. Return the arrival vertex.
		 * 
		 * @param vertex
		 * @param method
		 * @return
		 */
		private String newEdge(String vertex, String method) {
			String dest = createNewNode();
			DefaultEdge edge = graph.addEdge(vertex, dest);
			edgeNames.put(edge,method);
			return dest;
		}

		private String getEdgeName(DefaultEdge edge) {
			return edgeNames.get(edge);
		}

		private String getThreadRoot(String threadId) {
			String troot = threadRoot.get(threadId);
			if ( troot == null ){
				troot = createNewNode();
				threadRoot.put(threadId, troot);
			}
			return troot;
		}

		private String createNewNode() {
			String nodeName = String.valueOf(nodes++);
			graph.addVertex(nodeName);
			return nodeName;
		}

		public DirectedGraph<String, DefaultEdge> getGraph() {
			return graph;
		}

		public HashMap<BctModelViolation, String> getViolationsVertexes() {
			return violationsVertexes;
		}
	}
	
	public static DynamicCallTree createDynamicCallTree(List<BctModelViolation> violations){
		
		GraphCreator gc = new GraphCreator();
		for ( BctModelViolation violation : violations ){
			gc.addViolation(violation);
		}
		
		DirectedGraph<String, DefaultEdge> graph = gc.getGraph();
		HashMap<BctModelViolation, String> violsMap = gc.getViolationsVertexes();
		
		return new DynamicCallTree(graph,violsMap);
		
	}
}
