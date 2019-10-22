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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;

import recorders.ViolationsRecorder.InteractionViolationType;

import util.RuntimeContextualDataUtil;
import util.cbe.AnomalousCallSequencesExporter;
import util.cbe.FailuresExporter;
import util.cbe.ModelViolationsExporter;
import automata.State;
import failureDetection.Failure;

public abstract class CBEViolationsRecorder implements ViolationsRecorder {
	


	public synchronized void recordAnomalousCallSequence(
			String invokingMethod,
			List<String> anomalousCallSequence,
			String[] currentActions,
			String[] currentTests,
			StackTraceElement[] stElements) throws ViolationsRecorderException {
		
		BctAnomalousCallSequence cs = new BctAnomalousCallSequence(
				System.currentTimeMillis(),
				getNewViolationId(-1),
				RuntimeContextualDataUtil.retrievePID(),
				RuntimeContextualDataUtil.retrieveStringStackTrace(stElements),
				currentActions, currentTests,
				String.valueOf(Thread.currentThread().getId()), 
				anomalousCallSequence.toArray(new String[anomalousCallSequence.size()]),
				invokingMethod
				);
		
		recordAnomalousCallSequence ( cs );
	}



	public synchronized  void recordAnomalousCallSequence(BctAnomalousCallSequence anomalousCallSequence ) throws ViolationsRecorderException {
		CommonBaseEvent event = exporter.createCBEAnomalousCallSequence(anomalousCallSequence);
		recordEvent(event);
	}

	private AnomalousCallSequencesExporter exporter = new AnomalousCallSequencesExporter();
	private ModelViolationsExporter modelViolationsExporter = new ModelViolationsExporter();
	private int vcount = 0;
	private FailuresExporter failureExporter = new FailuresExporter();


	public synchronized void recordInteractionViolation(int callId, String invokingMethod,
			String invokedMethod, State[] state, InteractionViolationType type,
			StackTraceElement[] stElements ) throws RecorderException {
		recordInteractionViolation(callId,invokingMethod, invokedMethod, state, type, stElements, null, null, -1);
	}
	
	public synchronized void recordInteractionViolation(int callId,String invokingMethod,
			String invokedMethod, State[] state, InteractionViolationType type,
			StackTraceElement[] stElements, String[] anomalousSequence,
			String toState, int anomalousEventPosition ) throws RecorderException {
		
			
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
				getNewViolationId(callId),
				invokingMethod,
				invokedMethod,
				violType,
				System.currentTimeMillis(),
				RuntimeContextualDataUtil.retrieveCurrentActions(),
				RuntimeContextualDataUtil.retrieveCurrentTestCases(),
				RuntimeContextualDataUtil.retrieveStringStackTrace(stElements),
				RuntimeContextualDataUtil.retrievePID(),
				RuntimeContextualDataUtil.retrieveThreadId(),
				retrieveStatesNames(state),
				anomalousSequence,
				toState,
				anomalousEventPosition 
		);
		
