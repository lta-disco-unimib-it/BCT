/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package tools;

import grammarInference.Engine.KBehaviorEngine;
import grammarInference.Engine.cookEngine;
import grammarInference.Engine.kInclusionEngine;
import grammarInference.Engine.kTailEngine;
import grammarInference.Engine.reissEngine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import modelsFetchers.IoModel;
import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import regressionTestManager.MetaDataHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tools.invariantGenerator.IoTraceNormalizerRunnable;
import tools.invariantGenerator.RawInteractionTracesNormalizer;
import tools.violationsAnalyzer.ViolationsUtil;
import traceReaders.TraceReaderException;
import traceReaders.metaData.MetaDataHandlerFactory;
import traceReaders.normalized.NormalizedInteractionTrace;
import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedInteractionTraceIterator;
import traceReaders.normalized.NormalizedIoTrace;
import traceReaders.normalized.NormalizedIoTraceHandler;
import traceReaders.normalized.NormalizedIoTraceIterator;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;
import traceReaders.normalized.traceCreation.ClassUsageInteractionTraceMaintainerWithOutgoing;
import traceReaders.normalized.traceCreation.MethodOutgoingTraceMaintainer;
import traceReaders.normalized.traceCreation.SerializedClassUsageInteractionTraceMaintainerWithoutOutgoing;
import traceReaders.normalized.traceCreation.TestedMethodsDector;
import traceReaders.raw.FileReaderException;
import traceReaders.raw.FileTracesReader;
import traceReaders.raw.IoInteractionMappingTrace;
import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;
import traceReaders.raw.TraceReaderFactory;
import traceReaders.raw.TracesReader;
import util.FileIndexAppend;
import util.FileUtil;
import util.FileIndex.FileIndexException;
import automata.fsa.FiniteStateAutomaton;
import conf.BctSettingsException;
import conf.ControlFileConfiguration;
import conf.EnvironmentalSetter;
import conf.InteractionInferenceEngineSettings;
import conf.InvariantGeneratorSettings;
import conf.SettingsException;
import dfmaker.core.DaikonNormalizedTracesMaker;
import dfmaker.core.ProgramPointDataStructures;
import dfmaker.core.ProgramPointHash;
import dfmaker.core.Superstructure;
import dfmaker.core.SuperstructureField;
import dfmaker.core.SuperstructuresMaker;
import dfmaker.core.VarTypeResolver;
import dfmaker.core.DaikonDeclarationMaker.DaikonComparisonCriterion;
import dfmaker.utilities.ProgramPointHashExtractor;

public class InvariantGenerator {
	///PROPERTIES-DESCRIPTION: Options that control the inference of models
	
	///force BCT to expand equivalence classes. Given two expressions x == y , y > 0  this option will lead to x == y , y > 0, x > 0 
	public static final String BCT_INFERENCE_EXPAND_EQUIVALENCES = "bct.inference.expandEquivalences";
	
	///run Daikon with undo optimizations otion set to true
	public static final String BCT_INFERENCE_UNDO_DAIKON_OPTIMIZATIONS = "bct.inference.undoDaikonOptimizations";
	
	///don't generate/use data properties that contain array variables (identified by looking for '[' in the expression)
	public static final String BCT_SKIP_ARRAYS = "bct.inference.skipArrays";

	///skip the generation of invariant daikon.inv.binary.twoScalar.NumericInt.ShiftZero (default is true)
	public static final String BCT_SKIP_SHIFT = "bct.inference.skipShift";

	///pass additional parameters to the JVM used to run Daikon
	private static final String BCT_INFERENCE_DAIKON_JVM_OPT = "bct.inference.daikonOpts";


	private static ArrayList methodList = new ArrayList();

	private static ArrayList pointcutForInteractionInvariantsChecking = new ArrayList();

	private static Set memory = new HashSet();

	private static MetaDataHandler metaDataHandler;

	protected static DerivedInvariantsRepository derivedInvariants = new DerivedInvariantsRepository(); 

	private static boolean skipPreprocessing = false;

	private static boolean skipInference = false;

	private static boolean expandEquivalences;

	private static boolean daikonUndoOptimizations;

	private static boolean skipShift;
	
	private static String daikonMem;
	
	public static enum  MethodPosition { ENTER, EXIT};


	static {
		expandEquivalences = Boolean.parseBoolean( System.getProperty(BCT_INFERENCE_EXPAND_EQUIVALENCES) ) ;
		daikonUndoOptimizations = Boolean.parseBoolean( System.getProperty(BCT_INFERENCE_UNDO_DAIKON_OPTIMIZATIONS) ) ;
		daikonMem=System.getProperty(BCT_INFERENCE_DAIKON_JVM_OPT,"-Xmx512M");
		skipArrays = Boolean.parseBoolean( System.getProperty(BCT_SKIP_ARRAYS) ) ;
		skipShift = isOptionSkipShiftEnabled();
	}
	
