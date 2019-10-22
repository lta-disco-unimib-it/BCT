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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import util.ProcessRunner;

public class Demangler {
	
	private static final Logger LOGGER = Logger.getLogger(Demangler.class.getCanonicalName()); 
	
	public static final Demangler INSTANCE = new Demangler();
	private Map<String,String> cache = new HashMap<String, String>();

	public String demangle( String functionName ){
		String demangled = cache.get(functionName);
		LOGGER.fine("demangled in cache "+demangled+" for "+functionName);
		if ( demangled == null ){
			LOGGER.warning("Using demangler without previously caching function names slows down the execution!!!");
			demangled = runDemangler(functionName);
			cache.put(functionName, demangled);
		}
		
		return demangled;
	}
	
	
	
	private ArrayList<String> runDemangler( Collection<String> functionNames ){	
		
			if ( functionNames.size() == 0 ){
				LOGGER.warning("Empty list of function names passed");
				return new ArrayList<String>();
			}
		
			ArrayList<String> command = new ArrayList<String>(functionNames.size()+2);
			ArrayList<String> demangledNames = new ArrayList<String>(functionNames.size());
			
			command.add( EnvUtil.getCppFiltPath() );
			command.add(  "-n" );
			for ( String functionName : functionNames ){
				command.add(  functionName );
			}
			
			
			int maxExecutionTime = 0;
			
			StringBuffer output = new StringBuffer();
			StringBuffer error = new StringBuffer();
			
			try {
				
				//				for ( int i = 0 ; i < 2; i++  ){
				ProcessRunner.run(command, output, error, maxExecutionTime);

				String result = output.toString();
				System.out.println("C++FILT OUTPUT: "+result);
				
				
				BufferedReader reader = new BufferedReader( new StringReader ( result ) );
				String line = null;
				while ( ( line = reader.readLine() ) != null ){
					if ( line.length() != 0 ){
						line=line.replaceAll(" ", "");
					}
					demangledNames.add(line);
				}
				//				}
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return demangledNames;
		
	}
	
	private String runDemangler( String functionName ){	
		ArrayList<String> command = new ArrayList<String>();
		
		command.add( EnvUtil.getCppFiltPath() );
		command.add(  "-n" );
			command.add(  functionName );
		
		
		
		int maxExecutionTime = 0;
		
		StringBuffer output = new StringBuffer();
		StringBuffer error = new StringBuffer();
		
		try {
			
			//				for ( int i = 0 ; i < 2; i++  ){
			ProcessRunner.run(command, output, error, maxExecutionTime);

			String result = output.toString().trim();
			//System.out.println("RES "+result);
			if ( result.length() != 0 ){
				return result.replaceAll(" ", "");
			}
			//				}
			
			return functionName;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	
}
	
	
	public static void main( String args[]){
		Demangler d = new Demangler();
		
		String demangled = d.demangle("_printf_frexp");
		
		System.out.println("Demangled "+demangled);
		
		
	}


	public String demangleNoTemplate(String functionName) {
		return DemangledNamesUtils.getSignatureNoTemplate( demangle(functionName) );
	}

	public void cacheAll(Collection<String> mangledNamesSet) {
		ArrayList<String> mangledNames = new ArrayList<String>(mangledNamesSet.size());
		mangledNames.addAll(mangledNamesSet);
		
		ArrayList<String> demangledNames = runDemangler(mangledNames);
		
		
//		System.out.println("demangled names: "+demangledNames);
//		System.out.println("mangled names: "+mangledNames);
		
		int size = mangledNames.size();
		
//		System.out.println("demangled names after size :"+demangledNames);
		for ( int i = 0; i < size; i++ ){
			cache.put(mangledNames.get(i), demangledNames.get(i));
		}
		
	}

}
