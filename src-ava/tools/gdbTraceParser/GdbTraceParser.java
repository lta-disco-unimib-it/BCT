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

import it.unimib.disco.lta.ava.ccrt.CcrtAvaLauncher;
import it.unimib.disco.lta.ava.ccrt.CcrtAvaLauncherOptions;
import it.unimib.disco.lta.ava.ccrt.CcrtOutputFiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



import modelsFetchers.ModelsFetcherFactoy;

import recorders.FileDataRecorder;
import recorders.FileExecutionsRepository;
import recorders.SessionsRegistry;
import tools.InvariantGenerator;
import traceReaders.normalized.traceCreation.MethodOutgoingTraceMaintainer;
import traceReaders.raw.FileInteractionTrace;
import traceReaders.raw.FileInteractionTraceRepository;
import traceReaders.raw.FileReaderException;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.TraceException;
import util.FileIndex.FileIndexException;
import util.FileUtil;
import util.RuntimeContextualDataUtil;
import util.componentsDeclaration.Component;
import util.componentsDeclaration.ComponentDefinitionImporter;
import util.componentsDeclaration.ComponentsDefinitionException;
import util.componentsDeclaration.CppDemangledSignatureParser;
import util.componentsDeclaration.CppMangledSignatureParser;
import util.componentsDeclaration.SignatureParser;
import check.Checker;
import check.SimplifyIoChecker;
import conf.EnvironmentalSetter;
import conf.InvariantGeneratorSettings;
import conf.management.ConfigurationFilesManager;
import conf.management.ConfigurationFilesManagerException;
import cpp.gdb.FileChangeInfo;
import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.FunctionMonitoringDataSerializer;
import cpp.gdb.GdbRegressionConfigCreator;
import cpp.gdb.ModifiedFunctionsDetector;
import cpp.gdb.RegressionConfigObjDumpListener;
import cpp.gdb.SourceLinesMapper;

public class GdbTraceParser {
	///PROPERTIES-DESCRIPTION: Options that control the parsing of BCT/RADAR traces
	
	///indicate to skip ionference of FSA
	public static final String BCT_NO_FSA_MODELS = "bct.inference.skipFSA";

	///indicate whether or not to dump checking traces
	public static final String BCT_SKIP_DUMPING_CHECKING = "bct.skipDumpingCheckingTrace";
	
	///indicate whether or not to compare pointers
	public static final String BCT_COMPARE_POINTERS = "bct.comparePointers";

	private static Logger LOGGER = Logger.getLogger(GdbTraceParser.class.getCanonicalName());

	private static boolean checkWithDaikon;


	private static final String NEW_THREAD_STRING = "[New Thread ";

	///if true use Daikon serialized models to execute the checking, otherwise use the tetxual data properties
	public static final String BCT_CHECK_WITH_DAIKON = "bct.checkWithDaikon";

	///if true deletes folder rawGdbThreadTraces
	public static final String BCT_CLEANUP_TRACES = "bct.cleanupGdbTraces";

	public static class Options {
		public static final String CHECK = "-check";
		public static final String ENABLE_AVA = "-enableAva";
		public static final String FILTER_ALL_NON_TERMINATING_FUNCTIONS = "-filterAllNonTerminatingFunctions";
		public static final String FILTER_NOT_TERMINATED_FUNCTIONS = "-filterNotTerminatedFunctionCalls";
		public static final String RESULTS_DIR = "-resultsDir";
		public static final String SIMULATE_CLOSING_OF_LAST_NOT_TERMINATED_FUNCTIONS = "-simulateClosingOfLastNotTerminatedFunctions";
		public static final String SIMULATE_CLOSING_OF_NOT_TERMINATED_FUNCTIONS = "-simulateClosingOfNotTerminatedFunctions";
		public static final String SKIP_CLEANING = "-skipCleaning";
		public static final String SKIP_CONFIG_CREATION = "-skipConfigCreation";
		public static final String DONT_TRACE_SESSIONS = "-dontTraceSessions";
		public static final String GENERATE_SINGLE_LINE_INVARIANTS = "-generateSingleLineInvariants";
		public static final String GENERATE_FUNCTION_INVARIANTS = "-generateFunctionInvariants";
		public static final String TRACE_PARENTS_VARIABLES = "-traceParentVariables";
		public static final String USE_DEMANGLED_NAMES = "-useDemangledNames";
		public static final String ANALIZED_EXECUTABLE = "-monitoredExecutable";
		public static final String CHECK_WITH_SIMPLIFY = "-checkWithSimplify";
		
