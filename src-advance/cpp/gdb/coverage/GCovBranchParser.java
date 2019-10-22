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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GCovBranchParser extends GCovParser<BranchId> {
	
	public GCovBranchParser(String sourcesFolderPath, String destinationFilePath) {
		super(sourcesFolderPath, destinationFilePath);
		// TODO Auto-generated constructor stub
	}
	
	

	@Override
	protected void parseFile(File file) {
		
		
		try {
			BufferedReader r = new BufferedReader( new FileReader (file) );
			
			String fileName = removeGCovExtension(file.getAbsolutePath());
			
			String line;
			int currentLine = -1;
			
			while ( ( line = r.readLine() ) != null ){
				if ( line.startsWith("branch") ){
					Integer branchNumber = Integer.valueOf(line.split("\\s+")[1]);
					
					FileNameAndCoverageKey<BranchId> key = new FileNameAndCoverageKey<BranchId>(fileName, new BranchId(currentLine, branchNumber) );
					addToCoverageMap(key, getValue(line));
					
				} else if ( line.contains(":") ){
					String lineNoString = line.split(":")[1];
					try {
						currentLine = Integer.valueOf(lineNoString.trim());
					} catch ( NumberFormatException e ){
						currentLine = -1;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private int getValue(String line) {
		if ( line.endsWith("never executed") ){
			return 0;
		} else if ( line.endsWith("taken 0%") ) {
			return 0;
		} else {
			return 1;	
		}
	}
	
	

}
