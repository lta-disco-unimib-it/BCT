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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;







import tools.violationsAnalyzer.MultiHashMap;
import tools.violationsAnalyzer.ViolationsUtil;
import util.FileUtil;
import util.componentsDeclaration.CppDemangledSignatureParser;

import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.FunctionMonitoringDataSerializer;
import cpp.gdb.LineData;

public class CBMCAssertionsInjector {
	private static final String BCT_HIDDEN_MAIN_NAME = "bct_hidden_main";

	///PROPERTIES-DESCRIPTION: Options that control how VART insert assertions in C code
	
	///comment out the lines of the functions that were not monitored during test cases execution
	public static final String COMMENT_OUT_LINES_NOT_MONITORED = "bct.cbmc.commentOutLinesNotMonitored";
	
	///if set to "all" all the functions that were not monitored are commented out. Otherwise also the callers of the monitored functions are not cmmented out.
	public static final String COMMENT_MODE = "bct.cbmc.commentMode";
	
	///delta relative to the first line of the function to comment out to be used to start commenting lines (negative numbers are accepted too)
	public static final String COMMENT_OUT_DELTA = "bct.cbmc.commentOutDelta";
	
	///prefix to be used for commenting lines, default is ";//BCT-STUBBED-OUT "
	public static final String COMMENT_OUT_BEGIN = "bct.cbmc.commentOutPrefix";

	///prefix to be used for commenting import of standard cpp libs in c++, default is true
	private static final String BCT_CBMC_REDEFINE_CPPLIBS = "bct.cbmc.redefineCppLibs";

	private static Logger LOGGER = Logger.getLogger(CBMCAssertionsInjector.class.getCanonicalName());

	private File cbmcTempFolder;

	private Set<String> testFunctions = new HashSet<String>();

	private List<String> additionalDefs = new ArrayList<String>();

	private String commentOutPrefix = ";//BCT-STUBBED-OUT ";
	private int commentOutDelta = 0;

	private boolean redefineMathFuncs;

	private boolean checkCoverage;

	private Map<String, FunctionMonitoringData> functionData;
	
	
	private static class InstrumentationInfo {
		public MultiHashMap<File, Integer> entryPoints = new MultiHashMap<File, Integer>();
	}
	
	{
		additionalDefs.add( "int atoi(char*);" );
		
		
		String commentOutDeltaString = System.getProperty(COMMENT_OUT_DELTA);
		if ( commentOutDeltaString != null ){
			commentOutDelta = Integer.valueOf(commentOutDeltaString);
		}
		
		String commentOutBeginString = System.getProperty(COMMENT_OUT_BEGIN);
		if ( commentOutBeginString != null ){
			commentOutPrefix = commentOutBeginString;
		}
	}


	public CBMCAssertionsInjector(File cbmcModelsFolder, Map<String, FunctionMonitoringData> functionData ) {
		this.cbmcTempFolder = cbmcModelsFolder;
		this.functionData = functionData;
	}
	
	public static CBMCAssertionsInjector buildAssertionsInjector( File srcFolder, File modelsFile, File cbmcTempFolder, File functionMonitoringDataFile ) throws FileNotFoundException, ClassNotFoundException, IOException{
		FileUtil.deleteRecursively(cbmcTempFolder);

		FileUtil.copyDirectory(srcFolder, cbmcTempFolder);
		
		Map<String, FunctionMonitoringData> _functionData;
		if ( functionMonitoringDataFile != null ){
			_functionData = FunctionMonitoringDataSerializer.load(functionMonitoringDataFile);
		} else {
			_functionData = new HashMap<String, FunctionMonitoringData>();
		}
		
		CBMCAssertionsInjector injector = new CBMCAssertionsInjector(cbmcTempFolder, _functionData);
		
		String redefineCppLibsString = System.getProperty( BCT_CBMC_REDEFINE_CPPLIBS );
		if ( redefineCppLibsString != null ){
			injector.redefineCppLibs = Boolean.parseBoolean(redefineCppLibsString);
		}
		
		String testFunctionsString = System.getProperty(BCT_TEST_FUNCTIONS);
		if ( testFunctionsString != null ){
			String[] funcs = testFunctionsString.split(";");
			List<String> testFunctions = Arrays.asList(funcs);
			injector.setTestFunctions( testFunctions );
		}
		
		return injector;
		
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		File srcFolder = new File( args[0] );
		File modelsFile = new File( args[1] );
		File cbmcTempFolder = new File( args[2] );

		File functionMonitoringDataFile = null;
		if ( args.length > 3 ){
			functionMonitoringDataFile = new File( args[3] );
		}
		
		CBMCAssertionsInjector injector = buildAssertionsInjector(srcFolder, modelsFile, cbmcTempFolder, functionMonitoringDataFile );

		Map<String, FunctionMonitoringData> functionData = new HashMap<String, FunctionMonitoringData>();
		



		boolean unitVerification = false;
		boolean redefineMathFuncs = false;
		boolean checkCoverage = false;
		boolean targetOnly = false;
		
		
		if ( args.length > 4 ){
			for ( int i = 4; i < args.length; i++){
				if ( "--unitVerification".equals(args[i]) ){
					unitVerification = true;
				}
				if ( "--redefineMathFuncs".equals(args[i]) ){
					redefineMathFuncs = true;
				}
				if ( "--checkCoverage".equals(args[i]) ){
					checkCoverage = true;
				}
				if ( "--targetOnly".equals(args[i]) ){
					targetOnly = true;
				}
				
			}
		}

		
		injector.setUnitVerification( unitVerification );
		injector.setRedefineMathFuncs( redefineMathFuncs );
		injector.setCheckCoverage( checkCoverage );
		
		
		
		if ( targetOnly ){
			injector.setEntryPointsType(EntryPointsTypes.Targets);
		} else {
			injector.setEntryPointsType(EntryPointsTypes.CallersOfTargets);
		}
		
		
		

		
		InstrumentationInfo instr = new InstrumentationInfo();
		injector.injectAssertions( modelsFile );

		
		
	}

