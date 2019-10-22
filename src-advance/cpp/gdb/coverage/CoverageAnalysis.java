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

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.TreeSet;

public class CoverageAnalysis {
	
	public static void main(String[] args) {
		if (args.length != 2) {
			throw new InvalidParameterException("The parser must be invoked with the following parameters: " 
					+ "\n- source folder path\n- destination file path");
		}
		
		GCovExecutor executor = new GCovExecutor();
		executor.runGCov(args[0]);
		
		GCovLineCoverageParser parser = new GCovLineCoverageParser(args[0], args[1]);
		parser.parseAll();		
		parser.save();
		
		System.out.println(parser.getMap());
	}

	public static <T extends Comparable<T>> void printCoverageMap(
			Hashtable<FileNameAndCoverageKey<T>,Integer> hashtable) {
		TreeSet<FileNameAndCoverageKey<T>> toPrint = new TreeSet<FileNameAndCoverageKey<T>>();
		
		for ( Entry<FileNameAndCoverageKey<T>, Integer> e : hashtable.entrySet() ){
			if ( e.getValue() > 0 ){
				toPrint.add(e.getKey());
			}
		}
		
		for ( FileNameAndCoverageKey<T> f : toPrint ){
			System.out.println( f.toString() + " : " + hashtable.get(f));
		}
	}
}
