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
package recorders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import automata.State;
import conf.ConfigurationSettings;
import failureDetection.Failure;



/**
 * This class handles violations 
 * 
 * @author Fabrizio Pastore fabrizio.pastore[AT]gmail.com
 *
 */
public interface ViolationsRecorder {

	public enum InteractionViolationType {
		illegalTransition,
		illegalInvocationSequence,
		illegalTerminationSequence,
		unexpectedTermination,
	}

	public static class IoViolationType {
		public static final IoViolationType enter = new Enter();
		public static final IoViolationType exit = new Exit();
		
		private static class Enter extends IoViolationType{
			
		}
		
		private static class Exit extends IoViolationType{
			
		}
	}
	
	
	
	
	public void init( ConfigurationSettings opts);

	public void recordIoViolationEnter(int callId, String signature, String expression, boolean result, Object[] argumentValues, Object returnValue, StackTraceElement[] stElements, Map<String, Object> localVariables) throws RecorderException;
	
	public void recordIoViolationExit(int callId, String signature, String expression, boolean result, Object[] argumentValues, Object returnValue, StackTraceElement[] stElements, Map<String, Object> localVariables, HashMap origValues ) throws RecorderException;
	
	public void recordInteractionViolation(int callId, String invokingMethod, String invokedMethod, State[] state, InteractionViolationType type, StackTraceElement[] stElements) throws RecorderException;
	
	public void recordInteractionViolation(int callId, String invokingMethod, String invokedMethod, State[] state, InteractionViolationType type, StackTraceElement[] stElements, String[] anomalousSequence, String toState, int anomalousEventPosition) throws RecorderException;

	public void recordAnomalousCallSequence( BctAnomalousCallSequence anomalousCallSequence ) throws ViolationsRecorderException;
	
	/**
	 * Records the given FSA Model Violation
	 * @param modelViolation
	 * @throws ViolationsRecorderException
	 */
	public void recordInteractionViolation(BctFSAModelViolation modelViolation)
			throws ViolationsRecorderException;

	/**
	 * This method records a BctModelViolation object
	 * 
	 * @param modelViolation
	 * @throws ViolationsRecorderException 
	 */
	public void recordIoViolation(BctIOModelViolation modelViolation)
			throws ViolationsRecorderException;
	
	public void recordFailure( Failure failure ) throws RecorderException;

	public void recordAnomalousCallSequence(String correspondingMethod, List<String> invocations, String[] currentActions, String[] currentTests, StackTraceElement[] stElements) throws ViolationsRecorderException;
}