	public void setCheckCoverage(boolean checkCoverage) {
		this.checkCoverage = checkCoverage;
	}

	public void setRedefineMathFuncs(boolean redefineMathFuncs) {
		this.redefineMathFuncs = redefineMathFuncs;
	}

	private void setTestFunctions(List<String> testFunctions) {
		this.testFunctions.addAll( testFunctions );
	}

	private boolean unitVerification;

	private FunctionMonitoringData mainFunction;

	private boolean hideManualAssertions = true;

	private HashMap<String, Set<FunctionMonitoringData>> commentOut;

	private boolean redefineCppLibs = true;

	private boolean useSimmetricIntegers = false;

	public boolean isUseSimmetricIntegers() {
		return useSimmetricIntegers;
	}

	public void setUseSimmetricIntegers(boolean useSimmetricIntegers) {
		this.useSimmetricIntegers = useSimmetricIntegers;
	}

	public void setUnitVerification(boolean unitVerification) {
		this.unitVerification = unitVerification;
	}

	public void injectAssertions(File modelsFile) throws IOException {
		
		File srcFolder = cbmcTempFolder;
		
		if ( redefineMathFuncs ){
			additionalDefs.add( "int rand();" );
		}
		
		ModelsData modelsData = loadModels( modelsFile );
		Map<String, SortedSetMultimap<Integer, String>> models = modelsData.outputMap; 

		this.mainFunction = functionData.get("main");
		
		Map<String, Set<FunctionMonitoringData>> functionsToCommentOut = retrieveFunctionsToCommentOut(functionData);

		LOGGER.fine("Functions to comment out:");
		for ( Entry<String, Set<FunctionMonitoringData>> e : functionsToCommentOut.entrySet() ){
			String file = e.getKey();
			for ( FunctionMonitoringData function : e.getValue() ){
				LOGGER.fine(file + " " + function.getMangledName() );	
			}
			
		}
		
		LOGGER.fine("Models ");
		for( Entry<String, SortedSetMultimap<Integer, String>>  entry : models.entrySet() ){
			LOGGER.fine("File: "+entry.getKey()+" "+entry.getValue());
		}
		
		HashSet<String> processedFiles = new HashSet<String>();
		for ( String fileName : modelsData.files ){

			SortedSetMultimap<Integer, String> assertionsPerLine = models.get(fileName);
			
			if ( LOGGER.isLoggable(Level.FINE ) ){
				LOGGER.fine("Assertions for "+fileName+" : "+assertionsPerLine );
			}
			
			injectAssertionsInFile( new File( srcFolder.getAbsolutePath() + "/" + fileName ) , fileName, assertionsPerLine, modelsData.exitModels, functionData, functionsToCommentOut );
			processedFiles.add(fileName);
		}

		for ( String fileName : functionsToCommentOut.keySet() ){
			
			if ( processedFiles.contains(fileName) ){
				continue;
			}
			
			if ( fileName == null ){
				
				Set<FunctionMonitoringData> funcs = functionsToCommentOut.get(null);
				String functionsList = "";
				for ( FunctionMonitoringData func : funcs ){
					functionsList+=func.getMangledName();
				}
				System.out.println("NULL FILENAME "+functionsList);
				LOGGER.warning("null filename "+functionsList);
				continue;
			}
			
			SortedSetMultimap<Integer, String> assertionsPerLine = models.get(fileName);
			injectAssertionsInFile( new File( srcFolder.getAbsolutePath() + "/" + fileName ) , fileName, assertionsPerLine, modelsData.exitModels, functionData, functionsToCommentOut );
		
		}
		
		if ( mainFunction != null ){
			String mainFile = mainFunction.getSourceFileLocationClean();
			if ( ! processedFiles.contains(mainFile) ){
				injectAssertionsInFile( new File( srcFolder.getAbsolutePath() + "/" + mainFile ) , mainFile, null , modelsData.exitModels, functionData, functionsToCommentOut );
			}
		}

		ProcessedAssertionsLoader.saveToTextFiles( processedAssertions, modelsFile );

	}

