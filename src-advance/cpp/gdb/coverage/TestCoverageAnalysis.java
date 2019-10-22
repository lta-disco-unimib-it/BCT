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
import java.io.FileFilter;
import java.security.InvalidParameterException;

import cpp.gdb.coverage.GCovExecutor.CoverageType;

public class TestCoverageAnalysis {
	
	public static void main(String[] args) {
		if (args.length != 2) {
			throw new InvalidParameterException("The parser must be invoked with the following parameters: " 
					+ "\n- source folder path\n- destination file path");
		}
		
		GCovExecutor executor = new GCovExecutor(CoverageType.LINE);
		executor.runGCov(args[0]);
		
		GCovTestParser parser = new GCovTestParser(args[0], args[1]);
		parser.parseAll();		
		parser.save();
		
		CoverageAnalysis.printCoverageMap(parser.getMap());
		
		File sourceFolder = new File ( args[0] );
		File[] filesToDelete =  sourceFolder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				return ( arg0.getName().endsWith("gcda") );
			}
		});
		
		for ( File fileToDelete : filesToDelete ){
			System.out.println("Deleting "+fileToDelete);
			fileToDelete.delete();
		}
	}
}
