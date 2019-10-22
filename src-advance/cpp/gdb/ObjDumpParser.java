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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parser an objdump file created with the command
 * 
 * objdump -l -r -g -d <executable> > <objdump_output_file>
 * 
 * @author fabrizio
 *
 */
public class ObjDumpParser {
	private static final Logger LOGGER = Logger.getLogger(ObjDumpParser.class.getCanonicalName());
	
	public void parse ( File objDumpFile, ObjDumpParserListener listener ) throws FileNotFoundException{	
		
		
		BufferedReader reader = new BufferedReader ( new FileReader( objDumpFile ) );

		String line = null;
		int lines = 0;
		
		try {
			
//			Demangler d = new Demangler();
			

			
			boolean newFunction = false;
			
			
			while( ( line = reader.readLine() ) != null ){
				lines++;
				String[] splitted;
				splitted = line.trim().split("(\\t+)");
				
				
				listener.newLine(line);
				
				if ( newFunction && splitted.length == 1){
					String functionName = splitted[0].substring(0, splitted[0].length()-1 );
					functionName = cleanWindowsFunctionNames(functionName);
					listener.newFunctionName( functionName );
					newFunction = false;
					continue;
				}
				newFunction = false;  //newFunction must be always reset at the line that follows he newFunction
				
				//System.out.println(splitted.length);
				if ( splitted.length == 1 ) {

					
					
					splitted = splitted[0].split(" ");
					
					
					
					if ( splitted[0].matches("(\\p{Print}|\\p{Blank})+:\\d+" ) ){
						String content = splitted[0];
						int pos = content.lastIndexOf(":");
						
//						splitted = splitted[0].split(":");
						String fileLocation = content.substring(0,pos);
						String lineNoString = content.substring(pos+1);
								//splitted[1])
						if ( EnvUtil.isWindows() ){
							fileLocation = EnvUtil.getWinPathFromCygWinPath(fileLocation);
						}
						//System.out.println(fileLocation);
						listener.newSourceLocation( fileLocation, Integer.valueOf( lineNoString ) );
						
					} else if ( splitted.length == 2 && splitted[1].endsWith(":") ){



						String curFunc = splitted[1].substring(0,splitted[1].length()-1);



						String enterPos = splitted[0];

						curFunc = getNameNoBrackets( curFunc );

						//					if ( curFunc.contains("stub") ){
						//						stubs.add( curFunc );
						//						continue;
						//					}
						curFunc = cleanWindowsFunctionNames(curFunc);
						LOGGER.fine("Line: "+lines);
						listener.newFunction ( curFunc, enterPos );
						newFunction=true;
					}
				}
				if ( splitted.length == 3 ){
					
					String address = splitted[0];
					if (address.endsWith(":") ){
						address = address.substring(0,address.length()-1);
					}
					
					listener.instruction(address, line );
					
					
					
					splitted = splitted[2].split("(\\s+)");
					if ( splitted[0].equals("leave") || splitted[0].equals("leaveq") ) {
						listener.leaveInstruction(address);
					} else if ( splitted[0].equals("ret" ) || splitted[0].equals("retq" ) ){
						
						
						listener.returnInstruction(address);
					} else if (splitted[0].equals("fldl")) {
						String jmpToAddress = splitted[1];
						if ( jmpToAddress.startsWith("*0x") ){
							jmpToAddress = jmpToAddress.substring(3);
						}
						
						listener.fldlInstruction(address, jmpToAddress  );
					} else if (splitted[0].equals("call") || splitted[0].equals("callq") ) {
						
						
						
						String callee = splitted[splitted.length - 1];

						String calleeName = ObjdumpExtractCallPoints
								.getNameNoBrackets(callee);
						calleeName = cleanWindowsFunctionNames(calleeName);
						
						listener.callInstruction(address, calleeName );
					} else if (splitted[0].equals("jmp")) {
						
						String jmpToAddress = splitted[1];
						if ( jmpToAddress.startsWith("*0x") ){
							jmpToAddress = jmpToAddress.substring(3);
						}
						
						listener.jmpInstruction(address, jmpToAddress  );
					} else if ( splitted[0].equals("pop") ){
						String register = splitted[1];
						if ( "%ebp".equals(register) ){
							listener.popEbp( address );
						}
					}
					
				}
				
				
			}

			

		} catch ( Exception e ) {
			LOGGER.log(Level.SEVERE,"Error processing line "+lines+" : "+line, e );
		} finally {
			if ( reader != null ){
				try {
					listener.objdumpEnd();
					reader.close();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE,"Error closing trace.", e );
				}
			}
		}	

		
	}


	private String cleanWindowsFunctionNames(String functionName) {
		if ( EnvUtil.isCygWin() ){
			if ( functionName.startsWith("_") ){
				functionName = functionName.substring(1);
			}
		}
		return functionName;
	}
	
	
	private void setWithinCall() {
		// TODO Auto-generated method stub
		
	}


	static String getNameNoBrackets(String curFunc) {
		if ( curFunc.endsWith(">") && curFunc.startsWith("<") ){
			curFunc = curFunc.substring(1,curFunc.length()-1);
		}
		
		return curFunc;
	}
	
	public static void main(String args[]) throws FileNotFoundException{
		ObjDumpPrinterParserListerner printer = new ObjDumpPrinterParserListerner();
		ObjDumpParser p = new ObjDumpParser();
		p.parse(new File(args[0]), printer);
	}
}