	private Map<String, Set<FunctionMonitoringData>> retrieveFunctionsToCommentOut(
			Map<String, FunctionMonitoringData> functionData) {
		if ( commentOut != null ){
			return commentOut;
		}
		
		commentOut = new HashMap<String,Set<FunctionMonitoringData>>();
		
		String commentOutString = System.getProperty(COMMENT_OUT_LINES_NOT_MONITORED);
		boolean commentOutNotMonitored = Boolean.parseBoolean(commentOutString);
		if ( ! commentOutNotMonitored ){
			return commentOut;
		}
		
		Map<String, FunctionMonitoringData> allFuncs = new HashMap<String, FunctionMonitoringData>();
		allFuncs.putAll(functionData);
		
		for ( FunctionMonitoringData f : functionData.values() ){
			for ( FunctionMonitoringData c :  f.getCallees() ){
				allFuncs.put(c.getMangledName(), c);
			}
			for ( FunctionMonitoringData c :  f.getCallers() ){
				allFuncs.put(c.getMangledName(), c);
			}
		}
		
		String commentMode = System.getProperty(COMMENT_MODE);
		if ( "all".equals(commentMode) ){
			for ( FunctionMonitoringData f : allFuncs.values() ){
				if ( f.isTargetFunction() ){
					continue;
				}
				f.getCallees();
				if ( monitorFunction(f) || f.isCalledByTargetFunction() ){
					continue;
				}
				
				String file = f.getSourceFileLocation();
				Set<FunctionMonitoringData> functions = commentOut.get(file);
				if ( functions == null ){
					functions = new HashSet<FunctionMonitoringData>();
					commentOut.put(file, functions);
				}
				functions.add(f);
			}			
		} else {
			for ( FunctionMonitoringData f : functionData.values() ){
				if ( f.isTargetFunction() ){
					continue;
				}

				if ( monitorFunction(f) || f.isCalledByTargetFunction() ){
					for ( FunctionMonitoringData called : f.getCallees() ){
						if ( ! ( called.isTargetFunction() || monitorFunction(called) ||  called.isCalledByTargetFunction() )  ){
							String file = called.getSourceFileLocation();
							Set<FunctionMonitoringData> functions = commentOut.get(file);
							if ( functions == null ){
								functions = new HashSet<FunctionMonitoringData>();
								commentOut.put(file, functions);
							}
							functions.add(called);
						}
					}
				}
			}
		}
		
		return commentOut;
	}

	private static class ExitAssertionData {
		public final List<String> assertions;
		public final FunctionMonitoringData functionData;
		private boolean missingReturnStatement;

		public ExitAssertionData(List<String> assertions,
				FunctionMonitoringData functionData, boolean forceExitAssertion) {
			super();
			this.assertions = assertions;
			this.functionData = functionData;
			this.missingReturnStatement = forceExitAssertion;
		}

	}

