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

import java.util.Set;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctModelViolation;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTree;

/**
 * This class creates an anomaly graph from a given dynamic call tree
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class AnomalyGraphCreator {

	public static AnomalyGraph createAnomalyGraph(DynamicCallTree dct) {
		DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g = new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		Set<BctModelViolation> violations = dct.getModelViolations();
		
		//Add all violations as nodes
		for ( BctRuntimeData parent : violations ){
			g.addVertex(parent.getId());
		}
		
		//add weights
		for ( BctModelViolation parent : violations ){
			for ( BctModelViolation child : violations ){
				if ( parent != child && parent.getCreationTime() <= child.getCreationTime() ){
					DefaultWeightedEdge edge = new DefaultWeightedEdge();
					g.addEdge(parent.getId(), child.getId(), edge );
					
					//weight as the distance between parent and child
					int weight = dct.getUndirectedViolationDistance(parent, child);
					
					g.setEdgeWeight(edge, weight);
				}
			}
		}
		
		return new AnomalyGraph(g,violations);
	}

}
