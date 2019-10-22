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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ShowFunctionDetails {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException {
		
		if( args.length == 0 ){
			System.out.println("Usage: "+ShowFunctionDetails.class.getName()+" /path/to/functions.ser <function_name>");
			System.exit(2);
		}
		
		File dest = new File( args[0] );

		Class<?> cl = Class.forName("it.unimib.disco.lta.bct.bctjavaeclipsecpp.core.util.CDTStandaloneCFileAnalyzer");
		if ( cl != null ){
			try {

				CSourceAnalyzerRegistry.setCSourceAnalyzer((CSourceAnalyzer) cl.newInstance());
				
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("no c source analyzer");
		}
		
		Map<String, FunctionMonitoringData> funcs = FunctionMonitoringDataSerializer.load(dest);

		if ( args.length <= 1 ){
			
			System.out.println("Please specify the name of a function.");
			
			System.out.println("Functions:");

			for ( FunctionMonitoringData caller : funcs.values() ){
				System.out.println(caller.getMangledName());
			}
			
			System.exit(2);
		}
		
		FunctionMonitoringData f = funcs.get(args[1]);
		if ( f == null ){
			System.out.println("Not found: "+args[1]);
			
			System.exit(1);
			

		}
		
		System.out.println(f.getMangledName());
		System.out.println("Source: "+f.getSourceFileLocation());
		System.out.println("Target: "+f.isTargetFunction());
		System.out.println("Callers:");
		
		Set<FunctionMonitoringData> callers = f.getCallers();
		
		for ( FunctionMonitoringData caller : callers ){
			String target = "";
			if ( caller.isTargetFunction() ){
				target = "(target)";
			}
			
			System.out.println(caller.getMangledName()+" "+caller.getSourceFileLocation() + target);
		}
		

		
		
		System.out.println("Parameters: ");
		for ( Parameter p  : f.getAllArgs() ){
			System.out.println(p.getName()+" "+p.isPointer()+" "+p.toString());
		}
		
		System.out.println("RV: ");
		Parameter p = f.getReturnParameter();
		System.out.println(p.getName()+" "+p.isPointer()+" "+p.toString());

		
	}

}