	private void injectAssertionsInFile(File sourceFile, String relativeFileName,
			SortedSetMultimap<Integer, String> assertionsPerLine, Map<String, List<String>> exitModels, Map<String, FunctionMonitoringData> functionData, Map<String, Set<FunctionMonitoringData>> functionsToCommentOut ) throws IOException {
		LOGGER.entering(CBMCAssertionsInjector.class.getCanonicalName(), "injectAssertionsInFile "+sourceFile.getAbsolutePath() );

		LOGGER.fine("Processing assertions for "+relativeFileName);

		int linesAddedToHandleMain = 0;
		int mainBegin = -1;
		if ( unitVerification ){

			LOGGER.entering(CBMCAssertionsInjector.class.getCanonicalName(), "mainFile " );
			if ( implementedInFile(mainFunction, sourceFile)){
				mainBegin = mainFunction.getFirstSourceLine()-1;
			}
			if ( mainFunction.isTargetFunction() || mainFunction.isCallerOfTargetFunction() ){
//				mainBegin =-1; NO it is necessary to add bct_hidden_main() to the new main
			}

		}
		
		
		//int cbmc_rand_int(){ int x = nondet_int(); __CPROVER_assume( x > -2147483648 ); return x; };
		
		Set<FunctionMonitoringData> commentedFunctions = functionsToCommentOut.get(relativeFileName);
		
		List<ExitAssertionData> exitAssertionsToInject = extractExitAssertions( sourceFile, functionData, exitModels, commentedFunctions );

		List<String> lines = FileUtil.getLines( sourceFile);

		TreeSet<Integer> commentedLines = commentOutLines(relativeFileName, functionsToCommentOut, lines);
		
		ArrayList<Entry<Integer,String>> linesList = new ArrayList<Entry<Integer,String>>();
		if ( assertionsPerLine != null ){
			linesList.addAll( assertionsPerLine.entries() );
		}
		assert ( ! linesList.contains(null) );

		Set<Integer> enterLines = extractEnterLines( sourceFile, functionData );


		LOGGER.fine("Assertions for file "+relativeFileName+" "+assertionsPerLine);
		
		for( int _curLine = lines.size()-1; _curLine >=0; _curLine-- ){

			String lineContent = lines.get(_curLine);
			Integer lineNo = _curLine+1;
			
			
			
			

			int lineIndex = _curLine;
			
			LOGGER.fine("Processing line "+lineNo);
			if ( commentedLines.contains(lineNo) ){
				LOGGER.fine("Skipping line "+lineNo + ": commented out");
				continue;
			}

			String lineContentTrim = lineContent.trim();
			//HACK TO WORK WITH CBMC 2013-07-04, it does not work if files have #include <assert.h> on cygwin
			if ( lineContentTrim.startsWith("#include") && lineContentTrim.contains("<assert.h>") ){
				lineContent = "//BCT: REMOVED FOR CBMC BUG: "+lineContent;
				lines.set(lineIndex, lineContent);
				lineContentTrim = lineContent.trim();
			}
			
			if ( redefineCppLibs ){
				if ( lineContentTrim.startsWith("#include") && ( lineContentTrim.contains("<cstdlib>") ) ){
					lineContent = "//BCT: REMOVED FOR OPTIMIZATION : "+lineContent;
					lines.set(lineIndex, lineContent);
					lineContentTrim = lineContent.trim();
				}	
			}
			
			if ( redefineMathFuncs ){
				if ( lineContentTrim.startsWith("#include") && ( lineContentTrim.contains("<math.h>") || lineContentTrim.contains("<stdlib.h>") ) ){
					lineContent = "//BCT: REMOVED FOR OPTIMIZATION : "+lineContent;
					lines.set(lineIndex, lineContent);
					lineContentTrim = lineContent.trim();
				}	
			}
			
			if ( hideManualAssertions ){
				if ( lineContentTrim.startsWith("assert") ){
					lineContent = "//BCT: HIDDEN MANUAL-ASSERTION: "+lineContent;
					lines.set(lineIndex, lineContent);
				}
			}
			
			boolean firstLineOfMain = false;
			if ( mainBegin > 0 && lineIndex == mainBegin){
				
				if ( ! lineContent.contains("main") ){//WORKAROUND BUG FIX
					mainBegin = mainBegin-1;
				} else {
					firstLineOfMain = true;
					lines.set(lineIndex, lineContent.replace("main", BCT_HIDDEN_MAIN_NAME) );
				}
			} else {
				if ( enterLines.contains( lineNo ) ){
					lineNo = lineNo+1;
					lineIndex = lineNo-1;
					LOGGER.fine("Skipping enter");
					continue;
				}
			}


			ExitAssertionData exitAssertion = retrieveExitAssertions( lineContent, lineNo, exitAssertionsToInject );


			if ( exitAssertion != null ){
				injectExitAssertions( lines, lineIndex, lineContent, exitAssertion, sourceFile, relativeFileName );
			}

			if ( assertionsPerLine != null ){
				//				String allAsssertions = "";
				
				SortedSet<String> assertionsToInject = assertionsPerLine.get(lineNo);
				
				
				
				if ( assertionsToInject != null ){
					LOGGER.fine("Assertions to inject "+assertionsToInject);
					
					if( canInjectLine(lines, lineIndex) ) {

						for ( String assertion : assertionsToInject ){
							processedAssertion( relativeFileName, lineNo, assertion );
							
							if ( checkCoverage ){
								String content = assertion.substring(6,assertion.length()-1);
								assertion="assert(0&&"+content+");";
							}
							
							lines.add(lineIndex,assertion);
							//					allAsssertions += assertion;
						}
					} else {
						LOGGER.fine("Line "+lineNo+" , cannot inject assertions: "+assertionsToInject);
					}
				} else {
					LOGGER.fine("No assertions for line "+lineNo);
				}
				//				String content = allAsssertions + lines.get(lineNo-1);
				//				lines.set(lineNo-1, content);
			}

			if ( firstLineOfMain ){
				String randomIntFunction = "cbmc_rand_int()";
				
				int oldLineIndex = lineIndex;
				lineIndex = lines.size();
				
				lines.add(lineIndex,"}");
				
				Set<FunctionMonitoringData> coveredTargets = new HashSet<FunctionMonitoringData>();
				HashSet<FunctionMonitoringData> uncoveredTargets = new HashSet<FunctionMonitoringData>();
				
				for ( FunctionMonitoringData function : functionData.values() ){
					if ( monitorFunction(function) ){

						if ( entryPointsType == EntryPointsTypes.CallersOfTargets ){
							//In case we use callers as entry point we try not 
							//not monitor target functions directly 
							//(even if they are callers of other targets)
							if ( function.isTargetFunction() ){
								uncoveredTargets.add(function);
								continue;
							}
						}
						
						if ( testFunctions.contains(function.getMangledName()) ){
							uncoveredTargets.addAll(function.getInvokedTargets());
							continue;
						}
						
						coveredTargets.addAll( function.getInvokedTargets() );
						
						String invocation = addCallInMCMain(lines, lineIndex,
								randomIntFunction, function);
						
					}
				}
				
				uncoveredTargets.removeAll(coveredTargets);
				if ( testFunctions.size() > 0 ){
					for ( FunctionMonitoringData function : uncoveredTargets ){
						addCallInMCMain(lines, lineIndex,
								randomIntFunction, function);
					}
				}
				
				lines.add(lineIndex,"int main(){");
				lines.add(lineIndex,"int "+BCT_HIDDEN_MAIN_NAME+"();");
				lines.add(lineIndex, buildRandomIntDefinition( randomIntFunction ));
				
				lineIndex = oldLineIndex;
			}
		}
		
		for ( String additionalDef : additionalDefs ){
			lines.add(0, additionalDef);
		}
		int additionalDefsNumber = additionalDefs.size();
		
		int assertionsAdded = 0;
		for( ProcessedAssertion processedAssertion : processedAssertions ){
			if ( ! processedAssertion.getRelativePath().equals(relativeFileName) ){
				//latest assertions added (i.e. the ones for current file) are on top of the list
				continue;
			}
			
			int newLineNo = processedAssertion.getLineNo() + assertionsAdded + additionalDefsNumber;
			
			
			processedAssertion.setSourceLineNo( newLineNo );
			assertionsAdded++;
		}

		BufferedWriter w = new BufferedWriter(new FileWriter(sourceFile));
		try{
			for ( String line : lines ){
				w.write(line);
				w.newLine();
			}
		} finally {
			w.close();
		}


	}

