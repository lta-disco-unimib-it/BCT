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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;

import cpp.gdb.coverage.BranchRegressionSuiteGenerator.TestCoverage;

import regressionTestManager.detectionMatrix.DetectionMatrix;

public class BranchRegressionSuiteGenerator {
	///PROPERTIES-DESCRIPTION: Options that control the identification of regression test suites
	
	///configure the BranchRegressionSuiteGenerator to not use delta (true/false)
	private static final String BCT_NO_DELTA = "bct.noDelta";
	
	///configure the BranchRegressionSuiteGenerator to use greedy search (true/false)
	private static final String BCT_GREEDY = "bct.greedy";

	public static class TestCoverage implements Comparable<TestCoverage> {
		private String testName;
		private int coveredBranches;
		private Map<FileNameAndCoverageKey<BranchId>, Integer> coverageMap;
		private int coverageIncrease;
		private Set<FileNameAndCoverageKey<BranchId>> coveredBranchesSet;
		private boolean[] coveredBranchesArray;

		@Override
		public String toString() {
			return "TestName: "+testName+" ("+coveredBranches+","+coverageIncrease+")";
		}
		
		public String getTestName() {
			return testName;
		}
		public int getCoveredBranches() {
			return coveredBranches;
		}
		
		public Set<FileNameAndCoverageKey<BranchId>> getCoveredBranchesSet() {
			if ( coverageMap == null ){
				return new HashSet<FileNameAndCoverageKey<BranchId>>();
			}
			
			if ( coveredBranchesSet != null ){
				return coveredBranchesSet;
			}
			
			HashSet<FileNameAndCoverageKey<BranchId>> set = new HashSet<FileNameAndCoverageKey<BranchId>>();
			for ( Entry<FileNameAndCoverageKey<BranchId>, Integer> entry  : coverageMap.entrySet() ){
				if ( entry.getValue() > 0 ){
					set.add(entry.getKey());
				}
			}
			
			coveredBranchesSet = Collections.unmodifiableSet( set );
			
			return coveredBranchesSet;
		}

		public TestCoverage(String testName, Boolean[] coverageVector, Map<FileNameAndCoverageKey<BranchId>, Integer> coverageMap) {
			this(testName,coverageMap,getCoveredItems( coverageMap ));
			this.coveredBranchesArray = new boolean[coverageVector.length];
			for ( int i = 0; i < coverageVector.length; i++ ){
				coveredBranchesArray[i] = coverageVector[i];
			}
		}
		
		public TestCoverage(String testName, int coveredItems) {
			this(testName,null,coveredItems);
		}

		private TestCoverage(String testName, Map<FileNameAndCoverageKey<BranchId>, Integer> coverageMap, int coveredItems) {
			this.testName = testName;
			
			if ( coverageMap != null ){
				this.coverageMap = new HashMap<FileNameAndCoverageKey<BranchId>, Integer>();
				for ( Entry<FileNameAndCoverageKey<BranchId>, Integer>  entry : coverageMap.entrySet() ){
					if ( entry.getValue() > 0 ){
						this.coverageMap.put(entry.getKey(), entry.getValue() );
					}
				}
			}
			
			this.coveredBranches = coveredItems;
		}
		
		@Override
		public int compareTo(TestCoverage o) {
			return coveredBranches-o.coveredBranches;
		}
		public void setCoverageIncrease(int testScore) {
			this.coverageIncrease = testScore;
		}
		public int getCoverageIncrease() {
			return coverageIncrease;
		}

		public boolean[] getCoveredBranchesArray() {
			return coveredBranchesArray;
		}


	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BranchRegressionSuiteGenerator g = new BranchRegressionSuiteGenerator();
		g.run(args);
	}

	public static class Suite {

		private List<TestCoverage> requiredTests;
		private List<TestCoverage> additionalTests;

		public Suite(List<TestCoverage> requiredTests2,
				List<TestCoverage> additionalTests2) {
			this.requiredTests = requiredTests2;
			this.additionalTests = additionalTests2;
		}

		public List<TestCoverage> getRequiredTests() {
			return requiredTests;
		}

		public List<TestCoverage> getAdditionalTests() {
			return additionalTests;
		}

	}

	private boolean greedy = false;
	private boolean noDelta = false;
	
	public boolean getGreedy() {
		return greedy;
	}

	public void setGreedy(boolean greedy) {
		this.greedy = greedy;
	}

