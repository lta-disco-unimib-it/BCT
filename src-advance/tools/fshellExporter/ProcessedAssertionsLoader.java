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
package tools.fshellExporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import util.FileUtil;

public class ProcessedAssertionsLoader {

	public static void saveToTextFiles( LinkedList<ProcessedAssertion> processedAssertions, File modelsFile) throws IOException {
		
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(modelsFile.getAbsolutePath()+".injected") );
			for ( ProcessedAssertion assertion : processedAssertions ){
				if ( assertion instanceof ReturnInstruction ){
					continue; //placeholder, skip
				}
				bw.write(assertion.toStringWithFunctionNameOrLine());
				bw.newLine();
			}
			bw.close();
		}
		
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(modelsFile.getAbsolutePath()+".injected.lines") );
			for ( ProcessedAssertion assertion : processedAssertions ){
				if ( assertion instanceof ReturnInstruction ){
					continue; //placeholder, skip
				}
				bw.write(assertion.toStringWithLine());
				bw.newLine();
			}
			bw.close();
		}
		
	}
	
	public static List<ProcessedAssertion> loadFromTextFiles( File modelsFile ) throws FileNotFoundException{
		List<String> assertionsInjected = FileUtil.getLines(new File(modelsFile.getAbsolutePath()+".injected"));
		List<String> assertionsInjectedLines = FileUtil.getLines(new File(modelsFile.getAbsolutePath()+".injected.lines"));
		
		if ( assertionsInjected.size() != assertionsInjectedLines.size() ){
			throw new IllegalStateException("Files were expected to contain the same number of assertions");
		}
		
		int len = assertionsInjected.size();
		
		ArrayList<ProcessedAssertion> processedAssertions = new ArrayList<ProcessedAssertion>();
		for ( int i = 0 ; i < len; i++){
			String assertionFunc = assertionsInjected.get(i);
			String assertionLine = assertionsInjectedLines.get(i);
			
			String[] func = assertionFunc.split("\t");
			String[] line = assertionLine.split("\t");
			
			String functionName = func[1];
			ProcessedAssertion processedAssertion;
			
			if ( isFunctionName(functionName) ){
				processedAssertion = new ProcessedAssertion(line[0], Integer.valueOf(line[1]), line[2], functionName );
			} else {
				processedAssertion = new ProcessedAssertion(line[0], Integer.valueOf(func[1]), line[2] );
			}
			processedAssertion.setSourceLineNo(Integer.valueOf(line[1]));
		
			processedAssertions.add( processedAssertion );
		}
		
		return processedAssertions;
	}

	private static boolean isFunctionName(String functionName) {
		char firstChar = functionName.charAt(0);
		if ( firstChar >= '0' && firstChar <= '9' ){
			return false;
		}
		return true;
		//		try {
//			Integer lineNumber = Integer.decode(functionName);
//		} catch ( NumberFormatException e ){
//
//		}
	}

}
