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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cpp.gdb.coverage.BranchRegressionSuiteGenerator.TestCoverage;

public class DeltaSorter {

	private static Logger LOGGER = Logger.getLogger(DeltaSorter.class.getCanonicalName());
	
	public List<TestCoverage> sort(ArrayList<FileNameAndCoverageKey<BranchId>> branchesToCover, List<TestCoverage> requiredTests) {
		if ( requiredTests == null || requiredTests.size() == 0 ){
			return requiredTests;
		}
		
		FastGraphTraverser gt = new FastGraphTraverser(branchesToCover,requiredTests);
		List<GraphTraverser> currentStep = new ArrayList<GraphTraverser>();
		currentStep.add(gt);
		
		int processed = 0;
		int missing = requiredTests.size();
		
		
		List<GraphTraverser> nextStep;
		
		List<GraphTraverser> onlyOne = new ArrayList<GraphTraverser>();
		do {
			LOGGER.fine("STEP "+processed);
			nextStep = new ArrayList<GraphTraverser>();
			
			for ( GraphTraverser current : currentStep ){
				nextStep.addAll(current.nextStep());
			}

			Runtime.getRuntime().gc();
			
			Collections.sort(nextStep, new Comparator<GraphTraverser>() {

				@Override
				public int compare(GraphTraverser o1, GraphTraverser o2) {
					return o2.getIncrease()-o1.getIncrease();
				}
			
			});
			
			if ( nextStep.size() == 0 ){
				//We are done
				break;
			}
			
			int max = nextStep.get(0).getIncrease();
			
			for ( int i = nextStep.size()-1 ; i >=0 ; i-- ){
				GraphTraverser current = nextStep.get(i);
				if ( current.getIncrease() < max || i > 4 ){
					nextStep.remove(i);
					continue;
				}
				
				
				
				if ( LOGGER.isLoggable(Level.FINE) ){
					current.updateTestsScore();
					LOGGER.fine("KEEP "+current);
				}
				
				if ( current.onlyOneMissing() ){
					current.complete();
					onlyOne.add(current);
					break;
				}
			}
			
			
			
			
			
			processed++;
			missing--;
			
			if ( onlyOne.size() > 0 ){
				nextStep = onlyOne;
				break;
			}
			
			currentStep = nextStep;
			
		} while ( missing > 0 );
		
		GraphTraverser result = currentStep.get(0);
		result.updateTestsScore();
		return result.getProcessed();
	}

	private int calculateMaxCoverage(List<TestCoverage> requiredTests) {
		Set<FileNameAndCoverageKey<BranchId>>  added = new HashSet<FileNameAndCoverageKey<BranchId>>();
		for ( TestCoverage  test : requiredTests ){
			added = test.getCoveredBranchesSet();
		}
		return added.size();
	}
	
	
	/*
	 * 
	public List<TestCoverage> _sort(List<TestCoverage> requiredTests) {
		if ( requiredTests == null || requiredTests.size() == 0 ){
			return requiredTests;
		}
		
		
		
		ICombinatoricsVector<TestCoverage> v = Factory.createVector(requiredTests);
		
		Generator<TestCoverage> gen = Factory.createPermutationGenerator(v);
		
		final int max = maxCoveredBranches(requiredTests);
		
		List<ICombinatoricsVector<TestCoverage>> perms = gen.generateFilteredObjects(new IFilter<ICombinatoricsVector<TestCoverage>>() {

			@Override
			public boolean accepted(long arg0,
					ICombinatoricsVector<TestCoverage> arg1) {
				return arg1.getValue(0).getCoveredBranches() == max;
			}
		});
		
		for ( int i = 1; i < requiredTests.size(); i++ ){
			perms = findBestAtLevel( i, perms );
		}

		ICombinatoricsVector<TestCoverage> best = perms.get(0);
		//trace the increase in coverage for each test case
		scorePermutation(best, true);
		
		return best.getVector();
		
	}

	public int maxCoveredBranches(List<TestCoverage> requiredTests) {
		int max = 0;
		for ( TestCoverage test : requiredTests ){
			if ( test.getCoveredBranches() > max ){
				max = test.getCoveredBranches(); 
			}
		}
		return max;
	}
	
	

	private List<ICombinatoricsVector<TestCoverage>> findBestAtLevel(int level, List<ICombinatoricsVector<TestCoverage>> perms) {
		int max = -1;
		
		
		List<ICombinatoricsVector<TestCoverage>> res = new ArrayList<ICombinatoricsVector<TestCoverage>>();
		
		for ( ICombinatoricsVector<TestCoverage> perm : perms){
			int score = scorePermutation( perm, false, level );
			if ( score > max ){
				max = score;
				res = new ArrayList<ICombinatoricsVector<TestCoverage>>();
				res.add(perm);
			} else if ( score == max ){
				res.add(perm);
			}
		}
		
		return res;
	}

	private int scorePermutation(ICombinatoricsVector<TestCoverage> perm, boolean saveTestScore) {
		return scorePermutation(perm, saveTestScore, perm.getSize());
	}
	
	private int scorePermutation(ICombinatoricsVector<TestCoverage> perm, boolean saveTestScore, int last ) {	
		Set<FileNameAndCoverageKey<BranchId>> permutationCoverage = new HashSet<FileNameAndCoverageKey<BranchId>>();
		int permutationScore = 0;
		
		last = Math.min(last, perm.getSize());
		for ( int i = 0; i < last; i++ ){
			
			TestCoverage test = perm.getValue(i);
			int testScore = scoreTest(permutationCoverage, test);
			
			if ( saveTestScore ){
				test.setCoverageIncrease( testScore );
			}
			
			permutationScore += testScore;
		}
		
		return permutationScore;
	}

	public int scoreTest(
			Set<FileNameAndCoverageKey<BranchId>> permutationCoverage,
			TestCoverage test) {
		Set<FileNameAndCoverageKey<BranchId>> testCoverage = test.getCoveredBranchesSet();
		
		int oldSize = permutationCoverage.size();
		permutationCoverage.addAll(testCoverage);
		int newSize = permutationCoverage.size();
		int testScore = newSize - oldSize;
		return testScore;
	}
*/
}
