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
package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import regressionTestManager.detectionMatrix.GenericTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.tcData.TestCaseInfo;

public class MatrixProcessor {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		if ( args.length < 3 ){
			printUsage();
			System.exit(-1);
		}
		
		File matrixFile = new File(args[0]);
		File resultFile = new File(args[1]);
		HashMap<String,String> replacements = new HashMap<String, String>();
		
		for ( int i = 2; i < args.length; ++i ){
			String[] vals = args[i].split(",");
			replacements.put(vals[0], vals[1]);
		}
		
		TcDetectionMatrix matrix = RegressionInvariantGenerator.getMatrixFromFile(matrixFile);
		
		for ( String replacementKey : replacements.keySet() ){
			System.out.println(replacementKey+" "+replacements.get(replacementKey));
		}
		
		
		HashMap<TestCaseInfo,Boolean[]> tcMap = new HashMap<TestCaseInfo, Boolean[]>();
		Iterator<TestCaseInfo> it = matrix.getTestCasesIterator();
		while ( it.hasNext() ){
			TestCaseInfo tc = it.next();
			tcMap.put(tc, matrix.getCoverageVectorForTest(tc.getName()) );
		}
		
		
		for ( Entry<String,String> e : replacements.entrySet() ){
			String key = e.getKey();
			String mergeTo = e.getValue();
			
			Boolean[] v = matrix.getCoverageVectorForTest(key);
			
			TestCaseInfo toRemove = null;
			
			for ( TestCaseInfo tc : tcMap.keySet() ){
				
				Boolean[] vTo = tcMap.get(tc);
				
				if ( tc.getName().equals(key) ){
					toRemove = tc;
				} else {
					if ( tc.getName().matches(mergeTo) ){
				
					Boolean[] vToNew = new Boolean[vTo.length];
					
					for ( int i = 0; i < vTo.length; ++i ){
						vToNew[i] = vTo[i] || v[i];
					}
					tcMap.put(tc, vToNew);
					
					}
				}
			}
			
			tcMap.remove(toRemove);
		}
		
		GenericTcDetectionMatrix resultMatrix = new GenericTcDetectionMatrix();
		resultMatrix.setElementsToCover(matrix.getElementsToCover());
		
		for ( Entry<TestCaseInfo,Boolean[]> e : tcMap.entrySet() ){
			resultMatrix.addTestVector(e.getKey(), e.getValue());
		}
		
		FileOutputStream os = new FileOutputStream( resultFile );
		TcDetectionMatrix.printCSVFormat(resultMatrix, os);
		
	}

	private static void printUsage() {
		System.out.println("This program given a matrix merge the results of a given test case with the ones matching a regular expression\n" +
				"\nUsage:" +
				"\n\t"+MatrixProcessor.class.getName()+" <originalMatrix> <outputMatrix> <replaceRules>+" +
						"\nOptions are:" +
						"\n\t<oginalMatrix>	path to the matrix in csv format" +
						"\n\t<outputMatrix> path of the file where you want to store the new matrix" +
						"\n\t<replaceRules>+ the rules you need to process the matrix, each rule is in the format:" +
						"\n\t\t\ttestCaseName,regexp\t during the processing step if a test matches the given testCaseName " +
						"\n\t\t\tthen its coverage information is merged (putting in or) with the one " +
						"\n\t\t\tof the test cases matching the given regular expression." +
						"\n\t\t\tEvery rule is separated from the next one by a space.");
		
	}

}
