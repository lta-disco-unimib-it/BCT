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



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Set;
import java.util.TreeSet;

import cpp.gdb.FunctionMonitoringData.ReturnData;
import cpp.pin.PINBufferedProbeGenerator;
import cpp.pin.PINProbeGenerator;

import tools.gdbTraceParser.FunctionEntryPointDetector;
import tools.gdbTraceParser.FunctionEntryPointDetector.FunctionEntryPoint;
import util.componentsDeclaration.Component;
import util.componentsDeclaration.ComponentDefinitionImporter;
import util.componentsDeclaration.ComponentsDefinitionException;
import util.componentsDeclaration.MatchingRuleInclude;

public class GdbRegressionConfigCreator {
	
	///PROPERTIES-DESCRIPTION: Options that control the generation of configurations script for RADAR/VART/BDCI
	
	///indicates the location where PIN has been installed 
	public static final String BCT_PIN_HOME = "bct.pinHome";
	
	///if true do not use buffer when monitoring with PIN (default: false)
	public static final String BCT_PIN_PROBES_WITHOUT_BUFFER = "bct.PINprobesWithoutBuffer";

	///if true PIN probes are created  (default: false)
	public static final String BCT_GENERATE_PIN_PROBES = "bct.generatePINprobes";

	///if true traces the line number of the return instruction taken
	private static final String BCT_TRACE_RETURN_LINES = "bct.traceReturnLines";

	///comma separated list of lines to monitor, each line is written as <file>:<line>
	public static final String BCT_MONITORING_LINES_TO_MONITOR = "bct.monitoring.linesToMonitor";
	
	///list of folder paths not to be considered as source folders. Path relative to project path. Separated by ; .
	private static final String BCT_FOLDERS_TO_FILTER_OUT = "bct.foldersToFilterOut";
	
	///if true, all the variables declared before the current breakpoint are inspected with a gdb print instruction
	private static final String BCT_INSPECT_LOCALS_ONE_BY_ONE = "bct.inspectLocalsOneByOne";
	
	public static final String SET_FOLLOW_FORK_MODE = "set follow-fork-mode ";
	private static final Logger LOGGER = Logger.getLogger(GdbRegressionConfigCreator.class.getCanonicalName());

	public static final String SET_LOGGING_FILE = "set logging file ";

	public static class Configuration {
		
		
		private StaticFunctionsFinder staticFunctionsFinder;
		private boolean monitorIntraComponentCalls;
		private boolean useDemangledNames;
		public static final String DEFAULT_LOGGNG_FILE_NAME = "gdb.monitor.txt";
		private String loggingFilePath = DEFAULT_LOGGNG_FILE_NAME;
		private boolean monitorInternalLines;
		private boolean monitorChildrenInternalLines;
		private boolean recordCallingContextData;
		private boolean monitorLibraryCalls;
		private boolean dll;
		private boolean traceReturnLine = false;
		
		public boolean isTraceReturnLine() {
			return traceReturnLine;
		}

		public void setTraceReturnLine(boolean traceReturnLine) {
			this.traceReturnLine = traceReturnLine;
		}

		private List<File> callgrindCoverageFiles = new ArrayList<File>();
		private Set<String> functionsToFilterOut;
		

		public Set<String> getFunctionsToFilterOut() {
			return functionsToFilterOut;
		}

		public void setFunctionsToFilterOut(Set<String> functionsToFilterOut) {
			this.functionsToFilterOut = functionsToFilterOut;
		}

		public StaticFunctionsFinder getStaticFunctionsFinder() {
			return staticFunctionsFinder;
		}

		public void setStaticFunctionsFinder(StaticFunctionsFinder staticFunctionsFinder) {
			this.staticFunctionsFinder = staticFunctionsFinder;
		}

		private boolean workingOnOriginalSoftware = true;
		
		public boolean isWorkingOnOriginalSoftware() {
			return workingOnOriginalSoftware;
		}

		public void setWorkingOnOriginalSoftware(boolean workingOnOriginalSoftware) {
			this.workingOnOriginalSoftware = workingOnOriginalSoftware;
		}

		private boolean monitorOnlyNotModifiedLines;
		
		public boolean isMonitorOnlyNotModifiedLines() {
			return monitorOnlyNotModifiedLines;
		}

		public void setMonitorOnlyNotModifiedLines(boolean monitorOnlyNotModifiedLines) {
			this.monitorOnlyNotModifiedLines = monitorOnlyNotModifiedLines;
		}

		private boolean monitorFunctionsCalledByTargetFunctions;
		public boolean isMonitorFunctionsCalledByTargetFunctions() {
			return monitorFunctionsCalledByTargetFunctions;
		}

		public void setMonitorFunctionsCalledByTargetFunctions(
				boolean monitorFunctionsCalledByTargetFunctions) {
			this.monitorFunctionsCalledByTargetFunctions = monitorFunctionsCalledByTargetFunctions;
		}

		private boolean monitorLocalVariables;
		public boolean isMonitorLocalVariables() {
			return monitorLocalVariables;
		}

		public void setMonitorLocalVariables(boolean monitorLocalVariables) {
			this.monitorLocalVariables = monitorLocalVariables;
		}

		private boolean monitorCallersOfModifiedFunctions;
		public boolean isMonitorCallersOfModifiedFunctions() {
			return monitorCallersOfModifiedFunctions;
		}

		public void setMonitorCallersOfModifiedFunctions(
				boolean monitorCallersOfModifiedFunctions) {
			this.monitorCallersOfModifiedFunctions = monitorCallersOfModifiedFunctions;
		}

		private boolean monitorFunctionsDefinedOutsideProject = false;
		public boolean isMonitorFunctionsDefinedOutsideProject() {
			return monitorFunctionsDefinedOutsideProject;
		}

		public void setMonitorFunctionsDefinedOutsideProject(
				boolean monitorFunctionsDefinedOutsideProject) {
			this.monitorFunctionsDefinedOutsideProject = monitorFunctionsDefinedOutsideProject;
		}

		private boolean monitorFunctionEnterExitPoints = true;
		private boolean monitorParentOnFork = true;
		private boolean detachOnFork = true;
		private String forkListenerPid = null;
		private boolean monitorPointerToThis = true;
		private List<String> foldersToFilterOut;
		public boolean inspectLocalsOneByOne = true; 
		
		{
			String val = System.getProperty(BCT_INSPECT_LOCALS_ONE_BY_ONE);
			if ( val != null ){
				inspectLocalsOneByOne = Boolean.valueOf(val);
			}
		}


		public List<String> getFoldersToFilterOut() {
			return foldersToFilterOut;
		}

		public void setFoldersToFilterOut(List<String> foldersToFilterOut) {
			this.foldersToFilterOut = foldersToFilterOut;
		}

		public boolean isMonitorPointerToThis() {
			return monitorPointerToThis;
		}

		public void setMonitorPointerToThis(boolean monitorPointerToThis) {
			this.monitorPointerToThis = monitorPointerToThis;
		}

		public void setForkListenerPid(String forkListenerPid) {
			this.forkListenerPid = forkListenerPid;
		}

		public void setDetachOnFork(boolean detachOnFork) {
			this.detachOnFork = detachOnFork;
		}

		public void setMonitorParentOnFork(boolean monitorParentOnFork) {
			this.monitorParentOnFork = monitorParentOnFork;
		}

		public boolean isMonitorFunctionEnterExitPoints() {
			return monitorFunctionEnterExitPoints;
		}

		public void setMonitorFunctionEnterExitPoints(
				boolean monitorFunctionEnterExitPoints) {
			this.monitorFunctionEnterExitPoints = monitorFunctionEnterExitPoints;
		}

		
		
		/**
		 * Return true if program is dll and we monitor enter/eit using address
		 * @return
		 */
		public boolean isDllAddress() {
			return false;
		}
		
		/**
		 * Return true if program is dll and we monitor enter/eit using line numbers
		 * @return
		 */
		public boolean isDllLine() {
			return dll;
		}
		
		public boolean isDll() {
			return isDllLine() || isDllAddress();
		}

		public void setDll(boolean value) {
			this.dll = value;
		}

		public boolean isRecordCallingContextData() {
			return recordCallingContextData;
		}

		public void setRecordCallingContextData(boolean recordCallingContextData) {
			this.recordCallingContextData = recordCallingContextData;
		}

		public void setMonitorChildrenInternalLines(boolean monitorChildrenInternalLines) {
			this.monitorChildrenInternalLines = monitorChildrenInternalLines;
		}

		public void setMonitorInternalLines(boolean monitorInternalLines) {
			this.monitorInternalLines = monitorInternalLines;
		}

		public boolean isUseDemangledNames() {
			return useDemangledNames;
		}

		public void setUseDemangledNames(boolean useDemangledNames) {
			this.useDemangledNames = useDemangledNames;
		}

		public boolean isMonitorIntraComponentCalls() {
			return monitorIntraComponentCalls;
		}

		public void setMonitorIntraComponentCalls(boolean monitorIntraComponentCalls) {
			this.monitorIntraComponentCalls = monitorIntraComponentCalls;
		}

		public String getLoggingFilePath() {
			return loggingFilePath ;
		}

		public void setLoggingFilePath( String loggingFilePath ){
			this.loggingFilePath = loggingFilePath;
		}

		public boolean isMonitorInternalLines() {
			return monitorInternalLines;
		}

		public boolean isMonitorChildrenInternalLines() {
			return monitorChildrenInternalLines;
		}

		public void setMonitorLibraryCalls(boolean b) {
			monitorLibraryCalls = b;
		}

		public boolean isMonitorLibraryCalls() {
			return monitorLibraryCalls;
		}

		public void addCallgrindCoverageFile(File file) {
			this.callgrindCoverageFiles.add( file );
		}

		public boolean isExcludeStaticInitializers() {
			return true;
		}

