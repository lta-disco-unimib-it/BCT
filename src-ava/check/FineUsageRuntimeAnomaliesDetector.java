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
package check;

import flattener.core.Handler;
import grammarInference.Record.Trace;
import grammarInference.Record.VectorTrace;
import it.unimib.disco.lta.ava.automataExtension.AutomataExtension;
import it.unimib.disco.lta.ava.automataExtension.AutomataExtensionType;
import it.unimib.disco.lta.ava.automataExtension.KBehaviorFSAExtender;
import it.unimib.disco.lta.ava.engine.AutomataViolationsAnalyzer;
import it.unimib.disco.lta.ava.engine.configuration.AvaConfiguration;
import it.unimib.disco.lta.ava.engine.configuration.AvaConfigurationFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import oracle.DFAFSANavigator;
import oracle.FSANavigator;
import oracle.FSANavigatorTrue;
import oracle.NFAFSANavigator;
import recorders.DataRecorder;
import recorders.RecorderException;
import recorders.RecorderFactory;
import recorders.ViolationsRecorder;
import recorders.ViolationsRecorderException;
import recorders.ViolationsRecorder.InteractionViolationType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.RuntimeContextualDataUtil;
import automata.State;
import automata.fsa.FiniteStateAutomaton;
import conf.ConfigurationSettings;
import conf.EnvironmentalSetter;
import conf.FineInteractionCheckerSettings;
import conf.InteractionCheckerSettings;

/**
 * This class receive monitoring events at runtime and records object usage 
 * anomaies.
 * 
 * @author Fabrizio Pastore - fabrizio.pastore@gmail.com
 *
 */
public class FineUsageRuntimeAnomaliesDetector implements DataRecorder {
	
