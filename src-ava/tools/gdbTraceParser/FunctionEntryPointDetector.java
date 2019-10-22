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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import tools.gdbTraceParser.FunctionEntryPointDetector.FunctionEntryPoint;
import util.StringUtils;
import util.componentsDeclaration.CppMangledSignatureParser;

import cpp.gdb.Demangler;
import cpp.gdb.EnvUtil;

public class FunctionEntryPointDetector {

	private static Logger LOGGER = Logger.getLogger(FunctionEntryPoint.class.getCanonicalName());

	public static class FunctionEntryPoint {
		private String address;
		private int line;

		public FunctionEntryPoint(String address, int line) {
			if ( address == null ){
				address = "";
			}
			this.address = address;
			this.line = line;


		}

		@Override
		public String toString() {
			return "[EntryPoint, address: "+address+" line:"+line+"]";
		}

		public String getAddress() {
			return address;
		}

		public int getLine() {
			return line;
		}

		@Override
		public boolean equals(Object obj) {

			if ( ! ( obj instanceof FunctionEntryPoint ) ){
				return false;
			}

			FunctionEntryPoint rhs = (FunctionEntryPoint) obj;

			if ( ! address.equals(rhs.address) ){
				return false;
			}

			if ( line != rhs.line  ){
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return (line+address).hashCode();
		}


	}

	private Map<String,FunctionEntryPoint> entryPoints = new HashMap<String,FunctionEntryPoint>();
	private GdbInfoExtractor gdbExecutor;

	public FunctionEntryPointDetector( File executable ){
		gdbExecutor = new GdbInfoExtractor(new File(EnvUtil.getGdbExecutablePath()));
		gdbExecutor.setExecutableToAnalyze(executable);
	}

	public FunctionEntryPoint getFunctionEntryPoint( String mangledName, String address ){
		FunctionEntryPoint entryPoint = entryPoints.get(mangledName);
		if ( entryPoint == null ){
			entryPoint = retrieveEntryPointFromGDB(mangledName, address);
			entryPoints.put(mangledName, entryPoint);
		}

		return entryPoint;
	}

	private String retrieveMethodNameFromGDBUsingAddress(String mangledName, String address){
		if ( ! address.startsWith("0x") ){
			address = "0x"+address;
		}
		String output = gdbExecutor.runGdbAndGetOutput("info symbol "+address);

		if ( output == null ){
			return null;
		}

		String lastLine = StringUtils.lastNonEmptyLine(output);
		
		if ( lastLine == null ){
			return null;
		}
		
		int methodNameEnd = lastLine.indexOf(" in section .text");
		if ( methodNameEnd <= 0 ){
			LOGGER.warning("GDB did not find symbol "+address+" for "+mangledName);
			return null;
		}

		String methodName = lastLine.substring(0, methodNameEnd);

		return methodName;
	}

	private FunctionEntryPoint retrieveEntryPointFromGDB(String mangledName, String address) {
		if ( mangledName.startsWith("_ZN") ){
			mangledName = Demangler.INSTANCE.demangle(mangledName);
		}

		String output = gdbExecutor.runGdbAndGetOutput("b "+mangledName);
		if( output == null ){
			LOGGER.warning("GDB output was NULL\n");
			return null;
		}

		FunctionEntryPoint res = generateFunctionEntryPointFromGdb(mangledName, output, 1);
		if ( res == null ){

			//FIXME: following line seem useless...


			LOGGER.warning("No entry point found for "+mangledName+" trying with address to resolve methodName from address");
			String newName = retrieveMethodNameFromGDBUsingAddress(mangledName, address);
			if ( newName != null ){
				output = gdbExecutor.runGdbAndGetOutput("b "+newName);
				if( output != null ){
					res = generateFunctionEntryPointFromGdb(newName, output, 1);	
				}

			}


			if ( res == null ){
				//sometimes on x86_64 and C++ objects are not loaded
				output = gdbExecutor.runGdbAndGetOutput("set breakpoint pending on\nb main\nb "+mangledName);
				if( output != null ){	
					res = generateFunctionEntryPointFromGdb(mangledName, output, 2);
				} else  {
					LOGGER.warning("GDB output was NULL\n");
				}
			}

			if ( res == null ){
				LOGGER.warning("No entry point found for "+mangledName);
				return null;
			}


		}

		return res;
	}

	public static FunctionEntryPoint generateFunctionEntryPointFromGdb(
			String mangledName, String output, int breakPointNumber) {
		BufferedReader r = new BufferedReader(  new StringReader(output) );

		String line;
		String address = null;
		int lineNo = -1;
		try {
			while ( ( line = r.readLine() ) != null ){
				if ( line.startsWith("Breakpoint "+breakPointNumber) ){
					int addressStart = line.indexOf("0x");
					if ( addressStart < 0 ){
						break;
					}
					int addressEnd = line.indexOf(":");
					if ( addressEnd < addressStart ){
						break;
					}

					int lineStart = line.indexOf(" line ");
					if ( lineStart < addressEnd ){
						break;
					}
					lineStart += 6;


					address = line.substring(addressStart, addressEnd);

					int lineEnd = line.length()-1;
					int lastDot = line.lastIndexOf('.');
					if ( lastDot > lineStart ){

						//Useful to avoid excpetions in the following case:
						//Breakpoint 1 at 0x804892f: file Learner.cpp, line 10. (2 locations)
						//when there are two locations the output returned by GDB cannot be trusted..
						if ( lastDot < lineEnd ){
							//we want to include
							//Breakpoint 1 at 0x400a6a: file Data.cpp, line 27.
							break;	
						}
						lineEnd = lastDot;

					}
					String lineString = line.substring(lineStart, lineEnd);
					lineNo = Integer.parseInt(lineString);
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( NumberFormatException e ){
			LOGGER.log(Level.WARNING, "Error processing line :" +lineNo, e);
		}
		if ( address == null || lineNo == -1 ){
			LOGGER.warning("No entry point found for "+mangledName+" address: "+address+" line: "+lineNo);
			LOGGER.warning("GDB output was:\n "+output);
			return null;
		}

		FunctionEntryPoint ep = new FunctionEntryPoint(address, lineNo);
		LOGGER.fine("Returning entry point: "+ep);
		return ep;
	}

}
