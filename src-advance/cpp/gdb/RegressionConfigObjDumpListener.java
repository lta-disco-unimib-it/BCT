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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegressionConfigObjDumpListener implements ObjDumpParserListener {
	private Logger LOGGER = Logger.getLogger(RegressionConfigObjDumpListener.class.getCanonicalName());

	public static final String STATIC_INITIALIZER_FUNCTION_NAME = "_Z41__static_initialization_and_destruction_0ii";

	public Map<String, FunctionMonitoringData> getFunctionsData() {
		return functionsData;
	}


	private Demangler d = new Demangler();
	private Map<String, FunctionMonitoringData> functionsData = new HashMap<String, FunctionMonitoringData>();
	private FunctionMonitoringData currentFuncData;
	private Set<String> sourcePrefixesToRemove = new HashSet<String>();
	private boolean lastInstructionWasCall;
	private String lastCallee;
	private boolean withinStaticInitializer;
	private Set<LineData> linesLinkedInStaticInitializer = new HashSet<LineData>();
	private String staticinitializerEnterAddress;
	private String staticInitializerEnterAddress;
	private boolean leaveAdded;
	private int previousSourceLocationLine;
	private int currentLine;
	private String addressOfInstructionFollowingLastLocation;
	private String popEbpAddress;
	private int lineInstructionsCounter; //instructions in the current line
	private int popEbpPosition = -1;
	private String lastFldlArg;
	private HashMap<LineData,LineData> observedLines = new HashMap<LineData,LineData>();
	private int currentFunctionInstructions;
	private boolean cppUnitTestFunction;

	public Set<LineData> getLinesLinkedInStaticInitializer() {
		return linesLinkedInStaticInitializer;
	}

	private boolean functionExists(String curFunc) {
		FunctionMonitoringData data = this.functionsData.get(curFunc);
		return data != null;
	}
	
	private FunctionMonitoringData getFunctionMonitoringData(String curFunc) {
		FunctionMonitoringData data = this.functionsData.get(curFunc);
		if ( data == null ){
			data = new FunctionMonitoringData(curFunc );
			functionsData.put(curFunc, data);
		}
		return data;

	}

	public void addSourceLocationPrefixToRemove(String sourceLocationPrefix){
		sourcePrefixesToRemove.add(sourceLocationPrefix);
	}

	public void setSourceLocationPrefixesToRemove(Collection<String> sourceLocationPrefix){
		sourcePrefixesToRemove = new HashSet<String>();

		sourcePrefixesToRemove.addAll(sourceLocationPrefix);
	}

	private void addCalleeInfo(String curFunc, String calleeName, String address) {
		FunctionMonitoringData caller = getFunctionMonitoringData(curFunc);
		FunctionMonitoringData callee = getFunctionMonitoringData(calleeName);

		caller.addCallee(callee);

		callee.addCaller(caller, address);

	}

	@Override
	public void callInstruction(String address, String calleeName) {

		if ( address == null ){
			throw new IllegalArgumentException( "Address cannot be null" );
		}
		lastInstructionWasCall = true;
		lastCallee = calleeName;
		addCalleeInfo(currentFuncData.getMangledName(), calleeName, address);
		
		if ( calleeName != null && 
				( calleeName.startsWith("_ZN7CppUnit12assertEquals") || calleeName.startsWith("_ZN7CppUnit6assert") ) ){
			currentFuncData.setCppUnitTestCase( true );
		}
	}

	

	@Override
	public void newFunction(String curFunc, String address) {
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("newFunction "+curFunc+" "+address);
		}
		
		leaveAdded=false; //reset

		curFunc = ObjDumpParser.getNameNoBrackets( curFunc );

		//					if ( curFunc.contains("stub") ){
		//						stubs.add( curFunc );
		//						continue;
		//					}

		if ( curFunc.equals(STATIC_INITIALIZER_FUNCTION_NAME) ){
			withinStaticInitializer = true;
			staticInitializerEnterAddress = address;
		} else {
			withinStaticInitializer = false;
		}
		
		if ( functionExists( curFunc ) ){
			functionsWithMultipleDefinitions.add( curFunc );
		}
		currentFuncData = getFunctionMonitoringData(curFunc); 	

		//NOT USEFUL
//		String demangled = d.demangle(curFunc);
//		if ( demangled == null ){
//			System.err.println("NULL demangled name for "+curFunc);
//			demangled=curFunc;
//		}
//		currentFuncData.setDemangledName(demangled);

		if ( address == null ){
			throw new IllegalArgumentException("Address cannot be null");
		}
		currentFuncData.setEnterAddress(address);

		currentFunctionInstructions = 0;
	}
	
	private boolean duplicatedFunctionsPruned = false;
	private Set<String> functionsWithMultipleDefinitions = new HashSet<String>();

	public String getStaticInitializerEnterAddress() {
		return staticInitializerEnterAddress;
	}

	@Override
	public void newFunctionName(String substring) {
		// TODO Auto-generated method stub
		//System.out.println("Setting name: "+substring);
		//currentFuncData.setDemangledName(substring);
	}

	@Override
	public void newSourceLocation(String fileLocation, int lineNum) {

//		if ( matchProgramPointMatcher( fileLocation, lineNum ) ){
//			
//		}
		lineInstructionsCounter=0;
		
		
		
		previousSourceLocationLine = currentLine;
		
		if ( withinStaticInitializer ){
			linesLinkedInStaticInitializer.add( new LineData(fileLocation,lineNum) );
		}
		
		LineData lineData = new LineData(fileLocation,lineNum);
		currentFuncData.addLine( lineData );

		if( observedLines.containsKey( lineData ) ){
			LineData prevLineData = observedLines.get(lineData); //retrieve the duplicated line already used in a function
			prevLineData.setDuplicated( true );
			lineData.setDuplicated(true);
		} else {
			observedLines.put( lineData, lineData );
		}
		

		if ( currentFuncData.getSourceFileLocation() != null ){
			if ( LOGGER.isLoggable(Level.FINE) ){
				LOGGER.fine("New Source Location but no current function "+fileLocation+" "+lineNum);
			}
			return; 
		}

		processLocationData ( fileLocation );



	}

	private boolean matchProgramPointMatcher(String fileLocation, int lineNum) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	private LineData getLineData(String fileLocation) {
		int pos = fileLocation.indexOf(':');

		if ( pos <= 0){
			return null;
		}

		return new LineData(fileLocation.substring(0,pos), Integer.valueOf(fileLocation.substring(pos)));
	}*/

	private boolean processLocationData(String fileLocation) {
		boolean within = false;

		currentFuncData.setAbsoluteSourceFileLocation( fileLocation );
		
		
		for( String sourcePrefixToRemove : sourcePrefixesToRemove ){
			sourcePrefixToRemove = sourcePrefixToRemove + File.separator;
//			System.out.println(sourcePrefixToRemove+" "+fileLocation);
			if ( fileLocation.startsWith( sourcePrefixToRemove ) ){
				fileLocation = fileLocation.substring(sourcePrefixToRemove.length());
				within=true;
				break;
			}
		}

		if ( ! within ){
			for( String sourcePrefixToRemove : sourcePrefixesToRemove ){

				File sourceFolder = new File ( sourcePrefixToRemove );
				File sourceFile = new File( fileLocation );
				try {
					String folderPath = sourceFolder.getCanonicalPath()+File.separator;
					String filePath = sourceFile.getCanonicalPath();
					
					if ( filePath.startsWith( folderPath ) ){
						fileLocation = filePath.substring( folderPath.length() );
						within=true;
						break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		
		
		fileLocation = removePathSeparator( fileLocation );
		
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("Setting Source Location to "+fileLocation);
		}

//		System.out.println(fileLocation+" "+within);
		currentFuncData.setSourceFileLocation( fileLocation );
		currentFuncData.setImplementedWithinProject( within );

		return within;
	}
	
	private String removePathSeparator(String fileLocation) {
		if ( fileLocation.startsWith("/") || fileLocation.startsWith("\\") ) {
			fileLocation=fileLocation.substring(1);
		} 
		return fileLocation;
	}

	@Override
	public void leaveInstruction(String address) {
//		Following lines lead to bug: when last source line is monitored, 
//		leave coincides with last source lines, which means that only one  
//		breakpoint is set, the one for the line and not the one for the exit
		
		handleReturnInternal(address);
		leaveAdded=true;
	}

	@Override
	public void returnInstruction(String address) {

		if ( popEbpPosition == lineInstructionsCounter - 1 ){
			//if previous instruction was a pop %ebp we can use that address for return
			address = popEbpAddress;
			popEbpPosition = -1;
			
		}
		
		handleReturnInternal(address);
	}

	private void handleReturnInternal(String address) {
		assert ( addressOfInstructionFollowingLastLocation == null || addressOfInstructionFollowingLastLocation != null ); //yes address.. could be either null or not, functions like <init> do not have any line No. associated
		
		//record the address before the return line, this is the address where jmp instructions are redirected
		currentFuncData.addAddressOfFirstInstrcutionOfLastLine( addressOfInstructionFollowingLastLocation );

		if ( address.equals(addressOfInstructionFollowingLastLocation) ){
			currentFuncData.setLastLineContainsOnlyReturn(true);
		}
		
		if ( leaveAdded ){
			return;
		}
		
		
		//Not necessary
		//address= ObjDumpUtil.addLeadingZerosToAddress(address);
		currentFuncData.addAddressExit(address);
		
		if ( this.withinStaticInitializer ){
			staticinitializerEnterAddress = address;
		}
//		System.out.println("Return for "+currentFuncData.getMangledName());
	}

	public String getStaticinitializerEnterAddress() {
		return staticinitializerEnterAddress;
	}

	@Override
	public void instruction(String address, String string) {
		lineInstructionsCounter++;
		
		if ( currentLine == ( previousSourceLocationLine + 1 ) ){
			addressOfInstructionFollowingLastLocation = address;
			currentFuncData.setAddressOfLastLine( address );
		}
		
		if ( lastInstructionWasCall ){
			lastInstructionWasCall = false;

			FunctionMonitoringData callee = getFunctionMonitoringData(lastCallee);
			callee.addAddressAfterCall( address );
		}
		
		updateEnterAddressIfNeeded(address);

	}

	private void updateEnterAddressIfNeeded(String address) {
		//The following is a BUG-FIX for Windows
		//Under windows the pointer to "this" is 0x1 at the beginning of the method (address "4011fc")
//		//In the following case the value of "this" is visible only at instruction "4011ff", 
		//so we need to set the entering address for function Worker::calculate at address "4011ff", i.e. 3 instructions after the beginning of method
//		//OBJDUMP EXAMPLE
//		004011fc <__ZN6Worker9calculateEii>:
//		_ZN6Worker9calculateEii():
//		/cygdrive/c/workspaceBCT/BCT_TestCasesProject/BCT/workspace_BCT_Testing/UninitializedVars/src/Worker.cpp:16
//		4011fc:	55                   	push   %ebp
//		4011fd:	89 e5                	mov    %esp,%ebp
//		4011ff:	83 ec 0c             	sub    $0xc,%esp
//		

		currentFunctionInstructions++;
		if ( currentFuncData != null && currentFunctionInstructions == 3 && EnvUtil.isWindows() && ! currentFuncData.hasExitAddresses() ){
			currentFuncData.setEnterAddress(address);
		}
	}

	@Override
	public void objdumpEnd() {
//		List<String> mangledNames = new ArrayList<String>(functionsData.size());
		
		removeFunctionsWithMultipleDefinitions();
		
		Demangler.INSTANCE.cacheAll( functionsData.keySet() );
		
		StaticFunctionsFinderFactory.getInstance().cacheAll(functionsData.values());

	}

	private void removeFunctionsWithMultipleDefinitions() {
		for( String key : functionsWithMultipleDefinitions ){
			functionsData.remove(key);
		}
	}

	@Override
	public void jmpInstruction(String address, String jmpToAddress) {
		currentFuncData.addJmpToAddress( address, jmpToAddress );
	}

	@Override
	public void newLine(String line) {
		currentLine++;
	}

	@Override
	public void popEbp(String address) {
		popEbpAddress = address;
		popEbpPosition = lineInstructionsCounter;
	}

	@Override
	public void fldlInstruction(String address, String jmpToAddress) {
		currentFuncData.setLastFldlArg( jmpToAddress );
	}

	

}
