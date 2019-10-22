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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;



public class ObjdumpExtractCallPoints {
	
	
//	public static void main(String args[]){
//		
//		File objdumpFile = new File( args[0] );
//		
//		File resultFile = new File( args[1] );
//		
//
//		try {
//			List<FunctionMonitoringData> entryPoints = extractFunctionEntryExitPoints( objdumpFile );
//		
//			FileWriter fileWriter = new FileWriter(resultFile);
//			
//			for (FunctionMonitoringData entryPoint : entryPoints ){
//				writeEntryPoint( fileWriter, entryPoint );
//			}
//			
//			fileWriter.close();
//			
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//	}
	
//	public static void writeEntryPoint(FileWriter fileWriter, FunctionMonitoringData entryPoint) throws IOException {
//		String begin;
//		
//		if ( entryPoint.isEnter() ){
//			begin = "ENTER";
//		} else {
//			begin = "EXIT";
//		}
//		
//		fileWriter.write(begin+" "+entryPoint.getMangledName()+" "+entryPoint.getAddressEnter()+" -- "+entryPoint.getDemangledName() );
//		System.out.println("Written");
//	}

	public static List<FunctionMonitoringData> extractFunctionEntryExitPoints( File objDumpFile ) throws FileNotFoundException{
		
		
		List<FunctionMonitoringData> result = new ArrayList<FunctionMonitoringData>();
		HashSet<String> stubs = new HashSet<String>();
		
		extractFunctionEntryExitPointsForImplementedFunctions(objDumpFile, result, stubs);
		
		//extractFunctionEntryExitPointsForStubs(objDumpFile, result, stubs);
		
		return result;
		
	}
	
	public static List<FunctionMonitoringData> extractFunctionEntryExitPointsForImplementedFunctions( File objDumpFile, List<FunctionMonitoringData> result, HashSet<String> stubs  ) throws FileNotFoundException{	
		
		
		BufferedReader reader = new BufferedReader ( new FileReader( objDumpFile ) );

		String line;
		
		try {
			
			Demangler d = new Demangler();
			
			FunctionMonitoringData currentFuncData = null;
			
			boolean addedReturn = true;
			String lastLocation = null;
			
			
			
			
			while( ( line = reader.readLine() ) != null ){
				String[] splitted;
				splitted = line.trim().split("(\\s+)");
				
				
				if ( splitted.length < 2 ){
					continue;
				}
				
				
				
				if ( splitted[1].endsWith(":") ){
					
					
					String curFunc = splitted[1].substring(0,splitted[1].length()-1);
					
					
					
					String enterPos = splitted[0];
					
					curFunc = getNameNoBrackets( curFunc );
					
					if ( curFunc.contains("stub") ){
						stubs.add( curFunc );
						continue;
					}
					
					
					String demangled = d.demangle(curFunc);
					
					currentFuncData = new FunctionMonitoringData(curFunc, demangled, enterPos );
					
					result.add(currentFuncData);
				}
				
				if ( splitted.length > 2 ){
					if ( splitted[2].equals("ret" ) || splitted[2].equals("retq" ) ){
						String address = splitted[1];
						if (address.endsWith(":") ){
							address = address.substring(0,address.length()-1);
						}
						
						int missingZeros = 16-address.length();
						if ( missingZeros > 0 ){
							address = addLeadingZeros( address, missingZeros );
						}
						
						currentFuncData.addAddressExit(address);
					}
				}
				
				if ( splitted.length > 0 && splitted[0].endsWith(":") ){
					lastLocation = splitted[0];
				}
			}

			return result;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if ( reader != null ){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	

		return null;
		
	}
	
	
	
	
	
	private static String addLeadingZeros(String address, int zeroesToAdd) {
		StringBuffer zeroesBuf = new StringBuffer(); 
		for ( int i = 0 ; i < zeroesToAdd; i++ ){
			zeroesBuf.append("0");
		}
		
		return zeroesBuf.toString()+address;
	}

	static String getNameNoBrackets(String curFunc) {
		if ( curFunc.endsWith(">") && curFunc.startsWith("<") ){
			curFunc = curFunc.substring(1,curFunc.length()-1);
		}
		
		return curFunc;
	}

	
	
	
	
	
	
	
}
