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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import conf.EnvironmentalSetter;
import util.FileUtil;
import util.HexUtil;
import util.StringUtils;

import cpp.gdb.Demangler;
import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.ModifiedFunctionsDetectorObjDumpListener;
import cpp.gdb.SourceLinesMapper;
import cpp.gdb.SourceMapperException;
import cpp.gdb.TraceUtils;

import dfmaker.core.Variable;
import difflib.StringUtills;
import failureDetection.FailureDetectorFactory;

import tools.gdbTraceParser.GdbThreadTraceListener.UsefulTraceSections;
import tools.gdbTraceParser.GdbThreadTraceParser.CallData;
import traceReaders.raw.FileInteractionTrace;
import util.QuotedStringTokenizer;
import util.componentsDeclaration.Component;
import util.componentsDeclaration.CppMangledSignatureParser;

public class GdbThreadTraceParser {
	

	private static final Logger LOGGER = Logger.getLogger(GdbThreadTraceParser.class.getCanonicalName());

	///PROPERTIES-DESCRIPTION: Options that control the parsing of BCT/RADAR thread traces
	
	///if false BCT fixes the first line of function by looking for proper values invoking gdb during the processing of objdump (default false) 
	public static final String BCT_AVOID_FIXING_LINE_NUMBERS = "bct.gdbThreadTraceParser.avoidFixingLineNumbers";
	
	///if false record whether a variable is not NULL only in the case it is a pointer. If true always record this info in the case of fields/objects/structs. (default false).
	public static final String BCT_ALWAYS_TRACE_NOT_NULL = "bct.gdbThreadTraceParser.alwaysTraceNotNull";
	
	///if true BCT transforms hex to integer values
	public static final String BCT_PROCESSING_HEX_TO_INT = "bct.gdbThreadTraceParser.hexToInt";
	
	private Demangler demangler = new Demangler();
	private EnumValuesDetector enumValuesDetector;
	private boolean useDemangledNames;
	private boolean limitNotNullToPointers = true; //TODO manage as option

	{
		avoidFixingLineNumbers = Boolean.parseBoolean(System.getProperty(BCT_AVOID_FIXING_LINE_NUMBERS) );
		
		limitNotNullToPointers = ! Boolean.parseBoolean(System.getProperty(BCT_ALWAYS_TRACE_NOT_NULL) );
	}
	
	public boolean isLimitNotNullToPointers() {
		return limitNotNullToPointers;
	}

	public void setLimitNotNullToPointers(boolean limitNotNullToPointers) {
		this.limitNotNullToPointers = limitNotNullToPointers;
	}

	public boolean isUseDemangledNames() {
		return useDemangledNames;
	}

	public void setUseDemangledNames(boolean useDemangledNames) {
		this.useDemangledNames = useDemangledNames;
	}

	private interface SectionParser {
		public void processLine( String line )  throws UnexpectedFormaException;
	}

	private class OriginalParser implements SectionParser {
		public void processLine( String line ){
			if ( GdbTraceUtil.isCurrentMethodOnStack( line ) ){
				processMethodOnStackAndExtractParameters( line );	
			} else if ( GdbTraceUtil.isEaxRegisterData( line ) ){
				processEaxRegisterData( line );
			} else if ( GdbTraceUtil.isLocalVariableData( line ) ){
				try {
					processLocalVariableData( line );
				} catch (UnexpectedFormaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//				} else if ( GdbTraceUtil.isProcessEnd( line ) ){
				//					commitMethodCall();

			} else if ( GdbTraceUtil.isGenericStackInfo( line ) ){
				processGenericStackInfo( line );
			}
			
		}
	}
	
	private class SignalParser implements SectionParser  {

		@Override
		public void processLine(String line) {
			
			StackTraceElement stackData = getStackInfoDataFromGdbStackLine("#0 "+line);
			
			currentCall.fileName=stackData.getFileName();
			currentCall.lineNo=stackData.getLineNumber();
			
			try {
			commitMethodCall();
			} catch ( Throwable t ){
				t.printStackTrace();
			}
			sectionParser = NullSectionParser;
		}
		
	}

	private class LocalsSectionParser implements SectionParser  {