	{
		String greedyVal = System.getProperty(BCT_GREEDY);
		if ( greedyVal != null ){
			greedy = Boolean.valueOf(greedyVal);
		}
		
		String noDeltaString = System.getProperty(BCT_NO_DELTA);
		if ( noDeltaString != null ){
			noDelta = Boolean.valueOf(noDeltaString);
		}
	}

	public void run(String[] args) {

		Suite s = generateSuite(args); 



		System.out.println("Must execute size: "+s.getRequiredTests().size());
		System.out.println("Aditional tests size: "+s.getAdditionalTests().size());

		for ( TestCoverage t : s.getRequiredTests() ){
			System.out.println(t.getTestName()+" "+t.getCoveredBranches()+" "+t.getCoverageIncrease());
		}

		for ( TestCoverage t : s.getAdditionalTests() ){
			System.out.println(t.getTestName()+" "+t.getCoveredBranches()+" "+t.getCoverageIncrease());
		}

	}

	public Suite generateSuite(String[] coverageFiles) {
		DetectionMatrix m = new DetectionMatrix();

		ArrayList<String> tests = new ArrayList<String>();
		ArrayList<Boolean[]> coverageVectors = new ArrayList<Boolean[]>();
		ArrayList<FileNameAndCoverageKey<BranchId>> branchesToCover = new ArrayList<FileNameAndCoverageKey<BranchId>>();
		
		ArrayList<Map<FileNameAndCoverageKey<BranchId>, Integer>> testCoverageMaps = new ArrayList<Map<FileNameAndCoverageKey<BranchId>, Integer>>();

		
		System.out.println("Coverage files : "+coverageFiles.length);
		
		populate(coverageFiles, m, tests, coverageVectors,branchesToCover, testCoverageMaps);

		

		
//
//		Collections.sort(requiredTests);
//		Collections.reverse(requiredTests);
		
		
		

		List<TestCoverage> requiredTests = new ArrayList<TestCoverage>();
		List<TestCoverage> additionalTests = new ArrayList<TestCoverage>();
		
		
		if ( greedy ){
			System.out.println("GREEDY");
			List<TestCoverage> allTests = new ArrayList<TestCoverage>();
			
			for ( int i = 0; i < testCoverageMaps.size(); i++ ){

				String testName = tests.get(i); 
				Boolean[] coverageVector = coverageVectors.get(i);
				Map<FileNameAndCoverageKey<BranchId>, Integer> coverageMap = testCoverageMaps.get(i);	
				

				allTests.add(new TestCoverage(testName, coverageVector, coverageMap));
			}
			
			DeltaSorter ds = new DeltaSorter();
			allTests = ds.sort( branchesToCover, allTests);
			
			requiredTests = new ArrayList<TestCoverage>();
			additionalTests = new ArrayList<TestCoverage>();
			
			for ( TestCoverage tc : allTests ){
				System.out.println(tc.getTestName()+" "+tc.getCoveredBranches()+" "+tc.getCoverageIncrease());
				
				if ( tc.getCoverageIncrease() > 0 ){
					requiredTests.add(tc);
				} else {
					additionalTests.add(tc);
				}
			}
			
			
			
		} else {
			
			Boolean[] requiredCoverage = m.branchbound();




			for ( int i = 0; i < requiredCoverage.length; i++ ){
				Boolean mustInclude = requiredCoverage[i];

				String testName = tests.get(i);
				Boolean[] coverageVector = coverageVectors.get(i);
				Map<FileNameAndCoverageKey<BranchId>, Integer> coverageMap = testCoverageMaps.get(i);	
				

				if ( mustInclude ){
					requiredTests.add(new TestCoverage(testName, coverageVector, coverageMap));
				} else {
					additionalTests.add(new TestCoverage(testName, getCoveredItems( coverageMap )));
				}

			}
			
			
			
			if ( noDelta ){
				Collections.sort(requiredTests);
				Collections.reverse(requiredTests);
				
				Set<FileNameAndCoverageKey<BranchId>> _processedTests = new HashSet<FileNameAndCoverageKey<BranchId>>();
				for ( TestCoverage test : requiredTests  ){
					int score = EagerGraphTraverser.calculateTestScore(_processedTests, test);
					_processedTests.addAll(test.getCoveredBranchesSet());
					test.setCoverageIncrease(score);
				}
				
			} else {
				//We sort the required tests to have first the ones that add more delta to the coverage
				DeltaSorter ds = new DeltaSorter();
				requiredTests = ds.sort(  branchesToCover, requiredTests);
			}
			
			//for the additionalTests we consider the overall coverage of the test (in fact they do not add any delta to the required tests)
			Collections.sort(additionalTests);
			Collections.reverse(additionalTests);

		}

		Suite s = new Suite(requiredTests,additionalTests);
		return s;
	}