	private String buildRandomIntDefinition(String randomIntFunction) {
		StringBuffer defLineBuffer = new StringBuffer();
		defLineBuffer.append( "int ");
		defLineBuffer.append( randomIntFunction );
		
		if ( useSimmetricIntegers ){
			defLineBuffer.append( "{ int x = nondet_int(); __CPROVER_assume( x > -2147483648 ); return x; }" );	
		} else {
			defLineBuffer.append( ";" );
		}

		return defLineBuffer.toString();
	}

	public enum EntryPointsTypes { Targets, CallersOfTargets }
	private EntryPointsTypes entryPointsType = EntryPointsTypes.CallersOfTargets;
	
	public void setEntryPointsType( EntryPointsTypes entryPointsType ){
		this.entryPointsType = entryPointsType;
	}
	
	public boolean monitorFunction(FunctionMonitoringData function) {
		if ( entryPointsType == EntryPointsTypes.Targets ){
			return function.isTargetFunction();
		}
		return function.isCallerOfTargetFunction();
	}

	public String addCallInMCMain(List<String> lines, int lineIndex,
			String randomIntFunction, FunctionMonitoringData function) {
		String invocation = getFunctionInvocationForVerification(
				randomIntFunction, function);
		if (function.getMangledName().equals("main") ){
			invocation = "bct_hidden_main();";
		}
		lines.add(lineIndex,"//VART-ENTRY-POINT: "+invocation);
		lines.add(lineIndex,invocation);
		return invocation;
	}

	public TreeSet<Integer> commentOutLines(String relativeFileName,
			Map<String, Set<FunctionMonitoringData>> functionsToCommentOut,
			List<String> lines) {
		TreeSet<Integer> res = new TreeSet<Integer>();
		
		Set<FunctionMonitoringData> funcsToCommentOut = functionsToCommentOut.get(relativeFileName);
		if ( funcsToCommentOut != null ){
			for ( FunctionMonitoringData toCommentOut : funcsToCommentOut ){
				for ( int i = toCommentOut.getFirstSourceLine()+commentOutDelta; i <= toCommentOut.getLastSourceLine(); i++ ){
					int lineNo = i-1;
					String lineContent = lines.get(lineNo);
					lines.set(lineNo, commentOutPrefix+lineContent);
					res.add(lineNo+1);
				}
			}
		}
		return res;
	}