		public void processLine( String line ) throws UnexpectedFormaException{
			if ( withinStruct() || printedVariable() || GdbTraceUtil.isLocalVariableData( line ) ){
				try {
					Variable v = createVariableFromVariableInfoLine(line);
					
					if ( v instanceof NotNullVariable ){
						boolean skip = false;
						
						if ( limitNotNullToPointers ) {
							if ( ! ((NotNullVariable) v).isOptimizedOut() ){
								//SKIP the variable
								skip = true;
							}
						}
							
						if ( ! skip ){						
							currentCall.addLocalVariable( v );
						}
					} else {
						currentCall.addLocalVariable( v );
					}
				} catch (IncompleteSequenceException e) {
					//the last variable in vars is an incomplete sequence
					//TODO: we could do something for thata, now we just ignore this info
				} catch (StructEndException e) {
					//it is ok
//					if ( e.getVariable() != null ){
//						currentCall.addLocalVariable( e.getVariable() );
//					}
				} catch (MembersOfStartException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (NoLocalsException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (NoArgumentsException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (StructDepthLimitException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (ParserException e) {
					LOGGER.severe("Error processing line: "+line);
					currentCall.invalidDataValues();
				}
			}
		}

	}

	private class ParentLocalsSectionParser implements SectionParser {
		public void processLine( String line ) throws UnexpectedFormaException{
			if (  withinStruct() || printedVariable() || GdbTraceUtil.isLocalVariableData( line ) ){
				try {
					Variable v = createVariableFromVariableInfoLine(line);
					currentCall.addParentLocalVariable( v );
				} catch (IncompleteSequenceException e) {
					//the last variable in vars is an incomplete sequence
					//TODO: we could do something for thata, now we just ignore this info
				} catch (StructEndException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
//					if ( e.getVariable() != null ){
//						currentCall.addLocalVariable( e.getVariable() );
//					}
				} catch (MembersOfStartException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (NoLocalsException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (NoArgumentsException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (StructDepthLimitException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (ParserException e) {
					LOGGER.severe("Error processing line: "+line);
					currentCall.invalidDataValues();
				} 
			}
		}
	}

	private class StackSectionParser implements SectionParser {
		public void processLine( String line ){
//			System.err.println("STACK-SECTION-PARSER");
			if ( GdbTraceUtil.isCurrentMethodOnStack( line ) ){
				processMethodOnStack( line );	
			} else if ( GdbTraceUtil.isGenericStackInfo( line ) ){
				processGenericStackInfo( line );
			}
		}
	}

	private class NullSectionParser implements SectionParser {
		public void processLine( String line ){

		}
	}

	private class RegistersSectionParser implements SectionParser {
		public void processLine( String line ){
			if ( doubleReturn ){
				doubleReturn = false;
				processDoubleReturn( line );
			} else {
				if ( GdbTraceUtil.isEaxRegisterData( line ) ){
					processEaxRegisterData( line );
				}
			}
		}

		
	}

	private void processDoubleReturn(String line) {
		LOGGER.info("processing double: "+line);
		String[] addresses = line.split("[\t ]+");
		if ( addresses.length != 3 ){
			return;
		}
		LOGGER.info("more than 3");
		String hexValue = createHexValue( addresses[2], addresses[1] );
		String doubleValue = HexUtil.hexToDoubleString(hexValue);
		LOGGER.info(doubleValue);
		currentCall.returnValues.add(
				new Variable( "eax", doubleValue, 1 )
				);
	}
	
	private String createHexValue(String v1, String v2) {
		v1 = v1.substring(2);
		v2 = v2.substring(2);
		return v1 + v2;
	}

	private class ArgsSectionParser implements SectionParser {
		public void processLine( String line ) throws UnexpectedFormaException{
			if (  withinStruct() || GdbTraceUtil.isLocalVariableData( line ) ){
				try {
					Variable v = createVariableFromVariableInfoLine(line);
					currentCall.addParameter(v);
				} catch (IncompleteSequenceException e) {
					//the last variable in vars is an incomplete sequence
					//TODO: we could do something for thata, now we just ignore this info
				} catch (StructEndException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
//					if ( e.getVariable() != null ){
//						currentCall.addParameter( e.getVariable() );
//					}
				} catch (MembersOfStartException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (NoLocalsException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (NoArgumentsException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (StructDepthLimitException e) {
					LOGGER.log(Level.FINE, "Valid parsing result.", e);
				} catch (ParserException e) {
					LOGGER.severe("Error processing line: "+line);
					currentCall.invalidDataValues();
				}
			}
		}
	}

	public final LocalsSectionParser LocalsSectionParser = new LocalsSectionParser();
	public final OriginalParser OriginalParser = new OriginalParser();
	public final NullSectionParser NullSectionParser = new NullSectionParser();
	public final SignalParser SignalSectionParser = new SignalParser();
	public final StackSectionParser StackSectionParser = new StackSectionParser();
	public final ParentLocalsSectionParser ParentLocalsSectionParser = new ParentLocalsSectionParser();

	public final RegistersSectionParser RegistersSectionParser = new RegistersSectionParser();
	public final ArgsSectionParser ArgsSectionParser = new ArgsSectionParser();


	private SectionParser sectionParser = NullSectionParser;

	private TraceState state;
	private List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();
	private int threadId;
	private String currentFunction;
	private Set<String> functionsToFilterOut;
	private enum TraceState { BeforeEnter , BeforeExit, GenericProgramPoint, BeforeEntEx };

	public static class CallData {
		String functionName;
		List<Variable> parameters = new ArrayList<Variable>();
		List<Variable> localVariables = new ArrayList<Variable>();
		List<Variable> parentLocalVariables = new ArrayList<Variable>();
		List<Variable> parentArguments = new ArrayList<Variable>();
		List<Variable> returnValues = new ArrayList<Variable>();
		List<String> globalVariableNames = new ArrayList<String>();
		int lineNo;
		String fileName;
		int threadId;

		LinkedList<StackTraceElement> stack;
		private boolean hasInvalidDataValues = false;
		public boolean toIgnore;

		public void addToStackBottom(StackTraceElement stackData) {
			if ( stack == null ){
				stack = new LinkedList<StackTraceElement>();
			}
			stack.addLast(stackData);
		}

		public void invalidDataValues() {
			hasInvalidDataValues=true;
		}

		public void addParentLocalVariable(Variable v) {
			parentLocalVariables.add(v);
		}

		public void addLocalVariable(Variable v) {
			localVariables.add(v);
		}

		public void addParameter(Variable v) {
			parameters.add(v);
		}

		public void addGlobalVariableName(String nextVariableName) {
			if ( nextVariableName.equals("this") || nextVariableName.equals("*this") ){
				return;
			}
			globalVariableNames.add( nextVariableName );
		}

		public boolean hasInvalidDataValues() {
			return hasInvalidDataValues;
		}
	}

	private CallData currentCall;
	private boolean addFakeCallsAtProcessEnd;
	private int callCounter = -1;
	private LinkedList<String> enteredFunctions=new LinkedList<String>();

	private boolean useOldParser = false;
	private LinkedList<Variable> structs = new LinkedList<Variable>();

	private String nextVariableName;
	private int maxStructDepth = 3;
	private int callstackCounter;
	private String lastFunctionLine;
	private String lastAdditionalReturnLine;

	private boolean floatReturn;

	private boolean doubleReturn;

	private boolean noStructStateCheck = true;

	private boolean PINtrace;

	private boolean changeHexToInt = Boolean.getBoolean(BCT_PROCESSING_HEX_TO_INT);

	public boolean isChangeHexToInt() {
		return changeHexToInt;
	}

	public void setChangeHexToInt(boolean changeHexToInt) {
		this.changeHexToInt = changeHexToInt;
	}

	private int lineCount;

	private FileInteractionTrace currentTrace;

	private boolean skipNext;

	public boolean isPINtrace() {
		return PINtrace;
	}

	public void setPINtrace(boolean pINtrace) {
		PINtrace = pINtrace;
	}

	public boolean isAddFakeCallsAtProcessEnd() {
		return addFakeCallsAtProcessEnd;
	}

	public void setAddFakeCallsAtProcessEnd(boolean addFakeCallsAtProcessEnd) {
		this.addFakeCallsAtProcessEnd = addFakeCallsAtProcessEnd;
	}

	public GdbThreadTraceParser(
			List<GdbThreadTraceListener> listeners) {
		this.listeners.addAll(listeners);
	}

	public void addListener( GdbThreadTraceListener listener ){
		listeners.add( listener );
	}

	private boolean printedVariable() {
		return nextVariableName != null;
	}

	void newSession(String sessionId){
		LOGGER.fine("NEW SESSION: "+sessionId);
		for (  GdbThreadTraceListener listener : listeners ){
			listener.newSession(sessionId);
		}
	}

	void processThreadTrace_old(FileInteractionTrace trace) throws IOException {
		BufferedReader br = null;

		File file = trace.getFile();
		threadId = trace.getThreadId().hashCode();

		int lineCount = 1;
		String line = "";

		try {
			br = FileUtil.createBufferedReaderForTrace(file);


			//			int skip = 2;


			while ( ( line = br.readLine() ) != null ){
				//				System.out.println(lineCount);
				//				if ( skip > 0 ){
				//
				//					if ( GdbTraceUtil.isCurrentMethodOnStack(line) ||
				//							GdbTraceUtil.isBctEnter( line )	){
				//						System.out.println("SKIP "+line);
				//						skip--;
				//						continue;
				//					}
				//				}
				LOGGER.fine(line);


				if ( GdbTraceUtil.isThreadStart( line ) ){
				} else if ( GdbTraceUtil.isBctPoint( line ) ){
					commitMethodCall();
					processBctPoint( line );
				} else if ( GdbTraceUtil.isBctEnter( line ) ){
					commitMethodCall();
					processBctEnter( line );
				} else if ( GdbTraceUtil.isBctExit( line) ){
					commitMethodCall();
					processBctExit( line  );
				} else if ( GdbTraceUtil.isCurrentMethodOnStack( line ) ){
					processMethodOnStackAndExtractParameters( line );	
				} else if ( GdbTraceUtil.isEaxRegisterData( line ) ){
					processEaxRegisterData( line );
				} else if ( GdbTraceUtil.isLocalVariableData( line ) ){
					try {
						processLocalVariableData( line );
					} catch (UnexpectedFormaException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//				} else if ( GdbTraceUtil.isProcessEnd( line ) ){
					//					commitMethodCall();

				} else if ( GdbTraceUtil.isGenericStackInfo( line ) ){
					processGenericStackInfo( line );
				}

				lineCount++;
			}

			commitMethodCall();
			processEnd( line, trace.getFile() );
			//			commitMethodCall();
		} catch ( RuntimeException e ){
			LOGGER.log(Level.SEVERE,"Error processing line "+lineCount+" : "+line,e);
			throw e;
		} finally {
			if ( br != null ){
				br.close();
			}


		}
	}


	private void processVariablePrint(String line) {
		nextVariableName = line.substring(15).trim();
		
		currentCall.addGlobalVariableName( nextVariableName );
	}

	public void processThreadTrace(FileInteractionTrace trace) throws IOException {
		
//		Handler h;
//		try {
//			h = new FileHandler("/tmp/BCT.run.thread.log");
//			h.setLevel(Level.ALL);
//			LOGGER.addHandler(h);
//		} catch ( IOException e1){
//			e1.printStackTrace();
//		} catch (SecurityException e1) {
//			e1.printStackTrace();
//		}
		
		reset();
		
		if ( filterNotTerminatingFunctions ){
			findNotTerminatingFunctions( trace );
		}
		
		optimizeParsing(listeners);
		
		System.err.println("Processing trace: "+trace.getFile().getName()); 
		BufferedReader br = null;

		File file = trace.getFile();
		threadId = trace.getThreadId().hashCode();

		currentTrace=trace;
		lineCount = 1;
		String line = "";
		
		if ( originalSoftwareFunctions == null ){
			LOGGER.warning("originalSoftwareFunctions is null");
		}

		try {
			br = FileUtil.createBufferedReaderForTrace(file);

			for ( GdbThreadTraceListener listener : listeners ){
				listener.traceStart( file );
			}
			
			while ( ( line = br.readLine() ) != null ){
//				System.out.println(lineCount);
//				System.out.println("LINE "+line);
				
				processLine(lineCount, line);



				lineCount++;
			}

			commitMethodCall();
			processEnd( line, trace.getFile() );
			//			commitMethodCall();
		} catch ( RuntimeException e ){
			LOGGER.log(Level.SEVERE,"Error processing line "+lineCount+" : "+line+" (file "+file.getAbsolutePath()+" )", e);
			throw e;
		} finally {
			if ( br != null ){
				br.close();
			}


		}
	}

	

	private void findNotTerminatingFunctions(FileInteractionTrace trace) {
		BufferedReader br;
		try {
			br = FileUtil.createBufferedReaderForTrace(trace.getFile());
			
			HashMap<String, Integer> functionsCounter = new HashMap<String,Integer>();
			HashSet<String> toIgnore = new HashSet<String>();
			
			String line;
			while ( ( line = br.readLine() ) != null ){
//				System.out.println(lineCount);
//				System.out.println("LINE "+line);
				if ( GdbTraceUtil.isBctEnter(line) ) {
					String fName = extractFunctionNameFromFirstBctPointLine(line, true);
					Integer v = functionsCounter.get(fName);
					if ( v == null ){
						v = 0;
						functionsCounter.put(fName, v);
					} else {
						functionsCounter.put(fName, v+1);
					}
				}
				
				if ( GdbTraceUtil.isBctExit(line) ) {
					String fName = extractFunctionNameFromFirstBctPointLine(line, false);
					Integer v = functionsCounter.get(fName);
					if ( v == null ){
						toIgnore.add(fName);
					} else {
						functionsCounter.put(fName, v-1);
					}
					
				}

			}
			
			for ( Entry<String,Integer> entry : functionsCounter.entrySet() ){
				if ( entry.getValue() != 0 ){
					toIgnore.add(entry.getKey());
				}
			}
			
			if ( functionsToFilterOut == null ){
				functionsToFilterOut = toIgnore;
			} else {
				functionsToFilterOut.addAll(toIgnore);
			}

			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}

	protected void processLine(int lineCount, String line) {
		if( skipNext ){
			skipNext=false;
			return;
		}
		
		if ( GdbTraceUtil.isCurrentMethodOnStack( line ) ){
			if ( originalSoftwareFunctions != null ){
				String functionName = TraceUtils.extractFunctionSignatureFromGenericProgramPointName(currentFunction);
				FunctionMonitoringData functionData = this.originalSoftwareFunctions.get(functionName);

				if ( functionData != null ){
					if ( ! functionData.hasParametersNumber() ){
						try {
							List<Variable> pars = getParameters( line );
							functionData.setParametersNumber( pars.size() );
						} catch (UnexpectedFormaException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					LOGGER.warning("FunctionData not avalable for function "+currentFunction);
				}
			} else {
				//Checked in the caller
				//LOGGER.warning("originalSoftwareFunctions is null");
			}
		}
		
		if ( GdbTraceUtil.isThreadStart( line ) ){
		} else if ( GdbTraceUtil.isBctPoint( line ) ){
			if ( parsingOptions_ParseFunctions ){
				commitMethodCall();
				processBctPoint( line );
			}
//					System.out.println("POINT"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctEnter( line ) ){
			//System.err.println("BCT-ENTER");
			if ( parsingOptions_ParseFunctions ){
				commitMethodCall();
				processBctEnter( line );
			}
//					System.out.println("ENTER"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctExit( line) ){
			if ( parsingOptions_ParseFunctions ){
				commitMethodCall();
				processBctExit( line  );
			}
//					System.out.println("EXIT"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctEnterExit( line) ){
			if ( parsingOptions_ParseFunctions ){
				commitMethodCall();
				processBctEntEx( line  );
			}
		} else if ( GdbTraceUtil.isVariablePrint( line ) ){
			if ( parsingOptions_ParseVariables ){
				processVariablePrint( line );	
			}
//					System.out.println("PRINT"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctLocals( line ) ){
			if ( parsingOptions_ParseVariables ){
				sectionParser=LocalsSectionParser;
			}
//					System.out.println("LocalsStart"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctParentLocals( line ) ){
			if ( parsingOptions_ParseVariables ){
				sectionParser=ParentLocalsSectionParser;
			}
//					System.out.println("ParentLocalsStart"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctStackTrace( line ) ){
			if ( parsingOptions_ParseFunctions ){
				sectionParser=StackSectionParser;
			}
			System.out.println("StackStart"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctArgs( line ) ){
			if ( parsingOptions_ParseVariables ){
				sectionParser=ArgsSectionParser;
			}
//					System.out.println("ArgsStart"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctRegisters( line ) ){
			if ( parsingOptions_ParseVariables ){
				sectionParser=RegistersSectionParser;
			}
//					System.out.println("RegistersStart"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctLocalsEnd( line ) ){
			sectionParser=NullSectionParser;
//					System.out.println("LocalsEnd"+" "+lineCount);
			checkParserStateEnd();
		} else if ( GdbTraceUtil.isBctParentLocalsEnd( line ) ){
			sectionParser=NullSectionParser;
//					System.out.println("ParentLocalsEnd"+" "+lineCount);
			checkParserStateEnd();
		} else if ( GdbTraceUtil.isBctStackTraceEnd( line ) ){
			sectionParser=NullSectionParser;
//					System.out.println("StackTraceEnd"+" "+lineCount);
		} else if ( GdbTraceUtil.isBctArgsEnd( line ) ){
			sectionParser=NullSectionParser;
//					System.out.println("ArgsEnd"+" "+lineCount);
			checkParserStateEnd();
		} else if ( GdbTraceUtil.isBctRegistersEnd( line ) ){
			sectionParser=NullSectionParser;
//					System.out.println("RegistersEnd"+" "+lineCount);
		} else if ( GdbTraceUtil.isSignal(line) ){
			commitMethodCall();
			processSignal(line);
		} else if ( GdbTraceUtil.isBctReturn( line) ){
			processBctAdditionalReturn( line );
		} else if ( GdbTraceUtil.isProcessExitCode( line ) ){
			commitMethodCall();
			processExitCode( line );
		} else if ( GdbTraceUtil.isProcessExitCodeWindows( line ) ){
			commitMethodCall();
			processExitCodeWindows( line );
		} else if ( GdbTraceUtil.isBctFloatReturn( line ) ){
			floatReturn = true;
		} else if ( GdbTraceUtil.isBctDoubleReturn( line ) ){
			doubleReturn = true;
		} else if ( GdbTraceUtil.isBctTestCase( line ) ){
			commitMethodCall();
			processTestCase( line );
		} else if ( GdbTraceUtil.isBctTestFail( line ) ){
			commitMethodCall();
			processTestFail( line );
		} else if ( useOldParser ){
			if ( parsingOptions_ParseVariables ){
				OriginalParser.processLine(line);
			}
		} else {
			try {
				sectionParser.processLine(line);
			} catch (UnexpectedFormaException e) {
				LOGGER.log(Level.SEVERE, "Error processing line "+lineCount, e);
				sectionParser = NullSectionParser;

				this.nextVariableName = null;
				this.structs.clear();
				
			}
		}
	}

	private void processTestFail(String line) {
		for ( GdbThreadTraceListener l : listeners ){
			l.testFail();
		}
	}

	private void processTestCase(String line) {
		int pos = line.indexOf(' ');
		if ( pos < 0 ){
			return;
		}
		String testName = line.substring(pos);
		testName = demangler.demangle(testName.trim());
		for ( GdbThreadTraceListener listener : listeners ){
			listener.testCase( testName );
		}
	}

	protected void processExitCodeWindows(String line) {
		Pattern p = Pattern.compile("\\[Inferior \\d+ \\(process \\d+\\) exited with code (\\d+)\\]");
		try {
			Matcher matcher = p.matcher(line);
			if  ( ! matcher.matches() ){
				System.out.println(" "+line);
				LOGGER.warning("ERROR PROCESSING EXIT CODE in line: " +line);
				return;
			}
			String exitCodeString = matcher.group(1);

			Integer exitCode = Integer.valueOf(exitCodeString);

			newExitCode(exitCode);
		} catch ( NumberFormatException e ) {
			e.printStackTrace();
		} catch ( IllegalStateException e ){
			System.err.println("Error processing line "+line);
			e.printStackTrace();
		}
	}

	private void processExitCode(String line) {
		int start = "Program exited with code ".length();
		String exitCodeString = line.substring(start, line.length() -1 );
		
		Integer exitCode = Integer.valueOf(exitCodeString);
		
		newExitCode(exitCode);
		
	}

	private void newExitCode(Integer exitCode) {
		for ( GdbThreadTraceListener listener : listeners ){
			listener.exitCode( exitCode );
		}
	}

	private void processBctAdditionalReturn(String line) {
		String[] splitted = line.split("\\s");
		lastAdditionalReturnLine = splitted[ 2 ];
	}

	private void reset() {
		// TODO Auto-generated method stub
		
	}

	private void processSignal(String line) {
		int signalNameEnd = line.indexOf(',');
		String signalName = "!SIGNAL!"+line.substring(24,signalNameEnd);
		
		commitMethodCall();
		
		state=TraceState.GenericProgramPoint;
		
		currentCall=new CallData();
		currentCall.functionName = signalName;
		
		sectionParser = SignalSectionParser;
	}

	private void checkParserStateEnd() {
		if ( structs.size() > 0 ){
			if ( noStructStateCheck ){
				structs = new LinkedList<Variable>();
			} else {
				throw new IllegalStateException("Error during parsing: struct cllosing brackets not found!");
			}
		}
	}

	private void processBctPoint(String line) {
		// TODO Auto-generated method stub
		currentCall = new CallData();
		state = TraceState.GenericProgramPoint;
		currentFunction = extractFunctionNameFromFirstBctPointLine(line,true);
		
		LOGGER.fine("Proceessed BCT point "+line);
		//not to call here resetLastAdditionalReturnLine(); we could be in a generic breakpoint for the last method line

		currentCall.functionName = currentFunction;
	}

	private void resetLastAdditionalReturnLine() {
		lastAdditionalReturnLine = null;
	}

	private void processGenericStackInfo(String line) {
		// TODO Auto-generated method stub
		StackTraceElement stackData = getStackInfoDataFromGdbStackLine(line);
		
		this.currentCall.addToStackBottom( stackData );
	}

	private void processEnd(String line, File file) {
		
		if ( callstackCounter != 0 ){
			LOGGER.warning("Trace not simmetric "+file.getAbsolutePath()+" "+callstackCounter);
			
			for ( Integer threadId : stacks.keySet() ){

				LinkedList<CallData> stack = new LinkedList<GdbThreadTraceParser.CallData>();
				stack.addAll ( stacks.get(threadId) );
				
				

				while ( stack.size() > 0 ){
					CallData currentCall = stack.pop();
					LOGGER.warning(currentCall.functionName);
				}
			}
			
			callstackCounter=0;
		}
		
		if ( addFakeCallsAtProcessEnd ){
			for ( Integer threadId : stacks.keySet() ){

				LinkedList<CallData> stack = stacks.get(threadId);

				while ( stack.size() > 0 ){
					CallData currentCall = stack.pop();
					for ( GdbThreadTraceListener listener : listeners ){
						LOGGER.fine("!!!EXIT (fake) " + currentCall.functionName );
						listener.functionExit( currentCall.functionName, null, null, null, null, null, threadId, currentCall.fileName, currentCall.lineNo, currentCall.stack, currentCall.globalVariableNames );
					}
				}
			}
		}

		for ( GdbThreadTraceListener listener : listeners ){
			listener.traceEnd(file.getAbsolutePath());
		}

	}

	protected void processLocalVariableData(String line) throws UnexpectedFormaException {
		try {
			currentCall.addLocalVariable( createVariableFromVariableInfoLine(line) );
		} catch (IncompleteSequenceException e) {
			//the last variable in returnValues was an incomplete sequence
			//TODO: we could do something, now we just ignore this info
		} catch (StructEndException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (MembersOfStartException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (NoLocalsException e) {
			LOGGER.log(Level.FINE,"Valid parser behavior",e);
		} catch (NoArgumentsException e) {
			LOGGER.log(Level.FINE,"Valid parser behavior",e);
		} catch (StructDepthLimitException e) {
			LOGGER.log(Level.FINE,"Valid parser behavior",e);
		} catch (ParserException e) {
			LOGGER.severe("Error processing line: "+line);
			currentCall.invalidDataValues();
		}
	}


	private Variable createVariableFromVariableInfoLine( String line ) throws IncompleteSequenceException, StructEndException, MembersOfStartException, NoLocalsException, NoArgumentsException, StructDepthLimitException, UnexpectedFormaException, ParserException {
		Variable var;
		if ( PINtrace ){
			var = createVariableFromVariableInfoLinePIN(line);
		} else {
			var = createVariableFromVariableInfoLineGdb(line);
		}
		
		if ( changeHexToInt ){
			if ( var.getValue().startsWith("0x") ){
				var.setValue( String.valueOf( HexUtil.hexToInt(var.getValue() )) );
			}
		}
		
		return var;
	}
	
	
	private Variable createVariableFromVariableInfoLinePIN( String line ) throws IncompleteSequenceException, StructEndException, MembersOfStartException, NoLocalsException, NoArgumentsException, StructDepthLimitException, UnexpectedFormaException, ParserException {
		Variable var = new Variable( nextVariableName, parseValuePIN(line), 1 );
		nextVariableName=null;
		return var;
	}
	
	
	private String parseValuePIN(String value) {
		if ( value.isEmpty() ){
			return "!NULL";
		}
		
		if ( value.charAt(0) == '\'' ){
			if ( value.length() == 1 ){
				skipNext=true;
				return String.valueOf((int)'\n');
			} 
			
			return String.valueOf((int)value.charAt(1));
			
		}
		
		if ( value.startsWith("0x") ){
			if ( !changeHexToInt ){
				if ( value.length() > 10 ){
					value = "0x"+value.substring(value.length()-10);//get the last 8, Daikon has problems processing 64 bit pointers
					return value;
				}
			}
		}
		return value;
	}

	private Variable createVariableFromVariableInfoLineGdb( String line ) throws IncompleteSequenceException, StructEndException, MembersOfStartException, NoLocalsException, NoArgumentsException, StructDepthLimitException, UnexpectedFormaException, ParserException {
//		System.out.println(line);
		
		int structsToClose = 0;
		if ( withinStruct() ){
			
//			if ( line.trim().charAt(0) == '}' ){
//				endStruct();
//				throw new StructEndException();
//			}
			if ( line.endsWith(",") ){
				line = line.substring(0, line.length()-1);
			}
			
			if ( line.indexOf('"') < 0 ){
				int openBrackets = StringUtils.occurrenciesOf( line , '{' );
				int closedBrackets =  StringUtils.occurrenciesOf( line , '}' );
				
				if ( closedBrackets != 0 ) {
					if ( closedBrackets < openBrackets ){
						throw new UnexpectedFormaException("Unable to parse line: "+line);
					}
					
					structsToClose = closedBrackets - openBrackets;
					if ( ! line.contains("=") ){
						closeStructs(structsToClose,true);
					}
					
					if ( structsToClose !=  0 ){
						int closingBracket = line.indexOf('}');
						int equal = line.indexOf('"');

						if ( equal < closingBracket ){
							line = line.substring(0, closingBracket);
						}
					}
				}
			}
			
			
			
		}
		try {
		ArrayList<String> dataParent = new ArrayList<String>();
		
		
		if ( printedVariable() ){
			//this block manages values generated with the command 'output'
			dataParent.add(nextVariableName);
			dataParent.add(line);
		} else {
			if ( line.matches(".* =   \\{.*") ){
				int assignmentPos = line.indexOf(" =   {");
				String variableName = line.substring(0,assignmentPos);
				String variableValue = line.substring(assignmentPos+4);
				dataParent.add(variableName);
				dataParent.add(variableValue);
			} else if ( line.matches(".* = \\s+\\{.*") ){
				int assignmentPos = line.indexOf(" =");
				String variableName = line.substring(0,assignmentPos).trim();
				int valueStart = line.indexOf('{');
				String variableValue = line.substring(valueStart);
				dataParent.add(variableName);
				dataParent.add(variableValue);
			} else {
				QuotedStringTokenizer tokenizerParent = new QuotedStringTokenizer(line,"=",false,true);

				while ( tokenizerParent.hasMoreElements() ){
					dataParent.add( tokenizerParent.nextToken() );
				}
			}
		}

		if ( dataParent.size() == 1 ){
			String dataValue = dataParent.get(0).trim();
			if ( dataValue.startsWith("<incomplete sequence") ){
				throw new IncompleteSequenceException();
			}
			if ( dataValue.startsWith("No arguments.") ){
				throw new NoArgumentsException(line);
			}
			if ( dataValue.startsWith("No locals") ){
				throw new NoLocalsException(line);
			}
			if ( dataValue.startsWith("members of") ){
				throw new MembersOfStartException(line);
			}
		}

		if ( dataParent.size() != 2 ){
			if ( dataParent.size() > 2 && dataParent.get(1).trim().startsWith("{") ){
				return new NotNullVariable( dataParent.get(0), 1 );
			}
			throw new UnexpectedFormaException("Unexpected format, expecting NAME=<VALUE>  or NAME=0x53625362 \"StringValue\"  but was " +line+" ("+dataParent+")");
		}

		String name = dataParent.get(0).trim();
		
//		String elem = dataParent.get(1).trim();
		
		

		String dataValue = dataParent.get(1).trim();
		
		boolean array = false;
		
		
		
		
		if ( nextVariableName != null ){
			name = nextVariableName;
			nextVariableName = null;
		}
		//Array with one element only
		if ( dataValue.matches("\\{\\[0\\] =.*}") ){
			if ( withinStruct() ){
				name = getCurrentStructName()+"."+name;
			}
			newArray(name);
			name = "["+getArrayPosition(dataValue)+"]";
			array=true;
			int pos = dataValue.indexOf("=");
			if( pos > 0 ){
				dataValue = dataValue.substring(pos+1,dataValue.length()-1).trim();
				name = getCurrentStructName()+ name;
				endStruct();
				return new Variable( name, parseValue(dataValue), 1 );
			}
		}
		
		if ( dataValue.matches("\\{\\[[0-9]+\\] .*") ){
			if ( withinStruct() ){
				name = getCurrentStructName()+"."+name;
			}
			newArray(name);
			name = "["+getArrayPosition(dataValue)+"]";
			array=true;
			int pos = dataValue.indexOf("=");
			if( pos > 0 ){
				dataValue = dataValue.substring(pos);
			}
		}
		
		if ( withinStruct() ){
			if ( name.trim().matches("\\[[0-9]+\\]") ){
				Integer pos = getArrayPosition(name);

				name = getCurrentStructName()+"["+pos+"]";
			} else {
				name = getCurrentStructName()+"."+name;
			}
		}
		
		if ( dataValue.endsWith("{") ){
			return newStruct( name );
		}
		
		if ( "<value optimized out>".equals(dataValue ) ) {
			return new NotNullVariable( name, 1, true );
		} 

		if ( !array && dataValue.startsWith("{") ){
			return new NotNullVariable( name, 1 );
		}

		QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(dataValue," \t",false,true);
		ArrayList<String> data = new ArrayList<String>();
		while ( tokenizer.hasMoreElements() ){
			data.add( tokenizer.nextToken() );
		}


		if ( data.size() > 2 ){
			
			return new NotNullVariable( name, 1 );




			//			throw new IllegalArgumentException("Unexpected format while processing this value "+dataValue+"\n" +
			//					"size: "+data.size()+" 2ndvalue "+data.get(2) +"\n"+
			//					" expecting NAME=<VALUE>  or NAME=0x53625362 \"StringValue\"  but was " +line+" ("+dataParent+")");
		}

		String value;
		String ptr = null;
		if ( data.size() == 1 ){
			value = data.get(0);
		} else {
			ptr = data.get(0);
			value = data.get(1);
			
			if ( value.startsWith("'\\") ){
				value = data.get(0);
			}
		}
		
		value=value.trim();
		
//		if ( ( ! value.startsWith("\"") ) && value.contains(" ") ){
//			StringTokenizer t = new StringTokenizer(" ");
//			value = t.nextToken();
//		}
		
		
		
		if ( value.endsWith(",") ){
			value = value.substring(0,value.length()-1);
		}
		
		Variable var = new Variable( name, parseValue(value), 1 );
		if ( processPointers && ptr != null ){
			ptr = ptr.trim();
			if ( ptr.startsWith("0x") ){


				if ( ptr.length() > 10 ){
					ptr = "0x"+ptr.substring(10);//get the last 8 bytes, Daikon has problems processing 64 bit pointers
				}

				var.setPointerAddress( ptr );
			}
		}

		return var;
	} finally {
		if ( structsToClose > 0 ){
			closeStructs(structsToClose,false);
		}
	}
	}

	public void closeStructs(int structsToClose, boolean throwException) throws StructEndException, ParserException {
		while ( structsToClose > 0 ){
			endStruct();
			structsToClose--;
		}
		if ( throwException ){
			throw new StructEndException();
		}
	}

	private Integer getArrayPosition(String elem) {
		
		int start = elem.indexOf("[");
		if ( start == -1 ){
			LOGGER.warning("NO START: "+elem);
		}
		int end = elem.indexOf("]");
		if ( start == -1 ){
			LOGGER.warning("NO END: "+elem);
		}
		Integer pos = Integer.valueOf(elem.substring(start+1, end));
		return pos;
	}

	private String getCurrentStructName() {
		return structs.peek().getName();
	}

	private void endStruct() throws ParserException{
		LOGGER.fine("Closing struct");
		if ( structs.size() == 0 ){
			throw new ParserException("Empty structs");
		}
		structs.pop();
	}

	private Variable newArray(String name) throws StructDepthLimitException {
		return newStruct(name);
	}
	
	private Variable newStruct(String name) throws StructDepthLimitException {
		LOGGER.fine("Opening struct");
		Variable var = new NotNullVariable( name,  1 );
		structs.push( var );
		
		
		if ( structs.size() > maxStructDepth ){
			throw new StructDepthLimitException();
		}
		
		
		return var;
	}

	private boolean withinStruct() {
		return structs.size() > 0 ;
	}

	private Variable createVariableFromRegisterInfoLine( String line ){
		
		QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(line," \t",false);
		ArrayList<String> data = new ArrayList<String>();
		while ( tokenizer.hasMoreElements() ){
			data.add( tokenizer.nextToken() );
		}

		String name = data.get(0);


		String value;
		if ( data.size() == 2 ){
			value = data.get(1);
		} else {
			if ( floatReturn ){
				value = HexUtil.hexToFloatString(data.get(1));
			} else {
				value = data.get(2);
			}
		}
		
		value=value.trim();
		return new Variable( name, parseValue(value), 1 );
	}

	/**
	 * @deprecated Use {@link HexUtil#hexToFloatString(String)} instead
	 */
	private static String hexToFloat( String value ){
		return HexUtil.hexToFloatString(value);
	}
	
	/**
	 * @deprecated Use {@link HexUtil#hexToDoubleString(String)} instead
	 */
	private static String hexToDouble( String value ){
		return HexUtil.hexToDoubleString(value);
	}
	
	private void processEaxRegisterData(String line) {
		currentCall.returnValues.add(
				createVariableFromRegisterInfoLine(line)		
				);
		floatReturn = false;
	}

	private void processMethodOnStackAndExtractParameters(String line) {
		processMethodOnStack(line);
		try {
			currentCall.parameters = getParameters( line );
		} catch (UnexpectedFormaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			currentCall.parameters = new ArrayList<Variable>();
		}
	}

	private void processMethodOnStack(String line) {
		LOGGER.fine("PROCESS ON STACK "+line+" "+currentFunction);
		String[] elements = line.split("\\s");

		processGenericStackInfo(line);


		StackTraceElement stackInfo = getStackInfoDataFromGdbStackLine(line);

		currentCall.fileName = stackInfo.getFileName();
		currentCall.lineNo = stackInfo.getLineNumber();
	}


	private  StackTraceElement getStackInfoDataFromGdbStackLine( String line ){
		String fileName = "";
		int lineNo = -1;

		//extract file and line
		int pos = line.lastIndexOf(" at ");
		if ( pos != -1 ){

			String locationLine = line.substring(pos+4);
			int separatorPos = locationLine.lastIndexOf(":");

			if ( separatorPos != -1 ){
				fileName = locationLine.substring(0, separatorPos);
				lineNo = Integer.valueOf( locationLine.substring(separatorPos+1).trim() );
				
				if ( lastAdditionalReturnLine != null && state == GdbThreadTraceParser.TraceState.BeforeExit ){
					int dotsPos = lastAdditionalReturnLine.lastIndexOf(':')+1;
					if ( dotsPos < 0 ){
						dotsPos = 0;
					}
					lineNo = Integer.valueOf(lastAdditionalReturnLine.substring(dotsPos));
					resetLastAdditionalReturnLine();
				}
			}

		}			

		String functionName = getFunctionNameFromGdbStackInfo(line);

		StackTraceElement result = new StackTraceElement(fileName, functionName, fileName, lineNo);


		return result;
	}

	private void commitMethodCall(){
		
		if ( currentCall == null ){
			LOGGER.fine("NO-COMMIT: currentCall is null");
			return;
		}

		if ( currentCall.functionName == null ){
			throw new IllegalArgumentException("Null function name");
		}
		LOGGER.fine("COMMIT: "+currentCall.functionName);
		
		if ( currentCall.functionName.equals("exit@plt") ){
			LOGGER.fine("DISCARDING-FUNCTION-CALL exit@plt");
			return;
		}
		
		
		if ( currentCall.toIgnore || ( functionsToFilterOut != null && functionsToFilterOut.contains(currentCall.functionName) ) ){
			return;
		}

		callCounter++;
		
		if ( positionsOfFunctionsToFilterOut != null && 
				positionsOfFunctionsToFilterOut.contains(callCounter) ){
			return;
		}

		if ( ! canInclude( currentCall ) ){
			return;
		}
		
		if ( hideFunction( currentCall) ){
			return;
		}
		
		
		if ( state == TraceState.BeforeEntEx ){
			state = TraceState.BeforeEnter;
			proceedWithMethodCallCommit();
			state = TraceState.BeforeExit;
		}
		
		proceedWithMethodCallCommit();
		
		currentCall = null;
	}

	private Set<String> programPointsToInclude;

	private boolean avoidFixingLineNumbers;
	
	public void setProgramPointsToInclude(Set<String> programPointsToInclude) {
		this.programPointsToInclude = programPointsToInclude;
	}

	private boolean canInclude(CallData currentCall) {
		if ( programPointsToInclude == null ){
			return true;
		}
		
		String ppName = currentCall.functionName;
		LOGGER.info("Checking for inclusion of program point "+ppName);
		
		
		
		for ( String matchingExpr : programPointsToInclude ){
			if ( ppName.matches(matchingExpr) ){
				return true;
			}
		}
		
		
		return false;
		
	}

	private void proceedWithMethodCallCommit() {

		if ( currentCall.hasInvalidDataValues() ){
			LOGGER.severe("currentcall has invalid data values, clearing it.");
			currentCall.localVariables.clear();
			currentCall.parameters.clear();
			currentCall.parentLocalVariables.clear();
			currentCall.parentLocalVariables.clear();
			currentCall.returnValues.clear();
		}

		try{
			switch ( state ){
			case BeforeEnter:
				callstackCounter++;
				for ( GdbThreadTraceListener listener : listeners ){

					LOGGER.fine("!!!ENTER for "+currentCall.functionName);

					updateNamesIfNecessary();

					enteredFunctions.push( currentCall.functionName );

					String functionName = currentCall.functionName;
					if (useDemangledNames ){
						functionName = demangler.demangle(functionName);
					}
					
					if ( this.PINtrace && ( this.changeHexToInt == false ) ){
						throw new IllegalStateException("hexToInt expected for PIN experiments ");
					}
					
					listener.functionEnter( functionName,currentCall.parameters, currentCall.localVariables, currentCall.parentLocalVariables, currentCall.parentArguments ,threadId, currentCall.fileName, currentCall.lineNo, currentCall.stack, currentCall.globalVariableNames );

				}
				break;
			case BeforeExit:
				callstackCounter--;
				updateNamesIfNecessary();
				for ( GdbThreadTraceListener listener : listeners ){

					if ( shouldAddFakeEnter() ){
						System.out.println("!!!ENTER (fake) for "+currentCall.functionName);
						enteredFunctions.push( currentCall.functionName );

						String functionName = currentCall.functionName;
						if (useDemangledNames ){
							functionName = demangler.demangle(functionName);
						}

						listener.functionEnter( functionName, currentCall.parameters, currentCall.localVariables, currentCall.parentLocalVariables, currentCall.parentArguments, threadId, currentCall.fileName, currentCall.lineNo, currentCall.stack, currentCall.globalVariableNames );
					}


					LOGGER.fine("!!!EXIT for "+currentCall.functionName);

					if ( enteredFunctions.isEmpty() ){
						System.err.println("!!!EXIT without ENTER for :"+currentCall.functionName);
					} else {
						enteredFunctions.pop();
					}

					String functionName = currentCall.functionName;
					if (useDemangledNames ){
						functionName = demangler.demangle(functionName);
					}
					
					if ( isPINtrace() ){
						ArrayList<Variable> newLocals = new ArrayList<>();
						for ( Variable v : currentCall.localVariables ){
							if ( v.getName().startsWith("BCT_return") ){
								newLocals.add(v);
							}
						}
						currentCall.localVariables = newLocals;
					}
					
					listener.functionExit( functionName, currentCall.parameters, currentCall.localVariables, currentCall.parentLocalVariables, currentCall.parentArguments, currentCall.returnValues, threadId, currentCall.fileName, currentCall.lineNo, currentCall.stack, currentCall.globalVariableNames );

				}
				break;
			case GenericProgramPoint:

				String functionName = currentCall.functionName;

				//BEGIN: Workaround for GDB recording
				//Gdb stops at the heading of the target function, and skips the first line
				{
					int pos = functionName.indexOf(':');
					if ( pos > 0  ){
						String currentName = functionName.substring(0,pos);
						if ( ! currentName.equals( lastFunctionLine ) ){
							
							if ( currentCall.stack != null ) {
								StackTraceElement top = currentCall.stack.get(0);
								Integer recordedValue = Integer.valueOf(functionName.substring(pos+1));
								int realLineNumber = top.getLineNumber();
								if ( recordedValue != realLineNumber ){
									if ( ! avoidFixingLineNumbers ){
										int valueToUse = recordedValue < realLineNumber ? recordedValue : realLineNumber;
										LOGGER.fine("REAL LINE NUMBER: "+realLineNumber+" RECORDED: "+recordedValue+" RENAMED TO "+valueToUse);
										functionName = currentName+":"+valueToUse;
									} else {
										LOGGER.fine("REAL LINE NUMBER: "+realLineNumber+" RECORDED: "+recordedValue+" FORCED NOT TO RENAME ");
									}
								} else {
									LOGGER.fine("REAL LINE NUMBER: "+realLineNumber+" RECORDED: "+recordedValue);
								}
							}

						}
						lastFunctionLine = currentName;
					} else {
						System.out.println("NOT POINT!!!! "+functionName);
					}
				}
				currentCall.functionName = functionName;
				//END Workaround

				updateNamesIfNecessary();
				functionName = currentCall.functionName;

				if (useDemangledNames ){
					int pos = functionName.indexOf(':');
					String post = "";
					if ( pos > 0  ){
						post = functionName.substring(pos);
						functionName =  functionName.substring(0,pos);
					}
					functionName = demangler.demangle(functionName);
					if ( pos > 0 ){
						functionName+=post;
					}
				}



				for ( GdbThreadTraceListener listener : listeners ){

					listener.genericProgramPoint( functionName, currentCall.parameters,
							currentCall.localVariables, currentCall.parentLocalVariables, currentCall.parentArguments, 
							threadId, currentCall.fileName, currentCall.lineNo, currentCall.stack, currentCall.globalVariableNames );
				}
				break;
			}

		} catch ( SourceMapperException e ){
			LOGGER.log(Level.SEVERE, "Mapper exception", e);
			LOGGER.warning("SKIPPING RECORDING OF PROGRAM POINT "+currentCall.functionName);
		}
	}
	
	

	private boolean hideFunction(CallData call) {
		if ( hideAddedDeletedFunctions ){
			if ( ( addedFunctionsComponent != null && addedFunctionsComponent.acceptBytecodeMethodSignature(call.functionName) )
					|| ( addedFunctionsComponent != null && deletedFunctionsComponent.acceptBytecodeMethodSignature(call.functionName) ) ){
				return true;
			}
		}
		
		return false;
	}

	private void updateNamesIfNecessary() throws SourceMapperException {
		if ( sourceMapper == null ){
			return;
		}
		
		if ( state == TraceState.GenericProgramPoint ){
//			currentCall.functionName;
			
			int pos = currentCall.functionName.lastIndexOf(':');
			
			if ( pos < 0 ){
				return;
			}
			
			String functionName = currentCall.functionName.substring(0, pos);
			int lineNo = Integer.valueOf( currentCall.functionName.substring(pos+1) );
			
			if  ( originalSoftwareFunctions == null ){
				LOGGER.warning("OriginalSoftwareFunctions null ");
				return;
			}
			
			FunctionMonitoringData function = originalSoftwareFunctions.get(functionName);
			if ( function == null ){
				LOGGER.warning("Cannot find "+functionName+" in original software.");
				LOGGER.warning("List of original software functions: "+originalSoftwareFunctions);
				return;
			}
			String fileLocation = function.getSourceFileLocation();
			
			int correspondingLine;
//			try {
				correspondingLine = sourceMapper.getCorrespondingLineInModifiedProject(fileLocation, lineNo);
//			} catch (SourceMapperException e1) {
//				
//				e1.printStackTrace();
//				return;
//			}
			
//			try {
				functionName = sourceMapper.getCorrespondingFunction( fileLocation, functionName, correspondingLine );
//			} catch (SourceMapperException e) {
//				e.printStackTrace();
//				assert false;// should never reach this point
//			}
			
			if ( correspondingLine < 0 ){
				return;
			}
			
			String newName = functionName +":" + correspondingLine;
			LOGGER.fine("RENAMED "+currentCall.functionName + " "+newName);
			currentCall.functionName = newName;
		}
	}

	private boolean shouldAddFakeEnter() {
		if ( currentCall == null ){
			return false;
		}
		
		if ( addFakeCallsAtProcessEnd ){
			if ( enteredFunctions.isEmpty() ){
				return true;
			}

			String functionName = enteredFunctions.peek();

			if ( ! functionName.equals(currentCall.functionName ) ) {
				return true;
			}
		}

		return false;
	}

	private static String getFunctionNameFromGdbStackInfo( String line ){
		int begin = line.indexOf('(');
		if ( begin < 0 ){
			return "";
		}
		String functionName = line.substring(line.indexOf(' ')+1,begin);
		String functionNameShort = functionName.substring(functionName.lastIndexOf(' ')+1);

		return functionNameShort;
	}


	protected List<Variable> getParameters(String line) throws UnexpectedFormaException {

		int begin = line.indexOf('(');
		int end = line.lastIndexOf(')');

		List<Variable> vars = new ArrayList<Variable>();

		//		int idx = currentFunction.indexOf('(');
		//		String currentFunctionName;
		//		if ( idx < 0 ){
		//			currentFunctionName = currentFunction;
		//		} else {
		//			currentFunctionName = currentFunction.substring(0, idx);
		//		}
		//		
		//			
		//		
		//		
		//		
		//
		//	
		//		
		//		String functionName = line.substring(3,begin).trim();
		//		String functionNameShort = functionName.substring(functionName.lastIndexOf(' ')+1);
		//		if ( ! ( line.contains(currentFunctionName ) 
		//				||  currentFunctionName.contains(functionName)
		//				|| currentFunctionName.contains(functionNameShort) ) ){
		//			return vars;
		//		}

		String parametersSub = line.substring(begin+1, end);
		QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(parametersSub,",",false,true);
		while ( tokenizer.hasMoreElements() ){
			String par = tokenizer.nextToken();
			if ( par.isEmpty() ){
				continue;
			}
			//			System.out.println(par);
			//			int eq = par.indexOf('=');
			//			String name = par.substring(0,eq);
			//			String value = par.substring(eq+1);
			//			Variable v = new Variable(name, parseValue( value ) , 1);
			//			
			Variable v;
			try {
				v = createVariableFromVariableInfoLine(par);
				vars.add(v);
			} catch (IncompleteSequenceException e) {
				//the last variable in vars is an incomplete sequence
				//TODO: we could do something for thata, now we just ignore this info
			} catch (StructEndException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MembersOfStartException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoLocalsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoArgumentsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StructDepthLimitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserException e) {
				LOGGER.severe("Error processing line: "+line);
				LOGGER.severe("Deleting all variable values");
				vars.clear();
				break;
			}

		}


		return vars;
	}

	private String parseValue(String value) {
		
//		value=value.trim();
		if ( "-nan(0x7fffff)".equals(value) ){
			return "NaN";
		}
		
		if ( "...".equals(value) ){
			return "!NULL";
		}
		
		if ( "0x0".equals(value) ){
			return "null";
		}

		//		if ( value.contains(" \"") ){
		//			int stringStart = value.indexOf('"');
		//			int stringEnd = value.indexOf('"', stringStart+1);
		//			return value.substring(stringStart,stringEnd+1);
		//		}
		
		if ( value.charAt(0) == '\'' ){
			return String.valueOf((int)value.charAt(1));
		}
		
		if ( value.startsWith("0x") ){
			if ( value.length() > 10 ){
				value = "0x"+value.substring(10);//get the last 8, Daikon has problems processing 64 bit pointers
				return value;
			}
		}
		
		if ( value.charAt(0) == '"' ){
			if ( value.endsWith("...") ){
				value = value.substring(0,value.length()-3);
			}
			
			return value;
		}

		if ( value.charAt(0) == '@' ){
			return "!NULL";
		}
		
		
		if ( isEnumValue(value) ){
//			System.out.println("IS-ENUM: "+value);
			value = processEnumValue( value );
		}
		
		if ( value.endsWith(")") ){
			char first = value.charAt(0);
			if ( first >= '0' && first <= '9' ){
				value = value.substring(0, value.length()-1 );
			}
		}
		
		return value;
	}

	

	
	private String processEnumValue(String value) {
		if ( value == null ){
			return "0";
		}
		return ""+value.hashCode();
//		if ( enumValuesDetector == null ){
//			return "!NULL";
//		}
//		String cval = enumValuesDetector.getConstantValue( value );
//		if ( cval == null ){
//			return "!NULL";
//		}
//		return cval;
	}

	private boolean isEnumValue(String value) {
		char firstChar = value.charAt(0);
		
		if ( firstChar == '@' ){
			return false;
		}
		
		if ( firstChar == '"' ){
			return false;
		}
		
		if ( firstChar == '-' ){
			return false;
		}
		
		if ( firstChar == '\'' ){
			return false;
		}
		
		
		if ( firstChar >= '0' && firstChar <= '9' ){
			return false;
		}
		
		if ( value.startsWith("0x") ){
			return false;
		}
		
		
		return true;
	}

	HashMap<Integer, LinkedList<CallData> > stacks = new HashMap<Integer, LinkedList<CallData> >();
	private TreeSet<Integer> positionsOfFunctionsToFilterOut;
	private File monitoredExecutable;
	private boolean parsingOptions_ParseVariables;
	private boolean parsingOptions_ParseFunctions;
	private SourceLinesMapper sourceMapper;
	private Map<String, FunctionMonitoringData> originalSoftwareFunctions;
	private boolean hideAddedDeletedFunctions;
	private Component addedFunctionsComponent;
	private Component deletedFunctionsComponent;

	private boolean processPointers;

	private boolean filterNotTerminatingFunctions;

	public boolean isParsingOptions_ParseVariables() {
		return parsingOptions_ParseVariables;
	}

	public void setParsingOptions_ParseVariables(
			boolean parsingOptions_ParseVariables) {
		this.parsingOptions_ParseVariables = parsingOptions_ParseVariables;
	}

	public boolean isParsingOptions_ParseFunctions() {
		return parsingOptions_ParseFunctions;
	}

	public void setParsingOptions_ParseFunctions(
			boolean parsingOptions_ParseFunctions) {
		this.parsingOptions_ParseFunctions = parsingOptions_ParseFunctions;
	}

	private LinkedList<CallData> getThreadStack( int threadId ){
		LinkedList<CallData> stack = stacks.get(threadId);

		if ( stack == null ){
			stack = new LinkedList<CallData>();
			stacks.put(threadId, stack);
		}

		return stack;

	}
	
	protected CallData getCurrentCall(){
		return currentCall;
	}

	private void processBctEnter(String line) {

		currentCall = new CallData();
		state = TraceState.BeforeEnter;
		currentFunction = extractFunctionNameFromFirstBctPointLine(line, true);

		if ( functionsToFilterOut != null && functionsToFilterOut.contains(currentFunction) ){
			currentCall.toIgnore = true;
		}
		
		if ( ! currentCall.toIgnore ){
			getThreadStack(threadId).push(currentCall);
		}
		
		resetLastAdditionalReturnLine();//just for safety
		
		currentCall.functionName = currentFunction;
	}

	private void processBctEntEx(String line) {

		currentCall = new CallData();
		state = TraceState.BeforeEntEx;
		currentFunction = extractFunctionNameFromFirstBctPointLine(line,true);

		if ( functionsToFilterOut != null && functionsToFilterOut.contains(currentFunction) ){
			currentCall.toIgnore = true;
		}
		
//		getThreadStack(threadId).push(currentCall);
		
		resetLastAdditionalReturnLine();//just for safety
		
		currentCall.functionName = currentFunction;
	}

	public String extractFunctionNameFromFirstBctPointLine(String line, boolean enter){
		String fn;
		
		int delta;
		if ( enter ){
			delta = 14;
		} else {
			delta = 13;
		}
		
		if ( PINtrace ){
			fn = line.substring(delta-1);
		} else {
			fn = line.substring(delta);
		}
		
		return fn;
	}

	private void processBctExit(String line) {
		currentCall = new CallData();
		state = TraceState.BeforeExit;
		currentFunction = extractFunctionNameFromFirstBctPointLine(line,false);

		if ( functionsToFilterOut != null && functionsToFilterOut.contains(currentFunction) ){
			currentCall.toIgnore = true;
		}

		if ( ! currentCall.toIgnore ){
			LinkedList<CallData> calls = getThreadStack(threadId);
			int pos;

			for ( pos = calls.size() - 1; pos >= 0; pos -- ){
				if ( currentFunction.equals( calls.get(pos).functionName ) ) {
					break;
				} else {
					//System.out.println("!!!processBctExit issue with : "+currentFunction+" VS "+calls.get(pos).functionName );
				}
			}

			//matching item found
			if ( pos >= 0 ){
				int delta = ( calls.size() - pos );
				if ( delta > 1 ){
					System.out.println("!!!processBctExit issue with exit for : "+currentFunction+" "+delta);
					for ( int x = pos; x < calls.size(); x++ ){
						System.out.println("!!!!!"+calls.get(x).functionName);
					}
				}
				calls.remove(pos);
			}
		}
		
		currentCall.functionName = currentFunction;
		//here we should not call resetLastAdditionalReturnLine()
	}

	public void setFuntionsToFilterOut(Set<String> functionsToFilterOut) {
		this.functionsToFilterOut = functionsToFilterOut;
	}

	public void setPositionsOfFunctionsToFilterOut(
			List<Integer> functionsToFilterOutForThsTrace) {
		positionsOfFunctionsToFilterOut = new TreeSet<Integer>();
		positionsOfFunctionsToFilterOut.addAll(functionsToFilterOutForThsTrace);
	}

	public void setMonitoredExecutable(File monitoredExecutable) {
		this.monitoredExecutable = monitoredExecutable;
		enumValuesDetector = new EnumValuesDetector(monitoredExecutable);
	}

	public void optimizeParsing(List<GdbThreadTraceListener> listeners) {
		resetParsingOptions(false);
		for ( GdbThreadTraceListener listener : listeners ){
			optimizeParsing( listener );
		}
	}

	private void optimizeParsing(GdbThreadTraceListener listener) {
		
		
		UsefulTraceSections[] usefulSections = listener.getUsefulTraceSections();
		
		if ( usefulSections == null ){
			resetParsingOptions( true );
			return;
		} else {
			
		}
		
		for ( UsefulTraceSections traceSection : usefulSections ) {
			switch ( traceSection ){
			case FUNCTIONS:
				parsingOptions_ParseFunctions = true;
				break;
			case ALL:
				resetParsingOptions(true);
			}
		}
	}

	private void resetParsingOptions(boolean b) {
		parsingOptions_ParseVariables = b;
		parsingOptions_ParseFunctions = b;
	}
	
	public static void main(String args[]) throws IOException{
		List<GdbThreadTraceListener> list = new ArrayList<GdbThreadTraceListener>();
		
		EnvironmentalSetter.setBctHome("/home/BCT/workspace_BCT_Testing/BDCI/Esempio13/AnalysisV0/BCT/");
		
		BctGdbThreadTraceListener listener = new BctGdbThreadTraceListener(false);
		
		list.add(listener);
		
		
		GdbThreadTraceParser t = new GdbThreadTraceParser(list );
		
		
		
		File traceFile = new File( args[0] );
		FileInteractionTrace trace = new FileInteractionTrace(traceFile.getName(), "0", "1", traceFile, null);
		t.processThreadTrace(trace);
	}

	public void setRemapNames(SourceLinesMapper sourceMapper, Map<String, FunctionMonitoringData> originalSoftwareFunctions) {
		this.sourceMapper = sourceMapper;
		this.originalSoftwareFunctions = originalSoftwareFunctions;
	}

	public void setHideAddedDeletedFunction(boolean hideAddedDeletedFunctions) {
		this.hideAddedDeletedFunctions = hideAddedDeletedFunctions;
	}

	public void setComponents(List<Component> components) {
		if ( components == null ){
			return;
		}
		for ( Component c : components ){
			if ( c.getName().equals(ModifiedFunctionsDetectorObjDumpListener.ADDED_FUNCTIONS_COMPONENT_NAME) ){
				addedFunctionsComponent = c;
			} else if ( c.getName().equals(ModifiedFunctionsDetectorObjDumpListener.DELETED_FUNCTIONS_COMPONENT_NAME) ){
				deletedFunctionsComponent = c;
			}
		}
	}

	public void setProcessPointers(boolean processPointers) {
		this.processPointers = processPointers;
	}

	public void setFilterNotTerminatingFunctions(boolean filterNotTerminatingFunctions) {
		this.filterNotTerminatingFunctions = filterNotTerminatingFunctions;
	}

	public int getProcessedCalls() {
		return callCounter+1;
	}
}
