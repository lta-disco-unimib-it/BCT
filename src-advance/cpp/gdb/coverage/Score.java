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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Score {
	
	public static void main(String[] args) {
		if (args.length != 3) {
			throw new InvalidParameterException("The score calculator must be invoked with the following parameters: " 
					+ "\n- passing coverage data path\n- failing coverage data path\n- destination file path");
		}
		computeScore(args[0], args[1], args[2]);
	}
	
	public static void computeScore(String passingTestsCoverageFile, String failingTestsCoverageFile, String destinationFilePath) {
		Hashtable<FileNameAndCoverageKey<Integer>, Integer> passingCoverage = GCovParser.<Integer>load(passingTestsCoverageFile);
		Hashtable<FileNameAndCoverageKey<Integer>, Integer> failingCoverage = GCovParser.<Integer>load(failingTestsCoverageFile);
		Hashtable<FileNameAndCoverageKey<Integer>, Double> scores = new Hashtable<FileNameAndCoverageKey<Integer>, Double>();
		int totalPassed = 0, totalFailed = 0;
		
		for (Integer coverage : passingCoverage.values()) {
			totalPassed += coverage;
		}
		
		for (Integer coverage : failingCoverage.values()) {
			totalFailed += coverage;
		}
		
		HashSet<FileNameAndCoverageKey<Integer>> allKeys = new HashSet<FileNameAndCoverageKey<Integer>>();
		allKeys.addAll(passingCoverage.keySet());
		allKeys.addAll(failingCoverage.keySet());
		for (FileNameAndCoverageKey<Integer> key : allKeys ) {
			
			float failed = getCoverage(failingCoverage.get(key));
			float passed = getCoverage(passingCoverage.get(key));
			
//			System.out.println(key+" "+failed+" "+passed);
			
			double score = (double) ((failed / totalFailed) / (passed / totalPassed + failed / totalFailed));
//			System.out.println(score);
			scores.put(key, score);
		}
		
		LinkedHashMap<FileNameAndCoverageKey<Integer>, Double> sortedTable = sortByScore(scores);
		save(sortedTable, destinationFilePath);
		
		for ( Entry<FileNameAndCoverageKey<Integer>, Double> e : sortedTable.entrySet() ){
			System.out.println(e.getValue() +" "+e.getKey());
		}
		
//		System.out.println(sortedTable);
	}
	
	private static void save(LinkedHashMap<FileNameAndCoverageKey<Integer>, Double> sortedTable, String destinationFilePath) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(destinationFilePath));
			
			out.println("Line Score");
			for(FileNameAndCoverageKey key : sortedTable.keySet()) {
				Double score = sortedTable.get(key);
				out.println(key + " " + score);
			}
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static LinkedHashMap<FileNameAndCoverageKey<Integer>, Double> sortByScore(Hashtable<FileNameAndCoverageKey<Integer>, Double> table) {
		List<Entry<FileNameAndCoverageKey<Integer>, Double>> list = new LinkedList<Map.Entry<FileNameAndCoverageKey<Integer>, Double>>(table.entrySet());
		Collections.sort(list, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				
				
				Double value1 = ((Map.Entry<FileNameAndCoverageKey<Integer>, Double>) o1).getValue();
				Double value2 = ((Map.Entry<FileNameAndCoverageKey<Integer>, Double>) o2).getValue();
				
				
				int comp = Double.compare(value2, value1);
				
				if ( comp != 0 ){
					return comp;
				}
				
				FileNameAndCoverageKey<Integer> k1 = ((Map.Entry<FileNameAndCoverageKey<Integer>, Double>) o1).getKey();
				FileNameAndCoverageKey<Integer> k2 = ((Map.Entry<FileNameAndCoverageKey<Integer>, Double>) o2).getKey();
				
				comp = k1.getFilePath().compareTo( k2.getFilePath() );
				if ( comp != 0 ){
					return comp;
				}
				
				return k1.getLineNumber().compareTo(k2.getLineNumber());
			}
		});
		
	    LinkedHashMap<FileNameAndCoverageKey<Integer>, Double> result = new LinkedHashMap<FileNameAndCoverageKey<Integer>, Double>();
		for (Iterator<Entry<FileNameAndCoverageKey<Integer>, Double>> it = list.iterator(); it.hasNext();) {
			Entry<FileNameAndCoverageKey<Integer>, Double> next = it.next();
			result.put(next.getKey(), next.getValue());
		}
		
		return result;
	}
	
	private static int getCoverage(Integer coverage) {
		return coverage != null ? coverage : 0; 
	}
}
