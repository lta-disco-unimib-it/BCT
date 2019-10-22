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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cpp.gdb.coverage.BranchRegressionSuiteGenerator.TestCoverage;


public class EagerGraphTraverser implements GraphTraverser {
	private Logger LOGGER = Logger.getLogger(EagerGraphTraverser.class.getCanonicalName());
	
	private Set<TestCoverage> toProcess;
	private List<TestCoverage> processed;
	
	public List<TestCoverage> getProcessed() {
		return processed;
	}

	private int increase;
	private Set<FileNameAndCoverageKey<BranchId>> processedCoverage;

	public int getIncrease() {
		return increase;
	}

	
	
	@Override
	public String toString() {
		return "Processed: "+processed+" ; To process: "+toProcess;
	}



	public EagerGraphTraverser( Collection<TestCoverage> toProcess ){
		this.toProcess = new HashSet<BranchRegressionSuiteGenerator.TestCoverage>();
		this.toProcess.addAll(toProcess);
		this.processed = new ArrayList<BranchRegressionSuiteGenerator.TestCoverage>();
		this.processedCoverage = new HashSet<FileNameAndCoverageKey<BranchId>>();
		this.increase = 0;
	}
	
	private EagerGraphTraverser( Set<TestCoverage> toProcess, List<TestCoverage> processed, Set<FileNameAndCoverageKey<BranchId>> processedCoverage, int increase ){
		this.toProcess = toProcess;
		this.processed = processed;
		this.increase = increase;
		this.processedCoverage = processedCoverage;
	}
	
	public List<GraphTraverser> nextStep() {

		List<GraphTraverser> high = findHighestDelta( toProcess );
		
		return high;
	}


	private List<GraphTraverser> findHighestDelta(Set<TestCoverage> _requiredTests) {
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("toProcess : "+_requiredTests);
		}
		
		ArrayList<TestCoverage> requiredTests = new ArrayList<TestCoverage>();
		requiredTests.addAll(_requiredTests);
		
		
		//Pay attention cannot run this method concurrently!!!
		for (TestCoverage test : requiredTests ){
			test.setCoverageIncrease(calculateTestScore( processedCoverage, test ) );
		}

		LOGGER.fine("SORTING");
		Collections.sort(requiredTests, new Comparator<TestCoverage>() {

			@Override
			public int compare(TestCoverage o1, TestCoverage o2) {
				return o2.getCoverageIncrease()-o1.getCoverageIncrease();
			}
		});

		int max = requiredTests.get(0).getCoverageIncrease();
		
		List<GraphTraverser> list = new ArrayList<GraphTraverser>();
		for ( TestCoverage test : requiredTests  ){
			if ( test.getCoverageIncrease() != max ){
				return list;
			}
			
			if ( LOGGER.isLoggable(Level.FINE) ){
				LOGGER.fine("ADDING test to new GT "+test.getTestName());
			}
			
			HashSet<TestCoverage> _toProcess = new HashSet<BranchRegressionSuiteGenerator.TestCoverage>(toProcess.size());
			_toProcess.addAll(toProcess);
			_toProcess.remove(test);
			
			ArrayList<TestCoverage> _processed = new ArrayList<TestCoverage>(processed.size()+1);
			_processed.addAll(processed);
			_processed.add(test);
			
			Set<FileNameAndCoverageKey<BranchId>> newCoveredBranches = test.getCoveredBranchesSet();
			HashSet<FileNameAndCoverageKey<BranchId>> _processedCoverage = new HashSet<FileNameAndCoverageKey<BranchId>>(newCoveredBranches.size()+processedCoverage.size());
			_processedCoverage.addAll(processedCoverage);
			_processedCoverage.addAll(newCoveredBranches);
			
			GraphTraverser gt = new EagerGraphTraverser(_toProcess,_processed, _processedCoverage, max);
			
			list.add(gt);
			
			if ( LOGGER.isLoggable(Level.FINE) ){
				LOGGER.fine("ADDED");
			}
		}

		
		
		return list;
	}

	public static int calculateTestScore(Collection<FileNameAndCoverageKey<BranchId>> _processedTests, TestCoverage test ) {
		Set<FileNameAndCoverageKey<BranchId>> coverage = test.getCoveredBranchesSet();
		
		int score = 0;
		for (  FileNameAndCoverageKey<BranchId>  _branch : coverage ){
			if ( ! _processedTests.contains(_branch) ){
				score++;
			}
		}
		
		return score;
	}

	public void updateTestsScore() {
		Set<FileNameAndCoverageKey<BranchId>> _processedTests = new HashSet<FileNameAndCoverageKey<BranchId>>();
		for ( TestCoverage test : processed  ){
			int score = calculateTestScore(_processedTests, test);
			_processedTests.addAll(test.getCoveredBranchesSet());
			test.setCoverageIncrease(score);
		}
	}



	public void complete() {
		
//		Set<FileNameAndCoverageKey<BranchId>> _processedTests = new HashSet<FileNameAndCoverageKey<BranchId>>();
		
//		ArrayList<TestCoverage> toMove = new ArrayList<TestCoverage>();
//		_processedTests.addAll(processedCoverage);
//		for ( TestCoverage t : toProcess ){
//			int score = calculateTestScore(_processedTests, t);
//			if ( score == 1 ){
//				toMove.add( t );
//			}
//		}
		
		
		
//		Collections.reverse(toMove);
//		toProcess.removeAll(toMove);
//		processed.addAll(toMove);
		
		ArrayList<TestCoverage> missing = new ArrayList<TestCoverage>();
		missing.addAll(toProcess);
		Collections.sort(missing, new Comparator<TestCoverage>() {

			@Override
			public int compare(TestCoverage o1, TestCoverage o2) {
				int delta = ( o2.getCoverageIncrease() - o1.getCoverageIncrease() );
				if ( delta != 0 ){
					return delta;
				}
				return o2.getCoveredBranches() - o1.getCoveredBranches();
			}
		});
		toProcess.clear();
//		Collections.sort(missing);
//		Collections.reverse(missing);
		processed.addAll(missing);
	}



	public boolean onlyOneMissing() {
		Set<FileNameAndCoverageKey<BranchId>> _processedTests = new HashSet<FileNameAndCoverageKey<BranchId>>();
		_processedTests.addAll(processedCoverage);
		for ( TestCoverage t : toProcess ){
			int score = calculateTestScore(_processedTests, t);
			if ( score > 1 ){
				return false;
			}
		}
		return true;
	}



	public void clean() {
		processed = null;
		processedCoverage = null;
		toProcess = null;
	}

}