	private Set<Integer> extractEnterLines(File sourceFile,
			Map<String, FunctionMonitoringData> functionData) {
		if ( functionData == null ){
			return null;
		}

		Set<Integer> res = new HashSet<Integer>();

		List< ExitAssertionData> map = new ArrayList< ExitAssertionData>();

		try {
			List<String> lines = FileUtil.getLines( sourceFile);

			for ( FunctionMonitoringData function : functionData.values() ){
				if ( ! function.isImplementedWithinProject() ){
					continue;
				}

				if ( ! implementedInFile(function,sourceFile) ){
					continue;
				}

				res.add(function.getFirstSourceLine());
			}
		} catch (Exception e ){
			LOGGER.log(Level.WARNING, "", e);
		}

		return res;
	}

	public String getFunctionInvocationForVerification(
			String randomIntFunction, FunctionMonitoringData function) {
		String invocation = function.getDemangledName();
		
		if ( function.isStatic() ){
			int pos = invocation.indexOf('(');
			if ( pos > 0 ){
				invocation = invocation.substring(0,pos);
			}
		} else {
			if ( function.isCpp() ){
				return "//SKIP, cannot invoke CPP instance method "+invocation;
			}
		}
				
		invocation = invocation+"(";
		
		int pars = function.getParametersNumber();
		
		if ( pars == -1 ){
			return "//invocation of "+function.getMangledName()+" SKIPPED";
		}
		
		for (int i = 0; i < pars;i++){
			if ( i > 0 ){
				invocation+=",";
			}
			invocation+=randomIntFunction;
		}
		invocation+=");";
		return invocation;
	}

	private LinkedList<ProcessedAssertion> processedAssertions = new LinkedList<ProcessedAssertion>();

	//	public static final String BCT_ASSERTIONS_EXP = "bct.assertions.expressions";
	
	///list of test functions that must be used as entry points by model checker
	public static final String BCT_TEST_FUNCTIONS = "bct.testCases"; 
	
	private void processedAssertion(String sourceFileRelativePath, Integer lineNo,
			String assertion ) {
		processedAssertions.push(new ProcessedAssertion(sourceFileRelativePath,lineNo,assertion));
	}
	
	
	

	private void injectExitAssertions(List<String> lines, int curLine,
			String lineContent, ExitAssertionData exitAssertion, File sourceFile, String relativeFileName) {

		if ( exitAssertion.assertions == null ){
			return;
		}
		
		LOGGER.info("Injecting exit assertions for function "+exitAssertion.functionData.getMangledName()+" in file "+sourceFile.getAbsolutePath()+" : "+curLine);
		
		boolean voidReturnType = false;
		{
			String returnType = exitAssertion.functionData.getReturnType();
			
			if ( "struct*".equals( returnType ) ){
				LOGGER.warning("ERRONEOUS return type: "+returnType);
				return;
			}
			
			if ( returnType.endsWith("void") || returnType.isEmpty() ){
				voidReturnType = true;
			}
		}
		
		
		
		String newVar = null;
		if ( ! exitAssertion.missingReturnStatement ){
			
			if ( ! canInjectExit( lines, curLine ) ){
				return;
			}
			
			String returnType = exitAssertion.functionData.getReturnType();

			returnType = returnType.trim();
			
			if ( returnType.startsWith("static") ){
				returnType = returnType.substring(7).trim();
			}
			
			if ( returnType.startsWith("*") ){
				returnType = "void "+returnType;
			}


			if ( voidReturnType ){
				String lineContentNoSpace = lineContent.replaceAll("\\s", "");
				
				if ( ! "return;".equals(lineContentNoSpace) ){
					LOGGER.warning("CANNOT INJECT EXIT ASSERTION: Non empty return in empty function : "+lineContent );
					return;
				}
				
				
				lines.add(curLine,"{ ");
				
				lines.remove(curLine+1);
				String newReturn = "return; }";
				processedAssertions.push(new ReturnInstruction(relativeFileName,curLine));
				lines.add(curLine+1,newReturn);
			} else {
				newVar = "_ret_"+(curLine+1);
				lineContent = "{ "+lineContent.replace("return ", returnType+" "+newVar+"=");

				lines.add(curLine,lineContent);
				lines.remove(curLine+1);
				String newReturn = "return "+newVar+"; }";
				processedAssertions.push(new ReturnInstruction(relativeFileName,curLine));
				lines.add(curLine+1,newReturn);
			}
		} else {
			LOGGER.info("MISSING RETURN STATEMENT, not adding aux return var.");
//			return;
		}
		
		int pos = curLine+1;
		
		ArrayList<String> assertions = new ArrayList<String>();
		for ( String assertion : exitAssertion.assertions){
			
			if (newVar == null && assertion.contains("return") ){
				LOGGER.info("SKIPPING return assertion: "+assertion);
				continue;
			}
			//TODO: we should parse the line to be sure taht we replace only variable return, and not suffixes in variables like 'returnResult'
			//TODO: we should properly handle access to fields if we want assertions for C++
			
			processedExitAssertion(relativeFileName, exitAssertion.functionData.getMangledName(), pos+1, assertion);
			
			
			if ( newVar != null ){
				assertion = assertion.replaceAll("return", newVar); 
			} 
			
			if ( checkCoverage ){
				String content = assertion.substring(6,assertion.length()-1);
				assertion="assert(0&&"+content+");";
			}
			
			assertions.add(assertion); //the assertion in fron of the list is the one at the bottom of the return block
			
			
			
			lines.add(pos,assertion);
			
			
		}
		
		
	}