		public static final String EXCLUDE_GENERIC_PROGRAM_POINTS_FROM_FSA= "-noGenericProgramPointsInFSA";
		
		
		public static final String ORIGINAL_SOFTWARE_OBJDUMP = "-originalSoftwareObjdump";
		public static final String ORIGINAL_SOFTWARE_FOLDER = "-originalSoftwareFolder";
		public static final String MODIFIED_SOFTWARE_FOLDER = "-modifiedSoftwareFolder";
		public static final String RENAME_FOR_MODIFIED = "-renameForModified";
		public static final String MODIFIED_SOFTWARE_OBJDUMP = "-modifiedSoftwareObjdump";
		public static final String FILTER_USED_VARIABLES = "-filterUsedVariables";
		public static final String COMPONENTS_DEFINITION_FILE = "-componentsDefinitionFile";
		public static final String HIDE_ADDED_MODIFIED = "-hideAddedModified";
		public static final String DLL = "-dll";
		public static final String ORIGINAL_SOFTWARE_FUNCTIONS = "-originalSoftwareFunctionsFile";
		public static final String MODIFIED_SOFTWARE_FUNCTIONS = "-modifiedSoftwareFunctionsFile";
		public static final String PP_TO_INCLUDE = "-programPoints";
		public static final String PROCESS_POINTERS = "-processPointers";
		public static final String SKIP_NORMALIZING_FAILING_TRACE = "-skipNormalizingFailingTrace";
	}



	private File rawThreadTraces;

	private String executionId;

	private FileInteractionTraceRepository interactionRepository;
	private FileExecutionsRepository executionRepository;
	private BufferedWriter currentTraceWriter;
	private Set<String> functionsToFilterOut;

	private boolean addFakeCallsAtProcessEnd;

	private HashMap<Integer, List<Integer>> positionsOfFunctionsToFilterOut;

//	private int traceCounter;

	public boolean isAddFakeCallsAtProcessEnd() {
		return addFakeCallsAtProcessEnd;
	}

	public void setAddFakeCallsAtProcessEnd(boolean addFakeCallsAtProcessEnd) {
		this.addFakeCallsAtProcessEnd = addFakeCallsAtProcessEnd;
	}

	private boolean useDemangleNames;

	private File monitoredExecutable;

	private boolean parsingOptions_ParseAll;

	private SourceLinesMapper sourceMapper;

	//private Map<String, FunctionMonitoringData> originalSoftwareFunctions;

	private boolean hideAddedDeletedFunctions;

	private List<Component> components;

	private Set<String> programPointsToInclude;

	private boolean processPointers;

	private boolean PINtrace;

	private boolean gizipped;

	private boolean filterNotTerminatingFunctions = false;

	
	
	public boolean isUseDemangleNames() {
		return useDemangleNames;
	}

	public void setUseDemangleNames(boolean useDemangleNames) {
		this.useDemangleNames = useDemangleNames;
	}

