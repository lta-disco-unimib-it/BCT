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
import java.util.List;
import java.util.Set;

import modelsViolations.BctModelViolation;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import tools.violationsAnalyzer.anomalyGraph.AnomalyGraph;
import tools.violationsAnalyzer.anomalyGraph.AnomalyGraphCreator;
import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTree;
import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTreeCreator;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilter;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManager;
import tools.violationsAnalyzer.filteringStrategies.IdManagerAction;



public class BctViolationsAnalyzer {

	private BctRuntimeDataFilter strategy;
	public BctRuntimeDataFilter getStrategy() {
		return strategy;
	}

	public BctViolationsManager getViolationsManager() {
		return violationsManager;
	}

	public FailuresManager getFailuresManager() {
		return failuresManager;
	}

	public IdManager getIdManager() {
		return idManager;
	}

	private BctViolationsManager violationsManager;
	private FailuresManager failuresManager;
	private IdManager idManager;

	public BctViolationsAnalyzer( BctViolationsManager violationsManager, FailuresManager failuresManager, BctRuntimeDataFilter strategy, IdManager  idManager){
		this.violationsManager = violationsManager;
		this.strategy = strategy;
		this.failuresManager = failuresManager;
		this.idManager = idManager;
	}
	
	public BctViolationsAnalysisResult analyze(String failureId){
		List<AnomalyGraph> anomalyGraphs = getAllAnomalyGraphs(failureId);
		AnomalyGraph bestAnomalyGraph = getBestAnomalyGraph(failureId, anomalyGraphs);
		
		return new BctViolationsAnalysisResult(failureId,anomalyGraphs,bestAnomalyGraph);
	}
	
	/**
	 * Returns the anomaly graph that better clusterizes the anomalies detected
	 * 
	 * @param failureId
	 * @return
	 */
	private AnomalyGraph getBestAnomalyGraph(String failureId, List<AnomalyGraph> anomalyGraphs){
		
		 

		//calculate all the gains
		AnomalyGraph previousAnomalyGraph = anomalyGraphs.get(0);
		double previousCoh = previousAnomalyGraph.getGraphInverseCohesion();
		double previousMaxWeight = previousAnomalyGraph.getMaxWeigth();
		
		double gains[] = new double[anomalyGraphs.size()-1];
		
		//If we gave just 2 violations we keep the connected graph as the best
		if ( anomalyGraphs.size() <= 2 ){
			return anomalyGraphs.get(0);
		}
		
		for ( int i = 1; i < anomalyGraphs.size(); ++i ){
			AnomalyGraph currentAnomalyGraph = anomalyGraphs.get(i);
			double currentCoh = currentAnomalyGraph.getGraphInverseCohesion();
			double currentMaxWeight = currentAnomalyGraph.getMaxWeigth();
		
			gains[i-1] = Math.abs(( currentCoh - previousCoh) /(currentMaxWeight-previousMaxWeight));
			
			previousCoh = currentCoh;
			previousMaxWeight = currentMaxWeight;
		}
		
		//detect the best gain
		int bestGainPos = -1;
		double max = 0;
		for ( int i = 0; i < gains.length; ++i ){
		
			if ( gains[i] > max ){
				bestGainPos = i;
				max = gains[i]; 
			}
		}
		
		return anomalyGraphs.get(bestGainPos+1);
		
	}
	
	/**
	 * Return a list of BctViolations that correspond to the filtering criteria defined with the filtering strategy
	 * applied to the failure to analyze
	 * 
	 * @param failureId the id of the failure to consider
	 * @return
	 */
	public List<BctModelViolation> getFilteredBctViolations(String failureId){
		return strategy.getFilteredData(violationsManager, failuresManager, idManager, failureId);
	}
	
	/**
	 * Returns all the anomaly graphs obtained by iteratively removing the maximum weights
	 * 
	 * @param failureId
	 * @return
	 */
	private List<AnomalyGraph> getAllAnomalyGraphs(String failureId){
		
		ArrayList<AnomalyGraph> anomalyGraphs = new ArrayList<AnomalyGraph>();
		List<BctModelViolation> violations = getFilteredBctViolations(failureId);
		
//		for ( BctModelViolation violation : violations ){
//			System.out.println(violation.getViolatedModel());
//		}
		
		DynamicCallTree dct = DynamicCallTreeCreator.createDynamicCallTree(violations);
		
		AnomalyGraph anomalyGraph = AnomalyGraphCreator.createAnomalyGraph(dct);
		int connectedComponetsNumber = anomalyGraph.getConnectedComponents().size();
		anomalyGraphs.add(anomalyGraph);
		
		while ( connectedComponetsNumber < anomalyGraph.getVertexesNumber() ){
			anomalyGraph = getNextAnomalyGraph(anomalyGraph,connectedComponetsNumber);
			anomalyGraphs.add(anomalyGraph);
			connectedComponetsNumber = anomalyGraph.getConnectedComponents().size();
		}
		
		return anomalyGraphs;
	}

	/**
	 * Given an anomaly graph removes edges starting from the ones with the highest weight, while it detects a graph with more connected components
	 * 
	 * @param anomalyGraph
	 * @param connectedComponetsNumber
	 * @return
	 */
	private AnomalyGraph getNextAnomalyGraph(AnomalyGraph anomalyGraph,
			int connectedComponetsNumber) {
		DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = anomalyGraph.getGraph();
		
		int currentComponentsNumber = connectedComponetsNumber;
		List<Set<String>> currentComponents = null; 
		
		while ( currentComponentsNumber == connectedComponetsNumber ){
			Double mw = GraphUtil.getMaxWeight(graph);
			Set<DefaultWeightedEdge> maxEdges = GraphUtil.getEdgesWithWeight(graph,mw);
			graph.removeAllEdges(maxEdges);
			currentComponents = GraphUtil.getConnectedComponents(graph);
			currentComponentsNumber = currentComponents.size();
		}
		
		AnomalyGraph ag = new AnomalyGraph(graph,anomalyGraph.getViolations());
		ag.setConnectedComponents(currentComponents);
		
		return ag;
	}
}