	private HashMap<String,FiniteStateAutomaton> automata = new HashMap<String, FiniteStateAutomaton>();
	private boolean dfa;
	private AutomataViolationsAnalyzer analyzer;
	private static final FSANavigator nullFSANavigator = new FSANavigatorTrue();
	
	
	static {
		UsageAnomaliesIdentifier hook = new UsageAnomaliesIdentifier();
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	
	public static class UsageAnomaliesIdentifier extends Thread {
		
		public void run(){
			System.out.println("BCT Checking Anomalies");
			
			for ( Map<Integer,ExecutionData> data : stacks ){
				for ( Entry<Integer,ExecutionData> entry : data.entrySet() ){
					ExecutionData executionData = entry.getValue();
					
					FSANavigator navigator = executionData.navigator;
					
					if ( ( ! executionData.isAnomalous() ) && ( ! navigator.finalState() ) ) {
						executionData.setAnomalous(true);
						logViolation(executionData.threadId, executionData.signature, executionData.navigator, true);
					}
					
					if ( executionData.isAnomalous() ){
						logAnomalousSequence(executionData);
					}
				
					
					
				}
			}
		}
	}
	
	private static class ExecutionData {
		List<String> invocations = new ArrayList<String>();
		FSANavigator navigator;
		long threadId;
		private String signature;
		private boolean anomalous;

		public boolean isAnomalous() {
			return anomalous;
		}

		public void setAnomalous(boolean anomalous) {
			this.anomalous = anomalous;
		}

		public ExecutionData ( FSANavigator navigator, long threadId, String signature ){
			this.navigator = navigator;
			this.threadId = threadId;
			this.signature = signature;
		}
		
		public void addMethodCall( String method ){
			invocations.add(method);
		}
		
		
	}
	
	private static List<Map<Integer,ExecutionData>> stacks = new ArrayList<Map<Integer,ExecutionData>>();
	
	private static ThreadLocal<Map<Integer,ExecutionData>> stack = new ThreadLocal<Map<Integer,ExecutionData>>(){

		@Override
		protected Map<Integer,ExecutionData> initialValue() {
			Map<Integer, ExecutionData> _stack = new HashMap<Integer,ExecutionData>();
			
			synchronized (stacks) {
				stacks.add(_stack);
			}
			
			return _stack;
		}
		
	};
	
	public void checkEnter(Object calledObject, long threadId, String signature) {
		
		//System.err.println("ENTER: "+calledObject+" "+signature);
		//Checks are done at EXIT point because otherwise we donot have the object for constructors calls
		
		
	}

	/**
	 * Load the automaton from the model fetcher and keep trace of it.
	 * If th emodel does not exists returns null.
	 * 
	 * 
	 * @param signature
	 * @return
	 * @throws ModelsFetcherException
	 */
	private FSANavigator getFSANavigator(String signature) {

		FiniteStateAutomaton fsa;
		if ( automata.containsKey(signature) ){
			fsa = automata.get(signature);
		} else {

			ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;

			try {
				if ( ! mf.interactionModelExist(signature) ){
					System.err.println("!!!BCT: no model for "+signature);
					return nullFSANavigator;
				} else {
					try {
						fsa = mf.getInteractionModel(signature);
					} catch (ModelsFetcherException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return nullFSANavigator;
					}
				}
			} catch (ModelsFetcherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return nullFSANavigator;
			}
		
			automata.put(signature, fsa);
		}
		
		if ( dfa ){
			return new DFAFSANavigator(fsa, signature);
		} else {
			return new NFAFSANavigator(fsa, signature);
		}
	}
	
	private static void logViolation(long threadId, String invokedMethod, FSANavigator currentFSA, boolean finalState) {
		String invokingMethod = currentFSA.getCorrespondingMethod();
		Set<State> statesSet = currentFSA.getCurrentStates();
		State[] states = statesSet.toArray(new State[statesSet.size()]);
	
		logViolation(threadId, invokingMethod, invokedMethod, states, finalState);
	}
	
	private static void logViolation(long threadId, String invokingMethod, String invokedMethod,  State[] states, boolean finalState) {
		ViolationsRecorder recorder = RecorderFactory.getViolationsRecorder();
		
		
		
		
		try {
			if (finalState) {
				recorder.recordInteractionViolation(-1, invokingMethod,invokedMethod,states,ViolationsRecorder.InteractionViolationType.unexpectedTermination, Thread.currentThread().getStackTrace());
			} else {
				recorder.recordInteractionViolation(-1, invokingMethod,invokedMethod,states,ViolationsRecorder.InteractionViolationType.illegalTransition, Thread.currentThread().getStackTrace());
			}
		} catch ( RecorderException e ) {
			//TODO: add a logger
			System.err.println("Error recording interaction violation");
		}
	}

	public void checkExit(Object calledObject, long threadId, String signature) {
		//System.err.println("EXIT: "+calledObject+" "+signature);
		
		
		Map<Integer, ExecutionData> threadStack = stack.get();
		
		if ( calledObject == null ){
			return;
		}
		
		int objectHash = System.identityHashCode(calledObject);
		
		ExecutionData data = threadStack.get(objectHash);
		
		if ( data == null ){
			data = new ExecutionData(getFSANavigator(calledObject.getClass().getCanonicalName()),threadId,signature);
			threadStack.put(objectHash,data);	
		}
		
		
		
		FSANavigator currentFSANavigator = data.navigator;
		if ( currentFSANavigator.isValidState() ){
			boolean accepted = currentFSANavigator.newEvent(signature);
			if ( ( ! accepted ) ){
				data.setAnomalous(true);
				//System.err.println("NOT ACCEPTED "+signature);
				logViolation(threadId,signature,currentFSANavigator,false);
			}
		}
		
		
		data.invocations.add(signature);
		
		
		//threadStack.push(new ExecutionData(getFSANavigator(signature)));
		
		
		
		
//		Map<Integer, ExecutionData> threadStack = stack.get();
//		
//		
//		int hash = System.identityHashCode(calledObject);
//		ExecutionData executionData = threadStack.get(hash);
//		
//		FSANavigator navigator = executionData.navigator;
//		
//		if ( navigator.isValidState() && ( ! navigator.finalState() ) && ( ! fineAnalysisEnabled ) ){
//			logViolation(threadId, signature, executionData.navigator, true);
//		}
//		
//		if ( ( ! navigator.isValidState() ) && recordAnomalousSequences ){
//			logAnomalousSequence(executionData);
//		}
//		
//		if ( ( ! navigator.isValidState() ) && fineAnalysisEnabled ){
//			runFineViolationAnalysis(executionData);
//		}
		
	}
	
	private void runFineViolationAnalysis(ExecutionData executionData) {
		int kbehaviorK = EnvironmentalSetter.getInferenceEngineSettings().getMinTrustLen();
		
		KBehaviorFSAExtender extender = new KBehaviorFSAExtender(4);
		
		FiniteStateAutomaton original = executionData.navigator.getFSA();
		
		FiniteStateAutomaton fsa = (FiniteStateAutomaton) original.clone();
		
		Trace t = new VectorTrace();
		for ( String invocation : executionData.invocations ){
			t.addSymbol( invocation );	
		}
		
		List<AutomataExtension> extensions = extender.extendFSA(fsa, t);

		HashMap<String,State> statesMap = new HashMap<String, State>();
		for (  State state : fsa.getStates() ){
			statesMap.put(state.getName(), state);
		}
		
		for ( AutomataExtension extension : extensions ){
			
			logExtension( extension, executionData, statesMap );
			
		}
		
	}

	private void logExtension(
			AutomataExtension extension,
			ExecutionData executionData, HashMap<String, State> statesMap) {
		ViolationsRecorder recorder = RecorderFactory.getViolationsRecorder();
		
			
		List<String> anomalousEvents = extension.getExtensionEvents();
		String[] events = anomalousEvents.toArray(new String[anomalousEvents.size()]);
		
		String invokingMethod = executionData.navigator.getCorrespondingMethod();
		
		
		
		State[] states = new State[1]; 
		states[0] = statesMap.get(extension.getFromState());
		
		AutomataExtensionType extensionType = extension.getExtensionType();
		
		InteractionViolationType violType = null;
		String invokedMethod = null;
		if ( extensionType == AutomataExtensionType.Branch ){
			violType = ViolationsRecorder.InteractionViolationType.illegalInvocationSequence;
			invokedMethod  = events[0];
		} else if ( extensionType == AutomataExtensionType.Tail){
			violType = ViolationsRecorder.InteractionViolationType.illegalTerminationSequence;
			invokedMethod = events[0];
		} else if ( extensionType == AutomataExtensionType.FinalState){
			violType = ViolationsRecorder.InteractionViolationType.unexpectedTermination;
		}
		
		try {
			recorder.recordInteractionViolation(-1, invokingMethod,invokedMethod,states,violType, Thread.currentThread().getStackTrace(), events, extension.getToState(), extension.getExtensionPosition() );
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void logAnomalousSequence(ExecutionData executionData) {
		ViolationsRecorder recorder = RecorderFactory.getViolationsRecorder();
		
		try {
			
			
			recorder.recordAnomalousCallSequence(executionData.navigator.getCorrespondingMethod(), 
					executionData.invocations, 
					RuntimeContextualDataUtil.retrieveCurrentActions(),
					RuntimeContextualDataUtil.retrieveCurrentTestCases(),
					Thread.currentThread().getStackTrace() );
		} catch (ViolationsRecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Error recording anomalous sequence");
		}
	}



	public void init(InteractionCheckerSettings settings) {
		FineInteractionCheckerSettings mySettings = new FineInteractionCheckerSettings(settings);
		dfa = mySettings.isDFA();
			
		//System.out.println("AS "+recordAnomalousSequences);
		//System.out.println("FE "+fineAnalysisEnabled);
	}

	@Override
	public void init(ConfigurationSettings opts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordInteractionEnter(String methodSignature, long threadId)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordInteractionEnterMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordInteractionExit(String methodSignature, long threadId)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordInteractionExitMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoEnter(String methodSignature, Handler[] parameters)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoEnterMeta(String methodSignature, Handler[] parameters,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoExit(String methodSignature, Handler[] parameters)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoExit(String methodSignature, Handler[] parameters,
			Handler returnValue) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoExitMeta(String methodSignature, Handler[] parameters,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoExitMeta(String methodSignature, Handler[] parameters,
			Handler returnValue, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");	
	}

	@Override
	public void recordIoInteractionEnter(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoInteractionEnterMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoInteractionEnterMeta(Object calledObject,
			String methodSignature, Handler[] parameters, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		checkEnter(calledObject, threadId, methodSignature);
	}

	@Override
	public void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		throw new RecorderException("Not implemented");
	}

	@Override
	public void recordIoInteractionExitMeta(Object calledObject,
			String methodSignature, Handler[] parameters, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		checkExit(calledObject, threadId, methodSignature);
	}

	@Override
	public void recordIoInteractionExitMeta(Object calledObject,
			String methodSignature, Handler[] parameters, Handler returnValue,
			long threadId, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		
		checkExit(calledObject, threadId, methodSignature);
	}

	@Override
	public void newExecution(String execution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordGenericProgramPoint(String programPointName,
			Handler[] variables, long threadId) throws RecorderException {
		throw new NotImplementedException();
	}
	
	@Override
	public void recordAdditionalInfoToLast(Handler additionalData)
			throws RecorderException {
		//TODO: could be enough to write to the current file, but need to deal with the offset
		throw new RecorderException("Not implemented: recordAdditionalInfoToLast");
	}

}