		@Override
		public String toString(){
			StringBuffer sb = new StringBuffer();
			
			sb.append("GdbRegressionConfigCreator.Configuration [ \n\t");
			sb.append("staticFunctionsFinder "+  staticFunctionsFinder+"\n\t");
			sb.append("monitorIntraComponentCalls "+ monitorIntraComponentCalls+"\n\t");
			sb.append("useDemangledNames "+useDemangledNames+"\n\t");
			sb.append("loggingFilePath "+ loggingFilePath +"\n\t");
			sb.append("monitorInternalLines "+ monitorInternalLines+"\n\t");
			sb.append("monitorChildrenInternalLines "+ monitorChildrenInternalLines+"\n\t");
			sb.append("recordCallingContextData "+ recordCallingContextData+"\n\t");
			sb.append("monitorLibraryCalls "+ monitorLibraryCalls+"\n\t");
			sb.append("dll "+ dll+"\n\t");
			sb.append("callgrindCoverageFiles "+ callgrindCoverageFiles +"\n");
			
			sb.append("]");
			
			return sb.toString();
		}

		public boolean isMonitorParentOnFork() {
			return monitorParentOnFork;
		}

		public boolean isDetachOnFork() {
			return detachOnFork ;
		}

		public String getForkListenerPid() {
			return forkListenerPid;
		}
	}


	public static final String bctPrintPointersFunctionName = "bct_print_pointers";


	private static final String bctForkListenerFunctionName = "bct_before_fork";
	private static final String bctAfterForkListenerFunctionName = "bct_after_fork";
//	private static boolean useEntryPointDetector = false;

	private static boolean useDMalloc = Boolean.getBoolean("bct.useDMalloc");



	HashMap<FunctionMonitoringData,List<Integer>> childrenBreakpointsMap = new HashMap<FunctionMonitoringData, List<Integer>>();
	private AdditionalGdbCommandsCreator additionalCommandsCreator;
	
	public AdditionalGdbCommandsCreator getAdditionalCommandsCreator() {
		return additionalCommandsCreator;
	}

	public void setAdditionalCommandsCreator(
			AdditionalGdbCommandsCreator additionalCommandsCreator) {
		this.additionalCommandsCreator = additionalCommandsCreator;
	}

	private int breakpoints;

	private Configuration configuration;


	private HashMap<LineData,String> monitoredLines = new HashMap<LineData,String>();


	private List<Integer> criticalExitBreakpoints = new ArrayList<Integer>();


	private SourceLinesMapper sourceLinesMapper;


	private Collection<String> sourceFolders;


	private Set<String> coveredFunctions;
	private FunctionEntryPointDetector functionEntryPointDetector;
	private boolean hasTestCases;
	private boolean filterOutDisabled = false;

	private boolean generatePINprobes = Boolean.getBoolean(BCT_GENERATE_PIN_PROBES);

	private boolean bufferedPin = ! Boolean.getBoolean(BCT_PIN_PROBES_WITHOUT_BUFFER);
	
	

	

//	private List<Integer> criticalExitBreakpoints = new ArrayList<Integer>();

	


