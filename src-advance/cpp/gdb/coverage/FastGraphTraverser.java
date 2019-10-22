/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
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
package cpp.gdb.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cpp.gdb.coverage.BranchRegressionSuiteGenerator.TestCoverage;


public class FastGraphTraverser implements GraphTraverser {
	private Logger LOGGER = Logger.getLogger(FastGraphTraverser.class.getCanonicalName());
	
	private boolean toProcess[];
	private boolean processed[];
	
	public List<TestCoverage> getProcessed() {
		return processedTestsList;
	}

	private int increase;
	private boolean[] processedCoverage;
	private List<TestCoverage> processedTestsList = new LinkedList<BranchRegressionSuiteGenerator.TestCoverage>();

	public int getIncrease() {
		return increase;
	}

	public static class Reference {
		Map<FileNameAndCoverageKey<BranchId>,Integer> branches;
		Map<TestCoverage,Integer> tests;
		ArrayList<TestCoverage> testsList;
		private TestCoverage[] testsArray;
		
		public Reference(
				Collection<FileNameAndCoverageKey<BranchId>> branches,
				Collection<TestCoverage> tests) {
			
			this.branches = new HashMap<FileNameAndCoverageKey<BranchId>, Integer>(branches.size());
			this.tests = new HashMap<BranchRegressionSuiteGenerator.TestCoverage, Integer>(tests.size());
			this.testsList = new ArrayList<BranchRegressionSuiteGenerator.TestCoverage>(tests.size());
			this.testsArray = new BranchRegressionSuiteGenerator.TestCoverage[tests.size()];
			
			for ( FileNameAndCoverageKey<BranchId> branch : branches ){
				this.branches.put(branch, this.branches.size());
			}
			
			int pos = 0;
			for ( TestCoverage  test : tests ){
				this.tests.put(test, pos);
				this.testsList.add(test);
				testsArray[pos] = test;
				pos++;
			}
		}
		
		
	}
	
	private Reference reference;
	
	@Override
	public String toString() {
		return "Processed: "+processed+" ; To process: "+toProcess;
	}



	public FastGraphTraverser( ArrayList<FileNameAndCoverageKey<BranchId>> branchesToCover, Collection<TestCoverage> tests){
		reference = new Reference(branchesToCover,tests);
		this.processed = new boolean[reference.tests.size()];
		this.processedCoverage =  new boolean[reference.branches.size()];
		this.increase = 0;
	}
	
	private FastGraphTraverser( FastGraphTraverser gt, TestCoverage test, int increase ){
		reference = gt.reference;
		this.processed = new boolean[reference.tests.size()];
		this.processedCoverage =  new boolean[reference.branches.size()];
		System.arraycopy(gt.processed, 0, processed, 0, processed.length);
		System.arraycopy(gt.processedCoverage, 0, processedCoverage, 0, processedCoverage.length);
		
		this.increase = increase;
		this.processedTestsList.addAll(gt.processedTestsList);
		
		addTestToProcessed(processed,processedCoverage,test);
		this.processedTestsList.add(test);
		
		
	}

	public void addTestToProcessed(boolean[] processed, boolean[] processedCoverage, TestCoverage test) {
		Integer testPos = reference.tests.get(test);
		processed[testPos] = true;
		
		boolean[] testCoverage = test.getCoveredBranchesArray();
		for ( int i = 0; i < processedCoverage.length; i++ ){
			if ( testCoverage [i] ){
				processedCoverage[i] = true;
			}
		}
	}
	
	public List<GraphTraverser> nextStep() {

		List<GraphTraverser> high = findHighestDelta();
		
		return high;
	}


	private List<GraphTraverser> findHighestDelta() {
		
		
		//Pay attention cannot run this method concurrently!!!
		for ( int i=0; i < processed.length; i++ ){
			TestCoverage test = reference.testsArray[i];
			
			if ( processed[i] ){
				test.setCoverageIncrease(-1);
				continue;
			}
			
			test.setCoverageIncrease(calculateTestScore( processedCoverage, test ) );
		}

		LOGGER.fine("SORTING");
		Collections.sort(reference.testsList, new Comparator<TestCoverage>() {

			@Override
			public int compare(TestCoverage o1, TestCoverage o2) {
				return o2.getCoverageIncrease()-o1.getCoverageIncrease();
			}
		});

		int max = reference.testsList.get(0).getCoverageIncrease();
		
		List<GraphTraverser> list = new ArrayList<GraphTraverser>();
//		if ( max == 0 ){
//			return list;
//		}
		
		for ( TestCoverage test : reference.testsList  ){
			if ( test.getCoverageIncrease() != max ){
				return list;
			}
			
			if ( LOGGER.isLoggable(Level.FINE) ){
				LOGGER.fine("ADDING test to new GT "+test.getTestName());
			}
			
			
			FastGraphTraverser gt = new FastGraphTraverser( this, test, max);
			
			list.add(gt);
			
			if ( LOGGER.isLoggable(Level.FINE) ){
				LOGGER.fine("ADDED");
			}
			if ( max <= 1 ){
				return list; //just return a list with one element
			}
		}

		
		
		return list;
	}

	private int calculateTestScore(boolean[] processedCoverage, TestCoverage test ) {
		boolean coverage[] = test.getCoveredBranchesArray();
		
		int score = 0;
		 
		for ( int pos = 0; pos < coverage.length; pos++ ){
			if ( ( ! processedCoverage[pos] ) && coverage[pos] ){
				score++;
			}
		}
		
		return score;
	}

	public void updateTestsScore() {
		boolean[] _processedTests = new boolean[reference.testsArray.length];
		boolean[] _processedCoverage = new boolean[processedCoverage.length];
		for ( TestCoverage test : processedTestsList  ){
			int score = calculateTestScore(_processedCoverage, test);
			addTestToProcessed( _processedTests, _processedCoverage, test );
			test.setCoverageIncrease(score);
		}
		
		
	}



	public void complete() {
		throw new RuntimeException("Not implemented");
	}
//		ArrayList<TestCoverage> missing = new ArrayList<TestCoverage>();
//		missing.addAll(toProcess);
//		Collections.sort(missing, new Comparator<TestCoverage>() {
//
//			@Override
//			public int compare(TestCoverage o1, TestCoverage o2) {
//				int delta = ( o2.getCoverageIncrease() - o1.getCoverageIncrease() );
//				if ( delta != 0 ){
//					return delta;
//				}
//				return o2.getCoveredBranches() - o1.getCoveredBranches();
//			}
//		});
//		toProcess.clear();
////		Collections.sort(missing);
////		Collections.reverse(missing);
//		processed.addAll(missing);
//	}



	public boolean onlyOneMissing() {
		return false;
	}
//		
//		for ( int i = 0; i < processed.length; i++ ){
//			if ( ! processed[i] ){
//				TestCoverage t = reference.testsArray[i];
//				int score = calculateTestScore(processed, t);
//				if ( score > 1 ){
//					return false;
//				}	
//			}
//			
//		}
//		return true;
//	}



	public void clean() {
		processed = null;
		processedCoverage = null;
		toProcess = null;
	}

}