		recordInteractionViolation(modelViolation);
		
	}

	/**
	 * Records the given FSA Model Violation
	 * @param modelViolation
	 * @throws ViolationsRecorderException
	 */
	public synchronized void recordInteractionViolation(BctFSAModelViolation modelViolation) throws ViolationsRecorderException{
		
		CommonBaseEvent event = modelViolationsExporter.createCBEFSAModelViolation(modelViolation);
		recordEvent(event);
		
	}
	
	/**
	 * This method given an array of State return an array with the corresponding state names
	 * 
	 * @param states
	 * @return
	 */
	private String[] retrieveStatesNames(State[] states) {
		String sn[] = new String[states.length];
		
		for ( int i = 0; i < states.length; i++ ){
			sn[i] = states[i].getName();
		}
		return sn;
	}

	
	public synchronized void recordIoViolationEnter(int callId, String signature, String expression,
			boolean result, Object[] argumentValues, Object returnValue,
			StackTraceElement[] stElements, Map<String,Object> localVariables) throws RecorderException {
		
		
		
		BctIOModelViolation modelViolation = new BctIOModelViolation(
				getNewViolationId(callId),
				signature,
				BctIOModelViolation.Position.ENTER,
				expression,
				System.currentTimeMillis(),
				RuntimeContextualDataUtil.retrieveCurrentActions(),
				RuntimeContextualDataUtil.retrieveCurrentTestCases(),
				RuntimeContextualDataUtil.retrieveStringStackTrace(stElements),
				RuntimeContextualDataUtil.retrievePID(),
				RuntimeContextualDataUtil.retrieveThreadId(),
				getParameters(result, argumentValues, returnValue, localVariables, null)
				);
		
		recordIoViolation(modelViolation);
	}
	
	public synchronized void recordIoViolation(BctIOModelViolation modelViolation) throws ViolationsRecorderException{
		
		CommonBaseEvent event = modelViolationsExporter.createCBEIoViolation(modelViolation);
		recordEvent(event);
		
	}
	


	private String getNewViolationId(long callId) {
		return ManagementFactory.getRuntimeMXBean().getStartTime()+"@"+getPid()+"@"+callId+":"+String.valueOf(vcount++);
	}



	public synchronized void recordIoViolationExit(int callId,String signature, String expression,
			boolean result, Object[] argumentValues, Object returnValue,
			StackTraceElement[] stElements, Map<String,Object> localVariables, HashMap origValues)
			throws RecorderException {
		
		
		BctIOModelViolation modelViolation = new BctIOModelViolation(
				getNewViolationId(callId),
				signature,
				BctIOModelViolation.Position.EXIT,
				expression,
				System.currentTimeMillis(),
				RuntimeContextualDataUtil.retrieveCurrentActions(),
				RuntimeContextualDataUtil.retrieveCurrentTestCases(),
				RuntimeContextualDataUtil.retrieveStringStackTrace(stElements),
				getPid(),
				RuntimeContextualDataUtil.retrieveThreadId(),
				getParameters(true, argumentValues, returnValue, localVariables, null)
				);
		
		
		recordIoViolation(modelViolation);
		
	
	}


	

	
	private String getPid() {
		return 	RuntimeContextualDataUtil.retrievePID();
	}

	public String getParameters(boolean recordReturnValue, Object[] argumentValues, Object returnValue, Map<String,Object> localVariables, HashMap origValues ) throws RecorderException {
		
		
		
		//save parameters state
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		FileViolationsRecorder.recordParameters(pw, argumentValues);
		if ( recordReturnValue ){
			FileViolationsRecorder.recordReturnvalue(pw, returnValue);
		}
		
		if ( localVariables != null ){
			for ( Entry<String,Object> e : localVariables.entrySet() ){
				pw.write( e.getKey() + "\n");
				pw.write( escape( e.getValue() ) + "\n");
				pw.write( "1\n");
			}
		}
		
		if (  origValues != null ){
			Iterator<String> it = origValues.keySet().iterator();
			while( it.hasNext() ){
				String key = it.next();
				pw.write("orig("+key+") = "+origValues.get(key)+"\n");
			}	
		}
		
		
		pw.close();
		
		return sw.toString();
	
		
	}
	
	private String escape(Object value) {
		if ( value == null ){
			return "null";
		}
		
		if ( value instanceof String ){
			return ((String) value).replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t")
			;
			
			
		}
		
		return value.toString();
	}



	/**
	 * Records the given CBE
	 * 
	 * @param event
	 * @throws ViolationsRecorderException
	 */
	protected abstract void recordEvent(CommonBaseEvent event) throws ViolationsRecorderException;
	
	public synchronized void recordFailure(Failure failure) throws RecorderException {
		CommonBaseEvent cbe = failureExporter.createFailureEvent(failure);
		
		recordEvent(cbe);
	}

	public void recordViolation(BctModelViolation v) throws ViolationsRecorderException {
		if ( v instanceof BctIOModelViolation ){
			recordIoViolation((BctIOModelViolation) v);
		} else if ( v instanceof BctFSAModelViolation ){
			recordInteractionViolation((BctFSAModelViolation) v);
		} else {
			throw new ViolationsRecorderException("Violation type not identified");
		}
	}
	
}