	private void processedExitAssertion(String relativeFileName,
			String mangledName, int lineNo, String assertion) {
		processedAssertions.push(new ProcessedAssertion(relativeFileName,lineNo,assertion,mangledName));
	}

	//	private static Pattern terminatorChar = Pattern.compile(".*[}{;]$");
	public static boolean endsWithTerminator(String lineContent) {
		
		char last = lineContent.charAt(lineContent.length()-1);
		
		switch ( last ){
		case '}':
		case '{':
		case ';':
			return true;
		}
		
		if ( lineContent.endsWith("*/") ){
			return true;
		}
		
		if ( lineContent.startsWith("//") ){
			return true;
		}
		
		return false;
	}
	private boolean canInjectLine(List<String> lines, int curLine) {
		String prevLine = retrievePreviousLine(lines, curLine);
		
		if ( prevLine == null ){
			LOGGER.info("CANNOT INSERT ASSERTION: No previous line, curLine "+curLine);
			return false;
		}
		
		if ( ! endsWithTerminator(prevLine) ){
			LOGGER.info("CANNOT INSERT ASSERTION: Previous line does not end with terminator char, curLine "+curLine+" , previous line: "+prevLine);
			return false;
		}
		
		String nextLine = retrieveNextLine(lines, curLine);
		
		if ( nextLine != null ){
			if ( startWithMIddleKeyword( nextLine ) ){
				LOGGER.info("CANNOT INSERT ASSERTION: Nextline start with keyword: "+nextLine+" curline: "+curLine);
				return false;
			}
		}
		
		
//		String lineContent = lines.get( curLine ).trim();
//		
//		if ( startsWithOperator( lineContent ) ){
//			LOGGER.info("CANNOT INSERT ASSERTION: Line starts with operator, curLine "+curLine+" lineContent "+lineContent);
//			return false;
//		}
		
		return true;
	}

	private boolean startWithMIddleKeyword(String nextLine) {
		nextLine = nextLine.trim();
		if ( nextLine.startsWith("else") ){
			return true;
		}
		if ( nextLine.startsWith("while") && nextLine.endsWith(";") ){
			return true;
		}
		
		return false;
	}

	public String retrievePreviousLine(List<String> lines, int curLine) {
		while (  curLine > 0 ){
			String line = lines.get( curLine - 1 ).trim();
			if ( ! line.isEmpty() ){
				return line;
			}
			curLine--;
		}
		return null;
	}

	public String retrieveNextLine(List<String> lines, int curLine) {
		
		while (  curLine < lines.size() ){
			String line = lines.get( curLine ).trim();
			if ( ! line.isEmpty() ){
				return line;
			}
			curLine++;
		}
		return null;
	}
	
	private boolean canInjectExit(List<String> lines, int curLine) {
		String lineContent = lines.get( curLine ).trim();
		
		if ( ! lineContent.endsWith(";") ){
			LOGGER.info("CANNOT INSERT ASSERTION: return line not terminated, curLine "+curLine);
			return false;
		}
		
		if ( startsWithOperator( lineContent ) ){
			LOGGER.info("CANNOT INSERT ASSERTION: return line starts with operator, curLine "+curLine);
			return false;
		}
		
		return true;
	}

	private static Pattern operatorStart = Pattern.compile("^[a-zA-Z0-9_].*");
	public static boolean startsWithOperator(String lineContent) {
		if ( lineContent.startsWith("//") ){
			return false;
		}
		return ! operatorStart.matcher(lineContent).matches();
	}

	private ExitAssertionData retrieveExitAssertions(String lineContent, int curLine,
			List<ExitAssertionData> exitAssertionsToInject ) {
		if ( exitAssertionsToInject == null ){
			return null;
		}

		boolean lineContainsReturn = lineContainsReturn( lineContent );


		for ( ExitAssertionData exitAssertion : exitAssertionsToInject ){
			if ( exitAssertion.missingReturnStatement ){
				if ( exitAssertion.functionData.getLastSourceLine() == (curLine+1) ){
					return exitAssertion;
				}
			}
			if ( lineContainsReturn && exitAssertion.functionData.containsLine(curLine) ){
				return exitAssertion;
			}
		}
		return null;
	}