	public GdbTraceParser(File resultDir) {

		rawThreadTraces = resultDir;
		rawThreadTraces.mkdir();

		interactionRepository = new FileInteractionTraceRepository(rawThreadTraces);
		executionRepository = new FileExecutionsRepository( rawThreadTraces );

	}

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {

		boolean checking = false;

		File resultDir = new File("GdbTracesAnalysis");

		List<File> traces = new ArrayList<File>();

		if ( args.length == 0 ){
			printHelp();
			System.exit(1);
		}
		
		
		Handler h;
		try {
			h = new FileHandler("/tmp/BCT.run.log");
			h.setLevel(Level.ALL);
			h.setFormatter(new SimpleFormatter());
//			Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
			LOGGER.addHandler(h);
		} catch (SecurityException e1){
			e1.printStackTrace();
		} catch (IOException e1 ){
			e1.printStackTrace();
		}

		
		
		boolean enableAva = false;
		boolean filterNonTerminatingFunctions = false;
		boolean simulateClosingOfLastNotTerminatedFunctions = false;
		boolean simulateClosingOfNotTerminatedFunctions = false;
		boolean skipNotTerminatedFunctionCalls = false;
		boolean skipCleaning = false;
		boolean traceSessions = true;
		boolean generateFunctionInvariants = false;
		boolean generateSingleLineInvarinats = false;
		boolean useDemangledNames = false;
		boolean skipConfigCreation= false;
		boolean traceParentVariables = false;
		File monitoredExecutable = null;
		File originalSoftwareObjDump = null;
		File modifiedSoftwareObjDump = null;
		
		boolean checkWithSimplify = false;
		
		boolean renameForModified = false;
		
		ArrayList<File> modifiedSoftwareFolders = new ArrayList<File>();
		ArrayList<File> originalSoftwareFolders = new ArrayList<File>();
		
		boolean excludeGenericProgramPointsFromFSA = false;
		
		boolean filterUsedVariables = false;
		File componentsDefinitionFile = null;
		boolean hideAddedModified = false;
		boolean dll = false;
		boolean processPointers = false;
		
		boolean dumpCheckingTraces = ! Boolean.getBoolean(BCT_SKIP_DUMPING_CHECKING);
		
		File originalFunctionsFile = null;
		File modifiedFunctionsFile = null;
		Map<String, FunctionMonitoringData> originalFunctions = null;
		Map<String, FunctionMonitoringData> modifiedFunctions = null;
		
		Set<String> programPointsToInclude = null;
		
		for ( int i = 0; i < args.length; i++ ){

			String arg = args[i];

			if ( Options.CHECK.equals(arg) ){
				checking = true;
			} else if ( Options.EXCLUDE_GENERIC_PROGRAM_POINTS_FROM_FSA.equals(arg) ){
				MethodOutgoingTraceMaintainer.setTraceGenericProgramPoints(false);
				excludeGenericProgramPointsFromFSA = true;
			} else if ( Options.DONT_TRACE_SESSIONS.equals(arg) ){
				traceSessions=false;
			} else if ( Options.CHECK_WITH_SIMPLIFY.equals(arg) ){
				checkWithSimplify =true;
			} else if ( Options.RENAME_FOR_MODIFIED.equals(arg) ){
				renameForModified =true;
			} else if ( Options.USE_DEMANGLED_NAMES.equals(arg) ){
				useDemangledNames=true;
			} else if ( Options.TRACE_PARENTS_VARIABLES.equals(arg) ){
				traceParentVariables=true;
			} else if ( Options.GENERATE_SINGLE_LINE_INVARIANTS.equals(arg) ){
				generateSingleLineInvarinats=true;
			} else if ( Options.GENERATE_FUNCTION_INVARIANTS.equals(arg) ){
				generateFunctionInvariants=true;
			} else if ( Options.SKIP_CLEANING.equals(arg) ){
				skipCleaning=true;
			} else if ( Options.SKIP_CONFIG_CREATION.equals(arg) ){
				skipConfigCreation=true;
			} else if ( Options.PP_TO_INCLUDE.equals(arg) ){
				programPointsToInclude = new HashSet<String>();
				String programPointsString = args[++i];
				String[] programPoints = programPointsString.split(";");
				programPointsToInclude.addAll(Arrays.asList(programPoints));
			} else if ( Options.ANALIZED_EXECUTABLE.equals(arg) ){
				monitoredExecutable= new File(args[++i]);
			} else if ( Options.RESULTS_DIR.equals(arg) ){
				resultDir= new File(args[++i]);
			} else if ( Options.FILTER_USED_VARIABLES.equals(arg) ){
				filterUsedVariables = true;
			} else if ( Options.ENABLE_AVA.equals(arg) ){
				enableAva = true;
			} else if ( Options.FILTER_ALL_NON_TERMINATING_FUNCTIONS.equals(arg) ){
				filterNonTerminatingFunctions = true;
			} else if ( Options.SIMULATE_CLOSING_OF_LAST_NOT_TERMINATED_FUNCTIONS.equals(arg) ){
				simulateClosingOfLastNotTerminatedFunctions = true;
			} else if ( Options.SIMULATE_CLOSING_OF_LAST_NOT_TERMINATED_FUNCTIONS.equals(arg) ){
				simulateClosingOfNotTerminatedFunctions = true;
			} else if ( Options.FILTER_NOT_TERMINATED_FUNCTIONS.equals(arg) ){
				skipNotTerminatedFunctionCalls = true;	
			} else if ( Options.ORIGINAL_SOFTWARE_FUNCTIONS.equals(arg) ){
				originalFunctionsFile = new File(args[++i]) ;
				originalFunctions = FunctionMonitoringDataSerializer.load( originalFunctionsFile );
			} else if ( Options.MODIFIED_SOFTWARE_FUNCTIONS.equals(arg) ){
				modifiedFunctionsFile = new File(args[++i]);
				modifiedFunctions = FunctionMonitoringDataSerializer.load( modifiedFunctionsFile );
			} else if ( Options.ORIGINAL_SOFTWARE_FOLDER.equals(arg) ){
				originalSoftwareFolders.add( new File(args[++i]) );
			} else if ( Options.MODIFIED_SOFTWARE_FOLDER.equals(arg) ){
				modifiedSoftwareFolders.add( new File(args[++i]) );
			} else if ( Options.ORIGINAL_SOFTWARE_OBJDUMP.equals(arg) ){
				originalSoftwareObjDump = new File(args[++i]);
			} else if ( Options.MODIFIED_SOFTWARE_OBJDUMP.equals(arg) ){
				modifiedSoftwareObjDump = new File(args[++i]);
			} else if ( Options.COMPONENTS_DEFINITION_FILE.equals(arg) ){
				componentsDefinitionFile = new File(args[++i]);
			} else if ( Options.DLL.equals(arg) ){
				dll = true;
			} else if ( Options.PROCESS_POINTERS.equals(arg) ){
				processPointers = true;
				System.setProperty(BCT_COMPARE_POINTERS, "true");//used by class DaikonDeclarationMaker
			} else if ( Options.HIDE_ADDED_MODIFIED.equals(arg) ){
				hideAddedModified = true;
			} else if ( Options.SKIP_NORMALIZING_FAILING_TRACE.equals(arg) ){
				dumpCheckingTraces = false;
			} else {
				traces.add(new File(arg));
			}


		}
		LOGGER.info("Dumping checking: "+dumpCheckingTraces);
		LOGGER.info("Functions file "+originalFunctionsFile);
		
		EnvironmentalSetter.setFlattenerType(flattener.flatteners.BreadthObjectFlattener.class);



		if ( (!skipCleaning) && ( ! checking ) && resultDir.exists() ){
			System.err.println(resultDir.getAbsolutePath() + " exists. Program will exit.");
			System.exit(1);
		}

		resultDir.mkdir();




		EnvironmentalSetter.setBctHome(resultDir.getAbsolutePath());
		
		
		if ( checkWithSimplify ){
			Checker.setIoChecker(new SimplifyIoChecker() );
		} 
		
		checkWithDaikon = Boolean.parseBoolean( System.getProperty(BCT_CHECK_WITH_DAIKON) );
		if ( checkWithDaikon) {
//			File bctHome = EnvironmentalSetter.getBctHomeFile();
//			File confDir = new File( bctHome, "conf" );
//			File confFiles = new File( confDir, "files" );
//			File modelsFetcherSettings = 
			System.out.println("Using Daikon checker");
			Checker.setIoChecker(new DaikonIoChecker());
		}
		
		
		
		
		
		if ( traces.isEmpty() ){
			if ( checking ){
				File tracesFolder = new File( resultDir.getAbsolutePath()+"/tracesToVerify/" );
				populateWithAllTraceFilesInFolder(traces, tracesFolder);
				
				//OLD VERSION
//				traces.add( new File( resultDir.getAbsolutePath()+"/tracesToVerify/gdb.monitor.txt"));
			} else {
				File tracesFolder = new File( resultDir.getAbsolutePath()+"/validTraces/" );
				populateWithAllTraceFilesInFolder(traces, tracesFolder);
				
				//OLD VERSION
//				traces.add( new File( resultDir.getAbsolutePath()+"/validTraces/gdb.monitor.txt"));
			}
		} else {
			ArrayList<File> newTraces = new ArrayList<>();
			for( File trace : traces ){
				if ( trace.isDirectory() ){
					populateWithAllTraceFilesInFolder(newTraces, trace);
				} else {
					newTraces.add(trace);
				}
			}
			traces = newTraces;
		}

		try {

			if ( (! checking) && (!skipConfigCreation) ){
				createConfig();
			}


			File gdbTraces = getGdbTracesDir( checking, resultDir );

			if ( checking ){
				if ( gdbTraces .exists() ){
					FileUtil.deleteDirectoryContents(gdbTraces);
				}
			}


			GdbTraceParser parser = new GdbTraceParser(gdbTraces);
			if ( componentsDefinitionFile != null ){
				List<Component> components;
				try {
					SignatureParser sp;
					if ( useDemangledNames ){
						sp = new CppDemangledSignatureParser();
					} else {
						sp = new CppMangledSignatureParser();
					}
					components = ComponentDefinitionImporter.getComponents(componentsDefinitionFile, sp);
					parser.setComponents( components );
				} catch (ComponentsDefinitionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			parser.setProcessPointers( processPointers );
			parser.setHideAddedDeletedFunctions( hideAddedModified );
			
			if ( (! checking) && renameForModified ){
				if ( originalFunctions != null ){
					parser.setRenameForModified( originalSoftwareFolders, modifiedSoftwareFolders, originalFunctions, modifiedFunctions );
				} else {
					parser.setRenameForModified( originalSoftwareFolders, modifiedSoftwareFolders, originalSoftwareObjDump, modifiedSoftwareObjDump );
				}
			}
			
			if ( simulateClosingOfLastNotTerminatedFunctions ){
				parser.setAddFakeCallsAtProcessEnd(true);
			}

			
			parser.parseTraces(traces);
	
			if (monitoredExecutable != null ){
				parser.setMonitoredExecutable(monitoredExecutable);
			}


			if ( filterNonTerminatingFunctions ) {
				parser.setFilterNotTerminatingFunctions(true);
				FilterGdbThreadTraceListener filter = new FilterGdbThreadTraceListener();



				{
					List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();
					listeners.add( filter  );
					parser.processTraces(listeners);
				}

				

				Set<String> toFilter = parser.getFunctionsToFilterOut();
				if ( toFilter == null ){
					toFilter = new HashSet<String>();
				}
				
				toFilter.addAll(filter.getFunctionsNotClosedOrOpened());
				
				System.out.println("Functions to filter out: ");
				System.out.println(toFilter);
				
				System.out.println("Functions not filtered: ");
				System.out.println(filter.getFunctionsNotFiltered() );
			}



			if ( skipNotTerminatedFunctionCalls ) {

				FilterNotClosedFunctionCallsGdbThreadTraceListener filter = new FilterNotClosedFunctionCallsGdbThreadTraceListener();

				{
					List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();
					listeners.add( filter  );
					parser.processTraces(listeners);
				}



				parser.setPositionsOfFunctionsToFilterOut( filter.getFunctionsNotClosedInTrace() );
			}

			
//			boolean identifyDataFlows = true;
//			DataFlowGdbThreadTraceListener dfFilter = new DataFlowGdbThreadTraceListener();
//
//			if ( identifyDataFlows ){
//				
//				{
//					List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();
//					listeners.add( dfFilter  );
//					parser.processTraces(listeners);
//				}
//			}

			parser.setUseDemangleNames(useDemangledNames);

			if ( programPointsToInclude != null ){
				parser.setProgramPointsToInclude(programPointsToInclude);
			}
			
			
			List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();


			File klfaDataRecordingDir = getKlfaDataRecordingDir(resultDir,checking);
			BctGdbThreadTraceListener bctListener = null;

			
			ArrayList<ProgramPointsCluster> programPointsClusters = new ArrayList<ProgramPointsCluster>();

			if ( generateFunctionInvariants ){
				programPointsClusters.add(new AllFunctionProgramPointsCluster());
			}

			if ( generateSingleLineInvarinats ){
				programPointsClusters.add(new SingleLineProgramPointsCluster());
			}
			
			
			if ( checking ){ 
				
				

				FileUtil.deleteDirectoryContents(klfaDataRecordingDir);

				//Checking listener for BCT 
				BctGdbCheckingThreadTraceListener checkListener = new BctGdbCheckingThreadTraceListener();
				checkListener.setExcludeGenericProgramPointsFromFSAChecking( excludeGenericProgramPointsFromFSA );
				checkListener.setProgramPointsClusters(programPointsClusters);
				
				if ( dll ){
					checkListener.setRenameFunctions(true);
				}
				
				
				if ( filterUsedVariables ){
					
//					Collection<String> sources = new ArrayList<String>();
//					for ( File source : modifiedSoftwareFolders ){
//						sources.add(source.getAbsolutePath());
//					}
					
					BctCheckingFilter filter = new BctCheckingFilter( modifiedFunctions );
					checkListener.setCheckingFilter( filter );
				}
				
				listeners.add( checkListener );

				bctListener = new BctGdbThreadTraceListener(simulateClosingOfNotTerminatedFunctions );
				bctListener.setTraceSessions( traceSessions );
				bctListener.setTraceParents(traceParentVariables);
				bctListener.setProgramPointsClusters(programPointsClusters);
				
				listeners.add( bctListener );

				if ( enableAva ){
					listeners.add( new KLFAGdbThreadTraceListener( klfaDataRecordingDir ) );
				}

			} else {

				bctListener = new BctGdbThreadTraceListener(simulateClosingOfNotTerminatedFunctions );
				bctListener.setTraceSessions( traceSessions );
				bctListener.setTraceParents(traceParentVariables);
				bctListener.setProgramPointsClusters(programPointsClusters);
				
				listeners.add( bctListener );

				

				

				if ( enableAva ){
					listeners.add( new KLFAGdbThreadTraceListener( klfaDataRecordingDir ) );
				}

			}

			parser.processTraces(listeners);

			//Keep folllowing lines here: required to save traces both during checking and dataRecording
			if (bctListener != null ){
				bctListener.stop();
			}

			if ( checking ){ //Checking

				if ( dumpCheckingTraces ){
					try {
						runBctDataRecording();//dump checking traces
					} catch ( Throwable t ){
						t.printStackTrace();
					}
				}
				
				if ( enableAva ){
					System.out.println("Running AVA checking");
					runAvaInference( resultDir, klfaDataRecordingDir, checking );
				}
			} else {

				System.out.println("Running BCT inference");
				try {
					runBctInference();
				} catch ( Throwable t ){
					t.printStackTrace();
				}
				
				if ( enableAva ){
					System.out.println("Running AVA inference");
					runAvaInference( resultDir, klfaDataRecordingDir, checking );
				}
			}

			if ( originalFunctionsFile != null ){
				LOGGER.info("Saving Functions file "+originalFunctionsFile);
				FunctionMonitoringDataSerializer.store( originalFunctions.values(), originalFunctionsFile );
			}
			
			if ( modifiedFunctionsFile != null ){
				FunctionMonitoringDataSerializer.store( modifiedFunctions.values(), modifiedFunctionsFile );
			}

			boolean cleanup = Boolean.parseBoolean(System.getProperty(BCT_CLEANUP_TRACES,"true"));
			if ( cleanup ){
				gdbTraces.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FileReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationFilesManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setFilterNotTerminatingFunctions(boolean b) {
		this.filterNotTerminatingFunctions = b;
	}

	public static void populateWithAllTraceFilesInFolder(List<File> traces,
			File tracesFolder) {
		File[] traceFilesInFolder = tracesFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return ! name.startsWith(".");
			}
			
		});
		for ( File f : traceFilesInFolder){
			traces.add( f );
		}
	}

	private void setProcessPointers(boolean processPointers) {
		this.processPointers = processPointers;
	}

	private void setRenameForModified(ArrayList<File> originalSoftwareFolders,
			ArrayList<File> modifiedSoftwareFolders, 
			Map<String, FunctionMonitoringData> originalFunctions,
			Map<String, FunctionMonitoringData> modifiedFunctions) {
		try {
			sourceMapper = SourceLinesMapper.createMapperFromFunctionData(originalSoftwareFolders, modifiedSoftwareFolders, originalFunctions, modifiedFunctions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setHideAddedDeletedFunctions(boolean hideAddedModified) {
		hideAddedDeletedFunctions = hideAddedModified;
	}

	private void setComponents(List<Component> components) {
		this.components = components;
	}

	private void setRenameForModified(ArrayList<File> originalSoftwareFolders,
			ArrayList<File> modifiedSoftwareFolders, File originalSoftwareObjDump, File modifiedSoftwareObjDump) {
		
		try {
			sourceMapper = SourceLinesMapper.createMapperFromSoftware(originalSoftwareFolders, modifiedSoftwareFolders, originalSoftwareObjDump, modifiedSoftwareObjDump);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

	private void setMonitoredExecutable(File monitoredExecutable) {
		this.monitoredExecutable = monitoredExecutable;
	}

	private void setPositionsOfFunctionsToFilterOut(
			HashMap<Integer, List<Integer>> hashMap) {
		positionsOfFunctionsToFilterOut = hashMap;
	}

	private static File getGdbTracesDir(boolean checking, File resultDir) {
		File gdbTraces;
		if ( ! checking ){
			gdbTraces = new File(resultDir,"rawGDBThreadTraces");
		} else {
			gdbTraces = new File(resultDir,"rawGDBThreadTraces.checking");	
		}

		return gdbTraces;
	}

	private static void printHelp(){
		System.out.println( "\nThis program infers models from valid execution traces recorded with GDB, and idnetify the anomalies present in faulty execution traces." +
				"\nUsage :" +
				"\n\n"+GdbTraceParser.class.getName()+"[OPTIONS] <gdbTrace> [<gdbTrace>..]" +
				"\nOptions can be:" +
				"\n\t-check : verify if the given trace corresponds to the model. If this option is missing the tool uses the trace to infer models that generalize the software behavior described in the trace." +
				"\n\t-resultsDir <dir> : save the results of the analysis in the given directory." +
				"\n");
	}


	private static void runAvaInference(File resultDir, File klfaTracesDir, boolean checking ) {


		File avaResult = new File ( resultDir,  "ava" );

		avaResult.mkdirs();

		if ( checking ){
			FileUtil.deleteRecursively(new File( avaResult, "klfaChecking"));
		} 





		CcrtAvaLauncherOptions options = new CcrtAvaLauncherOptions(avaResult.getAbsolutePath());

		CcrtOutputFiles ccrtOutputs = new CcrtOutputFiles(avaResult);

		try {

			options.skipEventsSeparation = true;

			options.skipEventsTypesDetection = true;



			options.clean = false;

			options.analysisGranularity = "componentLevel";

			options.kBehaviorK = "2";

			options.minimizationLimit = "100";


			File csvFile;


			if ( checking ){
				options.performChecking = true;
				options.performTraining = false;
				csvFile = options.outputFiles.faultyCsvLogFile;
			} else {

				options.performChecking = false;
				options.performTraining = true;	

				csvFile = options.outputFiles.correctCsvLogFile;
			}



			generateKlfaCSV( klfaTracesDir, csvFile );



			CcrtAvaLauncher.runCcrtAnalysis(options, ccrtOutputs);



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void runBctInference() {
		ConfigurationFilesManager.setDaikon_config("essentials");
		List<String> args = new ArrayList<String>();
		args.add("-default");
		
		if ( checkWithDaikon ){
			args.add("-filterDaikonExpressions");	
		}
		
		boolean noFSA = Boolean.getBoolean(BCT_NO_FSA_MODELS);
		if ( noFSA ){
			System.out.println("NO-FSA_MODELS!!!!");
			args.add("-noInteractionModels");
		}
		System.out.println("!!!NO-FSA :"+noFSA);
		
		InvariantGenerator.main( args.toArray( new String[args.size()]) );
	}

	private static void runBctDataRecording(){

		InvariantGeneratorSettings igs = EnvironmentalSetter.getInvariantGeneratorSettings();
		String declsDir = igs.getProperty("normalizedTraceHandlerFile.declsDir");
		declsDir=declsDir.replaceAll("/Preprocessing/decls$", "/Preprocessing.checking/decls");
		igs.setProperty("normalizedTraceHandlerFile.declsDir", declsDir);

		String dtraceDir = igs.getProperty("normalizedTraceHandlerFile.dtraceDir");
		dtraceDir=dtraceDir.replaceAll("/Preprocessing/dtrace$", "/Preprocessing.checking/dtrace" );
		igs.setProperty("normalizedTraceHandlerFile.dtraceDir", dtraceDir);


		String interactionDir=igs.getProperty("normalizedTraceHandlerFile.interactionDir");
		interactionDir=interactionDir.replaceAll("/Preprocessing/interaction$","/Preprocessing.checking/interaction");
		igs.setProperty("normalizedTraceHandlerFile.interactionDir", interactionDir);

		InvariantGenerator.main(new String[]{"-default","-skipInference"} );
	}

	private static void generateKlfaCSV(File klfaTracesDir, File traceToGenerate) throws IOException {

		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(traceToGenerate) );

			for ( File file : getKlfaTraces( klfaTracesDir ) ){

				BufferedReader r = null;
				try {
					r = new BufferedReader ( new FileReader (file) );
					String line;
					while ( ( line = r.readLine() ) != null ) {
						bw.write(line);
						bw.newLine();
					}

					bw.write("|"); //TRace separator

					bw.newLine();

				} finally {
					if ( r != null ){
						r.close();
					}
				}

			}

		} finally {
			if ( bw!= null ){
				bw.close();
			}
		}
	}

	private static File[] getKlfaTraces(File klfaTracesDir) {
		// TODO Auto-generated method stub
		return klfaTracesDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				System.out.println("File is "+pathname);
				return pathname.getName().endsWith("csv");
			}
		});
	}

	public void setFunctionsToFilterOut(Set<String> functionsToFilterOut) {
		this.functionsToFilterOut = functionsToFilterOut;
	}
	
	public Set<String> getFunctionsToFilterOut(){
		return this.functionsToFilterOut;
	}
	
	/**
	 * Set regular expressions that match the program points to include. All the others will be excluded.
	 * 
	 * @param functionsToInclude
	 */
	private void setProgramPointsToInclude(Set<String> functionsToInclude) {
		this.programPointsToInclude = functionsToInclude;
	}

	private static void createConfig() throws ConfigurationFilesManagerException {
		ConfigurationFilesManager.setDaikon_config("essentials");
		ConfigurationFilesManager.updateConfigurationFiles();
	}

	private static File getKlfaDataRecordingDir(File resultDir, boolean checking) {
		File klfaDir = new File ( getDataRecordingDir(resultDir,checking), "klfaTraces" );
		klfaDir.mkdir();
		return klfaDir;
	}

	private static File getDataRecordingDir(File resultDir, boolean checking) {
		String dirName;
		if ( ! checking ){
			dirName = "DataRecording";
		} else {
			dirName = "DataRecording.checking";
		}

		File dr = new File(resultDir,dirName);
		dr.mkdirs();

		return dr;
	}


	public void parseTraces(List<File> traces) throws IOException, FileReaderException {
		for ( File trace : traces ){
			if ( PinTraceUtil.isPinTrace(trace ) ){
				PINtrace=true; //FIXME: assuming all traces are PIN if one is PIN
				processPinTrace(trace);
			}else{
				parseTrace(trace);
			}
		}
	}

	private void processPinTrace(File trace) throws FileReaderException {
		
		
		String pid = PinTraceUtil.getPID( trace );
		try {
			setExecutionId(Long.parseLong(pid));
		} catch (NumberFormatException | FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String threadId = getThreadExecutionId( PinTraceUtil.geThreadId( trace ) );
		
		if ( (!gizipped) && traceIsGZipped(trace) ){
			gizipped=true;
			interactionRepository.setGZipped();
		}
		
		FileInteractionTrace ftrace = (FileInteractionTrace) interactionRepository.getRawTrace(threadId);
		File traceFile = ftrace.getFile();
		
		try {
			Files.createLink(traceFile.toPath(), trace.toPath());
			//Replaced by the above on 2017/02/14
			//FileUtil.copyFile(trace, traceFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private boolean traceIsGZipped(File trace) {
		return trace.getName().endsWith(".gz");
	}



	public void processTraces(List<GdbThreadTraceListener> listeners) throws IOException, FileReaderException {

		List<InteractionTrace> rawTraces = interactionRepository.getRawTraces();



		Collections.sort(rawTraces, new Comparator<InteractionTrace>() {

			@Override
			public int compare(InteractionTrace o1, InteractionTrace o2) {
				try {


					String o1Trace = o1.getTraceId();
					String o2Trace = o2.getTraceId();

					String o1TraceN = o1Trace.substring(0,o1Trace.indexOf('.'));
					String o2TraceN = o2Trace.substring(0,o2Trace.indexOf('.'));

					Integer o1N = Integer.valueOf(o1TraceN);
					Integer o2N = Integer.valueOf(o2TraceN);

					return o1N-o2N;
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TraceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				return 0;
			}
		});

		for ( InteractionTrace trace : rawTraces ){
			try {
				System.out.println(trace.getSessionId()+" "+trace.getThreadId()+" "+executionRepository.getExecutionInfo( trace.getSessionId() ));
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	
		
		String lastSession = null;
		int traceCounter = 0;
		
		long calls=0;
		
		for ( InteractionTrace trace : rawTraces ){
			
			//resetting session id, fix for bug 202
			if ( lastSession == null ){
				//System.out.println("RESETTING ");
				try {
					String executionName = executionRepository.getExecutionInfo( trace.getSessionId() );
					RuntimeContextualDataUtil.setPid(executionName);
				} catch (FileIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			GdbThreadTraceParser tp = new GdbThreadTraceParser(listeners);
			if ( PINtrace ){
				tp.setPINtrace(true);
				tp.setChangeHexToInt(true);
			}
			
			tp.setFilterNotTerminatingFunctions( filterNotTerminatingFunctions );
			
			tp.setProcessPointers( processPointers );
			
			tp.setHideAddedDeletedFunction( hideAddedDeletedFunctions );
			if ( components != null ){
				tp.setComponents ( components );
			}
			tp.setUseDemangledNames(useDemangleNames);
			tp.setMonitoredExecutable( monitoredExecutable );

			if ( sourceMapper != null ){
				tp.setRemapNames( sourceMapper, sourceMapper.getOriginalSoftwareFunctions() );
			}
			
			if ( positionsOfFunctionsToFilterOut != null ){
				List<Integer> functionsToFilterOutForThsTrace = positionsOfFunctionsToFilterOut.get(traceCounter);
				if ( functionsToFilterOutForThsTrace != null ){
					tp.setPositionsOfFunctionsToFilterOut( functionsToFilterOutForThsTrace );
				}
			}

			if ( lastSession != null ) {
				if ( ! trace.getSessionId().equals(lastSession) ){
					
					try {
						String executionName = executionRepository.getExecutionInfo( trace.getSessionId() );
						tp.newSession(executionName);
					} catch (FileIndexException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}

			lastSession=trace.getSessionId();

			

			tp.setAddFakeCallsAtProcessEnd(addFakeCallsAtProcessEnd);

			tp.setFuntionsToFilterOut( functionsToFilterOut );
			tp.setProgramPointsToInclude(programPointsToInclude);
			
			tp.processThreadTrace( (FileInteractionTrace) trace );

			calls += tp.getProcessedCalls();
			
			traceCounter++;
		}
		
		if ( calls == 0 ){
			System.out.println("!!!!GdbTraceParser: NO CALLS PROCESSED");
		}
		
		System.out.println("!!!!GdbTraceParser, CALLS PROCESSED "+calls);

	}




	

	private void parseTrace(File file) throws IOException {


		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));



			//		
			//		currentTraceWriter.write("!!!BCT-ENTER:  ");
			//		currentTraceWriter.newLine();
			//		
			//		currentTraceWriter.write("#0   ");
			//		currentTraceWriter.newLine();


			String line;
			while ( ( line = br.readLine() ) != null ){

				if ( GdbTraceUtil.isRunStart( line ) ){

					cloaseActiveTrace();
					incrementExecutionId();
					processThreadStart("main");

					continue;
				} else if ( GdbTraceUtil.isThreadStart( line ) ){
					processThreadStartLine( line );
				} else if ( GdbTraceUtil.isThreadSwitch( line ) ){
					processThreadSwitchLine( line );
				}

				if ( currentTraceWriter != null ){
					currentTraceWriter.write(line);
					currentTraceWriter.newLine();
				}

			}

		} finally {
			if ( br != null ){
				br.close();
			}

			cloaseActiveTrace();
		}
	}


	private void cloaseActiveTrace() throws IOException {
		if ( currentTraceWriter != null ){
			currentTraceWriter.close();
		}
	}

	private void processThreadSwitchLine(String line) throws IOException {

		String threadId = extractThreadIdFromThreadSwitchLine(line);

		processThreadSwitch(threadId);
	}

	private void processThreadSwitch(String threadId) throws IOException {
		processThread(threadId,false);
	}

	private void processThread(String threadId, boolean start) throws IOException {
		try {

			String thread = getThreadExecutionId(threadId);

			if ( ! start ){
				if ( ! interactionRepository.containsTrace(thread) ){
					thread = getThreadExecutionId("main");
				}
			}


			FileInteractionTrace trace = (FileInteractionTrace) interactionRepository.getRawTrace(thread);

			File traceFile = trace.getFile();
			System.out.println( threadId+" "+thread+" "+traceFile.getName()+" "+traceFile.getAbsolutePath());
			if ( currentTraceWriter != null ){ //This happens for the first instructions
				currentTraceWriter.close();
			}

			currentTraceWriter = new BufferedWriter( new FileWriter( traceFile, true ) );

		} catch (FileReaderException e) {
			e.printStackTrace();
		}
	}

	private String extractThreadIdFromThreadSwitchLine(String line) {
		int threadPrefixLen = "[Switching to Thread ".length();

		return line.substring(threadPrefixLen,threadPrefixLen+10);

	}

	private void processThreadStartLine(String line) throws IOException {

		String threadId = extractThreadIdFromNewThreadLine(line);


		processThreadStart(threadId);

	}

	int fakeThreadIds = 0;
	private String extractThreadIdFromNewThreadLine(String line) {
		int threadPrefixLen = NEW_THREAD_STRING.length();
		int threadEnd = line.indexOf("]");
		if ( threadEnd < 0 ){
			return String.valueOf(fakeThreadIds++);
		}
		return line.substring(threadPrefixLen,threadEnd);

	}

	private void processThreadStart(String threadId) throws IOException {
		processThread(threadId, true);
	}

	private void incrementExecutionId() {
		try {
			long pid = System.currentTimeMillis();
			setExecutionId(pid);
		} catch (FileIndexException e) {
			e.printStackTrace();
		}
	}

	private void setExecutionId(long pid) throws FileIndexException {
		RuntimeContextualDataUtil.setPid( String.valueOf(pid) +"@pc" );
		this.executionId = executionRepository.newExecution(""+SessionsRegistry.getCurrentSessionId());
	}

	private String getThreadExecutionId(String threadId) {
		return executionId+FileDataRecorder.THREAD_EXECUTION_SEPARATOR+threadId;
	}

}
