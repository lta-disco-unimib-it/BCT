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
package recorders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;
import automata.State;
import conf.ConfigurationSettings;
import failureDetection.Failure;
import recorders.RecorderException;
import recorders.ViolationsRecorderException;
import recorders.ViolationsRecorder;
import recorders.ViolationsRecorder.InteractionViolationType;
import util.RuntimeContextualDataUtil;

public class TestViolationsRecorderStub implements ViolationsRecorder {
	private List<BctAnomalousCallSequence> anomalousCallSequences = new ArrayList<BctAnomalousCallSequence>();
	private List<BctFSAModelViolation> fsaViolations = new ArrayList<BctFSAModelViolation>();

	public List<BctAnomalousCallSequence> getAnomalousCallSequences() {
		return anomalousCallSequences;
	}

	public List<BctFSAModelViolation> getFsaViolations() {
		return fsaViolations;
	}

	private int violations = 0;

	public void init(ConfigurationSettings opts) {

	}

	public void clear(){
		anomalousCallSequences = new ArrayList<BctAnomalousCallSequence>();
		fsaViolations = new ArrayList<BctFSAModelViolation>();
	}

	public void recordAnomalousCallSequence(
			BctAnomalousCallSequence anomalousCallSequence)
	throws ViolationsRecorderException {
		anomalousCallSequences.add(anomalousCallSequence);
	}

	public void recordAnomalousCallSequence(String correspondingMethod,
			List<String> anomalousCallSequence, String[] tests, String[] actions, StackTraceElement[] stElements)
	throws ViolationsRecorderException {
		BctAnomalousCallSequence cs = new BctAnomalousCallSequence(
				System.currentTimeMillis(),
				getNewViolationId(),
				RuntimeContextualDataUtil.retrievePID(),
				RuntimeContextualDataUtil.retrieveStringStackTrace(stElements),
				tests,
				actions,
				String.valueOf(Thread.currentThread().getId()),
				anomalousCallSequence.toArray(new String[anomalousCallSequence.size()]),
				correspondingMethod
		);

		recordAnomalousCallSequence(cs);
	}

	private String getNewViolationId() {
		return String.valueOf(violations ++);
	}

	public void recordFailure(Failure failure) throws RecorderException {

	}

	public void recordInteractionViolation(int callId, String invokingMethod,
			String invokedMethod, State[] state, InteractionViolationType type,
			StackTraceElement[] stElements) throws RecorderException {
		recordInteractionViolation(callId, invokingMethod, invokedMethod, state, type, stElements, null, null, -1);
	}


	public void recordInteractionViolation(int callId, String invokingMethod,
			String invokedMethod, State[] state, InteractionViolationType type,
			StackTraceElement[] stElements, String[] anomalousSequence,
			String toState, int anomalousEventPosition) throws RecorderException {



		String violType;

		if ( type == InteractionViolationType.illegalTransition ){
			violType = BctModelViolation.ViolationType.UNEXPECTED_EVENT;
		} else if ( type == InteractionViolationType.illegalTerminationSequence ){
			violType = BctModelViolation.ViolationType.UNEXPECTED_TERMINATION_SEQUENCE;
		} else if ( type == InteractionViolationType.illegalInvocationSequence ){
			violType = BctModelViolation.ViolationType.UNEXPECTED_INVOCATION_SEQUENCE;
		} else {
			violType = BctModelViolation.ViolationType.UNEXPECTED_TERMINATION;
		}

		BctFSAModelViolation modelViolation = new BctFSAModelViolation(
				getNewViolationId(),
				invokingMethod,
				invokedMethod,
				violType,
				System.currentTimeMillis(),
				RuntimeContextualDataUtil.retrieveCurrentActions(),
				RuntimeContextualDataUtil.retrieveCurrentTestCases(),
				RuntimeContextualDataUtil.retrieveStringStackTrace(stElements),
				RuntimeContextualDataUtil.retrievePID(),
				String.valueOf(Thread.currentThread().getId()),
				retrieveStatesNames(state),
				anomalousSequence,
				toState,
				anomalousEventPosition
		);

		recordInteractionViolation(modelViolation);

	}

	private String[] retrieveStatesNames(State[] states) {
		String sn[] = new String[states.length];

		for ( int i = 0; i < states.length; i++ ){
			sn[i] = states[i].getName();
		}
		return sn;
	}

	public void recordInteractionViolation(BctFSAModelViolation modelViolation)
	throws ViolationsRecorderException {
		fsaViolations.add(modelViolation);
	}

	public void recordIoViolation(BctIOModelViolation modelViolation)
	throws ViolationsRecorderException {

	}

	public void recordIoViolationEnter(int callId, String signature, String expression,
			boolean result, Object[] argumentValues, Object returnValue,
			StackTraceElement[] stElements, Map<String,Object> localVariables) throws RecorderException {

	}

	public void recordIoViolationExit(int callId, String signature, String expression,
			boolean result, Object[] argumentValues, Object returnValue,
			StackTraceElement[] stElements, Map<String,Object> localVariables, HashMap origValues)
	throws RecorderException {

	}



}
