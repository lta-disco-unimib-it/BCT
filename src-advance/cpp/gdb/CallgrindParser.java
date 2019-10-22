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
package cpp.gdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CallgrindParser {

	public Set<String> getCoveredFunctions(File f) throws IOException {
		BufferedReader r = new BufferedReader( new FileReader(f) );
		
		
		Set<String> coveredFunctions = new HashSet<String>();
		
		String line;
		String functionName = null;
		
		while ( ( line = r.readLine() ) != null ){
			
			if ( line.startsWith("cfn")  ){
				
				String[] chunks = line.split("\\s");
				if ( chunks.length >= 2 ){
					functionName = chunks[1];
				}
			} else if ( line.startsWith("calls")  ){
				int eqpos = line.indexOf('=');
				int spacePos = line.indexOf(' ');
				String counterString = line.substring(eqpos+1, spacePos);
				Integer count = Integer.valueOf(counterString);
				if ( count > 0 ){
					coveredFunctions.add(functionName);
				}
			}
		}
		
		return coveredFunctions;
		
	}
	
	public static void main(String args[]) throws IOException{
		CallgrindParser p = new CallgrindParser();
		Set<String> fns = p.getCoveredFunctions(new File(args[0]));
		
		for ( String fn : fns ){
			System.out.println(fn);
		}
		
		System.out.println("Total : "+fns.size());
	}

}
