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
package check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import conf.InteractionCheckerSettings;

import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import recorders.RecorderException;
import recorders.RecorderFactory;
import recorders.ViolationsRecorder;
import automata.State;
import automata.fsa.FSAConfiguration;
import automata.fsa.FSAStepWithClosureSimulator;
import automata.fsa.FiniteStateAutomaton;

public class InteractionInvariantHandler implements InteractionChecker {

	private static HashMap<String,FiniteStateAutomaton> automata = new HashMap<String, FiniteStateAutomaton>();
	
	private static Map threadMap = new Hashtable();

	private static FiniteStateAutomaton nullAutomaton = new FiniteStateAutomaton();

	private static ArrayList nullConfiguration = new ArrayList();

	private static String nullString = "";

	public interface MethodCallType {
		public final String IN = "in";

		public final String OUT = "out";

		public String getExtension();
	}

	private static final class EnterMethod implements MethodCallType {

		public String getExtension() {
			return "in";
		}
	}

	private static final class ExitMethod implements MethodCallType {

		public String getExtension() {
			return "out";
		}
	}

	public static final MethodCallType ENTER = new EnterMethod();

	public static final MethodCallType EXIT = new ExitMethod();

	private static void _initThreadStack(long threadID, String signature) {
		
		Stack stack = new Stack();
		try {
			
		
			FiniteStateAutomaton f = getFSA( signature );
			
			if ( f != null ){
				FSAConfiguration startConfiguration = new FSAConfiguration(f
						.getInitialState(), null, null, null);
				ArrayList startConfigurations = new ArrayList();
				startConfigurations.add(startConfiguration);
				stack.push(signature);
				stack.push(startConfigurations);
				stack.push(f);
			} else {
				stack.push(nullString);
				stack.push(nullConfiguration);
				stack.push(nullAutomaton);
			}
		} catch (Exception e) {
			stack.push(nullString);
			stack.push(nullConfiguration);
			stack.push(nullAutomaton);
		}
		
			threadMap.put(new Long(threadID), stack);
			return;
		
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
	private static FiniteStateAutomaton getFSA(String signature) throws ModelsFetcherException {
		//System.out.println("BCTLOG getFSA : "+signature+" "+automata.size());
		if ( automata.containsKey(signature) ){
			return automata.get(signature);
		}
		
		ModelsFetcher mf = ModelsFetcherFactoy.modelsFetcherInstance;
		FiniteStateAutomaton f;
		if ( ! mf.interactionModelExist(signature) ){
			f = null;
		} else {
			f = mf.getInteractionModel(signature);
		}
		
		automata.put(signature, f);
		
		return f;
	}

	private void _checkCurrentTransition(int callId, Stack s, String signature) {
		FiniteStateAutomaton f = null;
		ArrayList previousConfigurations = null;
		ArrayList reachedConfigurations = null;

		f = (FiniteStateAutomaton) s.pop();
		previousConfigurations = (ArrayList) s.pop();
		String prevMethod = (String) s.pop();
		reachedConfigurations = new ArrayList();

		if (f == nullAutomaton) {
			//System.out.println("NullAutomaton extracted. Current transition is "+ signature);
		} else {
			FSAStepWithClosureSimulator simulator = new FSAStepWithClosureSimulator(
					f);

			for (int i = 0; i < previousConfigurations.size(); i++) {
				FSAConfiguration previousConfiguration = (FSAConfiguration) previousConfigurations
						.get(i);
				previousConfiguration.setUnprocessedInput(signature);
				reachedConfigurations.addAll(simulator.stepConfiguration(previousConfiguration));
			}
			if (reachedConfigurations.isEmpty()) {
				logViolation(callId, prevMethod, f, previousConfigurations, signature, ENTER );
			}
		}
		s.push(prevMethod);
		s.push(reachedConfigurations);
		s.push(f);
	}

	private void logViolation(int callId, String method, FiniteStateAutomaton fsa,
			ArrayList configurations, String invokedMethod, MethodCallType call) {

		ViolationsRecorder recorder = RecorderFactory.getViolationsRecorder();
		
		if (fsa.equals(nullAutomaton))
			return;
		
		CurrentViolationsMaintainer vm = CurrentViolationsMaintainer.getInstance();

		//If this fsa for the current method execution was already violated do not report the violation
		if ( vm.isFSAViolated(fsa) ){
			return;
		}
		
		vm.addViolatedFSA(fsa);
		
		State state[];
		if (configurations.size() <= 0) {
			state = new State[1];
			state[0] = fsa.getInitialState();
		} else {
			state = new State[configurations.size()];
			for (int i = 0; i < configurations.size(); i++) {
				FSAConfiguration configuration = (FSAConfiguration) configurations
						.get(i);
				state[i] = configuration.getCurrentState();
			}
		}
		
		StackTraceElement[] stack;
		if ( Checker.simulatedStackTrace != null ){
			stack = Checker.simulatedStackTrace;
		} else {
			stack = Thread.currentThread().getStackTrace(); 
		}
		
		
		try {
			if (call.getExtension().equals(MethodCallType.IN)) {
				recorder.recordInteractionViolation(callId, method,invokedMethod,state,ViolationsRecorder.InteractionViolationType.illegalTransition, stack );
			} else {
				recorder.recordInteractionViolation(callId, method,invokedMethod,state,ViolationsRecorder.InteractionViolationType.unexpectedTermination, stack );
			}
		} catch ( RecorderException e ) {
			//TODO: add a logger
			System.err.println("Error recording interaction violation");
		}

	}

	private static void printStackTrace(StackTraceElement[] stack) {
		boolean foundJoinPoint = false;
		System.out.println("***TRACE:");
		for (int i = 0; i < stack.length; i++) {
			if ((foundJoinPoint == false)
					&& ((stack[i].getClassName().indexOf("___AW_JoinPoint") != -1) || (stack[i]
							.getClassName().indexOf("$_AW_$") != -1))) {
				foundJoinPoint = true;
			}
			if ((foundJoinPoint == true)
					&& (stack[i].getMethodName().indexOf("$_AW_$") == -1)
					&& (stack[i].getClassName().indexOf("___AW_JoinPoint") == -1)) {
				System.out.println(stack[i].getClassName() + "."
						+ stack[i].getMethodName() + ":"
						+ stack[i].getLineNumber());
			}
		}
	}

	private static void _loadNextFsa(Stack s, String signature) {
		try {
			
			FiniteStateAutomaton f = getFSA(signature);
			
			if ( f != null ){
				FSAConfiguration startConfiguration = new FSAConfiguration(f
						.getInitialState(), null, null, null);
				ArrayList startConfigurations = new ArrayList();
				startConfigurations.add(startConfiguration);
				s.push(signature);
				s.push(startConfigurations);
				s.push(f);
			} else {
				s.push(nullString);
				s.push(nullConfiguration);
				s.push(nullAutomaton);
				
				//FIXME: record missing models
			}
		} catch (Exception e) {
			s.push(nullString);
			s.push(nullConfiguration);
			s.push(nullAutomaton);
		}
	}




	@Override
	public void checkEnter( int callId, long threadID,	String signature) {
//		BctFileLogger.getInstance().log("#"+threadID+"ENTER "+signature);
//		for ( StackTraceElement element : Thread.currentThread().getStackTrace() )
//			System.out.println("#"+threadID+"STACK +"+element.getClassName()+"."+element.getMethodName()+"."+element.getLineNumber());
//		
//		System.out.println(threadID+"#"+signature+"B#");

		Stack s = (Stack) threadMap.get(new Long(threadID));
		if (s == null)
			_initThreadStack(threadID, signature);
		else {
			_checkCurrentTransition(callId, s, signature);
			_loadNextFsa(s, signature);
		}
	}

	@Override
	public void checkExit( int callId, long threadID, String signature) {

//		BctFileLogger.getInstance().log("#"+threadID+"EXIT "+signature);
//		for ( StackTraceElement element : Thread.currentThread().getStackTrace() )
//			System.out.println("#"+threadID+"STACK +"+element.getClassName()+"."+element.getMethodName()+"."+element.getLineNumber());
//		
		
//		System.out.println(threadID+"#"+signature+"E#");
		Stack s = (Stack) threadMap.get( Long.valueOf(threadID));

		FiniteStateAutomaton fsa = (FiniteStateAutomaton) s.pop();
		ArrayList previousConfigurations = (ArrayList) s.pop();
		String method = (String) s.pop();

		//FIXME: was not used, check if checking phas enow is correct
//		FSAStepWithClosureSimulator simulator = new FSAStepWithClosureSimulator(fsa);

		boolean isFinalState = false;

		for (int i = 0; i < previousConfigurations.size(); i++) {

			FSAConfiguration configuration = (FSAConfiguration) previousConfigurations
					.get(i);
			if (fsa.isFinalState(configuration.getCurrentState()))
				isFinalState = true;
		}

		if (!isFinalState) {
			logViolation(callId, method, fsa, previousConfigurations, signature, EXIT );
		}

		if (s.isEmpty()) {
			threadMap.remove(new Long(threadID));
		}
		
		CurrentViolationsMaintainer.getInstance().executionFinished(fsa);
	}

	public void init(InteractionCheckerSettings settings) {
		
	}

	@Override
	public void checkProgramPoint(int callId, int threadID, String programPointId) {
		Stack s = (Stack) threadMap.get(new Long(threadID));
		if (s == null)
			_initThreadStack(threadID, programPointId);
		else {
			_checkCurrentTransition(callId, s, programPointId);

		}
	}
}