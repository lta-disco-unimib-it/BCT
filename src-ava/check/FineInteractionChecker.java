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
import java.util.Set;
import java.util.Stack;

import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import oracle.DFAFSANavigator;
import oracle.FSANavigator;
import oracle.FSANavigatorTrue;
import oracle.NFAFSANavigator;
import recorders.RecorderException;
import recorders.RecorderFactory;
import recorders.ViolationsRecorder;
import recorders.ViolationsRecorderException;
import recorders.ViolationsRecorder.InteractionViolationType;
import util.RuntimeContextualDataUtil;
import automata.State;
import automata.fsa.FiniteStateAutomaton;
import conf.EnvironmentalSetter;
import conf.FineInteractionCheckerSettings;
import conf.InteractionCheckerSettings;

public class FineInteractionChecker implements InteractionChecker {
	
	private HashMap<String,FiniteStateAutomaton> automata = new HashMap<String, FiniteStateAutomaton>();
	private boolean dfa;
	private AutomataViolationsAnalyzer analyzer;
	private static final FSANavigator nullFSANavigator = new FSANavigatorTrue();
	
	private boolean fineAnalysisEnabled;
	private boolean recordAnomalousSequences;
	
	private static class ExecutionData {
		List<String> invocations = new ArrayList<String>();
		FSANavigator navigator;
		


		public ExecutionData ( FSANavigator navigator ){
			this.navigator = navigator;
		}
		
		public void addMethodCall( String method ){
			invocations.add(method);
		}
		
		
	}
	
	private static ThreadLocal<Stack<ExecutionData>> stack = new ThreadLocal<Stack<ExecutionData>>(){

		@Override
		protected Stack<ExecutionData> initialValue() {
			Stack<ExecutionData> _stack = new Stack<ExecutionData>();
			
			return _stack;
		}
		
	};
	
	@Override
	public void checkEnter(int callId, long threadId, String signature) {
		Stack<ExecutionData> threadStack = stack.get();
		
		if ( threadStack.size() == 0 ){
			threadStack.push(new ExecutionData(getFSANavigator(signature)));	
			return;
		}
		
		
		ExecutionData data = threadStack.peek();
		
		FSANavigator currentFSANavigator = data.navigator;
		if ( currentFSANavigator.isValidState() ){
			boolean accepted = currentFSANavigator.newEvent(signature);
			if ( ( ! accepted ) && ( ! fineAnalysisEnabled ) ){
				logViolation(callId, threadId,signature,currentFSANavigator,false);
			}
		}
		
		if ( recordAnomalousSequences || fineAnalysisEnabled ){
			data.invocations.add(signature);
		}
		
		threadStack.push(new ExecutionData(getFSANavigator(signature)));
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
	
	private void logViolation(int callId, long threadId, String invokedMethod, FSANavigator currentFSA, boolean finalState) {
		String invokingMethod = currentFSA.getCorrespondingMethod();
		Set<State> statesSet = currentFSA.getCurrentStates();
		State[] states = statesSet.toArray(new State[statesSet.size()]);
	
		logViolation(callId, threadId, invokingMethod, invokedMethod, states, finalState);
	}
	
	private void logViolation(int callId, long threadId, String invokingMethod, String invokedMethod,  State[] states, boolean finalState) {
		ViolationsRecorder recorder = RecorderFactory.getViolationsRecorder();
		
		
		StackTraceElement[] stack;
		if ( Checker.simulatedStackTrace != null ){
			stack = Checker.simulatedStackTrace;
		} else {
			stack = Thread.currentThread().getStackTrace();
		}
		
		try {
			if (finalState) {
				recorder.recordInteractionViolation(callId, invokingMethod,invokedMethod,states,ViolationsRecorder.InteractionViolationType.unexpectedTermination, stack );
			} else {
				recorder.recordInteractionViolation(callId, invokingMethod,invokedMethod,states,ViolationsRecorder.InteractionViolationType.illegalTransition, stack );
			}
		} catch ( RecorderException e ) {
			//TODO: add a logger
			System.err.println("Error recording interaction violation");
		}
	}

	@Override
	public void checkExit(int callId, long threadId, String signature) {

		Stack<ExecutionData> threadStack = stack.get();
		if ( threadStack.size() == 0 ){
			return;
		}
		
		ExecutionData executionData = threadStack.pop();
		FSANavigator navigator = executionData.navigator;
		
		if ( navigator.isValidState() && ( ! navigator.finalState() ) && ( ! fineAnalysisEnabled ) ){
			logViolation(callId, threadId, signature, executionData.navigator, true);
		}
		
		if ( ( ! navigator.isValidState() ) && recordAnomalousSequences ){
			logAnomalousSequence(executionData);
		}
		
		if ( ( ! navigator.isValidState() ) && fineAnalysisEnabled ){
			runFineViolationAnalysis(executionData);
		}
		
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
			
			logExtension( -1, extension, executionData, statesMap );
			
		}
		
	}

	private void logExtension(
			int callId,
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
			recorder.recordInteractionViolation(callId, invokingMethod,invokedMethod,states,violType, Thread.currentThread().getStackTrace(), events, extension.getToState(), extension.getExtensionPosition() );
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void logAnomalousSequence(ExecutionData executionData) {
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
		
		AvaConfiguration conf = AvaConfigurationFactory.createDefaultAvaConfiguration(mySettings.getAVAPathLen());
		
		analyzer = new AutomataViolationsAnalyzer( conf );
		
		
		
		fineAnalysisEnabled = mySettings.isFineAnalysisEnabled();
		
		recordAnomalousSequences = mySettings.isAnomalousSequencesRecordingEnabled();
		
		//System.out.println("AS "+recordAnomalousSequences);
		//System.out.println("FE "+fineAnalysisEnabled);
	}

	@Override
	public void checkProgramPoint(int callId, int threadId, String signature) {
		// TODO Auto-generated method stub
		Stack<ExecutionData> threadStack = stack.get();
		
		if ( threadStack.size() == 0 ){
			threadStack.push(new ExecutionData(getFSANavigator(signature)));	
			return;
		}
		
		
		ExecutionData data = threadStack.peek();
		
		FSANavigator currentFSANavigator = data.navigator;
		if ( currentFSANavigator.isValidState() ){
			boolean accepted = currentFSANavigator.newEvent(signature);
			if ( ( ! accepted ) && ( ! fineAnalysisEnabled ) ){
				logViolation(callId, threadId,signature,currentFSANavigator,false);
			}
		}
		
		if ( recordAnomalousSequences || fineAnalysisEnabled ){
			data.invocations.add(signature);
		}
	}

}
