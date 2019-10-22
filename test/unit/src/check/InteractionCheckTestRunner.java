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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import it.unimib.disco.lta.alfa.inferenceEngines.FSAExtensionsRecorder.FSAExtension;
import it.unimib.disco.lta.ava.automataExtension.AutomataExtension;
import it.unimib.disco.lta.ava.automataExtension.AutomataExtensionType;
import it.unimib.disco.lta.ava.automataExtension.AutomataExtension.StateType;
import automata.State;
import automata.Transition;
import automata.fsa.FiniteStateAutomaton;
import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;

public class InteractionCheckTestRunner {

	public static final long SIMPLE_CHECK_THREAD = 5;
	
	public static class AutomataExtensions {
		public static final AutomataExtension simpleCheckNewFinal = createSimpleCheckNewFinalExtension();
		public static final AutomataExtension simpleCheckNewBranch = createSimpleCheckNewBranchExtension();
		public static final AutomataExtension simpleCheckNewTail = createSimpleCheckNewTailExtension();
	}
	
	private static AutomataExtension createSimpleCheckNewTailExtension(){
		ArrayList<String> extensionEvents = new ArrayList<String>();
		extensionEvents.add("Simple.methodC()");
		extensionEvents.add("Simple.methodD()");
		extensionEvents.add("Simple.methodE()");
		
		ArrayList<String> expectedEvents = new ArrayList<String>();
		expectedEvents.add("Simple.method80()");
		
		AutomataExtension simpleCheckNewFinal = new AutomataExtension(
				AutomataExtensionType.Tail, 
				80,
				80,
				"q80", 
				StateType.Existing,
				extensionEvents,
				extensionEvents,
				null, 
				0, 
				expectedEvents,
				expectedEvents		
		);
		return simpleCheckNewFinal;
	}
	
	private static AutomataExtension createSimpleCheckNewFinalExtension(){
		ArrayList<String> extensionEvents = new ArrayList<String>();
		extensionEvents.add("Simple.method70()");
		
		ArrayList<String> expectedEvents = new ArrayList<String>();
		expectedEvents.add("Simple.method99()");
		
		AutomataExtension simpleCheckNewFinal = new AutomataExtension(
				AutomataExtensionType.FinalState, 
				70,
				70,
				"q70", 
				StateType.Existing,
				extensionEvents,
				extensionEvents,
				null, 
				0, 
				expectedEvents,
				expectedEvents		
		);
		return simpleCheckNewFinal;
	}
	
	private static AutomataExtension createSimpleCheckNewBranchExtension(){
		ArrayList<String> extensionEvents = new ArrayList<String>();
		extensionEvents.add("Simple.methodA()");
		extensionEvents.add("Simple.methodB()");
		
		ArrayList<String> expectedEvents = new ArrayList<String>();
		expectedEvents.add("Simple.method30()");
		
		AutomataExtension simpleCheckNewFinal = new AutomataExtension(
				AutomataExtensionType.Branch, 
				30,
				30,
				"q30", 
				StateType.Existing,
				extensionEvents,
				extensionEvents,
				"q30", 
				0, 
				expectedEvents,
				expectedEvents		
		);
		return simpleCheckNewFinal;
	}
	
	
	public static void runSimpleCheck(InteractionChecker checker) throws ModelsFetcherException {
		runCheck(checker,TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, SIMPLE_CHECK_THREAD, new ArrayList<AutomataExtension>() );
	}
	
	public static void runSimpleCheck_newFinal(InteractionChecker checker) throws ModelsFetcherException {
		ArrayList<AutomataExtension> extensions = new ArrayList<AutomataExtension>();
		
		extensions.add(AutomataExtensions.simpleCheckNewFinal);
		
		runCheck(checker,TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, SIMPLE_CHECK_THREAD,  extensions );
	}
	
	public static void runSimpleCheck_newBranch(InteractionChecker checker) throws ModelsFetcherException {
		ArrayList<AutomataExtension> extensions = new ArrayList<AutomataExtension>();
		
		extensions.add(AutomataExtensions.simpleCheckNewBranch);
		
		runCheck(checker,TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, SIMPLE_CHECK_THREAD,  extensions );
	}
	