	public GdbRegressionConfigCreator(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public static class Options {

		private static final String LOCATION_PREFIX = "-locationPrefix";
		public static final String EXECUTABLE = "-executable";
		public static final String LOG_FILE_PATH = "-logFilePath";
		public static final String MONITOR_FUNCTIONS_OUTSIDE_PROJECT = "-monitorProjectFunctionsOnly";
		public static final String MONITOR_LIBRARY_CALLS = "-monitorLibraryCalls";
		public static final String MONITOR_INTERNAL_LINES = "-monitorInternalLines";
		public static final String MONITOR_ALL_FUNCTIONS = "-monitorAllFunctions";
		public static final String SOURCE_PROGRAM_POINT = "-sourceProgramPoint";
		public static final String DONT_MONITOR_FUNCTION_ENTER_EXIT = "-dontMonitorEnterExitPoints";
		public static final String DEMANGLE = "-demangle";
		public static final String DLL = "-dll";
		public static final String CALLGRIND_COVERAGE = "-callgrindOutput";
		
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		ArrayList<String> locationPrefixesToExclude = new ArrayList<String>();
		ArrayList<String> sourceProgramPoints = new ArrayList<String>();

		String logFilePath = null;
		boolean useDemangledNames = false;

		Configuration conf = new Configuration();
		
		boolean monitorAllFunctions = false;
		boolean monitorGlobalVariables = true;
		
		File executable = null;
		
		for ( int i = 0; i < args.length ; ++i ){
			if ( Options.LOCATION_PREFIX.equals(args[i]) ){
				locationPrefixesToExclude.add(args[++i]);
			} else if ( Options.CALLGRIND_COVERAGE.equals(args[i]) ){
				conf.addCallgrindCoverageFile(new File(args[++i]));
			} else if ( Options.EXECUTABLE.equals(args[i]) ){
				executable=new File(args[++i]);
			} else if ( Options.LOG_FILE_PATH.equals(args[i]) ){
				logFilePath  = args[++i];
			} else if ( Options.DEMANGLE.equals(args[i]) ){
				useDemangledNames=true;
			} else if ( Options.SOURCE_PROGRAM_POINT.equals(args[i]) ){
				sourceProgramPoints.add(args[++i]);
			} else if ( Options.MONITOR_ALL_FUNCTIONS.equals(args[i]) ){
				monitorAllFunctions=true;
			} else if ( Options.DLL.equals(args[i]) ){
				conf.setDll(true);
			} else if ( Options.MONITOR_INTERNAL_LINES.equals(args[i]) ){
				conf.setMonitorInternalLines(true);
			} else if ( Options.MONITOR_LIBRARY_CALLS.equals(args[i]) ){
				conf.setMonitorLibraryCalls( true );
			} else if ( Options.MONITOR_FUNCTIONS_OUTSIDE_PROJECT.equals(args[i]) ){
				conf.setMonitorFunctionsDefinedOutsideProject(true);
			} else if ( Options.DONT_MONITOR_FUNCTION_ENTER_EXIT.equals(args[i]) ){
				conf.setMonitorFunctionEnterExitPoints(false);
			}
		}


		File objdumpFile = new File(args[args.length-3]);


		String targetsExpressionsFileLocation = args[args.length-2];

		File targetsExpressionsFile;
		if ( targetsExpressionsFileLocation.equals("--") ){
			targetsExpressionsFile=null;
		} else {
			targetsExpressionsFile = new File(targetsExpressionsFileLocation);
		}

		File configFile = new File(args[args.length-1]);
		
		boolean useEntryPointDetector = true;
		

		try {

			
			conf.setMonitorIntraComponentCalls(true);
			conf.setUseDemangledNames(useDemangledNames);

			if ( logFilePath != null ){
				conf.setLoggingFilePath(logFilePath);
			}

			GdbRegressionConfigCreator c = new GdbRegressionConfigCreator(conf);

			//List<String> expressions = getExpressions( targetsExpressionsFile );
			List<Component> components;
			if ( targetsExpressionsFile != null ){
				components = ComponentDefinitionImporter.getComponents(targetsExpressionsFile);
			} else {
				components = null;
			}
			
			if ( monitorAllFunctions ){
				components = new ArrayList<Component>();
				Component cAll = new Component("ALL");
				cAll.addRule( new MatchingRuleInclude(".*", ".*", ".*") );
				components.add( cAll );
			}

			if ( executable != null ){
				if ( monitorGlobalVariables ){
					AdditionalGdbCommandsCreator additionalCommandsCreator = new GlobalVariablesCommandsCreator( executable );
					c.setAdditionalCommandsCreator(additionalCommandsCreator);
				}
				
				if ( useEntryPointDetector ){
					LOGGER.fine("Setting FunctionEntryPointDetector ");
					FunctionEntryPointDetector detector = new FunctionEntryPointDetector( executable );
					c.setFunctionEntryPointDetector( detector );
				}
			} else {
				LOGGER.fine("No Executable");
			}

			c.createConfig( objdumpFile, components , configFile, locationPrefixesToExclude, sourceProgramPoints );

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ComponentsDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		// File parentFile = new File( args[0]);
		// File childrenFile = new File( args[1]);
		//		

		// GdbRegressionConfigCreator c = new GdbRegressionConfigCreator();
		//		
		// File output = new File(args[2]);
		//		
		// c.createConfig( output, parentFile, childrenFile );
	}

	public void setFunctionEntryPointDetector( FunctionEntryPointDetector detector) {
		functionEntryPointDetector = detector;
	}

	private static List<String> getExpressions(File targetsExpressionsFile) throws FileNotFoundException {

		Scanner scanner = null;
		List<String> lines = new ArrayList<String>();
		try {
			scanner = new Scanner(targetsExpressionsFile);
			while ( scanner.hasNextLine() ){
				lines.add( scanner.nextLine() ); 
			}
		} finally {
			if ( scanner != null ){
				scanner.close();
			}
		}
		return lines;
	}


	public static Map<String, FunctionMonitoringData> getDeclaredFunctions( File objDumpFile, Collection<String> locationPrefixesToExclude  ) throws FileNotFoundException{
		return extractFunctionsData(objDumpFile, locationPrefixesToExclude).getFunctionsData();
	}

	
	
	public static RegressionConfigObjDumpListener extractFunctionsData ( File objDumpFile, Collection<String> locationPrefixesToExclude  ) throws FileNotFoundException{

		ObjDumpParser parser = new ObjDumpParser();

		RegressionConfigObjDumpListener listener = new RegressionConfigObjDumpListener();

		listener.setSourceLocationPrefixesToRemove(locationPrefixesToExclude);

		parser.parse(objDumpFile, listener);

		return listener;
	}





	//private FunctionMonitoringData getFunctionMonitoringData(String curFunc) {
	//		FunctionMonitoringData data = this.functionsData.get(curFunc);
	//		if ( data == null ){
	//			data = new FunctionMonitoringData(curFunc );
	//			functionsData.put(curFunc, data);
	//		}
	//		return data;
	//		
	//	}

	private static String addLeadingZeros(String address, int zeroesToAdd) {
		StringBuffer zeroesBuf = new StringBuffer(); 
		for ( int i = 0 ; i < zeroesToAdd; i++ ){
			zeroesBuf.append("0");
		}

		return zeroesBuf.toString()+address;
	}

	public Set<FunctionMonitoringData> createConfig(File objdumpFile, Collection<Component> components,
			File resultFile, Collection<String> locationPrefixesToExclude ) throws IOException {
		return createConfig(objdumpFile, components, resultFile, locationPrefixesToExclude, null );
	}


	public Set<FunctionMonitoringData> createConfig(File objdumpFile, Collection<Component> components,
			File resultFile, Collection<String> locationPrefixesToExclude, List<String> additionalProgramPointLocations ) throws IOException {
		return createConfig(objdumpFile, components, resultFile, locationPrefixesToExclude, additionalProgramPointLocations, null);
	}
	

	public Set<FunctionMonitoringData> createConfig(File objdumpFile, Collection<Component> components,
			File resultFile, Collection<String> locationPrefixesToExclude, List<String> additionalProgramPointLocations, SourceLinesMapper sourceLinesMapper ) throws IOException {
		
		this.sourceLinesMapper = sourceLinesMapper;
		
		{
			String value = System.getProperty(BCT_MONITORING_LINES_TO_MONITOR);
			if ( value != null ){
				if ( additionalProgramPointLocations == null ){
					additionalProgramPointLocations = new ArrayList<String>();
				}
				
				String[] lines = value.split(",");
				for( String line : lines ){
					additionalProgramPointLocations.add(line);
				}
			}
		}
		
		if ( CppGdbLogger.isFine() ){
			CppGdbLogger.INSTANCE.fine("Creating RegressionConfiguration using "+configuration.toString()+" " +
							"\nobjdumpFile: "+objdumpFile+"" +
							"\ncomponents: "+components+
							"\nresultFile: "+resultFile+
							"\nlocationPrefixesToExclude (src folders): "+locationPrefixesToExclude+
							"\nadditionalProgramLocations: "+additionalProgramPointLocations);
		}
		
		if ( configuration.getStaticFunctionsFinder() != null ){
			StaticFunctionsFinderFactory.setInstance(configuration.getStaticFunctionsFinder());
		}
		
		CoverageParser p = new CoverageParser();
		if ( configuration.callgrindCoverageFiles.size() > 0 ){
			coveredFunctions = new HashSet<String>();
			for ( File file : configuration.callgrindCoverageFiles ){
				coveredFunctions.addAll( p.getCoveredFunctions( file ) );
			}
		}
		
		{
			String foldersS = System.getProperty(BCT_FOLDERS_TO_FILTER_OUT);
			if ( foldersS != null ){
				configuration.foldersToFilterOut = new ArrayList<String>();
				String[] folders = foldersS.split(";");
				for ( String folder : folders ){
					configuration.foldersToFilterOut.add(folder);
				}
			}
		}
		
		{ 
			String traceRerurnLines = System.getProperty(BCT_TRACE_RETURN_LINES);
			if ( traceRerurnLines != null ){
				configuration.setTraceReturnLine(Boolean.parseBoolean(traceRerurnLines));
			}
		}

//		setupSourceLinesMapper(fileChanges);
		
		ReturnTypesRegistry.clear();

		sourceFolders = getCleanPaths( locationPrefixesToExclude );

		RegressionConfigObjDumpListener listener = extractFunctionsData(objdumpFile, locationPrefixesToExclude );


		BufferedWriter w = new BufferedWriter(new FileWriter(resultFile));

		writeHeader(w);

		FunctionMonitoringDataTransformer t;

		if ( configuration.isUseDemangledNames()  ){
			t = new FunctionMonitoringDataTransformerDemangled();
		} else {
			t = new FunctionMonitoringDataTransformerMangled();
		}

		
		Set<FunctionMonitoringData> monitoredFunctions = generateConfigForComponents(
				components, listener, w, t, listener.getLinesLinkedInStaticInitializer() );

		if ( additionalProgramPointLocations != null ){
			
			HashSet<FunctionMonitoringData> additionallyMonitoredFunctions = generateConfigForAdditionalProgramPointLocations( w, additionalProgramPointLocations, listener.getFunctionsData() );
			monitoredFunctions.addAll(additionallyMonitoredFunctions);
		}

		
		manageCriticalLines(w,listener, listener.getLinesLinkedInStaticInitializer(), listener.getStaticInitializerEnterAddress(), listener.getStaticinitializerEnterAddress());
		
		writeFooter(w);

		w.close();

		//generatePINprobes=true;
		if ( generatePINprobes == true ){
			
			String probesFolder = resultFile.getName()+".probes";
			File probesFolderFile = new File( resultFile.getParentFile(), probesFolder );
			probesFolderFile.mkdir();
			
			System.out.println("!!!PIN probes written to: "+probesFolderFile.getAbsolutePath());
			
			generatePINprobe( probesFolderFile, monitoredFunctions );
		}
		
		return monitoredFunctions;
	}



	private void generatePINprobe(File probesFolder,
			Set<FunctionMonitoringData> monitoredFunctions) {
		// TODO Auto-generated method stub
		PINProbeGenerator generator;
		if ( bufferedPin ) {
			generator = new PINBufferedProbeGenerator();
		} else {
		generator = new PINProbeGenerator();
		}
		
		try {
			generator.writePinProbe(probesFolder, "bdciProbe", monitoredFunctions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<String>  getCleanPaths(Collection<String> locationPrefixesToExclude) {
		List<String> res = new ArrayList<String>();
		for ( String location : locationPrefixesToExclude ){
			File f = new File ( location );
			try {
				location = f.getCanonicalPath();
			} catch (IOException e) {
			}
			res.add(location);
		}
		return res;
			
	}

//	private void setupSourceLinesMapper(List<FileChangeInfo> fileChanges) {
//		if ( fileChanges != null ){
//			if ( configuration.isMonitorOnlyNotModifiedLines() ){
//				try {
//					sourceLinesMapper = new SourceLinesMapper( fileChanges );
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	private HashSet<FunctionMonitoringData> generateConfigForAdditionalProgramPointLocations(
			BufferedWriter w, List<String> additionalProgramPointLocations, Map<String, FunctionMonitoringData> functionsMap) throws IOException {

		HashSet<FunctionMonitoringData> monitoredFunctions = new HashSet<FunctionMonitoringData>();
		
		for( String pp : additionalProgramPointLocations ){
			int id = newBreakpointId();

			FunctionMonitoringData function = writeProgramPointMonitoring(w, pp, id, functionsMap.values());
			
			if ( function != null ){
				monitoredFunctions.add(function);
			}
		}
		
		return monitoredFunctions;
	}

	private FunctionMonitoringData writeProgramPointMonitoring(BufferedWriter w, String pp, int id, Collection<FunctionMonitoringData> functionsData)
			throws IOException {
		
		int lineSep = pp.indexOf(':');
		
		File location = null;
		
		String file = pp.substring(0, lineSep);
		
		int lineNo = -1;
		if ( lineSep > 0 ){
			location = retrieveFileForSourceLocation( file );
			lineNo=Integer.valueOf(pp.substring(lineSep+1));
		}
		
		FunctionMonitoringData function = null;
		for( FunctionMonitoringData f : functionsData ){
			if ( f.containsLine(lineNo) ){
				function = f;
			}
		}
		
		List<String> additionalGdbInstructions = null;
		if ( function != null ){
			additionalGdbInstructions = getCommandsToTraceAdditionalReturns( function, new LineData( file, lineNo) );
		}
		
		String ppName = function.getMangledName()+":"+lineNo;
		writeFunctionMonitoringInternal(w, function, ppName, "line", false, pp, null, "", id, "POINT", location, lineNo, true, additionalGdbInstructions );
		
		writeBreakpointClosing(w);
		
		return function;
	}

	private File retrieveFileForSourceLocation(String file) {
		if ( file.startsWith("/") ){
			return new File( file );
		}

		for ( String source : sourceFolders ){
			File locationFile = new File( source+"/"+file );
			if ( locationFile.exists() ){
				return locationFile;
			}
		}

		return new File( file );

	}

	private Set<FunctionMonitoringData> generateConfigForComponents(
			Collection<Component> components,
			RegressionConfigObjDumpListener listener, BufferedWriter writer,
			FunctionMonitoringDataTransformer transformer, Set<LineData> criticalLines ) throws IOException {

		Set<FunctionMonitoringData> monitoredFunctions = new HashSet<FunctionMonitoringData>();

		System.out.println("Generating config for components: "+components);
		
		if ( components == null || components.size() == 0 ){
			return monitoredFunctions;
		}

		
		
		Set<FunctionMonitoringData> targets = getTargets(components, listener.getFunctionsData(), transformer );

		
		
		System.out.println("Targets: "+targets);
		
		//Write children, i.e. functions that are called by targets and not targets


		Set<FunctionMonitoringData> parentTargets = new HashSet<FunctionMonitoringData>();

		writer.write("#CHILDREN MONITORING CONFIG\n");
		
		for (FunctionMonitoringData target : targets) {
			
			updateEntryPoint(target);
			
			for ( FunctionMonitoringData parent : target.getCallers() ){
				if( ! canMonitorTarget(parent) ){
					continue;
				}
				updateEntryPoint(parent);
				parentTargets.add(parent);
			}
		}
		
		for (FunctionMonitoringData target : targets) {
			
			for ( FunctionMonitoringData targetCallee : target.getCallees() ){
				
				targetCallee.setCalledByTargetFunction(true);
				
				if ( targets.contains(targetCallee) ){
					continue;  //discard it cause it is a target
				}

				if ( parentTargets.contains(targetCallee) ){
					continue;  //discard it cause it is a target
				}
				
				if ( targetCallee.getAddressEnter() == null ){
					System.err.println("Missing entry address for "+targetCallee.getMangledName()+" SKIP");
					//This happens when there is a call to a function whose address has been stored in a registry
					//we may want to extend recording also to these calls

					continue;
				}
				
				
				if ( ! targetCallee.hasValidAddresses() ){
					System.err.println("Invalid address for "+targetCallee.getMangledName()+" SKIP");
					continue;
				}
				
				
				
				updateEntryPoint(targetCallee);
	
//				FIXME: add option to keep callees
//				if ( ! configuration.isMonitorFunctionsCalledByTargetFunctions() ){
//					continue;
//				}
				
				//TODO: check what happens in case two targets have the same child
				monitoredFunctions.add( targetCallee );
				writeChildDefs(writer, targetCallee, target, transformer );

			}


		}

		for ( FunctionMonitoringData parent : parentTargets ){
			parent.setCallerOfTargetFunction( true );
		}
		
		//Remove parents that are also targets
		parentTargets.removeAll(targets);

		if ( this.configuration.isMonitorCallersOfModifiedFunctions() ){
			
			writer.write("#CHILDREN OF PARENTS\n");
			
			//Monitor children of target callers 

			for (FunctionMonitoringData target : parentTargets) {
				
				if( ! canMonitorTarget(target) ){
					continue;
				}

				for ( FunctionMonitoringData targetCallee : target.getCallees() ){
					if ( targets.contains(targetCallee) || parentTargets.contains(targetCallee) ){
						continue;  //discard it cause it is a target
					}

					if ( targetCallee.getAddressEnter() == null ){
						System.err.println("Missing entry address for "+targetCallee.getMangledName()+" SKIP");
						//This happens when there is a call to a function whose address has been stored in a registry
						//we may want to extend recording also to these calls

						continue;
					}

					if ( ! targetCallee.hasValidAddresses() ){
						System.err.println("Invalid address for "+targetCallee.getMangledName()+" SKIP");
						continue;
					}
					
					targetCallee.setCalledByParentOfTargetFunction(true);
					
					monitoredFunctions.add( targetCallee );
					writeChildDefs(writer, targetCallee, target, transformer );

				}


			}
		}


		disableChildren(writer, childrenBreakpointsMap);

		writer.write("#TARGET MONITORING\n");
		
		
		
		for (FunctionMonitoringData target : targets  ) {
			
			
			if ( target.getAddressEnter() == null ){
				System.err.println("Missing entry address for "+target.getMangledName()+" SKIP");
				//This happens when there is a call to a function whose address has been stored in a registry
				//we may want to extend recording also to these calls

				continue;
			}

			if ( ! target.hasValidAddresses() ){
				System.err.println("Invalid address for "+target.getMangledName()+" SKIP");
				continue;
			}
			target.setTargetFunction( true );
			monitoredFunctions.add( target );
			writeParentDef(writer, target, transformer, criticalLines );
		}
		
		writer.write("#PARENT MONITORING\n");

		if ( this.configuration.isMonitorCallersOfModifiedFunctions() ){
			
			for (FunctionMonitoringData target : parentTargets  ) {
				
				
				
				if ( target.getAddressEnter() == null ){
					System.err.println("Missing entry address for "+target.getMangledName()+" SKIP");
					//This happens when there is a call to a function whose address has been stored in a registry
					//we may want to extend recording also to these calls

					continue;
				}

				if ( ! target.hasValidAddresses() ){
					System.err.println("Invalid address for "+target.getMangledName()+" SKIP");
					continue;
				}
				
				
				monitoredFunctions.add( target );
				writeParentDef(writer, target, transformer, criticalLines );
			}
		}
		
		handleTestCases( listener, writer );
		
		FunctionMonitoringData mainFunction = listener.getFunctionsData().get("main");
		if ( mainFunction != null ){
			monitoredFunctions.add(mainFunction);
		}
		
		return monitoredFunctions;
	}

	private HashSet<String> removeDuplicates(Set<FunctionMonitoringData> targets) {
		HashSet<String> allNames = new HashSet<>();
		HashSet<String> namesToRemove = new HashSet<>();
		
		Set<FunctionMonitoringData> toRemove = new HashSet<>();
		
		for ( FunctionMonitoringData target : targets ){
			if ( allNames.contains(target.getMangledName()) ){
				toRemove.add(target);
				namesToRemove.add(target.getMangledName());
			}
			allNames.add(target.getMangledName());
		}
		
		targets.removeAll(toRemove);
		allNames.removeAll(namesToRemove);
		
		return allNames;
	}

	private void handleTestCases(RegressionConfigObjDumpListener listener, BufferedWriter w) throws IOException {
		hasTestCases = false;
		for ( FunctionMonitoringData f : listener.getFunctionsData().values() ){
			if ( f.isCppUnitTestCase() ){
				hasTestCases = true;
				try {
					monitorCppUnitTestCase( w, f );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		addAssertionsMonitoring(w);
	}

	public void addAssertionsMonitoring(BufferedWriter w) throws IOException {
		if ( hasTestCases ){
			w.append("b Asserter.cpp:13");
			w.newLine();
			w.append("commands");
			w.newLine();
			w.append("silent");
			w.newLine();
			w.append("echo !!!BCT-TEST-FAIL\\n");
			w.newLine();
			
			writeBreakpointClosing(w);
			
			w.append("b Asserter.cpp:21");
			w.newLine();
			w.append("commands");
			w.newLine();
			w.append("silent");
			w.newLine();
			w.append("echo !!!BCT-TEST-FAIL\\n");
			w.newLine();
			
			writeBreakpointClosing(w);
			
		}
	}

	private void monitorCppUnitTestCase(BufferedWriter w, FunctionMonitoringData f) throws IOException {
		w.append("b *0x"+f.getAddressEnter());
		w.newLine();
		w.append("commands");
		w.newLine();
		w.append("silent");
		w.newLine();
		w.append("echo !!!BCT-TEST-CASE "+f.getMangledName()+"\\n");
		w.newLine();
		
		writeBreakpointClosing(w);
	}

	private Set<FunctionMonitoringData> getTargets(Collection<Component> components, Map<String, FunctionMonitoringData> functionsData, FunctionMonitoringDataTransformer t) {
		Set<FunctionMonitoringData> targets = new HashSet<FunctionMonitoringData>();

		for ( FunctionMonitoringData func : functionsData.values() ){
			if ( LOGGER.isLoggable(Level.FINE) ){
				LOGGER.fine("Processing function data for "+func);
			}
			if ( ( ! func.isImplementedWithinProject() ) && ( ! configuration.isMonitorFunctionsDefinedOutsideProject() ) ) {
				LOGGER.fine("SKIPPING function outside project "+func.getMangledName() );
				continue; //skip functions not defined within project if we do not want to monitor them
			}
			for ( Component comp : components ){
				if ( comp.acceptMethod( t.getPackageName( func ), t.getClassName(func), t.getMethodSignature(func) ) ){

					if ( canMonitorTarget( func ) ){
						targets.add(func);
					}
				}
			}
		}

		return targets;
	}

	private boolean canMonitorFunction(FunctionMonitoringData func) {
		if ( configuration.isExcludeStaticInitializers() ){
			
			if (  func.getMangledName().contains("__static_initialization_and_destruction") ){
				return false;
			}
			
		}
		
		if ( filterOutForCoverage( func ) ){
			return false;
		}
		
		if ( filterOut(func) ){
			return false;
		}
		
		return true;
		
	}
	
	private boolean filterOut(FunctionMonitoringData func) {
		if ( filterOutDisabled ){
			return false;
		}
		
		if ( configuration.functionsToFilterOut == null && configuration.foldersToFilterOut == null ){
			filterOutDisabled = true; //performance improvement
			return false;
		}
		
		if ( configuration.foldersToFilterOut != null ){
			for ( String folder : configuration.foldersToFilterOut ){
				String srcFolder = func.getSourceFolder();
				if ( srcFolder != null && srcFolder.startsWith(folder) ){
					return true;
				}
			}
		}
		
		if ( configuration.functionsToFilterOut == null ){
			return false;
		}
		System.out.println("!!!Has functions to filter out");
		return configuration.functionsToFilterOut.contains(func.getMangledName());
		
	}

	private boolean canMonitorTarget(FunctionMonitoringData func) {
		
		if ( ! canMonitorFunction(func) ){
			LOGGER.fine("Cannot monitor "+func.getMangledName());
			return false;
		}
		
		if ( func.isImplementedWithinProject() ){
			return true;
		}
		
		LOGGER.fine("Not within project "+func.getMangledName());
		
		return configuration.isMonitorFunctionsDefinedOutsideProject();
		
	}

	private boolean filterOutForCoverage(FunctionMonitoringData func) {
		if ( coveredFunctions != null ){
			if ( ! coveredFunctions.contains(func.getMangledName()) ){
				return true;
			}
		}
		return false;
	}

	void writeFooter(BufferedWriter w) throws IOException {

		w.write("echo !!!BCT-NEW-EXECUTION\\n");
		w.newLine();
		if ( configuration.isDllLine() ){
			w.write("c");
			w.newLine();
		} else {
			w.write("run");
			w.newLine();
		}
		w.write("quit");
		w.newLine();
	}

	void writeHeader(BufferedWriter w) throws IOException {
		writeGdbScriptHeader(w,configuration.getLoggingFilePath());
		
		writeAdditionalHeadings( w );
		
		if ( configuration.isDllLine() ){
			w.write("start");
			w.newLine();
		}
	}
	
	private void writeAdditionalHeadings(BufferedWriter w) throws IOException {
		
		if ( configuration.getForkListenerPid() != null ){
			configuration.setDetachOnFork(true);
			configuration.setMonitorParentOnFork(true);
		}
		
		{
			String forkMode;
			if ( configuration.isMonitorParentOnFork() ){
				forkMode = "parent";
			} else {
				forkMode = "child";
			}

			w.write(SET_FOLLOW_FORK_MODE+forkMode);
			w.newLine();
		}
		
		{
			String forkMode;
			if ( configuration.isDetachOnFork() ){
				forkMode = "on";
			} else {
				forkMode = "off";
			}

			w.write("set detach-on-fork "+forkMode);
			w.newLine();
		}
		
		
		if ( configuration.getForkListenerPid() != null ){
			w.write("define "+bctForkListenerFunctionName);
			w.newLine();
			w.write("print kill("+configuration.getForkListenerPid()+",10)");
			w.newLine();
			w.write("echo \\n");
			w.newLine();
			w.write("print sleep(2)");
			w.newLine();
			w.write("echo \\n");
			w.newLine();
			w.write("end");
			w.newLine();
			
			w.write("define "+bctAfterForkListenerFunctionName);
			w.newLine();
			w.write(SET_FOLLOW_FORK_MODE+"child"); //after fork we monitor child
			w.newLine();
			w.newLine();
			w.write("echo \\n");
			w.newLine();
			w.write("end");
			w.newLine();
		}
		

		
		
		
	}

	public static void writeGdbScriptHeader(BufferedWriter w, String loggingFilePath) throws IOException {
		// TODO Auto-generated method stub
		w.write("delete");
		w.newLine();
		w.write("set pagination off");
		w.newLine();
		w.write("set editing off");
		w.newLine();
		w.write("set confirm off");
		w.newLine();
		w.write("set verbose off");
		w.newLine();
		
		if ( loggingFilePath != null ){
			w.write("set logging redirect");
			w.newLine();
			w.write(SET_LOGGING_FILE+loggingFilePath );
			w.newLine();
		}
		
		w.write("set logging on");
		w.newLine();

		w.write("set print address");
		w.newLine();
		w.write("set print array");
		w.newLine();
		w.write("set print array-indexes");
		w.newLine();
		w.write("set print elements 20");
		w.newLine();
		w.write("set print frame-arguments none");//set to NONE because we monitor args with command "info args"
//		w.write("set print frame-arguments all");
		w.newLine();
		w.write("set print inferior-events");
		w.newLine();
		

		
		
		//set print max-symbolic-offset
		w.write("set print null-stop");
		w.newLine();
		w.write("set print object");
		w.newLine();
		//set print pascal_static-members -- Set printing of pascal static members
		w.write("set print pretty");
		w.newLine();
		w.write("set print repeats 20");
		w.newLine();
		w.write("set print sevenbit-strings");
		w.newLine();
		w.write("set print static-members off"); //BUG-FIX: this slows down ABB execution a lot
		w.newLine();
		//		set print symbol-filename");
		w.write("set print thread-events");
		w.newLine();
		w.write("set print union");
		w.newLine();
		w.write("set breakpoint pending on");
		//								w.write("set print vtbl");

		w.newLine();
		
		
		if ( useDMalloc ){
	
			w.write("set environment LD_PRELOAD=libdmalloc.so");
			w.write("sharedlibrary");

			w.write("define "+bctPrintPointersFunctionName);
			w.newLine();
			
			w.write("call malloc_valid($arg0)");
			w.write("if $$0 == 0");

			addPrintPointersLogic( w );

			w.write("end");
			w.newLine();
			w.write("end");
			w.newLine();			
		} else {
			w.write("define "+bctPrintPointersFunctionName);
			w.newLine();

			addPrintPointersLogic( w );
			
			w.write("end");
			w.newLine();
		}
		
		
	}

	private static void addPrintPointersLogic(BufferedWriter w) throws IOException {
		w.write("echo !!!BCT-VARIABLE $arg0\\n");
		w.newLine();
		w.write("output $arg0");
		w.newLine();
		w.write("echo \\n");
		w.newLine();
		
		String lowest_accessible_address = getLowestAccessibleAddress();

		w.write("if $arg0 > "+lowest_accessible_address );
		w.newLine();
		w.write("echo !!!BCT-VARIABLE *$arg0\\n");
		w.newLine();
		w.write("output *$arg0");
		w.newLine();
		w.write("echo \\n");
		w.newLine();
		w.write("end");
		w.newLine();
	}

	public static String getLowestAccessibleAddress() {
		//in some cases the value of the lowest accessible address should be 
		//retrieved by running the program with gdb, stopping the execution, 
		//and the running in another shell teh command
		
		//cat /proc/<PID>/maps 
//		00400000-00426000 r-xp 00000000 fb:02 14443872                           /home/pastore/Programs/sort/coreutils-8.18-sort-1-base/src/sort
//		00625000-00626000 r--p 00025000 fb:02 14443872                           /home/pastore/Programs/sort/coreutils-8.18-sort-1-base/src/sort
//		00626000-00627000 rw-p 00026000 fb:02 14443872                           /home/pastore/Programs/sort/coreutils-8.18-sort-1-base/src/sort
//		7ffff783b000-7ffff79b5000 r-xp 00000000 fb:01 265717                     /lib/libc-2.12.1.so
//		7ffff79b5000-7ffff7bb5000 ---p 0017a000 fb:01 265717                     /lib/libc-2.12.1.so
//		7ffff7bb5000-7ffff7bb9000 r--p 0017a000 fb:01 265717                     /lib/libc-2.12.1.so
//		7ffff7bb9000-7ffff7bba000 rw-p 0017e000 fb:01 265717                     /lib/libc-2.12.1.so
//		7ffff7bba000-7ffff7bbf000 rw-p 00000000 00:00 0 
//		7ffff7bbf000-7ffff7bd7000 r-xp 00000000 fb:01 265901                     /lib/libpthread-2.12.1.so
//		7ffff7bd7000-7ffff7dd6000 ---p 00018000 fb:01 265901                     /lib/libpthread-2.12.1.so
//		7ffff7dd6000-7ffff7dd7000 r--p 00017000 fb:01 265901                     /lib/libpthread-2.12.1.so
//		7ffff7dd7000-7ffff7dd8000 rw-p 00018000 fb:01 265901                     /lib/libpthread-2.12.1.so
//		7ffff7dd8000-7ffff7ddc000 rw-p 00000000 00:00 0 
//		7ffff7ddc000-7ffff7dfc000 r-xp 00000000 fb:01 265710                     /lib/ld-2.12.1.so
//		7ffff7fdd000-7ffff7fe0000 rw-p 00000000 00:00 0 
//		7ffff7ff9000-7ffff7ffb000 rw-p 00000000 00:00 0 
//		7ffff7ffb000-7ffff7ffc000 r-xp 00000000 00:00 0                          [vdso]
//		7ffff7ffc000-7ffff7ffd000 r--p 00020000 fb:01 265710                     /lib/ld-2.12.1.so
//		7ffff7ffd000-7ffff7ffe000 rw-p 00021000 fb:01 265710                     /lib/ld-2.12.1.so
//		7ffff7ffe000-7ffff7fff000 rw-p 00000000 00:00 0 
//		7ffffffde000-7ffffffff000 rw-p 00000000 00:00 0                          [stack]
//		ffffffffff600000-ffffffffff601000 r-xp 00000000 00:00 0                  [vsyscall]
		
		//the lowest address of the stack (7ffffffde000 in this case) shoul be returned TODO: check if pointers to the heap address stay in another memory location
		
		return "1";
	}

	private void disableChildren(BufferedWriter w,
			HashMap<FunctionMonitoringData, List<Integer>> childrenBreakpointsMap) throws IOException {
		for ( List<Integer> childrenBreakpoints : childrenBreakpointsMap.values() ){
			for (int i : childrenBreakpoints ) {
				w.append("disable " + i);
				w.newLine();
			}
		}
	}

	void writeChildDefs(BufferedWriter w, FunctionMonitoringData child, FunctionMonitoringData caller, FunctionMonitoringDataTransformer t ) throws IOException {


		logEnteringChild( child );
		
		
		
		
		if ( ( ! configuration.isMonitorLibraryCalls() ) &&
				( ! child.isImplementedWithinProject() ) ){
			return;
		}
		
		if ( ! canMonitorFunction(child) ){
			logCannotMonitorChild(child);
			return;
		}

		if ( configuration.isMonitorFunctionEnterExitPoints() ){

			List<Integer> childrenBreakPoints = childrenBreakpointsMap.get(caller);
			if ( childrenBreakPoints == null ){
				childrenBreakPoints = new ArrayList<Integer>();
				childrenBreakpointsMap.put(caller, childrenBreakPoints);
			}
			String enterAddress = child.getAddressEnter();

			
			List<Integer> childrenExitBreakpoints = writeChildrenExitDefinitions(w, child, caller, t,
					childrenBreakPoints);
			
			int id = newBreakpointId();
			writeChildDef(w, child, true, false, enterAddress, t, childrenExitBreakpoints, caller, id, false );
			childrenBreakPoints.add(id);

		}
		
		//Following code is not correct, children lines will be always monitored, even when they are not invoked by a target
		
		if ( ! configuration.isMonitorChildrenInternalLines() ){
			return;
		}

		writeLinesBreakPoints(w, child, t );

	}

	private List<Integer> writeChildrenExitDefinitions(BufferedWriter w,
			FunctionMonitoringData child, FunctionMonitoringData caller,
			FunctionMonitoringDataTransformer t,
			List<Integer> childrenBreakPoints) throws IOException {
		List<Integer> toEnable = new ArrayList<Integer>();

		if ( child.isEnterExit() ){
			return toEnable;
		}
		
		if ( child.getExitAddresses().size() > 0 ){
			for ( String exitAddress : child.getExitAddresses() ){ //usually it is just one
				int id = newBreakpointId();
//				childrenBreakPoints.add(id);
				toEnable.add( id );
				writeChildDef(w, child, false, false, exitAddress, t, null, caller, id, true );
			}
		} else {
			
			
				//This code properly manages the following case:
				//	the target function is not called, 
				//  the code following the target function is executed
				//	we should enable these breakpoint only in the enter breakpoint
				//FIXME:the return breakpoint does not work in case of recursion, all of them will be disabled at the first return
				ArrayList<Integer> newChildrebBP = new ArrayList<Integer>();
				ArrayList<String> addresses = new ArrayList<String>();
				
				for ( String exitAddress : child.getAddressesAfterCallPoints() ){ //no exit instruction foud, lets instrument the caller
					int bpId = newBreakpointId();
					
					newChildrebBP.add( bpId );
					addresses.add(exitAddress); //this way we are sure id and exit correspond
				}
				
//				newChildrebBP.addAll(childreBreakPoints); //the first N elements are exit addressses
				
				for ( int i = 0; i < addresses.size(); i++ ){
					int id = newChildrebBP.get(i);
					String exitAddress = addresses.get(i);
					
					toEnable.add( id );
//					childrenBreakPoints.add(id);
					writeChildDef(w, child, false, true, exitAddress, t, newChildrebBP, caller, id, true );
				}
				
				
			}
			
//			for ( String exitAddress : child.getAddressesAfterCallPoints() ){ //no exit instruction foud, lets instrument the caller
//
//				int id = newBreakpointId();
//				toEnable.add( id );
//				childrenBreakPoints.add(id);
//				writeChildDef(w, child, false, true, exitAddress, t, null, caller, id );
//			}
//		}
		return toEnable;
	}


	private void writeLinesBreakPoints(BufferedWriter w,
			FunctionMonitoringData child, FunctionMonitoringDataTransformer t ) throws IOException {


		

		
		LineData[] cLines = child.getLines().toArray(new LineData[0]);
		
		int start = cLines.length > 0 ? 1 : 0;
		
		if ( functionEntryPointDetector != null ){
			FunctionEntryPoint entryPoint = functionEntryPointDetector.getFunctionEntryPoint(child.getMangledName(), child.getAddressEnter() );
			if ( entryPoint != null ){
				for ( int i = 0; i < cLines.length; i++ ){
					if ( cLines[i].getLineNumber() > entryPoint.getLine() ){
						start = i;
						LOGGER.fine("New start line is "+cLines[start].getLineNumber());
						break;
					}
				}
			}
		}
		
		//Bug 197 skip first line
		//TODO: check if it is really the first
		for ( int i = start; i < cLines.length; i++ ){
			LineData lineData = cLines[i];
			
			if ( i == ( cLines.length-1) ){
				//if it is the last line check if it isn't already monitored by the return breakpoint
				if ( configuration.monitorFunctionEnterExitPoints && child.isLastLineContainsOnlyReturn() ){
					logCannotMonitorLine( lineData );
					continue;
				}
			}
			
			if ( ! canMonitorLine( lineData ) ){
				
				logCannotMonitorLine( lineData );
				
				continue;
			}
			
			logCanMonitorLine( lineData );
			
			
			
			int id = newBreakpointId();
			String line = getLineBreakpoint( lineData ); 
			
			//REMINDER: modificato child.getMangledName was t.getMethod(child)
			
			List<String> additionalGdbInstructions = getCommandsToTraceAdditionalReturns( child, lineData );
			monitoredLines.put(lineData,String.valueOf(id));
			
			writeFunctionMonitoringInternal(w, child, createLineBreakPointAddress(child, lineData), "line", false, line, t, "", id, "POINT", child.getAbsoluteFile(), child.getFirstSourceLine(), child.isStatic(), additionalGdbInstructions );
			
			
			
			
			
			writeBreakpointClosing(w);
			
			
		}
	}

	private String createLineBreakPointAddress(FunctionMonitoringData child,
			LineData lineData) {
		return child.getMangledName()+":"+lineData.getLineNumber();
	}
	

	private Set<ReturnData> processedAdditionalReturns = new HashSet<ReturnData>();
	private boolean add_exit_on_caller_side_if_needed = false;


	
	
	private List<String> getCommandsToTraceAdditionalReturns(FunctionMonitoringData child,
			LineData lineData) throws IOException {
//		return new ArrayList<String>();
		
//		ReturnData returnData = findCorrespondingAdditionaReturn ( child, lineData );
//		if ( returnData == null ){
//			return null;
//		}
//		
		List<String> result = new ArrayList<String>();
		
		if ( ! configuration.isTraceReturnLine() ){
			return result;
		}
		
		if ( ! child.returnsInLine( lineData ) ){
			return result;
		}
			
		
//		
//		processedAdditionalReturns.add( returnData );
//		
//		result.add("#RETURN INSTRUCTION: "+returnData.getJmpingAddress()+" "+returnData.getJmpingLine().getLineNumber());
//		
//		result.add("echo !!!BCT-RETURN-INSTRUCTION "+returnData.getJmpingLine().getLineNumber()+"\\n");
//		
		
		
		result.add("echo !!!BCT-RETURN-INSTRUCTION "+lineData.getAddress()+" "+lineData.getFileLocation()+":"+lineData.getLineNumber()+"\\n");
		return result;
	}

	private ReturnData findCorrespondingAdditionaReturn(FunctionMonitoringData child, LineData lineData) {

		for ( ReturnData returnData : child.getAdditionalReturns() ){
			if ( returnData.getJmpingLine() == lineData ){
				return returnData;
			}
		}
		return null;
	}

	private String getLineBreakpoint(LineData lineData) {
		if ( EnvUtil.isWindows() || ( lineData.isDuplicated() && lineData.hasAddress() ) ){ //BUG-FIX: a same line may appear in multiple function bodies because of g++ debugging info
			return "*0x"+lineData.getAddress();
		}
		return lineData.getFileLocation()+":"+lineData.getLineNumber();
	}

	private boolean canMonitorLine(LineData lineData) {
		if ( ! configuration.isMonitorOnlyNotModifiedLines() ){
			CppGdbLogger.fine("Accpecting because MONITORING ALL");
			return true;
		}
		
		Map<String, List<Integer>> sourceLinesMap;
		if ( configuration.isWorkingOnOriginalSoftware() ){
			sourceLinesMap = sourceLinesMapper.getOriginalSourcesLines();
		} else {
			sourceLinesMap = sourceLinesMapper.getModifiedSourcesLines();
		}
		
		String fileLocation = lineData.getFileLocation();
		
		File sourceFile = new File( fileLocation );
		
		try {
			String absolutePath = sourceFile.getCanonicalPath();
			for ( String sourceLocation : sourceFolders ){

				if ( ! absolutePath.startsWith(sourceLocation) ){
					CppGdbLogger.INSTANCE.fine("NOT IN SOUCES");
					continue;
				}

				String sourceFileLocation = absolutePath.substring(sourceLocation.length());

				List<Integer> linesToMonitor = sourceLinesMap.get(sourceFileLocation);
				
				if ( linesToMonitor == null ){
					linesToMonitor = sourceLinesMap.get("/"+sourceFileLocation);
				}
				
				if ( linesToMonitor == null ){
					linesToMonitor = sourceLinesMap.get("\\"+sourceFileLocation);
				}
				
				if ( linesToMonitor == null ){
					if ( sourceFileLocation.startsWith("\\") || sourceFileLocation.startsWith("/") )
					linesToMonitor = sourceLinesMap.get(sourceFileLocation.substring(1));
				}
				
				if ( linesToMonitor == null ){
					
					logNoLineToMonitor(sourceLinesMap,sourceFileLocation);
					return false;
				}

				if ( linesToMonitor.contains(lineData.getLineNumber() ) ) {
					return true;
				}
			}

		} catch ( IOException e ){
			e.printStackTrace();
		}
		return false;
	}



	private void manageCriticalLines(BufferedWriter w, RegressionConfigObjDumpListener listener, Collection<LineData> collection, String staticInitializerEnterAddress, String staticInitializerExitAddress) throws IOException{
		if ( collection == null ){
			return;
		}
		
		if ( collection.size() == 0 ){
			return;
		}
		
		if ( configuration.isDllLine() ){
			//management of critical breakpoints creates problems with Dlls because
			//the breakpoint is set at a specific address
			return;
		}
		
		List<String> toEnable = new ArrayList<String>();
		
		w.write("#DISABLING MONITORING OF LINES REFERENCED IN STATIC INITIALIZER");
		w.newLine();
		
		for ( LineData criticalLine : collection ){
			String breakpointId = monitoredLines.get(criticalLine);
			
			if ( breakpointId == null ){
				continue;
			}
			
			toEnable.add(breakpointId);
				
			
		}
		
		for ( Integer bp : criticalExitBreakpoints ){
			toEnable.add(bp.toString());
		}
		
		FunctionMonitoringData initFunction = listener.getFunctionsData().get("_init");
		
		if (  initFunction == null ){
			System.out.println("CANNOT FIND _init FUNCTION!!!");
			return;
		}
		
		w.write("b *0x"+initFunction.getAddressEnter());
		w.newLine();
		w.write("commands");
		w.newLine();
		for ( String bp : toEnable ){
			w.write("disable "+bp);
			w.newLine();
		}
		
		
		
		w.write("c");
		w.newLine();
		w.write("end");
		w.newLine();
		
		
		
		//
		for ( String exit : initFunction.getExitAddresses() ){
			w.write("b *0x"+exit);
			w.newLine();
			w.write("commands");
			w.newLine();
			for ( String bp : toEnable ){
				w.write("enable "+bp);
				w.newLine();
			}

			w.write("c");
			w.newLine();
			w.write("end");
			w.newLine();
		}
		
	}


	private int newBreakpointId() {
		// TODO Auto-generated method stub
		return ++breakpoints;
	}

	void writeChildDef(BufferedWriter w, FunctionMonitoringData def, boolean isEnter, boolean isCallerSide, String address, FunctionMonitoringDataTransformer t, List<Integer> childrenExitBreakpoints, 
			FunctionMonitoringData caller, int id, boolean disabled )
			throws IOException {

		writeFunctionMonitoring(w, def, "child", isEnter, isCallerSide, address, t, caller.getMangledName(), id );

		
		//		if ( toDisable != null ){
		//			
		//				w.append("set variable $breakpoint_"+def.getMangledName()+" = $breakpoint_"+def.getMangledName()+"+1");
		//				w.newLine();
		//			
		//		}
		
		if ( childrenExitBreakpoints != null ){
			for ( Integer idToDisable : childrenExitBreakpoints ){
				if ( isEnter ){
					w.append("enable " + idToDisable);
				} else {
					w.append("disable " + idToDisable);
				}
				w.newLine();
			}
		}
		
		if ( disabled ){ 
			//The breakpoint disables itself after execution
			//if it did not already disable itself
			if ( childrenExitBreakpoints == null || ! childrenExitBreakpoints.contains(id) ){
				w.append("disable " + id);
				w.newLine();
			}
		}
		
		writeBreakpointClosing(w);
		
		if ( disabled ){
			//at the beginning is disabled
			w.append("disable " + id);
			w.newLine();
		}

	}

	private void writeParentDef(BufferedWriter w, FunctionMonitoringData def, FunctionMonitoringDataTransformer t, Set<LineData> criticalLines) throws IOException {
		System.out.println("writeParentDef "+def.getDemangledName()+" ");
		List<Integer> childreBreakPoints = childrenBreakpointsMap.get(def);

		if ( childreBreakPoints == null ){
			childreBreakPoints = new ArrayList<Integer>();
		}
		
		TreeSet treeSet = def.getLines();
		LineData lineData = (LineData) treeSet.last();
		
		boolean isCritical = false;
		if ( criticalLines.contains(lineData) ){
			isCritical = true;
		}
		
		if ( configuration.isMonitorFunctionEnterExitPoints() ){

			
			if ( add_exit_on_caller_side_if_needed && def.getExitAddresses().size() == 0 ){
				//This code properly manages the following case:
				//	the target function is not called, 
				//  the code following the target function is executed
				//	we should enable these breakpoint only in the enter breakpoint
				//FIXME:the return breakpoint does not work in case of recursion, all of them will be disabled at the first return
				ArrayList<Integer> newChildrebBP = new ArrayList<Integer>();
				ArrayList<String> addresses = new ArrayList<String>();
				
				for ( String exitAddress : def.getAddressesAfterCallPoints() ){ //no exit instruction foud, lets instrument the caller
					int bpId = newBreakpointId();
					
					newChildrebBP.add( bpId );
					addresses.add(exitAddress); //this way we are sure id and exit correspond
				}
				
				newChildrebBP.addAll(childreBreakPoints); //the first N elements are exit addressses
				
				for ( int i = 0; i < addresses.size(); i++ ){
					int bpId = newChildrebBP.get(i);
					String exitAddress = addresses.get(i);
					writeParentDef(w, def, false, exitAddress, childreBreakPoints, t, bpId );
				}
				
				
				childreBreakPoints = newChildrebBP;
			}
			
			writeParentDef(w, def, true, def.getAddressEnter(), childreBreakPoints, t, -1 );

			if ( def.getExitAddresses().size() > 0 && ! def.isEnterExit() ){
				for ( String exitPoint : def.getExitAddresses() ){
					int bpId = writeParentDef(w, def, false, exitPoint, childreBreakPoints, t, -1 );
					if ( isCritical ){
						criticalExitBreakpoints .add(bpId);
					}
				}
			} else {
				//HANDLED BEFORE
			}
		}

		if ( configuration.isMonitorInternalLines() ){
			writeLinesBreakPoints(w, def, t );
		}
		
		processAdditionalReturns( w, def, true, "" );
	}

	private void processAdditionalReturns(BufferedWriter w, FunctionMonitoringData def, boolean isCallerSide, String callerInfo) throws IOException {
		if ( ! configuration.isTraceReturnLine() ){
			return;
		}
		for ( ReturnData returnData : def.getAdditionalReturns() ){
			processAdditionalReturn( w, def, returnData, isCallerSide, callerInfo );
		}
	}

	private void processAdditionalReturn(BufferedWriter w, FunctionMonitoringData def, ReturnData returnData, boolean isCallerSide, String callerInfo) throws IOException {
		LineData jumpingLine = returnData.getJmpingLine();
		
		String bpLocation = createLineBreakPointAddress(def, jumpingLine );
		String address = getLineBreakpoint(jumpingLine);
		
		int id = newBreakpointId();
				
		writeBreakpointHeading(w, bpLocation, "RETURN", isCallerSide, address, callerInfo, id, "RETURN", true);
		
		writeAdditionalReturnsMonitoring(w, def, jumpingLine);
		
		writeBreakpointClosing(w);
		
		
	}

	private void writeAdditionalReturnsMonitoring(BufferedWriter w,
			FunctionMonitoringData def, LineData jumpingLine) throws IOException {
		List<String> additional = getCommandsToTraceAdditionalReturns(def, jumpingLine);
		
		if ( additional == null ){
			return;
		}
		
		for ( String line : additional ){
			w.append(line);
			w.newLine();
		}
		
	}

	private int writeParentDef(BufferedWriter w, FunctionMonitoringData def, boolean isEnter, String address, List<Integer> childrenBreakPoints, FunctionMonitoringDataTransformer t, int suggestedId ) throws IOException {
		int id;
		
		if ( suggestedId == -1 ){
			id = newBreakpointId();
		} else {
			id = suggestedId;
		}
		
		System.out.println("writeParentDef "+def.getDemangledName()+" breakpoint id: "+id);
		
		writeFunctionMonitoring(w, def, "parent", isEnter, false, address, t, "", id );

		if ( isEnter ) {
			for (int i : childrenBreakPoints) {
				w.append("enable " + i);
				w.newLine();
			}
		} else {
			for (int i : childrenBreakPoints) {
				w.append("disable " + i);
				w.newLine();
			}
		}
		
		
		

		writeBreakpointClosing(w);
		
		return id;
	}

	private void writeBreakpointClosing(BufferedWriter w) throws IOException {
		w.append("c");
		w.newLine();
		w.append("end");
		w.newLine();
	}

	private void writeFunctionMonitoring(BufferedWriter w,
			FunctionMonitoringData def, String type, boolean isEnter, boolean isCallerSide, String address, FunctionMonitoringDataTransformer t, String callerInfo, int id ) throws IOException {
		
		String pos = isEnter ? "ENTER" : "EXIT";

		
		
		
		
		if ( isEnter ){
			FunctionEntryPoint entryPoint = null;
			address = updateEntryPoint(def);
		}
		
		String method = def.getMangledName();
		String bpLocation = "*0x"+address;
		
		boolean isEnterExit = def.isEnterExit();
		if ( isEnterExit ){
			pos = "ENTEX";
		}
		
		System.out.println("writeFunctionMonitoring "+def.getDemangledName()+" "+pos);
		
		if ( configuration.isDllLine() ){//CANNOT USE EntryPOintDetector because libraries are not loaded!!!
			LineData line;
			if ( isEnter ){
				//TODO: use entryPoint
				line = def.getFirstSourceLineData();
			} else {
				line = def.getLastSourceLineData();
			}
			
			bpLocation = getLineBreakpoint(line);
		}
		
		writeFunctionMonitoringInternal(w, def, method, type, isCallerSide, bpLocation, t, callerInfo, id, pos, def.getAbsoluteFile(), def.getFirstSourceLine(), def.isStatic(), null );

		if ( ! isEnter || isEnterExit ) {

			w.append("echo !!!BCT-registers\\n");
			w.newLine();
//			w.append("info all-registers");
			
			if ( isFloat(def.getReturnType()) ){
				writeFloatReturnInspectionCommand(w);
			} else if ( isDouble(def.getReturnType()) ){
				if ( EnvUtil.is64Arch() ){
					writeFloatReturnInspectionCommand(w);
				} else {
					writeDoubleReturnInspectionCommand(w, def);
				}
			} else {


				w.append("info register eax");
				w.newLine();
			}
//			w.append("echo \\n");
//			w.newLine();
			
			w.append("echo !!!BCT-registers-end\\n");
			w.newLine();
		}

	}

	public String updateEntryPoint(FunctionMonitoringData def) {
		
		
		String address = def.getAddressEnter();
		if ( functionEntryPointDetector != null ){
			System.out.println("Using functionEntryPointDetector");
			String method = def.getMangledName();
			
			if ( isSystemMethod( method ) ){
				LOGGER.fine("Skipping entry point detection because system method "+method);
			} else {

				LOGGER.fine("Using functionEntryPointDetector");
				FunctionEntryPoint entryPoint = functionEntryPointDetector.getFunctionEntryPoint(method, def.getAddressEnter());
				if ( entryPoint != null ){

					address = entryPoint.getAddress();
					if ( address.startsWith("0x") ){
						address = address.substring(2);
					}

					def.setEnterAddress(address);
				}
			}
		}
		return address;
	}

	private boolean isSystemMethod(String method) {
		return method.contains("@");
	}

	public void writeDoubleReturnInspectionCommand(BufferedWriter w,
			FunctionMonitoringData def) throws IOException {
		w.append("echo !!!BCT-DOUBLE-RETURN\\n");
		w.newLine();
		String raddress = def.getLastFldlArg();
		if ( raddress == null ){
			LOGGER.warning("NULL RETURN ADDRESS for FUNCTION "+def.getMangledName());
		} else if ( raddress.matches("-??0x[1-9]+(\\(%[a-z][a-z][a-z]\\))") ){
			int parStart = raddress.indexOf('(') ;
			int registerStart = parStart + 2;
			int registerEnd = raddress.length()-1;
			String memoryAddress = raddress.substring(0, parStart );
			String register = raddress.substring(registerStart, registerEnd);
			String sign = memoryAddress.startsWith("-") ? "" : "+";
			String toInspect = "$"+register + sign + memoryAddress;

			w.append("x/2xw "+toInspect);
			w.newLine();
		}
	}

	public void writeFloatReturnInspectionCommand(BufferedWriter w)
			throws IOException {
		w.append("echo !!!BCT-FLOAT-RETURN\\n");
		w.newLine();
		w.append("info register eax");
		w.newLine();
	}

	private boolean isDouble(String varType) {
		if ( varType == null ){
			return false;
		}
		return varType.equals("double");
	}

	private boolean isFloat(String varType) {
		if ( varType == null ){
			return false;
		}
		return varType.equals("float");
	}

	private void writeFunctionMonitoringInternal(BufferedWriter w,
			FunctionMonitoringData functionData, String position, String type, boolean isCallerSide,
			String address, FunctionMonitoringDataTransformer t,
			String callerInfo, int id, String programPointType, File location, int line, boolean isStatic,
			List<String> additionalGdbInstructions) throws IOException {

		boolean lineBreakPoint = false;
		int lineNo = -1;
		
		if ( position.contains(":") ){
			lineBreakPoint = true;
			
			int linePos = position.indexOf(':');
			String lineNoString = position.substring(linePos+1);
			lineNo = Integer.valueOf(lineNoString);
		}
		
		boolean isExit = programPointType.equals("EXIT");
		boolean isEnter = ! isExit;
		
		writeBreakpointHeading(w, position, type, isCallerSide, address,
				callerInfo, id, programPointType, lineBreakPoint);
		
		w.append("echo !!!BCT-" + programPointType + ": " + position + "\\n");
		w.newLine();
		
		if ( additionalGdbInstructions != null ){
			for ( String additionalInstruction : additionalGdbInstructions ){
				w.append(additionalInstruction);
				w.newLine();
			}
		}
		
		w.append("echo !!!BCT-stack\\n");
		w.newLine();
		w.append("bt");
		w.newLine();
		w.append("echo !!!BCT-stack-end\\n");
		w.newLine();
		
		//CODE for previous BUG-FIX, no longer needed
		//if ( configuration.isDllLine() || ( ! isExit ) ) { 
			//in case of exit breakpoint set at a line position we can safely record locals
			//in the general case we cannot, because the ret instruction follows the leave instruction, which cleans the stack (it seems at least)
		//BUG-FIX end
		
		if ( isEnter || ! EnvUtil.isWindows() ){//necessary for fixing BUG under WINDOWS.
			//on WINDOWS both the last line of the function and the ret instruction point to cleaned out function arguments
			//however the first instruction points to valid arguments
			w.append("echo !!!BCT-args\\n");
			w.newLine();
			w.append("info args");
			w.newLine();
			
			w.append("echo !!!BCT-args-end\\n");
			w.newLine();
		}

		File funcFile = functionData.getSourceFile();
		boolean doFiltering = VariablesFilter.INSTANCE.varsToIgnoreInFile(funcFile);
		
			if ( this.configuration.monitorLocalVariables ){ //also the object *this is no longer the same
				w.append("echo !!!BCT-locals\\n");
				w.newLine();

				if ( lineBreakPoint || isExit ){ //we exclude the enter case because all the variables won't be initialized
					if( ! ( isExit && EnvUtil.isWindows() ) ){ //we exclude WINDOWS for BUG on local values 
						w.append("info locals");
						w.newLine();
					}
					
					
					if ( configuration.inspectLocalsOneByOne ) {
						for ( LocalVariableDeclaration var : functionData.getLocalVariableDeclarations() ){
							if ( lineBreakPoint ) {
								if ( var.inScopeForMonitoring( lineNo )  
										&& lineNo < functionData.getLastSourceLine() 
										){
									//if ( functionData.getAbsoluteFile().getName().equals("sha1_name.c") && var.getName().equals("str") )
									//	System.out.println("!!!!AddingVariable "+var.getName()+" "+functionData.getMangledName());
									if ( doFiltering ){
										if ( ! VariablesFilter.INSTANCE.ignore(funcFile, var.getName() ) ){
											addPrintVariableInstructions( w, var.getName() );
										}
									} else {
										addPrintVariableInstructions( w, var.getName() );
									}
								}
							} else if ( isExit ) { //all the variables are declared at exit
								//Do nothing, locals are no longer available at this point
							} else {
								//Do nothing, no local variable is declared at enter
							}
						}
					}
				}
				
				if ( functionData != null ){
					w.append("#"+functionData.getMangledName());
					w.newLine();	
					
					for ( String arg : functionData.getPointerArgs() ){
						
						if ( doFiltering ){
							if ( ! VariablesFilter.INSTANCE.ignore(funcFile, arg ) ){
								addPrintPointerInstructions(w,arg);	
							}
						} else {
							addPrintPointerInstructions(w,arg);	
						}
					}
					for ( String arg : functionData.getReferenceArgs() ){

						
						if ( doFiltering ){
							if ( ! VariablesFilter.INSTANCE.ignore(funcFile, arg ) ){
								addPrintPointerInstructions(w,arg);	
							}
						} else {
							addPrintPointerInstructions(w,arg);	
						}
					}
					for ( String arg : functionData.getScalarArgs() ){
						if ( doFiltering ){
							if ( ! VariablesFilter.INSTANCE.ignore(funcFile, arg ) ){
								addPrintVariableInstructions( w, arg );
							}
						} else {
							addPrintVariableInstructions( w, arg );
						}
					}
				}

				if (! isStatic ){
					if ( configuration.monitorPointerToThis ){
						System.out.println("PRINTPOINTERS ");
						addPrintPointerInstructions(w,"this");
					}
				}

				w.append("echo !!!BCT-locals-end\\n");
				w.newLine();
				w.append("echo !!!BCT-globals\\n");
				w.newLine();
				if ( additionalCommandsCreator != null && location != null ){
					//System.out.println("additionalCommandsCreator creating FOR "+position+" ");
					LOGGER.info("additionalCommandsCreator creating FOR "+position+" ");
					w.append( additionalCommandsCreator.createAdditionalMonitoringCommands(location, line) );
				} else {
					System.out.println("additionalCommandsCreator SKIPPING FOR "+position+" "+additionalCommandsCreator+" "+location);
				}
				w.append("echo !!!BCT-globals-end\\n");
				w.newLine();
			}
		//}


		if ( configuration.isRecordCallingContextData() ){
			w.append("frame 1");
			w.newLine();
			w.append("echo !!!BCT-caller-locals\\n");
			w.newLine();
			w.append("info locals");
			w.newLine();
			w.append("echo !!!BCT-caller-locals-end\\n");
			w.newLine();
			w.append("frame 0");
			w.newLine();
		}
	}

	private void addPrintVariableInstructions(BufferedWriter w, String varName) throws IOException {
		w.append("echo !!!BCT-VARIABLE "+varName+"\\n");
		w.newLine();
		w.append("output "+varName);
		w.newLine();
		w.append("echo \\n");
		w.newLine();
	}

	public void addPrintPointerInstructions(BufferedWriter w,String varName)
			throws IOException {
		w.append("bct_print_pointers "+varName);
		w.append("\n");
		w.append("echo \\n");
		w.append("\n");
	}

	private void writeBreakpointHeading(BufferedWriter w, String position,
			String type, boolean isCallerSide, String address,
			String callerInfo, int id, String programPointType,
			boolean lineBreakPoint) throws IOException {
		w.append("#breakpoint "+id);
		w.newLine();
		w.append("# code for " + type + " " + programPointType + " "
				+ position);
		w.newLine();
		if ( isCallerSide ){
			w.append("# executed on caller "+callerInfo);
			w.newLine();
		}

		if ( configuration.isDllAddress() && ! lineBreakPoint ){
			w.append("print " + position);
			w.newLine();
			w.append("b 0x$1");
		} else {
			w.append("b " + address);
		}
		w.newLine();
		w.append("commands");
		w.newLine();
		w.append("silent");
		w.newLine();
	}




	//	static List<FunctionMonitoringData> extractFunctionsDefinitions(
	//			File parentFile) throws IOException {
	//
	//		List<FunctionMonitoringData> result = new ArrayList<FunctionMonitoringData>();
	//
	//		BufferedReader reader = new BufferedReader(new FileReader(parentFile));
	//
	//		String line;
	//		try {
	//			while ((line = reader.readLine()) != null) {
	//
	//				int mangledStart = line.indexOf(' ') + 1;
	//				int addressStart = line.indexOf(' ', mangledStart) + 1;
	//				int separatorStart = line.indexOf(' ', addressStart) + 1;
	//				int demangledStart = line.indexOf(' ', separatorStart) + 1;
	//
	//				String entryType = line.substring(0, mangledStart - 1);
	//
	//				boolean isEnter;
	//				if (entryType.equals("ENTER")) {
	//					isEnter = true;
	//				} else {
	//					isEnter = false;
	//				}
	//
	//				String address = line.substring(addressStart,
	//						separatorStart - 1);
	//				String mangledSignature = line.substring(mangledStart,
	//						addressStart - 1);
	//				String demangledSignature = line.substring(demangledStart);
	//
	//				FunctionMonitoringData funcDef = new FunctionMonitoringData(
	//						mangledSignature, demangledSignature, address, isEnter);
	//
	//				result.add(funcDef);
	//
	//			}
	//		} finally {
	//			if (reader != null) {
	//				reader.close();
	//			}
	//		}
	//
	//		return result;
	//	}

	
	private static void logEnteringChild(FunctionMonitoringData child) {
		if ( CppGdbLogger.isFine() ){
			CppGdbLogger.INSTANCE.entering(GdbRegressionConfigCreator.class.getCanonicalName(), "writeChildDefs");
			CppGdbLogger.fine("Working on child: " +child.getMangledName() );
		}
	}

	private static void logCannotMonitorChild(FunctionMonitoringData child) {
		if ( CppGdbLogger.isFine() ){
			CppGdbLogger.fine("Cannot monitor "+child.getMangledName());
		}
	}
	
	private static void logCanMonitorLine(LineData lineData) {
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("Can monitor "+lineData.getFileLocation()+" "+lineData.getLineNumber());
		}
	}

	private static void logCannotMonitorLine(LineData lineData) {
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("Cannot monitor "+lineData.getFileLocation()+" "+lineData.getLineNumber());
		}
	}
	
	private static void logNoLineToMonitor(Map<String, List<Integer>> sourceLinesMap, String sourceFileLocation) {
		if ( LOGGER.isLoggable(Level.FINE) ){
			LOGGER.fine("NO LINE TO MONITOR FOR FILE "+sourceFileLocation);
			String inMap = "";
			for ( String file : sourceLinesMap.keySet() ){
				inMap += " "+file;
			}
			LOGGER.fine("FILES IN MAP: "+inMap);
		}
	}
}
