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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class GCovLineCoverageParser extends GCovParser<Integer> {
	
	public GCovLineCoverageParser(String sourcesFolderPath,
			String destinationFilePath) {
		super(sourcesFolderPath, destinationFilePath);
		
	}

	protected void parseFile(File file) {
		System.out.println("Parsing " + file.getName());
		Scanner sc = null;
		
		try {
			sc = new Scanner(file);
			sc.useDelimiter(":\\s*");
			
			sc.next(); //Line executions counter
			sc.next(); //Line number
			sc.next(); //String "Source"
			
			
			String sourceFile = sc.nextLine();
			if ( sourceFile == null ){
				try {
					sourceFile = removeGCovExtension(file.getCanonicalPath().toString());
				} catch (IOException e) {
					sourceFile = removeGCovExtension(file.getAbsolutePath().toString());;
				}
			} else {
				sourceFile = sourceFile.substring(1);
			}
			
//			System.out.println("SOURCE "+sourceFile);
			
			while (sc.hasNextLine()) {
				String count = sc.next(); //Line executions counter
				String lineNumber = sc.next(); //Line number
				sc.nextLine();
				
				int lineNumberInt = Integer.valueOf(lineNumber);
				if (lineNumberInt == 0) { //Skip the .gcov file header
					continue; 
				}
				
				count = count.trim();
				try {
					int lineExecutionCount = Integer.parseInt(count);
					System.err.println("line " + lineNumber + " executed " + lineExecutionCount + " times");
					
					int valueToAdd = getCoverageValueToAdd(lineExecutionCount);
					
					FileNameAndCoverageKey<Integer> key = new FileNameAndCoverageKey<Integer>(sourceFile, lineNumberInt);
					
					addToCoverageMap( key, valueToAdd );
					
				} catch (NumberFormatException e) {
					continue;
				} 
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (sc != null) {
				sc.close();
			}
		}
	}

	

}