	public static void runSimpleCheck_newTail(InteractionChecker checker) throws ModelsFetcherException {
		ArrayList<AutomataExtension> extensions = new ArrayList<AutomataExtension>();
		
		extensions.add(AutomataExtensions.simpleCheckNewTail );
		
		runCheck(checker,TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, SIMPLE_CHECK_THREAD,  extensions );
		
		
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void runCheck( InteractionChecker checker, String mainMethodSignature, long threadId, List<AutomataExtension> changes) throws ModelsFetcherException {
		
		HashMap<String,AutomataExtension> changesMap = new HashMap<String, AutomataExtension>();
		for ( AutomataExtension change : changes ){
			changesMap.put(change.getFromState(), change);
		}
		
		ModelsFetcher instance = ModelsFetcherFactoy.modelsFetcherInstance;
		
		//get simple main method
		FiniteStateAutomaton mainFSA = instance.getInteractionModel(mainMethodSignature);
		checker.checkEnter( -1, threadId, mainMethodSignature );
		
		State state = mainFSA.getInitialState();
		

		state.hashCode();
		Transition[] ts = mainFSA.getTransitionsFromState(state);
		
		int count = 0;
		
		while ( ts.length != 0 ){
			count++;
			if ( changesMap.containsKey(state.getName()) ){
				AutomataExtension change = changesMap.remove(state.getName());
				state = applyChange(checker,change,mainFSA,threadId);
			} else {
				
				runCheck(checker,ts[0].getDescription(), threadId, changes);
				
				state = ts[0].getToState();
			}
		
			if ( state == null ){
				break;
			}
			
			ts = mainFSA.getTransitionsFromState(state);
			
		}
		checker.checkExit(-1, threadId, mainMethodSignature );
	}

	private static State applyChange(InteractionChecker checker, AutomataExtension change, FiniteStateAutomaton fsa, long threadId) throws ModelsFetcherException {
		if ( change.getExtensionType() == AutomataExtensionType.Branch ){
			
			List<String> anomalousEvents = change.getExtensionEvents();
			for ( String event : anomalousEvents ){
				
				runCheck(checker,event, threadId, new ArrayList<AutomataExtension>(0));
				
			}
			
			String toStateName = change.getToState();
			
			return getState(fsa,toStateName);
			
		} else if ( change.getExtensionType() == AutomataExtensionType.Tail ){
			List<String> anomalousEvents = change.getExtensionEvents();
			for ( String event : anomalousEvents ){
				
				runCheck(checker,event, threadId, new ArrayList<AutomataExtension>(0));
				
			}
			
			//return null to block the simulation: we end in a tail
			return null;
		} else if ( change.getExtensionType() == AutomataExtensionType.FinalState ){
			//return null to block the simulation: we have reached a new final state
			return null;
		}
		return null;
	}
	
	
	
	
	public static void createSequence( HashMap<String,List<String>> tracesMap, String mainMethodSignature, List<AutomataExtension> changes) throws ModelsFetcherException {
		List<String> calls = new ArrayList<String>();
		
		HashMap<String,AutomataExtension> changesMap = new HashMap<String, AutomataExtension>();
		for ( AutomataExtension change : changes ){
			changesMap.put(change.getFromState(), change);
		}
		
		ModelsFetcher instance = ModelsFetcherFactoy.modelsFetcherInstance;
		
		//get simple main method
		FiniteStateAutomaton mainFSA = instance.getInteractionModel(mainMethodSignature);
		
		
		State state = mainFSA.getInitialState();
		

		state.hashCode();
		Transition[] ts = mainFSA.getTransitionsFromState(state);

		
		int count = 0;
		
		while ( ts.length != 0 ){
			count++;
			if ( changesMap.containsKey(state.getName()) ){
				AutomataExtension change = changesMap.remove(state.getName());
				
				state = createSequenceChange(tracesMap,calls,change,mainFSA);
				
			} else {
				calls.add(ts[0].getDescription());
				createSequence(tracesMap,ts[0].getDescription(), changes);
				
				state = ts[0].getToState();
			}
		
			if ( state == null ){
				break;
			}
			
			ts = mainFSA.getTransitionsFromState(state);
			
		}
		
		tracesMap.put(mainMethodSignature, calls);
	}

	private static State createSequenceChange( HashMap<String,List<String>> tracesMap, List<String> sequence, AutomataExtension change, FiniteStateAutomaton fsa) throws ModelsFetcherException {
		if ( change.getExtensionType() == AutomataExtensionType.Branch ){
			
			List<String> anomalousEvents = change.getExtensionEvents();
			for ( String event : anomalousEvents ){
				sequence.add(event);
				createSequence(tracesMap,event, new ArrayList<AutomataExtension>(0));
				
			}
			
			String toStateName = change.getToState();
			
			return getState(fsa,toStateName);
			
		} else if ( change.getExtensionType() == AutomataExtensionType.Tail ){
			List<String> anomalousEvents = change.getExtensionEvents();
			for ( String event : anomalousEvents ){
				sequence.add(event);
				createSequence(tracesMap,event, new ArrayList<AutomataExtension>(0));
				
			}
			
			//return null to block the simulation: we end in a tail
			return null;
		} else if ( change.getExtensionType() == AutomataExtensionType.FinalState ){
			//return null to block the simulation: we have reached a new final state
			return null;
		}
		return null;
	}
	
	
	

	
	

	private static State getState(FiniteStateAutomaton fsa, String toStateName) {
		for ( State s : fsa.getStates() ){
			if ( s.getName().equals(toStateName) ){
				return s;
			}
		}
		
		return null;
	}

	public static HashMap<String, List<String>> getAnomalousSequence_SimpleCheck_newFinal() throws ModelsFetcherException {
		HashMap<String, List<String>> anomalousSequences = new HashMap<String, List<String>>();
		List<AutomataExtension> changes = new ArrayList<AutomataExtension>();
		changes.add(AutomataExtensions.simpleCheckNewFinal);
		
		createSequence(anomalousSequences, TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, changes );
		
		return anomalousSequences;
	}

	public static List<List<String>> getUnexpectedSequence_SimpleCheck_newFinal() throws ModelsFetcherException {
		List<List<String>> changes = new ArrayList<List<String>>();
		
		changes.add(AutomataExtensions.simpleCheckNewFinal.getExtensionEvents());
		
		return changes;
	}

	public static HashMap<String, List<String>> getAnomalousSequence_SimpleCheck_newBranch() throws ModelsFetcherException {
		HashMap<String, List<String>> anomalousSequences = new HashMap<String, List<String>>();
		List<AutomataExtension> changes = new ArrayList<AutomataExtension>();
		changes.add(AutomataExtensions.simpleCheckNewBranch);
		
		createSequence(anomalousSequences, TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, changes );
		
		return anomalousSequences;
	}

	public static List<List<String>> getUnexpectedSequence_SimpleCheck_newBranch() throws ModelsFetcherException {
		List<List<String>> changes = new ArrayList<List<String>>();
		
		changes.add(AutomataExtensions.simpleCheckNewBranch.getExtensionEvents());
		
		return changes;
	}

	public static HashMap<String, List<String>> getAnomalousSequence_SimpleCheck_newTail() throws ModelsFetcherException {
		HashMap<String, List<String>> anomalousSequences = new HashMap<String, List<String>>();
		List<AutomataExtension> changes = new ArrayList<AutomataExtension>();
		changes.add(AutomataExtensions.simpleCheckNewTail);
		
		createSequence(anomalousSequences, TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, changes );
		
		return anomalousSequences;
	}

	public static List<List<String>> getUnexpectedSequence_SimpleCheck_newTail() {
		List<List<String>> changes = new ArrayList<List<String>>();
		
		changes.add(AutomataExtensions.simpleCheckNewTail.getExtensionEvents());
		
		return changes;
	}

	public static void runSimpleCheck_newBranch_newTail(
			InteractionChecker checker) throws ModelsFetcherException {
		ArrayList<AutomataExtension> extensions = new ArrayList<AutomataExtension>();
		
		extensions.add(AutomataExtensions.simpleCheckNewBranch );
		
		extensions.add(AutomataExtensions.simpleCheckNewTail );
		
		runCheck(checker,TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, SIMPLE_CHECK_THREAD,  extensions );
	}

	public static HashMap<String, List<String>> getAnomalousSequence_SimpleCheck_newBranch_newTail() throws ModelsFetcherException {
		HashMap<String, List<String>> anomalousSequences = new HashMap<String, List<String>>();
		List<AutomataExtension> changes = new ArrayList<AutomataExtension>();
		changes.add(AutomataExtensions.simpleCheckNewBranch);
		changes.add(AutomataExtensions.simpleCheckNewTail);
		
		createSequence(anomalousSequences, TestModelsFetcherStub.SIMPLE_MAIN_SIGNATURE, changes );
		
		return anomalousSequences;
	}

	public static List<List<String>> getUnexpectedSequence_SimpleCheck_newBranch_newTail() {
		List<List<String>> changes = new ArrayList<List<String>>();
		
		changes.add(AutomataExtensions.simpleCheckNewBranch.getExtensionEvents());
		changes.add(AutomataExtensions.simpleCheckNewTail.getExtensionEvents());
		
		return changes;
	}

}