	public  void populate(
			String[] args,
			DetectionMatrix m,
			ArrayList<String> tests,
			ArrayList<Boolean[]> coverageVectors,
			ArrayList<FileNameAndCoverageKey<BranchId>> branchesToInclude,
			ArrayList<Map<FileNameAndCoverageKey<BranchId>, Integer>> testCoverageMaps) {

		if ( args.length == 0 ){
			return;
		}

		String first = args[0];

//		branchesToInclude.addAll( extractBranchesToInclude(first) );

		System.out.println("Lines to include: ");
		for ( String line : linesToInclude){
			System.out.println(line);
		}
		
		
		
		
		//		extractBranchesToInclude( );

		Set<FileNameAndCoverageKey<BranchId>> branches = new HashSet<FileNameAndCoverageKey<BranchId>>();
		
		LinkedList<Hashtable<FileNameAndCoverageKey<BranchId>, Integer>> coveredBranchesList = new LinkedList<Hashtable<FileNameAndCoverageKey<BranchId>, Integer>>();
		
		List<String> fileNames = new ArrayList<String>(args.length);
		
		for ( String fileName : args ){
			File file = new File( fileName );
			fileNames.add(file.getName());
			
			Hashtable<FileNameAndCoverageKey<BranchId>, Integer> coveredBranches = GCovParser.<BranchId>load(file.getAbsolutePath());
			
			coveredBranchesList.add(coveredBranches);
			
			branches.addAll(extractBranchesToInclude(coveredBranches));
		}
		
		for ( FileNameAndCoverageKey<BranchId> branch : branches ){
			branchesToInclude.add(branch);
		}
		
		System.out.println("Branches to include: "+branchesToInclude.size());
		for (  FileNameAndCoverageKey<BranchId> b : branchesToInclude ){
			System.out.println(b);
		}
		
		int fileIndex = 0;
		for ( Hashtable<FileNameAndCoverageKey<BranchId>, Integer> coveredBranches : coveredBranchesList ){
			
			
			Boolean[] coverageVector = new Boolean[branchesToInclude.size()];

			TreeMap<FileNameAndCoverageKey<BranchId>,Integer> coveredAndFiltered = new TreeMap<FileNameAndCoverageKey<BranchId>, Integer>();

			int i = -1;
			for ( FileNameAndCoverageKey<BranchId> key : branchesToInclude ){

				Integer value = coveredBranches.get(key);

				coveredAndFiltered.put(key,value);

				i++;
				if ( value>0 ){
					coverageVector[i] = true;
				} else {
					coverageVector[i] = false;
				}
			}

			m.addTestVector(coverageVector);
			coverageVectors.add(coverageVector);
			tests.add(fileNames.get(fileIndex));
			testCoverageMaps.add( coveredAndFiltered );
			
			fileIndex++;
		}
	}

	public List<FileNameAndCoverageKey<BranchId>> extractBranchesToInclude(Hashtable<FileNameAndCoverageKey<BranchId>, Integer> branches) {

		LinkedList<FileNameAndCoverageKey<BranchId>> list = new LinkedList<FileNameAndCoverageKey<BranchId>>();
		
		for ( Entry<FileNameAndCoverageKey<BranchId>, Integer> e : branches.entrySet() ){
			if ( ! accept(e.getKey()) ){
				continue;
			}
			list.add(e.getKey());
		}

		Collections.sort(list);

		return list;
	}

	private HashSet<String> linesToInclude = new HashSet<String>();
	public void addLineToInclude( String path, int line ){
		linesToInclude.add(createLineId(path, line));
	}

	public String createLineId(String path, int line) {
		return path+":"+line;
	}

	private  boolean accept(FileNameAndCoverageKey<BranchId> key) {

		if ( linesToInclude.size() == 0 ){
			return true;
		}

		String filePath = key.getFilePath();
		BranchId branchId = key.getLineNumber();
		int lineNo = branchId.getLineNo();

		try {
			filePath = new File( filePath ).getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String id = createLineId(filePath, lineNo);
		System.out.println("Checking " + id);
		return linesToInclude.contains(id);
	}

	private static int getCoveredItems(
			Map<FileNameAndCoverageKey<BranchId>, Integer> coverageMap) {
		int sum = 0;
		for ( Integer e : coverageMap.values() ){
			sum += e;
		}
		return sum;
	}

}