	private List<ExitAssertionData> extractExitAssertions(
			File sourceFile, Map<String, FunctionMonitoringData> functionData, Map<String, List<String>> exitModels, Set<FunctionMonitoringData> commentedFunctions) throws FileNotFoundException {
		try {
			if ( functionData == null ){
				return null;
			}

			List< ExitAssertionData> map = new ArrayList< ExitAssertionData>();


			List<String> lines = FileUtil.getLines( sourceFile);

			for ( FunctionMonitoringData function : functionData.values() ){
				if ( ! function.isImplementedWithinProject() ){
					continue;
				}

				if ( ! implementedInFile(function,sourceFile) ){
					continue;
				}

				if ( commentedFunctions != null && commentedFunctions.contains(function) ){
					continue;
				}

				boolean forceExitAssertion = false;
				if ( ! functionContainsReturnStatement( lines, function ) ){
					forceExitAssertion = true;
				}

				//we use ends with because we work on temporary folder, which is not in the same location of getAbsoluteFile
				String relativeSource = sourceFile.getAbsolutePath().substring( cbmcTempFolder.getAbsolutePath().length() );
				if ( function.getAbsoluteFile().getAbsolutePath().endsWith( relativeSource ) ){
					map.add( new ExitAssertionData(exitModels.get(function.getMangledName()), function, forceExitAssertion));
				}
			}

			return map;

		} catch ( IllegalStateException e ){
			System.err.println("Error processing file "+sourceFile);
			throw e;
		}
	}

	private boolean implementedInFile(FunctionMonitoringData function,
			File sourceFile) {
		String sourceFileRelative = sourceFile.getAbsolutePath().substring(cbmcTempFolder.getAbsolutePath().length()+1);
		if ( sourceFileRelative.equals(function.getSourceFileLocation()) ){
			return true;
		}

		return false;
	}

	private boolean functionContainsReturnStatement(List<String> lines, FunctionMonitoringData function) {
		int first = function.getFirstSourceLine()-1;
		int last = function.getLastSourceLine();

		for( int i = last-1; i >= first; i-- ){
			if ( i >= lines.size() ){
				throw new IllegalStateException("Function "+function.getMangledName()+" expected to start at line "+first+" and end at line "+last+". But file has only "+lines.size());
			}
			String line = lines.get(i);

			if ( lineContainsReturn(line) ){
				return true;
			}
		}

		return false;
	}

	private boolean lineContainsReturn(String line) {
		StringTokenizer st = new StringTokenizer(line,ViolationsUtil.DELIMITERS);
		while ( st.hasMoreTokens() ){
			if ( st.nextToken().equals("return") ){
				return true;
			}
		}
		return false;
	}

	private ModelsData loadModels(File modelsFile) throws IOException {
		Map<String, SortedSetMultimap<Integer, String>> outputMap = new HashMap<String, SortedSetMultimap<Integer, String>>();
		Map<String,List<String>> exitModels = new HashMap<String, List<String>>();
		List<String> files = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new FileReader(modelsFile));

		String line;
		String currentFile = null;
		SortedSetMultimap<Integer, String> currentMap = null;

		while ( ( line = reader.readLine() ) != null ){
			String[] content = line.split("\\t");
			String file = content[0];

			String assertion = content[2];

			if ( ! file.equals(currentFile ) ){
				currentFile = file;
				files.add(file);
				currentMap =  TreeMultimap.create();
				outputMap.put(currentFile, currentMap);
			}


			try {
				Integer lineNo = Integer.valueOf(content[1]);


				assert( lineNo != null );
				currentMap.put(lineNo, assertion);
			} catch ( NumberFormatException e ){
				String functionName = content[1];
				List<String> assertionsList = exitModels.get(functionName);
				if ( assertionsList == null ){
					assertionsList = new ArrayList<String>();
					exitModels.put(functionName, assertionsList);
				}
				assertionsList.add(assertion);
			}



		}


		return new ModelsData( outputMap, exitModels, files );
	}

	private static class ModelsData{

		public final Map<String, SortedSetMultimap<Integer, String>> outputMap;
		public final Map<String, List<String>> exitModels;
		private List<String> files;

		public ModelsData(
				Map<String, SortedSetMultimap<Integer, String>> outputMap,
				Map<String, List<String>> exitModels, List<String> files) {
			this.outputMap = outputMap;
			this.exitModels = exitModels;
			this.files = files;
		}

	}
}
