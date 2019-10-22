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
package regressionTestManager.detectionMatrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import regressionTestManager.RegressionTestCollection;
import regressionTestManager.tcData.TestCaseInfo;




public abstract class TcDetectionMatrix <ElementToCover> {
	private ArrayList<TestCaseInfo> testCases = new ArrayList<TestCaseInfo>();
	private ArrayList<ElementToCover> variables = new ArrayList<ElementToCover>();
	private DetectionMatrix dm = new DetectionMatrix();
	private final static String csvSeparator = ",";
	/**
	 * Add informations on what elements the test case cover.
	 * 
	 * @param tcInfo
	 * @param elements
	 */
	public void addTestVector(TestCaseInfo tcInfo, Boolean[] elements) {
		testCases.add(tcInfo);
		Boolean[] _elements = new Boolean[elements.length];
		
		System.arraycopy(elements,0, _elements, 0, elements.length);
		
		dm.addTestVector(_elements);
	}


	public void setElementsToCover(ArrayList<ElementToCover> mts) {
		variables.addAll(mts);
	}
	
	/**
	 * Return a compatibility suite of test cases using branch and bound algorithm. 
	 * @return
	 */
	public RegressionTestCollection branchbound(){
		DetectionMatrix oldMatrix = createDetectionMatrix ( dm.D );
		/*
		for ( int x = 0; x < testCases.size(); x++ ){
			
		}
		*/
		Boolean[] res = dm.branchbound();
		
		dm = oldMatrix;
		return makeRegressionSuite(res);
	}
	

	/**
	 * Return a compatibility suite of test cases using branch and bound algorithm.
	 * @return
	 */
	public RegressionTestCollection greedy(){
		DetectionMatrix oldMatrix = createDetectionMatrix ( dm.D );
		Boolean[] res = dm.greedy();

		dm = oldMatrix;
		return makeRegressionSuite( res );
	}
	

	/**
	 * Create a detection matrix given a vector of bolean arrays
	 * 
	 * @param d
	 * @return
	 */
	private DetectionMatrix createDetectionMatrix(Vector d) {
		Iterator it = d.iterator();
		DetectionMatrix copy = new DetectionMatrix();
		while ( it.hasNext() ){
			Boolean[] values = (Boolean[]) it.next();
			Boolean[] newV = new Boolean[values.length];
			System.arraycopy(values, 0, newV, 0, values.length);
			copy.addTestVector( newV );
		}
		return copy;
	}
	
	/**
	 * Generate a regression test suite with test cases which position is true in the passes array.
	 * @param res
	 * @return
	 */
	private RegressionTestCollection makeRegressionSuite( Boolean[] res ){
		RegressionTestCollection suite = new RegressionTestCollection();
		int i = 0;
		for ( TestCaseInfo testCase : testCases ){
			if ( res[i++].equals(Boolean.TRUE) ){
				suite.addTest(testCase, 100.0);
			}
		}
		return suite;
	}

	public Iterator<Boolean[]> coverageIterator() {
		return this.dm.D.iterator();
	}


	public Iterator<ElementToCover> getElementsToCoverIterator() {
		return variables.iterator();
	}
	
	public ArrayList<ElementToCover> getElementsToCover() {
		ArrayList<ElementToCover> res = new ArrayList<ElementToCover>();
		res.addAll(variables);
		return res;
	}
	
	public Iterator<TestCaseInfo> getTestCasesIterator() {
		return testCases.iterator();
	}


	public int getTrueCount(TestCaseInfo testCase) {
		int x = testCases.indexOf(testCase);
		Boolean[] elements = (Boolean[]) dm.D.get(x);
		int count=0;
		for ( int i = 0; i < elements.length; i++ ){
			if ( elements.equals(Boolean.TRUE) )
				count++;
		}
		return count;
	}
	
	public Boolean[] getCoverageVectorForTest(String testCaseName) {
		int x = getTestPosition(testCaseName);
		if ( x < 0 )
			return null;
		return (Boolean[]) dm.D.get(x);
	}
	
	/**
	 * Return the position in the test cases vector of the thest case whith the given name
	 * Returns -1 if it is npt found
	 * 
	 * @param testCaseName
	 * @return
	 */
	private int getTestPosition(String testCaseName) {
		int pos = 0;
		for ( TestCaseInfo tcInfo : testCases ){
			if ( tcInfo.getName().equals(testCaseName) ){
				return pos;
			}
			++pos;	
		}
		return -1;
	}


	public int getTestCasesNumber() {
		return testCases.size();
	}
	

	public int getElementsSize() {
		if ( dm.D.size() == 0 ){
			return 0;
		}
		return ((Boolean[])dm.D.get(0)).length;
	}

	public boolean equals(Object o){
		if ( o == this ){
			return true;
		}
		
		if ( ! ( o instanceof TcDetectionMatrix ) ){
			return false;
		}
		
		TcDetectionMatrix rhs = (TcDetectionMatrix)o;
		
		return ( testCases.equals( rhs.testCases ) );
		//&& variables.equals(rhs.variables) && dm.equals(rhs.dm) );
	}
	

	public static void printCSVFormat( TcDetectionMatrix dmIO, OutputStream stream ) {
		
		PrintWriter w = new PrintWriter ( stream );
		
		w.write("Test Case");
		Iterator varIt = dmIO.getElementsToCoverIterator();
		while ( varIt.hasNext() ){
			w.write( csvSeparator+varIt.next() );
		}
		w.write("\n");
		
		
		Iterator it = dmIO.coverageIterator();
		Iterator<TestCaseInfo> tcIt = dmIO.getTestCasesIterator();
		while ( it.hasNext() ){
			w.write( tcIt.next().getName() );
			Boolean[] v = (Boolean[]) it.next();
			for( int i = 0; i < v.length; i++ )
				w.write(csvSeparator+v[i].toString());
			w.write("\n");
		}
		w.close();
			
	}

	public static GenericTcDetectionMatrix readCSVFormat( InputStream stream ) throws IOException {
		GenericTcDetectionMatrix dm = new GenericTcDetectionMatrix(); 
		
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		
		//HEADER
		String line = br.readLine();
		if ( line!=null ){
			String[] titles = line.split(csvSeparator);
			
			ArrayList<String> elements = new ArrayList<String>(); 
			
			for( int i = 1; i < titles.length; ++i ){
				elements.add(titles[i]);
			}
			
			dm.setElementsToCover(elements);
			
			
		}
		
		while ( ( line = br.readLine() ) != null  ){
			String[] values = line.split(csvSeparator);
			
			UnknownTestCaseInfo tc = new UnknownTestCaseInfo(values[0]);
			
			Boolean[] covered = new Boolean[values.length-1];
			
			for( int i = 1; i < values.length; ++i ){
				
				boolean value = Boolean.parseBoolean(values[i]);
				
				covered[i-1] = value;
			}
			
			dm.addTestVector(tc, covered);
		}
		
		return dm;
	}
	
	
}