	static boolean isOptionSkipShiftEnabled(){
		String skipShiftString = System.getProperty(InvariantGenerator.BCT_SKIP_SHIFT);
		boolean skipShift = true;
		if ( skipShiftString != null ){
			skipShift = Boolean.valueOf(skipShiftString);
		}
		return skipShift;
	}
	/**
	 * Normalize Io Traces by running the trace normalization process on differenty threads 
	 *  
	 * @param nThreads
	 * @param metaHandler
	 * @param methodsToIgnore
	 * @param addAdditionalInvariants
	 * @param runTimeout 
	 * @throws BctSettingsException
	 * @throws TraceException
	 */
	private static void concurrentlyNormalizeIoTracks( int nThreads, MetaDataHandler metaHandler, Set<String> methodsToIgnore, boolean addAdditionalInvariants, boolean expandReferences, int runTimeout ) throws BctSettingsException, TraceException {
		TracesReader tr = TraceReaderFactory.getReader();

		Iterator it;
		try {
			it = tr.getIoTraces();




			ExecutorService threadPool = Executors.newFixedThreadPool(nThreads); 


			while ( it.hasNext() ){
				IoTrace  trace = (IoTrace) it.next();
				String methodName = trace.getMethodName();
				System.out.println("Normalizing IO trace for method: "+methodName);



				boolean ignore=false;
//				if (  methodsToIgnore.contains(methodName) ){
//				ignore = true;
//				}
				for ( String methodToIgnore : methodsToIgnore ){
					if ( methodName.startsWith(methodToIgnore) ){
						ignore=true;
						break;
					}
				}


				if ( ignore ){
					System.out.println(methodName+" must be ignored, skipping");
				} else {
					//LAUNCH THREADS
					IoTraceNormalizerRunnable traceNormalizer = new IoTraceNormalizerRunnable(trace, metaHandler, ignore, expandReferences);
					threadPool.submit(traceNormalizer);
				}

				while ( ! threadPool.isTerminated() ){
					try {
						threadPool.awaitTermination(runTimeout, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		} catch (TraceReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	/**
	 * Run a collection of Runnable using a pool of nThread and stops the one that runs for more than runTimeout seconds
	 * 
	 * @param toRun
	 * @param nThreads
	 * @param runTimeout
	 */
	private static void runAndStopBlocked(List<Runnable> toRun, int nThreads, long runTimeout) {
		// TODO Auto-generated method stub
		ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
		HashMap<Future<?>,Runnable> runnableResults = new HashMap<Future<?>, Runnable>();
		ArrayList<Future<?>> executionOrder = new ArrayList<Future<?>>();


		for ( Runnable toRunEntry : toRun ){
			Future<?> f = threadPool.submit(toRunEntry);
			runnableResults.put(f, toRunEntry);
			executionOrder.add(f);
		}


		//This loop checks if a thread is starving, in this case it stops it

		ArrayList<Future<?>> currentlyRunning = new ArrayList<Future<?>>();

		for ( int i = 0; i < nThreads; ++i ){
			currentlyRunning.add(executionOrder.get(i));
		}


		while ( ! threadPool.isTerminated() ){

			try {
				threadPool.awaitTermination(runTimeout+1, TimeUnit.SECONDS );

				//Remove the finished tasks, it stopschecking when finds nThread tasks running
				//It is based on the assumption that the thread pool runs thread in the insertion order

				int currentCounter = 0;
				for ( Future<?> f : executionOrder ){

					if ( f.isDone() ){
						executionOrder.remove(f);
					} else {
						++currentCounter;
					}

					if ( currentCounter > nThreads ){
						break;
					}
				}

				for ( int i=0;i<nThreads;++i){
					Future<?> currentITH = executionOrder.get(i); 
					if ( currentlyRunning.contains(currentITH) ){
						Runnable runnable = runnableResults.get(currentITH);
						System.err.println("Thread for "+runnable+" run for more than "+runTimeout+" seconds: STOPPING IT");
						boolean result = currentITH.cancel(true);
						if ( result ){
							System.err.println("STOPPED");
						} else {
							System.err.println("ALREADY TERMINATED");
						}
						currentlyRunning.remove(currentITH);
						executionOrder.remove(i);
					}
				}

				Iterator<Future<?>> executionOrderIt = executionOrder.iterator();
				while ( currentlyRunning.size() < nThreads && executionOrderIt.hasNext() ){
					Future<?> nextF = executionOrderIt.next();
					if ( ! nextF.isDone() ){
						currentlyRunning.add(nextF);
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	public static void normalizeIoTrace( IoTrace trace, MetaDataHandler metaHandler, boolean addAdditionalInvariants, boolean expandReferences, Set<Long> entryPointsToSkip ) throws NormalizedTraceHandlerException, TraceException, IOException{
//		Make superstructure
		InvariantGeneratorSettings igs = EnvironmentalSetter.getInvariantGeneratorSettings();
		
		boolean optimize = igs.isOptimizationEnabled();
		
		DaikonComparisonCriterion comparisonCriterion = igs.getDaikonComparisonCriterion();
		
		SuperstructuresMaker tn;
		DaikonNormalizedTracesMaker nm;
		
		if ( optimize ){
			System.out.println("OPTIMIZE");
			Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(trace);
			System.out.println("HASHES EXTRACTED");
			tn = new SuperstructuresMaker( trace, ppHashes, expandReferences );
			nm = new DaikonNormalizedTracesMaker( tn, ppHashes, expandReferences );
		} else {
			tn = new SuperstructuresMaker( trace,  expandReferences );
			nm = new DaikonNormalizedTracesMaker( tn, expandReferences );
		}
		
		if ( igs.getExcludeConstantLikeNames() ){
			ArrayList<String> regex = new ArrayList<String>();
			regex.add(".*[A-Z][A-Z][A-Z].*");
			tn.setExclusionRegexs(regex);
		}
		
		
		
		NormalizedIoTraceHandler nth = TraceHandlerFactory.getNormalizedIoTraceHandler( );
		NormalizedIoTrace normalizedTrace = nth.newIoTrace(trace);
		nm.normalizeTrace(trace,normalizedTrace,metaHandler, entryPointsToSkip );
		nth.saveTrace( normalizedTrace, comparisonCriterion );
		Superstructure entrySuperStructure = tn.getEntrySuperStructure();
		Set<Entry<String, String>> refs = entrySuperStructure.getReferences();
		Iterator<Entry<String, String>> itrefs = refs.iterator();
		
		if ( entrySuperStructure == null ){
			System.out.println("NULL ENTRY");
		}
		
//		while( itrefs.hasNext() ){
//			Entry<String,String> entry = itrefs.next();
//			//System.out.println(entry.getKey()+" -> "+entry.getValue());
//		}

		if ( addAdditionalInvariants ){
			addDeriverInvariants( MethodPosition.ENTER, trace.getMethodName(), entrySuperStructure );
		}

		Superstructure exitSuperStructure = tn.getExitSuperStructure();

		Set<Entry<String, String>> refse = exitSuperStructure.getReferences();
		Iterator<Entry<String, String>> itrefse = refs.iterator();
		//System.out.println("REFS EXIT");
		while( itrefse.hasNext() ){
			Entry<String,String> entry = itrefse.next();
			//System.out.println(entry.getKey()+" -> "+entry.getValue());
		}

		if ( addAdditionalInvariants ){
			addDeriverInvariants( MethodPosition.EXIT, trace.getMethodName(), exitSuperStructure );
		}

		ProgramPointDataStructures programPointData = new ProgramPointDataStructures( trace.getMethodName(), entrySuperStructure, exitSuperStructure );
		nth.addProgramPointData( programPointData );
	}

	private static Map<String, List<String>> methodCallIdsToSkipForMethod = new HashMap<String, List<String>>();

	private static boolean enableFilteringOfDaikonExpressions = false;

	private static boolean skipArrays;
	
	private static Set<Long> calculateEntryPointsToSkip( FileTracesReader reader, String methodName) {
		
		List<String> callIds = methodCallIdsToSkipForMethod.get( methodName );
		if ( callIds == null ){
			return null;
		}
		
		HashSet<Long> set = new HashSet<Long>();
		for ( String methodCallId : callIds ){
			String sessionId = FileTracesReader.getSessionId( methodCallId );
			String threadId = FileTracesReader.getThreadId( methodCallId );
			Long methodCallNumber = FileTracesReader.getMethodCallNumber( methodCallId );

			IoInteractionMappingTrace offsets = reader.getIoMappingsForInteractionTrace(sessionId, threadId);
			long offset = offsets.getOffsetForMethodCall((int) methodCallNumber.longValue());

			set.add(offset);
		}
		
		return set;
	}

	private static void normalizeIoTracks( MetaDataHandler metaHandler, Set<String> methodsToIgnore, boolean addAdditionalInvariants , boolean expandReferences ) throws BctSettingsException, TraceException {
		TracesReader tr = TraceReaderFactory.getReader();
		try {
			Iterator it = tr.getIoTraces();

			while ( it.hasNext() ){
				IoTrace  trace = (IoTrace) it.next();
				String methodName = trace.getMethodName();
				System.out.println("Normalizing IO trace for method: "+methodName);
				boolean ignore=false;
				if (  methodsToIgnore.contains(methodName) ){
					ignore = true;
				}
//				for ( String methodToIgnore : methodsToIgnore ){
//					if ( methodName.startsWith(methodToIgnore) ){
//						ignore=true;
//						break;
//					}
//				}
				
				Set<Long> entryPointsToSkip = null;
				if ( tr instanceof FileTracesReader ){
					entryPointsToSkip = calculateEntryPointsToSkip( (FileTracesReader) tr, trace.getMethodName()); 
				}
				
				if ( ignore ){
					System.out.println(methodName+" must be ignored, skipping");
				} else {
					try{
						normalizeIoTrace(trace, metaHandler, addAdditionalInvariants, expandReferences, entryPointsToSkip );
					} catch (IOException e) {
						e.printStackTrace();
					} catch (TraceException e) {
						e.printStackTrace();
					} catch (NormalizedTraceHandlerException e) {
						e.printStackTrace();
					} catch ( Throwable t ){
						t.printStackTrace();
					}
				}
			}
		
		} catch (TraceException e) {
			e.printStackTrace();
		} catch (TraceReaderException e) {
			e.printStackTrace();
		}



	}


	/**
	 * Add derived invariants for the enter/exit phase of a method call.
	 * This invariants can be generated from some brief analysis of the structure of io traces, 
	 * but they are not generated by Daikon (for lack of functionality or because it is not so efficient let it derive them).
	 * 
	 * Actually they are like:
	 * 
	 * parameter[0] != null
	 * parameter[0].x == parameter[1].x
	 * 
	 * @param position			specifies if the passes superstructure refers to enter o exit
	 * @param methodName		the method to wich this superstructure refers to
	 * @param superStructure	the superstructure
	 */
	private static void addDeriverInvariants(MethodPosition position, String methodName, Superstructure superStructure) {
		//Add not-null invariants
		//
		//When we see that there are always children of a parameter 
		//
//		Iterator<SuperstructureField> it = superStructure.varFields().iterator();
//		while ( it.hasNext() ){
//		SuperstructureField field = it.next();
//		if ( ! field.isFakeHash() ){

//		String varName = field.getVarName();

//		//if variable name is like parameter[0] we do not need to add != null constraint
//		if ( ! varName.contains(".") )
//		continue;

//		int lastPoint = varName.lastIndexOf('.'); 
//		String parent = varName.substring(0, lastPoint);

//		String invariant = parent+" != null";

//		if ( position == position.ENTER )
//		derivedInvariants.addPrecondition(methodName, invariant );
//		else
//		derivedInvariants.addPostcondition(methodName, invariant );
//		}

//		}


		Iterator<String> itNotNull = superStructure.getAlwaysNotNull().iterator();
		while ( itNotNull.hasNext() ){
			String fieldName = itNotNull.next();
			String invariant = fieldName+" != null";
			if ( position == position.ENTER )
				derivedInvariants.addPrecondition(methodName, invariant );
			else
				derivedInvariants.addPostcondition(methodName, invariant );

		}


		//Add constant references 
		Iterator<Entry<String, String>> refIterator = superStructure.getConstantReferences().iterator();
		while ( refIterator.hasNext() ){
			Entry<String, String> entry = refIterator.next();
			String invariant = entry.getKey()+" == "+entry.getValue();
			if ( position == position.ENTER )
				derivedInvariants.addPrecondition(methodName, invariant );
			else
				derivedInvariants.addPostcondition(methodName, invariant );
		}

	}



	private static void runDaikon( File outputPath, String configFile, String daikonAdditionalOptions) throws NormalizedTraceHandlerException {

		NormalizedIoTraceHandler nth;

		nth = TraceHandlerFactory.getNormalizedIoTraceHandler();
		
		InvariantGeneratorSettings igs = EnvironmentalSetter.getInvariantGeneratorSettings();
		
		final int daikonLimit = igs.getDaikonExecutionTimeLimit();
		
		NormalizedIoTraceIterator traceIterator = nth.getIoTracesIterator();

		File disteilledIdxFile = new File(outputPath,"iotraces.idx");
		FileIndexAppend distilledIndex = new FileIndexAppend( disteilledIdxFile, ".io.txt");
		
		
		while ( traceIterator.hasNext() ){
			try {
				
				
				NormalizedIoTrace trace = traceIterator.next();

				String id;
				if ( ! distilledIndex.containsName(trace.getMethodName()) ){
					id = distilledIndex.add( trace.getMethodName() );
				} else {
					id = distilledIndex.getId(trace.getMethodName());
				}
				File outputInvariant = new File( outputPath, id+".inv.gz" );
				
				
				//CanonicalPath is used because under linux daikon cannot work passing absolutePath
				//Under linux parameters cannot be passed between double quiotes
				String cmd;

				cmd = "java "+daikonMem+" -classpath \"" + igs.getDaikonPath()  + "\" daikon.Daikon "+daikonAdditionalOptions
						+" --conf_limit "+igs.getDaikonConfidenceLevel()
//						+" --nohierarchy"
						+" --config \""+ EnvironmentalSetter.getBctHomeNoJAR()  + File.separator + "conf" + File.separator + "files" + File.separator + getDaikonConfigFileName(configFile)+"\""; 
				
				if ( daikonUndoOptimizations || igs.getDaikonUndoOptimizations() ){
					cmd += " --config_option daikon.PptSliceEquality.set_per_var=true --config_option daikon.Daikon.undo_opts=true";
				}				
				
						
				cmd += " -o "
				//String cmd = "java -classpath \"" + ClassPath.getDaikonPath() + "\" daikon.Daikon -o "

				+ "\""
				+ outputInvariant.getCanonicalPath()
				+ "\" "
				+ "\""
				+ trace.getDaikonDeclFile()
				+ "\" "
				+"\""
				+ trace.getDaikonTraceFile()
				+ "\""
				;

				//if the system is a Unix we need to remove double quotes from parameters 
				if ( File.separator.equals("/") ){
					cmd = cmd.replace("\"", "");
				}

				System.out.println(cmd);

				final Process p = Runtime.getRuntime().exec(cmd);

				final BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				final BufferedInputStream err = new BufferedInputStream(p.getErrorStream());


				
				
				final File distilledModel = new File(outputPath,id);
				final BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( distilledModel ) );

				StopperThread stopperThread=null;
				//Start time limit thread if necessary
				if ( daikonLimit > 0 ){
					stopperThread = new StopperThread(p,daikonLimit);
					stopperThread.start();
				}
				
				
				//Start daikon
				Thread t = new Thread() {

					public void run() {
						try {
							while (true) {
								int c = in.read();
								if (c < 0)
									break;
								else
									out.write(c);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				};
				t.start();
				Thread t1 = new Thread() {

					public void run() {
						try {
							while (true) {
								int c = err.read();
								if (c < 0)
									break;
								else
									System.err.write(c);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				};
				t1.start();
				try {
					p.waitFor();
					in.close();
					out.close();

					//if we are here p has been terminated, thus we can stop the stopperThead
					if ( stopperThread != null && stopperThread.isAlive() ){
						stopperThread.terminate();	
					}
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}

				processImplicationsFile(distilledModel, outputInvariant);
				
//				File outputInvariantNew = new File(outputPath,outputInvariant+".inv.gz");
//				outputInvariant.renameTo( outputInvariantNew ); //We keep daikon invariants

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		System.out.println("Daikon execution done");
	}

	

	protected static void generateFsa( File outputPath, String daikonEngine ) throws NormalizedTraceHandlerException{


		NormalizedInteractionTraceHandler nth = TraceHandlerFactory.getNormalizedInteractionTraceHandler();
		NormalizedInteractionTraceIterator traceIterator = nth.getInteractionTracesIterator();
		ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
		File fileIndex = new File(outputPath,"methods.idx");
		FileIndexAppend index = new FileIndexAppend( fileIndex, ".ser" );

		while ( traceIterator.hasNext() ){

			NormalizedInteractionTrace trace =  traceIterator.next();


			String methodName = trace.getMethodName();
			File fsaFile = trace.getTraceFile();
			//POTA
			//String id = index.getId(methodName);
			//if ( id == null )
			//	id = index.add(methodName);
			//
			//File output = new File( outputPath, id );
			//
			//System.out.println("Processing file " + fsaFile.getName());
			//		
			//InvariantGenerator.runFSAEngine(daikonEngine, fsaFile, output);
			//	
			try {
				String id;
				if ( ! index.containsName(methodName) )
					id = index.add(methodName);
				else

					id = index.getId(methodName);

				

				System.out.println("Processing file " + fsaFile.getAbsolutePath());

				FiniteStateAutomaton fsa = InvariantGenerator.runFSAEngine(daikonEngine, fsaFile);


				mf.addInteractionModel(methodName, fsa);
			} catch (ModelsFetcherException e) {
				System.out.println("Cannot save fsa for "+methodName);
				e.printStackTrace();
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}

	/**
	 * Given a Daikon configuration name returns the name of the corresponding configuration file
	 * 
	 * @param configName
	 * @return
	 */
	private static String getDaikonConfigFileName(String configName )
	{
		return configName.toLowerCase()+".txt";

	}

	private static FiniteStateAutomaton runFSAEngine(String daikonEngine, File f )
	{
		if (daikonEngine.equalsIgnoreCase(InteractionInferenceEngineSettings.REISS))
		{	
			System.out.println("Start Reiss Engine");
			return reissEngine.process(f);
		}
		else if(daikonEngine.equalsIgnoreCase(InteractionInferenceEngineSettings.KTAIL))
		{	
			System.out.println("Start Ktail Engine");
			return kTailEngine.process(f);
		}
		else if(daikonEngine.equalsIgnoreCase(InteractionInferenceEngineSettings.KINCLUSION))
		{	
			System.out.println("Start Kinclusion Engine");
			return kInclusionEngine.process(f);
		}
		else if(daikonEngine.equalsIgnoreCase(InteractionInferenceEngineSettings.COOK)) 
		{
			System.out.println("Start Cook Engine");
			return cookEngine.process(f);
		}
		else
		{

//			File optFile = new File ( output.getParentFile(), "optimized.tmp" ); 
//			System.out.println("Creating optimized interaction file "+optFile.getAbsolutePath());
//			try {
//			createOptimizedKBehaviorFile(f,optFile);
//			} catch (IOException e) {
//			System.out.println("Cannot create optimized trace file, using original trace");
//			optFile = f;
//			}
			System.out.println("Start Kbehavior Engine");
			return KBehaviorEngine.process(f);
		}
	}

	/**
	 * Generates an optimized trace file for KBehavior algorithm by sorting traces and removing duplicated one. 
	 * It is useful when thare are many traces but 
	 * few different paths.
	 * @param originalTrace	the original trace file	
	 * @param filteredTrace	the optimized file
	 * @throws IOException 
	 */
	private static void createOptimizedKBehaviorFile(File originalTrace, File filteredTrace) throws IOException {
		final int bSize = 256;

		FileReader  reader = new FileReader(originalTrace);


		char[] buffer = new char[bSize];

		TreeSet<String> inserted = new TreeSet<String>();

		int c;
		int pos = 0;
		while ( ( c = reader.read() ) >= 0 ){
			if ( c == '|' ){
				String trace = new String(buffer,0,pos);
				pos = 0;

				if ( ! inserted.contains(trace) ){
					inserted.add(trace);
				}


			} else {
				if ( buffer.length == pos ){
					char[] aux = new char[buffer.length+bSize];
					System.arraycopy(buffer, 0, aux, 0, pos);
					buffer = aux;
				}
				buffer[pos] = (char)c;
				pos++;
			}
		}

		reader.close();

		FileWriter writer = new FileWriter(filteredTrace);
		for ( String trace : inserted ){
			writer.write(trace);
			writer.write('|');
		}
		writer.close();


	}



	private static String processMethodName(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		while (true) {
			String line = in.readLine();
			if (line == null) {
				in.close();
				return null;
			} else if (line.contains("===")) {
				int idx = -1;
				//we can have white lines between ====== and method name
				do{
					line = in.readLine();
					idx = line.indexOf(":::");
				} while ( line != null && idx < 0 );

				in.close();

				return line.substring(0, idx );
			}
		}
	}

	private static void setPreconditions(File f, String methodName, IoModel ioModel, ProgramPointDataStructures ppData) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		//BufferedWriter pre = new BufferedWriter(new FileWriter(new File(
		//		destinationPath, methodName + ".in.txt")));
		outerWhile: while (true) {
			String line = in.readLine();
			if (line == null)
				break;
			else if (line.contains(":ENTER") || line.contains(":::POINT"))
				while (true) {
					line = in.readLine();
					//FIXME: throw an exception
					if ( line == null || line.startsWith("Exiting") )
						break;
					if (line.startsWith("======"))
						break outerWhile;
					if (line.startsWith("class daikon.") || line.length() < 2 )
						continue;
					else {
						addPrecondition(ioModel, ppData, line);
					}
				}
		}
		memory.clear();
		outerWhile: while (true) {
			String line = in.readLine();
			if (line == null)
				break;
			else if (line.contains(":EXIT"))
				while (true) {
					line = in.readLine();
					if (line == null || line.startsWith("Exiting"))
						break outerWhile;
					if (line.startsWith("class daikon.") || line.length() < 2 )
						continue;
					else if (line.contains("orig(")) {
						line = line.substring(line.lastIndexOf("orig("), line
								.length());
						line.trim();
						line = line.substring(4, line.length());
						memory.add(line);
					}
				}
		}
		Iterator i = memory.iterator();
		while (i.hasNext()) {
			String store = "store" + (String) i.next();
			ioModel.addPrecondition(store);
		}
		in.close();
	}

	private static void setPostconditions(File f, String methodName,
			IoModel ioModel, ProgramPointDataStructures ppData) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));

		outerWhile: while (true) {
			String line = in.readLine();
			if (line == null)
				break;
			else if (line.contains(":EXIT"))
				while (true) {
					line = in.readLine();
					if ( line == null ){
						break outerWhile;
					} else if (line.startsWith("Exiting")){
						break outerWhile;
					} else	if (line.startsWith("class daikon.") || line.length() < 2 ) {
						continue;
					} else {
						addPostcondition(ioModel, ppData, line);
					}
				}
		}
		//New implementation does not need remove
		//Iterator i = memory.iterator();
		//while (i.hasNext()) {
		//	String store = "remove" + (String) i.next();
		//	post.add(store);
		//}

		in.close();

	}


	private static void processImplicationsFile( File textualInvariantFile, File serializedInvariantFile )
	throws IOException {
		//FIXME: this can be done better
		String methodName = processMethodName(textualInvariantFile);
		try {
			if (methodName == null)
				System.out.println("No method name for "+textualInvariantFile.getAbsolutePath()+" File " + textualInvariantFile + " skipped");
			else {
				IoModel ioModel = new IoModel();


				NormalizedIoTraceHandler nth = TraceHandlerFactory.getNormalizedIoTraceHandler( );
				ProgramPointDataStructures ppData = nth.getProgramPointData(methodName);
				
				setPreconditions(textualInvariantFile, methodName, ioModel, ppData );
				setPostconditions(textualInvariantFile, methodName, ioModel, ppData );
				methodList.add(methodName);


				//Now we add additional invariants the we can derive from the traces but Daikon cannot
				//For example
				//	parameter[0] != null
				//	parameter[0] == parameter[3]
				//

				
				if ( derivedInvariants.hasPreconditions( methodName) ){
					Iterator<String> it = derivedInvariants.getPreconditions(methodName);
					while( it.hasNext() ){
						String expression = it.next();
						addPrecondition(ioModel, ppData, expression);
					}
				}

				if ( derivedInvariants.hasPostconditions( methodName) ){
					Iterator<String> it = derivedInvariants.getPostconditions(methodName);
					while( it.hasNext() ){
						String expression = it.next();
						addPostcondition(ioModel, ppData, expression);
					}
				}

				if ( expandEquivalences ){
					System.out.println("Expansion of equivalences not required from 2013/07/16, expanded by Daikon");
					
//					System.out.println("Expanding equivalences");
//					ioModel.expandEquivalences();
				} else {
					System.out.println("Not expanding equivalences");
				}
				
				ModelsFetcher mr = ModelsFetcherFactoy.modelsFetcherInstance;
//				System.out.println("!!!MODELS FETCHER "+mr);
//				System.out.println("!!!MODELS FETCHER "+mr.getClass().getCanonicalName());
				mr.addIoModel( methodName, ioModel );
				mr.addSerializedIoModel(methodName, serializedInvariantFile);
				
				System.out.println("File " + textualInvariantFile + " processed");
			}
		} catch ( Exception e ) {
			System.err.println("There was an error processing "+textualInvariantFile);
			e.printStackTrace();
		}
	}

	public static void addPostcondition(IoModel ioModel,
			ProgramPointDataStructures ppData, String expression) {
		if ( filterOutExpression(ppData.getExitSuperStructure(), expression) ){
			System.out.println("Skipping postcondition: "+expression);
			return;
		}
		ioModel.addPostcondition(expression);
	}

	public static void addPrecondition(IoModel ioModel,
			ProgramPointDataStructures ppData, String expression) {
		if ( filterOutExpression(ppData.getEntrySuperStructure(), expression) ){
			System.out.println("Skipping precondition: "+expression);
			return;
		}
//		System.out.println("!!!ADDING PRECONDITION "+expression);
		ioModel.addPrecondition(expression);
	}

	private static boolean filterOutExpression(
			Superstructure superstructure, String expression) {
		
		if ( ! enableFilteringOfDaikonExpressions ){
			return false;
		}
		
		if ( skipArrays ){
			if ( expression.contains("[") ){
				System.out.println("Skipping array expression");
				return true;
			}
		}
		
		if ( skipShift ){
			if ( expression.contains(">>") ){
				System.out.println("Skipping shift expression");
				return true;
			}
		}
		
		Set<String> vars = ViolationsUtil.extractVariables(expression);
		
		for( String var : vars ){
			SuperstructureField field = superstructure.getField(var);
			if ( field == null ){
				System.out.println("Var not found in superstructure: '"+var+"'");
				return true;
			}
			if ( VarTypeResolver.Types.hashcodeType.equals(field.getVarType()) ){
				//is a pointer, consider only models like ptr != null
//				if( ! expression.endsWith(" != null") ){
//					return true;
//				}
				if( ! expression.contains(var+" != null") ){
					System.out.println("Pointer with expression other than != null");
					return true;
				}
			}
		}
		
		return false;
	}

	private static void writeDefinition(File destinationPath)
	throws IOException {
		PrintWriter definition = new PrintWriter(new BufferedWriter(
				new FileWriter(new File(destinationPath,
				"definitionforcheck.xml"))));
		// HEADER
		definition.println("<aspectwerkz>");
		definition.println("  <system id=\"InvariantCheck\">");
		definition.println("    <package name=\"check\">");
		// IO INVARIANTS CHECK
		definition.println("      <aspect class=\"IoInvariantCheckerAspect\">");
		Iterator methods = methodList.iterator();
		int pc = 0;
		while (methods.hasNext()) {
			String signature = (String) methods.next();
			definition.print("        <pointcut name=\"");
			definition.print("pc" + pc++);
			if (signature.contains(".new("))
				definition.print("\" expression=\"execution(");
			// constructor
			else
				definition.print("\" expression=\"execution(* ");
			// method
			//autore modifica: Valle
			//conversione del nome file da formato JVM in formato java
			// method
			definition.print(ConvertJVMtoJava.convert(signature));
			definition.println(")\" /> ");
		}
		// BEFORE
		definition
		.print("        <advice name=\"before\" type=\"before\" bind-to=\"");
		for (int i = 0; i < pc; i++) {
			definition.print("pc" + i);
			if (i == pc - 1)
				break;
			definition.print(" || ");
		}
		definition.println("\" />");
		// AFTER
		definition
		.print("        <advice name=\"after\" type=\"after\" bind-to=\"");
		for (int i = 0; i < pc; i++) {
			definition.print("pc" + i);
			if (i == pc - 1)
				break;
			definition.print(" || ");
		}
		definition.println("\" />");
		definition.println("      </aspect>");
		// INTERACTION INVARIANTS CHECK
		definition
		.println("      <aspect class=\"InteractionInvariantCheckerAspect\">");
		Iterator pointcuts = pointcutForInteractionInvariantsChecking
		.iterator();
		pc = 0;
		while (pointcuts.hasNext()) {
			String signature = (String) pointcuts.next();
			definition.print("        <pointcut name=\"");
			definition.print("pc" + pc++);
			if (signature.contains(".new("))
				definition.print("\" expression=\"execution(");
			// constructor
			else
				definition.print("\" expression=\"execution(* ");
			// method
			//autore modifica: Valle
			//conversione del nome file da formato JVM in formato java
			// method
			definition.print(ConvertJVMtoJava.convert(signature));
			definition.println(")\" /> ");
		}
		// BEFORE
		definition
		.print("        <advice name=\"before\" type=\"before\" bind-to=\"");
		for (int i = 0; i < pc; i++) {
			definition.print("pc" + i);
			if (i == pc - 1)
				break;
			definition.print(" || ");
		}
		definition.println("\" />");
		// AFTER
		definition
		.print("        <advice name=\"after\" type=\"after\" bind-to=\"");
		for (int i = 0; i < pc; i++) {
			definition.print("pc" + i);
			if (i == pc - 1)
				break;
			definition.print(" || ");
		}
		definition.println("\" />");
		definition.println("      </aspect>");
		// FOOTER
		definition.println("    </package>");
		definition.println("  </system>");
		definition.println("</aspectwerkz>");
		definition.close();
	}

	private static void writeBatchFile(File destinationPath, File[] classpath,
			String mainClass, String[] argv) throws IOException {
		PrintWriter batchFile = new PrintWriter(new BufferedWriter(
				new FileWriter(new File(destinationPath, "runwithcheck.bat"))));
		batchFile
		.write("aspectwerkz -Daspectwerkz.definition.file=definitionforcheck.xml ");
		batchFile.write("-cp \""
				+ EnvironmentalSetter.getBctHome() );
		//inserito nel ciclo for destinationPath.getCanonicalPath() per
		// configurare correttamente il cp di runwithcheck
		for (int i = 0; i < classpath.length; i++){
			batchFile.write( classpath[i].getAbsolutePath() );
		}
		batchFile.write( ";"+ destinationPath.getCanonicalPath() );
		batchFile.write("\" " + mainClass + " ");
		for (int i = 0; i < argv.length; i++)
			batchFile.write(argv[i] + " ");
		batchFile.println();
		batchFile.close();

		//To work everywhere
		batchFile = new PrintWriter(new BufferedWriter(
				new FileWriter(new File(destinationPath, "runwithcheck.sh"))));
		batchFile
		.write("aspectwerkz -Daspectwerkz.definition.file=definitionforcheck.xml ");
		batchFile.write("-cp \""
				+ EnvironmentalSetter.getBctHome() );
		//inserito nel ciclo for destinationPath.getCanonicalPath() per
		// configurare correttamente il cp di runwithcheck
		for (int i = 0; i < classpath.length; i++)
			batchFile.write( File.pathSeparator + classpath[i].getAbsolutePath() + File.pathSeparator
					+ destinationPath.getCanonicalPath() );
		batchFile.write( "\"" );
		batchFile.write(" " + mainClass + " ");
		for (int i = 0; i < argv.length; i++)
			batchFile.write(argv[i] + " ");
		batchFile.println();
		batchFile.close();

	}


	private static void writeBatchFileOffline(File destinationPath,
			File[] classpath, String mainClass, String[] argv, String testMethod)
	throws IOException {
		//creation of weaveapplication.bat
		PrintWriter batchFile = new PrintWriter(new BufferedWriter(
				new FileWriter(
						new File(destinationPath, "weaveapplication.bat"))));
		//batchFile.write("xcopy Z:"+"\\"+"BCT"+"\\"+"bin"+"\\"+"check ");
		batchFile.write("xcopy");
		try {
			batchFile.write(" \""
					+ EnvironmentalSetter.getBctHomeNoJAR());
			//+ new File(PreProcessor.class.getResource("/").toURI()));
		} catch (Exception e) {
		}
		batchFile.write("\\" + "check" + "\" ");
		for (int i = 0; i < classpath.length; i++)
			batchFile.write("\"" + classpath[i].getAbsolutePath());
		batchFile.write("\\" + "check" + "\"");
		batchFile.println();
		batchFile.write("aspectwerkz -offline definitionforcheck.xml -cp ");
		for (int i = 0; i < classpath.length; i++)
			batchFile.write("\"" + classpath[i].getAbsolutePath() + "\"");
		batchFile.write(" ");
		for (int i = 0; i < classpath.length; i++)
			batchFile.write("\"" + classpath[i].getAbsolutePath() + "\"");
		batchFile.println();
		batchFile.close();

		//creation of weaveapplication.sh
		batchFile = new PrintWriter(new BufferedWriter(
				new FileWriter(
						new File(destinationPath, "weaveapplication.sh"))));
		batchFile.write("cp -r ");
		try {
			batchFile.write(" "
					+ EnvironmentalSetter.getBctHomeNoJAR());
			//+ EnvironmentalSetter.getBctHomeNoJAR());
			//+ new File(PreProcessor.class.getResource("/").toURI()));
		} catch (Exception e) {
		}
		batchFile.write( File.separator + "check" + File.separator + " ");
		for (int i = 0; i < classpath.length; i++)
			batchFile.write( classpath[i].getAbsolutePath() );
		batchFile.write( File.separator + "check" + File.separator );
		batchFile.println();
		batchFile.write("aspectwerkz -offline definitionforcheck.xml -cp ");

		// adding path to the XML parser to the classpath
		//batchFile.write("\"" + ClassPath.getXmlParserPath()+"\"" );


		for (int i = 0; i < classpath.length; i++)
			batchFile.write( File.pathSeparator + classpath[i].getAbsolutePath() );
		batchFile.write(" ");
		for (int i = 0; i < classpath.length; i++)
			batchFile.write( classpath[i].getAbsolutePath() );
		batchFile.println();
		batchFile.close();

		//creation of runwithcheck.bat
		PrintWriter batchFile2 = new PrintWriter(new BufferedWriter(
				new FileWriter(new File(destinationPath, "runwithcheck.bat"))));
		batchFile2.write("java ");
		try {
			batchFile2.write("-cp \""
					+ EnvironmentalSetter.getBctHome() );
		} catch (Exception e) {
		}
		for (int i = 0; i < classpath.length; i++)
			batchFile2.write("" + classpath[i].getAbsolutePath() );
		batchFile2
		.write(";.;%ASPECTWERKZ_HOME%/lib/aspectwerkz-2.0.jar;%ASPECTWERKZ_HOME%/lib/aspectwerkz-core-2.0.jar\"");
		batchFile2.write(" ");
		batchFile2
		.write("-Daspectwerkz.definition.file=definitionforcheck.xml");
		batchFile2.write(" ");
		batchFile2.write(" " + mainClass + " ");
		batchFile2.println();
		batchFile2.close();


		//creation of runwithcheck.sh
		batchFile2 = new PrintWriter(new BufferedWriter(
				new FileWriter(new File(destinationPath, "runwithcheck.sh"))));
		batchFile2.write("java ");
		try {
			batchFile2.write("-cp \""
					+ EnvironmentalSetter.getBctHome()
			);
		} catch (Exception e) {
		}
		for (int i = 0; i < classpath.length; i++)
			batchFile2.write( File.pathSeparator + classpath[i].getAbsolutePath() );
		batchFile2
		.write( File.pathSeparator+"."+File.pathSeparator+"$ASPECTWERKZ_HOME/lib/aspectwerkz-2.0.jar"+File.pathSeparator+"$ASPECTWERKZ_HOME/lib/aspectwerkz-core-2.0.jar\"");
		batchFile2.write(" ");
		batchFile2
		.write("-Daspectwerkz.definition.file=definitionforcheck.xml");
		batchFile2.write(" ");
		batchFile2.write(" " + mainClass + " ");
		batchFile2.println();
		batchFile2.close();
	}

	public static void processLogFiles(File inputPath, File outputPath,
			File[] classpath, String mainClass, String[] args, String fsaEngine, String daikonConfigFile)
	throws IOException {



		processLogs();

		writeBatchFile(outputPath, classpath, mainClass, args);
	}

	private static void processLogs() {
		processLogs(null);
	}



	private static void processLogs( MetaDataHandler metaHandler ) {
		methodList.clear();
		pointcutForInteractionInvariantsChecking.clear();
		memory.clear();		
		
		InvariantGeneratorSettings iGS = EnvironmentalSetter.getInvariantGeneratorSettings();
		
		Set<String> methodsToIgnore = iGS.getMethodsToIgnore();
		Set<String> methodsToInclude = iGS.getMethodsToIncludePatterns();
		
		System.out.println("Patterns of methods to include exclusively: "+methodsToInclude);
		
		boolean addAddditionalInvariants = iGS.addAdditionInvariants();
		boolean expandReferences = iGS.isExapandReferences();
		int parallelInferenceThreads = iGS.getParallelInferenceThreads();
		System.out.println("EXPAND? "+expandReferences);
		
		
		
		try {
			if ( parallelInferenceThreads == 1 ){
				
				try {
					runSingleThreadModelInferenceTasks(metaHandler, methodsToIgnore, addAddditionalInvariants, expandReferences, iGS, methodsToInclude);
				} catch (FileReaderException e) {
					throw new IOException(e);
				}
			} else if ( parallelInferenceThreads >  1 ) {
				runMultipleThreadsModelInferenceTasks( parallelInferenceThreads );
			}

			if ( iGS.getDeleteTemporaryDir() ){
				FileUtil.deleteRecursively(iGS.getTemporaryDir());
				
			}
		} catch (BctSettingsException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NormalizedTraceHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Throwable cause = e.getCause();
			while ( cause != null ){
				System.err.println("Caused by ");
				cause = e.getCause();
			}
		}
		//POTA
		//try {
		//	writeDefinition(iGS.getDistilledDir());
		//} catch (IOException e) {
		//
		//	e.printStackTrace();
		//	System.err.println("Cannot write definition file for aspects");
		//}
	}



	private static void runMultipleThreadsModelInferenceTasks(int parallelInferenceThreads) {
		throw new NotImplementedException();
	}

	/**
	 * Run the tasks to inference behavioral models in sequence. The tasks are run by this thread.
	 *  
	 * @param metaHandler
	 * @param methodsToIgnore
	 * @param addAddditionalInvariants
	 * @param iGS
	 * @param methodsToInclude 
	 * @throws IOException
	 * @throws BctSettingsException
	 * @throws TraceException
	 * @throws NormalizedTraceHandlerException
	 * @throws FileReaderException 
	 */
	private static void runSingleThreadModelInferenceTasks(MetaDataHandler metaHandler, Set<String> methodsToIgnore, boolean addAddditionalInvariants, boolean expandReferences, InvariantGeneratorSettings iGS, Set<String> methodsToInclude) throws IOException, BctSettingsException, TraceException, NormalizedTraceHandlerException, FileReaderException {
		
		if ( iGS.getSkipInference() ){
			skipInference=true;
		}
		
		if ( ! skipPreprocessing ){
			
			if ( iGS.getInferComponentsInteractionModels() ){
				System.out.println("Normalizing interaction traces...");
				RawInteractionTracesNormalizer interactionNormalizer = new RawInteractionTracesNormalizer();
				iGS.setTraceMaintainerType(MethodOutgoingTraceMaintainer.class);
				interactionNormalizer.normalizeInteractionTraces( metaHandler, methodsToIgnore, methodsToInclude );
				methodCallIdsToSkipForMethod = interactionNormalizer.getMethodCallIdsToSkip();
				
				if ( interactionNormalizer.isMetaHandlerDisabled() ){
					metaHandler = null;
				}
				
				System.out.println("Done normalizing interaction tracks");
			} else {
				System.out.println("Skipping preprocessing of Interaction traces, inferInteractionModels option set to false.");
			}
			//FIXME: define a way to infer multiple kind of models
			if ( iGS.getInferClassesUsageInteractionModels() ){
				System.out.println("Normalizing interaction traces...");
				RawInteractionTracesNormalizer interactionNormalizer = new RawInteractionTracesNormalizer();
				iGS.setTraceMaintainerType(SerializedClassUsageInteractionTraceMaintainerWithoutOutgoing.class);
				interactionNormalizer.normalizeInteractionTraces( metaHandler, methodsToIgnore, methodsToInclude );
				System.out.println("Done normalizing interaction tracks");
			} else {
				System.out.println("Skipping preprocessing for class usage models.");
			}
			
			if ( iGS.getIdentifyTestedMethods() ){
				System.out.println("Normalizing interaction traces...");
				RawInteractionTracesNormalizer interactionNormalizer = new RawInteractionTracesNormalizer();
				iGS.setTraceMaintainerType(TestedMethodsDector.class);
				interactionNormalizer.normalizeInteractionTraces( metaHandler, methodsToIgnore, methodsToInclude );
				System.out.println("Done normalizing interaction tracks");
			} else {
				System.out.println("Skipping preprocessing for class usage models.");
			}
			
			if ( iGS.getInferClassesUsageInteractionModelsWithOutgoingCalls() ){
				System.out.println("Normalizing interaction traces...");
				RawInteractionTracesNormalizer interactionNormalizer = new RawInteractionTracesNormalizer();
				iGS.setTraceMaintainerType(ClassUsageInteractionTraceMaintainerWithOutgoing.class);
				interactionNormalizer.normalizeInteractionTraces( metaHandler, methodsToIgnore, methodsToInclude );
				System.out.println("Done normalizing interaction tracks");
			} else {
				System.out.println("Skipping preprocessing for class usage models with outgoing calls.");
			}
			
			if ( iGS.getInferComponentsIOModels() ){
				System.out.println("Normalizing I/O tracks...");
				normalizeIoTracks( metaHandler, methodsToIgnore, addAddditionalInvariants, expandReferences );
				System.out.println("Done normalizing I/O tracks");
			} else {
				System.out.println("Skipping preprocessing of IO traces, inferIOModels option set to false.");
			}
			
			
		} else {
			System.out.println("Skipping preprocessing phase, skipPreprocessing option set to true.");
		}
		
		if ( ! skipInference ) {
			if ( iGS.getInferComponentsIOModels() ){
			System.out.println("Inferring I/O invariants...");
			runDaikon(  iGS.getDistilledDir(), iGS.getDaikonConfig(), iGS.getDaikonAdditionalOptions() );
			System.out.println("Done inferring I/O invariants");
			} else {
				System.out.println("Skipping inference of IO models, inferIOModels option set to false.");	
			}
			
			
			//FIXME: define a way to infer multiple kind of models
			if ( iGS.getInferComponentsInteractionModels() || iGS.getInferClassesUsageInteractionModels() ){
				System.out.println("Inferring interaction invariants...");
				generateFsa( iGS.getDistilledDir(), iGS.getFSAEngine() );
				System.out.println("Done inferring interaction invariants");
			} else {
				System.out.println("Skipping inference of interaction models, inferInteractionModels option set to false.");
			}
			
		} else {
			System.out.println("Skipping model inference pahse, preprocessOnly option set to true.");
		}

	}

	public static void processLogFilesOffline(File inputPath, File outputPath,
			File[] classpath, String mainClass, String[] args, String offline, String daikonEngine, String daikonConfigFile)
	throws IOException {
		/*		methodList.clear();
		pointcutForInteractionInvariantsChecking.clear();
		memory.clear();
		normalizeIoTracks(new File(inputPath, "01-logs/"), new File(outputPath,
				"02-normalized/"));
		normalizeInteractionInvariantLog(new File(inputPath, "01-logs/"),
				new File(outputPath, "02-normalized/"));
		runDaikon(new File(outputPath, "02-normalized/"), new File(outputPath,
				"03-distilled/"));
		generateFsa(new File(outputPath, "02-normalized/"), new File(
				outputPath, "03-distilled/"));
		generateIoInvariants(new File(outputPath, "03-distilled/"), new File(
				outputPath, "04-invariants/"));
		generateInteractionInvariants(new File(outputPath, "03-distilled/"),
				new File(outputPath, "04-invariants/"));
		writeDefinition(outputPath);*/
		processLogs();
		writeBatchFileOffline(outputPath, classpath, mainClass, args, offline);
	}

	public static boolean controlOptions(String options1, String options2)
	{
		if ((options1.equalsIgnoreCase(InteractionInferenceEngineSettings.KTAIL)||options1.equalsIgnoreCase(InteractionInferenceEngineSettings.COOK)||
				options1.equalsIgnoreCase(InteractionInferenceEngineSettings.KBEHAVIOR)||options1.equalsIgnoreCase(InteractionInferenceEngineSettings.KINCLUSION)||
				options1.equalsIgnoreCase(InteractionInferenceEngineSettings.REISS))&&(options2.equalsIgnoreCase(ControlFileConfiguration.DEFAULT)||options2.equalsIgnoreCase(ControlFileConfiguration.ESSENTIALS)||
						options2.equalsIgnoreCase(ControlFileConfiguration.INTERMEDIATE)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static void main(String argv[]) {
		String propertiesFilePath = null;
		String modelsFetcherFilePath = null;
		String inferenceEngineFilePath = null;

		System.out.println("PAY ATTENTION: daikon jar path and daikon confidence level now must be set in invariantGeneratorSettings configuration  file");
		
		System.out.println("SKIP ARRAYS : "+skipArrays);
		
		if ( argv.length == 0 ){
			printHelp();
			System.exit(-1);
		}
		
		Boolean inferIOModels = null;
		Boolean inferInteractionModels = null;
		
		for ( int i = 0; i < argv.length; i++ ){
			if ( argv[i].equals("-skipInference") || argv[i].equals("-preprocessOnly")){
				skipInference = true;
			} else if ( argv[i].equals("-skipPreprocessing") ){
				skipPreprocessing = true;
			} else if ( argv[i].equals("-noInteractionModels") ){
				inferInteractionModels = false;
			} else if ( argv[i].equals("-noIOModels") ){
				inferIOModels = false;
			} else	if ( argv[i].equals("-invariantGeneratorConfFile") ){
				propertiesFilePath = argv[++i];
			} else if ( argv[i].equals("-modelsFetcherConfFile") ){
				modelsFetcherFilePath = argv[++i];
			} else if ( argv[i].equals("-inferenceEngineConfFile") ){
				inferenceEngineFilePath = argv[++i];
			} else if ( argv[i].equals("-filterDaikonExpressions") ){
				enableFilteringOfDaikonExpressions = true;
			} else if ( argv[i].equals("-default") ){
				;	//do nothing just eat the option
			} else {
					printHelp();
					System.exit(-1);
			}
		}
		
		if ( propertiesFilePath != null )
			try {
				EnvironmentalSetter.setInvariantGeneratorSettingsFile(propertiesFilePath);
			} catch (SettingsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}

			if ( modelsFetcherFilePath != null ){
				try {
					EnvironmentalSetter.setModelsFetcherSettingsFile(modelsFetcherFilePath);
				} catch (SettingsException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

			if ( inferenceEngineFilePath != null ){
				try {
					EnvironmentalSetter.setInferenceEngineSettingsFile(inferenceEngineFilePath);
				} catch (SettingsException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

			InvariantGeneratorSettings iGS = EnvironmentalSetter.getInvariantGeneratorSettings();
			if ( inferIOModels != null ){
				iGS.setInferComponentsIOModels(inferIOModels);
			}
			if ( inferInteractionModels != null ){
				iGS.setInferComponentsInteractionModels(inferInteractionModels);
			}
			
			
			
			metaDataHandler = MetaDataHandlerFactory.getMetaDataHandler();
			processLogs( metaDataHandler );

			/*
		if (argv.length < 9) {

			System.out
					.println("Usage: java tools.InvariantGenerator -logs <path> -dest <path> -TARGET_HOME <classpath> class [args...]");
			System.out.println();
			System.out
					.println("-logs: must specify the directory wich contains the log of the testcase execution.");
			System.out
					.println("-dest: denote the directory where the invariants and the script will be placed.");
			System.out
					.println("-TARGET_HOME: specify the classpath of the application that must be monitored");
			System.out
					.println("<-InferenceEngine>: specify Inference Engine (-KTAIL or -KBEHAVIOR or -COOK or -KINCLUSION or -REISS)");
			System.out
					.println("<-DaikonFileConfiguration>: specify type of Daikon file Configuration (-DEFAULT or -ESSENTIALS or -INTERMEDIATE");
			System.out.println("[args...]: optional command line arguments");
			System.out.println();
			System.out
					.println("Example: java tools.InvariantGenerator -logs c:/logs/ -dest c:/out/ -TARGET_HOME c:/bin/ MyClass -InferenceEngine -DaikonFileConfiguration -WeavingType");
		} else {
			if (!argv[0].equals("-logs"))
				throw new IllegalArgumentException("First option must be -logs");
			else if (!argv[2].equals("-dest"))
				throw new IllegalArgumentException(
						"Second option must be -dest");
			else if (!argv[4].equals("-TARGET_HOME"))
				throw new IllegalArgumentException(
						"Third argument must be -TARGET_HOME");

			EnvironmentalSetter.setConfigurationValues();



			File input = new File(argv[1]);
			File output = new File(argv[3]);
			String[] stringClasspath = argv[5].split(File.pathSeparator);
			ArrayList classpath = new ArrayList();
			for (int i = 0; i < stringClasspath.length; i++)
				classpath.add(new File(stringClasspath[i]));
			ArrayList args = new ArrayList();
			for (int i = 9; i < argv.length; i++)
				args.add(argv[i]);

			//aggiunta la possibilit? di specificare il tipo di weaving
			if (argv.length == 9
					|| (argv.length == 10 && argv[9].equals("-online"))) {
				if (controlOptions(argv[7].substring(1),argv[8])==true)
				{
					processLogFiles(input, output, (File[]) classpath
						.toArray(new File[0]), argv[6], (String[]) args
						.toArray(new String[0]), argv[7].substring(1), argv[8]);
				}
				else
				{
					System.out.println("error: Incorrect type of Engine Inference or Daikon file configuration ");
				}
			} else if (argv.length == 10 && argv[9].equals("-offline")) {

				if (controlOptions(argv[7].substring(1),argv[8])==true)
				{
					processLogFilesOffline(input, output, (File[]) classpath
						.toArray(new File[0]), argv[6], (String[]) args
						.toArray(new String[0]), argv[9], argv[7].substring(1), argv[8]);
				}
				else
				{
					System.out.println("error: Incorrect type of Engine Inference or Daikon file configuration ");
				}
			} else {
				System.out
						.println("error: you must specify only -online or -offline options");
				System.out.println();
			}

		}*/
	}



	private static void printHelp() {
		System.out.println("Usage : \n" +
				"java tools.InvariantGenerator OPTIONS\n" +
				"\n" +
				"OPTIONS are:\n" +
				"-default : use configuration config under BCT_HOME/conf/files\n"+
				"-invariantGeneratorConfFile <file> : invariant generator configuration file (if not specified uses the one specified in BCT.properties)\n" +
				"-modelsFetcherConfFile <file> : models fecther configuration file, " +
				"the file with configuration options for the component that reads and store models\n" +
				"-inferenceEngineConfFile <file> : inference engine configuration file, the file with the options for the InferenceEngine you want to use\n"+
				"-preprocessOnly : do not infer models, do only preprocessing of traces\n" +
				"-skipPreprocessing : infer models using the already preprocessed traces (of course such traces must be present)\n" +
				"-noIOModels : do not infer IO Models\n" +
				"-noInteractionModels : do not infer interaction models"
		);
		//FIXME: add description of the configuration file format
	}

	public static MetaDataHandler getMetaDataHandler() {
		return metaDataHandler;
	}

	public static void setMetaDataHandler(MetaDataHandler metaDataHandler) {
		InvariantGenerator.metaDataHandler = metaDataHandler;
	}
}