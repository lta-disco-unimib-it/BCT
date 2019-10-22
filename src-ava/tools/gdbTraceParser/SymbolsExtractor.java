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
package tools.gdbTraceParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import util.ProcessRunner;

public class SymbolsExtractor {
	
	private String nmExecutable = "nm";

	public Set<String> getMangledFunctions(String executablePath){
		
		HashMap<String, String> symbols = retrieveSymbolAddressesFromNM(executablePath);
		
		return symbols.keySet();
	}
	
	public String getAddressForFunction(String executablePath, String mangledFunction){
		
		HashMap<String, String> symbols = retrieveSymbolAddressesFromNM(executablePath);
		
		return symbols.get(mangledFunction);
	}

	public HashMap<String,String> retrieveSymbolAddressesFromNM(String executablePath){
		String output = executeNM(executablePath);
		
		BufferedReader r = new BufferedReader ( new StringReader( output ) );
		
		
		HashMap<String, String> result = new HashMap<String,String>();
		String line;
		try {
			while ( ( line = r.readLine() ) != null ){
				String[] lineContent = line.split(" ");
				String functionName = lineContent[lineContent.length-1];
				
				String address = null;
				if ( lineContent.length >= 3 ){
					address = "0x"+lineContent[lineContent.length-3];
					
				}
				
				result.put(functionName, address);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}

	public String executeNM(String executablePath) {
		ArrayList<String> command = new ArrayList<String>();
		command.add(nmExecutable);
		command.add(executablePath);
		
		StringBuffer outputBuffer = new StringBuffer();
		StringBuffer errorBuffer = new StringBuffer();
		
		try {
			ProcessRunner.run(command, outputBuffer, errorBuffer, 5);
			
			//System.out.println(outputBuffer);
			
			return outputBuffer.toString();

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
